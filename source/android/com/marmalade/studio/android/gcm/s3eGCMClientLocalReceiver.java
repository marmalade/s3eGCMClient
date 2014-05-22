package com.marmalade.studio.android.gcm;

import com.ideaworks3d.marmalade.LoaderActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

 
public class s3eGCMClientLocalReceiver extends s3eGCMClientReceiverBase {

	@Override
	public void onReceive(Context context, Intent intent) {
	
        super.onReceive(context, intent);
        
		// TODO: block receive when disabled
		
		try {
			Bundle bundle = intent.getExtras();

		     String message = bundle.getString("alarm_message");
		     
		     int requestCode = intent.getExtras().getInt("requestCode");
		     // TODO: remove requestCode from shared preferences
		     
			
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(context)
			        .setSmallIcon(getIconId())
			        .setContentTitle(getGameTitle())
			        .setContentText(message)
			        .setAutoCancel(true);

			mBuilder.setDefaults(Notification.DEFAULT_ALL);
			mBuilder.setOnlyAlertOnce(true);
		    
			// Get intent
	        Intent intent2 = context.getPackageManager().getLaunchIntentForPackage( context.getPackageName());
			        
	        if ( null != intent2) {
				// Add flag
				intent2.addFlags( PendingIntent.FLAG_ONE_SHOT);
	        
	        	// Create content intent
	            PendingIntent content_intent = PendingIntent.getActivity( context, requestCode, intent2, PendingIntent.FLAG_ONE_SHOT);
	            
	            
	            // Set content intent
	            mBuilder.setContentIntent( content_intent);
	                    
	            // Notify
	            NotificationManager mNotificationManager =
	    			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    			mNotificationManager.notify(requestCode, mBuilder.build());
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}