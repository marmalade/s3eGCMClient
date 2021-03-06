/*
 * Internal header for the s3eGCMClient extension.
 *
 * This file should be used for any common function definitions etc that need to
 * be shared between the platform-dependent and platform-indepdendent parts of
 * this extension.
 */

/*
 * NOTE: This file was originally written by the extension builder, but will not
 * be overwritten (unless --force is specified) and is intended to be modified.
 */


#ifndef S3EGCMCLIENT_INTERNAL_H
#define S3EGCMCLIENT_INTERNAL_H

#include "s3eTypes.h"
#include "s3eGCMClient.h"
#include "s3eGCMClient_autodefs.h"


/**
 * Initialise the extension.  This is called once then the extension is first
 * accessed by s3eregister.  If this function returns S3E_RESULT_ERROR the
 * extension will be reported as not-existing on the device.
 */
s3eResult s3eGCMClientInit();

/**
 * Platform-specific initialisation, implemented on each platform
 */
s3eResult s3eGCMClientInit_platform();

/**
 * Terminate the extension.  This is called once on shutdown, but only if the
 * extension was loader and Init() was successful.
 */
void s3eGCMClientTerminate();

/**
 * Platform-specific termination, implemented on each platform
 */
void s3eGCMClientTerminate_platform();

void s3eGCMClientSetAppData_platform(const char* title, int icon_id);

const char* s3eGCMClientGetRegisterId_platform(const char* sender_id);

void s3eGCMClientUnregisterId_platform();

void s3eGCMClientLocalNotificationSchedule_platform(int delaySec, int id, const char* alertBody, const char* alertAction, const char* sound);

void s3eGCMClientLocalNotificationUnschedule_platform(int id);

void s3eGCMClientLocalNotificationEnable_platform(bool enable);

void s3eGCMClientLocalNotificationClearAll_platform();

#endif /* !S3EGCMCLIENT_INTERNAL_H */
