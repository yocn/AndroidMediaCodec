#include <ffmpeg/libavutil/time.h>
#include <unistd.h>

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
#include <code-self/common/JniProgress.h>
}
#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_FFMpegSimpleVideoPlayerNormalTime_##name


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
JNI_METHOD_NAME(play)(JNIEnv *env, jobject jobj, jstring url, jobject surface);
}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(play)(JNIEnv *env, jobject jobj, jstring url, jobject surface) {
    jboolean copy;

    const char *m_Url = env->GetStringUTFChars(url, &copy);
    LOGE("-------------------------init-----------------m_Url--%s", m_Url);
//1.创建封装格式上下文
    AVFormatContext *m_AVFormatContext = avformat_alloc_context();

//2.打开输入文件，解封装
    if (avformat_open_input(&m_AVFormatContext, m_Url, nullptr, nullptr) != 0) {
        LOGCATE("DecoderBase::InitFFDecoder avformat_open_input fail.");
        return;
    }

//3.获取音视频流信息
    if (avformat_find_stream_info(m_AVFormatContext, nullptr) < 0) {
        LOGCATE("DecoderBase::InitFFDecoder avformat_find_stream_info fail.");
        return;
    }

    int m_StreamIndex = 0;
//4.获取音视频流索引
    for (int i = 0; i < m_AVFormatContext->nb_streams; i++) {
        if (m_AVFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            m_StreamIndex = i;
            break;
        }
    }

    if (m_StreamIndex == -1) {
        LOGCATE("DecoderBase::InitFFDecoder Fail to find stream index.");
        return;
    }
//5.获取解码器参数
    AVStream *avStream = m_AVFormatContext->streams[m_StreamIndex];
    AVCodecParameters *codecParameters = avStream->codecpar;
    double time_base = av_q2d(avStream->time_base);
    double fps = av_q2d(avStream->avg_frame_rate);
    LOGE("time_base::%lf fps:%lf", time_base, fps);

//6.根据 codec_id 获取解码器
    AVCodec *m_AVCodec = avcodec_find_decoder(codecParameters->codec_id);
    if (m_AVCodec == nullptr) {
        LOGCATE("DecoderBase::InitFFDecoder avcodec_find_decoder fail.");
        return;
    }

//7.创建解码器上下文
    AVCodecContext *m_AVCodecContext = avcodec_alloc_context3(m_AVCodec);
    if (avcodec_parameters_to_context(m_AVCodecContext, codecParameters) != 0) {
        LOGCATE("DecoderBase::InitFFDecoder avcodec_parameters_to_context fail.");
        return;
    }

//8.打开解码器
    int result = avcodec_open2(m_AVCodecContext, m_AVCodec, nullptr);
    if (result < 0) {
        LOGCATE("DecoderBase::InitFFDecoder avcodec_open2 fail. result=%d", result);
        return;
    }

//9.创建存储编码数据和解码数据的结构体
    AVPacket *m_Packet = av_packet_alloc(); //创建 AVPacket 存放编码数据
    AVFrame *m_Frame = av_frame_alloc(); //创建 AVFrame 存放解码后的数据

//2.1. 分配存储 RGB 图像的 buffer
    int m_VideoWidth = m_AVCodecContext->width;
    int m_VideoHeight = m_AVCodecContext->height;

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
                                              m_AVCodecContext->pix_fmt,
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

//10.解码循环
    while (av_read_frame(m_AVFormatContext, m_Packet) >= 0) { //读取帧
        if (m_Packet->stream_index == m_StreamIndex) {
            if (avcodec_send_packet(m_AVCodecContext, m_Packet) != 0) { //视频解码
                return;
            }
            while (avcodec_receive_frame(m_AVCodecContext, m_Frame) == 0) {
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

                long durationForRealTime = m_AVFormatContext->duration / 1000;
                double curr = m_Frame->pts * time_base;
                int percent = curr * 100 * 1000 / durationForRealTime;

                // 记录当前的时间戳
                gettimeofday(&tpend, nullptr);
                double curr_delta_time = get_base_time(&start_timeval, &tpend);
                // 相对于pts的时间计算应该sleep的时间
                double sleep_time = curr - curr_delta_time;
                LOGE("m_Frame:%lf  durationForRealTime:%ld %ld/%ld   curr_delta_time::%lf  sleep_time::%lf  percent:%d",
                     curr, durationForRealTime,
                     tpend.tv_sec, tpend.tv_usec, curr_delta_time, sleep_time, percent);

                for (int i = 0; i < m_VideoHeight; ++i) {
                    //一行一行地拷贝图像数据
                    memcpy(dstBuffer + i * dstLineSize, m_FrameBuffer + i * srcLineSize,
                           srcLineSize);
                }
//解锁当前 Window ，渲染缓冲区数据
                ANativeWindow_unlockAndPost(m_NativeWindow);
                progress(env, jobj, curr, durationForRealTime, percent);
                // sleep相应的毫秒
                usleep(sleep_time * 1000 * 1000);
            }
        }
        av_packet_unref(m_Packet); //释放 m_Packet 引用，防止内存泄漏
    }

//3.4. 释放 ANativeWindow
    if (m_NativeWindow)
        ANativeWindow_release(m_NativeWindow);

//2.4. 释放资源
    if (m_RGBAFrame != nullptr) {
        av_frame_free(&m_RGBAFrame);
        m_RGBAFrame = nullptr;
    }

    if (m_FrameBuffer != nullptr) {
        free(m_FrameBuffer);
        m_FrameBuffer = nullptr;
    }

    if (m_SwsContext != nullptr) {
        sws_freeContext(m_SwsContext);
        m_SwsContext = nullptr;
    }

//11.释放资源，解码完成
    if (m_Frame != nullptr) {
        av_frame_free(&m_Frame);
        m_Frame = nullptr;
    }

    if (m_Packet != nullptr) {
        av_packet_free(&m_Packet);
        m_Packet = nullptr;
    }

    if (m_AVCodecContext != nullptr) {
        avcodec_close(m_AVCodecContext);
        avcodec_free_context(&m_AVCodecContext);
        m_AVCodecContext = nullptr;
        m_AVCodec = nullptr;
    }

    if (m_AVFormatContext != nullptr) {
        avformat_close_input(&m_AVFormatContext);
        avformat_free_context(m_AVFormatContext);
        m_AVFormatContext = nullptr;
    }

}
