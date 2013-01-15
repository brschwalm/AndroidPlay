package com.anythinksolutions.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseConnector {

	private static final String DB_NAME = "user_contacts";
	private SQLiteDatabase db;
	private DatabaseOpenHelper dbOpenHelper;
	
	public DatabaseConnector(Context context){
		dbOpenHelper = new DatabaseOpenHelper(context, DB_NAME, null, 1);
	}
	
	public void open() throws SQLException{
		db = dbOpenHelper.getWritableDatabase();
	}
	
	public void close(){
		if(db != null)
			db.close();
	}
	
	private ContentValues setContent(String name, String email, String phone, String state, String city){
		ContentValues c = new ContentValues();
		c.put("name", name);
		c.put("email", email);
		c.put("phone", phone);
		c.put("state", state);
		c.put("city", city);
		return c;
	}
	
	public void insertContact(String name, String email, String phone, String state, String city){
		ContentValues c = setContent(name, email, phone, state, city);
		
		open();
		db.insert("contacts", null, c);
		close();
	}
	
	public void updateContact(long id, String name, String email, String phone, String state, String city){
		ContentValues c = setContent(name, email, phone, state, city);
		open();
		db.update("contacts", c,  getIdString(id), null);
		close();
	}
	
	public Cursor getAllContacts(){
		return db.query("contacts", new String[] {"_id", "name"}, null, null, null, null, "name");
	}
	
	public Cursor getOneContact(long id){
		return db.query("contacts", null, getIdString(id), null, null, null, null);
	}
	
	public void deleteContact(long id){
		open();
		db.delete("contacts", getIdString(id), null);
		close();
	}
	
	private String getIdString(long id){
		return "_id=" + id;
	}
	
	private class DatabaseOpenHelper extends SQLiteOpenHelper{
		public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version){
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			String createQuery = "CREATE TABLE contacts(" +
								 "_id integer primary key autoincrement, " +
								 "name TEXT, " +
								 "email TEXT, " +
								 "phone TEXT, " +
								 "street TEXT, " +
								 "city TEXT)";
			
			db.execSQL(createQuery);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub			
		}
	}
}
