//
// Created by 赵英坤 on 8/20/21.
//


extern "C" {
#include "JniProgress.h"
#include <code-self/GlobalMacro.h>

void progress(JNIEnv *env, jobject jobj, int progress) {
    jclass jclazz = env->FindClass("com/yocn/libnative/NativeProgress");
    jmethodID progressMethod = env->GetMethodID(jclazz, "progress", "(I)V");
    env->CallVoidMethod(jobj, progressMethod, progress);
}
}