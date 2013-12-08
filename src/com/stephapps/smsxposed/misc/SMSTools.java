package com.stephapps.smsxposed.misc;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.stephapps.smsxposed.R;

public class SMSTools {
	public static final String ADDRESS = "address";
	public static final String DATE = "date";
	public static final String DATE_SENT = "date_sent";
	public static final String READ = "read";
	public static final String SEEN = "seen";
	public static final String PROTOCOL = "protocol";
	public static final String SUBJECT = "subject";
	public static final String REPLY_PATH_PRESENT = "reply_path_present";
	public static final String SERVICE_CENTER = "service_center";

	private static final boolean DEBUG = false;
	public static final long THREAD_NONE = -2;
	public static final String SMS_THREAD_ID = "thread_id";
	private static final String[] SMS_THREAD_ID_PROJECTION = new String[] { SMS_THREAD_ID };
	   
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
    
    public static SmsMessage[] getMessagesFromIntent( Intent intent) 
    {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        String format = intent.getStringExtra("format");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
    
//    /**
//     * Get the thread ID of the SMS message with the given URI
//     * @param context The context
//     * @param uri The URI of the SMS message
//     * @return The thread ID, or THREAD_NONE if the URI contains no entries
//     */
//    public static long getSmsThreadId(Context context, Uri uri) {
//        Cursor cursor = SQLiteWrapper.query(
//            context,
//            context.getContentResolver(),
//            uri,
//            SMS_THREAD_ID_PROJECTION,
//            null,
//            null,
//            null);
//
//        if (cursor == null) {
//            if (DEBUG) {
//                Log.d("SMSTools", "getSmsThreadId uri: " + uri + " NULL cursor! returning THREAD_NONE");
//            }
//            return THREAD_NONE;
//        }
//
//        try {
//            if (cursor.moveToFirst()) {
//                long threadId = cursor.getLong(cursor.getColumnIndex(SMS_THREAD_ID));
//                if (DEBUG) {
//                    Log.d("SMSTools", "getSmsThreadId uri: " + uri +
//                            " returning threadId: " + threadId);
//                }
//                return threadId;
//            } else {
//                if (DEBUG) {
//                    Log.d("SMSTools", "getSmsThreadId uri: " + uri +
//                            " NULL cursor! returning THREAD_NONE");
//                }
//                return THREAD_NONE;
//            }
//        } finally {
//            cursor.close();
//        }
//    }
    
    public static boolean extractSMSFromIntent(Intent intent)
    {
        Bundle extras;
        byte[][] pdus = null;
        extras = intent.getExtras();
        if (extras == null) return false;

        pdus =  (byte[][])extras.get("pdus");
        if (pdus.length <= 0) return false;

        SmsMessage smsMessage;
        String s2;
        smsMessage = SmsMessage.createFromPdu((byte[])pdus[0]);
        String s1;
        try
        {
            s1 = smsMessage.getMessageBody().toString();
        }
        // Misplaced declaration of an exception variable
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        
        return false;

    }
    
    public static String getContactName(Context context, String phoneNumber) 
    {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
    
    public static CharSequence[] getTextFromNotificationView(RemoteViews contentView, Context context)
    {
    	CharSequence[] notificationsText = new String[3];
    	
    	try {
			/* Re-create a 'local' view group from the info contained in the remote view */
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup localView = (ViewGroup) inflater.inflate(contentView.getLayoutId(), null);
			contentView.reapply(context, localView);
			
			notificationsText[0] = ((TextView)localView.findViewById(16908310)).getText();
//    	notificationsText[1] = ((TextView)localView.findViewById(16909082)).getText();
			notificationsText[2] = ((TextView)localView.findViewById(16908358)).getText().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return notificationsText;
    }

}
