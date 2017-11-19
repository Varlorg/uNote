package app.varlorg.unote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
    private EditText titre;
    private EditText note;
    private TextView noteT;
    private TextView titreT;

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

        titre = (EditText) findViewById(R.id.TitreNoteEdition);
        note = (EditText) findViewById(R.id.NoteEdition);
        noteT = (TextView) findViewById(R.id.NoteEditionTitre);
        titreT = (TextView) findViewById(R.id.TitreNote);

        Intent intent = getIntent();
        if (intent != null) {
            titre.setText(intent.getStringExtra(EXTRA_TITLE));
            note.setText(intent.getStringExtra(EXTRA_NOTE));
            edit = intent.getBooleanExtra(EXTRA_EDITION, false);
            id = intent.getIntExtra(EXTRA_ID, 0);
            titre.setTag(null);
            note.setTag(null);
            titre.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1,
                                              int arg2, int arg3) {
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    titre.setTag("modified");
                }
            });
            note.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1,
                                              int arg2, int arg3) {
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    note.setTag("modified");
                }
            });
        }
        titre.setTextSize(Integer.parseInt(pref.getString("pref_sizeNote", "16")));
        note.setTextSize(Integer.parseInt(pref.getString("pref_sizeNote", "16")));
        titreT.setTextSize(Integer.parseInt(pref.getString("pref_sizeNote", "16")));
        noteT.setTextSize(Integer.parseInt(pref.getString("pref_sizeNote", "16")));

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
            	//Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
            	Toast.makeText(this, this.getString(R.string.toast_save), Toast.LENGTH_LONG).show();
            }
            else {
            	//Toast.makeText(this, "Note updated", Toast.LENGTH_LONG).show();
            	Toast.makeText(this, this.getString(R.string.toast_update), Toast.LENGTH_LONG).show();
            }
        }
        else {
        	//Toast.makeText(this, "Failed to save", Toast.LENGTH_LONG).show();
        	Toast.makeText(this, this.getString(R.string.toast_fail), Toast.LENGTH_LONG).show();
        }

        noteBdd.close();
        this.finish();
        returnMain();
    }

    public void returnMain()
    {
        this.finish();
        //Intent intent = new Intent(NoteEdition.this, NoteMain.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        //startActivity(intent);
    }

    public void quit(View v)
    {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        if ( ( note.getTag() != null || titre.getTag() != null ) && pref.getBoolean("pref_cancel",false) == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    //.setTitle("Cancel modification")
                    .setTitle(NoteEdition.this.getString(R.string.toast_titleCancel))
                    //.setMessage("Are you sure?")
                    .setMessage(NoteEdition.this.getString(R.string.toast_msgCancel))
                    //.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    .setPositiveButton(NoteEdition.this.getString(R.string.toast_positiveButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            NoteEdition.this.finish();
                            returnMain();
                        }
                    })
                    //.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    .setNegativeButton(NoteEdition.this.getString(R.string.toast_negativeButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }else {
            this.finish();
            returnMain();
        }
    }

    @Override
    public void onBackPressed() {
        if ( ( note.getTag() != null || titre.getTag() != null ) && pref.getBoolean("pref_cancel_back",false) == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle(NoteEdition.this.getString(R.string.toast_titleCancel))
                    .setMessage(NoteEdition.this.getString(R.string.toast_msgCancel))
                    .setPositiveButton(NoteEdition.this.getString(R.string.toast_positiveButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            NoteEdition.this.finish();
                            returnMain();
                        }
                    })
                    .setNegativeButton(NoteEdition.this.getString(R.string.toast_negativeButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }else {
            this.quit(this.findViewById(id));
        }
    }
}
