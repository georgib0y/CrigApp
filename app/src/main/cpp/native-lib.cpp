#include <jni.h>
#include <string>


extern "C"
JNIEXPORT void JNICALL
Java_com_github_georgib0y_crigapp_UCI_uciNewGame(JNIEnv *env, jobject thiz, jlong ptr) {}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_georgib0y_crigapp_UCI_sendPosition(JNIEnv *env, jobject thiz, jlong ptr,
                                                   jstring pos_str) {
    return nullptr;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_github_georgib0y_crigapp_UCI_initUci(JNIEnv *env, jobject thiz) {
    return 0;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_github_georgib0y_crigapp_UCI_logUciPosition(JNIEnv *env, jobject thiz, jlong ptr,
                                                     jstring pos_str) {}
extern "C"
JNIEXPORT void JNICALL
Java_com_github_georgib0y_crigapp_UCI_sendGo(JNIEnv *env, jobject thiz, jlong ptr) {
    // TODO: implement sendGo()
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_georgib0y_crigapp_UCI_validatePosition(JNIEnv *env, jobject thiz, jstring pos_str) {
    return 0;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_georgib0y_crigapp_UCI_searchPosition(JNIEnv *env, jobject thiz, jlong ptr,
                                                     jstring pos_str) {
    return nullptr;
}