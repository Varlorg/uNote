package app.varlorg.unote;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;

public class Preference extends PreferenceActivity {
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 2;
    private static Uri uri;
    private static PreferenceCategory exportDirCategory;
    private static String outputDir;
    private android.preference.Preference exportDirSelect;
    Bundle savedInstanceState;

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
        this.savedInstanceState = savedInstanceState;
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
                //openDirectory();
                //return(true);
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

        android.preference.Preference importFromCSV = findPreference("importFromCSV");
        importFromCSV.setOnPreferenceClickListener(arg0 -> {
            Uri uri;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if(!Environment.isExternalStorageManager()){
                    try {
                        //uri = Environment.getExternalStoragePublicDirectory();
                        uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                        startActivity(intent);
                        final int takeFlags = intent.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception ex){
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                    }
                }
            }
            String outputDir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("output_backup_dir", getApplicationContext().getExternalFilesDir(null).toString());
            filePicker(outputDir, true);
            return true;
        });

        File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
        exportDirCategory = (PreferenceCategory) findPreference("exportInfo");
        //File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        outputDir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("output_backup_dir", getApplicationContext().getExternalFilesDir(null).toString());
        exportDirCategory.setTitle(this.getString(R.string.export_info_path));

        exportDirSelect = findPreference("exportDirSelect");
        exportDirSelect.setSummary(this.getString(R.string.export_path_select_summary) + " " + outputDir);
        exportDirSelect.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                Uri uri;

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    if(!Environment.isExternalStorageManager()){
                        try {
                            //uri = Environment.getExternalStoragePublicDirectory();
                            uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                            startActivity(intent);
                            final int takeFlags = intent.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (Exception ex){
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(intent);
                        }
                    }
                }
                //openDirectory();
                String outputDir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("output_backup_dir", getApplicationContext().getExternalFilesDir(null).toString());
                filePicker(outputDir, false);

                return true;
            }
        });
        exportDirSelect.setSummary(this.getString(R.string.export_path_select_summary) + " " + outputDir);

    }
    private void openDirectory() {
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE );
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT );
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    //based on https://github.com/ltguillaume/droidshows/blob/main/src/nl/asymmetrics/droidshows/DroidShows.java
    private static Comparator<File> filesComperator = new Comparator<File>() {
        public int compare(File f1, File f2) {
            if (f1.isDirectory() && !f2.isDirectory())
                return 1;
            if (f2.isDirectory() && !f1.isDirectory())
                return -1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    };
    private File[] dirContents(File folder, final boolean showFiles)  {
        if (folder.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File file = new File(dir.getAbsolutePath() + File.separator + filename);
                    if (showFiles)
                        return file.isDirectory()
                                || file.isFile() ;//&& file.getName().toLowerCase().indexOf("droidshows.db") == 0;
                    else
                        return file.isDirectory();
                }
            };
            File[] list = folder.listFiles(filter);
            Log.d(BuildConfig.APPLICATION_ID, "showFiles " + showFiles);
            Log.d(BuildConfig.APPLICATION_ID, "folder " + folder);

            if (list != null) {
                Log.d(BuildConfig.APPLICATION_ID, "listFiles " + list.length);
                Arrays.sort(list, filesComperator);
                for (File  l : list  ) {
                    Log.d(BuildConfig.APPLICATION_ID, "list " + l.toString());
                }
            }
            return list == null ? new File[0] : list;
        } else {
            return new File[0];
        }
    }
    private void confirmRestore(final String backupFile) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_import_csv))
                .setMessage(getString(R.string.dialog_import_csv_msg) + " " + backupFile)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int nbNote = 0;
                        Log.d(BuildConfig.APPLICATION_ID, "confirmRestore  " + backupFile);
                        try {
                            boolean first = true;
                            for( String[] l : CSVUtils.read(backupFile,',', '"'))
                            {
                                Log.d(BuildConfig.APPLICATION_ID, "CSVUtils read  " + l.length);
                                for(String elt: l ){
                                    Log.d(BuildConfig.APPLICATION_ID, "CSVUtils read  " + elt);
                                }
                                NotesBDD noteBdd = new NotesBDD(getApplicationContext());
                                noteBdd.open();
                                if(first) { // 1st line is "TITLE","DATE_CREATION","DATE_MODIFICATION", "NOTE"
                                    first = false;
                                }
                                else {
                                    long rc = noteBdd.insertNote(new Note(l[0], l[l.length - 1]));
                                    if(rc == -1 ){
                                        Log.d(BuildConfig.APPLICATION_ID, "CSVUtils insert error  " + String.join("-", l));
                                        customToast("Error");
                                    }else {
                                        nbNote += 1;
                                    }
                                }
                                noteBdd.close();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        customToast(nbNote + getString(R.string.toast_import_csv_added)); // " note(s) added"
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    private void filePicker(final String folderString, final boolean restoring) {
        File folder = new File(folderString);
        //File[] tempDirList = dirContents(folder, false);
        File[] tempDirList = dirContents(folder, restoring);
        int showParent = (folderString.equals(Environment.getExternalStorageDirectory().getPath()) ? 0 : 1);
        File[] dirList = new File[tempDirList.length + showParent];
        String[] dirNamesList = new String[tempDirList.length + showParent];
        if (showParent == 1) {
            dirList[0] = folder.getParentFile();
            dirNamesList[0] = "..";
        }
        for(int i = 0; i < tempDirList.length; i++) {
            dirList[i + showParent] = tempDirList[i];
            dirNamesList[i + showParent] = tempDirList[i].getName();
            if (restoring && tempDirList[i].isFile())
                dirNamesList[i + showParent] += " ("+ SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT).format(tempDirList[i].lastModified()) +")";
        }
        AlertDialog.Builder filePicker = new AlertDialog.Builder(this)
                .setTitle(folder.toString())
                .setItems(dirNamesList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File chosenFile = dirList[which];
                        if (chosenFile.isDirectory()) {
                            filePicker(chosenFile.toString(), restoring);
                        } else if (restoring) {
                            Log.d(BuildConfig.APPLICATION_ID, "confirmRestore " + chosenFile.toString());
                            confirmRestore(chosenFile.toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        if (!restoring)
            filePicker.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //backup(false, folderString);
                    uri = Uri.parse(folderString); //Uri.fromFile(new File(STRING));
                    //PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("output_backup_dir", folderString).apply();
                    SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("output_backup_dir", folderString);
                    editor.commit();
                    Log.d(BuildConfig.APPLICATION_ID, "backup " + folderString);
                    outputDir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("output_backup_dir", getApplicationContext().getExternalFilesDir(null).toString());
                    exportDirSelect.setSummary(Preference.this.getString(R.string.export_info_path) + " " + outputDir );
                    onCreate(null);
                }
            });
        filePicker.show();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
//            uri = null;
//            if (resultData != null) {
//                uri = resultData.getData();
//                /*final int takeFlags = intent.getFlags()
//                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                getContentResolver().takePersistableUriPermission(uri, takeFlags);*/
//                // Enregistrez l'URI pour une utilisation ultérieure
//                Log.d(BuildConfig.APPLICATION_ID, "uri " + uri );
//                try {
//                    //String docId = DocumentsContract.getTreeDocumentId(uri);
//                    //exportDirSelect.setSummary(Preference.this.getString(R.string.export_info_path) + " " + docId  );
//                    //exportDirSelect.setSummary(Preference.this.getString(R.string.export_info_path)  );
//                    exportDirSelect.setSummary(Preference.this.getString(R.string.export_info_path) + " " + getContentResolver().openInputStream(uri)  );
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }

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
