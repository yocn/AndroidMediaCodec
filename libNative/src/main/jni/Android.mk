LOCAL_PATH := $(call my-dir)

INCLUDE_LIBYUV := $(LOCAL_PATH)/libyuv/include
INCLUDE_X264 := $(LOCAL_PATH)/X264/build/include

GLOBAL_C_INCLUDES := $(INCLUDE_LIBYUV)/ \
$(INCLUDE_X264)/

include $(CLEAR_VARS)

# module名字叫myYuv，生成libNative.so
LOCAL_MODULE := Native
# 引用libyuv的头文件
LOCAL_C_INCLUDES := $(GLOBAL_C_INCLUDES)
# jni调用libyuv
LOCAL_SRC_FILES := YUVTrans.cpp
# 引用Android JNI Log 使用#include <android/log.h> 必须添加，否则会报error: undefined reference to '__android_log_print'
LOCAL_LDLIBS    := -llog

# 引用静态库libyuv_static.a
LOCAL_STATIC_LIBRARIES := libyuv_static libx264

include $(BUILD_SHARED_LIBRARY)

# 找到子目录下所有的Android.mk执行
include $(call all-makefiles-under,$(LOCAL_PATH))