package com.anythinksolutions.addressbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewContact extends Activity {

	private long rowId;
	private TextView nameTextView;
	private TextView phoneTextView;
	private TextView emailTextView;
	private TextView streetTextView;
	private TextView cityTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_contact);

		nameTextView = (TextView) findViewById(R.id.nameTextView);
		phoneTextView = (TextView) findViewById(R.id.phoneTextView);
		emailTextView = (TextView) findViewById(R.id.emailTextView);
		streetTextView = (TextView) findViewById(R.id.streetTextView);
		cityTextView = (TextView) findViewById(R.id.cityTextView);

		Bundle extras = getIntent().getExtras();
		rowId = extras.getLong(Constants.ROW_ID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new LoadContactTask().execute(rowId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.view_contact, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()){
		
		case R.id.editItem:
			Intent editIntent = new Intent(this, AddEditContact.class);
			
			editIntent.putExtra(Constants.ROW_ID, rowId);
			editIntent.putExtra(Constants.NAME_ID, nameTextView.getText().toString());
			editIntent.putExtra(Constants.PHONE_ID, phoneTextView.getText().toString());
			editIntent.putExtra(Constants.EMAIL_ID, emailTextView.getText().toString());
			editIntent.putExtra(Constants.STREET_ID, streetTextView.getText().toString());
			editIntent.putExtra(Constants.CITY_ID, cityTextView.getText().toString());
		
			startActivity(editIntent);
			return true;
			
		case R.id.deleteItem:
			deleteContact();
			return true;
			
		default:
			return super.onMenuItemSelected(featureId, item);
		}		
	}
	
	private void deleteContact(){
		AlertDialog.Builder builder = new AlertDialog.Builder(ViewContact.this);
		builder.setTitle(R.string.confirm_title);
		builder.setMessage(R.string.confirm_message);
		
		builder.setPositiveButton(R.string.button_delete,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final DatabaseConnector dbCon = new DatabaseConnector(ViewContact.this);
						
						//Delete the contact in a separate thread
						AsyncTask<Long, Object, Object> deleteTask = 
								new AsyncTask<Long, Object, Object>(){
							@Override
							protected Object doInBackground(Long... params){
								dbCon.deleteContact(params[0]);
								return null;
							}
							
							@Override
							protected void onPostExecute(Object result) {
								finish();	//returns to address book activity
							}
						};
						
						deleteTask.execute(new Long[] { rowId });
					}
				});
		
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.show();
	}

	private class LoadContactTask extends AsyncTask<Long, Object, Cursor> {
		DatabaseConnector dbCon = new DatabaseConnector(ViewContact.this);

		@Override
		protected Cursor doInBackground(Long... params) {
			dbCon.open();
			return dbCon.getOneContact(params[0]);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			result.moveToFirst();

			int nameIndex = result.getColumnIndex(Constants.NAME_ID);
			int phoneIndex = result.getColumnIndex(Constants.PHONE_ID);
			int emailIndex = result.getColumnIndex(Constants.EMAIL_ID);
			int streetIndex = result.getColumnIndex(Constants.STREET_ID);
			int cityIndex = result.getColumnIndex(Constants.CITY_ID);

			nameTextView.setText(result.getString(nameIndex));
			phoneTextView.setText(result.getString(phoneIndex));
			emailTextView.setText(result.getString(emailIndex));
			streetTextView.setText(result.getString(streetIndex));
			cityTextView.setText(result.getString(cityIndex));

			result.close();
			dbCon.close();
		}
	}
}
