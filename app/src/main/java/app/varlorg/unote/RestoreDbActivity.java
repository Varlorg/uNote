package app.varlorg.unote;

/**
 * Inspired by the one from SimplyDo
 */

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RestoreDbActivity extends ListActivity {
    private static final int DIALOG_RESTORE_WARN = 300;
    private static final String EXTENSION        = ".db";
    private static final String NOM_BDD          = "notes.db";

    private ArrayAdapter <NameOnlyFile> adapter;
    private AlertDialog.Builder restoreWarningBuilder;
    private NameOnlyFile restoreFile;
    private FilenameFilter restoreFilenameFilter;
    private Comparator <NameOnlyFile> comparator;

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

        adapter = new ArrayAdapter <>(this, R.layout.restore_entry, R.id.RestoreName);

        refresh();

        setListAdapter(adapter);
        registerForContextMenu(getListView());

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
    protected Dialog onCreateDialog(int id)
    {
        if (id == DIALOG_RESTORE_WARN)
        {
            return(restoreWarningBuilder.create());
        }

        return(super.onCreateDialog(id));
    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle((this.getString(R.string.dialog_backup_menu)));
        menu.add(0, v.getId(), 0, (this.getString(R.string.dialog_backup_menu_deletion)));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo aInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        boolean res = false;
        if (item.getTitle().equals((this.getString(R.string.dialog_backup_menu_deletion))))
        {
            restoreFile = adapter.getItem(aInfo.position);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
            .setTitle(this.getString(R.string.dialog_delete_backup) + " " + adapter.getItem(aInfo.position).toString())
            .setMessage(this.getString(R.string.dialog_delete_msg))
            .setPositiveButton(this.getString(R.string.dialog_delete_yes), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    if (!restoreFile.getFile().delete())
                    {
                        (Toast.makeText(RestoreDbActivity.this, RestoreDbActivity.this.getString(R.string.toast_backup_deleted_error), Toast.LENGTH_LONG)).show();
                    }
                    else
                    {
                        refresh();
                        (Toast.makeText(RestoreDbActivity.this, RestoreDbActivity.this.getString(R.string.toast_backup_deleted), Toast.LENGTH_LONG)).show();
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
        return(res);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        restoreFile = adapter.getItem(position);

        try
        {
            // test restore file
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                restoreFile.getFile().getPath(), null, SQLiteDatabase.OPEN_READONLY);
            db.close();

            // Dialog: This will overwrite the existing items, continue?
            showDialog(DIALOG_RESTORE_WARN);
        }
        catch (Exception e)
        {
            Toast t = Toast.makeText(this, R.string.restoreToastInvalidDB, Toast.LENGTH_LONG);
            t.show();
        }
    }

    private void refresh()
    {
        File backupDirectory = new File(
            Environment.getExternalStorageDirectory(),
            BuildConfig.APPLICATION_ID);

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
            Toast.makeText(
                this,
                R.string.restoreToastMountProblem,
                Toast.LENGTH_LONG
                ).show();
            return;
        }

        // backup old file
        File    dbFile    = getDatabasePath(NOM_BDD);
        File    dbBakFile = getDatabasePath(NOM_BDD + ".bak");
        boolean moved     = dbFile.renameTo(dbBakFile);
        if (!moved)
        {
            Toast.makeText(
                this,
                R.string.restoreToastUnableToMove,
                Toast.LENGTH_LONG
                ).show();
            return;
        }

        try
        {
            // copy new file into place
            NotesBDD noteBdd = new NotesBDD(null);
            noteBdd.importDB(restoreFile.getFile());

            // delete backup
            dbBakFile.delete();
        }
        catch (Exception e)
        {
            // put the old database back
            dbFile.delete();
            dbBakFile.renameTo(dbFile);

            Toast.makeText(
                this,
                R.string.restoreToastCopyFailed,
                Toast.LENGTH_LONG
                ).show();
            return;
        }

        Toast.makeText(
            this,
            (new NameOnlyFile(restoreFile.getFile())).toString() + " " +
            getResources().getString(R.string.restoreToastRestoreFinished),
            Toast.LENGTH_LONG
            ).show();

        finish();
    }

    private static class NameOnlyFile
    {
        private File file;

        public NameOnlyFile(File f)
        {
            file = f;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString()
        {
            String name = file.getName();

            return(name.substring(0, name.length() - EXTENSION.length()));
        }
    }
}
