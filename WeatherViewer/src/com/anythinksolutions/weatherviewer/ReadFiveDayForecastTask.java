package com.anythinksolutions.weatherviewer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

public class ReadFiveDayForecastTask extends AsyncTask<Object, Object, String> {
	
	private static final String TAG = "ReadFiveDayForecastTask";		//Used with logging
	
	private String zipCode;
	private FiveDayListener forecastListener;
	private Resources resources;
	private DailyForecast[] forecasts;
	private static final int NUMBER_OF_DAYS = 5;
	
	public interface FiveDayListener{
		public void onForecastLoaded(DailyForecast[] forecasts);
	}
	
	
	public ReadFiveDayForecastTask(String zip, FiveDayListener listener, Context context){
		this.zipCode = zip;
		this.forecastListener = listener;
		this.resources = context.getResources();
		this.forecasts = new DailyForecast[NUMBER_OF_DAYS];
	}
	
	@Override
	protected String doInBackground(Object... params) {
	
		try{
			URL url = new URL("[weather url]");
			Reader r = new InputStreamReader(url.openStream());
			JsonReader jsR = new JsonReader(r);
			
			String name = jsR.nextName();
			if(name.equals(resources.getString(R.string.js_current))){
				jsR.beginArray();
				jsR.skipValue();	//skip current conditions
				jsR.skipValue();	//skip request 
				jsR.beginArray();	//Start the "weather" array
				
				//read next 5 days
				for(int i = 0; i < NUMBER_OF_DAYS; i++){
					
					jsR.beginObject();
					if(jsR.hasNext()){
						forecasts[i] = readDayForecast(jsR);
					}
				}
				
				jsR.endArray();
				jsR.endArray();
			}
			
			jsR.close();			
		}
		catch(MalformedURLException e){
			Log.v(TAG, e.toString());
		}
		catch(IOException e){
			Log.v(TAG, e.toString());
		}
		
		return null;
	}
	
	private DailyForecast readDayForecast(JsonReader reader){
		
		String[] forecast = new String[4];
		Bitmap icon = null;
		
		final String DATE_FIELD = this.resources.getString(R.string.js_date);
		final String HIGH_TEMP_FIELD = this.resources.getString(R.string.js_high_temp);
		final String LOW_TEMP_FIELD = this.resources.getString(R.string.js_low_temp);
		final String DESC_FIELD = this.resources.getString(R.string.js_desc);
		final String VALUE_FIELD = this.resources.getString(R.string.js_value);
		final String ICON_FIELD = this.resources.getString(R.string.js_image);
		
		try{
			while(reader.hasNext()){
				String name = reader.nextName();
				
				if(name.equals(DATE_FIELD))
					forecast[DailyForecast.DAY_INDEX] = reader.nextString();
				else if(name.equals(HIGH_TEMP_FIELD))
					forecast[DailyForecast.HIGH_TEMP_INDEX] = reader.nextString();
				else if(name.equals(LOW_TEMP_FIELD))
					forecast[DailyForecast.LOW_TEMP_INDEX] = reader.nextString();
				else if(name.equals(DESC_FIELD)){
					reader.beginArray();
					reader.beginObject();
					name = reader.nextName();
					if(name.equals(VALUE_FIELD))
						forecast[DailyForecast.PREDICTION_INDEX] = reader.nextString();
					reader.endObject();
					reader.endArray();
				}
				else if(name.equals(ICON_FIELD)){
					reader.beginArray();
					reader.beginObject();
					name = reader.nextName();
					if(name.equals(VALUE_FIELD))
						icon = ReadForecastTask.getImage(reader.nextString(), this.resources, 0);
					reader.endObject();
					reader.endArray();
				}
				else
					reader.skipValue();
			}
			
			reader.endObject();
		}
		catch(IOException e){
			Log.v(TAG, e.toString());
		}
		
		return new DailyForecast(forecast, icon);
	}
}
