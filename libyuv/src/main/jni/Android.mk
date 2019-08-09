LOCAL_PATH := $(call my-dir)

GLOBAL_C_INCLUDES :=$(LOCAL_PATH)/libyuv/include

include $(CLEAR_VARS)

#include $(LOCAL_PATH)/jni/Android.mk

LOCAL_MODULE    := YUVTrans
LOCAL_SRC_FILES := yuvInterface/YUVTrans.cpp

LOCAL_C_INCLUDES := $(GLOBAL_C_INCLUDES)
include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))