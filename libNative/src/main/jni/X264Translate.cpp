#include <jni.h>
#include <string>
#include <stdint.h>
#include <inttypes.h>
#include "x264/build/include/x264.h"
#include <android/log.h>

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)

//typedef struct _Encoder {
//    x264_param_t *param;
//    x264_t *handle;
//    x264_picture_t *picture;
//    x264_nal_t *nal;
//} Encoder;

typedef struct {
    x264_param_t *param;
    x264_t *handle;
    x264_picture_t *picture;
    x264_nal_t *nal;
} Encoder;

jlong Java_h264_com_H264Encoder_CompressBegin(JNIEnv *env, jobject thiz,
                                              jint width, jint height) {
    Encoder *en = (Encoder *) malloc(sizeof(Encoder));
    en->param = (x264_param_t *) malloc(sizeof(x264_param_t));
    en->picture = (x264_picture_t *) malloc(sizeof(x264_picture_t));
    x264_param_default(en->param); //set default param
    //en->param->rc.i_rc_method = X264_RC_CQP;
    en->param->i_log_level = X264_LOG_NONE;
    en->param->i_width = width; //set frame width
    en->param->i_height = height; //set frame height
    en->param->rc.i_lookahead = 0;
    en->param->i_bframe = 0;
    en->param->i_fps_num = 5;
    en->param->i_fps_den = 1;
    if ((en->handle = x264_encoder_open(en->param)) == 0) {
        return 0;
    }
    /* Create a new pic */
    x264_picture_alloc(en->picture, X264_CSP_I420, en->param->i_width,
                       en->param->i_height);
    return (jlong) en;
}

jint Java_h264_com_H264Encoder_CompressEnd(JNIEnv *env, jobject thiz, jlong handle) {
    Encoder *en = (Encoder *) handle;
    if (en->picture) {
        x264_picture_clean(en->picture);
        free(en->picture);
        en->picture = 0;
    }
    if (en->param) {
        free(en->param);
        en->param = 0;
    }
    if (en->handle) {
        x264_encoder_close(en->handle);
    }
    free(en);
    return 0;
}

jint Java_h264_com_H264Encoder_CompressBuffer(JNIEnv *env, jobject thiz, jlong handle, jint type,
                                              jbyteArray in, jint insize, jbyteArray out) {
    Encoder *en = (Encoder *) handle;
    x264_picture_t pic_out;
    int i_data = 0;
    int nNal = -1;
    int result = 0;
    int i = 0, j = 0;
    int nPix = 0;
    jbyte *Buf = (jbyte *) env->GetByteArrayElements(in, 0);
    jbyte *h264Buf = (jbyte *) env->GetByteArrayElements(out, 0);
    jbyte *pTmpOut = h264Buf;
    int nPicSize = en->param->i_width * en->param->i_height;
    /*
    Y数据全部从在一块，UV数据使用interleave方式存储
    YYYY
    YYYY
    UVUV
     */
    uint8_t *y = en->picture->img.plane[0];
    uint8_t *v = en->picture->img.plane[1];
    uint8_t *u = en->picture->img.plane[2];
    memcpy(en->picture->img.plane[0], Buf, nPicSize);
    for (i = 0; i < nPicSize / 4; i++) {
        *(u + i) = *(Buf + nPicSize + i * 2);
        *(v + i) = *(Buf + nPicSize + i * 2 + 1);
    }
    switch (type) {
        case 0:
            en->picture->i_type = X264_TYPE_P;
            break;
        case 1:
            en->picture->i_type = X264_TYPE_IDR;
            break;
        case 2:
            en->picture->i_type = X264_TYPE_I;
            break;
        default:
            en->picture->i_type = X264_TYPE_AUTO;
            break;
    }
    if (x264_encoder_encode(en->handle, &(en->nal), &nNal, en->picture, &pic_out) < 0) {
        return -1;
    }
    for (i = 0; i < nNal; i++) {
        memcpy(pTmpOut, en->nal[i].p_payload, en->nal[i].i_payload);
        pTmpOut += en->nal[i].i_payload;
        result += en->nal[i].i_payload;
    }
    return result;
}


extern "C" JNIEXPORT void JNICALL
Java_com_yocn_libnative_X264Translater_initX264Encoder(JNIEnv *env, jobject thiz,
                                                       int width, int height, int fps, int bite) {

}



