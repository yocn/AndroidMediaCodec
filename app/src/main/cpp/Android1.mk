LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(LOCAL_PATH)/jni/Android.mk

LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := native-lib.cpp

include $(BUILD_SHARED_LIBRARY)