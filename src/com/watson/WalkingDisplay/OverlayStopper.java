package com.watson.WalkingDisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OverlayStopper extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
			
			// call startService after 
			//Receiving ACTION_SCREEN_OFF, or ACTION_USER_PRESENT
		
	        Intent i = new Intent(context, CameraOverlayService.class);
	        i.setAction(intent.getAction());
	        context.startService(i);

	}

}
