extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include <ffmpeg/libswscale/swscale.h>
#include <ffmpeg/libavutil/imgutils.h>
#include <android/native_window_jni.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include <GlobalMacro.h>
#include <Util.h>
}

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_FFMpegSimplePlayer_##name

extern "C" {
JNIEXPORT void JNICALL
JNI_METHOD_NAME(play)(JNIEnv *env, jobject jobj, jstring url, jobject surface);
}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(play)(JNIEnv *env, jobject jobj, jstring url, jobject surface) {
    jboolean copy;
    LOG(env, jobj);
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
    AVCodecParameters *codecParameters = m_AVFormatContext->streams[m_StreamIndex]->codecpar;

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
//计算 Buffer 的大小
    int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA, m_VideoWidth, m_VideoHeight, 1);
//为 m_RGBAFrame 分配空间
    auto *m_FrameBuffer = (uint8_t *) av_malloc(bufferSize * sizeof(uint8_t));
    av_image_fill_arrays(m_RGBAFrame->data, m_RGBAFrame->linesize, m_FrameBuffer, AV_PIX_FMT_RGBA,
                         m_VideoWidth, m_VideoHeight, 1);
//2.2. 获取转换的上下文
    SwsContext *m_SwsContext = sws_getContext(m_VideoWidth, m_VideoHeight,
                                              m_AVCodecContext->pix_fmt,
                                              m_RenderWidth, m_RenderHeight, AV_PIX_FMT_RGBA,
                                              SWS_FAST_BILINEAR, nullptr, nullptr, nullptr);

//3.1. 利用 Java 层 SurfaceView 传下来的 Surface 对象，获取 ANativeWindow
    ANativeWindow *m_NativeWindow = ANativeWindow_fromSurface(env, surface);

//3.2. 设置渲染区域和输入格式
    ANativeWindow_setBuffersGeometry(m_NativeWindow, m_VideoWidth,
                                     m_VideoHeight, WINDOW_FORMAT_RGBA_8888);

//10.解码循环
    while (av_read_frame(m_AVFormatContext, m_Packet) >= 0) { //读取帧
        if (m_Packet->stream_index == m_StreamIndex) {
            if (avcodec_send_packet(m_AVCodecContext, m_Packet) != 0) { //视频解码
                return;
            }
            while (avcodec_receive_frame(m_AVCodecContext, m_Frame) == 0) {
                //获取到 m_Frame 解码数据，在这里进行格式转换，然后进行渲染，下一节介绍 ANativeWindow 渲染过程

//2.3. 格式转换
                sws_scale(m_SwsContext, m_Frame->data, m_Frame->linesize, 0, m_VideoHeight,
                          m_RGBAFrame->data, m_RGBAFrame->linesize);

//3.3. 渲染
                ANativeWindow_Buffer m_NativeWindowBuffer;
//锁定当前 Window ，获取屏幕缓冲区 Buffer 的指针
                ANativeWindow_lock(m_NativeWindow, &m_NativeWindowBuffer, nullptr);
                auto *dstBuffer = static_cast<uint8_t *>(m_NativeWindowBuffer.bits);

                int srcLineSize = m_RGBAFrame->linesize[0];//输入图的步长（一行像素有多少字节）
                int dstLineSize = m_NativeWindowBuffer.stride * 4;//RGBA 缓冲区步长

                for (int i = 0; i < m_VideoHeight; ++i) {
                    //一行一行地拷贝图像数据
                    memcpy(dstBuffer + i * dstLineSize, m_FrameBuffer + i * srcLineSize,
                           srcLineSize);
                }
//解锁当前 Window ，渲染缓冲区数据
                ANativeWindow_unlockAndPost(m_NativeWindow);

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