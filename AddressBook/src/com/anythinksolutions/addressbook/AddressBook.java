package com.anythinksolutions.addressbook;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class AddressBook extends ListActivity {

	
	private ListView contactListView;
	private CursorAdapter contactAdapter;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactListView = getListView();
        contactListView.setOnItemClickListener(viewContactListener);
        //contactListView.setBackgroundColor(Color.BLACK);
        
        //map each contact's name to a textview in the ListView layout
        String[] from = new String[] { Constants.NAME_ID };
        int[] to = new int[] { R.id.contactTextView };
        contactAdapter = new SimpleCursorAdapter(AddressBook.this, R.layout.contact_list_item, null, from, to);
        setListAdapter(contactAdapter);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	new GetContactsTask().execute((Object[])null);
    }
    
    @Override
    protected void onStop() {

    	//Deactivate the cursor and switch it to null
    	Cursor cursor = contactAdapter.getCursor();
    	if(cursor != null)
    		cursor.deactivate();
    	contactAdapter.changeCursor(null);
    	
    	super.onStop();
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_address_book, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	Intent addIntent = new Intent(AddressBook.this, AddEditContact.class);
    	startActivity(addIntent);
    	
    	return super.onOptionsItemSelected(item);
    }
    
    private class GetContactsTask extends AsyncTask<Object, Object, Cursor>{

    	DatabaseConnector dbCon = new DatabaseConnector(AddressBook.this);
    	
		@Override
		protected Cursor doInBackground(Object... params) {
			dbCon.open();
			return dbCon.getAllContacts();
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			contactAdapter.changeCursor(result);
			dbCon.close();
		}
    	
    }

    OnItemClickListener viewContactListener = new OnItemClickListener() {
    	@Override
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    		Intent viewIntent = new Intent(AddressBook.this, ViewContact.class);
    		viewIntent.putExtra(Constants.ROW_ID, arg3);
    		startActivity(viewIntent);
    	}
	};
}
