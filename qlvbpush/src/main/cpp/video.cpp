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

    x264_nal_t *nal = nullptr;//结构体，存储压缩编码后的码流数据；
    int pi_nal;// nal单元数量
    x264_picture_t pic_out;//编码后图片
    //1.视频编码器， 2.nal，  3.pi_nal是nal中输出的NAL单元的数量， 4.输入原始的图片，  5.输出编码后图片
    //返回值：x264_encoder_encode函数 返回返回的 NAL 中的字节数。如果没有返回 NAL 单元，则在错误时返回负数和零。
    //一帧YUV为H.264码流
    //x264_nal_t中的数据在下一次调用x264_encoder_encode之后就无效了，因此必须在调用
    //x264_encoder_encode 或 x264_encoder_headers 之前使用或拷贝其中的数据。
    int result = x264_encoder_encode(videoEncoder, &nal, &pi_nal, picture, &pic_out);
    if (result < 0) {
        LOGE("x264编码失败")
        pthread_mutex_unlock(&mutex); // 同学们注意：一旦编码失败了，一定要解锁，否则有概率性造成死锁了
        return;
    }
    int sps_length, pps_length;
    uint8_t sps[100]; // 用于接收 sps 的数组定义
    uint8_t pps[100];
    //记录的是当前帧的pts 显示时间 每次都累加下去
    picture->i_pts += 1;

    for (int i = 0; i < pi_nal; ++i) {
        //是sps
        x264_nal_t &x264Nal = nal[i];
        if (x264Nal.i_type == NAL_SPS) {
            //payload  的字节大小
            sps_length = x264Nal.i_payload - 4;//启始码
            // p_payload 该NAL单元存储数据的开始地 
            memcpy(sps, x264Nal.p_payload + 4, sps_length);
        } else if (x264Nal.i_type == NAL_PPS) {
            pps_length = x264Nal.i_payload - 4; // 去掉起始码 之前我们学过的内容：00 00 00 01）
            memcpy(pps, x264Nal.p_payload + 4, pps_length); // 由于上面减了4，所以+4挪动这里的位置开始
            sendSpsPps(sps, pps, sps_length, pps_length); // pps是跟在sps后面的，这里拿到的pps表示前面的sps肯定拿到了
        } else {
            //发送I帧 P帧
            sendFrame(x264Nal.i_type, x264Nal.i_payload, x264Nal.p_payload);
        }

    }

    pthread_mutex_unlock(&mutex);

}

void Video::init(int width, int height, int fps, int rate) {
    LOGE("VIDEO init")
    pthread_mutex_lock(&mutex);
    this->mWidth = width;
    this->mHeight = height;
    this->mFps = fps;
    this->mRate = rate;
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
    //码率 i_bitrate 表示平均码率，参数的量纲是 kilobits/sec，编码器最终输出文件或者视频流的大小将尽量与这个值相符
    param.rc.i_bitrate = rate / 1000;
    //https://blog.csdn.net/CrystalShaw/article/details/89394113
    //https://blog.csdn.net/zhuiyuanqingya/article/details/103105156
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
    x264_picture_alloc(picture, param.i_csp, param.i_width, param.i_height);

    //编码器打开
    videoEncoder = x264_encoder_open(&param);
    if (videoEncoder) {
        LOGE("初始化成功")
    }
    pthread_mutex_unlock(&mutex);

}

/**
 *
 * @param type  帧类型
 * @param payload  帧长度
 * @param frame  帧数据
 */
void Video::sendFrame(int type, int payload, uint8_t *frame) {
    // 去掉起始码 00 00 00 01 或者 00 00 01
    if (frame[2] == 0x00) { // 00 00 00 01
        frame += 4; // 例如：共10个，挪动4个后，还剩6个
        // 保证 我们的长度是和上的数据对应，也要是6个，所以-= 4
        payload -= 4;
    } else if (frame[2] == 0x01) { // 00 00 01
        frame += 3; // 例如：共10个，挪动3个后，还剩7个
        // 保证 我们的长度是和上的数据对应，也要是7个，所以-= 3
        payload -= 3;
    }
    int body_size = 5 + 4 + payload;
    auto *packet = new RTMPPacket; // 开始封包RTMPPacket

    RTMPPacket_Alloc(packet, body_size); // 堆区实例化 RTMPPacket
    // 区分关键帧 和 非关键帧
    packet->m_body[0] = 0x27; // 普通帧 非关键帧
    if (type == NAL_SLICE_IDR) {
        packet->m_body[0] = 0x17; // 关键帧
    }
    packet->m_body[1] = 0x01; // 重点是此字节 如果是1 帧类型（关键帧 非关键帧），    如果是0一定是 sps pps
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;

    packet->m_body[5] = (payload >> 24) & 0xFF;
    packet->m_body[6] = (payload >> 16) & 0xFF;
    packet->m_body[7] = (payload >> 8) & 0xFF;
    packet->m_body[8] = payload & 0xFF;

    memcpy(&packet->m_body[9], frame, payload); // 拷贝H264的裸数据

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO; // 包类型 视频包
    packet->m_nBodySize = body_size; // 设置好 关键帧 或 普通帧 的总大小
    packet->m_nChannel = 10; // 通道ID，随便写一个，注意：不要写的和rtmp.c(里面的m_nChannel有冲突 4301行)
    packet->m_nTimeStamp = -1; // sps pps 包 没有时间戳
    packet->m_hasAbsTimestamp = 0; // 时间戳绝对或相对 也没有时间搓
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE; // 包的类型：若是关键帧的话，数据量比较大，所以设置大包
    videoCallBack(packet);
}

void Video::setVideoCallBack(VideoCallBack callBack) {

    this->videoCallBack = callBack;
}

void Video::sendSpsPps(uint8_t *sps, uint8_t *pps, int sps_length, int pps_length) {


    int body_size = 5 + 8 + sps_length + 3 + pps_length;
    auto *pPacket = new RTMPPacket();
    RTMPPacket_Alloc(pPacket, body_size);
    int i = 0;
    pPacket->m_body[i++] = 0x17; // 十六进制

    pPacket->m_body[i++] = 0x00; // 十六进制   如果全部都是0，就能够证明 sps+pps
    pPacket->m_body[i++] = 0x00; // 十六进制
    pPacket->m_body[i++] = 0x00; // 十六进制
    pPacket->m_body[i++] = 0x00; // 十六进制

    pPacket->m_body[i++] = 0x01; // 十六进制 版本
    pPacket->m_body[i++] = sps[1]; // 十六进制 版本
    pPacket->m_body[i++] = sps[2]; // 十六进制 版本
    pPacket->m_body[i++] = sps[3]; // 十六进制 版本

    pPacket->m_body[i++] = 0xFF; // 十六进制 版本
    pPacket->m_body[i++] = 0xE1; // 十六进制 版本


    pPacket->m_body[i++] = (sps_length >> 8) & 0xFF; // 取高8位
    pPacket->m_body[i++] = sps_length & 0xFF; // 取出低8位
    memcpy(&pPacket->m_body[i], sps, sps_length); // sps拷贝进去了

    i += sps_length; // 拷贝完sps数据 ，i移位，（下面才能准确移位）
    pPacket->m_body[i++] = 0x01; // 十六进制 版本 pps个数，用0x01来代表
    pPacket->m_body[i++] = (pps_length >> 8) & 0xFF; // 取高8位
    pPacket->m_body[i++] = pps_length & 0xFF; // 去低8位

    memcpy(&pPacket->m_body[i], pps, pps_length); // pps拷贝进去了

    i += pps_length; // 拷贝完pps数据 ，i移位，（下面才能准确移位）
    pPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;

    pPacket->m_nBodySize = body_size; // 设置好 sps+pps的总大小
    pPacket->m_nChannel = 10;//通道id
    pPacket->m_nTimeStamp = 0;// sps pps 包 没有时间戳
    pPacket->m_headerType = RTMP_PACKET_SIZE_MEDIUM;//中包
    videoCallBack(pPacket);

}