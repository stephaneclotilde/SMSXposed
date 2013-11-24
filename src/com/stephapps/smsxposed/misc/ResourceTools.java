package com.stephapps.smsxposed.misc;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ResourceTools {

	public static Drawable resizDrawable(Drawable image, float density) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, (int)(48*density), (int)(48*density), false);
        return new BitmapDrawable(bitmapResized);
    }
}
