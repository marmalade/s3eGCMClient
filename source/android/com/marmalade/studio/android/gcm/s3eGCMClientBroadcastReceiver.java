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
 */

package com.marmalade.studio.android.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import android.app.Notification;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
//import s3eGCMClient;

/**
 * Handling of GCM messages.
 */
public class s3eGCMClientBroadcastReceiver extends s3eGCMClientReceiverBase {
	
	// Google cloud messaging
    static private GoogleCloudMessaging m_GCM = null;
    
	// Context
	static private int m_NotificationId = 1;
        
    @Override
    public void onReceive( Context context, Intent intent) {

        super.onReceive(context, intent);
		
		// Get google cloud messaging
		m_GCM = GoogleCloudMessaging.getInstance( m_Context);
		
		if ( null != m_Context && null != m_GCM) {
		
			if ( intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			
				// Handle registration
				handleRegistration( intent);
				
			} else if ( intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			
				// Handle notification
				handleNotification( intent);
			}
		} 
		
		// Set result code
		setResultCode( Activity.RESULT_OK);		
    }

    // ---------------------------------------------------------------------------------
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void doNotificationCallback() {
    	
    	try {
    		
    		// Get extension class
    		final Class extension_class = Class.forName("s3eGCMClient");
    		
    		// Get notification method
    		final Method notification_method = extension_class.getMethod("s3eGCMClientNotificationReceived", new Class[] { });
    		
    		// Access method
			AccessController.doPrivileged( new PrivilegedExceptionAction() {
			    public Object run() throws Exception {

			    	// Set accessible
			    	if( !notification_method.isAccessible()) { notification_method.setAccessible(true); }
			    	
			    	// Invoke
					notification_method.invoke( extension_class.newInstance());
					
			      return null;
			    }
			});
			
		} catch (Exception e) {
						
			// Do nothing
			// e.printStackTrace();
		}
    }
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void doRegistrationCallback( String reg_id) {
    	
    	try {
		
			// Get context
			final Context context = m_Context;
		
			// Get registration identifier
			final String registration_id = reg_id;
    		
    		// Get extension class
    		final Class extension_class = Class.forName("s3eGCMClient");
    		
    		// Get registration method
    		final Method registration_method = extension_class.getMethod("s3eGCMClientRegistrationReceived", new Class[] { Context.class, String.class });
    		
    		// Access method
			AccessController.doPrivileged( new PrivilegedExceptionAction() {
			    public Object run() throws Exception {

			    	// Set accessible
			    	if( !registration_method.isAccessible()) { registration_method.setAccessible(true); }
			    	
			    	// Invoke
					registration_method.invoke( extension_class.newInstance(), context, registration_id);
					
			      return null;
			    }
			});
			
		} catch (Exception e) {
									
			// Do nothing
			// e.printStackTrace();

		}
    }
	
	private void handleNotification( Intent intent) {
	
		// Get message type
        String message_type = m_GCM.getMessageType(intent);
        
        // Get extras bundles
        Bundle extras = intent.getExtras();
		
		 // Get message
		//String message = extras.getString("Message");
		//String alert = extras.getString("Alert");
        String alert = extras.getString("alert");
    		int type = 1;
    		try {
    			type = Integer.parseInt(extras.getString("Type"));
    		} catch (Exception e) {
    		}
                        
		// There is no difference within the message types yet
        if ( GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals( message_type)) {
        	
		if (type == 1)
		{
            		sendToast( alert);
		}
		else if (type == 2)
		{
            		sendNotification( alert);
		}
		else if (type == 3)
		{
            		sendToast( alert);
            		sendNotification( alert);
		}
            
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals( message_type)) {
        	
		if (type == 1)
		{
            		sendToast( alert);
		}
		else if (type == 2)
		{
            		sendNotification( alert);
		}
		else if (type == 3)
		{
            		sendToast( alert);
            		sendNotification( alert);
		}
            
        } else {
        	
		if (type == 1)
		{
            		sendToast( alert);
		}
		else if (type == 2)
		{
            		sendNotification( alert);
		}
		else if (type == 3)
		{
            		sendToast( alert);
            		sendNotification( alert);
		}
        }
		                
        // Do notification callback
        doNotificationCallback();
	}	
	
	private void handleRegistration( Intent intent) {
	
		String registration_id = intent.getStringExtra("registration_id");
				
		if ( null != registration_id) {
		
			// Do registration callback
			doRegistrationCallback( registration_id);
		}
	}
    private void sendToast( String msg) {
	Context context = m_Context;
	CharSequence text = msg;
	int duration = Toast.LENGTH_SHORT;

	Toast.makeText(context, text, duration).show();

	}
    
    private void sendNotification( String msg) {
    	
    	// Get notification manager
    	NotificationManager notification = 
    			( NotificationManager) m_Context.getSystemService( Context.NOTIFICATION_SERVICE);
            	
        // Get intent
        Intent intent = m_Context.getPackageManager().getLaunchIntentForPackage( m_Context.getPackageName());
		        
        if ( null != intent) {
		
			// Add flag
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
        
        	// Create content intent
            PendingIntent content_intent = PendingIntent.getActivity( m_Context, 0, intent, 0);
            
            // Create notification compat builder
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( m_Context)
            	.setAutoCancel( true)
    	        .setContentText( msg)
    	        .setContentTitle( getGameTitle() )
    	        .setSmallIcon( getIconId() )
	       .setDefaults(Notification.DEFAULT_ALL)
    	        .setStyle( new NotificationCompat.BigTextStyle().bigText(msg));
            
            // Set content intent
            mBuilder.setContentIntent( content_intent);
                    
            // Notify
            notification.notify( m_NotificationId, mBuilder.build());
        }
    }
}
