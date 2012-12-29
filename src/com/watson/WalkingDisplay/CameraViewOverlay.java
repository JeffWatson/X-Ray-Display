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

import java.io.IOException;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;

public class CameraViewOverlay extends SurfaceView implements Callback {
	private int width; //width of preview frame
	private int height; //height of preview
	private Camera mCamera; //our Camera
    private SurfaceHolder mHolder; //surface holding our camera's view
    private boolean isLoaded; //has the surface been loaded?
    
    /*
     * @param context context for the view
     */    
	public CameraViewOverlay(Context context) {
		super(context);
	}
	
	/*
     * @param context context for the view
     * @param w width of frame
     * @param h height of frame
     */
	public CameraViewOverlay(Context context, int w, int h) {
		super(context);
		width = w;
		height = h;
		
		//construct our SurfaceHolder accordingly
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setFixedSize(width, height);
		Log.i("CameraViewOverlay", "sizes set: width " + width + ", height "+height);
		
	}
	
	/** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        	Log.e("GetCameraInstance", "camera don't exist", e);
        }
        return c; // returns null if camera is unavailable
    }
    
    public void onConfigurationChanged(Configuration newConfig)
    {
    	super.onConfigurationChanged(newConfig);
    	
    	if(isLoaded && mCamera != null)
		{
			mCamera.setDisplayOrientation(getRotationAngle());
		}
    }

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.i("CameraViewOverlay", "entering surface change");
		// If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.		
        Log.i("CameraViewOverlay", "exiting surface change");

	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("CameraViewOverlay", "entering surface created");
		
		isLoaded = true;
        startCameraPreview();

		Log.i("CameraViewOverlay", "exiting surface created");

	}
	
	private void startCameraPreview()
	{
		Log.d("CameraViewOverlay", "entering startCameraPreview");
				
		try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }
		
		/* Get the camera;
		 * modify the parameters;
		 * reset them 
		 */
		mCamera = getCameraInstance(); 
		
		if(mCamera != null)
		{
		Camera.Parameters p = mCamera.getParameters(); 	
		p.setPreviewSize(width, height);
		mCamera.setParameters(p);
		
		mCamera.setDisplayOrientation(getRotationAngle());
		try {
			mCamera.setPreviewDisplay(mHolder);
			if(mCamera != null)
				mCamera.startPreview();
			Log.i("CameraViewOverlay", "preview started");
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("CameraViewOverlay", "preview not started");
		}
		}
	}
	
	public Bitmap getCameraBitmap()
	{
		return null;
	}
	
	private int getRotationAngle() //returns the correct display of the camera rotated
	{
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    	int rotation = wm.getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;

            case Surface.ROTATION_90: degrees = 90; break;

            case Surface.ROTATION_180: degrees = 180; break;

            case Surface.ROTATION_270: degrees = 270; break;

        }
        int result = 0;
        CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info);
        result = (info.orientation - degrees + 360) % 360;
        
        return result;
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		isLoaded = false;
		
		mCamera.release();
	}

}
