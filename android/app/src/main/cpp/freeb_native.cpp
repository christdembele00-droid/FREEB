#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_freeb_app_MainActivity_nativeName(JNIEnv* env, jobject) {
    return env->NewStringUTF("FREEB Native");
}
