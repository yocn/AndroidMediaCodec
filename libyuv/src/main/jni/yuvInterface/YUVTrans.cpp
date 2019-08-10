#include <jni.h>
#include <string>
#include "libyuv/include/libyuv.h"
#include <android/log.h>

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_yocn_libyuv_YUVTransUtil_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

/**
 * int I420ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);
 */
//
extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_ARGBToI420(JNIEnv *env, jobject thiz,
                                             jbyteArray src_argb, int src_stride_argb,
                                             jbyteArray dst_y, int dst_stride_y,
                                             jbyteArray dst_u, int dst_stride_u,
                                             jbyteArray dst_v, int dst_stride_v,
                                             int width, int height) {
    uint8_t *rgbBuffer = (uint8_t *) env->GetByteArrayElements(src_argb, NULL);
    uint8_t *yBuffer = (uint8_t *) env->GetByteArrayElements(dst_y, NULL);
    uint8_t *uBuffer = (uint8_t *) env->GetByteArrayElements(dst_u, NULL);
    uint8_t *vBuffer = (uint8_t *) env->GetByteArrayElements(dst_v, NULL);

    LOGV("ARGBToI420  1");

    libyuv::ARGBToI420(rgbBuffer, src_stride_argb, yBuffer, dst_stride_y, uBuffer, dst_stride_u,
                       vBuffer, dst_stride_v, width, height);

    LOGV("ARGBToI420  2");
//    env->ReleaseByteArrayElements(src_argb, (jbyte *) rgbBuffer, NULL);
//    env->ReleaseByteArrayElements(src_argb, (jbyte *) yBuffer, NULL);
//    env->ReleaseByteArrayElements(src_argb, (jbyte *) uBuffer, NULL);
//    env->ReleaseByteArrayElements(src_argb, (jbyte *) vBuffer, NULL);

}
extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_convertToArgb(JNIEnv *env, jobject thiz,
                                                jbyteArray src_frame, int src_size,
                                                jbyteArray dst_argb, int dst_stride_argb,
                                                int crop_x, int crop_y,
                                                int src_width, int src_height,
                                                int crop_width, int crop_height,
                                                int rotation,
                                                int format) {

    uint8_t *yuvFrame = (uint8_t *) env->GetByteArrayElements(src_frame, 0);
    uint8_t *rgbBuffer = (uint8_t *) env->GetByteArrayElements(dst_argb, 0);

    LOGV("convertToArgb  1");
    libyuv::ConvertToARGB(yuvFrame, src_size, rgbBuffer, dst_stride_argb, crop_x, crop_y, src_width,
                          src_height, crop_width, crop_height, libyuv::kRotate0,
                          libyuv::FOURCC_IYUV);

    LOGV("convertToArgb  2");
//    env->ReleaseByteArrayElements(src_frame, (jbyte *) yuvFrame, 0);
//    env->ReleaseByteArrayElements(dst_argb, (jbyte *) rgbBuffer, 0);
}
