//
// Created by 赵英坤 on 8/23/21.
//

#ifndef ANDROIDMEDIACODEC_MP3LAME_H
#define ANDROIDMEDIACODEC_MP3LAME_H

#include "../../../lame-3.100/include/lame.h"

inline int get_mp3_size(int pcm_size) {
    return (int) (1.25 * pcm_size) + 7200;
}

class Mp3Lame {
private:
    lame_global_flags *gfp{};
    FILE *mp3_file;

    void init(const char *mp3_path);

    void release();

public:
    Mp3Lame(const char *mp3_path);

    ~Mp3Lame();

    void encode(
            short int *input_buffer,
            const int input_size,
            bool is_last);

};


#endif //ANDROIDMEDIACODEC_MP3LAME_H
