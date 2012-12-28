package com.watson.WalkingDisplay;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WalkingDisplayAppWidgetProvider extends AppWidgetProvider {
	
	//private  boolean StopServiceRunning;

	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        //boolean serviceIsRunning;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            //serviceIsRunning = CameraOverlayService.isServiceRunning;

            // Create an Intent to launch CameraOverlayService
            Intent intent = new Intent(context, CameraOverlayService.class);
            intent.setAction(CameraOverlayService.WIDGET_PRESSED);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            
            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.walking_display_appwidget);
            views.setOnClickPendingIntent(R.id.start_button, pendingIntent);
            
            
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}

	
	/*public void onEnabled(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, CameraOverlayService.class);
            //intent.putExtra("screen_off", false);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.walking_display_appwidget);
            views.setOnClickPendingIntent(R.id.start_button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}*/


	


}
