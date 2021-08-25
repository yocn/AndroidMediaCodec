//
// Created by 赵英坤 on 8/18/21.
//

#ifndef ANDROIDMEDIACODEC_AUDIO_RENDER_H
#define ANDROIDMEDIACODEC_AUDIO_RENDER_H

#include "x264/extras/stdint.h"

class AudioRender {
public:
    virtual void InitRender() = 0;
    virtual void Render(uint8_t *pcm, int size) = 0;
    virtual void ReleaseRender() = 0;
    virtual ~AudioRender() {}
};

#endif //ANDROIDMEDIACODEC_AUDIO_RENDER_H
