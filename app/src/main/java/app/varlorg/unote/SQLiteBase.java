package app.varlorg.unote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SQLiteBase extends SQLiteOpenHelper
{
    private static final String TABLE_NOTES = "table_notes";
    private static final String COL_ID = "ID";
    private static final int NUM_COL_ID = 0;
    private static final String COL_NOTE = "Note";
    private static final int NUM_COL_ISBN = 1;
    private static final String COL_TITRE = "Titre";
    private static final int NUM_COL_TITRE = 2;
    private static final String COL_DATECREATION = "Date_creation";
    private static final int NUM_COL_DATECREATION = 3;
    private static final String COL_DATEMODIFICATION = "Date_modification";
    private static final int NUM_COL_DATEMODIFICATION = 4;
    
    private static final String CREATE_BDD = "CREATE TABLE " + TABLE_NOTES + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_NOTE + " TEXT NOT NULL, "
            + COL_TITRE + " TEXT NOT NULL, " + COL_DATECREATION + " TEXT NOT NULL, " + COL_DATEMODIFICATION +  " TEXT NOT NULL);";

    public SQLiteBase(Context context, String name, CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //on créé la table à partir de la requête écrite dans la variable CREATE_BDD
        db.execSQL(CREATE_BDD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //supprimer la table et de la recréer
        //comme ça lorsque je change la version les id repartent de 0
        db.execSQL("DROP TABLE " + TABLE_NOTES + ";");
        onCreate(db);
    }

}
