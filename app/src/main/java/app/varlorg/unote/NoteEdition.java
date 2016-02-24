package app.varlorg.unote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class NoteEdition extends Activity
{
    final String EXTRA_TITLE = "TitreNoteEdition";
    final String EXTRA_NOTE = "NoteEdition";
    final String EXTRA_EDITION = "edition";
    final String EXTRA_ID = "id";
    boolean edit = false;
    int id = 0;
    SharedPreferences pref;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean("pref_theme",false) == false) {
            setTheme(android.R.style.Theme_DeviceDefault);
        } else {
            setTheme(android.R.style.Theme_DeviceDefault_Light);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noteedition);

        EditText titre = (EditText) findViewById(R.id.TitreNoteEdition);
        EditText note = (EditText) findViewById(R.id.NoteEdition);

        Intent intent = getIntent();
        if (intent != null)
        {
            titre.setText(intent.getStringExtra(EXTRA_TITLE));
            note.setText(intent.getStringExtra(EXTRA_NOTE));
            edit = intent.getBooleanExtra(EXTRA_EDITION,false);
            id = intent.getIntExtra(EXTRA_ID,0);
         }

        titre.setTextSize(Integer.parseInt(pref.getString("pref_sizeNote", "16")));
        note.setTextSize(Integer.parseInt(pref.getString("pref_sizeNote", "16")));

    }

    public void save(View v)
    {

        EditText titreElt = (EditText)findViewById(R.id.TitreNoteEdition);
        String 	titre = titreElt.getText().toString();
        EditText note = (EditText)findViewById(R.id.NoteEdition);
        String contenu = note.getText().toString();

        NotesBDD noteBdd = new NotesBDD(this);

        Note n = new Note(titre, contenu);
        noteBdd.open();
        if( edit == false){
        	noteBdd.insertNote(n);
        }
        else
        {
        	noteBdd.updateNote(id,n);
        }
        
        Note noteFromBdd = noteBdd.getNoteWithTitre(n.getTitre());
        if(noteFromBdd != null) {
            if( edit == false){
            	Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
            }
            else {
            	Toast.makeText(this, "Note updated", Toast.LENGTH_LONG).show();
            }
        }
        else {
        	Toast.makeText(this, "Failed to save", Toast.LENGTH_LONG).show();
        }

        noteBdd.close();
        this.finish();
        Intent intent = new Intent(this, NoteMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void quit(View v)
    {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (pref.getBoolean("pref_cancel",false) == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Cancel modification")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            NoteEdition.this.finish();
                            Intent intent = new Intent(NoteEdition.this, NoteMain.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }else {
            this.finish();
            Intent intent = new Intent(this, NoteMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }
}
