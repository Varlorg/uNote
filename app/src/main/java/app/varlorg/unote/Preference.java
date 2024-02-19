package app.varlorg.unote;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Preference extends PreferenceActivity {
    /** Called when the activity is first created. */

    private int textSize;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        textSize = Integer.parseInt(pref.getString("pref_sizeNote", "18"));
        if ( textSize == -1 )
        {
            textSize = Integer.parseInt(pref.getString("pref_sizeNote_custom", "18"));
        }

        if (!pref.getBoolean("pref_theme", false))
        {
            setTheme(android.R.style.Theme_DeviceDefault);
        }
        else
        {
            setTheme(android.R.style.Theme_DeviceDefault_Light);
        }
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        android.preference.Preference button = findPreference("buttonExport");
        button.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                NotesBDD noteBdd = new NotesBDD(null);
                String path      = noteBdd.exportDB(Preference.this);
                if (path != null)
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        TextView textView = new TextView(Preference.this);
                        textView.setText(Preference.this.getString(R.string.toast_export_db) + " " + path + " ! ");
                        textView.setTextSize((int)(textSize * NoteMain.TOAST_TEXTSIZE_FACTOR));

                        Toast toast = new Toast(Preference.this);
                        toast.setView(textView);
                        toast.show();
                    }
                }
                else
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        TextView textView = new TextView(Preference.this);
                        textView.setText(" Error " + path + " ! ");
                        textView.setTextSize((int)(textSize * NoteMain.TOAST_TEXTSIZE_FACTOR));

                        Toast toast = new Toast(Preference.this);
                        toast.setView(textView);
                        toast.show();
                    }
                }
                return(false);
            }
        });
        android.preference.Preference buttonImport = findPreference("buttonImport");
        buttonImport.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                Intent restoreActivity = new Intent(getBaseContext(), RestoreDbActivity.class);
                startActivity(restoreActivity);
                return(false);
            }
        });
        android.preference.Preference buttonExportCSV = findPreference("buttonExportCSV");
        buttonExportCSV.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                NotesBDD noteBdd = new NotesBDD(Preference.this);
                noteBdd.open();
                String path      = noteBdd.exportCSV(Preference.this);
                if (path != null)
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        TextView textView = new TextView(Preference.this);
                        textView.setText(Preference.this.getString(R.string.toast_export_db) + " " + path + " ! ");
                        textView.setTextSize((int)(textSize * NoteMain.TOAST_TEXTSIZE_FACTOR));

                        Toast toast = new Toast(Preference.this);
                        toast.setView(textView);
                        toast.show();
                    }
                }
                else
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        TextView textView = new TextView(Preference.this);
                        textView.setText(" Error " + path + " ! ");
                        textView.setTextSize((int)(textSize * NoteMain.TOAST_TEXTSIZE_FACTOR));

                        Toast toast = new Toast(Preference.this);
                        toast.setView(textView);
                        toast.show();
                    }
                }
                return(false);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, NoteMain.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
