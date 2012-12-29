package com.watson.WalkingDisplay;

import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class WalkingDisplayActivity extends Activity implements OnCheckedChangeListener {
	/*TODO 
	 * 1. Add Some Explanations of settings, maybe a welcome? 
	 * 2. Widget changes?
	 * 2. Clean up code
	 * 3. ????
	 * 4. Profit
	 */
	
	private final String TAG = "WalkingDisplayActivity";
	
	private final String PREFS_NAME = "WalkingDisplay_Prefs";
	
	private int xOffset = 0;
	private int yOffset = 0;
	private int mGravity = 48;
	private int mPreviewHeight = 0;
	private int mPreviewWidth = 0;
	private boolean transparency = false;
	private boolean persistance = false;
	private static List <Camera.Size> sizeList;
	private int lastSizeSelection = 0; 
	private int compression = 0;
	private float transLevel = 0;
	private SeekBar barComp = null;
	private SeekBar barTrans = null;
	private boolean showWelcomeDialog = true;
	private Switch serviceSwitch;
	
	
    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        restoreSharedPrefs();
        
        setupActionBar();
        serviceSwitch.setChecked(CameraOverlayService.isServiceRunning);
        serviceSwitch.setOnCheckedChangeListener(this);
        
        if(showWelcomeDialog)
        {
        	//LayoutInflater inflater = getLayoutInflater();
        	View layout = View.inflate(this, R.layout.first_run_help_dialog, null);
        	CheckBox dialogCheckBox = (CheckBox) layout.findViewById(R.id.dialog_checkbox);
        	dialogCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				//@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					// TODO Auto-generated method stub
					showWelcomeDialog = arg1;
				}
        		
        	});
        	dialogCheckBox.setChecked(showWelcomeDialog);
        	
        	AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        	
        	alt_bld.setIcon(R.drawable.ic_launcher);
        	alt_bld.setTitle(R.string.dialog_welcome_title);
        	alt_bld.setView(layout);

        	alt_bld.setCancelable(false)
        	.setPositiveButton(R.string.dialog_ok_text, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        	// Action for 'ok' Button

        	}
        	});
        	
        	AlertDialog alert = alt_bld.create();
        	alert.show();
        }
        
        Spinner GravSpinner = (Spinner) findViewById(R.id.gravity_spinner);
        Spinner SizeSpinner = (Spinner) findViewById(R.id.frame_size_spinner);

        
        //sets values for the gravity spinner
        //easy because they're strings as a resource
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.gravity_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        GravSpinner.setAdapter(adapter);
        
        
        //sets values for the preview size spinner
        // harder, because the values have to be 
        // loaded from the camera.
        try
        {
        	Camera mCamera = CameraViewOverlay.getCameraInstance();
        	sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        	mCamera.release();
        	
        	// sizeList needs to be reversed  && processed 
        	//(for better default behavior)
        	Collections.reverse(sizeList);
        	
        	//get size of display to remove huge picture sizes
        	Display display = getWindowManager().getDefaultDisplay();
        	int width;
        	int height;
        	
        	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        	if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
        	    // Do something for ICS and above versions
        		Point size = new Point();
            	display.getSize(size);
            	width = size.x;
            	height = size.y;            	
        		
        	} else{
        	    // do something for phones running an SDK before ICS
        		width = display.getWidth();
            	height = display.getHeight();
        	}


        	       
        	for(int i = 0; i < sizeList.size(); i++)
        	{
        		if(sizeList.get(i).width > width || sizeList.get(i).height > height)
        		{
        			sizeList.remove(i);
        			i--;
        		}
        	}
        	
        	String[] stringArray = new String[sizeList.size()];
        	for(int i = 0; i < sizeList.size(); i++)
        	{
        		String temp = "";
        		temp += sizeList.get(i).width + "x" + sizeList.get(i).height;
            	
        		stringArray[i] = temp;
        	}
        	
        	
        
        	class SizeOnItemSelectedListener implements OnItemSelectedListener {

        		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            	//Toast.makeText(parent.getContext(), parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
            	            	
            		mPreviewHeight = sizeList.get(pos).height;
            		mPreviewWidth = sizeList.get(pos).width;
            		lastSizeSelection = pos;
            	}

            	public void onNothingSelected(AdapterView<?> parent) {
            	// Do nothing.
            	}
        	}
        
        ArrayAdapter<String> adapter2 = new ArrayAdapter <String>(
        		this, android.R.layout.simple_spinner_item, stringArray);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SizeSpinner.setAdapter(adapter2);
        SizeSpinner.setOnItemSelectedListener(new SizeOnItemSelectedListener());
    
        
        
        } 
        catch (Exception e)
        {
    		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        	alt_bld.setMessage(R.string.dialog_error_body)
        	// Title for AlertDialog
        	.setTitle(R.string.dialog_error_title)
        	// Icon for AlertDialog
        	.setIcon(R.drawable.ic_launcher)
        	.setCancelable(false)
        	.setPositiveButton(R.string.dialog_ok_text, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        	// Action for 'ok' Button
        	}
        	});
        	AlertDialog alert = alt_bld.create();
        	alert.show();
        	
        	Log.e(TAG, "Unable to attach to Camera");
        	
        	
        }
        
        class GravityOnItemSelectedListener implements OnItemSelectedListener {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                          	            	
            	if(pos == 0)
            		mGravity = Gravity.TOP | Gravity.LEFT;
            	if (pos == 1)
            		mGravity = Gravity.TOP;
            	if (pos == 2)
            		mGravity = Gravity.TOP | Gravity.RIGHT;           	
            	
            	if (pos == 3)
            		mGravity = Gravity.LEFT;
            	if (pos == 4)
            		mGravity = Gravity.CENTER;
            	if (pos == 5)
            		mGravity = Gravity.RIGHT;
            	
            	if(pos == 6)
            		mGravity = Gravity.BOTTOM | Gravity.LEFT;
            	if (pos == 7)
            		mGravity = Gravity.BOTTOM;
            	if(pos == 8)
            		mGravity = Gravity.BOTTOM | Gravity.RIGHT;    	
            	
            }

            public void onNothingSelected(AdapterView<?> parent) {
              // Do nothing.
            }
        }

        

        GravSpinner.setOnItemSelectedListener(new GravityOnItemSelectedListener());
        
        setTextInSpinners(GravSpinner, SizeSpinner);
        assignValuesToSeekbars();
        setCheckBoxes();
        

    }
    
    private void setupActionBar() {

        	    ActionBar actionBar = getActionBar();

        	    ViewGroup v = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.main_actionbar, null);
        	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        	    actionBar.setCustomView(v,
        	    				new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
        	                    ActionBar.LayoutParams.WRAP_CONTENT,
        	                    Gravity.CENTER_VERTICAL | Gravity.RIGHT));

        	    serviceSwitch = (Switch) v.findViewById(R.id.switch_service);		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.walking_display_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId()) {
    	
    	case R.id.menu_help:
    		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        	alt_bld.setMessage(R.string.dialog_help_body)
        	// Title for AlertDialog
        	.setTitle(R.string.dialog_help_title)
        	// Icon for AlertDialog
        	.setIcon(R.drawable.ic_launcher)
        	.setCancelable(false)
        	.setPositiveButton(R.string.dialog_ok_text, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        	// Action for 'ok' Button
        	}
        	});
        	AlertDialog alert = alt_bld.create();
        	alert.show();
    		return true;
    	
    	
    	default:
		return super.onOptionsItemSelected(item);	
    	}
    }
    
    private void setCheckBoxes() {
    	
    	CheckBox boxTransparency = (CheckBox) findViewById(R.id.checkBoxTransparency);
    	CheckBox boxPersistance = (CheckBox) findViewById(R.id.checkBoxPersistence);
    	
    	boxTransparency.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			//@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				transparency = isChecked;
				barTrans.setEnabled(transparency);
				barComp.setEnabled(transparency);					
			}    		
    	
    	});
    	
    	boxPersistance.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			//@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				persistance = isChecked;
			}
    		
    	});
    	
    	boxTransparency.setChecked(transparency);
    	boxPersistance.setChecked(persistance);
    	
    	barTrans.setEnabled(transparency);
		barComp.setEnabled(transparency);

		
	}

	private void setTextInSpinners(Spinner g, Spinner s)
    {
    	/*
    	// gravity
    	*/
    	if (mGravity == 51)
    		g.setSelection(0);//top left    	
    	if (mGravity == 48)    		
    		g.setSelection(1);//top
    	if (mGravity == 53)
    		g.setSelection(2);//top right
    	
    	if (mGravity == 3)
    		g.setSelection(3);//left
    	if (mGravity == 17)
    		g.setSelection(4);//center
    	if (mGravity == 5)   		
    		g.setSelection(5);//right 
    	
    	if (mGravity == 83)
    		g.setSelection(6);// bottom left
    	if (mGravity == 80)	
    		g.setSelection(7);//bottom
    	if (mGravity == 85)
    		g.setSelection(8);//bottom right
    	
    	/*
    	// sizes
    	*/
    	s.setSelection(lastSizeSelection);
    	
    	
    }
    
    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
    	
    	savedInstanceState.putInt("xOffset", xOffset);
    	savedInstanceState.putInt("yOffset", yOffset);
    	savedInstanceState.putInt("mGravity", mGravity);


        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.

      xOffset = savedInstanceState.getInt("xOffset");
      yOffset = savedInstanceState.getInt("yOffset");
      mGravity = savedInstanceState.getInt("mGravity");
      
      
    } */
    
    public void onStart()
    {
    	super.onStart();
    }
        
    public void onPause()
    {
    	saveSharedPrefs();
    	super.onPause();    	
    }
    
    private void restoreSharedPrefs()
    {
    	SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
    	
    	xOffset = prefs.getInt("xOffset", 0);
    	yOffset = prefs.getInt("yOffset", 0);
    	mGravity = prefs.getInt("mGravity", 48);
    	mPreviewHeight = prefs.getInt("mPreviewHeight", 300);
    	mPreviewWidth = prefs.getInt("mPreviewWidth", 300);
    	transparency = prefs.getBoolean("transparency", true);
    	persistance = prefs.getBoolean("persistance", false);
    	lastSizeSelection = prefs.getInt("lastSizeSelection", 0);
    	compression = prefs.getInt("compression", 10);
    	transLevel = prefs.getFloat("transLevel", 70f);
    	showWelcomeDialog = prefs.getBoolean("showWelcomeDialog", true);
    	
    }
    
    private void saveSharedPrefs()
    {
    	SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putInt("xOffset", xOffset);
        editor.putInt("yOffset", yOffset);
        editor.putInt("mGravity", mGravity);
        editor.putInt("mPreviewHeight", mPreviewHeight);
        editor.putInt("mPreviewWidth", mPreviewWidth);
        editor.putBoolean("transparency", transparency);
        editor.putBoolean("persistance", persistance);
        editor.putInt("lastSizeSelection", lastSizeSelection);
        editor.putInt("compression", compression);
        editor.putFloat("transLevel", transLevel);
        editor.putBoolean("showWelcomeDialog", showWelcomeDialog);
                
        editor.commit();
    	
    }
    
    public void startService()
    {    	
    	saveSharedPrefs();
    	
    	Intent intent = new Intent(getApplicationContext(), CameraOverlayService.class);
    	//intent.putExtra("screen_off", false);
    	startService(intent);
    }
    
    public void assignValuesToSeekbars()
    {
    	SeekBar barX = (SeekBar) this.findViewById(R.id.seekBarX);
    	SeekBar barY = (SeekBar) this.findViewById(R.id.seekBarY);
    	barComp = (SeekBar) this.findViewById(R.id.seekBarCompression);
    	barTrans = (SeekBar) this.findViewById(R.id.seekBarTransparency);

    	
    	final TextView text_xOffset = (TextView) this.findViewById(R.id.text_view_xOffset);
    	final TextView text_yOffset = (TextView) this.findViewById(R.id.text_view_yOffset);
    	final TextView text_compression = (TextView) this.findViewById(R.id.text_view_compression);
    	final TextView text_transLevel = (TextView) this.findViewById(R.id.text_view_trans_level);
    	
    	barX.setMax(500);
    	barY.setMax(500);
    	barComp.setMax(100);
    	barTrans.setMax(100);
    	
    	barX.setProgress(xOffset);
    	barY.setProgress(yOffset);
    	barComp.setProgress(compression);
    	barTrans.setProgress((int) transLevel);
    	
    	text_xOffset.setText(R.string.xOffset);
    	text_xOffset.append(": " + xOffset + " " + getString(R.string.pixels));
    	
    	text_yOffset.setText(R.string.yOffset);
    	text_yOffset.append(": " + yOffset + " " + getString(R.string.pixels));
    	
    	text_compression.setText(R.string.compression);
    	text_compression.append(": " + compression + "%");
    	
    	text_transLevel.setText(R.string.trans_level);
    	text_transLevel.append(": " + transLevel + "%");

    	barX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

    	    //@Override
    	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    	        xOffset = progress;
    	    	text_xOffset.setText(R.string.xOffset);
    	    	text_xOffset.append(": " + xOffset + " " + getString(R.string.pixels));
    	    }

    	    //@Override
    	    public void onStartTrackingTouch(SeekBar seekBar) {

    	    }

    	   // @Override
    	    public void onStopTrackingTouch(SeekBar seekBar) {

    	    }
    	});
    	
    	barY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

    	    //@Override
    	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    	        yOffset = progress;
    	    	text_yOffset.setText(R.string.yOffset);
    	    	text_yOffset.append(": " + yOffset + " " + getString(R.string.pixels));
    	    }

    	   // @Override
    	    public void onStartTrackingTouch(SeekBar seekBar) {

    	    }

    	    //@Override
    	    public void onStopTrackingTouch(SeekBar seekBar) {

    	    }
    	});
    	
    	barComp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

    	    //@Override
    	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    	        compression = progress;
    	    	text_compression.setText(R.string.compression);
    	    	text_compression.append(": " + compression + "%");
    	    }

    	    //@Override
    	    public void onStartTrackingTouch(SeekBar seekBar) {

    	    }

    	    //@Override
    	    public void onStopTrackingTouch(SeekBar seekBar) {

    	    }
    	});
    	
    	barTrans.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

    	    //@Override
    	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    	        transLevel = progress;
    	    	text_transLevel.setText(R.string.trans_level);
    	    	text_transLevel.append(": " + transLevel + "%");
    	    }

    	    //@Override
    	    public void onStartTrackingTouch(SeekBar seekBar) {

    	    }

    	    //@Override
    	    public void onStopTrackingTouch(SeekBar seekBar) {

    	    }
    	});
    }

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		serviceSwitch.setChecked(isChecked);
		startService();
	}
}

