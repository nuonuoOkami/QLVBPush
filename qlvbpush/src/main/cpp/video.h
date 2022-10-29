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
    typedef void (*VideoCallBack)(RTMPPacket *packet);

private:
    pthread_mutex_t mutex;
    int mWidth;
    int mHeight;
    //帧率
    int mFps;
    //码率
    int mRate;
    // Y分量的长度
    int y_len;
    // uv分量的长度
    int uv_len;
    //264编码器
    x264_t *videoEncoder = 0;
    //x264_picture_t 结构体描述一视频帧的特征，该结构体定义在x264.h中。
    //x264_image_t 结构用于存放一帧图像实际像素数据 glide怎么实现的
    //https://blog.nowcoder.net/n/e25bc9720f6b41ba959045b9882163f1?from=nowcoder_improve
    x264_picture_t *picture = 0;
    VideoCallBack videoCallBack;

public:
    Video();

    ~Video();

    void init(int width, int height, int fps, int rate);

    void encode(signed char *data);

    void sendSpsPps(uint8_t sps[100], uint8_t pps[100], int len, int len1);

    void setVideoCallBack(VideoCallBack);

    void sendFrame(int type, int payload, uint8_t *frame);


};


#endif //QLVBPUSH_VIDEO_H
