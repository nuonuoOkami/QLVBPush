#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_nuonuo_qlvbpush_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_VideoHelper_pushData(JNIEnv *env, jobject thiz, jbyteArray data) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_nuonuo_qlvbpush_VideoHelper_videoInit(JNIEnv *env, jobject thiz, jint fps, jint bitrate,
                                               jint width, jint height) {
    // TODO: implement videoInit()
}