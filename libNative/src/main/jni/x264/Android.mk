LOCAL_PATH := $(call my-dir)
X264_SRC_LIBPATH := $(call my-dir)/build/lib/libx264.a

include $(CLEAR_VARS)
LOCAL_MODULE := x264
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(X264_SRC_LIBPATH)
include $(PREBUILT_STATIC_LIBRARY)
#include $(BUILD_STATIC_LIBRARY)
