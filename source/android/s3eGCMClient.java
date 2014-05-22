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

java implementation of the s3eGCMClient extension.

Add android-specific functionality here.

These functions are called via JNI from native code.
*/
/*
 * NOTE: This file was originally written by the extension builder, but will not
 * be overwritten (unless --force is specified) and is intended to be modified.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ideaworks3d.marmalade.LoaderAPI;
import com.ideaworks3d.marmalade.LoaderActivity;
import java.sql.Timestamp;
import com.marmalade.studio.android.gcm.s3eGCMClientLocalReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.app.Activity;
import java.util.Calendar;
 
public class s3eGCMClient
{
	// Callback functions
	private static native void native_notificationCallback();
	
	// Constants
	static private final String PROPERTY_APP_VERSION = "app_version";
	static private final String PROPERTY_REG_ID = "registration_id";
	static private final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "on_server_expiration_time_ms";
	static private final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7; // 7 Days
	static private final int REGISTRATION_TRIALS = 3;
    
    // would prefer if this class had a proper class path and we could share these, but whatever
    static private final String PROPERTY_FILE = "game_notification_prefs";
    static private final String PROPERTY_GAME_TITLE = "game_title";
    static private final String PROPERTY_ICON_ID = "icon_id";
	    
	// Context
	static private Context m_Context = null;
	
	// Google cloud messaging
    static private GoogleCloudMessaging m_GCM = null;
    
    // Registration identifier
    static private String m_RegId = "";
    
    // Sender identifier
    static private String m_SenderId = "";
	
	// Trials
    static private int m_Trials = 0;
    
	public s3eGCMClient() {
	
	}
	
    public void s3eGCMClientSetAppData(String title, int icon_id)
    {
        LoaderAPI.trace("s3eGCMClientSetAppData");
        saveGameTitle(title);
        LoaderAPI.trace("..game title set");
        saveIconId(icon_id);
        LoaderAPI.trace("..icon id set, all done");
    }
    
    public String s3eGCMClientGetRegisterId( String sender_id)
    {    	
		try {

			// Set sender identifier
			m_SenderId = sender_id;
					
			// Get context
			Context context = getContext();
			
			if ( null != context) {

				// Get google cloud messaging
				m_GCM = GoogleCloudMessaging.getInstance(context);
										
				// Set registration identifier
				setRegistrationId();
				
				// If not registration identifier
				if ( m_RegId.length() == 0) { m_Trials = 0; register(); }

			}
		} catch ( Exception ex) {
	        
			// Do nothing
			// e.printStackTrace();
	    }
		
		return ( m_RegId);
    }

	public void s3eGCMClientUnregisterId()
    {    	
	try {					
			Context context = getContext();
			
			if ( null != context) {
			
				// Get google cloud messaging
				//m_GCM = GoogleCloudMessaging.getInstance(context);
				//m_GCM.unregister();
									
				
			}
		} catch ( Exception ex) {
	        
			// Do nothing
			// e.printStackTrace();
	    }
    }
	
	public void s3eGCMClientRegistrationReceived( Context context, String reg_id)
    {				
		if ( null != context) {
					
			// Save new registration identifier
			saveRegistrationId( context, reg_id);
		}
		
		if ( null != LoaderActivity.m_Activity) {
						
			// Store new registration identifier
			m_RegId = reg_id;
		}
    }
	
	public void s3eGCMClientNotificationReceived()
    {				
		if ( null != LoaderActivity.m_Activity) {
						
			// Call native notification callback
			native_notificationCallback();
		}
    }
    
	// ---------------------------------------------------------------------------------
    
    private int getAppVersion( Context context) {
    	
        try {
        	
            PackageInfo info = context.getPackageManager().getPackageInfo( context.getPackageName(), 0);
            return info.versionCode;
            
        } catch (NameNotFoundException e) {
        	
            // Should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    private SharedPreferences getGCMPreferences( Context context) {
    	
    	// Return shared preferences
        return context.getSharedPreferences( s3eGCMClient.class.getSimpleName(), Context.MODE_PRIVATE);
    }
    
    private void setRegistrationId() {
    	
    	// Get preferences
        final SharedPreferences prefs = getGCMPreferences( m_Context);
		        
        // Get registration identifier from preferences
        String reg_id = prefs.getString(PROPERTY_REG_ID, "");
                		
        // Check if there is no registration identifier
        if ( reg_id.length() == 0) { return; }
		        
        // Check application is not updated or expired
        int reg_version = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        long exp_time = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, Long.MIN_VALUE);
		        
        if ( ( reg_version != getAppVersion( m_Context)) || ( System.currentTimeMillis() > exp_time)) {
        	
            return;
        }
		        
        m_RegId = reg_id;
    }
    
    private void register() {
    		    
	    try {
		
			++m_Trials;
		
			if ( m_Trials <= REGISTRATION_TRIALS) {
	    					
				// Register
				m_RegId = m_GCM.register( m_SenderId);
				
				if ( null == m_RegId) { m_RegId = ""; }
				
				// Save the registration identifier
				saveRegistrationId( m_Context, m_RegId);
			} 
			else {
			
				if ( null == m_RegId) { m_RegId = ""; }
			}
	        
	    } catch ( Exception ex) {
	        			
			register();
	    }     
    }
    
    private void saveRegistrationId(  Context context, String reg_id) {
    	        		
        // Get preferences editor
        SharedPreferences.Editor editor = getGCMPreferences( context).edit();
        
        // Write data
		editor.putString(PROPERTY_REG_ID, reg_id);
		editor.putInt(PROPERTY_APP_VERSION, getAppVersion( context));
		editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, ( System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS));
		
		// Commit data
		editor.commit();
    }
    
    
    private void saveGameTitle(String game_title)
    {
        // Get preferences editor
        Context context = getContext();
        SharedPreferences prefs = context.getSharedPreferences( PROPERTY_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Write data
        editor.putString(PROPERTY_GAME_TITLE, game_title);
        
        // Commit data
        editor.commit();
    }

    private void saveIconId(int icon_id)
    {
        // Get preferences editor
        Context context = getContext();
        SharedPreferences prefs = context.getSharedPreferences( PROPERTY_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Write data
		editor.putInt(PROPERTY_ICON_ID, icon_id);
		
		// Commit data
		editor.commit();
    }

    private Context getContext() {
        if (m_Context == null) {
            m_Context = LoaderActivity.m_Activity.getApplicationContext();
        }
        
        return m_Context;
    }

    public void s3eGCMClientLocalNotificationSchedule(int delaySec, int id, String alertBody, String alertAction, String sound)
    {
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, delaySec);

        Intent intent = new Intent(LoaderAPI.getActivity(), s3eGCMClientLocalReceiver.class);
        intent.putExtra("alarm_message", alertBody);
        intent.putExtra("requestCode", id);
        // TODO: put extra sound?
        // TODO: put extra action?

        // TODO: shared preferences to keep notificationsIDs

        PendingIntent sender = PendingIntent.getBroadcast(LoaderAPI.getActivity(), id, intent,
                       PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        Activity activity = LoaderAPI.getActivity();
        AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        
        LoaderAPI.trace(String.format("s3eGCMClientLocalNotificationSchedule: notification %08x %s in %d seconds", id, alertBody, delaySec));
    }
    
    public void s3eGCMClientLocalNotificationUnschedule(int id)
    {
        Intent intent = new Intent(LoaderAPI.getActivity(), s3eGCMClientLocalReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(LoaderAPI.getActivity(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Activity me = LoaderAPI.getActivity();

        AlarmManager am = (AlarmManager) me.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);

        LoaderAPI.trace(String.format("s3eGCMClientLocalNotificationUnschedule: cancelled %08x", id));

        // TODO: shared preferences to remove notificationsIDs
    }
    
    public void s3eGCMClientLocalNotificationEnable(boolean enable)
    {
        if (enable) 
        {
               // TODO: set shared preferences to not block receive
               // allow adding/removing ?
        }
        else
        {
               // TODO: set shared preferences to block receive
               // allow adding/removing ?
        }
    }
    
    public void s3eGCMClientLocalNotificationClearAll()
    {
        // TODO: shared preferences to remove all notificationsIDs
    }
}
