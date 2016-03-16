package app.varlorg.unote;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.Toast;

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
                String path = noteBdd.exportDB();
                //Toast.makeText(Preference.this, "Database exported in "+ path + " ! ", Toast.LENGTH_LONG).show();
                Toast.makeText(Preference.this, Preference.this.getString(R.string.toast_export_db) + path + " ! ", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        android.preference.Preference buttonImport = findPreference("buttonImport");
        buttonImport.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(
                android.preference.Preference arg0) {
                NotesBDD noteBdd = new NotesBDD(null);
                String path = noteBdd.importDB();
                //Toast.makeText(Preference.this, "Database imported from "+ path + " ! ", Toast.LENGTH_LONG).show();
                Toast.makeText(Preference.this, Preference.this.getString(R.string.toast_import_db)+ path + " ! ", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        /*android.preference.Preference buttonDelete = findPreference("buttonDelete");
        buttonDelete.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(
                    android.preference.Preference arg0) {
                NotesBDD noteBdd = new NotesBDD(Preference.this);
                noteBdd.clean();
                Toast.makeText(Preference.this, "Database cleaned ! ", Toast.LENGTH_LONG).show();
                return false;
            }
        });*/
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
