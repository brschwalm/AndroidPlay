package com.anythinksolutions.weatherviewer;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.anythinksolutions.weatherviewer.ReadForecastTask.ForecastListener;
import com.anythinksolutions.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class WeatherProvider extends AppWidgetProvider {

	private static final int BITMAP_SAMPLE_SIZE = 4;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		startUpdateService(context);
	}
	
	private String getZipCode(Context context){
		SharedPreferences prefs = context.getSharedPreferences(WeatherViewerActivity.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
		String zip = prefs.getString(WeatherViewerActivity.PREFERRED_CITY_ZIPCODE_KEY, context.getResources().getString(R.string.default_zip));
		return zip;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(WeatherViewerActivity.UPDATE_WIDGET_BROADCAST))
			startUpdateService(context);
		
		super.onReceive(context, intent);
	}
	
	private void startUpdateService(Context context){
		Intent startSvc = new Intent(context, WeatherService.class);
		startSvc.putExtra(context.getResources().getString(R.string.zip_extra), getZipCode(context));
		context.startService(startSvc);
	}

	public static class WeatherService extends IntentService implements ForecastListener{
		
		public WeatherService(){
			super(WeatherService.class.toString());
		}
		
		private Resources resources;
		private String zipCode;
		private String location;
		
		@Override
		protected void onHandleIntent(Intent intent) {
			resources = getApplicationContext().getResources();
			zipCode = intent.getStringExtra(resources.getString(R.string.zip_extra));
			new ReadLocationTask(zipCode, this, new LocationListener(zipCode)).execute();
		}
	
		private class LocationListener implements LocationLoadedListener {

			private String zipCode;
			
			public LocationListener(String zip){
				this.zipCode = zip;
			}
			
			@Override
			public void onLocationLoaded(String city, String state, String country) {
				Context ctx = getApplicationContext();
				if(city == null){
					Utils.showToast(ctx, R.string.null_data_toast);
					return;
				}
				
				location = city + " " + state + ", " + zipCode + " " + country;
				ReadForecastTask rfTask = new ReadForecastTask(zipCode, (ForecastListener)WeatherService.this, WeatherService.this);
				rfTask.setSampleSize(BITMAP_SAMPLE_SIZE);
				rfTask.execute();
				
			}
		
		}

		@Override
		public void onForecastLoaded(Bitmap image, String temp, String feels, String humid, String precip) {
			Context ctx = getApplicationContext();
			
			if(image == null){
				Utils.showToast(ctx, R.string.null_data_toast);
				return;
			}
			
			//Create the Intent and the Pending Intent to wait for the user to tap the widget
			Intent i = new Intent(ctx, WeatherViewerActivity.class);
			PendingIntent pI = PendingIntent.getActivity(getBaseContext(), 0, i, 0);
			
			//Use a RemoveViews to update the widget view since it is in a different process
			RemoteViews rv = new RemoteViews(getPackageName(), R.layout.weather_app_widget_layout);
			rv.setTextViewText(R.id.location, location);
			rv.setTextViewText(R.id.temperatureTextView, temp + (char)0x00B0 + resources.getString(R.string.temp_unit));
			rv.setTextViewText(R.id.feelsLikeTextView, feels + (char)0x00B0 + resources.getString(R.string.temp_unit));
			rv.setTextViewText(R.id.humidityTextView, humid + (char)0x0025);
			rv.setTextViewText(R.id.precipChanceTextView, precip + (char)0x0025);
			rv.setImageViewBitmap(R.id.weatherImageView, image);
			
			ComponentName widgetName = new ComponentName(this, WeatherProvider.class);
			AppWidgetManager mgr = AppWidgetManager.getInstance(this);
			mgr.updateAppWidget(widgetName, rv);			
		}
   }
}
