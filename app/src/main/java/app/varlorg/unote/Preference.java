package app.varlorg.unote;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class Preference extends PreferenceActivity {
    /** Called when the activity is first created. */

    private int textSize;
    void customToast(String msgToDisplay){
        LinearLayout linearLayout=new LinearLayout(getApplicationContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        GradientDrawable shape=new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(50);
        shape.setColor(getResources().getColor(android.R.color.background_light));
        shape.setStroke(3,getResources().getColor(android.R.color.transparent));

        TextView textView=new TextView(getApplicationContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        textView.setMaxWidth((int)(getResources().getDisplayMetrics().widthPixels*0.9));
        textView.setText(msgToDisplay);
        textView.setTextSize((int)(textSize*NoteMain.TOAST_TEXTSIZE_FACTOR));
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setAlpha(1f);
        textView.setBackground(shape);
        int pad_width=(int)(getResources().getDisplayMetrics().widthPixels*0.04);
        int pad_height=(int)(getResources().getDisplayMetrics().heightPixels*0.02);
        textView.setPadding(pad_width,pad_height,pad_width,pad_height);

        Toast toast=new Toast(getApplicationContext());

        linearLayout.addView(textView);
        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_LONG);

        toast.show();
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
                        customToast(Preference.this.getString(R.string.toast_export_db) + " " + path + " ! ");
                    }
                }
                else
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(" Error " + path + " ! ");
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
                noteBdd.close();
                if (path != null)
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(Preference.this.getString(R.string.toast_export_db) + " " + path + " ! ");
                    }
                }
                else
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(" Error " + path + " ! ");
                    }
                }
                return(false);
            }
        });

        android.preference.Preference buttonExportAllNotes = findPreference("buttonExportAllNotes");
        buttonExportAllNotes.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                NotesBDD noteBdd = new NotesBDD(Preference.this);
                noteBdd.open();
                String path = noteBdd.exportAllNotes(Preference.this);
                noteBdd.close();
                if (path != null)
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(Preference.this.getString(R.string.toast_export_all_notes) + " " + path + " ! ");
                    }
                }
                else
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(" Error " + path + " ! ");
                    }
                }
                return(false);
            }
        });


        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
        PreferenceCategory textPref = (PreferenceCategory) findPreference("exportInfo");
        textPref.setTitle(this.getString(R.string.export_info_path) + " " + externalFilesDir);
        /*android.preference.Preference buttonExportInfo = findPreference("buttonExportInfo");
        buttonExportInfo.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            File externalFilesDir = getApplicationContext().getExternalFilesDir(null);

            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Export info")
                        .setMessage("Export path " + externalFilesDir)

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setNeutralButton(android.R.string.ok, null)
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        //.setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                return true;
            }
        });*/
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
