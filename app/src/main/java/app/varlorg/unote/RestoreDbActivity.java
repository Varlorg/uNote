package app.varlorg.unote;

/**
 * Inspired by the one from SimplyDo
 */

import static app.varlorg.unote.NoteMain.POPUP_TEXTSIZE_FACTOR;
import static app.varlorg.unote.NoteMain.customToastGeneric;
import static app.varlorg.unote.NoteMain.setUi;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Comparator;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RestoreDbActivity extends ListActivity {
    private static final String EXTENSION        = ".db";
    private static final String NOM_BDD          = "notes.db";
    private static final String TABLE_NOTES          = "table_notes";
    private static final String COL_ID               = "ID";
    private ArrayAdapter <NameOnlyFile> adapter;
    private AlertDialog.Builder restoreWarningBuilder;
    private NameOnlyFile restoreFile;
    private FilenameFilter restoreFilenameFilter;
    private Comparator <NameOnlyFile> comparator;
    private SharedPreferences pref;
    int textSize;
    boolean toast_enabled;
    void customToast(String s){
        customToastGeneric(RestoreDbActivity.this, RestoreDbActivity.this.getResources(), s);
    }
    public RestoreDbActivity()
    {
        restoreFilenameFilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                return(filename.endsWith(EXTENSION));
            }
        };

        comparator = new Comparator <NameOnlyFile>()
        {
            @Override
            public int compare(NameOnlyFile object1, NameOnlyFile object2)
            {
                return(object2.toString().compareTo(object1.toString()));
            }
        };
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        setUi(this, pref, getApplicationContext(), getWindow());

        textSize = Integer.parseInt(pref.getString("pref_sizeNote", "18"));
        if ( textSize == -1 )
        {
            textSize = Integer.parseInt(pref.getString("pref_sizeNote_custom", "18"));
        }
        toast_enabled = pref.getBoolean("pref_notifications", true);
        adapter = new ArrayAdapter <NameOnlyFile>(this, R.layout.restore_entry, R.id.RestoreName) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Cast the list view each item as text view
                TextView item = (TextView) super.getView(position,convertView,parent);
                // Change the item text size
                item.setTextSize((int) (NoteMain.TOAST_TEXTSIZE_FACTOR * textSize));
                // return the view
                return item;
            }
        };

        refresh();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
        String outputDir = pref.getString("output_backup_dir", externalFilesDir.toString());
        File        sd            = new File(outputDir);
        TextView tv = new TextView(this);
        tv.setText(sd.toString() + "/*" + EXTENSION);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextSize((int) (0.8 * textSize));
        setListAdapter(adapter);
        registerForContextMenu(getListView());
        getListView().addHeaderView(tv, null, false);

        restoreWarningBuilder = new AlertDialog.Builder(this);
        restoreWarningBuilder.setMessage(R.string.restoreWarnMessage)
        .setCancelable(true)
        .setTitle(R.string.restoreWarnTitle)
        .setPositiveButton(R.string.restoreWarnPositive, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                doRestore();
                dialog.cancel();
            }
        })
        .setNegativeButton(R.string.restoreWarnNegative, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });
    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle((this.getString(R.string.dialog_backup_menu)));
        menu.add(0, v.getId(), 0, (this.getString(R.string.dialog_backup_menu_deletion)));
        menu.add(0, v.getId(), 0, (this.getString(R.string.dialog_backup_rename_valid)));
        menu.add(0, v.getId(), 0, (this.getString(R.string.menu_detail)));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo aInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        boolean res = false;
        restoreFile = adapter.getItem(aInfo.position - 1);

        if (item.getTitle().equals((this.getString(R.string.menu_detail))))
        {
            int db_size = 0;
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    restoreFile.getFile().getPath(), null, SQLiteDatabase.OPEN_READONLY);
            try {
                Cursor c = db.rawQuery(String.format("SELECT count(%s) FROM %s ;", COL_ID, TABLE_NOTES),
                        null);
                c.moveToFirst();
                db_size = c.getInt(0);
                c.close();
            } catch (Exception e) {

            }
            db.close();

            //SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
            //String           date = df.format(restoreFile.getFile().lastModified());
            String noteDetails = "<b>" +   restoreFile + "</b><br/>" +
                    "<b>" +  this.getString(R.string.detail_number_notes) + ":</b>  " + db_size +
                //"<br/><i>" + this.getString(R.string.detail_modified) + " " + date + "</i>";
                "<br/><i>" + this.getString(R.string.detail_modified) + " " + new Date(restoreFile.getFile().lastModified()) + "</i>";


            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(this.getString(R.string.menu_detail));
            alertDialog.setMessage(Html.fromHtml(noteDetails));
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    // Do nothing
                }
            });

            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
            ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
        }
        else if (item.getTitle().equals((this.getString(R.string.dialog_backup_menu_deletion))))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
            .setTitle(this.getString(R.string.dialog_delete_backup) + " " + restoreFile.toString())
            .setMessage(this.getString(R.string.dialog_delete_msg))
            .setPositiveButton(this.getString(R.string.dialog_delete_yes), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    if (!restoreFile.getFile().delete())
                    {
                        if ( toast_enabled ){
                            customToast(RestoreDbActivity.this.getString(R.string.toast_backup_deleted_error));
                        }
                    }
                    else
                    {
                        refresh();
                        if ( toast_enabled ){
                            customToast(RestoreDbActivity.this.getString(R.string.toast_backup_deleted));
                        }
                    }
                }
            })
            .setNegativeButton(this.getString(R.string.dialog_delete_no), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.cancel();
                }
            })
            .show();
        }
        else if (item.getTitle().equals((this.getString(R.string.dialog_backup_rename_valid))))
        {
            pref = PreferenceManager.getDefaultSharedPreferences(this);
            int text_color;
            if (!pref.getBoolean("pref_theme", false))
            {
                text_color = getResources().getColor(android.R.color.white);
            }
            else
            {
                text_color =getResources().getColor(android.R.color.black);
            }

            Log.d(BuildConfig.APPLICATION_ID, "dialog_backup_menu_rename");
            final EditText input = new EditText(getApplicationContext());
            LinearLayout.LayoutParams lp    = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setTextSize(textSize);
            input.setTextColor(text_color);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            //AlertDialog.Builder builder = new AlertDialog.Builder(this, theme_used);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle(getApplicationContext().getString(R.string.dialog_backup_rename) + " " + restoreFile.toString())
                    .setMessage(getApplicationContext().getString(R.string.dialog_backup_rename_msg))
                    .setView(input)
                    .setPositiveButton(getApplicationContext().getString(R.string.dialog_backup_rename_valid), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            String newDbName = input.getText().toString() + ".db";
                            Log.d(BuildConfig.APPLICATION_ID, "dialog_backup_menu_rename - " + newDbName);
                            Log.d(BuildConfig.APPLICATION_ID, "dialog_backup_menu_rename - " + restoreFile.getFile().getParentFile() );
                            File newDbNamefile = new File(restoreFile.getFile().getParentFile(), newDbName);
                            boolean isMoved = restoreFile.getFile().renameTo(newDbNamefile);
                            if (!isMoved) {
                                if ( toast_enabled ){
                                    customToast(RestoreDbActivity.this.getString(R.string.toast_backup_rename_error));
                                }
                            }
                            else {
                                refresh();
                                if ( toast_enabled ){
                                    customToast(RestoreDbActivity.this.getString(R.string.toast_backup_rename_success));
                                }
                            }
                        }
                    })
                    .setNeutralButton(getApplicationContext().getString(R.string.dialog_add_pwd_cancel), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    });
            //    .show();
            final AlertDialog alertDialog = builder.create();
            // Bug on Lollipop when large text size to display the 3 buttons
            // https://stackoverflow.com/questions/27187353/dialog-buttons-with-long-text-not-wrapping-squeezed-out-material-theme-on-an
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    try {
                        LinearLayout linearLayout = (LinearLayout) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).getParent();
                        int wPos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).getWidth();
                        int wNeu = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).getWidth();
                        if (linearLayout != null && wPos + wNeu > linearLayout.getWidth()) {
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            linearLayout.setGravity(Gravity.RIGHT);
                        }
                    } catch (Exception ignored) {

                    }
                }
            });
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(Math.min(36,(int)(textSize * POPUP_TEXTSIZE_FACTOR)));
            alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(Math.min(36,(int)(textSize * POPUP_TEXTSIZE_FACTOR)));
            ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
        }
        return(res);
    }
    public boolean isValidSQLite(String dbPath) {
        File file = new File(dbPath);

        if (!file.exists() || !file.canRead()) {
            return false;
        }

        try {
            FileReader fr = new FileReader(file);
            char[] buffer = new char[16];

            fr.read(buffer, 0, 16);
            String str = String.valueOf(buffer);
            fr.close();

            return str.equals("SQLite format 3\u0000");

        } catch (Exception e) {
            Log.d(BuildConfig.APPLICATION_ID, "isValidSQLite failed " + e );
            return false;
        }
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        restoreFile = adapter.getItem(position - 1);

        if (isValidSQLite(restoreFile.getFile().getPath()))
        {
            // test restore file
           /* SQLiteDatabase db = SQLiteDatabase.openDatabase(
                restoreFile.getFile().getPath(), null, SQLiteDatabase.OPEN_READONLY);
            db.close();*/

            // Dialog: This will overwrite the existing items, continue?
            AlertDialog adRestoreWarningBuilder = restoreWarningBuilder.create();
            adRestoreWarningBuilder.setTitle(this.getString(R.string.restoreWarnTitle) + " " + restoreFile.getFile().getName() );
            // Bug on Lollipop when large text size to display the 3 buttons
            // https://stackoverflow.com/questions/27187353/dialog-buttons-with-long-text-not-wrapping-squeezed-out-material-theme-on-an
            adRestoreWarningBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    try {
                        LinearLayout linearLayout = (LinearLayout) adRestoreWarningBuilder.getButton(DialogInterface.BUTTON_POSITIVE).getParent();
                        int wPos = adRestoreWarningBuilder.getButton(DialogInterface.BUTTON_POSITIVE).getWidth();
                        int wNeg = adRestoreWarningBuilder.getButton(DialogInterface.BUTTON_NEGATIVE).getWidth();
                        if (linearLayout != null && wPos + wNeg > linearLayout.getWidth()) {
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            linearLayout.setGravity(Gravity.RIGHT);
                        }
                    } catch (Exception ignored) {

                    }
                }
            });
            adRestoreWarningBuilder.show();
            adRestoreWarningBuilder.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
            adRestoreWarningBuilder.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
            ((TextView)adRestoreWarningBuilder.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
        }
        else
        //catch (Exception e)
        {
            //Log.d(BuildConfig.APPLICATION_ID, "Restore db failed " + e );
            Log.d(BuildConfig.APPLICATION_ID, "Restore db failed "  );
            if ( toast_enabled ){
                customToast(RestoreDbActivity.this.getString(R.string.restoreToastInvalidDB));
            }
        }
    }

    private void refresh()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
        String outputDir = pref.getString("output_backup_dir", externalFilesDir.toString());
        File backupDirectory = new File(outputDir);

        adapter.clear();
        if (backupDirectory.isDirectory())
        {
            File[] files = backupDirectory.listFiles(restoreFilenameFilter);
            for (File f : files)
            {
                adapter.add(new NameOnlyFile(f));
            }
            adapter.sort(comparator);
        }
        adapter.notifyDataSetChanged();
    }

    private void doRestore()
    {
        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state))
        {
            if ( toast_enabled ){
                customToast(RestoreDbActivity.this.getString(R.string.restoreToastMountProblem));
            }
            return;
        }

        // backup old file
        File    dbFile    = getDatabasePath(NOM_BDD);
        File    dbBakFile = getDatabasePath(NOM_BDD + ".bak");
        boolean moved     = dbFile.renameTo(dbBakFile);
        if (!moved)
        {
            if ( toast_enabled ){
                customToast(RestoreDbActivity.this.getString(R.string.restoreToastUnableToMove));
            }
            return;
        }

        try
        {
            // copy new file into place
            NotesBDD noteBdd = new NotesBDD(getApplicationContext());
            noteBdd.importDB(restoreFile.getFile());

            // delete backup
            dbBakFile.delete();
        }
        catch (Exception e)
        {
            // put the old database back
            dbFile.delete();
            dbBakFile.renameTo(dbFile);

            if ( toast_enabled ){
                customToast(RestoreDbActivity.this.getString(R.string.restoreToastCopyFailed));
            }
            return;
        }

        if ( toast_enabled ){
            customToast(RestoreDbActivity.this.getString(R.string.restoreToastRestoreFinished));
        }

        finish();
    }

    private static class NameOnlyFile
    {
        private File file;

        public NameOnlyFile(File f)
        {
            file = f;
        }

        public File getFile()
        {
            return(file);
        }

        @Override
        public String toString()
        {
            String name = file.getName();

            return(name.substring(0, name.length() - EXTENSION.length()));
        }
    }
}
