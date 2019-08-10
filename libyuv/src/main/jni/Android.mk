LOCAL_PATH := $(call my-dir)

GLOBAL_C_INCLUDES :=$(LOCAL_PATH)/libyuv/include

include $(CLEAR_VARS)

LOCAL_MODULE := myuv
LOCAL_C_INCLUDES := $(GLOBAL_C_INCLUDES)

LOCAL_SRC_FILES := yuvInterface/YUVTrans.cpp
LOCAL_LDLIBS    := -llog

LOCAL_STATIC_LIBRARIES := libyuv_static

include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))