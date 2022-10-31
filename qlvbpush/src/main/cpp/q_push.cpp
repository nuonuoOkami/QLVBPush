#include <jni.h>
#include <string>


#include "video.h"

#include "audio.h"
#include <rtmp.h>
#include "safe_queue.h"
#include "log4c.h"

Video *video = nullptr;
Audio *audio = nullptr;
//是否已经准备好
bool is_ready = false;
SafeQueue<RTMPPacket *> packets;
uint32_t start_time;
//是否正在推流
bool is_live;

pthread_t pid_start;

extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_VideoHelper_native_1Video_1Init(JNIEnv *env, jobject thiz, jint fps,
                                                         jint bitrate, jint width, jint height) {

    if (video) {
        video->init(width, height, fps, bitrate);
    }

}

void callBack(RTMPPacket *rtmpPacket) {
    if (rtmpPacket) {

        //帧加上时间搓 sps pps不需要
        if (rtmpPacket->m_nTimeStamp == -1) {
            rtmpPacket->m_nTimeStamp = RTMP_GetTime() - start_time;
        }
        packets.insert(rtmpPacket);
    }
}
/**
 * 初始化push
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_QLVBPushHelper_pushInit(JNIEnv *env, jobject thiz) {
    video = new Video();
    audio = new Audio();
    video->setVideoCallBack(callBack);
}


/**
 * 推送数据
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_VideoHelper_native_1Video_1Push(JNIEnv *env, jobject thiz,
                                                         jbyteArray data) {

    //如果video是null或者没准备好 就返回
    if (!video || !is_ready) { return; }
    jbyte *j_data = env->GetByteArrayElements(data, nullptr);
    if (video) {
        video->encode(j_data);
    }
    env->ReleaseByteArrayElements(data, j_data, 0);
}

/**
 * 开始rtmp推流
 * @param args
 * @return
 */
void *rtmp_push(void *args) {
    char *url = static_cast<char *>(args);
    RTMP *rtmp ;
    int result;
    do {
        rtmp = RTMP_Alloc();
        if (!rtmp) {
            LOGE("rtmp 初始化失败");
            break;
        }
        RTMP_Init(rtmp);
        rtmp->Link.timeout = 5;
        result = RTMP_SetupURL(rtmp, url);
        if (!result) {
            LOGE("rtmp 初始化失败");
            break;
        }

        //开始输出
        RTMP_EnableWrite(rtmp);
        //链接
        result = RTMP_Connect(rtmp, nullptr);
        if (!result) { // FFmpeg 0 就是成功      RTMP 0 就是失败
            LOGE("rtmp 连接建立失败:%d, url:%s", result, url);
            break;
        }

        result = RTMP_ConnectStream(rtmp, 0);
        if (!result) { // FFmpeg 0 就是成功      RTMP 0 就是失败
            LOGE("rtmp 连接流失败 %d", result);
            break;
        }
        start_time = RTMP_GetTime(); //开始时间
        //准备好推流
        is_ready = true;
        packets.setPlayState(true);
        //开始搞包
        RTMPPacket *rtmpPacket = nullptr;
        while (is_ready) {
            packets.take(rtmpPacket);
            if (!is_ready) { break; }
            if (!rtmpPacket) {
                LOGE("is_ready rtmp !rtmpPacket break ");
                continue;
            }
            //给rtmp的流 ID
            rtmpPacket->m_nInfoField2 = rtmp->m_stream_id;
            LOGE("m_stream_id")
            //发送数据
            result = RTMP_SendPacket(rtmp, rtmpPacket, 1);
            LOGE("rtmp RTMP_SendPacket ");
            //释放
            RTMPPacket_Free(rtmpPacket);
            delete rtmpPacket;
            rtmpPacket = nullptr;
            if (!result) { //      RTMP 0 就是失败
                LOGE("rtmp 发送包 失败 自动断开服务器");
                break;
            }

        }

        //循环之外要释放
        RTMPPacket_Free(rtmpPacket);
        delete rtmpPacket;
        rtmpPacket = nullptr;
    } while (false);
    is_live = false;
    is_ready = false;
    packets.setPlayState(false);
    packets.clear();

    //释放rtmp
    if (rtmp) {
        RTMP_Close(rtmp); // 先关闭
        RTMP_Free(rtmp); // 再释放，如果直接释放，可能是释放失败
    }
    delete url;

    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_QLVBPushHelper_native_1start_1live(JNIEnv *env, jobject thiz,
                                                            jstring path) {
    if (is_live) {
        return;
    }
    is_live = true;
    LOGE("start_1live")
    const char *live_path = env->GetStringUTFChars(path, nullptr);
    char *rtmpUrl = new char(strlen(live_path) + 1);
    stpcpy(rtmpUrl, live_path);//深拷贝
    LOGE("rtmpUrl%s", rtmpUrl)
    pthread_create(&pid_start, nullptr, rtmp_push, rtmpUrl);
    env->ReleaseStringUTFChars(path, live_path);

}

