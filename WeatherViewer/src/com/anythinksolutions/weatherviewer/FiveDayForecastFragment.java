package com.anythinksolutions.weatherviewer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
	public String getZipCode() {
		return zipCode;
	}

}
