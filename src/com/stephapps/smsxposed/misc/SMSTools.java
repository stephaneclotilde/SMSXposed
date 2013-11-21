package com.stephapps.smsxposed.misc;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import com.stephapps.smsxposed.R;

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
	
	//---sends an SMS message to another device---
    //@SuppressWarnings("deprecation")
    public static void sendSMS(Context context, String phoneNumber, String message)
    {     
    	context = context.getApplicationContext();
        //Log.v("phoneNumber",phoneNumber);
        //Log.v("MEssage",message);
        PendingIntent pi = PendingIntent.getActivity(context, 0,
            new Intent(context, Object.class), 0);//Object.class is a dummy class                
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, pi, null);  
        
        //put the msg in sent sms
        ContentValues values = new ContentValues();
        values.put("address", phoneNumber);
        values.put("body", message);
        context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
    }

}
