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

Generic implementation of the s3eGCMClient extension.
This file should perform any platform-indepedentent functionality
(e.g. error checking) before calling platform-dependent implementations.
*/

/*
 * NOTE: This file was originally written by the extension builder, but will not
 * be overwritten (unless --force is specified) and is intended to be modified.
 */


#include "s3eGCMClient_internal.h"
s3eResult s3eGCMClientInit()
{
    //Add any generic initialisation code here
    return s3eGCMClientInit_platform();
}

void s3eGCMClientTerminate()
{
    //Add any generic termination code here
    s3eGCMClientTerminate_platform();
}

void s3eGCMClientSetAppData(const char * title, int icon_id)
{
    s3eGCMClientSetAppData_platform(title, icon_id);
}

const char* s3eGCMClientGetRegisterId(const char* sender_id)
{
	return s3eGCMClientGetRegisterId_platform(sender_id);
}

void s3eGCMClientUnregisterId()
{
	s3eGCMClientUnregisterId_platform();
}

void s3eGCMClientLocalNotificationSchedule(int delaySec, int id, const char* alertBody, const char* alertAction, const char* sound)
{
       s3eGCMClientLocalNotificationSchedule_platform(delaySec, id, alertBody, alertAction, sound);
}

void s3eGCMClientLocalNotificationUnschedule(int id)
{
       s3eGCMClientLocalNotificationUnschedule_platform(id);
}

void s3eGCMClientLocalNotificationEnable(bool enable)
{
       s3eGCMClientLocalNotificationEnable_platform(enable);
}

void s3eGCMClientLocalNotificationClearAll()
{
       s3eGCMClientLocalNotificationClearAll_platform();
}

