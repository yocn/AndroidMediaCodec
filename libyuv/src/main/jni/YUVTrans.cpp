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

    libyuv::ARGBToI420(rgbBuffer, src_stride_argb, yBuffer, dst_stride_y, uBuffer, dst_stride_u,
                       vBuffer, dst_stride_v, width, height);

//    env->ReleaseByteArrayElements(src_argb, (jbyte *) rgbBuffer, NULL);
//    env->ReleaseByteArrayElements(src_argb, (jbyte *) yBuffer, NULL);
//    env->ReleaseByteArrayElements(src_argb, (jbyte *) uBuffer, NULL);
//    env->ReleaseByteArrayElements(src_argb, (jbyte *) vBuffer, NULL);

}

void rotateI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint degree) {
    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    if (degree == libyuv::kRotate90 || degree == libyuv::kRotate270) {
        libyuv::I420Rotate((const uint8_t *) src_i420_y_data, width,
                           (const uint8_t *) src_i420_u_data, width >> 1,
                           (const uint8_t *) src_i420_v_data, width >> 1,
                           (uint8_t *) dst_i420_y_data, height,
                           (uint8_t *) dst_i420_u_data, height >> 1,
                           (uint8_t *) dst_i420_v_data, height >> 1,
                           width, height,
                           (libyuv::RotationMode) degree);
    }
}


extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_rotateYUV420(JNIEnv *env, jobject thiz,
                                               jbyteArray src_y,
                                               jbyteArray src_u,
                                               jbyteArray src_v,
                                               jbyteArray dst_y,
                                               jbyteArray dst_u,
                                               jbyteArray dst_v,
                                               int width, int height, int rotate
) {

    uint8_t *ySrc = (uint8_t *) env->GetByteArrayElements(src_y, 0);
    uint8_t *uSrc = (uint8_t *) env->GetByteArrayElements(src_u, 0);
    uint8_t *vSrc = (uint8_t *) env->GetByteArrayElements(src_v, 0);

    uint8_t *yDst = (uint8_t *) env->GetByteArrayElements(dst_y, 0);
    uint8_t *uDst = (uint8_t *) env->GetByteArrayElements(dst_u, 0);
    uint8_t *vDst = (uint8_t *) env->GetByteArrayElements(dst_v, 0);

    libyuv::RotationMode rotateMode = libyuv::kRotate0;
    if (rotate == 90) {
        rotateMode = libyuv::kRotate90;
    } else if (rotate == 180) {
        rotateMode = libyuv::kRotate180;
    } else if (rotate == 270) {
        rotateMode = libyuv::kRotate270;
    }
    /**
     *
int I420Rotate(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_u,
               int dst_stride_u,
               uint8_t* dst_v,
               int dst_stride_v,
               int width,
               int height,
               enum RotationMode mode);
     */
    LOGV("convertToArgb  1");
    libyuv::I420Rotate(ySrc, width,
                       uSrc, width >> 1,
                       vSrc, width >> 1,
                       yDst, height,
                       uDst, height >> 1,
                       vDst, height >> 1,
                       width, height, rotateMode);

    LOGV("convertToArgb  2");
//    env->ReleaseByteArrayElements(src_frame, (jbyte *) yuvFrame, 0);
//    env->ReleaseByteArrayElements(dst_argb, (jbyte *) rgbBuffer, 0);
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
    libyuv::ConvertToARGB(yuvFrame, src_size, rgbBuffer, dst_stride_argb, crop_x, crop_y,
                          src_width,
                          src_height,
                          crop_width,
                          crop_height,
                          libyuv::kRotate90,
                          libyuv::FOURCC_IYUV);


    LOGV("convertToArgb  2");
//    env->ReleaseByteArrayElements(src_frame, (jbyte *) yuvFrame, 0);
//    env->ReleaseByteArrayElements(dst_argb, (jbyte *) rgbBuffer, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_NV21ToArgb(JNIEnv *env, jobject thiz,
                                             jbyteArray src_y, int src_stride_y,
                                             jbyteArray src_vu, int src_stride_vu,
                                             jbyteArray dst_argb, int dst_stride_argb,
                                             int width,
                                             int height) {

    uint8_t *srcY = (uint8_t *) env->GetByteArrayElements(src_y, 0);
    uint8_t *srcUv = (uint8_t *) env->GetByteArrayElements(src_vu, 0);
    uint8_t *dstARGB = (uint8_t *) env->GetByteArrayElements(dst_argb, 0);

    LOGV("NV21ToArgb  1");
    /**
     * int NV21ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_vu,
               int src_stride_vu,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);
     */
    libyuv::NV21ToARGB(srcY, src_stride_y,
                       srcUv, src_stride_vu,
                       dstARGB, dst_stride_argb,
                       width, height
    );

    LOGV("NV21ToArgb  2");
//    env->ReleaseByteArrayElements(src_frame, (jbyte *) yuvFrame, 0);
//    env->ReleaseByteArrayElements(dst_argb, (jbyte *) rgbBuffer, 0);
}
