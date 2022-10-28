//
// Created by leo on 2022/10/26.
//

#include "video.h"
#include "log4c.h"

Video::Video() {

    pthread_mutex_init(&mutex, nullptr);
}

Video::~Video() {

    pthread_mutex_destroy(&mutex);

}

void Video::encode(signed char *data) {
    pthread_mutex_lock(&mutex);

    if (!picture) { return; }

    //挪动Y分量
    memcpy(picture->img.plane[0], data, y_len);

    for (int i = 0; i < uv_len; ++i) {
        //挪动U分量
        *(picture->img.plane[1] + i) = *(data + y_len + i * 2 + 1);

        //挪动V分量
        *(picture->img.plane[2] + i) = *(data + y_len + i * 2);

    }
    //todo


    pthread_mutex_unlock(&mutex);

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
    param.i_level_idc = 32;
    //编码比特流的CSP，仅支持i420，色彩空间设置
    param.i_csp = X264_CSP_I420;
    param.i_width = width;
    param.i_height = height;
    //b帧数为0 因为需要向前向后参考会卡
    param.i_bframe = 0;
    // 码率控制方式。CQP(恒定质量)，CRF(恒定码率)，ABR(平均码率)
    param.rc.i_rc_method = X264_RC_CRF;
    //码率
    param.rc.i_bitrate = rate / 1000;
    //https://blog.csdn.net/CrystalShaw/article/details/89394113
    //i_vbv_max_bitrate 最大码率 平均码率模式下，最大瞬时码率，默认0(与-B设置相同)
    //i_vbv_buffer_size 码率控制缓冲区的大小，单位kbit，默认0 */
    param.rc.i_vbv_buffer_size = rate / 1000;
    //https://www.suninf.net/2019/02/x264-zero-latency.html
    // 与force-cfr选项相对应：vfr_input=1时，为可变帧率，使用timebase和timestamps做码率控制；vfr_input=0时，为固定帧率，使用fps做码率控制。
    param.b_vfr_input = 0;
    //帧率设置 和ffmpeg差不多 i_fps_num  频率分子/时间基分子 i_fps_den 频率分母/时间基分母
    param.i_fps_num = fps;
    param.i_fps_den = 1;
    param.i_timebase_den = param.i_fps_num;
    param.i_timebase_num = param.i_fps_den;
    //计算i帧时间间隔
    param.i_keyint_max = fps * 2;
    //是否写带 sps  pps head里面的数据
    param.b_repeat_headers = 1;
    //线程数1
    param.i_threads = 1;
    //x264_param_apply_profile()：指定编码使用的profile。不保证使用指定的profile，有可能会降低profile，设置成功时返回0。支持的profile有"baseline", "main", "high", "high10", "high422", "high444"。
    //如果你的播放器仅能支持特定等级的话，就需要指定等级选项。大多数播放器支持高等级，就不需要指定等级选项了。
    x264_param_apply_profile(&param, "baseline");

    //初始化
    picture = new x264_picture_t;
    x264_picture_alloc(picture, param.i_csp, param.i_height, param.i_height);

    //编码器打开
    videoEncoder = x264_encoder_open(&param);
    if (videoEncoder) {
        LOGE("初始化成功")
    }
    pthread_mutex_unlock(&mutex);

}

void Video::sendFrame(int type, int payload, uint8_t *payload1) {

}

void Video::setVideoCallBack(void (*param)(RTMPPacket *)) {


}

void Video::sendSpsPps(uint8_t *sps, uint8_t *pps, int len, int len1) {


}