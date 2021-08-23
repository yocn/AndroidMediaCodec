#include <code-self/audio/opensl_render.h>

extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include <ffmpeg/libswscale/swscale.h>
#include <ffmpeg/libavutil/imgutils.h>
#include <android/native_window_jni.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include <code-self/GlobalMacro.h>
#include <code-self/Util.h>
#include <code-self/JniProgress.h>
#include <SLES/OpenSLES.h>
#include "SLES/OpenSLES_Android.h"
}

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_FFMpegSimpleAudioPlayer_##name

extern "C" {
JNIEXPORT void JNICALL
JNI_METHOD_NAME(convertJni)(JNIEnv *env, jobject jobj, jstring src, jstring out);
}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(convertJni)(JNIEnv *env, jobject jobj, jstring src, jstring out) {
    // 源文件路径
    const char *src_path = env->GetStringUTFChars(src, nullptr);
    // 生产的pcm文件路径
    const char *dst_path = env->GetStringUTFChars(out, nullptr);
    LOGE("-------------------------init-----------------src_path--%s", src_path);
    LOGE("-------------------------init-----------------dst_path--%s", dst_path);
    // AVFormatContext 对象创建
    AVFormatContext *avFormatContext = avformat_alloc_context();
    // 打开音频文件
    int ret = avformat_open_input(&avFormatContext, src_path, nullptr, nullptr);
    if (ret != 0) {
        LOGE("打开文件失败");
        return;
    }
    // 输出音频文件的信息
    av_dump_format(avFormatContext, 0, src_path, 0);
    // 获取音频文件的流信息
    ret = avformat_find_stream_info(avFormatContext, nullptr);
    if (ret < 0) {
        LOGE("获取流信息失败");
        return;
    }
    // 查找音频流在文件的所有流集合中的位置
    int streamIndex = 0;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        enum AVMediaType avMediaType = avFormatContext->streams[i]->codecpar->codec_type;
        if (avMediaType == AVMEDIA_TYPE_AUDIO) {  //这边和视频不一样，是AUDIO
            streamIndex = i;
        }
    }
    AVStream *stream = avFormatContext->streams[streamIndex];
    // 拿到对应音频流的参数
    AVCodecParameters *avCodecParameters = stream->codecpar;
    // 获取解码器的标识ID
    enum AVCodecID avCodecId = avCodecParameters->codec_id;
    // 通过获取的ID，获取对应的解码器
    AVCodec *avCodec = avcodec_find_decoder(avCodecId);
    // 创建一个解码器上下文对象
    AVCodecContext *avCodecContext = avcodec_alloc_context3(nullptr);
    if (avCodecContext == nullptr) {
        //创建解码器上下文失败
        LOGE("创建解码器上下文失败");
        return;
    }
    // 将新的API中的 codecpar 转成 AVCodecContext
    avcodec_parameters_to_context(avCodecContext, avCodecParameters);
    ret = avcodec_open2(avCodecContext, avCodec, nullptr);
    if (ret < 0) {
        LOGE("打开解码器失败 ");
        return;
    }
    LOGE("decodec name: %s", avCodec->name);

    //压缩数据包
    AVPacket *packet = (AVPacket *) av_malloc(sizeof(AVPacket));
    //解压缩后存放的数据帧的对象
    AVFrame *inFrame = av_frame_alloc();
    //frame->16bit 44100 PCM 统一音频采样格式与采样率
    //创建swrcontext上下文件
    SwrContext *swrContext = swr_alloc();
    //音频格式  输入的采样设置参数
    AVSampleFormat inFormat = avCodecContext->sample_fmt;
    // 出入的采样格式
    AVSampleFormat outFormat = AV_SAMPLE_FMT_S16;
    // 输入采样率
    int inSampleRate = avCodecContext->sample_rate;
    // 输出采样率
    int outSampleRate = 44100;
    // 输入声道布局
    uint64_t in_ch_layout = avCodecContext->channel_layout;
    //输出声道布局，双声道
    uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;
    //给Swrcontext 分配空间，设置公共参数
    swr_alloc_set_opts(swrContext, out_ch_layout, outFormat, outSampleRate,
                       in_ch_layout, inFormat, inSampleRate, 0, nullptr
    );
    // 初始化
    swr_init(swrContext);
    // 获取声道数量
    int outChannelCount = av_get_channel_layout_nb_channels(out_ch_layout);

    int currentIndex = 1;
    int lastPercent = -1;
    LOGE("声道数量%d ", outChannelCount);
    // 设置音频缓冲区间 16bit   44100  PCM数据, 双声道
    uint8_t *out_buffer = (uint8_t *) av_malloc(2 * 44100);
    // 创建pcm的文件对象
    FILE *fp_pcm = fopen(dst_path, "wb");


    long durationForRealTime = avFormatContext->duration / 1000;
    AVRational time_base_rational = stream->time_base;
    double time_base = av_q2d(time_base_rational);
    // Audio only. Audio frame size, if known. Required by some formats to be static.
    int frame_size = avCodecParameters->frame_size;
    int bit_rate = avCodecParameters->bit_rate;
    int sample_rate = avCodecParameters->sample_rate;
    long durationForPts = stream->duration;

    LOGE("------------------------------------------------------------------");
    //开始读取源文件，进行解码
    while (av_read_frame(avFormatContext, packet) >= 0) {
        if (packet->stream_index == streamIndex) {
            avcodec_send_packet(avCodecContext, packet);
            //解码
            ret = avcodec_receive_frame(avCodecContext, inFrame);
            int nb_samples = inFrame->nb_samples;
            if (ret == 0) {
                //将每一帧数据转换成pcm
                swr_convert(swrContext, &out_buffer, 2 * 44100,
                            (const uint8_t **) inFrame->data, nb_samples);
                //获取实际的缓存大小
                int out_buffer_size = av_samples_get_buffer_size(nullptr, outChannelCount,
                                                                 nb_samples, outFormat, 1);
                // 写入文件
                fwrite(out_buffer, 1, out_buffer_size, fp_pcm);
                LOGE("out_buffer_size:::%d", out_buffer_size);
            }
            ++currentIndex;

            double timePerFrame = 1.0 * inFrame->nb_samples * 1000 / 44100;
            double currTime = currentIndex * timePerFrame;
            int percent = ceil(1.0F * packet->pts * 100 / durationForPts);
            double real_time = time_base * inFrame->pts;


            double curr = inFrame->pts * time_base;

//            if (percent % 20 == 0 && lastPercent != percent) {
//                lastPercent = percent;
            LOGE("currTime::%lf  percent:%d, currentIndex:%d, real_time::%lf,  pts:%ld, real_time2::%lf,  packet->pos:%ld",
                 currTime, percent, currentIndex, real_time, inFrame->pts, curr, packet->pos);
//            }

            progress(env, jobj, (long) currTime, durationForRealTime, percent);
        }
    }
    LOGE("duration:%ld  frame_size:%d, bit_rate:%d, "
         "sample_rate:%d, currentIndex:%d,  time_base::%2.14lf, durationAll:%ld  num:%d  den:%d",
         durationForRealTime, frame_size, bit_rate,
         sample_rate, currentIndex, time_base, durationForPts, time_base_rational.num, time_base_rational.den);

    // 及时释放
    fclose(fp_pcm);
    av_frame_free(&inFrame);
    av_free(out_buffer);
    swr_free(&swrContext);
    avcodec_close(avCodecContext);
    avformat_close_input(&avFormatContext);

    env->ReleaseStringUTFChars(src, src_path);
    env->ReleaseStringUTFChars(out, dst_path);

}