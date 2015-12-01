package fr.commandestation.main;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigurationPreferences extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(fr.commandestation.R.xml.preference);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
