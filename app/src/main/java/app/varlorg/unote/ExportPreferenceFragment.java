package app.varlorg.unote;

import static app.varlorg.unote.NoteMain.customToastGeneric;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;

public class ExportPreferenceFragment extends PreferenceFragment {
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 2;
    private static String outputDir;
    private static PreferenceCategory exportDirCategory;
    private static android.preference.Preference exportDirSelect;

    private static Uri uri;
    private void openDirectory() {
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //intent.setType("*/*");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE );
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    //based on https://github.com/ltguillaume/droidshows/blob/main/src/nl/asymmetrics/droidshows/DroidShows.java
    private Comparator<File> filesComperator = new Comparator<File>() {
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
        new AlertDialog.Builder(getActivity().getApplicationContext())
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
                                NotesBDD noteBdd = new NotesBDD(getActivity().getApplicationContext());
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
        AlertDialog.Builder filePicker = new AlertDialog.Builder(getActivity())
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
                    SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("output_backup_dir", folderString);
                    editor.commit();
                    Log.d(BuildConfig.APPLICATION_ID, "backup " + folderString);
                    outputDir = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("output_backup_dir", getContext().getExternalFilesDir(null).toString());
                    exportDirSelect.setSummary(getContext().getString(R.string.export_info_path) + " " + outputDir );
                    onCreate(null);
                }
            });
        filePicker.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(BuildConfig.APPLICATION_ID, "onActivityResult");
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            Activity activity = getActivity();
            Context applicationContext = activity.getApplicationContext();
            outputDir = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("output_backup_dir", applicationContext.getExternalFilesDir(null).toString());
            exportDirSelect.setSummary(applicationContext.getString(R.string.export_info_path) + " " + outputDir );
            String folderString = data.getData().toString();
            Log.d(BuildConfig.APPLICATION_ID, "onActivityResult if " + data.getData().toString());
            SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(applicationContext);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("output_backup_dir", folderString);
            editor.commit();
            Log.d(BuildConfig.APPLICATION_ID, "backup " + folderString);
        }
    }
    void customToast(String s){
        Activity activity = getActivity();
        Context applicationContext = activity.getApplicationContext();
        customToastGeneric(applicationContext, applicationContext.getResources(), s);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_export);
        Activity activity = getActivity();
        Context applicationContext = activity.getApplicationContext();
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        android.preference.Preference button = findPreference("buttonExport");
        button.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(android.preference.Preference arg0)
            {
                NotesBDD noteBdd = new NotesBDD(null);
                String path      = noteBdd.exportDB(applicationContext);
                if (path != null)
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(applicationContext.getString(R.string.toast_export_db) + " " + path + " ! ");
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
                Intent restoreActivity = new Intent(applicationContext, RestoreDbActivity.class);
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
                NotesBDD noteBdd = new NotesBDD(applicationContext);
                noteBdd.open();
                String path      = noteBdd.exportCSV(applicationContext);
                noteBdd.close();
                if (path != null)
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(applicationContext.getString(R.string.toast_export_db) + " " + path + " ! ");
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
                NotesBDD noteBdd = new NotesBDD(applicationContext);
                noteBdd.open();
                String path = noteBdd.exportAllNotes(applicationContext);
                noteBdd.close();
                if (path != null)
                {
                    if ( pref.getBoolean("pref_notifications", true)) {
                        customToast(applicationContext.getString(R.string.toast_export_all_notes) + " " + path + " ! ");
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
/*
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
                filePicker(outputDir, true);*/
            return true;
        });

        File externalFilesDir = applicationContext.getExternalFilesDir(null);
        exportDirCategory = (PreferenceCategory) findPreference("exportInfo");
        //File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        outputDir = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("output_backup_dir", applicationContext.getExternalFilesDir(null).toString());
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
                            intent.setData(Uri.parse(String.format("package:%s", applicationContext.getPackageName())));
                            startActivity(intent);
                                /*final int takeFlags = intent.getFlags()
                                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);*/
                        } catch (Exception ex){
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(intent);
                        }
                    }
                }
                //openDirectory();
                String outputDir = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("output_backup_dir", applicationContext.getExternalFilesDir(null).toString());
                filePicker(outputDir, false);
                outputDir = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("output_backup_dir", applicationContext.getExternalFilesDir(null).toString());
                exportDirSelect.setSummary(applicationContext.getString(R.string.export_info_path) + " " + outputDir );

                return true;
            }
        });
        exportDirSelect.setSummary(this.getString(R.string.export_path_select_summary) + " " + outputDir);

    }
}
