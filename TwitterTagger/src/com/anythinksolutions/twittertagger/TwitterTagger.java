package com.anythinksolutions.twittertagger;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.Menu;

public class TwitterTagger extends Activity {

	private static final String SEARCHES = "searches";
	
	private SharedPreferences savedSearches;
	private TableLayout queryTableLayout;
	private EditText queryEditText;
	private EditText tagEditText;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Get saves searches from SharedPreferences
		savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);
		
		//Get the UI Components
		queryTableLayout = (TableLayout)findViewById(R.id.queryTableLayout);
		queryEditText = (EditText)findViewById(R.id.queryEditText);
		tagEditText = (EditText)findViewById(R.id.tagEditText);
		Button saveButton = (Button)findViewById(R.id.saveButton);
		Button clearTagsButton = (Button)findViewById(R.id.clearTagsButton);
		
		//Register necessary listeners
		saveButton.setOnClickListener(saveListener);
		clearTagsButton.setOnClickListener(clearTagsListener);
		
		refreshButtons(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void refreshButtons(String newTag){
		
		//Get the searches and sort them alphabetically
		String[] tags = savedSearches.getAll().keySet().toArray(new String[0]);
		Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER);
		
		//Check to see if we're adding a new tag or getting all of them
		if(newTag != null){
			makeTagGUI(newTag, Arrays.binarySearch(tags, newTag));
		}
		else{
			//Display all the saved searches
			for(int i = 0; i < tags.length; ++i){
				makeTagGUI(tags[i], i);
			}
			
		}
	}
	
	private void makeTag(String query, String tag){
		String original = savedSearches.getString(tag,  null);
		
		//get a SharedPreferences.Editor to store new tag/query pair
		SharedPreferences.Editor preferencesEditor = savedSearches.edit();
		preferencesEditor.putString(tag,  query);
		preferencesEditor.apply();
		
		//If this is a new query, add it to the GUI
		if(original == null)
			refreshButtons(tag);
	}
	
	private void makeTagGUI(String tag, int index){
		//Get a reference to the LayoutInflater
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View newTagView = inflater.inflate(R.layout.new_tag_view, null);
		
		Button newTagButton = (Button)newTagView.findViewById(R.id.newTagButton);
		Button newEditButton = (Button)newTagView.findViewById(R.id.newEditButton);
		
		newTagButton.setText(tag);
		newTagButton.setOnClickListener(queryListener);
		newEditButton.setOnClickListener(editListener);
		
		//Add the item to the queryTableLayout
		queryTableLayout.addView(newTagView, index);
	}
	
	private void clearButtons(){
		queryTableLayout.removeAllViews();
	}
	
	private OnClickListener saveListener = new OnClickListener(){
		@Override
		public void onClick(View v){
			//If required values are present, make the tag
			if(queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0){
				makeTag(queryEditText.getText().toString(), tagEditText.getText().toString());
				queryEditText.setText("");
				tagEditText.setText("");
				
				//hide the soft keyboard
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
			}
			else{
				//Alert the user they need to input more data
				AlertDialog.Builder builder = new AlertDialog.Builder(TwitterTagger.this);
				builder.setTitle(R.string.missingTitle);
				builder.setMessage(R.string.missingMessage);
				builder.setPositiveButton(R.string.ok, null);
				
				builder.show();
			}
		}
	};
	
	private OnClickListener clearTagsListener = new OnClickListener(){
		@Override
		public void onClick(View v){
			if(savedSearches.getAll().isEmpty())
				return;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(TwitterTagger.this);
			builder.setTitle(R.string.confirmTitle);
			builder.setMessage(R.string.confirmMessage);
			builder.setPositiveButton(R.string.erase,
						new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								clearButtons();
								SharedPreferences.Editor editor = savedSearches.edit();
								editor.clear();
								editor.apply();
							}
						});
			builder.setCancelable(true);
			builder.setNegativeButton(R.string.cancel, null);
			builder.show();
		}
	};
	private OnClickListener queryListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			//Get the query and create the Url
			String buttonText = ((Button)v).getText().toString();
			String query = savedSearches.getString(buttonText, null);
			String url = getString(R.string.search_url) + query;
			
			//Create the Intent and launch the web browser
			Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browseIntent);			
		}	
	};
	private OnClickListener editListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TableRow buttonRow = (TableRow)v.getParent();
			Button searchButton = (Button)buttonRow.findViewById(R.id.newTagButton);
			
			String tag = searchButton.getText().toString();
			tagEditText.setText(tag);
			queryEditText.setText(savedSearches.getString(tag, null));
			
		}
	};
}
