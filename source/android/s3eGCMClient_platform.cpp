/*
 * (C) 2001-2014 Marmalade. All Rights Reserved.
 *
 * This document is protected by copyright, and contains information
 * proprietary to Marmalade.
 *
 * This file consists of source code released by Marmalade under
 * the terms of the accompanying End User License Agreement (EULA).
 * Please do not use this program/source code before you have read the
 * EULA and have agreed to be bound by its terms.
 *
 * android-specific implementation of the s3eGCMClient extension.
 * Add any platform-specific functionality here.
 */
/*
 * NOTE: This file was originally written by the extension builder, but will not
 * be overwritten (unless --force is specified) and is intended to be modified.
 */
#include "s3eGCMClient_internal.h"

#include "s3eEdk.h"
#include "s3eEdk_android.h"
#include <jni.h>
#include "IwDebug.h"

static size_t const REGISTER_ID_SIZE = 4096;
static char g_RegisterId[REGISTER_ID_SIZE];
static jobject g_Obj;
static jmethodID g_s3eGCMClientSetAppData;
static jmethodID g_s3eGCMClientGetRegisterId;
static jmethodID g_s3eGCMClientUnregisterId;
static jmethodID g_s3eGCMClientLocalNotificationSchedule;
static jmethodID g_s3eGCMClientLocalNotificationUnschedule;
static jmethodID g_s3eGCMClientLocalNotificationEnable;
static jmethodID g_s3eGCMClientLocalNotificationClearAll;

void JNICALL s3eGCMClient_notificationCallback(JNIEnv* env, jobject obj)
{
	s3eGCMClientCallbackData data;
    s3eEdkCallbacksEnqueue(S3E_EXT_GCMCLIENT_HASH, S3E_GCMCLIENT_CALLBACK_NOTIFICATION_RECEIVED, (void*)&data, sizeof(s3eGCMClientCallbackData));
}

s3eResult s3eGCMClientInit_platform()
{	
    // Initialize register id char array
    memset(g_RegisterId, 0, REGISTER_ID_SIZE);
	
    // Get the environment from the pointer
    JNIEnv* env = s3eEdkJNIGetEnv();
    jobject obj = NULL;
    jmethodID cons = NULL;
	
    // Get the extension class
    jclass cls = s3eEdkAndroidFindClass("s3eGCMClient");
    if (!cls)
        goto fail;

    // Get its constructor
    cons = env->GetMethodID(cls, "<init>", "()V");
    if (!cons)
        goto fail;

    // Construct the java class
    obj = env->NewObject(cls, cons);
    if (!obj)
        goto fail;

    // Get all the extension methods
    g_s3eGCMClientSetAppData = env->GetMethodID(cls, "s3eGCMClientSetAppData", "(Ljava/lang/String;I)V");
    if (!g_s3eGCMClientSetAppData)
        goto fail;

    g_s3eGCMClientGetRegisterId = env->GetMethodID(cls, "s3eGCMClientGetRegisterId", "(Ljava/lang/String;)Ljava/lang/String;");
    if (!g_s3eGCMClientGetRegisterId)
        goto fail;

    g_s3eGCMClientUnregisterId = env->GetMethodID(cls, "s3eGCMClientUnregisterId", "()V");
    if (!g_s3eGCMClientUnregisterId)
        goto fail;

    g_s3eGCMClientLocalNotificationSchedule = env->GetMethodID(cls, "s3eGCMClientLocalNotificationSchedule", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (!g_s3eGCMClientLocalNotificationSchedule)
        goto fail;

    g_s3eGCMClientLocalNotificationUnschedule = env->GetMethodID(cls, "s3eGCMClientLocalNotificationUnschedule", "(I)V");
    if (!g_s3eGCMClientLocalNotificationUnschedule)
        goto fail;

    g_s3eGCMClientLocalNotificationEnable = env->GetMethodID(cls, "s3eGCMClientLocalNotificationEnable", "(Z)V");
    if (!g_s3eGCMClientLocalNotificationEnable)
        goto fail;

    g_s3eGCMClientLocalNotificationClearAll = env->GetMethodID(cls, "s3eGCMClientLocalNotificationClearAll", "()V");
    if (!g_s3eGCMClientLocalNotificationClearAll)
        goto fail;

    static const JNINativeMethod methods[] =
    {
        {"native_notificationCallback","()V",(void*)&s3eGCMClient_notificationCallback}
    };
	
    // Register the native hooks
    if (env->RegisterNatives(cls, methods,sizeof(methods)/sizeof(methods[0])))
        goto fail;

    IwTrace(GCMCLIENT, ("GCMCLIENT init success"));
    g_Obj = env->NewGlobalRef(obj);
    env->DeleteLocalRef(obj);
    env->DeleteGlobalRef(cls);

    // Add any platform-specific initialisation code here
    return S3E_RESULT_SUCCESS;

fail:
    jthrowable exc = env->ExceptionOccurred();
    if (exc)
    {
        env->ExceptionDescribe();
        env->ExceptionClear();
        IwTrace(s3eGCMClient, ("One or more java methods could not be found"));
    }
    return S3E_RESULT_ERROR;

}

void s3eGCMClientTerminate_platform()
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    env->DeleteGlobalRef(g_Obj);
    g_Obj = NULL;
}

void s3eGCMClientSetAppData_platform(const char * title, int icon_id)
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    jstring title_jstr = env->NewStringUTF(title);
    env->CallVoidMethod(g_Obj, g_s3eGCMClientSetAppData, title_jstr, icon_id);
}

const char* s3eGCMClientGetRegisterId_platform(const char* sender_id)
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    jstring sender_id_jstr = env->NewStringUTF(sender_id);
    jstring reg_id = ( jstring) env->CallObjectMethod(g_Obj, g_s3eGCMClientGetRegisterId, sender_id_jstr);
	
	if (reg_id) 
	{
		jboolean free;
		const char* p_res = env->GetStringUTFChars( reg_id, &free);
		
		if ( p_res)
		{
			if ( strlen(p_res) < REGISTER_ID_SIZE)
			{
				strcpy( g_RegisterId, p_res);
			}
			
			env->ReleaseStringUTFChars(reg_id, p_res);
		}
	}
	
    return ( g_RegisterId);
}

void s3eGCMClientUnregisterId_platform()
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    env->CallObjectMethod(g_Obj, g_s3eGCMClientUnregisterId);
	
    return;
}

void s3eGCMClientLocalNotificationSchedule_platform(int delaySec, int id, const char* alertBody, const char* alertAction, const char* sound)
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    jstring alertBody_jstr = env->NewStringUTF(alertBody);
    jstring alertAction_jstr = env->NewStringUTF(alertAction);
    jstring sound_jstr = env->NewStringUTF(sound);
    env->CallVoidMethod(g_Obj, g_s3eGCMClientLocalNotificationSchedule, delaySec, id, alertBody_jstr, alertAction_jstr, sound_jstr);
}

void s3eGCMClientLocalNotificationUnschedule_platform(int id)
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    env->CallVoidMethod(g_Obj, g_s3eGCMClientLocalNotificationUnschedule, id);
}

void s3eGCMClientLocalNotificationEnable_platform(bool enable)
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    env->CallVoidMethod(g_Obj, g_s3eGCMClientLocalNotificationEnable, enable);
}

void s3eGCMClientLocalNotificationClearAll_platform()
{
    JNIEnv* env = s3eEdkJNIGetEnv();
    env->CallVoidMethod(g_Obj, g_s3eGCMClientLocalNotificationClearAll);
}
