//
// Created by 赵英坤 on 8/20/21.
//

#ifndef ANDROIDMEDIACODEC_JNIPROGRESS_H
#define ANDROIDMEDIACODEC_JNIPROGRESS_H

extern "C" {
#include <code-self/GlobalMacro.h>
void progress(JNIEnv *env, jobject jobj, long curr, long total, int progress);
}

#endif //ANDROIDMEDIACODEC_JNIPROGRESS_H
