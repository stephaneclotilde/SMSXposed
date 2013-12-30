package com.stephapps.smsxposed;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.stephapps.smsxposed.misc.SMSTools;

public class QuickResponseDialogActivity extends Activity {

	String mSMSSender = null, mSMSMsg = null;
	int mNotificationId = -1;
	boolean mKeyGuardHasBeenManuallyDisabled = false;
	KeyguardManager.KeyguardLock mLock;
	
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
		KeyguardManager  keyGuard = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
		if (keyGuard.isKeyguardLocked()&&!keyGuard.isKeyguardSecure())
		{
			mLock = keyGuard.newKeyguardLock("tagName");
			mLock.disableKeyguard();
			mKeyGuardHasBeenManuallyDisabled=true;
		}
		
		String contactName = SMSTools.getContactName(this, mSMSSender);
		if (contactName==null) contactName = mSMSSender;
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		new AlertDialog.Builder(this)
	    .setTitle("Respond to SMS")
	    .setMessage(getString(R.string.to_recipe)+contactName)
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() 
	    {
	        public void onClick(DialogInterface dialog, int whichButton) 
	        {
	            Editable response = input.getText(); 
	            Context context = QuickResponseDialogActivity.this.getApplicationContext();
	            SMSTools.sendSMS(context, mSMSSender, response.toString());
	            SMSTools.markMessageRead(context,mSMSSender , mSMSMsg);
	            
	            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	            notificationManager.cancel(mNotificationId);
	     	   
	            dialog.cancel();
                finish();
                
 //               if (mKeyGuardHasBeenManuallyDisabled) mLock.reenableKeyguard();
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	dialog.cancel();
//	        	if (mKeyGuardHasBeenManuallyDisabled) mLock.reenableKeyguard();	        	 
                finish();              
	        }
	    })
	    .setOnCancelListener(new OnCancelListener() {	
			@Override
			public void onCancel(DialogInterface dialog) {
//				if (mKeyGuardHasBeenManuallyDisabled) mLock.reenableKeyguard();	        	 
                finish();
			}
		})
		.show();		
	}
}
