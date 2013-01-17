package com.anythinksolutions.addressbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddEditContact extends Activity {
	private long rowId;
	private EditText nameEditText;
	private EditText phoneEditText;
	private EditText emailEditText;
	private EditText streetEditText;
	private EditText cityEditText;
	private boolean isNewContact;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_edit_contact);
		
		nameEditText = (EditText)findViewById(R.id.nameEditText);
		phoneEditText = (EditText)findViewById(R.id.phoneEditText);
		emailEditText = (EditText)findViewById(R.id.emailEditText);
		streetEditText = (EditText)findViewById(R.id.streetEditText);
		cityEditText = (EditText)findViewById(R.id.cityEditText);
		
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			rowId = extras.getLong(Constants.ROW_ID);
			
			String name = extras.getString(Constants.NAME_ID);
			nameEditText.setText(name);
			
			phoneEditText.setText(extras.getString(Constants.PHONE_ID));
			emailEditText.setText(extras.getString(Constants.EMAIL_ID));
			streetEditText.setText(extras.getString(Constants.STREET_ID));
			cityEditText.setText(extras.getString(Constants.CITY_ID));
		}
		else
			isNewContact = true;
		
		Button saveContactButton = (Button)findViewById(R.id.saveContactButton);
		saveContactButton.setOnClickListener(saveListener);
	}
	
	OnClickListener saveListener = new OnClickListener() {
				
		@Override
		public void onClick(View v) {
			if(nameEditText.getText().length() > 0){
				AsyncTask<Object, Object, Object> saveContactTask = new AsyncTask<Object, Object, Object>(){
					@Override
					protected Object doInBackground(Object... params){
						saveContact();
						return null;
					}
					
					@Override
					protected void onPostExecute(Object result){
						finish();
					}
				};
				
				saveContactTask.execute((Object[])null);
			}
			else{
				AlertDialog.Builder dlg = new AlertDialog.Builder(AddEditContact.this);
				dlg.setTitle(R.string.error_title);
				dlg.setMessage(R.string.error_message);
				dlg.setPositiveButton(R.string.error_button, null);
				dlg.show();
			}
			
		}
	};

	private void saveContact(){
		
		DatabaseConnector dbCon = new DatabaseConnector(this);
		
		if(isNewContact){
			dbCon.insertContact(nameEditText.getText().toString(),
								emailEditText.getText().toString(),
								phoneEditText.getText().toString(),
								streetEditText.getText().toString(),
								cityEditText.getText().toString());
		}
		else{
			dbCon.updateContact(rowId,
					nameEditText.getText().toString(),
					emailEditText.getText().toString(),
					phoneEditText.getText().toString(),
					streetEditText.getText().toString(),
					cityEditText.getText().toString());
		}
	}
}
