package app.varlorg.unote;

import static app.varlorg.unote.NoteMain.customToastGeneric;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;

//import yuku.ambilwarna.AmbilWarnaDialog;

public class Preference extends PreferenceActivity {
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 2;
    private static Uri uri;
    private static PreferenceCategory exportDirCategory;
    private static String outputDir;
    private android.preference.Preference exportDirSelect;
    Bundle savedInstanceState;

    private int textSize;
    void customToast(String s){
        customToastGeneric(Preference.this, Preference.this.getResources(), s);
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        //String uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}");

        textSize = Integer.parseInt(pref.getString("pref_sizeNote", "18"));
        if ( textSize == -1 )
        {
            textSize = Integer.parseInt(pref.getString("pref_sizeNote_custom", "18"));
        }

        NoteMain.setUi(this, pref, getApplicationContext(), getWindow());

        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        android.preference.Preference pref_screen_global = findPreference("pref_screen_global");
        pref_screen_global.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                Intent globalActivity = new Intent(getBaseContext(), PreferenceGlobal.class);
                startActivity(globalActivity);
                return(false);
            }
        });
        android.preference.Preference pref_screen_edition = findPreference("pref_screen_edition");
        pref_screen_edition.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                Intent globalActivity = new Intent(getBaseContext(), PreferenceEdition.class);
                startActivity(globalActivity);
                return(false);
            }
        });
        android.preference.Preference pref_screen_main = findPreference("pref_screen_main");
        pref_screen_main.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                Intent globalActivity = new Intent(getBaseContext(), PreferenceMain.class);
                startActivity(globalActivity);
                return(false);
            }
        });
        android.preference.Preference pref_screen_export = findPreference("pref_screen_export");
        pref_screen_export.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                Intent globalActivity = new Intent(getBaseContext(), PreferenceExport.class);
                startActivity(globalActivity);
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
