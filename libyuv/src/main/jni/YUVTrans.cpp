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

}

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_rotateI420(JNIEnv *env, jobject thiz,
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
    LOGV("convertToArgb  1");
    libyuv::I420Rotate(ySrc, width,
                       uSrc, width >> 1,
                       vSrc, width >> 1,
                       yDst, height,
                       uDst, height >> 1,
                       vDst, height >> 1,
                       width, height, rotateMode);

}

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_I420ToArgb(JNIEnv *env, jobject thiz,
                                             jbyteArray src_frame, int src_size,
                                             jbyteArray dst_argb, int dst_stride_argb,
                                             int crop_x, int crop_y,
                                             int src_width, int src_height,
                                             int crop_width, int crop_height,
                                             int rotate,
                                             int format) {

    libyuv::RotationMode rotateMode = libyuv::kRotate0;
    if (rotate == 90) {
        rotateMode = libyuv::kRotate90;
    } else if (rotate == 180) {
        rotateMode = libyuv::kRotate180;
    } else if (rotate == 270) {
        rotateMode = libyuv::kRotate270;
    }

    uint8_t *yuvFrame = (uint8_t *) env->GetByteArrayElements(src_frame, 0);
    uint8_t *rgbBuffer = (uint8_t *) env->GetByteArrayElements(dst_argb, 0);

    LOGV("convertToArgb  1");
    libyuv::ConvertToARGB(yuvFrame, src_size, rgbBuffer, dst_stride_argb, crop_x, crop_y,
                          src_width,
                          src_height,
                          crop_width,
                          crop_height,
                          rotateMode,
                          libyuv::FOURCC_IYUV);
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

    LOGV("NV21ToArgb");
    libyuv::NV12ToARGB(srcY, src_stride_y,
                       srcUv, src_stride_vu,
                       dstARGB, dst_stride_argb,
                       width, height
    );
}

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_NV21ToI420(JNIEnv *env, jobject thiz,
                                             jbyteArray src_y, int src_stride_y,
                                             jbyteArray src_vu, int src_stride_vu,
                                             jbyteArray dst_y, int dst_stride_y,
                                             jbyteArray dst_u, int dst_stride_u,
                                             jbyteArray dst_v, int dst_stride_v,
                                             int width,
                                             int height) {

    uint8_t *srcY = (uint8_t *) env->GetByteArrayElements(src_y, 0);
    uint8_t *srcUv = (uint8_t *) env->GetByteArrayElements(src_vu, 0);
    uint8_t *dstY = (uint8_t *) env->GetByteArrayElements(dst_y, 0);
    uint8_t *dstU = (uint8_t *) env->GetByteArrayElements(dst_u, 0);
    uint8_t *dstV = (uint8_t *) env->GetByteArrayElements(dst_v, 0);

    LOGV("NV21ToI420");
    libyuv::NV21ToI420(srcY, src_stride_y,
                       srcUv, src_stride_vu,
                       dstY, dst_stride_y,
                       dstU, dst_stride_u,
                       dstV, dst_stride_v,
                       width, height
    );
}
//ARGBRotate

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libyuv_YUVTransUtil_ARGBRotate(JNIEnv *env, jobject thiz,
                                             jbyteArray src_argb, int src_stride_argb,
                                             jbyteArray dst_argb, int dst_stride_argb,
                                             int width, int height, int rotate) {

    uint8_t *srcARGB = (uint8_t *) env->GetByteArrayElements(src_argb, 0);
    uint8_t *dstARGB = (uint8_t *) env->GetByteArrayElements(dst_argb, 0);

    libyuv::RotationMode rotateMode = libyuv::kRotate0;
    if (rotate == 90) {
        rotateMode = libyuv::kRotate90;
    } else if (rotate == 180) {
        rotateMode = libyuv::kRotate180;
    } else if (rotate == 270) {
        rotateMode = libyuv::kRotate270;
    }

    LOGV("ARGBRotate");
    /*
     * int ARGBRotate(const uint8_t* src_argb,
               int src_stride_argb,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height,
               enum RotationMode mode)
     */
    libyuv::ARGBRotate(srcARGB, src_stride_argb, dstARGB, dst_stride_argb, width, height,
                       rotateMode);
}


