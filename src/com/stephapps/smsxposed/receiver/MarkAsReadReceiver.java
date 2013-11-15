package com.stephapps.smsxposed.receiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.stephapps.smsxposed.misc.PhoneTools;
import com.stephapps.smsxposed.misc.SMSTools;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MarkAsReadReceiver extends BroadcastReceiver{
			 
	@Override
	 public void onReceive(Context context, Intent intent) {
		Log.i("MarkAsReadReceiver","onReceive");
		
       PhoneTools.hideStatusBar(context);
       
       Bundle extras = intent.getExtras();
       SMSTools.markMessageRead(context, extras.getString("sms_sender"), extras.getString("sms_msg"));
       Toast.makeText(context, "mark as read", Toast.LENGTH_SHORT).show();
       
	 } 
}