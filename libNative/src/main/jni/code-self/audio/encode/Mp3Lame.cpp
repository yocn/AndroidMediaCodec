//
// Created by 赵英坤 on 8/23/21.
//

#include "Mp3Lame.h"
#include "GlobalMacro.h"

Mp3Lame::Mp3Lame(const char *mp3_path) {
    init(mp3_path);
}

Mp3Lame::~Mp3Lame() {
    release();
}

void Mp3Lame::init(const char *mp3_path) {
    gfp = lame_init();
    mp3_file = fopen(mp3_path, "wb");
    lame_set_num_channels(gfp, 2);
    lame_set_in_samplerate(gfp, 44100);
    lame_set_brate(gfp, 128);
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

void Mp3Lame::encode(short int *input_buffer, const int input_size, bool is_last) {
    int mp3buf_size = get_mp3_size(input_size / 2 / 2);
    int out_size = 0;
    auto *mp3_buffer = new unsigned char[mp3buf_size];
    if (is_last) {
        out_size = lame_encode_flush(gfp, mp3_buffer, mp3buf_size);
        LOGE("input_size:%d   in_size::%d  out_size::%d  is_last:%d", input_size, mp3buf_size, out_size,
             is_last);
        fwrite(mp3_buffer, 1, out_size, mp3_file);
        lame_mp3_tags_fid(gfp, mp3_file);
    } else {
        out_size = lame_encode_buffer_interleaved(gfp, input_buffer, input_size, mp3_buffer,
                                                  mp3buf_size);
        LOGE("input_size:%d   in_size::%d  out_size::%d  is_last:%d", input_size, mp3buf_size, out_size,
             is_last);
        fwrite(mp3_buffer, 1, out_size, mp3_file);
    }
}