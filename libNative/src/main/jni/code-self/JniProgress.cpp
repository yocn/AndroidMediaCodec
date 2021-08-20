//
// Created by 赵英坤 on 8/20/21.
//


extern "C" {
#include "JniProgress.h"
#include <code-self/GlobalMacro.h>

void progress(JNIEnv *env, jobject jobj, long curr, long total, int progress) {
    jclass jclazz = env->FindClass("com/yocn/libnative/NativeProgress");
    jmethodID progressMethod = env->GetMethodID(jclazz, "progress", "(JJI)V");
    env->CallVoidMethod(jobj, progressMethod, curr, total, progress);
}
}