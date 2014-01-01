package com.stephapps.smsxposed.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;

public class FlashFlashlightReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		flashFlashlight(context);
	}

	private void flashFlashlight(Context context)
	{
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
		{
			try {
				final Camera cam = Camera.open();     
				Parameters p = cam.getParameters();
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				cam.setParameters(p);
				
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						cam.stopPreview();
						cam.release();
					}
				}, 300);
			} catch (Exception e) {e.printStackTrace();}			
		}
	}
}
