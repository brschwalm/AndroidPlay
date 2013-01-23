package com.anythinksolutions.weatherviewer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

public class ReadForecastTask extends AsyncTask<Object, Object, String> {

	private static final String TAG = "ReadForecastTask.java";
	
	private String zipCode;
	private Resources resources;
	
	private String temp;
	private String feels;
	private String humid;
	private String precip;
	private Bitmap image;
	
	private int bitmapSampleSize = -1;
	
	private ForecastListener weatherListener;
	
	public interface ForecastListener{
		public void onForecastLoaded(Bitmap image, String temp, String feels, String humid, String precip);
	}
	
	public ReadForecastTask(String zip, ForecastListener listener, Context context){
		this.zipCode = zip;
		this.weatherListener = listener;
		this.resources = context.getResources();
	}
	
	public void setSampleSize(int sampleSize){
		this.bitmapSampleSize = sampleSize;
	}
	
	@Override
	protected String doInBackground(Object... args) {
		try{
			URL wsUrl = new URL(resources.getString(R.string.weather_url_prefix) + zipCode + "&format=json&num_of_days=5&key=" + resources.getString(R.string.weatherbug_api_key));
			Reader reader = new InputStreamReader(wsUrl.openStream());
			JsonReader jsReader = new JsonReader(reader);
			jsReader.beginObject();
			
			String name = jsReader.nextName();
			if(name.equals(resources.getString(R.string.js_current)))
				parseForecast(jsReader);
			
			jsReader.close();
		}
		catch(MalformedURLException e){
			Log.v(TAG, e.toString());
		}
		catch(IOException e){
			Log.v(TAG, e.toString());
		}
		catch(IllegalStateException e){
			Log.v(TAG, e.toString() + zipCode);
		}
		
		return null;
	}

	public static Bitmap getImage(String urlString, Resources resources, int bitmapSampleSize){
		
		Bitmap icon = null;
		
		try{
			URL bmpUrl = new URL(urlString);
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			if(bitmapSampleSize != -1)
				opts.inSampleSize = bitmapSampleSize;
			
			icon = BitmapFactory.decodeStream(bmpUrl.openStream(), null, opts);
		}
		catch(MalformedURLException e){
			Log.v(TAG, e.toString());
		}
		catch(IOException e){
			Log.v(TAG, e.toString());
		}
		
		return icon;
	}

	private String parseForecast(JsonReader reader){
		try{
			
			final String TEMP_NAME = resources.getString(R.string.js_temp);
			final String HUMID_NAME = resources.getString(R.string.js_humid);
			//final String FEELS_NAME = resources.getString(R.string.js_feels);
			final String PRECIP_NAME = resources.getString(R.string.js_precip);
			final String IMAGE_NAME = resources.getString(R.string.js_image);
			final String DESC_NAME = resources.getString(R.string.js_desc);
			final String VALUE_NAME = resources.getString(R.string.js_value);
			
			reader.beginArray();
			reader.beginObject();
			
			while(reader.hasNext()){
				String name = reader.nextName();
				
				if(name.equals(TEMP_NAME)){
					this.temp = reader.nextString();
				}
				else if(name.equals(HUMID_NAME)){
					this.humid = reader.nextString();
				}
//				else if(name.equals(FEELS_NAME)){
//					this.feels = reader.nextString();
//				}
				else if(name.equals(PRECIP_NAME)){
					this.precip = reader.nextString();
				}
				else if(name.equals(IMAGE_NAME)){
					reader.beginArray();
					reader.beginObject();
					
					name = reader.nextName();
					if(name.equals(VALUE_NAME)){
						String imgString = reader.nextString();
						this.image = getImage(imgString, resources, -1);
					}
					
					reader.endObject();
					reader.endArray();
				}
				else if(name.equals(DESC_NAME)){
					reader.beginArray();
					reader.beginObject();
					
					name = reader.nextName();
					if(name.equals(VALUE_NAME)){
						this.feels = reader.nextString();						
					}
					
					reader.endObject();
					reader.endArray();
				}
				else
					reader.skipValue();				
			}
		}
		catch(IOException e){
			Log.v(TAG, e.toString());
		}
		
		return null;
	}
}
