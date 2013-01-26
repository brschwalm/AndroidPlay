package com.anythinksolutions.weatherviewer;

import android.graphics.Bitmap;

public class DailyForecast {
	
	public static final int DAY_INDEX = 0;
	public static final int PREDICTION_INDEX = 1;
	public static final int HIGH_TEMP_INDEX = 2;
	public static final int LOW_TEMP_INDEX = 3;
	
	final private String[] forecast;
	final private Bitmap icon;
	
	public DailyForecast(String[] forecast, Bitmap icon){
		this.forecast = forecast;
		this.icon = icon;
	}
	
	public Bitmap getIconBitmap(){
		return icon;
	}
	
	public String getDay(){
		return forecast[DAY_INDEX];
	}
	
	public String getDescription(){
		return forecast[PREDICTION_INDEX];
	}
	
	public String getHighTemp(){
		return forecast[HIGH_TEMP_INDEX];
	}
	
	public String getLowTemp(){
		return forecast[LOW_TEMP_INDEX];
	}
}
