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

package com.watson.XRayDisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.watson.XRayDisplay.R;

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
