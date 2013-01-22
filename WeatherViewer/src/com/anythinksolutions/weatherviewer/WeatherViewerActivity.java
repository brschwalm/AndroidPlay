package com.anythinksolutions.weatherviewer;

import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anythinksolutions.weatherviewer.AddCityDialogFragment.DialogFinishedListener;
import com.anythinksolutions.weatherviewer.CitiesFragment.CitiesListChangeListener;
import com.anythinksolutions.weatherviewer.ReadLocationTask.LocationLoadedListener;

public class WeatherViewerActivity extends Activity implements DialogFinishedListener{

	public static final String UPDATE_WIDGET_BROADCAST = "com.anythinksolutions.weatherviewer.UPDATE_WIDGET";
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
		
		listCitiesFragment = (CitiesFragment)getFragmentManager().findFragmentById(R.id.cities);
		listCitiesFragment.setCititesListChangeListener(citiesChangedListener);
		
		favoriteCitiesMap = new HashMap<String, String>();
		weatherHandler = new Handler();
		weatherSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
		
		setupTabs();	//setup navigation in the action bar
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_TAB_KEY, currentTab);
		outState.putString(LAST_SELECTED_KEY, lastSelectedCity);
		
		super.onSaveInstanceState(outState);		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentTab = savedInstanceState.getInt(CURRENT_TAB_KEY);
		lastSelectedCity = savedInstanceState.getString(LAST_SELECTED_KEY);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(favoriteCitiesMap.isEmpty()){
			loadSavedCities();
		}
		
		if(favoriteCitiesMap.isEmpty()){
			addSampleCities();
		}
		
		getActionBar().selectTab(getActionBar().getTabAt(currentTab));
		loadSelectedForecast();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_weather_viewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.add_city_item){
			showAddCityDialog();
		}
		return true;
	}
	
	private void showAddCityDialog(){
		AddCityDialogFragment frag = new AddCityDialogFragment();
		FragmentTransaction txn = getFragmentManager().beginTransaction();
		frag.show(txn, "");
	}
	
	@Override
	public void onDialogFinished(String zipCodeString, boolean preferred) {
		getCityName(zipCodeString, preferred);
	}
	
	private void getCityName(String zip, boolean preferred){
		if(favoriteCitiesMap.containsValue(zip)){
			Toast error = Toast.makeText(WeatherViewerActivity.this, WeatherViewerActivity.this.getResources().getString(R.string.duplicate_zip_error), Toast.LENGTH_LONG);
			error.setGravity(Gravity.CENTER, 0, 0);
			error.show();
		}
		else{
			new ReadLocationTask(zip, this, new CityNameLocationLoadedListener(zip, preferred)).execute();
		}
	}

	//Internal class to listen for loading of a city and to handle it appropriately
	private class CityNameLocationLoadedListener implements LocationLoadedListener{
		private String zip;
		private boolean preferred;
		
		public CityNameLocationLoadedListener(String zip, boolean preferred){
			this.zip = zip;
			this.preferred = preferred;
		}
		
		@Override
		public void onLocationLoaded(String city, String state, String country) {
			if(city != null){
				addCity(city, zip, !preferred);
				if(preferred)
					setPreferredCity(city);
			}
			else{
				Toast zipToast = Toast.makeText(WeatherViewerActivity.this, WeatherViewerActivity.this.getResources().getString(R.string.invalid_zip_error), Toast.LENGTH_LONG);
				zipToast.setGravity(Gravity.CENTER, 0, 0);
				zipToast.show();
			}
		}
	}
	
	private CitiesListChangeListener citiesChangedListener = new CitiesListChangeListener(){
		@Override
		public void onPreferredCityChanged(String cityNameString) {
			setPreferredCity(cityNameString);
		};
		
		@Override
		public void onSelectedCityChanged(String cityNameString) {
			selectForecast(cityNameString);
		};
	};
	
	private void loadSelectedForecast(){
		String city = lastSelectedCity;
		
		if(city == null)
			city = weatherSharedPreferences.getString(PREFERRED_CITY_NAME_KEY, getResources().getString(R.string.default_zip));

		selectForecast(city);
	}

	public void setPreferredCity(String city){
		//get the zip for the city
		String zip = favoriteCitiesMap.get(city);
		Editor e = weatherSharedPreferences.edit();
		e.putString(PREFERRED_CITY_NAME_KEY, city);
		e.putString(PREFERRED_CITY_ZIPCODE_KEY, zip);
		e.apply();
		
		lastSelectedCity = null;
		loadSelectedForecast();
		
		final Intent updateWidgetIntent = new Intent(UPDATE_WIDGET_BROADCAST);
		weatherHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				sendBroadcast(updateWidgetIntent);
			}
		}, BROADCAST_DELAY);
		
	}

	//Adds a city to the cities list fragment and selects it if applicable.
	// also puts the zip for the city in the shared prefs and adds the city to the favorites map.
	public void addCity(String city, String zip, boolean select){
		favoriteCitiesMap.put(city, zip);
		listCitiesFragment.addCity(city, select);
		Editor e = weatherSharedPreferences.edit();
		e.putString(city, zip);
		e.apply();
	}

	//Changes the forecast fragment to the correct one, showing the forecast
	// for the appropriate city.
	public void selectForecast(String city){
		lastSelectedCity = city;
		String zip = favoriteCitiesMap.get(city);
		if(zip == null) return;
		
		//get current visible forecast fragment
		ForecastFragment frag = (ForecastFragment)getFragmentManager().findFragmentById(R.id.forecast_replacer);
		//Make the change necessary
		if(frag == null ||
		   !(frag.getZipCode().equals(zip) && correctTab(frag))){
			
			//Find the right fragment based on the current tab
			if(currentTab == CURRENT_CONDITIONS_TAB)
				frag = SingleForecastFragment.newInstance(zip);
			else
				frag = FiveDayForecastFragment.newInstance(zip);
			
			//Switch fragments
			FragmentTransaction forecastTrans = getFragmentManager().beginTransaction();
			forecastTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			forecastTrans.replace(R.id.forecast_replacer, frag);
			forecastTrans.commit();
		}
	}
	
	private void loadSavedCities(){
		Map<String, ?> citiesMap = weatherSharedPreferences.getAll();
		for(String cityString : citiesMap.keySet()){
			if(!(cityString.equals(PREFERRED_CITY_NAME_KEY) ||
				 cityString.equals(PREFERRED_CITY_ZIPCODE_KEY)))
				addCity(cityString, (String)citiesMap.get(cityString), false);
		}
	}
	
	private void addSampleCities(){
		String[] sampleCities = getResources().getStringArray(R.array.default_city_names);
		String[] sampleZips = getResources().getStringArray(R.array.default_city_zipcodes);
		
		for(int i = 0; i < sampleCities.length; i++){
			addCity(sampleCities[i], sampleZips[i], false);
		}
		
		setPreferredCity(sampleCities[0]);
	}

	private boolean correctTab(ForecastFragment frag){
		if(currentTab == CURRENT_CONDITIONS_TAB)
			return (frag instanceof SingleForecastFragment);
		else
			return (frag instanceof FiveDayForecastFragment);
	}
	
	private void setupTabs(){
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		Tab tabCC = bar.newTab();		
		tabCC.setText(getResources().getString(R.string.current_conditions));
		tabCC.setTabListener(weatherTabListener);
		bar.addTab(tabCC);
		
		Tab tabFD = bar.newTab();
		tabFD.setText(getResources().getString(R.string.five_day_forecast));
		tabFD.setTabListener(weatherTabListener);
		bar.addTab(tabFD);
		
		currentTab = CURRENT_CONDITIONS_TAB;
	}
	
	//Handles changing of the current tab
	TabListener weatherTabListener = new TabListener() {
		
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
		
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			selectTab(tab.getPosition());
		}
		
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {}
	};
	
	private void selectTab(int tab){
		currentTab = tab;
		loadSelectedForecast();
	}
}
