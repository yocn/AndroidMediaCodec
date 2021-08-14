extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
}

#include <android/log.h>
#include <jni.h>

#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"yocn",__VA_ARGS__)

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_TestFFmpeg_##name

extern "C" {
JNIEXPORT void JNICALL JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobject);
}

JNIEXPORT void JNICALL JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobject) {
    LOGE("-------------------------init-------------------");

}
