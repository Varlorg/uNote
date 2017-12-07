package app.varlorg.unote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class SQLiteBase extends SQLiteOpenHelper
{
    private static final String TABLE_NOTES = "table_notes";
    private static final String COL_ID = "ID";
    private static final String COL_NOTE = "Note";
    private static final String COL_TITRE = "Titre";
    private static final String COL_DATECREATION = "Date_creation";
    private static final String COL_DATEMODIFICATION = "Date_modification";
    private static final String COL_PASSWORD = "password";

    private static final String TEXT_NOT_NULL = " TEXT NOT NULL, ";
    private static final String CREATE_BDD = "CREATE TABLE " + TABLE_NOTES + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_NOTE + TEXT_NOT_NULL
            + COL_TITRE + TEXT_NOT_NULL + COL_DATECREATION + TEXT_NOT_NULL + COL_DATEMODIFICATION +  TEXT_NOT_NULL  + COL_PASSWORD  + " VARCHAR(41) );";

    public SQLiteBase(Context context, String name, CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_BDD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Create password column when upgrading old version ( < 1.1 )
        if (oldVersion < 2 )
            db.execSQL("ALTER TABLE " +  TABLE_NOTES + " ADD COLUMN " + COL_PASSWORD +" VARCHAR(41);");
    }
}
