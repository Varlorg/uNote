package app.varlorg.unote;

import android.app.ActionBar;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class NoteEdition extends Activity
{
    private static final String EXTRA_TITLE   = "TitreNoteEdition";
    private static final String EXTRA_NOTE    = "NoteEdition";
    private static final String EXTRA_EDITION = "edition";
    private static final String EXTRA_ID      = "id";
    private static final String EXTRA_SIZE    = "pref_sizeNote";
    private boolean edit = false;
    private int id       = 0;
    private SharedPreferences pref;
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

        int textSize = Integer.parseInt(pref.getString(EXTRA_SIZE, "16"));
        titre.setTextSize(textSize);
        note.setTextSize(textSize);
        titreT.setTextSize(textSize);
        noteT.setTextSize(textSize);
        final Button buttonSave = (Button)findViewById(R.id.ButtonSave);
        final Button buttonQuit = (Button)findViewById(R.id.ButtonQuit);
        buttonSave.setTextSize(textSize);
        buttonQuit.setTextSize(textSize);

        final LinearLayout buttonsBar = (LinearLayout)findViewById(R.id.editionButtons);
        buttonsBar.post(new Runnable()
        {
            @Override
            public void run()
            {
                Boolean forceButtons_horizontal = pref.getBoolean("pref_forceEditionButtonsH", false);
                if ((buttonQuit.getLineCount() > 1 || buttonSave.getLineCount() > 1) && !forceButtons_horizontal)
                {
                    buttonsBar.setOrientation(LinearLayout.VERTICAL);
                    buttonQuit.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;
                    buttonSave.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;
                }
            }
        });
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
        //Autosave
        if ((note.getTag() != null || titre.getTag() != null) && (pref.getString("pref_back_action", "0").equals("3")))
        {
            save(getWindow().getDecorView().getRootView());
        }
        // Always cancel confirmation or as return button with confirmation enable
        if ((note.getTag() != null || titre.getTag() != null) &&
            (pref.getString("pref_back_action", "0").equals("2") ||
             (pref.getString("pref_back_action", "0").equals("1") && pref.getBoolean("pref_cancel", false))))
        {
            dialogConfirmationExit();
        }
        else
        {
            returnMain();
        }
    }
}
