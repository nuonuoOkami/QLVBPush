//
// Created by leo on 2022/10/26.
//

#include "audio.h"

Audio::Audio() {
}

Audio::~Audio() {
    delete buffer;
    buffer = nullptr;
    if (audioEncoder) {
        faacEncClose(audioEncoder);
        audioEncoder = nullptr;
    }
}

void Audio::initAudioEncoder(int sample_rate, int num_channels) {

    this->mChannels = num_channels;
    //faacEncHandle FAACAPI faacEncOpen(
    //        unsigned long sampleRate,      // pcm音频采样率，8k,16k,44100等
    //        unsigned int numChannels,      // pcm音频通道，mono = 1 / stereo = 2
    //        unsigned long *inputSamples,   // 一次输入的样本数
    //        unsigned long *maxOutputBytes);// 输出aac buffer的最大size
    //        函数调用成功会return一个编码器faacEncHandle，同时确定输入样本数和输出aac buffer最大size;
    //原文链接：https://blog.csdn.net/qinglongzhan/article/details/81315532
    //https://blog.csdn.net/qinglongzhan/article/details/81315532?utm_source=blogxgwz0
    audioEncoder = faacEncOpen(sample_rate, mChannels, &inputSamples, &maxOutputBytes);
    if (!audioEncoder) {
        LOGE("audioEncoder 初始化失败")
        return;
    }

    // 获取编码配置的结构体指针
    // * 在调用设置编码参数之前，需要先调用该函数获取结构体指针，再进行参数填充
    faacEncConfigurationPtr config = faacEncGetCurrentConfiguration(audioEncoder);

    config->mpegVersion = MPEG4;//mpeg4标准 acc音频标准
    // // 对象类型只有为 LOW, iOS 的 AVAudioPlayer 才能播放
    //LC标准： https://zhidao.baidu.com/question/1948794313899470708.html
    config->aacObjectType = LOW;
    config->inputFormat = FAAC_INPUT_16BIT;
    //: 这里设置输出格式 0, 就是 FAAC 将 PCM 采样进行编码,
    // 编码出的格式是 AAC 原始数据 , 即没有解码信息的 ADIF 和 ADTS 的 AAC 纯样本裸数据 ;
    config->outputFormat = 0;
    // （开启降噪, 噪声控制）
    config->useTns = 1;

    //使用其中一个通道作为LFE通道
    config->useLfe = 0;

    int result = faacEncSetConfiguration(audioEncoder, config);
    if (!result) {
        LOGE("audioEncoder 设置参数失败")
        return;
    }

    LOGE("audioEncoder 设置参数成功")
    buffer = new u_char(maxOutputBytes);

}

RTMPPacket *Audio::getAudioSeqHeader() {

    u_char *ppBuffer;
    u_long len;
    /*
 int FAACAPI faacEncGetDecoderSpecificInfo( // 获取aac头信息
     faacEncHandle hEncoder, // faac 编码器
     unsigned char **ppBuffer, // 获取到的aac的头信息
     unsigned long *pSizeOfDecoderSpecificInfo // 获取到的aac的头信息的长度
 );
 */
    faacEncGetDecoderSpecificInfo(audioEncoder, &ppBuffer, &len);
    auto *pPacket = new RTMPPacket();
    //后面的2：有16bit描述 头数据的长度，就是16bit
    int bodySize = 2 + len;
    RTMPPacket_Alloc(pPacket, bodySize);
    // AF == AAC编码器，44100采样率，位深16bit，双声道
    // AE == AAC编码器，44100采样率，位深16bit，单声道
    pPacket->m_body[0] = 0xAF;
    if (mChannels == 1) {
        pPacket->m_body[0] = 0xAE; // 单声道
    }
    pPacket->m_body[1] = 0X00;
    //序列头数据
    memcpy(&pPacket->m_body[2], ppBuffer, 2);
    //包类型 音频
    pPacket->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    //长度 bodySize
    pPacket->m_nBodySize = bodySize;
    pPacket->m_nChannel = 11;
    //无时间戳
    pPacket->m_nTimeStamp = 0;
    pPacket->m_hasAbsTimestamp = 0;// Timestamp 是绝对值还是相对值?
    pPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;//大包
    return pPacket;
}

jint Audio::getInputSamples() {

    return inputSamples;
}

void Audio::setAudioCallback(AudioCallback audioCallback) {
    this->audioCallback = audioCallback;
}

void Audio::encodeData(int8_t *data) {

    //  reinterpret_cast https://zhuanlan.zhihu.com/p/33040213
    //static_cast 运算符完成*相关类型*之间的转换. 而 reinterpret_cast 处理*互不相关的类型*之间的转换.
    /**
    * 1，上面的初始化好的faac编码器
    * 2，数据（无符号的事情）
    * 3，上面的初始化好的样本数
    * 4，接收成果的 输出 缓冲区
    * 5，接收成果的 输出 缓冲区 大小
    * ret:返回编码后数据字节长度    byteLen 代表 音频编码器 OK SUccess，否则有问题
    */
    int byteLen = faacEncEncode(audioEncoder, reinterpret_cast<int32_t *>(data), inputSamples,
                                buffer,
                                maxOutputBytes);
    if (byteLen > 0) {
        auto *packet = new RTMPPacket;
        int body_size = 2 + byteLen; // 后面的byteLen：我们实际数据编码后的长度
        RTMPPacket_Alloc(packet, body_size); // 堆区实例化里面的成员 packet
        // 0xAF == AAC编码器，44100采样率，位深16bit，双声道
        // 0xAE == AAC编码器，44100采样率，位深16bit，单声道
        packet->m_body[0] = 0xAF; // 双声道
        if (mChannels == 1) {
            packet->m_body[0] = 0xAE; // 单声道
        }

        // 这里是编码出来的音频数据，所以都是 01，  非序列/非头参数
        packet->m_body[1] = 0x01;
        memcpy(&packet->m_body[2], buffer, byteLen);
        packet->m_packetType = RTMP_PACKET_TYPE_AUDIO; // 包类型，音频
        packet->m_nBodySize = body_size;
        packet->m_nChannel = 11; // 通道ID，随便写一个，注意：不要写的和rtmp.c(里面的m_nChannel有冲突 4301行)
        packet->m_nTimeStamp = -1; // 帧数据有时间戳
        packet->m_hasAbsTimestamp = 0; // 一般都不用
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE; // 大包的类型，如果是头信息，可以给一个小包
        // 把数据包放入队列
        audioCallback(packet);
    }
}

