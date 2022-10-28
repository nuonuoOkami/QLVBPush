#include <jni.h>
#include <string>


#include "video.h"

#include "audio.h"
#include <rtmp.h>

Video *video = nullptr;
Audio *audio = nullptr;
//是否已经准备好
bool is_ready = false;

extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_VideoHelper_pushData(JNIEnv *env, jobject thiz, jbyteArray data) {

    //如果video是null或者没准备好 就返回
    if (!video || !is_ready) { return; }
    jbyte *j_data = env->GetByteArrayElements(data, nullptr);
    if (video) {
        video->encode(j_data);
    }
    env->ReleaseByteArrayElements(data, j_data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_VideoHelper_native_1Video_1Init(JNIEnv *env, jobject thiz, jint fps,
                                                         jint bitrate, jint width, jint height) {

    if (video) {
        video->init(width, height, fps, bitrate);
    }

}

void callBack(RTMPPacket *rtmpPacket) {

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
                                                         jbyteArray byte) {
}