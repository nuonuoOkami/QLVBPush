#include <jni.h>
#include <string>


#include "video.h"

#include "audio.h"
#include <rtmp.h>

Video *video = nullptr;
Audio *audio = nullptr;


extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_VideoHelper_pushData(JNIEnv *env, jobject thiz, jbyteArray data) {

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