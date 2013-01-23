package com.anythinksolutions.weatherviewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anythinksolutions.weatherviewer.ReadForecastTask.ForecastListener;
import com.anythinksolutions.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class SingleForecastFragment extends ForecastFragment {

	private String zipCode;
	
	private static final String LOCATION_KEY = "location";
	private static final String TEMPERATURE_KEY = "temperature";
	private static final String FEELS_LIKE_KEY = "feels_like";
	private static final String HUMIDITY_KEY = "humidity";
	private static final String PRECIPITATION_KEY = "chance_precipitation";
	private static final String IMAGE_KEY = "image";
	
	private static final String ZIP_CODE_KEY = "id_key";
	
	private View forecastView;
	private TextView tempTextView;
	private TextView feelsTextView;
	private TextView humidTextView;
	private TextView locationTextView;
	private TextView precipTextView;
	private ImageView conditionsImageView;
	private TextView loadingTextView;
	private Context context;
	private Bitmap conditionBitmap;
	
	public static SingleForecastFragment newInstance(String zip){
		
		SingleForecastFragment frag = new SingleForecastFragment();
		Bundle args = new Bundle();
		args.putString(ZIP_CODE_KEY, zip);
		frag.setArguments(args);
		return frag;
	}
	
	public static SingleForecastFragment newInstance(Bundle args){
		String zipCode = args.getString(ZIP_CODE_KEY);
		return newInstance(zipCode);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.zipCode = savedInstanceState.getString(ZIP_CODE_KEY);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		outState.putString(LOCATION_KEY, Utils.getText(locationTextView));
		outState.putString(TEMPERATURE_KEY, Utils.getText(tempTextView));
		outState.putString(FEELS_LIKE_KEY, Utils.getText(feelsTextView));
		outState.putString(HUMIDITY_KEY, Utils.getText(humidTextView));
		outState.putString(PRECIPITATION_KEY, Utils.getText(precipTextView));
		outState.putParcelable(IMAGE_KEY, conditionBitmap);
	}
	
	@Override
	public String getZipCode() {
		return this.zipCode;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {

		View rootView = inflater.inflate(R.layout.forecast_fragment_layout, null);
		
		forecastView = rootView.findViewById(R.id.forecast_layout);
		loadingTextView = (TextView)rootView.findViewById(R.id.loading_message);
		locationTextView = (TextView)rootView.findViewById(R.id.location);
		tempTextView = (TextView)rootView.findViewById(R.id.temp);
		feelsTextView = (TextView)rootView.findViewById(R.id.feels);
		humidTextView = (TextView)rootView.findViewById(R.id.humid);
		precipTextView = (TextView)rootView.findViewById(R.id.precip);
		conditionsImageView = (ImageView)rootView.findViewById(R.id.forecast_image);
		
		context = rootView.getContext();
		return rootView;	
	}
	
	@Override
	public void onActivityCreated(Bundle args) {

		super.onActivityCreated(args);
		
		if(args == null){
			forecastView.setVisibility(View.GONE);
			loadingTextView.setVisibility(View.VISIBLE);
			
			new ReadLocationTask(zipCode, context, new WeatherLocationLoadedListener(zipCode)).execute();
		}
		else{
			conditionsImageView.setImageBitmap((Bitmap)args.getParcelable(IMAGE_KEY));
			locationTextView.setText(args.getString(LOCATION_KEY));
			tempTextView.setText(args.getString(TEMPERATURE_KEY));
			feelsTextView.setText(args.getString(FEELS_LIKE_KEY));
			humidTextView.setText(args.getString(HUMIDITY_KEY));
			precipTextView.setText(args.getString(PRECIPITATION_KEY));
		}
	}

	ForecastListener weatherForecastListener = new ForecastListener(){
		@Override
		public void onForecastLoaded(Bitmap img, String temp, String feels, String humid, String precip){
			if(!SingleForecastFragment.this.isAdded())
				return;		//means the user navigated away from this view.
			else if(img == null){
				Toast error = Toast.makeText(context, context.getResources().getString(R.string.null_data_toast), Toast.LENGTH_LONG);
				error.setGravity(Gravity.CENTER, 0, 0);
				error.show();
				return;
			}
			
			Resources res = SingleForecastFragment.this.getResources();
			
			conditionsImageView.setImageBitmap(img);
			tempTextView.setText(temp + (char)0x00B0 + res.getString(R.string.temp_unit));
			feelsTextView.setText(feels + (char)0x00B0 + res.getString(R.string.temp_unit));
			humidTextView.setText(humid + (char)0x0025);
			precipTextView.setText(precip + (char)0x0025);
			
			loadingTextView.setVisibility(View.GONE);
			forecastView.setVisibility(View.VISIBLE);			
		}
		
	};

	private class WeatherLocationLoadedListener implements LocationLoadedListener{

		private String zipCode;
		
		public WeatherLocationLoadedListener(String zip){
			this.zipCode = zip;
		}
		
		@Override
		public void onLocationLoaded(String city, String state, String country) {
			if(city == null){
				Utils.showToast(context, R.string.null_data_toast);
			}
			
			locationTextView.setText(city + " " + state + ", " + zipCode + " " + country);
			new ReadForecastTask(zipCode, weatherForecastListener, locationTextView.getContext()).execute();
			
		}
		
	}
}
