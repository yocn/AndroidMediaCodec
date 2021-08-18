//
// Created by 赵英坤 on 8/17/21.
//

#ifndef ANDROIDMEDIACODEC_GLOBALMACRO_H
#define ANDROIDMEDIACODEC_GLOBALMACRO_H

extern "C" {
#include <jni.h>
#include <android/log.h>
}

#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, "LogUtil", __VA_ARGS__)
#define LOGCATE LOGE

#endif //ANDROIDMEDIACODEC_GLOBALMACRO_H

