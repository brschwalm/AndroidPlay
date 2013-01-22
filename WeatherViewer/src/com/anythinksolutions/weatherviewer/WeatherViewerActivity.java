package com.anythinksolutions.weatherviewer;

import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

import com.anythinksolutions.weatherviewer.AddCityDialogFragment.DialogFinishedListener;

public class WeatherViewerActivity extends Activity implements DialogFinishedListener{

	public static final int BROADCAST_DELAY = 10000;
	private static final int CURRENT_CONDITIONS_TAB = 0;
	public static final String PREFERRED_CITY_NAME_KEY = "preferred_city_name";
	public static final String PREFERRED_CITY_ZIPCODE_KEY = "preferred_city_zip";
	public static final String SHARED_PREFERENCES_KEY = "weather_viewer_shared_preferences";
	private static final String CURRENT_TAB_KEY = "current_tab";
	private static final String LAST_SELECTED_KEY = "last_selected";
	
	private int currentTab;
	private String lastSelectedCity;
	private SharedPreferences weatherSharedPreferences;
	
	private Map<String, String> favoriteCitiesMap;
	private CitiesFragment listCitiesFragment;
	private Handler weatherHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_viewer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_weather_viewer, menu);
		return true;
	}

	@Override
	public void onDialogFinished(String zipCodeString, boolean preferred) {
		// TODO Auto-generated method stub
		
	}

}
