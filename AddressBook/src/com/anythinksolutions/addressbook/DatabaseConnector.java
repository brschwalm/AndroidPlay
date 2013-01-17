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
	private static final String TABLE_NAME = "contacts";
	private static final String ID_PREFIX = "_id";
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
		c.put(Constants.NAME_ID, name);
		c.put(Constants.EMAIL_ID, email);
		c.put(Constants.PHONE_ID, phone);
		c.put(Constants.STREET_ID, state);
		c.put(Constants.CITY_ID, city);
		return c;
	}
	
	public void insertContact(String name, String email, String phone, String state, String city){
		ContentValues c = setContent(name, email, phone, state, city);
		
		open();
		db.insert(TABLE_NAME, null, c);
		close();
	}
	
	public void updateContact(long id, String name, String email, String phone, String state, String city){
		ContentValues c = setContent(name, email, phone, state, city);
		open();
		db.update(TABLE_NAME, c,  getIdString(id), null);
		close();
	}
	
	public Cursor getAllContacts(){
		return db.query(TABLE_NAME, new String[] {ID_PREFIX, Constants.NAME_ID}, null, null, null, null, Constants.NAME_ID);
	}
	
	public Cursor getOneContact(long id){
		return db.query(TABLE_NAME, null, getIdString(id), null, null, null, null);
	}
	
	public void deleteContact(long id){
		open();
		db.delete(TABLE_NAME, getIdString(id), null);
		close();
	}
	
	private String getIdString(long id){
		return ID_PREFIX + "=" + id;
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
