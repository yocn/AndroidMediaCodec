LOCAL_PATH := $(call my-dir)

GLOBAL_C_INCLUDES :=$(LOCAL_PATH)/libyuv/include

include $(CLEAR_VARS)

# module名字叫myYuv，生成libmyYuv.so
LOCAL_MODULE := myYuv
# 引用libyuv的头文件
LOCAL_C_INCLUDES := $(GLOBAL_C_INCLUDES)
# jni调用libyuv
LOCAL_SRC_FILES := YUVTrans.cpp
# 引用Android JNI Log
LOCAL_LDLIBS    := -llog

# 引用静态库libyuv_static.a
LOCAL_STATIC_LIBRARIES := libyuv_static

include $(BUILD_SHARED_LIBRARY)

# 找到子目录下所有的Android.mk执行
include $(call all-makefiles-under,$(LOCAL_PATH))