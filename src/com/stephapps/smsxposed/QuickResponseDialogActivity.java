package com.stephapps.smsxposed;

import com.stephapps.smsxposed.misc.SMSTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

public class QuickResponseDialogActivity extends Activity {

	String mSMSSender = null, mSMSMsg = null;
	int mNotificationId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Log.i("QuickResponseDialogActivity","onCreate");
	    Bundle extras = getIntent().getExtras();
	    mSMSSender = extras.getString("sms_sender");
	    mSMSMsg = extras.getString("sms_msg");
	    mNotificationId = extras.getInt("notification_id");
	    displayAlert();
	}

	private void displayAlert()
	{
		String contactName = SMSTools.getContactName(this, mSMSSender);
		if (contactName==null) contactName = mSMSSender;
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		new AlertDialog.Builder(this)
	    .setTitle("Respond to SMS")
	    .setMessage(getString(R.string.to_recipe)+contactName)
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Editable response = input.getText(); 
	            Context context = QuickResponseDialogActivity.this.getApplicationContext();
	            SMSTools.sendSMS(context, mSMSSender, response.toString());
	            SMSTools.markMessageRead(context,mSMSSender , mSMSMsg);
	            
	            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	            notificationManager.cancel(mNotificationId);
	     	   
	            dialog.cancel();
                finish();
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	dialog.cancel();
                finish();
	        }
	    }).show();	
	}

}
