package com.stephapps.smsxposed;

import com.stephapps.smsxposed.misc.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SendBroadcastActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Log.i("SendBroadCastActivity","onCreate");
	    
	    Intent sendIntent = new Intent();
		sendIntent.setAction("android.provider.Telephony.SMS_RECEIVED");
		sendIntent.putExtras(getIntent().getExtras());
		sendBroadcast(sendIntent);  
	    finish();
	}
}
