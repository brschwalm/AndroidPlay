package com.anythinksolutions.weatherviewer;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anythinksolutions.weatherviewer.ReadFiveDayForecastTask.FiveDayListener;
import com.anythinksolutions.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class FiveDayForecastFragment extends ForecastFragment {

	private static final String ZIP_CODE_KEY = "id_key";
	private static final int NUMBER_DAILY_FORECASTS = 5;
	
	private String zipCode;
	private View[] dayViews;
	private TextView locationTextView;
	
	public static FiveDayForecastFragment newInstance(String zip){
		
		FiveDayForecastFragment frag = new FiveDayForecastFragment();
		Bundle args = new Bundle();
		args.putString(ZIP_CODE_KEY, zip);
		frag.setArguments(args);
		return frag;
	}
	
	public static FiveDayForecastFragment newInstance(Bundle args){
		
		String zip = args.getString(ZIP_CODE_KEY);
		return newInstance(zip);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.zipCode = savedInstanceState.getString(ZIP_CODE_KEY);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		
		View rootView = inflater.inflate(R.layout.five_day_forecast_layout, null);
		locationTextView = (TextView)rootView.findViewById(R.id.location);
		LinearLayout containerLl = (LinearLayout)rootView.findViewById(R.id.containerLinearLayout);
		
		int id;
		
		if(container.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			id = R.layout.single_forecast_layout_landscape;
		else
		{
			id = R.layout.single_forecast_layout_portrait;
			containerLl.setOrientation(LinearLayout.VERTICAL);
		}
		
		View forecastView;
		for(int i = 0; i < NUMBER_DAILY_FORECASTS; i++){
			forecastView = inflater.inflate(id, null);
			containerLl.addView(forecastView);
			dayViews[i] = forecastView;
		}
		
		new ReadLocationTask(zipCode, rootView.getContext(), new WeatherLocationLoadedListener(zipCode, rootView.getContext())).execute();
		return rootView;
	}
	
	@Override
	public String getZipCode() {
		return zipCode;
	}

	private class WeatherLocationLoadedListener implements LocationLoadedListener{
		private String zipCode;
		private Context context;
		
		public WeatherLocationLoadedListener(String zip, Context ctx){
			this.zipCode = zip;
			this.context = ctx;
		}
		
		@Override
		public void onLocationLoaded(String city, String state, String country) {
			if(city == null){
				Utils.showToast(context, R.string.null_data_toast);
				return;
			}
			
			locationTextView.setText(city + " " + state + ", " + zipCode + " " + country);
			
			//Now kick off the forecast task
			new ReadFiveDayForecastTask(this.zipCode, weatherForecastListener, locationTextView.getContext()).execute();
		}
	}
	
	FiveDayListener weatherForecastListener = new FiveDayListener(){

		@Override
		public void onForecastLoaded(DailyForecast[] forecasts) {
			// TODO Auto-generated method stub
			
		}
	
	};
}
