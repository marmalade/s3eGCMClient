package com.marmalade.studio.android.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class s3eGCMClientReceiverBase extends BroadcastReceiver {

	// Context
	protected Context m_Context = null;
    
    // would prefer if the s3eGCMClass had a proper class path and we could share these, but whatever
    static private final String PROPERTY_FILE = "game_notification_prefs";
    static private final String PROPERTY_GAME_TITLE = "game_title";
    static private final String PROPERTY_ICON_ID = "icon_id";
	
    @Override
    public void onReceive( Context context, Intent intent) {
    	// Get context
		m_Context = context;
    }

    protected String getGameTitle()
    {
        String ret = null;
        try
        {
            // Attempt to get the game title
            SharedPreferences prefs = m_Context.getSharedPreferences( PROPERTY_FILE, Context.MODE_PRIVATE);
            ret = prefs.getString(PROPERTY_GAME_TITLE,null);
        }
        catch (Exception ex)
        {
        }
        if (ret == null)
        {
            // 2nd chance, let's try and read the application name as set in the manifest
            try
            {
                int id = m_Context.getApplicationInfo().labelRes;
                ret = m_Context.getString(id);
            }
            catch (Exception ex)
            {
                ret = "";
            }
        }
        return ret;
    }
    
    protected int getIconId()
    {
        int icon_id = 0;
        
        try
        {
            // Attempt to get the game title
            SharedPreferences prefs = m_Context.getSharedPreferences( PROPERTY_FILE, Context.MODE_PRIVATE);
            icon_id = prefs.getInt(PROPERTY_ICON_ID, 0);
        }
        catch (Exception ex)
        {
        }
        
        return icon_id;
    }
}
