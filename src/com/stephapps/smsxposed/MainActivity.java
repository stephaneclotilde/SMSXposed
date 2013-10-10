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
		    Set<String> sources = new HashSet<String>(Arrays.asList(res.getStringArray(R.array.punctuation_array)));
		    Set<String> destinations = new HashSet<String>(Arrays.asList(res.getStringArray(R.array.symbols_array)));
		    Set<String> delayed_sources = new HashSet<String>(Arrays.asList(res.getStringArray(R.array.delayed_punctuation_array)));
		    Set<String> delayed_destinations = new HashSet<String>(Arrays.asList(res.getStringArray(R.array.delayed_symbols_array)));
		    edit.putStringSet(Constants.SOURCES, sources);
		    edit.putStringSet(Constants.DESTINATIONS, destinations);
		    edit.putStringSet(Constants.DELAYED_SOURCES, delayed_sources);
		    edit.putStringSet(Constants.DELAYED_DESTINATIONS, delayed_destinations);
		    edit.commit();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
