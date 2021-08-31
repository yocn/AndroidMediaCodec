//
// Created by 赵英坤 on 8/31/21.
//

#include <cstdio>
#include "ReadByteBuffer.h"

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_ReadByteBuffer_##name

extern "C" {
#include <GlobalMacro.h>
JNIEXPORT long JNICALL JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobj, jstring path);
JNIEXPORT void JNICALL
JNI_METHOD_NAME(read)(JNIEnv *env, jobject jobj, jobject buffer, jlong address);
JNIEXPORT void JNICALL JNI_METHOD_NAME(flush)(JNIEnv *env, jobject jobj, long address);
}

JNIEXPORT long JNICALL JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobj, jstring path) {
    const char *m_Path = env->GetStringUTFChars(path, nullptr);
    FILE *file = fopen(m_Path, "wb");
    return reinterpret_cast<long>(file);
}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(read)(JNIEnv *env, jobject jobj, jobject buffer, jlong address) {
    FILE *file = reinterpret_cast<FILE *>(address);
    void *buff = env->GetDirectBufferAddress(buffer);
    long size = env->GetDirectBufferCapacity(buffer);
    LOGE("size::%ld", size);
    fwrite(buff, size, 1, file);
}

JNIEXPORT void JNICALL JNI_METHOD_NAME(flush)(JNIEnv *env, jobject jobj, jlong address) {
    FILE *file = reinterpret_cast<FILE *>(address);
    fclose(file);
}

