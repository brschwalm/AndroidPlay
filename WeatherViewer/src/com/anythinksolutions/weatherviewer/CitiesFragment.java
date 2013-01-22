package com.anythinksolutions.weatherviewer;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CitiesFragment extends ListFragment {

	private static final String CURRENT_CITY_KEY = "current_city";

	private int currentCityIndex;
	
	public ArrayList<String> citiesArrayList;
	private CitiesListChangeListener citiesListChangeListener;
	private ArrayAdapter<String> citiesArrayAdapter;
	
	public interface CitiesListChangeListener{
		public void onSelectedCityChanged(String cityNameString);
		public void onPreferredCityChanged(String cityNameString);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(savedInstanceState != null){
			currentCityIndex = savedInstanceState.getInt(CURRENT_CITY_KEY);
		}
		
		citiesArrayList = new ArrayList<String>();
		setListAdapter(new CitiesArrayAdapter<String>(getActivity(), R.layout.city_list_item, citiesArrayList));
		
		ListView thisListView = getListView();
		citiesArrayAdapter = (ArrayAdapter<String>)getListAdapter();
		
		//Allow only 1 city to be selected at a time
		thisListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		thisListView.setBackgroundColor(Color.WHITE);
		thisListView.setOnItemLongClickListener(citiesLongClickListener);		
	}
	
	public void setCititesListChangeListener(CitiesListChangeListener listener){
		citiesListChangeListener = listener;
	}
	
	private class CitiesArrayAdapter<T> extends ArrayAdapter<String>{
		private Context context;
		
		public CitiesArrayAdapter(Context context, int textViewResourceId, List<String> objects){
			super(context, textViewResourceId, objects);
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			TextView listItemTextView = (TextView)super.getView(position, convertView, parent);
			
			if(isPreferredCity(listItemTextView.getText().toString())){
				listItemTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.btn_star, 0);
			}
			else{
				listItemTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
			
			return listItemTextView;
		}
		
		private boolean isPreferredCity(String city){
			SharedPreferences prefs = context.getSharedPreferences(WeatherViewerActivity.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
			return city.equals(prefs.getString(WeatherViewerActivity.PREFERRED_CITY_NAME_KEY, null));
		}
		
	};

	private OnItemLongClickListener citiesLongClickListener = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(android.widget.AdapterView<?> listView, View view, int arg2, long arg3) {
			final Context context = view.getContext();
			final Resources resources = context.getResources();
			final String cityNameString = ((TextView)view).getText().toString();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(resources.getString(R.string.city_dialog_message_prefix) + cityNameString + resources.getString(R.string.city_dialog_message_postfix));
			builder.setPositiveButton(resources.getString(R.string.city_dialog_preferred),
					new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							citiesListChangeListener.onPreferredCityChanged(cityNameString);
							citiesArrayAdapter.notifyDataSetChanged();							
						}
					});
			builder.setNeutralButton(resources.getString(R.string.city_dialog_delete),
					new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(citiesArrayAdapter.getCount() == 1){
								Toast lastCityToast = Toast.makeText(context,  resources.getString(R.string.last_city_warning), Toast.LENGTH_LONG);
								lastCityToast.setGravity(Gravity.CENTER, 0, 0);
								lastCityToast.show();
								return;
							}
							
							citiesArrayAdapter.remove(cityNameString);
							
							SharedPreferences p = context.getSharedPreferences(WeatherViewerActivity.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
							Editor pEdit = p.edit();
							pEdit.remove(cityNameString);
							pEdit.apply();
							
							String preferredCityString = p.getString(WeatherViewerActivity.PREFERRED_CITY_NAME_KEY, resources.getString(R.string.default_zip));
							if(cityNameString.equals(preferredCityString)){
								citiesListChangeListener.onPreferredCityChanged(citiesArrayList.get(0));
							}
							else if(cityNameString.equals(citiesArrayList.get(currentCityIndex))){
								citiesListChangeListener.onSelectedCityChanged(preferredCityString);
							}							
						}
					});
			builder.setNegativeButton(resources.getString(R.string.city_dialog_cancel),
					new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					
			});
			
			builder.create().show();
			return true;
		};
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_CITY_KEY,  currentCityIndex);
	};
}
