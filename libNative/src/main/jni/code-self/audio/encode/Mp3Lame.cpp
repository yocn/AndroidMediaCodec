//
// Created by 赵英坤 on 8/23/21.
//

#include "Mp3Lame.h"
#include "code-self/common/GlobalMacro.h"

Mp3Lame::Mp3Lame(const char *mp3_path) {
    init(mp3_path);
}

Mp3Lame::~Mp3Lame() {
    release();
}

void Mp3Lame::init(const char *mp3_path) {
    gfp = lame_init();
    mp3_file = fopen(mp3_path, "wb");
    // 设置被输入编码器的原始数据的声道数。
    lame_set_num_channels(gfp, 2);
    lame_set_in_samplerate(gfp, 44100);
    lame_set_brate(gfp, 128);
    // 设置最终mp3编码输出的声道模式，如果不设置则和输入声道数一样。参数是枚举，STEREO代表双声道，MONO代表单声道。
    lame_set_mode(gfp, STEREO);
    lame_set_quality(gfp, 7);   /* 2=high  5 = medium  7=low */
    int ret_code = lame_init_params(gfp);
    if (ret_code < 0) {
        LOGE("lame_init error:%d", ret_code);
    }
}

void Mp3Lame::release() {
    lame_close(gfp);
    fclose(mp3_file);
    mp3_file = nullptr;
}

/**
 * encode
 * @param input_buffer 应该是short int的，如果外面读取是uint_8或者char的，注意size应该除以二
 * @param input_size size应该是单个channel的size，并且应该是short int数组的size
 * @param is_last 是否是最后一帧。
 */
void Mp3Lame::encode(short int *input_buffer, const int input_size, bool is_last) {
    int mp3buf_size = get_mp3_size(input_size);
    int out_size = 0;
    auto *mp3_buffer = new unsigned char[mp3buf_size];
    if (is_last) {
        out_size = lame_encode_flush(gfp, mp3_buffer, mp3buf_size);
    } else {
        out_size = lame_encode_buffer_interleaved(gfp, input_buffer, input_size, mp3_buffer,
                                                  mp3buf_size);
    }
    LOGE("input_size:%d   in_size::%d  out_size::%d  is_last:%d", input_size, mp3buf_size,
         out_size, is_last);
    fwrite(mp3_buffer, 1, out_size, mp3_file);
    if (is_last) {
        lame_mp3_tags_fid(gfp, mp3_file);
    }
}