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
    static final String EXTRA_TITLE   = "TitreNoteEdition";
    static final String EXTRA_NOTE    = "NoteEdition";
    static final String EXTRA_EDITION = "edition";
    static final String EXTRA_ID      = "id";
    static final String EXTRA_SIZE    = "pref_sizeNote";
    boolean edit = false;
    int id       = 0;
    SharedPreferences pref;
    private EditText titre;
    private EditText note;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!pref.getBoolean("pref_theme", false))
        {
            setTheme(android.R.style.Theme_DeviceDefault);
        }
        else
        {
            setTheme(android.R.style.Theme_DeviceDefault_Light);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noteedition);

        titre = (EditText)findViewById(R.id.TitreNoteEdition);
        note  = (EditText)findViewById(R.id.NoteEdition);
        TextView noteT  = (TextView)findViewById(R.id.NoteEditionTitre);
        TextView titreT = (TextView)findViewById(R.id.TitreNote);

        Intent intent = getIntent();
        if (intent != null)
        {
            titre.setText(intent.getStringExtra(EXTRA_TITLE));
            note.setText(intent.getStringExtra(EXTRA_NOTE));
            edit = intent.getBooleanExtra(EXTRA_EDITION, false);
            id   = intent.getIntExtra(EXTRA_ID, 0);
            titre.setTag(null);
            note.setTag(null);
            titre.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void afterTextChanged(Editable arg0)
                {
                    // Add tag only ontextChanged
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1,
                                              int arg2, int arg3)
                {
                    // Add tag only ontextChanged
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                    titre.setTag("modified");
                }
            });
            note.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void afterTextChanged(Editable arg0)
                {
                    // Add tag only ontextChanged
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1,
                                              int arg2, int arg3)
                {
                    // Add tag only ontextChanged
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                    note.setTag("modified");
                }
            });
        }
        titre.setTextSize(Integer.parseInt(pref.getString(EXTRA_SIZE, "16")));
        note.setTextSize(Integer.parseInt(pref.getString(EXTRA_SIZE, "16")));
        titreT.setTextSize(Integer.parseInt(pref.getString(EXTRA_SIZE, "16")));
        noteT.setTextSize(Integer.parseInt(pref.getString(EXTRA_SIZE, "16")));
    }

    public void save(View v)
    {
        EditText titreElt    = (EditText)findViewById(R.id.TitreNoteEdition);
        String   titreEdited = titreElt.getText().toString();
        EditText noteEdited  = (EditText)findViewById(R.id.NoteEdition);
        String   content     = noteEdited.getText().toString();

        NotesBDD noteBdd = new NotesBDD(this);

        Note n = new Note(titreEdited, content);

        noteBdd.open();
        if (!edit)
        {
            noteBdd.insertNote(n);
        }
        else
        {
            noteBdd.updateNote(id, n);
        }

        Note noteFromBdd = noteBdd.getNoteWithTitre(n.getTitre());
        if (noteFromBdd != null)
        {
            if (!edit)
            {
                Toast.makeText(this, this.getString(R.string.toast_save), Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, this.getString(R.string.toast_update), Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this, this.getString(R.string.toast_fail), Toast.LENGTH_LONG).show();
        }

        noteBdd.close();
        this.finish();
        returnMain();
    }

    public void returnMain()
    {
        this.finish();
    }

    public void dialogConfirmationExit()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
        .setTitle(NoteEdition.this.getString(R.string.toast_titleCancel))
        .setMessage(NoteEdition.this.getString(R.string.toast_msgCancel))
        .setPositiveButton(NoteEdition.this.getString(R.string.toast_positiveButton), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                NoteEdition.this.finish();
                returnMain();
            }
        })
        .setNegativeButton(NoteEdition.this.getString(R.string.toast_negativeButton), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        })
        .show();
    }

    public void quit(View v)
    {
        if ((note.getTag() != null || titre.getTag() != null) && pref.getBoolean("pref_cancel", false))
        {
            dialogConfirmationExit();
        }
        else
        {
            returnMain();
        }
    }

    @Override
    public void onBackPressed()
    {
        if ((note.getTag() != null || titre.getTag() != null) && (pref.getBoolean("pref_cancel_back", false) || pref.getBoolean("pref_cancel", false)))
        {
            dialogConfirmationExit();
        }
        else
        {
            returnMain();
        }
    }
}
