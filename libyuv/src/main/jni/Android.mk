LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#include $(LOCAL_PATH)/jni/Android.mk

LOCAL_MODULE    := YUVTrans
LOCAL_SRC_FILES := yuvInterface/YUVTrans.cpp

include $(BUILD_SHARED_LIBRARY)