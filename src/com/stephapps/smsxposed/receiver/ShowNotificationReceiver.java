package com.stephapps.smsxposed.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.stephapps.smsxposed.QuickResponseDialogActivity;
import com.stephapps.smsxposed.R;
import com.stephapps.smsxposed.misc.PhoneTools;
import com.stephapps.smsxposed.misc.SMSTools;

public class ShowNotificationReceiver extends BroadcastReceiver{
	 
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("QuickResponseReceiver","onReceive");
	   
		String[] actions = context.getResources().getStringArray(R.array.action_buttons_array);
		Bundle extras = intent.getExtras();
	   
		Notification paramNotif = extras.getParcelable("notification");
		String smsSender = extras.getString("sms_sender");
		String smsMsg = extras.getString("sms_msg");
		Integer notificationId = extras.getInt("notification_id");
	    
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + smsSender));
		PendingIntent pendingCallIntent = PendingIntent.getActivity(context, 0, callIntent, 0);
		
		Intent respondIntent = new Intent(); 
		respondIntent.putExtra("sms_sender", smsSender);
		respondIntent.putExtra("sms_msg", smsMsg);
		respondIntent.setAction("com.stephapps.smsxposed.quickresponse_receiver");
		PendingIntent pendingRespondIntent = PendingIntent.getBroadcast(context, 0, respondIntent, PendingIntent.FLAG_UPDATE_CURRENT);		    	     

		Intent markAsReadIntent = new Intent();
		markAsReadIntent.putExtra("sms_sender", smsSender);
		markAsReadIntent.putExtra("sms_msg", smsMsg);
		markAsReadIntent.putExtra("notification_id", notificationId);
		markAsReadIntent.setAction("com.stephapps.smsxposed.markasread_receiver");
	    PendingIntent pendingIntentMarkAsRead = PendingIntent.getBroadcast(context, 0, markAsReadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification newNotif = new Notification.Builder(context)
		.setWhen(paramNotif.when)
        .setTicker(extras.getCharSequence("ticker"))
        .setLargeIcon(paramNotif.largeIcon)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(extras.getCharSequence("content_title"))
        .setContentIntent(paramNotif.contentIntent)
        .setPriority(paramNotif.priority)
        .setSound(paramNotif.sound)
        .setDefaults(paramNotif.defaults)
        .setDeleteIntent(paramNotif.deleteIntent)
        .setContentText(extras.getCharSequence("content_text"))
        .addAction(android.R.drawable.ic_menu_call, actions[0], pendingCallIntent)
        .addAction(android.R.drawable.ic_menu_send, actions[1], pendingRespondIntent)
        .addAction(android.R.drawable.checkbox_on_background, actions[2], pendingIntentMarkAsRead)
        .build();
		
	    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    notificationManager.notify(notificationId,newNotif);	       
		Intent quickResponseIntent = new Intent(context,QuickResponseDialogActivity.class);
		quickResponseIntent.putExtras(intent.getExtras());
		quickResponseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.getApplicationContext().startActivity(quickResponseIntent);
	}
}