package com.stephapps.smsxposed.misc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SMSTools {

	public static void markMessageRead(Context context, String number, String body) 
	{
	
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try{
	        while (cursor.moveToNext()) 
	        {
	                if ((cursor.getString(cursor.getColumnIndex("address")).equals(number)) && (cursor.getInt(cursor.getColumnIndex("read")) == 0)) {
	                    if (cursor.getString(cursor.getColumnIndex("body")).startsWith(body)) {
	                        String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
	                        ContentValues values = new ContentValues();
	                        values.put("read", true);
	                        context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
	                        return;
	                    }
	                }
	        }
	  }
        catch(Exception e)
	  {
	      Log.e("Mark Read", "Error in Read: "+e.toString());
	  }
	}
}
