/**
 * @author Jeff Watson
 * 
 *  Copyright 2012 Jeff Watson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.watson.WalkingDisplay;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class CameraOverlayService extends Service{
	
		public static String WIDGET_PRESSED = "Widget Pressed";
	
		private static final String PREFS_NAME = "WalkingDisplay_Prefs";
		private static final String TAG = "CameraOverlayService";
		private WindowManager wm;
	    private NotificationManager mNM;
	    private BroadcastReceiver mReceiver;
	    private WindowManager.LayoutParams params;

	    // Unique Identification Number for the Notification.
	    // We use it on Notification start, and to cancel it.
	    private int NOTIFICATION = R.string.local_service_started;
	    private CameraViewOverlay mView;
	    private static RelativeLayout mLayout ;
	    public static ImageView mTransImage;
	    
	    private static int xOffset;
	    private static int yOffset;
	    private static int mGravity;
	    private static int compression;
	    public static int mPreviewHeight;
	    public static int mPreviewWidth;
	    public static boolean transparency;
	    public static boolean persistence;
	    public static float transLevel;
	    
	    public static boolean isServiceRunning = false;
	    /**
	     * Class for clients to access.  Because we know this service always
	     * runs in the same process as its clients, we don't need to deal with
	     * IPC.
	     */
	    public class LocalBinder extends Binder {
	        CameraOverlayService getService() {
	            return CameraOverlayService.this;
	        }
	    }

	    @Override
	    public void onCreate() {
	    	// initialize the imageView of the Transparent preview
	    	mTransImage = new ImageView(this);
	    	
	    	// launch the notification
        	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        	
        	// for knowing when the screen turns off, and the user is present
	    	IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
	    	filter.addAction(Intent.ACTION_USER_PRESENT	);
	    	mReceiver = new OverlayStopper();
	    	registerReceiver(mReceiver, filter);
	    	
	    	//for adding and removing views from the main window
	        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
	        
	        //restore the saved settings, important for persistence	        
	    	restoreSharedPrefs();
	        
	        //set the layout parameters
	        params = new WindowManager.LayoutParams();

	        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
	        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
	        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
	        params.format = PixelFormat.TRANSLUCENT;
	        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
	        params.alpha = transLevel / 100;
	        params.gravity = mGravity;
	        params.x = xOffset; 
	        params.y = yOffset;
	    }

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	    	Log.i(TAG, "Received start id " + startId + ": " + intent);
	    	
	    	restoreSharedPrefs();
	    	
	    	// if persistence is off, just start or remove the view
	    	if(!persistence)
	    	{
	    		isServiceRunning = !isServiceRunning;

	    		
	    		if(isServiceRunning)
	    		{
	    			addViews();
	    		}
	    		if(!isServiceRunning)
	    		{
	    			endService();
	    		}
	    	}
	    	
	    	// if persistence is enabled, different actions must be taken
	    	if(persistence)
	    	{
	    		if(intent == null || intent.getAction() == null)
	    		{
	    			isServiceRunning = !isServiceRunning;
	    			
	    			if(isServiceRunning)
	    			{
	    				addViews();
	    			}
	    		}
	    		else
	    		{
	    			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
	    			{
	    				removeViewFromWindow();
	    			}
	    			if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
	    			{
	    				addViews();
	    			}
	    			if(intent.getAction().equals(WIDGET_PRESSED))
	    			{
	    				isServiceRunning = !isServiceRunning;
	    				
	    				if(isServiceRunning)
	    	    		{
	    	    			addViews();
	    	    		}
	    			}	
	    		}

	    		if(!isServiceRunning)
	    		{
	    			endService();
	    		}
	    	}
	    	
	        // We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
	        return START_STICKY;
	    }
	    

	    private void endService() {
	    	Intent stopping_intent = new Intent(getApplicationContext(), CameraOverlayService.class);
        	isServiceRunning = false;
        	stopService(stopping_intent);
        	mNM.cancel(NOTIFICATION);
        	
        	Log.i(TAG, "Service Ended");
			
		}

		private void addViews() {
			mLayout = new RelativeLayout(this);
			
       		if(transparency)
       		{
       			mView = new CameraViewOverlayTransparent(this, mPreviewWidth, mPreviewHeight, compression);
       			mTransImage = new ImageView(this);
       			mLayout.addView((View) mTransImage);
        		mTransImage.setImageBitmap(mView.getCameraBitmap());
     		}
        	else
        	{
        		mView =new CameraViewOverlay(this, mPreviewWidth, mPreviewHeight);
        	}
        			
        	mLayout.addView(mView);
        	wm.addView(mLayout, params);
		    Log.i(TAG, "Layout added");
		    showNotification();
			
		}

		private void removeViewFromWindow() {
			wm.removeView(mLayout);
			
			Log.i(TAG, "View Removed from Window");
		}

		@Override
	    public void onDestroy() {
	    	Log.i(TAG, "Received stop service");	    	
	    	
	        // Cancel the persistent notification.
	        mNM.cancel(NOTIFICATION);

	        try
	        {
	        	wm.removeView(mLayout);	            
	        }
	        catch(Exception e)
	        {
	        	//tried to remove a view that isn't there.
	        	//ignore
	        }
	       
	        mView = null;
	        
	        unregisterReceiver(mReceiver);
	    }
	    

	    @Override
	    public IBinder onBind(Intent intent) {
	        return mBinder;
	    }

	    // This is the object that receives interactions from clients.  See
	    // RemoteService for a more complete example.
	    private final IBinder mBinder = new LocalBinder();

	    /**
	     * Show a notification while this service is running.
	     */
		private void showNotification() {
	    	
	    	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        	if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
        	    // Do something for ICS and above versions
	    	
	    	//Build the notification
	    	Notification.Builder builder = new Notification.Builder(getBaseContext());
	        builder.setSmallIcon(R.drawable.ic_stat_name);
	        builder.setContentTitle(getString(R.string.app_name));
	        builder.setOngoing(true);
	        builder.setContentText(getString(R.string.notif_turn_off));
	        
	        //create an intent for when the status bar item is clicked
	        Intent notificationIntent = new Intent(this, CameraOverlayService.class);
	        //notificationIntent.putExtra("stop_service", true);
	        PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

	        builder.setContentIntent(contentIntent);
	        
	        //builder.setContentIntent(contentIntent);
	        Notification notification = builder.getNotification();

	        //Send the notification
	        mNM.notify(NOTIFICATION, notification);
        	}
        	else
        	{
        		// do something for phones running an SDK before ICS
        		
        	}

	    }
	    
	    private void restoreSharedPrefs()
	    {
	    	SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
	    	
	    	xOffset = prefs.getInt("xOffset", 0);
	    	yOffset = prefs.getInt("yOffset", 0);
	    	mGravity = prefs.getInt("mGravity", 48);
	    	mPreviewHeight = prefs.getInt("mPreviewHeight", 0);
	    	mPreviewWidth = prefs.getInt("mPreviewWidth", 0);
	    	transparency = prefs.getBoolean("transparency", false);
	    	persistence = prefs.getBoolean("persistance", false);
	    	compression = prefs.getInt("compression", 80);
	    	transLevel = prefs.getFloat("transLevel", .7f);
	    }
	}