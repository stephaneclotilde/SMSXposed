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
	
	public static boolean moveAppToSystemWithRoot(Context context)
	{
		if (RootTools.isAccessGiven()==false) return false;
		
		PackageInfo paramPackageInfo = null;
        try {
            paramPackageInfo = context.getPackageManager().getPackageInfo(
            		context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        ApplicationInfo localApplicationInfo = paramPackageInfo.applicationInfo;

        String str1 = "/system/app/" + localApplicationInfo.packageName
                + ".apk";
        String str2 = "busybox mv " + localApplicationInfo.sourceDir + " "
                + str1;
        Log.i("PhoneTools","packageName:"+str1+" ,sourceDir:"+localApplicationInfo.sourceDir);
        RootTools.remount("/system", "rw");
        RootTools.remount("/mnt", "rw");

        CommandCapture command = new CommandCapture(0, str2,
                "busybox chmod 644 " + str1){
        	@Override
    	    public void commandCompleted(int id, int exitCode) {
    	        Log.d("PhoneTools", "Command completed");
    	        RootTools.remount("/system", "ro");
    	        RootTools.remount("/mnt", "ro");
    	    }

        };

       
        try {
            RootTools.getShell(true).add(command);
			commandWait(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Log.d("PhoneTools", "Command finished");    
        
        return true;
	}
	
	public static boolean isAppInstalledAsSystem(Context context)
	{
		return  (context.getApplicationInfo().flags
				  & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
	}
	
	private static void commandWait(Command cmd) throws Exception {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 3200; //7 tries, 6350 msec

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("PhoneTools", "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }
}
