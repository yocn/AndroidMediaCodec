#include <jni.h>
#include <string>
#include <stdint.h>
#include <inttypes.h>
#include "x264/build/include/x264.h"
#include <android/log.h>

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)

typedef struct _Encoder{
    x264_param_t * param;
    x264_t *handle;
    x264_picture_t * picture;
    x264_nal_t  *nal;
} Encoder;

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_X264Translater_initX264Encoder(JNIEnv *env, jobject thiz,
                                                int width, int height, int fps, int bite) {

//    x264_encoder_open();
//    x264_param_default()
}



