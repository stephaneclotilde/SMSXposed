package com.stephapps.smsxposed;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.stephapps.smsxposed.R;
import com.stephapps.smsxposed.misc.Constants;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity 
{	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
	    if (savedInstanceState == null)
	        getFragmentManager().beginTransaction().replace(android.R.id.content,
	    new PrefsFragment()).commit();
	}
	
	public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener 
	{	
		private boolean mToastRebootOnChangeHasBeenShow = false;
		
		@Override
		public void onCreate(Bundle savedInstanceState) 
		{
			super.onCreate(savedInstanceState);
	
			// this is important because although the handler classes that read these settings
			// are in the same package, they are executed in the context of the hooked package
			getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
			addPreferencesFromResource(R.xml.preferences); 
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());			
			Editor edit = settings.edit();
		    Resources res =  getActivity().getResources();
		    String[] sources = res.getStringArray(R.array.punctuation_array);
		    String[] destinations = res.getStringArray(R.array.symbols_array);
		    String[] delayed_sources = res.getStringArray(R.array.delayed_punctuation_array);
		    String[] delayed_destinations = res.getStringArray(R.array.delayed_symbols_array);
		    String[] notifications_actions = res.getStringArray(R.array.action_buttons_array);
		    saveArray(edit,Constants.SOURCES, sources);
		    saveArray(edit,Constants.DESTINATIONS, destinations);
		    saveArray(edit,Constants.DELAYED_SOURCES, delayed_sources);
		    saveArray(edit,Constants.DELAYED_DESTINATIONS, delayed_destinations);
		    saveArray(edit,Constants.NOTIFICATION_ACTIONS, notifications_actions);
		    edit.commit();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
		{
			if (mToastRebootOnChangeHasBeenShow==false)
			{
				Toast.makeText(getActivity().getApplicationContext(),getString(R.string.changes_apply_on_reboot),Toast.LENGTH_SHORT).show();
				mToastRebootOnChangeHasBeenShow=true;
			}
			
			if (key.equals("sms_custom_icon_color_toggle"))
			{
				if (sharedPreferences.getFloat("density", -1)!=-1)
				{
					Editor edit = sharedPreferences.edit();
					edit.putFloat("density", getResources().getDisplayMetrics().density);
					edit.commit();
				}
			}
		}
		
		@Override
		public void onResume() {
		    super.onResume();
		    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		}

		@Override
		public void onPause() {
		    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		    super.onPause();
		}
		
		private static void saveArray(Editor edit, String name, String[] values)
		{
			edit.putInt(name+ "_size", values.length);
		    for(int i=0;i<values.length;i++)
		    	edit.putString(name+ "_" + i, values[i]);    
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	
}
