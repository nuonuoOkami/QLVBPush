//
// Created by leo on 2022/10/26.
//

#ifndef QLVBPUSH_VIDEO_H
#define QLVBPUSH_VIDEO_H

#include <rtmp.h>
#include <thread>
#include <x264.h>


class Video {
public:
    typedef void (*VideoCallBack(RTMPPacket *packet));

private:
    pthread_mutex_t mutex;
    int width;
    int height;
    //帧率
    int fps;
    //码率
    int rate;
    // Y分量的长度
    int y_len;
    // uv分量的长度
    int uv_len;
    //264编码器
    x264_t *videoEncoder = 0;
    x264_picture_t *picture = 0;
    VideoCallBack videoCallBack;

public:
    Video();

    ~Video();

    void init(int width, int height, int fps, int rate);

    void encode(signed char *data);

    void sendSpsPps(uint8_t sps[100], uint8_t pps[100], int len, int len1);

    void setVideoCallBack(void (*param)(RTMPPacket *));

    void  sendFrame(int type, int payload, uint8_t *payload1);


};


#endif //QLVBPUSH_VIDEO_H
