#include <jni.h>
#include <string>
#include "libyuv/include/libyuv.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_yocn_meida_codec_jni_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_yocn_libyuv_YUVTransUtil_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT void JNICALL
Java_com_yocn_meida_codec_jni_RotateI420(JNIEnv *env, jobject type, jbyteArray input_,
                                         jbyteArray output_, jint in_width, jint in_height,
                                         jint rotation) {
    jbyte *srcData = env->GetByteArrayElements(input_, NULL);
    jbyte *dstData = env->GetByteArrayElements(output_, NULL);

}

