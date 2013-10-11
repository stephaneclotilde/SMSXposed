package com.stephapps.smsxposed;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.stephapps.smsxposed.R;
import com.stephapps.smsxposed.misc.Constants;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
	    if (savedInstanceState == null)
	        getFragmentManager().beginTransaction().replace(android.R.id.content,
	    new PrefsFragment()).commit();
	}
	
	public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
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
		    saveArray(edit,Constants.SOURCES, sources);
		    saveArray(edit,Constants.DESTINATIONS, destinations);
		    saveArray(edit,Constants.DELAYED_SOURCES, delayed_sources);
		    saveArray(edit,Constants.DELAYED_DESTINATIONS, delayed_destinations);
		    edit.commit();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO Auto-generated method stub
			
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
