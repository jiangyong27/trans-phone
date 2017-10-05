#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_didi_transsms_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_didi_transsms_MainActivity_Test(
        JNIEnv* env,
        jobject ){
    std::string h = "ok";
    return env->NewStringUTF(h.c_str());
}
