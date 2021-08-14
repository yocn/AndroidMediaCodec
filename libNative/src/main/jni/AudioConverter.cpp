//
// Created by ZhaoLinlin on 2021/5/18.
//

//#include "share/pch.h"

extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
}

#include <android/log.h>
#include <string>
#include <pthread.h>
#include <mutex>
#include <utility>
#include <memory>
#include <jni.h>

namespace {

    class OutputStream;

    class InputStream;

    class ProcessCallback;

    class AudioConverter {

    public:
        static void initClass(JNIEnv *pEnv, jclass clazz);

        AudioConverter(ProcessCallback *callback, const char *sourcePath, const char *targetPath,
                       const char *format);

        ~AudioConverter() noexcept;

        const char *convert(int index_audio_track);

        void cancel();

    private:
        static const char TAG[];

        std::unique_ptr<OutputStream> target;
        std::unique_ptr<InputStream> inputStream;
    };

    const char AudioConverter::TAG[] = "AudioConverter";


    jmethodID progressMethod = nullptr;

    class ProcessCallback {
    public:
        virtual void onProgress(int progress) = 0;
    };


    class JavaProgressCallback : public ProcessCallback {

    public:
        static void init(JNIEnv *env, jclass clazz) {
            progressMethod = env->GetMethodID(clazz, "onProgress", "(I)V");
        }

        explicit JavaProgressCallback(JNIEnv *env, jobject javaObject) :
                env(env) {
            this->javaObject = env->NewGlobalRef(javaObject);
        }


        ~JavaProgressCallback() {
            env->DeleteGlobalRef(javaObject);
        }

        void onProgress(int progress) override {
            if (progress > 100)
                progress = 100;
            if (progress > lasProgress) {
                lasProgress = progress;
                env->CallVoidMethod(javaObject, progressMethod, progress);
            }
        }

    public:
        JNIEnv *env;
        jobject javaObject;
        int lasProgress = 0;
    public:

    };

    class ConvertException : public std::exception {
    public:
        explicit ConvertException(std::string what) : w(std::move(what)) {

        }

        explicit ConvertException(const char *what) : w(what) {
        }

        const char *what() const noexcept override {
            return w.c_str();
        }

    private:
        std::string w;
    };

    class InputStreamCallback {
    public:
        virtual void onAudioStream(AVCodecContext *codecContext) = 0;

        virtual void onFrame(AVFrame *frame) = 0;

        virtual void onEnd() = 0;
    };

    AVFrame *alloc_audio_frame(enum AVSampleFormat sample_fmt,
                                      uint64_t channel_layout,
                                      int sample_rate, int nb_samples) {
        AVFrame *frame = av_frame_alloc();
        int ret;

        if (!frame) {
//        fprintf(stderr, "Error allocating an audio frame\n");
//        exit(1);
            throw ConvertException("memory error: Error allocating an audio frame");
        }

        frame->format = sample_fmt;
        frame->channel_layout = channel_layout;
        frame->sample_rate = sample_rate;
        frame->nb_samples = nb_samples;

        if (nb_samples) {
            ret = av_frame_get_buffer(frame, 0);
            if (ret < 0) {
//            fprintf(stderr, "Error allocating an audio buffer\n");
//            exit(1);
                throw ConvertException("memory error: Error allocating an audio buffer");
            }
        }

        return frame;
    }

    class AudioFrameBuffer {

    public:
        AVFrame* buffer = {nullptr};
        AVFrame* bufferTmp = {nullptr};
        int SIZE = 1024 * 4;
        int count = 0;

        ~AudioFrameBuffer() {
            if (buffer != nullptr) {
                av_frame_free(&buffer);
                av_frame_free(&bufferTmp);
            }
        }

        void sendFrame(AVFrame* pFrame, int pCount) {
            if (buffer == nullptr) {
                buffer = alloc_audio_frame((enum AVSampleFormat)pFrame->format, pFrame->channel_layout, pFrame->sample_rate, SIZE);
                bufferTmp = alloc_audio_frame((enum AVSampleFormat)pFrame->format, pFrame->channel_layout, pFrame->sample_rate, SIZE);
            }

            av_frame_make_writable(buffer);
            av_frame_make_writable(bufferTmp);

            if (pCount + count > SIZE) {
                SIZE = pCount + count;
                AVFrame * buffer1 = alloc_audio_frame((enum AVSampleFormat)pFrame->format, pFrame->channel_layout, pFrame->sample_rate, SIZE);
                AVFrame* bufferTmp1 = alloc_audio_frame((enum AVSampleFormat)pFrame->format, pFrame->channel_layout, pFrame->sample_rate, SIZE);
                av_frame_make_writable(buffer1);
                av_frame_make_writable(bufferTmp1);

                if (count > 0) {
                    memcpy(buffer1->data[0], buffer->data[0], count * 4);
                    memcpy(buffer1->data[1], buffer->data[1], count * 4);

//                    memcpy(bufferTmp1->data[0], bufferTmp->data[0], count * 4);
//                    memcpy(bufferTmp1->data[1], bufferTmp->data[1], count * 4);
                }

                av_frame_free(&buffer);
                av_frame_free(&bufferTmp);

                buffer = buffer1;
                bufferTmp = bufferTmp1;
//                throw ConvertException("buffer internal error");
            }

            memcpy(buffer->data[0] + count * 4, pFrame->data[0], pCount * 4);
            memcpy(buffer->data[1] + count * 4, pFrame->data[1], pCount * 4);
            count += pCount;
        }

        int receiveFrame(AVFrame* out, int pCount) {
            if (count < pCount)
                return AVERROR(EAGAIN);

            out->nb_samples = pCount;
            memcpy(out->data[0], buffer->data[0], pCount * 4);
            memcpy(out->data[1], buffer->data[1], pCount * 4);

            int remain = count - pCount;
            count = remain;
            if (remain == 0) {
                return 0;
            }

            memcpy(bufferTmp->data[0], buffer->data[0] + pCount * 4, remain * 4);
            memcpy(bufferTmp->data[1], buffer->data[1] + pCount * 4, remain * 4);

            memcpy(buffer->data[0], bufferTmp->data[0], remain * 4);
            memcpy(buffer->data[1], bufferTmp->data[1], remain * 4);
            return 0;
        }
    };

    class InputStream {

    public:
        explicit InputStream(ProcessCallback *processCallback, InputStreamCallback *callback,
                             const char *path) : processCallback(processCallback),
                                                 callback(callback), sourcePath(path), lockMutex() {
        }

        ~InputStream() {
            release();
        }

    private:
        void release() {
            if (audio_dec_ctx != nullptr) {
                avcodec_free_context(&audio_dec_ctx);
                audio_dec_ctx = nullptr;
            }

            if (fmt_ctx != nullptr) {
                avformat_close_input(&fmt_ctx);
                fmt_ctx = nullptr;
            }

            if (pkt != nullptr) {
                av_packet_unref(pkt);
                av_packet_free(&pkt);
                pkt = nullptr;
            }

            if (frame != nullptr) {
                av_frame_free(&frame);
                frame = nullptr;
            }
        }

    private:
        InputStreamCallback *callback;
        AVFormatContext *fmt_ctx = nullptr;
        AVCodecContext *audio_dec_ctx = nullptr;
        int width = 0, height = 0;
        enum AVPixelFormat pix_fmt = AV_PIX_FMT_NONE;
        AVStream *audio_stream = nullptr;
        const char *src_filename = nullptr;
        int audio_stream_idx = -1;
        AVFrame *frame = nullptr;
        AVPacket *pkt = nullptr;
        int audio_frame_count = 0;
        int64_t duration = 0;
        std::unique_ptr<ProcessCallback> processCallback;
        bool stopped = false;
        std::mutex lockMutex;

        bool isStopped() {
            std::lock_guard<std::mutex> lg(lockMutex);
            return stopped;
        }

        void output_audio_frame(AVFrame *audioFrame) {
            callback->onFrame(audioFrame);
        }

        int decode_packet(AVCodecContext *dec, const AVPacket *package) {
            int ret;

            // submit the packet to the decoder
            ret = avcodec_send_packet(dec, package);
            if (ret < 0) {
                return ret;
//                throw ConvertException(
//                        std::string("decode error: Error submitting a packet for decoding: ") +
//                        av_err2str(ret));
//            fprintf(stderr, "Error submitting a packet for decoding (%s)\n", av_err2str(ret));
            }

            // get all the available frames from the decoder
            while (ret >= 0) {
                ret = avcodec_receive_frame(dec, frame);
                if (ret < 0) {
                    // those two return values are special and mean there is no output
                    // frame available, but there were no errors during decoding
                    if (ret == AVERROR_EOF || ret == AVERROR(EAGAIN))
                        return 0;

                    return ret;
//                    throw ConvertException(
//                            std::string("decode error: Error during decoding: ") + av_err2str(ret));
//                fprintf(stderr, "Error during decoding (%s)\n", av_err2str(ret));
//                return ret;
                }

                // write the frame data to output file
//            if (dec->codec->type == AVMEDIA_TYPE_VIDEO)
//                ret = output_video_frame(frame);
//            else
                output_audio_frame(frame);

                av_frame_unref(frame);
            }

            return 0;
        }

        static int find_audio_stream(AVFormatContext *formatContext, enum AVMediaType type,
                                     int index_audio_track) {
            int ret1 = av_find_best_stream(formatContext, type, index_audio_track, -1, nullptr, 0);
            __android_log_print(6, "AudioConverter", "find input ret1:%d", index_audio_track);

            unsigned int streams = formatContext->nb_streams;

            int ret = -1;
            if (index_audio_track < streams) {
                for (int i = 0; i < streams; ++i) {
                    AVMediaType codec_type = formatContext->streams[i]->codec->codec_type;
                    __android_log_print(6, "AudioConverter", "i:%d  codec_type:%d index_audio_track:%d", i, codec_type, index_audio_track);
                    if (codec_type == AVMEDIA_TYPE_AUDIO && i == index_audio_track) {
                        ret = i;
                    }
                }
            }
            __android_log_print(6, "AudioConverter", "find input ret:%d", ret);
            if (ret < 0) {
                ret = av_find_best_stream(formatContext, type, -1, -1, nullptr, 0);
            }
            __android_log_print(6, "AudioConverter", "output ret:%d", ret);
            return ret;
        }

        static void open_codec_context(int *stream_idx,
                                       AVCodecContext **dec_ctx, AVFormatContext *formatContext,
                                       enum AVMediaType type, int index_audio_track) {
            int ret, stream_index;
            AVStream *st;
            const AVCodec *dec;
            AVDictionary *opts = nullptr;

            ret = find_audio_stream(formatContext, type, index_audio_track);
            if (ret < 0) {
                throw ConvertException("stream error: audio stream not found.");
//            fprintf(stderr, "Could not find %s stream in input file '%s'\n",
//                    av_get_media_type_string(type), src_filename);
//            return ret;
            } else {
                stream_index = ret;
                st = formatContext->streams[stream_index];

                /* find decoder for the stream */
                dec = avcodec_find_decoder(st->codecpar->codec_id);
                if (!dec) {
//                fprintf(stderr, "Failed to find %s codec\n",
//                        av_get_media_type_string(type));
                    throw ConvertException("decode error: Failed to find codec");
//                return AVERROR(EINVAL);
                }

                /* Allocate a codec context for the decoder */
                *dec_ctx = avcodec_alloc_context3(dec);
                if (!*dec_ctx) {
//                fprintf(stderr, "Failed to allocate the %s codec context\n",
//                        av_get_media_type_string(type));
                    throw ConvertException("decode error: Failed to allocated the codec context");
//                return AVERROR(ENOMEM);
                }

                /* Copy codec parameters from input stream to output codec context */
                if ((ret = (avcodec_parameters_to_context(*dec_ctx, st->codecpar))) < 0) {
//                fprintf(stderr, "Failed to copy %s codec parameters to decoder context\n",
//                        av_get_media_type_string(type));
                    throw ConvertException(
                            std::string("decode error: Failed to copy codec parameters: ") +
                            av_err2str(ret));
//                return ret;
                }

                /* Init the decoders */
                if ((ret = (avcodec_open2(*dec_ctx, dec, &opts))) < 0) {
//                fprintf(stderr, "Failed to open %s codec\n",
//                        av_get_media_type_string(type));
                    throw ConvertException(
                            std::string("decode error: Failed to open codec") + av_err2str(ret));
//                return ret;
                }
                *stream_idx = stream_index;
            }

        }

        /*
        int get_format_from_sample_fmt(const char **fmt,
                                       enum AVSampleFormat sample_fmt) {
            int i;
            struct sample_fmt_entry {
                enum AVSampleFormat sample_fmt;
                const char *fmt_be, *fmt_le;
            } sample_fmt_entries[] = {
                    {AV_SAMPLE_FMT_U8,  "u8",    "u8"},
                    {AV_SAMPLE_FMT_S16, "s16be", "s16le"},
                    {AV_SAMPLE_FMT_S32, "s32be", "s32le"},
                    {AV_SAMPLE_FMT_FLT, "f32be", "f32le"},
                    {AV_SAMPLE_FMT_DBL, "f64be", "f64le"},
            };
            *fmt = nullptr;

            for (i = 0; i < FF_ARRAY_ELEMS(sample_fmt_entries); i++) {
                struct sample_fmt_entry *entry = &sample_fmt_entries[i];
                if (sample_fmt == entry->sample_fmt) {
                    *fmt = AV_NE(entry->fmt_be, entry->fmt_le);
                    return 0;
                }
            }

    //        fprintf(stderr,
    //                "sample format %s is not supported as output format\n",
    //        av_get_sample_fmt_name(sample_fmt));
    //        return -1;
            throw ConvertException("sample format %s is not supported as output format\n");
        }
         */

    public:
        void stop() {
            std::lock_guard<std::mutex> lg(lockMutex);
            if (stopped)
                return;

            stopped = true;

        }

        void init(int index_audio_track) {

            src_filename = sourcePath.c_str();
            __android_log_print(6, "AudioConverter", "init %s index_audio_track:%d", src_filename, index_audio_track);
            int ret;
            /* open input file, and allocate format context */
            if ((ret = avformat_open_input(&fmt_ctx, src_filename, nullptr, nullptr)) < 0) {
//            fprintf(stderr, "Could not open source file %s\n", src_filename);
//            exit(1);
                throw ConvertException(std::string("open source: file failed: ") + av_err2str(ret));
            }

            /* retrieve stream information */
            if ((ret = avformat_find_stream_info(fmt_ctx, nullptr)) < 0) {
//            fprintf(stderr, "Could not find stream information\n");
//            exit(1);
                throw ConvertException(
                        std::string("open source: Could not find stream information") +
                        av_err2str(ret));
            }

            open_codec_context(&audio_stream_idx, &audio_dec_ctx, fmt_ctx, AVMEDIA_TYPE_AUDIO, index_audio_track);
            audio_stream = fmt_ctx->streams[audio_stream_idx];

            duration = audio_stream->duration;
            if (duration < 0) {
                duration = av_rescale_q(fmt_ctx->duration,
                                           AV_TIME_BASE_Q,
                                           audio_stream->time_base);
            }

            /* dump input information to stderr */
//        av_dump_format(fmt_ctx, 0, src_filename, 0);

            frame = av_frame_alloc();
            if (!frame) {
//            fprintf(stderr, "Could not allocate frame\n");
//            AVERROR(ENOMEM);
                throw ConvertException("memory error: Could not allocate frame");
            }

            pkt = av_packet_alloc();
            if (!pkt) {
//            fprintf(stderr, "Could not allocate packet\n");
//            ret = AVERROR(ENOMEM);
                throw ConvertException("memory error: Could not allocate packet");
            }

            callback->onAudioStream(audio_dec_ctx);
        }

        void start() {
            int ret = 0;

//        if (audio_stream)
//            printf("Demuxing audio from file '%s' into '%s'\n", src_filename, audio_dst_filename);

            /* read frames from the file */
            int progress = 0;
            while (!isStopped() && av_read_frame(fmt_ctx, pkt) >= 0) {
                // check if the packet belongs to a stream we are interested in, otherwise
                // skip it
//            if (pkt->stream_index == video_stream_idx)
//                ret = decode_packet(video_dec_ctx, pkt);
//            else if (pkt->stream_index == audio_stream_idx)
                if (pkt->stream_index == audio_stream_idx) {
                    ret = decode_packet(audio_dec_ctx, pkt);

                    if (ret >= 0) {
                        progress = pkt->pts * 100 / duration;
                        processCallback->onProgress(progress);
                    }
                }
                av_packet_unref(pkt);
                if (ret < 0) {
                    if (ret == AVERROR_EOF || ret == AVERROR(EAGAIN) || progress > 90)
                        break;
                    else {
                        throw ConvertException(std::string("decode error: ") + av_err2str(ret));
                    }
                }

            }

            if (isStopped()) {
                throw ConvertException("cancelled");
            }

            /* flush the decoders */
//        if (video_dec_ctx)
//            decode_packet(video_dec_ctx, NULL);
            if (audio_dec_ctx)
                decode_packet(audio_dec_ctx, nullptr);

//        printf("Demuxing succeeded.\n");

//        if (audio_stream) {
//            enum AVSampleFormat sfmt = audio_dec_ctx->sample_fmt;

//            __android_log_print(6, "AudioConverter", "audio %d, %d, %d, %d", audio_dec_ctx->sample_fmt, audio_dec_ctx->sample_rate, audio_dec_ctx->channels, audio_dec_ctx->channel_layout);
//            int n_channels = audio_dec_ctx->channels;
//            const char *fmt;

//            if (av_sample_fmt_is_planar(sfmt)) {
//                const char *packed = av_get_sample_fmt_name(sfmt);
//                __android_log_print(6, "AudioConverter", "planar %s", packed);
//                printf("Warning: the sample format the decoder produced is planar "
//                       "(%s). This example will output the first channel only.\n",
//                       packed ? packed : "?");
//                sfmt = av_get_packed_sample_fmt(sfmt);
//                n_channels = 1;
//            }

//            ret = get_format_from_sample_fmt(&fmt, sfmt);

//            printf("Play the output audio file with the command:\n"
//                   "ffplay -f %s -ac %d -ar %d %s\n",
//                   fmt, n_channels, audio_dec_ctx->sample_rate,
//                   audio_dst_filename);
//        }

            callback->onEnd();

            release();
        }

    private:
        std::string sourcePath;
    };

    class OutputStream : public InputStreamCallback {
    public:
        OutputStream(const char *targetPath, const char *format)
                : targetPath(targetPath), format(format) {

        }

        ~OutputStream() {
//        __android_log_write(6, "AudioConverter", "~OutputStream");
            if (codecContext != nullptr)
                avcodec_free_context(&codecContext);

            if (frame != nullptr)
                av_frame_free(&frame);

//    sws_freeContext(sws_ctx);
            if (swr_ctx != nullptr)
                swr_free(&swr_ctx);


            if (context != nullptr) {
                if (!(context->oformat->flags & AVFMT_NOFILE))
                    /* Close the output file. */
                    avio_closep(&context->pb);

                /* free the stream */
                avformat_free_context(context);
            }
        }


    private:
        std::string targetPath;
        std::string format;
        AudioFrameBuffer audioFrameBuffer{};
        AVFormatContext *context{};
        AVStream *stream{};
        AVCodecContext *codecContext{};
        AVCodec *encoder{};
        AVFrame *frame{};
        int frameSize{0};
        struct SwrContext *swr_ctx{};
        int samples_count{};
        int64_t next_pts{};

        int sourceSample_rate = 0;
        uint64_t sourceLayout = 0;
        int sourceChannels = 0;
        AVSampleFormat sourceSampleFormat = AV_SAMPLE_FMT_NONE;
    public:
        void init() {
            int ret;
            ret = avformat_alloc_output_context2(&context, nullptr, format.c_str(),
                                                 targetPath.c_str());
            if (ret < 0) {
                throw ConvertException(
                        std::string("create target: can't alloc output") + av_err2str(ret));
            }

            add_stream(context->oformat->audio_codec);

            AVDictionary *opt = nullptr;
            open_audio(opt);

            if (!(context->oformat->flags & AVFMT_NOFILE)) {
                ret = avio_open(&context->pb, targetPath.c_str(), AVIO_FLAG_WRITE);
                if (ret < 0) {
                    throw ConvertException(
                            std::string("create target: can't open avio:") + av_err2str(ret));
                }
            }
            /* Write the stream header, if any. */

            ret = avformat_write_header(context, &opt);
            if (ret < 0)
                throw ConvertException(
                        std::string("create target: can't write header") + av_err2str(ret));
        }

        void end() {
            av_write_trailer(context);

            avcodec_free_context(&codecContext);
            av_frame_free(&frame);
//    sws_freeContext(sws_ctx);
            swr_free(&swr_ctx);


            if (!(context->oformat->flags & AVFMT_NOFILE))
                /* Close the output file. */
                avio_closep(&context->pb);

            /* free the stream */
            avformat_free_context(context);

            context = nullptr;
            codecContext = nullptr;
            frame = nullptr;
            swr_ctx = nullptr;
        }

        /* Add an output stream. */
        void add_stream(enum AVCodecID codec_id) {
            __android_log_print(4, "AudioConverter", "add audio %d", codec_id);
            int i;
            /* find the encoder */
            encoder = avcodec_find_encoder(codec_id);
            if (!encoder) {
                throw ConvertException("encode error: can't find encoder");
            }

            stream = avformat_new_stream(context, nullptr);
            if (!stream) {
                throw ConvertException("encode error: can't new stream");
            }
            stream->id = (int) (context->nb_streams - 1);
            codecContext = avcodec_alloc_context3(encoder);
            if (!codecContext) {
                throw ConvertException("encode error: can't alloc context3");
            }
            switch ((encoder)->type) {
                case AVMEDIA_TYPE_AUDIO:
                    codecContext->sample_fmt = encoder->sample_fmts ?
                                               encoder->sample_fmts[0] : AV_SAMPLE_FMT_FLTP;
                    codecContext->bit_rate = 128000;


/*
                codecContext->sample_rate = 44100;

                codecContext->channel_layout = AV_CH_LAYOUT_MONO;
                if (encoder->channel_layouts) {
                    codecContext->channel_layout = encoder->channel_layouts[0];
                    for (i = 0; encoder->channel_layouts[i]; i++) {
                        if (encoder->channel_layouts[i] == AV_CH_LAYOUT_MONO)
                            codecContext->channel_layout = AV_CH_LAYOUT_MONO;
                    }
                }

*/


                    codecContext->sample_rate = 44100;
                    if (encoder->supported_samplerates) {
                        codecContext->sample_rate = encoder->supported_samplerates[0];
                        for (i = 0; encoder->supported_samplerates[i]; i++) {
                            if (encoder->supported_samplerates[i] == 44100)
                                codecContext->sample_rate = 44100;
                        }
                    }
                    codecContext->channels = av_get_channel_layout_nb_channels(
                            codecContext->channel_layout);
                    codecContext->channel_layout = AV_CH_LAYOUT_STEREO;
                    if (encoder->channel_layouts) {
                        codecContext->channel_layout = encoder->channel_layouts[0];
                        for (i = 0; encoder->channel_layouts[i]; i++) {
                            if (encoder->channel_layouts[i] == AV_CH_LAYOUT_STEREO)
                                codecContext->channel_layout = AV_CH_LAYOUT_STEREO;
                        }
                    }


                    codecContext->channels = av_get_channel_layout_nb_channels(
                            codecContext->channel_layout);
                    stream->time_base = (AVRational) {1, codecContext->sample_rate};
                    break;

                default:
                    break;
            }
            /* Some formats want stream headers to be separate. */
            if (context->oformat->flags & AVFMT_GLOBALHEADER)
                codecContext->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
        }

        void open_audio(AVDictionary *opt_arg) {
//        AVFormatContext *oc = context;
            AVCodec *codec = encoder;
            AVCodecContext *c;
            OutputStream *ost = this;
            int nb_samples;
            int ret;
            AVDictionary *opt = nullptr;

            c = ost->codecContext;

            /* open it */
            av_dict_copy(&opt, opt_arg, 0);
            ret = avcodec_open2(c, codec, &opt);
            av_dict_free(&opt);
            if (ret < 0) {
//        fprintf(stderr, "Could not open audio codec: %s\n", av_err2str(ret));
//        exit(1);
                throw ConvertException(std::string("encode error: Could not open audio codec: ") +
                                       av_err2str(ret));
            }

            /* init signal generator */
//        ost->t = 0;
//        ost->tincr = 2 * M_PI * 110.0 / c->sample_rate;
//        /* increment frequency by 110 Hz per second */
//        ost->tincr2 = 2 * M_PI * 110.0 / c->sample_rate / c->sample_rate;


//            if (c->codec->capabilities & AV_CODEC_CAP_VARIABLE_FRAME_SIZE)
//                nb_samples = 10000;
//            else {
//                nb_samples = c->frame_size;


//            }
            nb_samples = 1024 * 32;


            ost->frame = getAudioFrame(1024 * 2);

            /* copy the stream parameters to the muxer */
            ret = avcodec_parameters_from_context(ost->stream->codecpar, c);
            if (ret < 0) {
//        fprintf(stderr, "Could not copy the stream parameters\n");
//        exit(1);
                throw ConvertException(
                        std::string("encode error: Could not copy the stream parameters: ") +
                        av_err2str(ret));
            }

            /* create resampler context */
            ost->swr_ctx = swr_alloc();
            if (!ost->swr_ctx) {
                throw ConvertException("encode error: Could not allocate resampler context");
//        fprintf(stderr, "Could not allocate resampler context\n");
//        exit(1);
            }

            /* set options */
            av_opt_set_int(ost->swr_ctx, "in_channel_layout", sourceLayout, 0);
            av_opt_set_int(ost->swr_ctx, "out_channel_layout", c->channel_layout, 0);
            av_opt_set_int(ost->swr_ctx, "in_channel_count", sourceChannels, 0);
            av_opt_set_int(ost->swr_ctx, "out_channel_count", c->channels, 0);
            av_opt_set_int(ost->swr_ctx, "in_sample_rate", sourceSample_rate, 0);
            av_opt_set_int(ost->swr_ctx, "out_sample_rate", c->sample_rate, 0);
            av_opt_set_sample_fmt(ost->swr_ctx, "in_sample_fmt", sourceSampleFormat, 0);
            av_opt_set_sample_fmt(ost->swr_ctx, "out_sample_fmt", c->sample_fmt, 0);

            /* initialize the resampling context */
            if ((ret = (swr_init(ost->swr_ctx))) < 0) {
                throw ConvertException(
                        std::string("encode error: Failed to initialize the resampling context: ") +
                        av_err2str(ret));
//        fprintf(stderr, "Failed to initialize the resampling context\n");
//        exit(1);
            }
        }

        AVFrame *getAudioFrame(int size) {
//            __android_log_print(6, "AudioConverter", "getAudioFrame %d", size);
            if (frame == nullptr) {
                frameSize = size;
                frame = alloc_audio_frame(codecContext->sample_fmt, codecContext->channel_layout,
                                          codecContext->sample_rate, size);
            }
            if (size <= frameSize)
                return frame;

            if (frame != nullptr) {
                av_frame_free(&frame);
            }
            frame = alloc_audio_frame(codecContext->sample_fmt, codecContext->channel_layout,
                                      codecContext->sample_rate, size);

            frameSize = size;
            return frame;
        }

        /*
     * encode one audio frame and send it to the muxer
     * return 1 when encoding is finished, 0 otherwise
     */

        void write_audio_frame(AVFrame *audioFrame) {
            int ret;
            int dst_nb_samples;

            if (audioFrame) {
                /* convert samples from native format to destination codec format, using the resampler */
                /* compute destination number of samples */
//            __android_log_print(6, "AudioConverter", "delay %ld, %ld", swr_get_delay(swr_ctx, sourceSample_rate), swr_get_delay(swr_ctx, codecContext->sample_rate));
//            int delay = swr_get_delay(swr_ctx, sourceSample_rate);
//            delay = av_rescale_rnd(
//                    delay,
//                    codecContext->sample_rate, sourceSample_rate, AV_ROUND_UP);
                dst_nb_samples = av_rescale_rnd(
                        swr_get_delay(swr_ctx, sourceSample_rate) +
                        audioFrame->nb_samples,
                        codecContext->sample_rate, sourceSample_rate, AV_ROUND_UP);
//                    codecContext->sample_rate, codecContext->sample_rate, AV_ROUND_UP);
//        av_assert0(dst_nb_samples == frame->nb_samples);
//            __android_log_print(6, "AudioConverter", "resample %d, %d, %d, %d, %d, %d", sourceSampleFormat, codecContext->sample_fmt, sourceSample_rate, codecContext->sample_rate, audioFrame->nb_samples, dst_nb_samples);

//            frame->nb_samples = dst_nb_samples;
                /* when we pass a frame to the encoder, it may keep a reference to it
                 * internally;
                 * make sure we do not overwrite it here
                 */
                frame = getAudioFrame(dst_nb_samples);
                ret = av_frame_make_writable(frame);
                if (ret < 0)
                    throw ConvertException(
                            std::string("encode error: av_frame_make_writable error: ") +
                            av_err2str(ret));
//            exit(1);

                /* convert to destination format */
                ret = swr_convert(swr_ctx,
                                  frame->data, dst_nb_samples,
                                  (const uint8_t **) audioFrame->data, audioFrame->nb_samples);
                if (ret < 0) {
                    throw ConvertException(
                            std::string("encode error: swr_convert error: ") + av_err2str(ret));
//            fprintf(stderr, "Error while converting\n");
//            exit(1);
                }
//                audioFrame = frame;
                frame->nb_samples = ret;

                audioFrameBuffer.sendFrame(frame, ret);

                ret = audioFrameBuffer.receiveFrame(frame, codecContext->frame_size);
                while (ret >= 0) {
                    int64_t pts = av_rescale_q(samples_count,
                                                   (AVRational) {1, codecContext->sample_rate},
                                                   codecContext->time_base);

                    frame->pts = pts;
                    samples_count += frame->nb_samples;

                    write_frame(frame);
                    av_frame_make_writable(frame);
                    ret = audioFrameBuffer.receiveFrame(frame, codecContext->frame_size);
                }

//                audioFrame->pts = av_rescale_q(samples_count,
//                                               (AVRational) {1, codecContext->sample_rate},
//                                               codecContext->time_base);
//                samples_count += ret;
            } else
                write_frame(audioFrame);
        }

        void write_frame(AVFrame *pFrame) {
            AVFormatContext *fmt_ctx = context;
            AVCodecContext *c = codecContext;
            AVStream *st = stream;
            int ret;

            // send the frame to the encoder
            ret = avcodec_send_frame(c, pFrame);
            if (ret < 0) {
//            fprintf(stderr, "Error sending a frame to the encoder: %s\n",
//                    av_err2str(ret));
//            exit(1);
                throw ConvertException(
                        std::string("encode error: Error sending a frame to the encoder: ") +
                        av_err2str(ret));
            }

            while (ret >= 0) {
                AVPacket pkt = {nullptr};

                ret = avcodec_receive_packet(c, &pkt);
                if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
                    break;
                else if (ret < 0) {
//            fprintf(stderr, "Error encoding a frame: %s\n", av_err2str(ret));
//            exit(1);
                    throw ConvertException(std::string("encode error: Error encoding a frame: ") +
                                           av_err2str(ret));
                }

                /* rescale output packet timestamp values from codec to stream timebase */
                av_packet_rescale_ts(&pkt, c->time_base, st->time_base);
                pkt.stream_index = st->index;

                /* Write the compressed frame to the media file. */
//        log_packet(fmt_ctx, &pkt);
                ret = av_interleaved_write_frame(fmt_ctx, &pkt);
                av_packet_unref(&pkt);
                if (ret < 0) {
                    throw ConvertException(
                            std::string("encode error: av_interleaved_write_frame: ") +
                            av_err2str(ret));
//            fprintf(stderr, "Error while writing output packet: %s\n", av_err2str(ret));
//            exit(1);
                }
            }

        }

        void onAudioStream(AVCodecContext *sourceCodecContext) override {
            sourceSample_rate = sourceCodecContext->sample_rate;
            sourceLayout = sourceCodecContext->channel_layout;
            sourceChannels = sourceCodecContext->channels;
            sourceSampleFormat = sourceCodecContext->sample_fmt;

            init();
        }

        void onFrame(AVFrame *audioFrame) override {
            write_audio_frame(audioFrame);
        }

        void onEnd() override {
            write_audio_frame(nullptr);
            end();
        }

    };


    AudioConverter::AudioConverter(ProcessCallback *callback, const char *sourcePath,
                                   const char *targetPath, const char *format)
            : target(new OutputStream(targetPath, format)),
              inputStream(new InputStream(callback, target.get(), sourcePath)) {

    }

    AudioConverter::~AudioConverter() noexcept = default;

    const char *AudioConverter::convert(int index_audio_track) {
        try {
            inputStream->init(index_audio_track);
            inputStream->start();
//        inputStream.reset();
//        target.reset();
        } catch (std::exception &e) {
//        __android_log_write(6, "AudioConverter", e.what());
            return e.what();
        }

        return nullptr;
    }

    void AudioConverter::cancel() {
        inputStream->stop();
    }

//////////////jni

    jlong nativeInit(JNIEnv *env,
                     jobject thzz, jstring source, jstring target, jstring format) {

        jboolean copy;
        const char *sourcePath = env->GetStringUTFChars(source, &copy);
        const char *targetPath = env->GetStringUTFChars(target, &copy);
        const char *formatUTF = env->GetStringUTFChars(format, &copy);

        auto *progressCallback = new JavaProgressCallback(env, thzz);
        auto *converter = new AudioConverter(progressCallback, sourcePath, targetPath, formatUTF);

        env->ReleaseStringUTFChars(source, sourcePath);
        env->ReleaseStringUTFChars(target, targetPath);
        env->ReleaseStringUTFChars(format, formatUTF);

        return jlong(converter);
    }

    jstring nativeConvert(JNIEnv *env,
                          jobject  /*thzz*/, jlong ptr, jint index_audio_track) {
        auto *converter = reinterpret_cast<AudioConverter *>(ptr);
        const char *error = converter->convert(index_audio_track);
        if (error == nullptr)
            return nullptr;

        return env->NewStringUTF(error);
    }

    void nativeStop(JNIEnv * /*env*/,
                    jobject  /*thzz*/, jlong ptr) {
        auto *converter = reinterpret_cast<AudioConverter *>(ptr);
        converter->cancel();
    }

    void nativeRelease(JNIEnv * /*env*/,
                       jobject  /*thzz*/, jlong ptr) {
        auto *converter = reinterpret_cast<AudioConverter *>(ptr);
        delete converter;
    }

    void av_log_my_callback(void *ptr, int level, const char *fmt, va_list vl) {
        AVClass *avc = ptr ? *(AVClass **) ptr : nullptr;
        __android_log_vprint(6, avc ? avc->class_name : "mediaConverter", fmt, vl);
    }


    const JNINativeMethod methods[] =
            {
                    {"nativeInit",    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)J", (void *) nativeInit},
                    {"nativeConvert", "(JI)Ljava/lang/String;",                                    (void *) nativeConvert},
                    {"nativeStop",    "(J)V",                                                      (void *) nativeStop},
                    {"nativeRelease", "(J)V",                                                      (void *) nativeRelease},
            };

    void AudioConverter::initClass(JNIEnv *env, jclass clazz) {
//            av_log_set_callback(&av_log_my_callback);
        env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0]));
        JavaProgressCallback::init(env, clazz);
//    if ( env->ExceptionCheck() )
//    {
//        _env->ExceptionDescribe();
//    }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_mxtech_av_AudioConverter_nativeInitClass(
        JNIEnv *env,
        jclass clazz) {
    AudioConverter::initClass(env, clazz);
}






