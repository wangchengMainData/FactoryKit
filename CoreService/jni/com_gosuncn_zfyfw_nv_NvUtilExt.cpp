#include <unistd.h>
#include <utils/Log.h>
#include <cutils/log.h>
#include <jni.h>
#include <nativehelper/JNIHelp.h>
#include <stdlib.h>
#include <string.h>
#include "android_runtime/AndroidRuntime.h"

#include <android/log.h>
#include "nv.h"
#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "NvUtilExt-JNI"
#define logdebug(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

static int SHOW_LOG = 1; // 1 for show log.
static int SHOW_NV_LOG = 1; // 1 for show log.
static const char* const className = "com/gosuncn/zfyfw/nv/NvUtilExt";

jint NativeInit() {
	/* Calling LSM init  */
	if(!Diag_LSM_Init(NULL)) { // 调用diag初始化
		logdebug("Diag_LSM_Init() failed.\n");
		return -1;
	}

    if(SHOW_LOG == 1){
	    logdebug("Diag_LSM_Init succeeded. \n");
	}
	/* Register the callback for the primary processor */
	register_callback();  // 注册回调方法
	return 1;
}

jstring CharTojstring(JNIEnv* env,const char* str) { // char转化为jstring
	jsize len = strlen(str);
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring,"<init>","([BLjava/lang/String;)V");
	jbyteArray barr = env->NewByteArray(len);
	env->SetByteArrayRegion(barr,0,len,(jbyte*)str);
	return (jstring)env->NewObject(clsstring,mid,barr,strencode);
}



JNIEXPORT void JNICALL Java_com_gosuncn_zfyfw_nv_NvUtilExt_nativeNvWrite
  (JNIEnv *env, jobject this_, jint index, jstring data){
//void android_nativeNvWrite(JNIEnv *env,jobject this_,jint index,jchar result) { //先读后存

	if(NativeInit() < 0){
	    logdebug("android_nativeNvWrite exit:init failed\n");
		return;
	}

    const char *nameStr = env->GetStringUTFChars(data, NULL);
    if(SHOW_LOG == 1 && nameStr != NULL){
        logdebug("android_nativeNvWrite data:%s len:%d\n", nameStr, (unsigned int)strlen(nameStr));
    }
    jobject jobj = this_;
    jobj = NULL;

	unsigned char tmp[23] = { 0 };
	unsigned char after[20] = { 0 };
	nv_items_enum_type nvId = (nv_items_enum_type)index; // 我们要操作的NVID
	memset(tmp, 0, sizeof(tmp));   // 申请mem
	memset(after, 0, sizeof(after));
	diag_nv_read(nvId,tmp, sizeof(tmp));  // 调用高通操作nv的api，读取NV给tmp
	for(unsigned int m=0;m < sizeof(tmp)-3;m++) {  // 去掉tmp的前三个字段，重新赋值给after
		//if (tmp[m+3] == NULL) {
		//	after[m] = ' ';
		//} else {
			after[m] = tmp[m+3];
		//}
	}
    if(SHOW_LOG == 1){
	    logdebug("android_nativeNvWrite index = %d\n",(int)index);
	}
	after[sizeof(tmp)-4] = '\0';

	if (SHOW_NV_LOG == 1) {
	    logdebug("android_nativeNvWrite 1 after strlen(after) = %d\n",(unsigned int)strlen((const char*)after));
		for (unsigned int n=0;n < sizeof(after);n++) {
			logdebug("android_nativeNvWrite,1 after[%d] = %02x \n",n,after[n]);
		}
	}

    unsigned int j=0;
    for (j=0;j < (unsigned int)strlen(nameStr);j++) {
	    after[j] = nameStr[j];
    }
    after[j] = '\0';

	if (SHOW_NV_LOG == 1) {
	    logdebug("android_nativeNvWrite 2 after strlen(after) = %d\n",(unsigned int)strlen((const char*)after));
		for (unsigned int n=0;n < sizeof(after);n++) {
			logdebug("android_nativeNvWrite,2 after[%d] = %02x \n",n,after[n]);
		}
	}
	diag_nv_write(nvId,after, sizeof(after));  // 调用高通写nv的api，重新写nv

    env->ReleaseStringUTFChars(data, nameStr);
}
JNIEXPORT jstring JNICALL Java_com_gosuncn_zfyfw_nv_NvUtilExt_nativeNvRead
  (JNIEnv *env, jobject this_, jint index){
//jstring android_nativeNvRead(JNIEnv *env) { // 读取nv

    JNIEnv *env1 = env;
    jobject jobj = this_;
    env1 = NULL;
    jobj = NULL;

    if(SHOW_LOG == 1){
	    logdebug("android_nativeNvRead index = %d\n",(int)index);
	}

	if (NativeInit() < 0) { // 若初始化不成功则直接返回
	    logdebug("android_nativeNvRead exit:init failed\n");
		return NULL;
	}

	unsigned char tmp[23] = { 0 };
	unsigned char after[20] = { 0 };
	memset(tmp, 0, sizeof(tmp));
	memset(after, 0, sizeof(after));
	nv_items_enum_type nvId = (nv_items_enum_type)index;
    if(SHOW_LOG == 1){
	    logdebug("android_nativeNvRead nvId = %d\n",(int)nvId);
	}

    if(SHOW_LOG == 1){
        char value0 = BYTE_PTR(index)[0];
        char value1 = BYTE_PTR(index)[1];
        char value2 = BYTE_PTR(index)[2];
        char value3 = BYTE_PTR(index)[3];

	    logdebug("android_nativeNvRead value0 = %d\n",(int)value0);
	    logdebug("android_nativeNvRead value1 = %d\n",(int)value1);
	    logdebug("android_nativeNvRead value2 = %d\n",(int)value2);
	    logdebug("android_nativeNvRead value3 = %d\n",(int)value3);
	}

	diag_nv_read(nvId,tmp, sizeof(tmp));  // 调用高通操作nv的api，读取NV给tmp

	if (SHOW_NV_LOG == 1) {
	    logdebug("android_nativeNvRead tmp strlen(tmp) = %d\n",(unsigned int)strlen((const char*)tmp));
		for(unsigned int i=0;i < sizeof(tmp);i++) {
			logdebug("android_nativeNvRead tmp p[%d] = %02x\n",i,tmp[i]);
		}
	}

    if(sizeof(tmp) > 3){
        for(unsigned int m=0;m < sizeof(tmp)-3;m++) {  // 去掉tmp的前三个字段，重新赋值给after
            //if (tmp[m+3] == NULL) {
            //	after[m] = ' ';
            //} else {
                after[m] = tmp[m+3];
            //}
        }
        after[sizeof(tmp)-4] = '\0';
	}

	const char* p = (const char*)(char*)after;
    unsigned int readLen = (unsigned int)strlen(p);
	if (SHOW_NV_LOG == 1) {
	    logdebug("android_nativeNvRead after strlen(after) = %d\n",(unsigned int)strlen(p));
		for(unsigned int i=0;i < sizeof(after);i++) {
			logdebug("android_nativeNvRead p[%d] = %02x\n",i,p[i]);
		}
	}
    char t[3] = {0};
	unsigned char result[46] = { 0 };
    for(unsigned int i= 0; i < readLen; i++){ // 16进制转string
        sprintf(t, "%c", (unsigned char)p[i]);
        memcpy(result + i, t, (unsigned int)strlen(t));
    }

	jstring flag_string = CharTojstring(env, (const char*)result);  // char转化为jstring
	return flag_string;
}

JNINativeMethod gMethods[] = {
		{ "nativeNvWrite", "(ILjava/lang/String;)V",(void*) Java_com_gosuncn_zfyfw_nv_NvUtilExt_nativeNvWrite },
		{ "nativeNvRead", "(I)Ljava/lang/String;",(void*) Java_com_gosuncn_zfyfw_nv_NvUtilExt_nativeNvRead }
};

int registerNativeMethods(JNIEnv* env) { //注册方法

	jclass clazz;
    logdebug("nvgxx registerNativeMethods s\n");
	clazz = env->FindClass(className);
	if (env->RegisterNatives(clazz, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0])) < 0) {
        logdebug("nvgxx registerNativeMethods e 1\n");
		return -1;
	}
    logdebug("nvgxx registerNativeMethods e 2\n");
	return 0;
}

jint JNI_OnLoad(JavaVM* vm, void* ) {
	JNIEnv* env = NULL;
	jint result = -1;

    logdebug("nvgxx JNI_OnLoad s\n");
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) == JNI_OK) {
        logdebug("nvgxx JNI_OnLoad 1\n");
		if (NULL != env && registerNativeMethods(env) == 0) {
            logdebug("nvgxx JNI_OnLoad  11\n");
			result = JNI_VERSION_1_4;
		}
	}else{
        logdebug("nvgxx JNI_OnLoad 2\n");
		if (NULL != env && registerNativeMethods(env) == 0) {
            logdebug("nvgxx JNI_OnLoad 21\n");
			result = JNI_VERSION_1_4;
		}
	}
    logdebug("nvgxx JNI_OnLoad e\n");
	return result;
}
