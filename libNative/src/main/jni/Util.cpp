extern "C" {
#include <GlobalMacro.h>

#define JNI_METHOD_NAME(name) Java_com_yocn_media_util_LogUtil_##name

void LOG(JNIEnv *env, jobject jobj) {
    jboolean copy;
//    jclass jclazz = env->GetObjectClass(jobj);
    jclass jclazz = env->FindClass("com/yocn/meida/util/LogUtil");
    jfieldID jfieldId = env->GetStaticFieldID(jclazz, "TAG", "Ljava/lang/String;");
    auto tagString = (jstring) env->GetStaticObjectField(jclazz, jfieldId);
    const char *tag = env->GetStringUTFChars(tagString, &copy);
    LOGE("tag::%s", tag);
    env->ReleaseStringUTFChars(tagString, tag);
}
}