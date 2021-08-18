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
}

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_FFMpegSimpleAudioPlayer_##name

extern "C" {
JNIEXPORT void JNICALL
JNI_METHOD_NAME(playAudio)(JNIEnv *env, jobject jobj, jstring url);
}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(playAudio)(JNIEnv *env, jobject jobj, jstring url) {
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
        if (m_AVFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
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

    //1. 生成 resample 上下文，设置输入和输出的通道数、采样率以及采样格式，初始化上下文
    SwrContext *m_SwrContext = swr_alloc();

    int64_t AUDIO_DST_CHANNEL_LAYOUT = av_get_channel_layout("mono");;
    int64_t AUDIO_DST_SAMPLE_RATE = 16000;
    AVSampleFormat DST_SAMPLT_FORMAT = AV_SAMPLE_FMT_S16;
    int64_t NB_SAMPLES = av_get_channel_layout_nb_channels(AUDIO_DST_CHANNEL_LAYOUT);
    int AUDIO_DST_CHANNEL_COUNTS = 1;

    av_opt_set_int(m_SwrContext, "in_channel_layout", m_AVCodecContext->channel_layout, 0);
    av_opt_set_int(m_SwrContext, "out_channel_layout", AUDIO_DST_CHANNEL_LAYOUT, 0);
    av_opt_set_int(m_SwrContext, "in_sample_rate", m_AVCodecContext->sample_rate, 0);
    av_opt_set_int(m_SwrContext, "out_sample_rate", AUDIO_DST_SAMPLE_RATE, 0);
    av_opt_set_sample_fmt(m_SwrContext, "in_sample_fmt", m_AVCodecContext->sample_fmt, 0);
    av_opt_set_sample_fmt(m_SwrContext, "out_sample_fmt", DST_SAMPLT_FORMAT, 0);

    swr_init(m_SwrContext);

//2. 申请输出 Buffer
    int m_nbSamples = (int) av_rescale_rnd(NB_SAMPLES, AUDIO_DST_SAMPLE_RATE,
                                           m_AVCodecContext->sample_rate, AV_ROUND_UP);
    int m_BufferSize = av_samples_get_buffer_size(nullptr, AUDIO_DST_CHANNEL_COUNTS, m_nbSamples,
                                                  DST_SAMPLT_FORMAT, 1);
    auto m_AudioOutBuffer = (uint8_t *) malloc(m_BufferSize);

//9.创建存储编码数据和解码数据的结构体
    AVPacket *m_Packet = av_packet_alloc(); //创建 AVPacket 存放编码数据
    AVFrame *m_Frame = av_frame_alloc(); //创建 AVFrame 存放解码后的数据

    int frame_index = 0;
//10.解码循环
    while (av_read_frame(m_AVFormatContext, m_Packet) >= 0) { //读取帧
        if (m_Packet->stream_index == m_StreamIndex) {
            if (avcodec_send_packet(m_AVCodecContext, m_Packet) != 0) { //视频解码
                return;
            }
            while (avcodec_receive_frame(m_AVCodecContext, m_Frame) == 0) {
                frame_index++;

                //3. 重采样，frame 为解码帧
                int ret = swr_convert(m_SwrContext, &m_AudioOutBuffer, m_BufferSize / 2,
                                      (const uint8_t **) m_Frame->data, m_Frame->nb_samples);
                if (ret > 0) {
                    //play
                    LOGE("play----index::%d", frame_index);
                }
            }
        }
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