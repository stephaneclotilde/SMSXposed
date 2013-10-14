package com.stephapps.smsxposed;
import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;


//@ReportsCrashes(formKey = "", // will not be used
//mailTo = "stephane@mobicrea.com", // my email here
//mode = ReportingInteractionMode.TOAST,
//resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		super.onCreate();
//		ACRA.init(this);
	}
}	