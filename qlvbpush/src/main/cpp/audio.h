//
// Created by leo on 2022/10/26.
//

#ifndef QLVBPUSH_AUDIO_H
#define QLVBPUSH_AUDIO_H

#include <rtmp.h>
#include <jni.h>
#include <faac.h>
#include <cstring>
#include <sys/types.h>
#include "log4c.h"


class Audio {
public:
    typedef void (*AudioCallback)(RTMPPacket *packet);
    Audio();

    ~Audio();
    //初始化音频编码器
    void initAudioEncoder(int sample_rate, int num_channels);

    jint getInputSamples();

    void encodeData(int8_t *data);

    void setAudioCallback(AudioCallback audioCallback);

    RTMPPacket * getAudioSeqHeader();

private:
    u_long inputSamples; // faac输出的样本数
    u_long maxOutputBytes; // faac 编码器 最大能输出的字节数
    int mChannels; // 通道数
    faacEncHandle  audioEncoder=0;// 音频编码器
    u_char *buffer = 0; // 后面要用到的 缓冲区
    AudioCallback audioCallback;
};


#endif //QLVBPUSH_AUDIO_H
