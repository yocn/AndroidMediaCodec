extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
}

#include <android/log.h>
#include <jni.h>

#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, "yocn", __VA_ARGS__)
#define LOGCATE LOGE

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_TestFFmpeg_##name

extern "C" {
JNIEXPORT void JNICALL JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobject, jstring url);
}

JNIEXPORT void JNICALL JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobject, jstring url) {
    LOGE("-------------------------init-------------------");
    jboolean copy;
    const char *m_Url = env->GetStringUTFChars(url, &copy);

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

    int m_StreamIndex = -1;
    int m_MediaType = AVMEDIA_TYPE_AUDIO;
//4.获取音视频流索引
    for (int i = 0; i < m_AVFormatContext->nb_streams; i++) {
        if (m_AVFormatContext->streams[i]->codecpar->codec_type == m_MediaType) {
            m_StreamIndex = i;
            LOGE("-------------------------m_StreamIndex：：：%d", i);
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
    const AVCodec *m_AVCodec = avcodec_find_decoder(codecParameters->codec_id);
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
//10.解码循环
    while (av_read_frame(m_AVFormatContext, m_Packet) >= 0) { //读取帧
        if (m_Packet->stream_index == m_StreamIndex) {
            if (avcodec_send_packet(m_AVCodecContext, m_Packet) != 0) { //视频解码
                LOGE("frame index:%d", m_StreamIndex);
                return;
            }
            while (avcodec_receive_frame(m_AVCodecContext, m_Frame) == 0) {
                //获取到 m_Frame 解码数据，在这里进行格式转换，然后进行渲染，下一节介绍 ANativeWindow 渲染过程
            }
        }
        av_packet_unref(m_Packet); //释放 m_Packet 引用，防止内存泄漏
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
