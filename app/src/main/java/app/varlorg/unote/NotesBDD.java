package app.varlorg.unote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotesBDD
{
    private static final int VERSION_BDD = 2;
    private static final String NOM_BDD  = "notes.db";

    private static final String TABLE_NOTES           = "table_notes";
    private static final String COL_ID                = "ID";
    private static final int NUM_COL_ID               = 0;
    private static final String COL_NOTE              = "Note";
    private static final int NUM_COL_ISBN             = 1;
    private static final String COL_TITRE             = "Titre";
    private static final int NUM_COL_TITRE            = 2;
    private static final String COL_DATECREATION      = "Date_creation";
    private static final int NUM_COL_DATECREATION     = 3;
    private static final String COL_DATEMODIFICATION  = "Date_modification";
    private static final int NUM_COL_DATEMODIFICATION = 4;
    private static final String COL_PASSWORD          = "password";
    private static final int NUM_COL_PASSWORD         = 5;
    private static final String SQL_ORDER             = " ORDER BY ";
    private static final String DATA_PATH             = "/data/";
    private static final String DATABASE_FOLDER       = "/databases/";

    private SQLiteDatabase bdd;

    private SQLiteBase maBaseSQLite;

    public NotesBDD(Context context)
    {
        //On créer la BDD et sa table
        maBaseSQLite = new SQLiteBase(context, NOM_BDD, null, VERSION_BDD);
    }

    public void open()
    {
        //on ouvre la BDD en écriture
        bdd = maBaseSQLite.getWritableDatabase();
    }

    public void close()
    {
        //on ferme l'accès à la BDD
        if (bdd != null)
        {
            bdd.close();
        }
    }

    public SQLiteDatabase getBDD()
    {
        return(bdd);
    }

    public long insertNote(Note note)
    {
        //Création d'un ContentValues (fonctionne comme une HashMap)
        ContentValues values = new ContentValues();

        //on lui ajoute une valeur associé à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
        values.put(COL_NOTE, note.getNote());
        values.put(COL_TITRE, note.getTitre());
        values.put(COL_DATECREATION, note.getDateCreation());
        values.put(COL_DATEMODIFICATION, note.getDateModification());
        //on insère l'objet dans la BDD via le ContentValues
        return(bdd.insert(TABLE_NOTES, null, values));
    }

    public int updateNote(int id, Note note)
    {
        //La mise à jour d'une note dans la BDD fonctionne plus ou moins comme une insertion
        //il faut simple préciser quelle note on doit mettre à jour grâce à l'ID
        ContentValues values = new ContentValues();

        values.put(COL_NOTE, note.getNote());
        values.put(COL_TITRE, note.getTitre());
        values.put(COL_DATEMODIFICATION, note.getDateModification());
        return(bdd.update(TABLE_NOTES, values, COL_ID + " = " + id, null));
    }

    public int updatePassword(int id, String pw)
    {
        ContentValues values = new ContentValues();

        values.put(COL_PASSWORD, pw);
        return(bdd.update(TABLE_NOTES, values, COL_ID + " = " + id, null));
    }

    public int removeNoteWithID(int id)
    {
        //Suppression d'un livre de la BDD grâce à l'ID
        return(bdd.delete(TABLE_NOTES, COL_ID + " = " + id, null));
    }

    public Note getNoteWithTitre(String titre)
    {
        //Récupère dans un Cursor les valeur correspondant à une note contenue dans la BDD (ici on sélectionne la note grâce à son titre)
        Cursor c = bdd.query(TABLE_NOTES, new String[] { COL_ID, COL_NOTE, COL_TITRE, COL_DATECREATION, COL_DATEMODIFICATION }, COL_TITRE + " LIKE ? ", new String[] { titre }, null, null, null);

        return(cursorToNote(c));
    }

    public Note getNoteWithId(int id)
    {
        //Récupère dans un Cursor les valeur correspondant à un livre contenu dans la BDD (ici on sélectionne le livre grâce à son titre)
        Cursor c = bdd.query(TABLE_NOTES, new String[] { COL_ID, COL_NOTE, COL_TITRE, COL_DATECREATION, COL_DATEMODIFICATION }, COL_ID + " LIKE " + id + "", null, null, null, null);

        return(cursorToNote(c));
    }

    //Cette méthode permet de convertir un cursor en une note
    private Note cursorToNote(Cursor c)
    {
        //si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0)
        {
            return(null);
        }

        //Sinon on se place sur le premier élément
        c.moveToFirst();
        //On créé un livre
        Note note = new Note();
        //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
        note.setId(c.getInt(NUM_COL_ID));
        note.setNote(c.getString(NUM_COL_ISBN));
        note.setTitre(c.getString(NUM_COL_TITRE));
        note.setDateCreation(c.getString(NUM_COL_DATECREATION));
        note.setDateModification(c.getString(NUM_COL_DATEMODIFICATION));
        //On ferme le cursor
        c.close();

        //On retourne la note
        return(note);
    }

    public List <Note> getAllNotes(int tri, boolean ordre)
    {
        List <Note> noteList    = new ArrayList <>();
        String      selectQuery = "SELECT  * FROM " + TABLE_NOTES + SQL_ORDER;
        // Select All Query
        if (tri == 1)
        {
            selectQuery += COL_DATECREATION + " ";
        }
        else if (tri == 2)
        {
            selectQuery += COL_DATEMODIFICATION + " ";
        }
        else if (tri == 3)
        {
            selectQuery += COL_TITRE + " ";
        }
        else
        {
            selectQuery += COL_TITRE + " COLLATE NOCASE ";
        }

        if (!ordre)
        {
            selectQuery = selectQuery + " DESC";
        }

        SQLiteDatabase db = this.maBaseSQLite.getWritableDatabase();
        return(fillListNote(db, selectQuery, noteList, null, false, false));
    }

    public List <Note> getSearchedNotes(String str, Boolean contentSearch, Boolean sensitiveSearch, int tri, boolean ordre)
    {
        List <Note> noteList = new ArrayList <>();
        // Select All Query
        SQLiteDatabase db          = this.maBaseSQLite.getWritableDatabase();
        String         selectQuery = null;
        String query_operator = "";
        if (sensitiveSearch)
        {
            query_operator = " GLOB ";
        }
        else
        {
            query_operator = " LIKE ";
        }
        selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE ";
        if (!contentSearch)
        {
            selectQuery += COL_TITRE + query_operator+" ? ";
        }
        else
        {
            selectQuery += COL_TITRE + query_operator + " ?" +
                    " OR ( " + COL_NOTE + query_operator + " ? AND " + COL_PASSWORD + " IS NULL) ";
        }
        selectQuery += SQL_ORDER;

        if (tri == 1)
        {
            selectQuery += COL_DATECREATION + " ";
        }
        else if (tri == 2)
        {
            selectQuery += COL_DATEMODIFICATION + " ";
        }
        else if (tri == 3)
        {
            selectQuery += COL_TITRE + " ";
        }
        else
        {
            selectQuery += COL_TITRE + " COLLATE NOCASE ";
        }

        if (!ordre)
        {
            selectQuery += " DESC";
        }

        return(fillListNote(db, selectQuery, noteList, str, contentSearch, sensitiveSearch));
    }

    public List <Note> fillListNote(SQLiteDatabase db, String selectQuery, List <Note> noteList, String s, boolean contentSearch, boolean sensitiveSearch)
    {
        Cursor c;
        Log.d(BuildConfig.APPLICATION_ID, "fillListNote  selectQuery " +  selectQuery);
        String query_operator = "";
        if (sensitiveSearch)
        {
            query_operator = "*";
        }
        else
        {
            query_operator = "%";
        }

        if (s != null&& contentSearch)
        {
            c = db.rawQuery(selectQuery, new String[] { query_operator + s + query_operator, query_operator + s + query_operator });
        }
        else if (s != null)
        {
            c = db.rawQuery(selectQuery, new String[] { query_operator + s + query_operator });
        }
        else
        {
            c = db.rawQuery(selectQuery, null);
        }

        // looping through all rows and adding to list
        if (c.moveToFirst())
        {
            do
            {
                Note note = new Note();
                note.setId(c.getInt(NUM_COL_ID));
                note.setNote(c.getString(NUM_COL_ISBN));
                note.setTitre(c.getString(NUM_COL_TITRE));
                note.setDateCreation(c.getString(NUM_COL_DATECREATION));
                note.setDateModification(c.getString(NUM_COL_DATEMODIFICATION));
                if (c.getString(NUM_COL_PASSWORD) != null)
                {
                    note.setPassword(c.getString(NUM_COL_PASSWORD));
                }
                // Adding contact to list
                noteList.add(note);
            } while (c.moveToNext());
        }

        // return contact list
        c.close();
        return(noteList);
    }

    // Getting notes Count

    /*public int getNotesCount()
     * {
     *  String countQuery = "SELECT  * FROM " + TABLE_NOTES;
     *  SQLiteDatabase db = this.maBaseSQLite.getReadableDatabase();
     *  Cursor cursor = db.rawQuery(countQuery, null);
     *  cursor.close();
     *
     *  // return count
     *  return cursor.getCount();
     * }*/

    public String exportDB(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        File externalFilesDir = context.getExternalFilesDir(null);
        String outputDir = pref.getString("output_backup_dir", externalFilesDir.toString());

        File        sd            = new File(outputDir);
        File        data          = Environment.getDataDirectory();
        FileChannel source        = null;
        FileChannel destination   = null;
        String      currentDBPath = DATA_PATH + BuildConfig.APPLICATION_ID + DATABASE_FOLDER + NOM_BDD;
        String      backupDBPath  = "unote_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime()) + ".db";
        File        currentDB     = new File(data, currentDBPath);

        File backupDB = new File(sd, backupDBPath);
        try {
            backupDB.createNewFile();
            // returns true, if the named file doesn't exist and was successfully created
            // returns false if the file exists => overwritten, manual action db cannot changed
        } catch (IOException e) {
            Log.e(BuildConfig.APPLICATION_ID, "IOException exportDB " + backupDB.toString(), e);
            return(null);
        }

        try (
            FileInputStream s = new FileInputStream(currentDB);
            FileOutputStream d = new FileOutputStream(backupDB);
            ) {
            source      = s.getChannel();
            destination = d.getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            Log.e(BuildConfig.APPLICATION_ID, "IOException exportDB", e);
            return(null);
        }
        return(backupDB.toString());
    }

    public String importDB(File dbToImport)
    {
        File        data          = Environment.getDataDirectory();
        FileChannel source        = null;
        FileChannel destination   = null;
        String      currentDBPath = DATA_PATH + BuildConfig.APPLICATION_ID + DATABASE_FOLDER + NOM_BDD;
        File        currentDB     = new File(data, currentDBPath);

        if (dbToImport.exists())
        {
            try (
                FileInputStream s = new FileInputStream(dbToImport);
                FileOutputStream d = new FileOutputStream(currentDB);
                ) {
                source      = s.getChannel();
                destination = d.getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
            } catch (IOException e) {
                Log.e(BuildConfig.APPLICATION_ID, "IOException importDB", e);
            }
            try {
                this.open();

                // check if column passwd exists
                Cursor cursor = bdd.rawQuery("SELECT * FROM " + TABLE_NOTES + " LIMIT 0", null);
                int index = cursor.getColumnIndex(COL_PASSWORD);
                if (index == -1) {
                    bdd.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COL_PASSWORD + " VARCHAR(41);");
                }
                this.close();
            } catch (Exception e) {
                Log.e(BuildConfig.APPLICATION_ID, "Exception importDB", e);
            }
            return(dbToImport.toString());
        }
        else
        {
            return(null);
        }
    }

    public String exportCSV(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        File externalFilesDir = context.getExternalFilesDir(null);
        String outputDir = pref.getString("output_backup_dir", externalFilesDir.toString());

        String      exportCSVFile  = "unote_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime()) + ".csv";

        File file = new File(outputDir, exportCSVFile);
        try {
            FileWriter csvWrite = new FileWriter(file,true);
            String      selectQuery = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_PASSWORD + " IS NULL ";
            if (this.maBaseSQLite == null )
                return "null";

            if (this.bdd == null )
                return "null2";
            //SQLiteDatabase db =
            //this.open();
            //db.close();
            Cursor c = this.bdd.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (c.moveToFirst())
            {
                CSVUtils.writeLine(csvWrite, Arrays.asList("TITLE",
                                "DATE_CREATION",
                                "DATE_MODIFICATION",
                                "NOTE"),
                        ',',
                        '"');
                do
                {
                    CSVUtils.writeLine(csvWrite, Arrays.asList(c.getString(NUM_COL_TITRE),
                                    c.getString(NUM_COL_DATECREATION),
                                    c.getString(NUM_COL_DATEMODIFICATION),
                                    c.getString(NUM_COL_ISBN)),
                            ',',
                            '"');

                } while (c.moveToNext());
            }

            // return contact list
            csvWrite.close();
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return(file.toString());
    }
    public String exportNote(Context context, int id, boolean exportDate, boolean exportTitle)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        File externalFilesDir = context.getExternalFilesDir(null);
        String outputDir = pref.getString("output_backup_dir", externalFilesDir.toString());
        String filename = exportNoteTo(context, id,  exportDate,  exportTitle, new File(outputDir));
        return filename;
    }
    public String exportNoteTo(Context context, int id, boolean exportDate, boolean exportTitle, File parentFolder)
    {
        Note n = this.getNoteWithId(id);
        String t = n.getTitre();

        String exportNoteFile = "unote_";
        if (exportTitle)
        {
            exportNoteFile += t.replaceAll("[\\\\/:*?\"<>|]", "_");
        }
        else {
            exportNoteFile += id ;
        }

        if (exportDate)
        {
            exportNoteFile += "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime()) ;
        }
        exportNoteFile += ".txt";

        File file = new File(parentFolder, exportNoteFile);
        try {
            FileWriter w = new FileWriter(file,false);
            if (this.maBaseSQLite == null )
                return "null";

            if (this.bdd == null )
                return "null2";

            StringBuilder sb = new StringBuilder();

            sb.append(t +"\n\n");
            sb.append(n.getNote());
            w.write(sb.toString());

            w.close();
            Log.d(BuildConfig.APPLICATION_ID, "exportNote " + file.toString());
        } catch (IOException e) {
            Log.e(BuildConfig.APPLICATION_ID, "Exception exportNoteTo", e);
        }
        return(file.toString());
    }

    public String exportAllNotes(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean exportDate = false;
        boolean exportTitle = false;
        if ( pref.getBoolean("pref_export_note_date", true))
        {
            exportDate = true;
        }
        if ( pref.getBoolean("pref_export_note_title", true))
        {
            exportTitle = true;
        }
        String      destFolder  = "unote_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
        File externalFilesDir = context.getExternalFilesDir(null);
        String outputDir = pref.getString("output_backup_dir", externalFilesDir.toString());
        File destinationPath = new File(outputDir, destFolder);
        Log.d(BuildConfig.APPLICATION_ID, "exportAllNotes "+ destinationPath);
        if( ! destinationPath.exists())
        {
            destinationPath.mkdirs();
            Log.d(BuildConfig.APPLICATION_ID, "exportNoteTo mkdir "+ destinationPath);
        }
        try {
            String      selectQuery = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + COL_PASSWORD + " IS NULL ";
            if (this.maBaseSQLite == null )
                return "null";

            if (this.bdd == null )
                return "null2";
            Cursor c = this.bdd.rawQuery(selectQuery, null);
            int noteExportedNb = 0;
            // looping through all rows and adding to list
            if (c.moveToFirst())
            {
                do
                {
                    exportNoteTo(context, c.getInt(NUM_COL_ID), exportDate, exportTitle, destinationPath);
                    noteExportedNb++;
                } while (c.moveToNext());
            }
            c.close();
            Log.d(BuildConfig.APPLICATION_ID, "noteExportedNb "+ noteExportedNb);
        } catch (Exception e) {
            Log.e(BuildConfig.APPLICATION_ID, "Exception exportAllNotes", e);
        }
        return(destinationPath.toString());
    }
}
