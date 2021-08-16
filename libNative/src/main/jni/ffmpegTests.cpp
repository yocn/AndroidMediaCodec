extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include <ffmpeg/libswscale/swscale.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
}

#include <android/log.h>
#include <jni.h>

#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, "yocn", __VA_ARGS__)
#define LOGCATE LOGE

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_TestFFmpeg_##name

extern "C" {
JNIEXPORT void JNICALL
JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobject, jstring url, jstring out_url);
JNIEXPORT void JNICALL
JNI_METHOD_NAME(decode2Yuv)(JNIEnv *env, jobject jobject, jstring url, jstring out_url);
}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobject, jstring url, jstring out_url) {
    jboolean copy;
    const char *m_Url = env->GetStringUTFChars(url, &copy);
    const char *m_Out_Url = env->GetStringUTFChars(out_url, &copy);
    LOGE("-------------------------init-----------------m_Url--%s", m_Url);
    LOGE("-------------------------init-----------------m_Out_Url--%s", m_Out_Url);

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

    //转换器上下文  转换方法需要
    SwsContext *pSwsContext = sws_getContext(m_AVCodecContext->width, m_AVCodecContext->height,
                                             m_AVCodecContext->pix_fmt,
                                             m_AVCodecContext->width, m_AVCodecContext->height,
                                             AV_PIX_FMT_YUV420P, SWS_BILINEAR, nullptr, nullptr,
                                             nullptr);

    //打开用于输出的文件
    FILE *fp_yuv = fopen(m_Out_Url, "wb");

    //9.创建存储编码数据和解码数据的结构体
    AVPacket *m_Packet = av_packet_alloc(); //创建 AVPacket 存放编码数据
    AVFrame *m_Frame = av_frame_alloc(); //创建 AVFrame 存放解码后的数据
    AVFrame *pYUVFrame = av_frame_alloc();    //转换后YUV的帧
//10.解码循环
    while (av_read_frame(m_AVFormatContext, m_Packet) >= 0) { //读取帧
        if (m_Packet->stream_index == m_StreamIndex) {
            if (avcodec_send_packet(m_AVCodecContext, m_Packet) != 0) { //视频解码
                LOGE("frame index:%d", m_StreamIndex);
                return;
            }
            while (avcodec_receive_frame(m_AVCodecContext, m_Frame) == 0) {
                //获取到 m_Frame 解码数据，在这里进行格式转换，然后进行渲染，下一节介绍 ANativeWindow 渲染过程
                LOGE("解码=%d  format::%d linesize->%d w/d::%d/%d", m_AVCodecContext->frame_number,
                     m_Frame->format, m_Frame->linesize[0], m_Frame->width, m_Frame->height);
                //转换
                sws_scale(pSwsContext,
                          (const uint8_t *const *) m_Frame->data,
                          m_Frame->linesize,
                          0,
                          m_Frame->height,
                          pYUVFrame->data,
                          pYUVFrame->linesize);

                //输出至文件
                int y_size = m_AVCodecContext->width * m_AVCodecContext->height;
                fwrite(m_Frame->data[0], 1, y_size, fp_yuv);
                fwrite(m_Frame->data[1], 1, y_size / 4, fp_yuv);
                fwrite(m_Frame->data[2], 1, y_size / 4, fp_yuv);
            }
        }
        av_packet_unref(m_Packet); //释放 m_Packet 引用，防止内存泄漏
    }

    fclose(fp_yuv);
//11.释放资源，解码完成
    if (m_Frame != nullptr) {
        av_frame_free(&m_Frame);
        m_Frame = nullptr;
    }

    if (pYUVFrame != nullptr) {
        av_frame_free(&pYUVFrame);
        pYUVFrame = nullptr;
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

JNIEXPORT void JNICALL
JNI_METHOD_NAME(decode2Yuv)(JNIEnv *env, jobject, jstring input_, jstring output_) {

//ffmpeg 开始前必掉用此函数
    av_register_all();

//路径  java String 转换 C 字符串
    jboolean copy;
    const char *inputStr = env->GetStringUTFChars(input_, &copy);
    const char *outputStr = env->GetStringUTFChars(output_, &copy);

//AVFormatContext结构体开辟内存
    AVFormatContext *pContext = avformat_alloc_context();

//Open an input stream and read the header
    if (avformat_open_input(&pContext, inputStr, nullptr, nullptr) < 0) {
        LOGE("打开失败");
    }

//Read packets of a media file to get stream information
    if (avformat_find_stream_info(pContext, nullptr) < 0) {
        LOGE("获取信息失败");
    }

    int video_stream_idx = -1;

//找到视频流
    for (int i = 0; i < pContext->nb_streams; ++i) {
        LOGE("循环流 %d", i);
        if (pContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_idx = i;
        }
    }

//    获取到解码器上下文
    AVCodecContext *codecContext = pContext->streams[video_stream_idx]->codec;
//    获取到解码器
    AVCodec *decoder = avcodec_find_decoder(codecContext->codec_id);

//Initialize the AVCodecContext to use the given AVCodec 在此为解码器上下文初始化？（不太确定）
    if (avcodec_open2(codecContext, decoder, nullptr) < 0) {
        LOGE("解码失败");
    }

//包
    AVPacket *avPacket = (AVPacket *) av_malloc(sizeof(AVPacket));
    av_init_packet(avPacket);
//解码的帧
    AVFrame *frame = av_frame_alloc();
//转换后YUV的帧
    AVFrame *pYUVFrame = av_frame_alloc();

    LOGE("宽 %d  高 %d", codecContext->width, codecContext->height);

//解码入参出参 用于标记 帧
    int got_frame;

//转换器上下文  转换方法需要
    SwsContext *pSwsContext = sws_getContext(codecContext->width, codecContext->height,
                                             codecContext->pix_fmt,
                                             codecContext->width, codecContext->height,
                                             AV_PIX_FMT_YUV420P, SWS_BILINEAR, nullptr, nullptr,
                                             nullptr);

//打开用于输出的文件
    FILE *fp_yuv = fopen(outputStr, "wb");

    while (av_read_frame(pContext, avPacket) >= 0) {
        avcodec_decode_video2(codecContext, frame, &got_frame, avPacket);
        if (got_frame > 0) {
            LOGE("解码=%d  format::%d linesize->%d w/d::%d/%d", codecContext->frame_number,
                 frame->format,
                 frame->linesize[0], frame->width, frame->height);
//转换
            sws_scale(pSwsContext,
                      (const uint8_t *const *) frame->data,
                      frame->linesize,
                      0,
                      frame->height,
                      pYUVFrame->data,
                      pYUVFrame->linesize);

//输出至文件
            int y_size = codecContext->width * codecContext->height;
            fwrite(frame->data[0], 1, y_size, fp_yuv);
            fwrite(frame->data[1], 1, y_size / 4, fp_yuv);
            fwrite(frame->data[2], 1, y_size / 4, fp_yuv);
        }
        av_free_packet(avPacket);
    }

//关闭文件  释放内存
    fclose(fp_yuv);
    av_frame_free(&frame);
    av_frame_free(&pYUVFrame);
    avcodec_close(codecContext);
    avformat_free_context(pContext);
}