package com.anythinksolutions.weatherviewer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ReadLocationTask extends AsyncTask<Object, Object, String> {

	private static final String TAG = "ReadLocationTask.java";

	private String zip;
	private Context context;
	private Resources resources;
	
	private String city;
	private String state;
	private String country;
	
	private LocationLoadedListener listener;
	
	//Interface for the object to receive notification when this task is complete
	public interface LocationLoadedListener{
		public void onLocationLoaded(String city, String state, String country);
	}
	
	//Constructor
	public ReadLocationTask(String zip, Context context, LocationLoadedListener listener){
		this.zip = zip;
		this.context = context;
		this.resources = context.getResources();
		this.listener = listener;
	}
	
	@Override
	protected String doInBackground(Object... params) {
		try{
			URL url = new URL(resources.getString(R.string.location_url_pre_zipcode) + zip + "&api_key=" + R.string.weatherbug_api_key);
			Reader reader = new InputStreamReader(url.openStream());
			JsonReader jReader = new JsonReader(reader);
			jReader.beginObject();
			
			//get next name
			String name = jReader.nextName();
			if(name.equals(resources.getString(R.string.location))){
				jReader.beginObject();
				String nextName;
				while(jReader.hasNext()){
					nextName = jReader.nextName();
					
					if(nextName.equals(resources.getString(R.string.city)))
						this.city = jReader.nextString();
					else if(nextName.equals(resources.getString(R.string.state)))
						this.state = jReader.nextString();
					else if(nextName.equals(resources.getString(R.string.country)))
						this.country = jReader.nextString();
					else
						jReader.skipValue();						
					}
				}
			
			jReader.close();
		}
		catch(MalformedURLException e){
			Log.v(TAG, e.toString());
		}
		catch(IOException e){
			Log.v(TAG, e.toString());
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if(city != null)
			listener.onLocationLoaded(city, state, country);
		else{
			Toast error = Toast.makeText(context, resources.getString(R.string.invalid_zip_error), Toast.LENGTH_LONG);
			error.setGravity(Gravity.CENTER, 0, 0);
			error.show();
		}
	}

}
