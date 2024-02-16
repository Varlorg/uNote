package app.varlorg.unote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import static app.varlorg.unote.NoteMain.POPUP_TEXTSIZE_FACTOR;
import static app.varlorg.unote.NoteMain.TOAST_TEXTSIZE_FACTOR;


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
    private TextView noteTV;
    private int textSize;
    private EditText searchNote;
    private Menu optionsMenu;
    private TextView noteT;
    private TextView titreT;
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
        noteTV = (TextView)findViewById(R.id.NoteEditionTV);
        noteT  = (TextView)findViewById(R.id.NoteEditionTitre);
        titreT = (TextView)findViewById(R.id.TitreNote);

        Intent intent = getIntent();
        if (intent != null)
        {
            titre.setText(intent.getStringExtra(EXTRA_TITLE));
            note.setText(intent.getStringExtra(EXTRA_NOTE));
            noteTV.setText(intent.getStringExtra(EXTRA_NOTE));
            if (intent.getStringExtra(EXTRA_NOTE) == null ) {
                noteTV.setVisibility(View.GONE);
                note.setVisibility(View.VISIBLE);
            }
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
        textSize = Integer.parseInt(pref.getString("pref_sizeNote", "18"));
        int textSizeButton = textSize < 15 ? textSize - 1: textSize - 4;
        if ( textSize == -1 )
        {
            textSize = Integer.parseInt(pref.getString("pref_sizeNote_custom", "18"));
            textSizeButton = Integer.parseInt(pref.getString("pref_sizeNote_button", "14" ));
        }
        titre.setTextSize(textSize);
        note.setTextSize(textSize);
        noteTV.setTextSize(textSize);
        titreT.setTextSize(textSize);
        noteT.setTextSize(textSize);
        final Button buttonSave = (Button)findViewById(R.id.ButtonSave);
        final Button buttonQuit = (Button)findViewById(R.id.ButtonQuit);
        buttonSave.setTextSize(textSizeButton);
        buttonQuit.setTextSize(textSizeButton);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //ajoute les entrées de menu_test à l'ActionBar
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        optionsMenu = menu;

        if (pref.getBoolean("pref_edit_mode_menu_all", false))
        {
            MenuItem itemDelete = optionsMenu.findItem(R.id.action_delete);
            itemDelete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            MenuItem itemReturn = optionsMenu.findItem(R.id.action_return);
            itemReturn.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (pref.getBoolean("pref_edit_mode_view", false))
        {
            MenuItem item = optionsMenu.findItem(R.id.action_switch_mode);
            item.setIcon(android.R.drawable.ic_menu_edit);
            noteTV.setText(note.getText());
            noteTV.setVisibility(View.VISIBLE);
            note.setVisibility(View.GONE);

            noteT.setText(getString(R.string.TexteEdition) + " 👁️");
        }
        else
        {
            noteTV.setVisibility(View.GONE);
            note.setVisibility(View.VISIBLE);
            noteT.setText( getString(R.string.TexteEdition) + " ✍️" );
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save){
            save(getWindow().getDecorView().getRootView());
            return true;
        }
        if (id == R.id.action_return){
            quit(getWindow().getDecorView().getRootView());
            return true;
        }
        if (id == R.id.action_switch_mode){
            //switch_mode(getWindow().getDecorView().getRootView());
            EditText note  = (EditText)findViewById(R.id.NoteEdition);
            TextView noteTV = (TextView)findViewById(R.id.NoteEditionTV);
            TextView noteT  = (TextView)findViewById(R.id.NoteEditionTitre);

            if ( noteTV.getVisibility() == View.VISIBLE){
                item.setIcon(android.R.drawable.ic_menu_view);

                noteTV.setVisibility(View.GONE);
                note.setVisibility(View.VISIBLE);

                noteT.setText( getString(R.string.TexteEdition) + " ✍️" ); // ✏️ ?
            }
            else {
                item.setIcon(android.R.drawable.ic_menu_edit);

                noteTV.setVisibility(View.VISIBLE);
                note.setVisibility(View.GONE);

                noteTV.setText(note.getText());

                noteT.setText(getString(R.string.TexteEdition) + " 👁️");
            }


            /*MenuItem menuItemView = (MenuItem) optionsMenu.findItem(R.id.action_view);
            menuItemView.setVisible(true);*/
            return true;
        }
        if (id == R.id.action_delete){
            final NotesBDD noteBdd = new NotesBDD(this);
            noteBdd.open();
            if (!edit)
            {
                dialogConfirmationExit();
            }
            else {
                pref = PreferenceManager.getDefaultSharedPreferences(this);

                if (pref.getBoolean("pref_del", true)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder
                            .setTitle(this.getString(R.string.dialog_delete_title))
                            .setMessage(this.getString(R.string.dialog_delete_msg))
                            .setPositiveButton(this.getString(R.string.dialog_delete_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int ida) {
                                    noteBdd.removeNoteWithID(id);
                                    noteBdd.close();
                                    Toast.makeText(NoteEdition.this, NoteEdition.this.getString(R.string.note_deleted), Toast.LENGTH_LONG).show();
                                    returnMain();

                                }
                            })
                            .setNegativeButton(this.getString(R.string.dialog_delete_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize((int) (textSize * POPUP_TEXTSIZE_FACTOR));
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize((int) (textSize * POPUP_TEXTSIZE_FACTOR));
                    ((TextView) alertDialog.findViewById(android.R.id.message)).setTextSize((int) (textSize * POPUP_TEXTSIZE_FACTOR));
                } else {
                    noteBdd.removeNoteWithID(id);
                    noteBdd.close();

                    returnMain();
                    Toast toast = Toast.makeText(this, this.getString(R.string.note_deleted), Toast.LENGTH_LONG);
                    ((TextView) ((LinearLayout) toast.getView()).getChildAt(0)).setTextSize((int) (TOAST_TEXTSIZE_FACTOR * textSize));
                    if (pref.getBoolean("pref_notifications", true))
                        toast.show();
                }
            }

            //returnMain();
            return true;
        }
        if (id == R.id.action_search){
            final String noteContent = ((EditText)findViewById(R.id.NoteEdition)).getText().toString();

            searchNote = findViewById(R.id.search_note);
            final FrameLayout searchNote_lay = findViewById(R.id.search_within_note);
            final ImageButton btnClear = (ImageButton)findViewById(R.id.btn_clear_edition);
            searchNote.setTextSize(textSize);

            ViewGroup.LayoutParams params=btnClear.getLayoutParams();
            params.width=Math.max(textSize*2,40);
            params.height=params.width;
            btnClear.setLayoutParams(params);
            //set event for clear button
            btnClear.setOnClickListener(onClickListener());

            if (searchNote_lay.getVisibility() == View.VISIBLE)
            {
                searchNote.setText("");
                note.setText(note.getText().toString());
                searchNote_lay.setVisibility(View.GONE);
                btnClear.setVisibility(View.GONE);
            }
            else {
                searchNote.requestFocus();
                (findViewById(R.id.search_within_note)).setVisibility(View.VISIBLE);
                searchNote.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                                                }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1,
                                                    int arg2, int arg3) {
                        // Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

                        if (!searchNote.getText().toString().equals(""))   //if edittext include text
                        {
                            btnClear.setVisibility(View.VISIBLE);
                            highlightText(s.toString());
                        } else     //not include text
                        {
                            btnClear.setVisibility(View.GONE);
                            note.setText(note.getText().toString());
                        }
                    }
                });
            }
            //use "test" string for test

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener onClickListener()
    {
        return(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchNote.setText(""); //clear edittext
            }
        });
    }

    private int highlightText(String s) {
        SpannableString spannableString = new SpannableString(note.getText());
        BackgroundColorSpan[] backgroundColorSpan =
                spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan bgSpan : backgroundColorSpan) {
            spannableString.removeSpan(bgSpan);
        }
        int indexOfKeyWord = spannableString.toString().indexOf(s);
        int count = 0;
        while (indexOfKeyWord >= 0) {
            //spannableString.setSpan(new BackgroundColorSpan(Color.GRAY), indexOfKeyWord,
            //spannableString.setSpan(new BackgroundColorSpan(Color.rgb(32,196,32)), indexOfKeyWord,
            spannableString.setSpan(new BackgroundColorSpan(Color.rgb(64,148,255)), indexOfKeyWord,
                    indexOfKeyWord + s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            indexOfKeyWord = spannableString.toString().indexOf(s, indexOfKeyWord + s.length());
            count++;
        }
        note.setText(spannableString);
        return count;
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
                if ( pref.getBoolean("pref_notifications", true))
                {
                    Toast toast = Toast.makeText(this, this.getString(R.string.toast_save), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
            else
            {
                if ( pref.getBoolean("pref_notifications", true))
                {
                    Toast toast = Toast.makeText(this, this.getString(R.string.toast_update), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
        else
        {
            if ( pref.getBoolean("pref_notifications", true))
            {
                Toast toast = Toast.makeText(this, this.getString(R.string.toast_fail), Toast.LENGTH_LONG);
                toast.show();
            }
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
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(Math.min(36,(int)(textSize * POPUP_TEXTSIZE_FACTOR)));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(Math.min(36,(int)(textSize * POPUP_TEXTSIZE_FACTOR)));
        ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
    }

    public void quit(View v)
    {
        if ((note.getTag() != null || titre.getTag() != null) && pref.getBoolean("pref_cancel", true))
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
             (pref.getString("pref_back_action", "0").equals("1") && pref.getBoolean("pref_cancel", true))))
        {
            dialogConfirmationExit();
        }
        else
        {
            returnMain();
        }
    }
}
