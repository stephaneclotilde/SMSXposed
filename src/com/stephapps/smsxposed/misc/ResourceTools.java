package com.stephapps.smsxposed.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class ResourceTools {

	public static Drawable resizDrawable(Drawable image, float density) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, (int)(48*density), (int)(48*density), false);
        return new BitmapDrawable(bitmapResized);
    }
	
	public static void generateNoteOnSD(String sFileName, String sBody)
	{
	    try
	    {
	        File root = new File(Environment.getExternalStorageDirectory(), "SMSXposed");
	        if (!root.exists()) {
	            root.mkdirs();
	        }
	        File gpxfile = new File(root, sFileName);
	        FileWriter writer = new FileWriter(gpxfile);
	        writer.append(sBody);
	        writer.flush();
	        writer.close();
	        Log.i("Resource Tools","file saved");
	        Log.i("Resource Tools",""+readFile("SMSXposed/"+sFileName));
	    }
	    catch(IOException e)
	    {
	         e.printStackTrace();
	    }
	} 
	
	public static void appendNoteOnSD(String sFileName, String sBody)
	{
	    String existingString = readFile("SMSXposed/"+sFileName);
	    generateNoteOnSD(sFileName,existingString+sBody);
	} 
	
	public static String readFile(String sFilePath)
	{
		//Find the directory for the SD Card using the API
		//*Don't* hardcode "/sdcard"
		File sdcard = Environment.getExternalStorageDirectory();

		//Get the text file
		File file = new File(sdcard,sFilePath);

		//Read text from file
		StringBuilder text = new StringBuilder();

		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();
		}
		catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
		
		return text.toString();
	}
}
