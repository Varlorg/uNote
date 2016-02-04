package app.varlorg.unote;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;

public class Preference extends PreferenceActivity {
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences         pref = PreferenceManager.getDefaultSharedPreferences(this);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            if (pref.getBoolean("pref_theme",false) == false) {
                setTheme(android.R.style.Theme_DeviceDefault);
            } else {
                setTheme(android.R.style.Theme_DeviceDefault_Light);
            }
        } else{
            if (pref.getBoolean("pref_theme",false) == false) {
                setTheme(android.R.style.Theme_Black);
            } else {
                setTheme(android.R.style.Theme_Light);
            }
        }
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        android.preference.Preference button = findPreference("buttonExport");
        button.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(
                android.preference.Preference arg0) {
                NotesBDD noteBdd = new NotesBDD(null);
                noteBdd.exportDB();
                return false;
            }
        });
        android.preference.Preference buttonImport = findPreference("buttonImport");
        buttonImport.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(
                android.preference.Preference arg0) {
                NotesBDD noteBdd = new NotesBDD(null);
                noteBdd.importDB();
                return false;
            }
        });

    }
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, NoteMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

}
