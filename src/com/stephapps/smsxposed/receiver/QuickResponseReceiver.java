package com.stephapps.smsxposed.receiver;

import com.stephapps.smsxposed.QuickResponseDialogActivity;
import com.stephapps.smsxposed.misc.PhoneTools;
import com.stephapps.smsxposed.misc.SMSTools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class QuickResponseReceiver  extends BroadcastReceiver{
	 
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("QuickResponseReceiver","onReceive");
		
		PhoneTools.hideStatusBar(context);
	    	       
		Intent quickResponseIntent = new Intent(context,QuickResponseDialogActivity.class);
		quickResponseIntent.putExtras(intent.getExtras());
		quickResponseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.getApplicationContext().startActivity(quickResponseIntent);
	}
}
