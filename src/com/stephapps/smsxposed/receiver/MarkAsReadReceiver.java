package com.stephapps.smsxposed.receiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.stephapps.smsxposed.misc.PhoneTools;
import com.stephapps.smsxposed.misc.SMSTools;

import android.app.NotificationManager;
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
	public void onReceive(Context context, Intent intent) 
	{
		Log.i("MarkAsReadReceiver","onReceive");
		
		Bundle extras = intent.getExtras();
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    notificationManager.cancel(extras.getInt("notification_id"));
        
		PhoneTools.hideStatusBar(context);
		
		if ((Build.VERSION.SDK_INT <= 18))//inferior to kitkat
	    {
			SMSTools.markMessageRead(context, extras.getString("sms_sender"), extras.getString("sms_msg"));
			Toast.makeText(context, "mark as read", Toast.LENGTH_SHORT).show();
	    }
		else
		{
//			Intent i = new Intent();
//			i.putExtras(extras);
//			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//			
//			if (extras.getString("package_name").equals("com.android.mms"))
//				i.setComponent(new ComponentName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity"));
//			else // HANGOUTS
//				i.setComponent(new ComponentName("com.google.android.talk", "com.google.android.apps.babel.phone.BabelHomeActivity"));
//			context.startActivity(i);
			Intent i = new Intent();
			i.putExtras(extras);
			if (extras.getString("package_name").equals("com.android.mms"))
				i.setAction("com.android.mms.transaction.MessageStatusReceiver.MESSAGE_STATUS_RECEIVED");
			else
				i.setAction("com.google.android.apps.babel.realtimechat.reset_error_notifications");
			context.sendBroadcast(i);
		}
	 } 
}