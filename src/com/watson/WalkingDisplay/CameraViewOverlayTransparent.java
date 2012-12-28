/*
 * @author Jeff Watson
 * 
 * This file transforms the camera image into a bitmap, which can
 * then have alpha applied, making it transparent. 
 * 
 */
package com.watson.WalkingDisplay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class CameraViewOverlayTransparent extends CameraViewOverlay implements Camera.PreviewCallback{
	private int width; //width of preview frame
	private int height; //height of preview
	private int compression; //level of compression to use
	private Camera mCamera; //our Camera
    private SurfaceHolder mHolder; //surface holding our camera's view
    private Bitmap bmp; //Bitmap object to draw to the screen
    private Matrix matrix = new Matrix(); //Matrix to orient
    private YuvImage yuv_image; //image data in YuV Format
	private Rect rect; // rectangle 
	private ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
	private int format; //format of data leaving camera, as an int
	private int rotationAngle; // the angle which to rotate the matrix & camera

	public CameraViewOverlayTransparent(Context context, int w, int h, int c) {
		super(context);
		
		rotationAngle = getRotationAngle();
		width = w;
		height = h;
		compression = c;
		rect = new Rect(0, 0, width, height);
		
		//set up the holder for this view to be 1x1 px ("invisible")
		mHolder = getHolder();
		mHolder.addCallback(this);    	
    	mHolder.setFixedSize(1, 1);
    	mHolder.setFormat(PixelFormat.TRANSPARENT);
    	
    	
    	Log.i("CameraViewOverlayTransparent", "sizes set: width " + width + ", height "+height);
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
        Log.i("CameraViewOverlayTransparent", "config change");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        rotationAngle = getRotationAngle();
        //mCamera.setDisplayOrientation(rotationAngle);
        matrix.setRotate(rotationAngle, width/2, height/2);
        
	}
    
    /* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("CameraViewOverlayTransparent", "entering surface created");
		
		startCameraPreview();

		Log.i("CameraViewOverlayTransparent", "exiting surface created");

	}
    
    
    private void startCameraPreview()
    {
    	Log.d("CameraViewOverlayTransparent", "entering startCameraPreview");
    	
    	try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }
    	
    	mCamera = getCameraInstance();  
    	if(mCamera != null)
    	{
    	mCamera.setPreviewCallback(this);
    	
    	Camera.Parameters p = mCamera.getParameters();
		p.setPreviewSize(width, height);
		format = p.getPreviewFormat();
		mCamera.setParameters(p);
		mCamera.setDisplayOrientation(rotationAngle);
        matrix.postRotate(rotationAngle);
		
        try {
			mCamera.setPreviewDisplay(mHolder);
			if(mCamera != null)
				mCamera.startPreview();
			Log.i("CameraViewOverlayTransparent", "preview started");
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("CameraViewOverlayTransparent", "preview not started");
		}
    	}
        
    }
    
	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.setPreviewCallback(null);
		mCamera.release();
	}
    
    public Bitmap getBitmap()
    {
    	return bmp;
    }

    /**
     * @author Jeff Watson
     * Convert data from camera to a image that can have transparency
     */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		
		// YUV formats require more conversion
		if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16)
		{
			// Get the YuV image
			yuv_image = new YuvImage(data, format, width, height, null);
			
			// Convert YuV to Jpeg
			output_stream.reset();
			
			yuv_image.compressToJpeg(rect, compression, output_stream);
			// Convert from Jpeg to Bitmap
			bmp = BitmapFactory.decodeByteArray(output_stream.toByteArray(), 0, output_stream.size());
					
			bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
					
		}			
				
		// Jpeg and RGB565 are supported by BitmapFactory.decodeByteArray
		else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565)
		{
			bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		CameraOverlayService.mTransImage.setImageBitmap(bmp);
		
	}

}
