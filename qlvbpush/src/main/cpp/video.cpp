//
// Created by leo on 2022/10/26.
//

#include "video.h"

Video::Video() {

    pthread_mutex_init(&mutex, nullptr);
}

Video::~Video() {

    pthread_mutex_destroy(&mutex);

}

void Video::encode(signed char *data) {

}

void Video::init(int width, int height, int fps, int rate) {
    pthread_mutex_lock(&mutex);
    this->width = width;
    this->height = height;
    this->fps = fps;
    this->rate = rate;
    //4个y用一个uv
    y_len = width * height;
    uv_len = y_len / 4;
    if (videoEncoder) {
        //https://blog.csdn.net/FRD2009041510/article/details/50847271
        //释放x264编码器
        x264_encoder_close(videoEncoder);
        videoEncoder = nullptr;
    }
    if (picture) {
        //释放相应的图像结构
        x264_picture_clean(picture);
        delete picture;
        picture = nullptr;
    }
    // 结构体x264_param_t是x264中最重要的结构体之一，主要用于初始化编码器
    x264_param_t param;
    //https://blog.csdn.net/dangxw_/article/details/50974880
    //ultrafast cpu占用最小，zerolatency 不缓存帧
    x264_param_default_preset(&param, "ultrafast", "zerolatency");
    //https://www.cnblogs.com/lihaiping/p/4193912.html
    //编码规格：https://wikipedia.tw.wjbk.site/wiki/H.264
    param.i_level_idc=32;
    //编码比特流的CSP，仅支持i420，色彩空间设置
    param.i_csp=X264_CSP_I420;
    param.i_width=width;
    param.i_height=height;


}

void Video::sendFrame(int type, int payload, uint8_t *payload1) {

}

void Video::setVideoCallBack(void (*param)(RTMPPacket *)) {


}

void Video::sendSpsPps(uint8_t *sps, uint8_t *pps, int len, int len1) {


}