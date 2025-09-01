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