package com.anythinksolutions.weatherviewer;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class Utils {

	public static String getText(TextView tv){
		return tv.getText().toString();
	}
	
	public static void showToast(Context context, String message, int length){
		Toast t = Toast.makeText(context, message, length);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	public static void showToast(Context context, String message){
		Utils.showToast(context, message, Toast.LENGTH_LONG);
	}
	
	public static void showToast(Context context, int messageId){
		Utils.showToast(context, context.getResources().getString(messageId));
	}
}
