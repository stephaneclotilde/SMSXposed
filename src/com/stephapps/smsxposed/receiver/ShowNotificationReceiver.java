package com.stephapps.smsxposed.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.stephapps.smsxposed.R;

public class ShowNotificationReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Log.i("ShowNotificationReceiver","onReceive");
		String[] actions = context.getResources().getStringArray(R.array.action_buttons_array);
		Bundle extras = intent.getExtras();
	   
		Notification paramNotif = extras.getParcelable("notification");
		Integer notificationId = extras.getInt("notification_id");
		boolean addShowBtn = extras.getBoolean("add_show_btn");
		boolean showSender = extras.getBoolean("show_sender");
		boolean showNotificationAction = extras.getBoolean("show_notification_action");
		
		final String smsMsg = extras.getString("sms_msg", null);
    	final String smsSender = extras.getString("sms_sender", null);
    	Log.i("ShowNotificationReceiver","msg:"+smsMsg+", sender:"+smsSender);		
 		CharSequence sender;
 		if (!showSender) 	sender = extras.getCharSequence("content_title");
 		else 				sender = "    ";

 		Notification newNotif;
 		
 		if (addShowBtn)
 		{

	 		Intent showIntent = new Intent();
	 		showIntent.setAction("com.stephapps.smsxposed.shownotification_receiver");
	 		showIntent.putExtra("notification_id", notificationId);
	 		showIntent.putExtra("notification", (Notification)paramNotif);
	 		showIntent.putExtra("show_notification_action", showNotificationAction);
	 		showIntent.putExtra("add_show_btn", false);
	 		showIntent.putExtra("sms_sender", smsSender);
	 		showIntent.putExtra("sms_msg", smsMsg);
	 		showIntent.putExtra("package_name", extras.getString("package_name"));
	 		showIntent.putExtra("ticker", paramNotif.tickerText);
	 		showIntent.putExtra("content_title", extras.getCharSequence("content_title"));
	 		showIntent.putExtra("content_text", extras.getCharSequence("content_text"));
  	 		PendingIntent pendingShowIntent = PendingIntent.getBroadcast(context, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT );		
 	 		
			newNotif = new Notification.Builder(context)
			.setWhen(paramNotif.when)
	        .setTicker("    ")
	        .setLargeIcon(paramNotif.largeIcon)
	        .setSmallIcon(R.drawable.stat_notify_sms)
	        .setContentTitle(sender)
	        .setAutoCancel(true)
	        .setContentIntent(paramNotif.contentIntent)
	        .setPriority(paramNotif.priority)
	        .setSound(paramNotif.sound)
	        .setDefaults(paramNotif.defaults)
	        .setDeleteIntent(paramNotif.deleteIntent)
	        .setContentText("    ")
	        .addAction(android.R.drawable.ic_menu_view, context.getResources().getStringArray(R.array.action_buttons_array)[3], pendingShowIntent)
	        .build();
 		}
 		else
 		{
 			if (extras.getBoolean("show_notification_action")==true)
 			{				
 				Intent callIntent = new Intent(Intent.ACTION_CALL);
 				callIntent.setData(Uri.parse("tel:" + smsSender));
 				PendingIntent pendingCallIntent = PendingIntent.getActivity(context, 0, callIntent, 0);
 				
 				Intent respondIntent = new Intent(); 
 				respondIntent.putExtra("sms_sender", smsSender);
 				respondIntent.putExtra("sms_msg", smsMsg);
 				respondIntent.putExtra("notification_id", notificationId);
 				respondIntent.setAction("com.stephapps.smsxposed.quickresponse_receiver");
 				PendingIntent pendingRespondIntent = PendingIntent.getBroadcast(context, 0, respondIntent, PendingIntent.FLAG_UPDATE_CURRENT);		    	     
 		
 				Intent markAsReadIntent = new Intent();
 				markAsReadIntent.putExtra("sms_sender", smsSender);
 				markAsReadIntent.putExtra("sms_msg", smsMsg);
 				markAsReadIntent.putExtra("notification_id", notificationId);
 				markAsReadIntent.putExtra("package_name", extras.getString("package_name"));
 				markAsReadIntent.setAction("com.stephapps.smsxposed.markasread_receiver");
 			    PendingIntent pendingIntentMarkAsRead = PendingIntent.getBroadcast(context, 0, markAsReadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 				newNotif = new Notification.Builder(context)
 				.setWhen(paramNotif.when)
 		        .setTicker(extras.getCharSequence("ticker"))
 		        .setLargeIcon(paramNotif.largeIcon)
// 		        .setSmallIcon() //no longer needed since it will be notified by the original app so the resources will be found
 		        .setSmallIcon(R.drawable.stat_notify_sms)
 		        .setContentTitle(extras.getCharSequence("content_title"))
 		        .setContentIntent(paramNotif.contentIntent)
 		        .setPriority(paramNotif.priority)
 		        .setAutoCancel(true)
 	//	        .setSound(paramNotif.sound)
 	//	        .setDefaults(paramNotif.defaults)
 		        .setDeleteIntent(paramNotif.deleteIntent)
 		        .setContentText(extras.getCharSequence("content_text"))
 		        .addAction(android.R.drawable.ic_menu_call, actions[0], pendingCallIntent)
 		        .addAction(android.R.drawable.ic_menu_send, actions[1], pendingRespondIntent)
 		        .addAction(android.R.drawable.checkbox_on_background, actions[2], pendingIntentMarkAsRead)
 		        .build();
 			}
 			else
 			{
 				newNotif = new Notification.Builder(context)
 				.setWhen(paramNotif.when)
 		        .setTicker(extras.getCharSequence("ticker"))
 		        .setLargeIcon(paramNotif.largeIcon)
 		        .setSmallIcon(R.drawable.stat_notify_sms)
 		        .setContentTitle(extras.getCharSequence("content_title"))
 		        .setContentIntent(paramNotif.contentIntent)
 		        .setPriority(paramNotif.priority)
 	//	        .setDefaults(paramNotif.defaults)
 		        .setAutoCancel(true)
 		        .setDeleteIntent(paramNotif.deleteIntent)
 		        .setContentText(extras.getCharSequence("content_text"))
 		        .build();		
 			}
 		}
 		
 //		if ((Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN)&&(paramNotif.bigContentView!=null))
//			newNotif.bigContentView = paramNotif.bigContentView;
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notificationId,newNotif);
		
	}
}