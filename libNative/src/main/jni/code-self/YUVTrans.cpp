#include <jni.h>
#include <string>
#include "libyuv/include/libyuv.h"
#include <android/log.h>

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)

//https://segmentfault.com/a/1190000005658738

extern "C" JNIEXPORT jstring JNICALL
Java_com_yocn_libnative_YUVTransUtil_stringFromJNI(
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
Java_com_yocn_libnative_YUVTransUtil_ARGBToI420(JNIEnv *env, jobject thiz,
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

    env->ReleaseByteArrayElements(src_argb, reinterpret_cast<jbyte *>(rgbBuffer), 0);
    env->ReleaseByteArrayElements(dst_y, reinterpret_cast<jbyte *>(yBuffer), 0);
    env->ReleaseByteArrayElements(dst_u, reinterpret_cast<jbyte *>(uBuffer), 0);
    env->ReleaseByteArrayElements(dst_v, reinterpret_cast<jbyte *>(vBuffer), 0);

}

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_YUVTransUtil_rotateI420(JNIEnv *env, jobject thiz,
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
    env->ReleaseByteArrayElements(src_y, reinterpret_cast<jbyte *>(ySrc), 0);
    env->ReleaseByteArrayElements(src_u, reinterpret_cast<jbyte *>(uSrc), 0);
    env->ReleaseByteArrayElements(src_v, reinterpret_cast<jbyte *>(vSrc), 0);

    env->ReleaseByteArrayElements(dst_y, reinterpret_cast<jbyte *>(yDst), 0);
    env->ReleaseByteArrayElements(dst_u, reinterpret_cast<jbyte *>(uDst), 0);
    env->ReleaseByteArrayElements(dst_v, reinterpret_cast<jbyte *>(vDst), 0);

}


extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_YUVTransUtil_rotateI420Full(JNIEnv *env, jobject thiz,
                                                    jbyteArray src_yuv,
                                                    jbyteArray dst_yuv,
                                                    int width, int height, int rotate
) {

    uint8_t *yuvSrc = (uint8_t *) env->GetByteArrayElements(src_yuv, 0);
    uint8_t *yuvDst = (uint8_t *) env->GetByteArrayElements(dst_yuv, 0);


    libyuv::RotationMode rotateMode = libyuv::kRotate0;
    if (rotate == 90) {
        rotateMode = libyuv::kRotate90;
    } else if (rotate == 180) {
        rotateMode = libyuv::kRotate180;
    } else if (rotate == 270) {
        rotateMode = libyuv::kRotate270;
    }
    LOGV("convertToArgb  1");
    libyuv::I420Rotate(yuvSrc, width,
                       yuvSrc + width, width / 4,
                       yuvSrc + width + width / 4, width / 4,
                       yuvDst, width,
                       yuvDst + width, width / 4,
                       yuvDst + width + width / 4, width / 4,
                       width, height, rotateMode);
    env->ReleaseByteArrayElements(src_yuv, reinterpret_cast<jbyte *>(yuvSrc), 0);
    env->ReleaseByteArrayElements(dst_yuv, reinterpret_cast<jbyte *>(yuvDst), 0);

}

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_YUVTransUtil_I420ToArgb(JNIEnv *env, jobject thiz,
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

    auto yuvFrame = (uint8_t *) env->GetByteArrayElements(src_frame, 0);
    auto *rgbBuffer = (uint8_t *) env->GetByteArrayElements(dst_argb, 0);

    LOGV("convertToArgb  1");
    libyuv::ConvertToARGB(yuvFrame, src_size, rgbBuffer, dst_stride_argb, crop_x, crop_y,
                          src_width,
                          src_height,
                          crop_width,
                          crop_height,
                          rotateMode,
                          libyuv::FOURCC_IYUV);

    env->ReleaseByteArrayElements(dst_argb, reinterpret_cast<jbyte *>(rgbBuffer), 0);
    env->ReleaseByteArrayElements(src_frame, reinterpret_cast<jbyte *>(yuvFrame), 0);

}

//do not use
extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_YUVTransUtil_NV21ToArgb(JNIEnv *env, jobject thiz,
                                                jbyteArray src_y, int src_stride_y,
                                                jbyteArray src_vu, int src_stride_vu,
                                                jbyteArray dst_argb, int dst_stride_argb,
                                                int width,
                                                int height) {

    uint8_t *srcY = (uint8_t *) env->GetByteArrayElements(src_y, 0);
    uint8_t *srcUv = (uint8_t *) env->GetByteArrayElements(src_vu, 0);
    uint8_t *dstARGB = (uint8_t *) env->GetByteArrayElements(dst_argb, 0);

    LOGV("NV21ToArgb 123:%d,%d,%d  45:%d,%d", src_stride_y, src_stride_vu, dst_stride_argb, width,
         height);
    libyuv::NV12ToARGB(srcY, src_stride_y,
                       srcUv, src_stride_vu,
                       dstARGB, dst_stride_argb,
                       width, height
    );

    env->ReleaseByteArrayElements(src_y, reinterpret_cast<jbyte *>(srcY), 0);
    env->ReleaseByteArrayElements(src_vu, reinterpret_cast<jbyte *>(srcUv), 0);
    env->ReleaseByteArrayElements(dst_argb, reinterpret_cast<jbyte *>(dstARGB), 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_YUVTransUtil_NV21ToI420(JNIEnv *env, jobject thiz,
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

    env->ReleaseByteArrayElements(src_y, reinterpret_cast<jbyte *>(srcY), 0);
    env->ReleaseByteArrayElements(src_vu, reinterpret_cast<jbyte *>(srcUv), 0);
    env->ReleaseByteArrayElements(dst_y, reinterpret_cast<jbyte *>(dstY), 0);
    env->ReleaseByteArrayElements(dst_u, reinterpret_cast<jbyte *>(dstU), 0);
    env->ReleaseByteArrayElements(dst_v, reinterpret_cast<jbyte *>(dstV), 0);
}

//ARGBRotate

extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_YUVTransUtil_ARGBRotate(JNIEnv *env, jobject thiz,
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

    env->ReleaseByteArrayElements(src_argb, reinterpret_cast<jbyte *>(srcARGB), 0);
    env->ReleaseByteArrayElements(dst_argb, reinterpret_cast<jbyte *>(dstARGB), 0);
}


