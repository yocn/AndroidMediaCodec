#include <code-self/audio/play/opensl_render.h>
#include <unistd.h>
#include "code-self/common/JniProgress.h"

extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include <ffmpeg/libswscale/swscale.h>
#include <ffmpeg/libavutil/imgutils.h>
#include <android/native_window_jni.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include <code-self/common/GlobalMacro.h>
#include <code-self/common/Util.h>
#include <SLES/OpenSLES.h>
#include "SLES/OpenSLES_Android.h"
}

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_FFMpegSimpleAudioVideoPlayer_##name

struct RGBMode {
    // sws_scale需要的格式
    AVPixelFormat pixelFormat;
    // 创建ANativeWindow需要的格式
    ANativeWindow_LegacyFormat aNativeWindowLegacyFormat;
    // 根据格式判断存储的步长
    int multi_stride;
};

inline double get_base_time(timeval *start, timeval *curr) {
    __kernel_time_t tv_sec;
    __kernel_suseconds_t tv_usec;
    if (curr->tv_usec < start->tv_usec) {
        tv_sec = curr->tv_sec - start->tv_sec - 1;
        tv_usec = curr->tv_usec + 1000000 - start->tv_usec;
    } else {
        tv_sec = curr->tv_sec - start->tv_sec;
        tv_usec = curr->tv_usec - start->tv_usec;
    }
    return tv_sec + tv_usec * 1.0 / 1000000;
}

extern "C" {
JNIEXPORT void JNICALL
JNI_METHOD_NAME(playJni)(JNIEnv *env, jobject jobj, jstring src, jobject surface);
}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(playJni)(JNIEnv *env, jobject jobj, jstring src, jobject surface) {
    // 源文件路径
    const char *src_path = env->GetStringUTFChars(src, nullptr);
    LOGE("-------------------------init-----------------src_path--%s", src_path);
    // AVFormatContext 对象创建
    AVFormatContext *avFormatContext = avformat_alloc_context();
    // 打开视频文件,解封装
    int ret = avformat_open_input(&avFormatContext, src_path, nullptr, nullptr);
    if (ret != 0) {
        LOGE("打开文件失败");
        return;
    }
    // 输出视频文件的信息
    av_dump_format(avFormatContext, 0, src_path, 0);
    // 获取视频文件的流信息
    ret = avformat_find_stream_info(avFormatContext, nullptr);
    if (ret < 0) {
        LOGE("获取流信息失败");
        return;
    }


    AVPacket *m_Packet = av_packet_alloc(); //创建 AVPacket 存放编码数据
    AVFrame *m_Frame = av_frame_alloc(); //创建 AVFrame 存放解码后的数据

    ////////////////////////////////////////////init audio /////////////////////////////////////////
    // 查找音视频流在文件的所有流集合中的位置
    int audio_stream_index = av_find_best_stream(avFormatContext, AVMEDIA_TYPE_AUDIO, -1, -1,
                                                 nullptr, 0);
    AVCodecParameters *audioCodecParameters = avFormatContext->streams[audio_stream_index]->codecpar;
    // 通过获取的ID，获取对应的解码器
    AVCodec *audioCodec = avcodec_find_decoder(audioCodecParameters->codec_id);
    // 创建一个解码器上下文对象
    AVCodecContext *audioCodecContext = avcodec_alloc_context3(nullptr);
    if (audioCodecContext == nullptr) {
        //创建解码器上下文失败
        LOGE("创建解码器上下文失败");
        return;
    }
    // 将新的API中的 codecpar 转成 AVCodecContext
    avcodec_parameters_to_context(audioCodecContext, audioCodecParameters);
    ret = avcodec_open2(audioCodecContext, audioCodec, nullptr);
    if (ret < 0) {
        LOGE("打开解码器失败 ");
        return;
    }

    //压缩数据包
//    AVPacket *packet = (AVPacket *) av_malloc(sizeof(AVPacket));
    //解压缩后存放的数据帧的对象
//    AVFrame *inFrame = av_frame_alloc();
    //frame->16bit 44100 PCM 统一音频采样格式与采样率
    //创建swrcontext上下文件
    SwrContext *swrContext = swr_alloc();
    //音频格式  输入的采样设置参数
    AVSampleFormat inFormat = audioCodecContext->sample_fmt;
    // 出入的采样格式
    AVSampleFormat outFormat = AV_SAMPLE_FMT_S16;
    // 输入采样率
    int inSampleRate = audioCodecContext->sample_rate;
    // 输出采样率
    int outSampleRate = 44100;
    // 输入声道布局
    uint64_t in_ch_layout = audioCodecContext->channel_layout;
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

    int currentIndex = 0;
    LOGE("声道数量%d ", outChannelCount);
    // 设置音频缓冲区间 16bit   44100  PCM数据, 双声道
    uint8_t *out_buffer = (uint8_t *) av_malloc(2 * 44100);

    OpenSLRender *openSlRender = new OpenSLRender();
    openSlRender->InitRender();

    ////////////////////////////////////////////init video /////////////////////////////////////////

    int video_stream_index = av_find_best_stream(avFormatContext, AVMEDIA_TYPE_VIDEO, -1, -1,
                                                 nullptr, 0);

    AVStream *videoStream = avFormatContext->streams[video_stream_index];
    AVCodecParameters *codecParameters = videoStream->codecpar;
    double time_base = av_q2d(videoStream->time_base);
    double fps = av_q2d(videoStream->avg_frame_rate);
//    LOGE("audio_stream_index:%d, video_stream_index:%d ", audio_stream_index, video_stream_index);

//6.根据 codec_id 获取解码器
    AVCodec *m_AVCodec = avcodec_find_decoder(codecParameters->codec_id);
    if (m_AVCodec == nullptr) {
        LOGCATE("DecoderBase::InitFFDecoder avcodec_find_decoder fail.");
        return;
    }

//7.创建解码器上下文
    AVCodecContext *videoCodecContext = avcodec_alloc_context3(m_AVCodec);
    if (avcodec_parameters_to_context(videoCodecContext, codecParameters) != 0) {
        LOGCATE("DecoderBase::InitFFDecoder avcodec_parameters_to_context fail.");
        return;
    }

//8.打开解码器
    int result = avcodec_open2(videoCodecContext, m_AVCodec, nullptr);
    if (result < 0) {
        LOGCATE("DecoderBase::InitFFDecoder avcodec_open2 fail. result=%d", result);
        return;
    }

//2.1. 分配存储 RGB 图像的 buffer
    int m_VideoWidth = videoCodecContext->width;
    int m_VideoHeight = videoCodecContext->height;

    int m_RenderWidth = m_VideoWidth;
    int m_RenderHeight = m_VideoHeight;

    AVFrame *m_RGBAFrame = av_frame_alloc();
    RGBMode rgb656 = {AV_PIX_FMT_RGB565LE, WINDOW_FORMAT_RGB_565, 2};
    RGBMode rgba8888 = {AV_PIX_FMT_RGBA, WINDOW_FORMAT_RGBA_8888, 4};

    RGBMode rgbMode = false ? rgb656 : rgba8888;

//计算 Buffer 的大小
    int bufferSize = av_image_get_buffer_size(rgbMode.pixelFormat, m_VideoWidth, m_VideoHeight, 1);
//为 m_RGBAFrame 分配空间
    auto *m_FrameBuffer = (uint8_t *) av_malloc(bufferSize * sizeof(uint8_t));
    av_image_fill_arrays(m_RGBAFrame->data, m_RGBAFrame->linesize, m_FrameBuffer,
                         rgbMode.pixelFormat, m_VideoWidth, m_VideoHeight, 1);

//2.2. 获取转换的上下文
    SwsContext *m_SwsContext = sws_getContext(m_VideoWidth, m_VideoHeight,
                                              videoCodecContext->pix_fmt,
                                              m_RenderWidth, m_RenderHeight, rgbMode.pixelFormat,
                                              SWS_FAST_BILINEAR, nullptr, nullptr, nullptr);

//3.1. 利用 Java 层 SurfaceView 传下来的 Surface 对象，获取 ANativeWindow
    ANativeWindow *m_NativeWindow = ANativeWindow_fromSurface(env, surface);

//3.2. 设置渲染区域和输入格式
    ANativeWindow_setBuffersGeometry(m_NativeWindow, m_VideoWidth,
                                     m_VideoHeight, rgbMode.aNativeWindowLegacyFormat);

    struct timeval tpend{};
    // 获取当前时间
    gettimeofday(&tpend, nullptr);
    // 记录开始解码的时间
    timeval start_timeval = {tpend.tv_sec, tpend.tv_usec};

    //开始读取源文件，进行解码
    while (av_read_frame(avFormatContext, m_Packet) >= 0) {
        LOGE("packet->pts;:%ld  packet->stream_index：%d  audio_stream_index:%d  video_stream_index:%d  ",
             m_Packet->pts, m_Packet->stream_index, audio_stream_index, video_stream_index);
        if (m_Packet->stream_index == video_stream_index) {
            if (avcodec_send_packet(videoCodecContext, m_Packet) != 0) { //视频解码
                return;
            }
            while (avcodec_receive_frame(videoCodecContext, m_Frame) == 0) {
                //获取到 m_Frame 解码数据，在这里进行格式转换，然后进行渲染

//2.3. 格式转换
                sws_scale(m_SwsContext, m_Frame->data, m_Frame->linesize, 0, m_VideoHeight,
                          m_RGBAFrame->data, m_RGBAFrame->linesize);

//3.3. 渲染
                ANativeWindow_Buffer m_NativeWindowBuffer;
//锁定当前 Window ，获取屏幕缓冲区 Buffer 的指针
                ANativeWindow_lock(m_NativeWindow, &m_NativeWindowBuffer, nullptr);
                auto *dstBuffer = static_cast<uint8_t *>(m_NativeWindowBuffer.bits);

                //输入图的步长（一行像素有多少字节），RGB_XXX的模式下linesize[0]总是为宽度
                int srcLineSize = m_RGBAFrame->linesize[0];
                //缓冲区步长 输出的stride步长，如果是RGBA是4，如果是RGB565是2
                int dstLineSize = m_NativeWindowBuffer.stride * rgbMode.multi_stride;

                long durationForRealTime = avFormatContext->duration / 1000;
                double curr = m_Frame->pts * time_base;

                // 记录当前的时间戳
                gettimeofday(&tpend, nullptr);
                double curr_delta_time = get_base_time(&start_timeval, &tpend);
                // 相对于pts的时间计算应该sleep的时间
                double sleep_time = curr - curr_delta_time;
                LOGE("m_Frame:%lf  durationForRealTime:%ld %ld/%ld   curr_delta_time::%lf  sleep_time::%lf",
                     curr, durationForRealTime,
                     tpend.tv_sec, tpend.tv_usec, curr_delta_time, sleep_time);

                for (int i = 0; i < m_VideoHeight; ++i) {
                    //一行一行地拷贝图像数据
                    memcpy(dstBuffer + i * dstLineSize, m_FrameBuffer + i * srcLineSize,
                           srcLineSize);
                }
//解锁当前 Window ，渲染缓冲区数据
                ANativeWindow_unlockAndPost(m_NativeWindow);
                // sleep相应的毫秒
//                usleep(sleep_time * 1000 * 1000);
            }
        } else if (m_Packet->stream_index == audio_stream_index) {
            avcodec_send_packet(audioCodecContext, m_Packet);
            //解码
            ret = avcodec_receive_frame(audioCodecContext, m_Frame);
            LOGE("audio ret:%d", ret);

            if (ret == 0) {
                //将每一帧数据转换成pcm
                swr_convert(swrContext, &out_buffer, 2 * 44100,
                            (const uint8_t **) m_Frame->data, m_Frame->nb_samples);
                //获取实际的缓存大小
                int out_buffer_size = av_samples_get_buffer_size(nullptr, outChannelCount,
                                                                 m_Frame->nb_samples, outFormat, 1);
                openSlRender->Render(out_buffer, out_buffer_size);
            }

            double timePerFrame = 1.0 * m_Frame->nb_samples * 1000 / 44100;
            double curr = ++currentIndex * timePerFrame;
            long duration = avFormatContext->duration / 1000;

            long durationAll = avFormatContext->streams[audio_stream_index]->duration;
            float percent2 = 1.0F * m_Packet->pts * 100 / durationAll;

            LOGE("percent2:%f pts:%ld  durationAll:%ld", ceil(percent2), m_Packet->pts * 100,
                 durationAll);

            progress(env, jobj, (long) curr, duration, ceil(percent2));
        }
    }
    av_packet_unref(m_Packet); //释放 m_Packet 引用，防止内存泄漏

    // 及时释放
    openSlRender->ReleaseRender();
//    av_frame_free(&inFrame);
    av_free(out_buffer);
    swr_free(&swrContext);
    avcodec_close(audioCodecContext);
    avformat_close_input(&avFormatContext);

    env->ReleaseStringUTFChars(src, src_path);

}