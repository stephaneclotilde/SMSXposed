package com.stephapps.smsxposed.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.stephapps.smsxposed.QuickResponseDialogActivity;
import com.stephapps.smsxposed.R;
import com.stephapps.smsxposed.SendBroadcastActivity;
import com.stephapps.smsxposed.misc.Constants;
import com.stephapps.smsxposed.misc.PhoneTools;
import com.stephapps.smsxposed.misc.SMSTools;

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
		
		Notification newNotif;
		
		if (extras.getBoolean("show_action_buttons")==true)
		{
			String smsSender = extras.getString("sms_sender");
			String smsMsg = extras.getString("sms_msg");
			
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
	
			newNotif = new Notification.Builder(context)
			.setWhen(paramNotif.when)
	        .setTicker(extras.getCharSequence("ticker"))
	       // .setLargeIcon(paramNotif.largeIcon)
	        //.setSmallIcon(R.drawable.ic_launcher) //no longer needed since it will be notified by the original app so the resources will be found
	        .setSmallIcon(paramNotif.icon)
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
		}
		else
		{
			newNotif = new Notification.Builder(context)
			.setWhen(paramNotif.when)
	        .setTicker(extras.getCharSequence("ticker"))
	        //.setLargeIcon(paramNotif.largeIcon)
	        //.setSmallIcon(R.drawable.ic_launcher) //no longer needed since it will be notified by the original app so the resources will be found
	        .setSmallIcon(paramNotif.icon)
	        .setContentTitle(extras.getCharSequence("content_title"))
	        .setContentIntent(paramNotif.contentIntent)
	        .setPriority(paramNotif.priority)
	        .setSound(paramNotif.sound)
	        .setDefaults(paramNotif.defaults)
	        .setDeleteIntent(paramNotif.deleteIntent)
	        .setContentText(extras.getCharSequence("content_text"))
	        .build();		
		}
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);			
		Editor edit = settings.edit();
		edit.putString("package_name_to_replace", extras.getString("package_name"));
		edit.putBoolean("replace_package_name", true);
		edit.commit();
		//no longer needed, see below
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);//TODO:needed because for it to update the notification it needs to be done by the app itself(AOSP sms or Hangouts or other)    
//		notificationManager.notify(notificationId,newNotif);	
		
		edit.putBoolean("replace_package_name", false);
		edit.commit();
//		//we send back to the application because only the app itself can update an existing notification
//		 Intent sendIntent = new Intent(context,SendBroadcastActivity.class);
//        sendIntent.putExtra("notification", newNotif);
//		sendIntent.putExtra("notification_id", notificationId);
//		sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.getApplicationContext().startActivity(sendIntent);
	}
}