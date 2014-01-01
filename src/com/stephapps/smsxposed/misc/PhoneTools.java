package com.stephapps.smsxposed.misc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;


public class PhoneTools {

	public static void hideStatusBar(Context context)
	{
		context = context.getApplicationContext();
		Object sbservice = context.getSystemService( "statusbar" );
		Class<?> statusbarManager=null;
        try {
        	 statusbarManager = Class.forName( "android.app.StatusBarManager" );
        	 Method hidesb;
			 if ((Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1))
				 hidesb = statusbarManager.getMethod( "collapse" );
			 else 
				 hidesb = statusbarManager.getMethod( "collapsePanels" );
		     hidesb.invoke( sbservice );
		} catch (ClassNotFoundException e) {e.printStackTrace();
		} catch (NoSuchMethodException e) {e.printStackTrace();	     
		} catch (IllegalArgumentException e) {e.printStackTrace();
		} catch (IllegalAccessException e) {e.printStackTrace();
		} catch (InvocationTargetException e) {e.printStackTrace();
		}
	}
}
