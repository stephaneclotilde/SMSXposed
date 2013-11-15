package com.stephapps.smsxposed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

public class QuickResponseDialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Log.i("QuickResponseDialogActivity","onCreate");
		
	    displayAlert();
	}

	private void displayAlert()
	{
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		
		new AlertDialog.Builder(this)
	    .setTitle("Respond")
	    .setMessage("")
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Editable value = input.getText(); 
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
