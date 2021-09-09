//
// Created by 赵英坤 on 9/9/21.
//

#include <cstdio>
#include <x264/x264.h>
#include <cstdlib>
#include <code-self/common/Util.h>
#include "GlobalMacro.h"
#include "JniProgress.h"

#include <sys/stat.h>//包含头文件。

//获取文件名为filename的文件大小。
extern int get_file_size(const char *filename) {
    struct stat statbuf;
    int ret;
    ret = stat(filename, &statbuf);//调用stat函数
    if (ret != 0) return -1;//获取失败。
    return statbuf.st_size;//返回文件大小。
}

#define JNI_METHOD_NAME(name) Java_com_yocn_libnative_X264Encoder_##name

extern "C" {

JNIEXPORT void JNICALL
JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobj, jstring yuv_path, jstring x264Path, jint width,
                      jint height, jint fps);

}

JNIEXPORT void JNICALL
JNI_METHOD_NAME(init)(JNIEnv *env, jobject jobj, jstring yuvPath, jstring x264Path, jint width,
                      jint height, jint fps) {
    const char *yuv_path = env->GetStringUTFChars(yuvPath, nullptr);
    const char *x264_path = env->GetStringUTFChars(x264Path, nullptr);

    LOGE("yuv_path::%s", yuv_path);
    LOGE("x264_path::%s", x264_path);
    int file_size = get_file_size(yuv_path);
    LOGE("yuv_ size::%d", file_size);

    FILE *infile = fopen(yuv_path, "rb");
    FILE *outfile = fopen(x264_path, "wb");
    if (!infile) {
        LOGE("yuv输入文件不存在~");
        return;
    }
    if (!outfile) {
        LOGE("open file error");
        return;
    }

    size_t yuv_size = width * height * 3 / 2;
    x264_t *encoder;
    x264_picture_t pic_in, pic_out;
    uint8_t *yuv_buffer;

    x264_param_t m_param;
    x264_param_default_preset(&m_param, "veryfast", "zerolatency");
    m_param.i_threads = 1;
    m_param.i_width = width;
    m_param.i_height = height;
    m_param.i_fps_num = fps;
    m_param.i_bframe = 10;
    m_param.i_fps_den = 1;
    m_param.i_keyint_max = 25;
    m_param.b_intra_refresh = 1;
    m_param.b_annexb = 1;
    x264_param_apply_profile(&m_param, "high");
    encoder = x264_encoder_open(&m_param);

    x264_encoder_parameters(encoder, &m_param);

    x264_picture_alloc(&pic_in, X264_CSP_I420, width, height);

    yuv_buffer = static_cast<uint8_t *>(malloc(yuv_size));

    pic_in.img.plane[0] = yuv_buffer;
    pic_in.img.plane[1] = pic_in.img.plane[0] + width * height;
    pic_in.img.plane[2] = pic_in.img.plane[1] + width * height / 4;

    int64_t i_pts = 0;

    x264_nal_t *nals;
    int nnal;
    while (fread(yuv_buffer, 1, yuv_size, infile) > 0) {
        pic_in.i_pts = i_pts++;
        long curr_size = i_pts * yuv_size;
        int percent = curr_size * 100 / file_size;
        LOGE("pts:%ld  yuv_size:%ld  total:%ld  file_size:%d percent:%d",
             i_pts, yuv_size, (i_pts * yuv_size), file_size, percent);
        progress(env, jobj, curr_size, file_size, percent);
        x264_encoder_encode(encoder, &nals, &nnal, &pic_in, &pic_out);
        x264_nal_t *nal;
        for (nal = nals; nal < nals + nnal; nal++) {
            fwrite(nal->p_payload, 1, nal->i_payload, outfile);
        }
    }
    x264_encoder_close(encoder);
    fclose(infile);
    fclose(outfile);
    free(yuv_buffer);
}

