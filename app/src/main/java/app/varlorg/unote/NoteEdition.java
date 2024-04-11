package app.varlorg.unote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.text.method.ScrollingMovementMethod;
import android.graphics.Typeface;

import static app.varlorg.unote.NoteMain.COLOR_TEXT_DEFAULT;
import static app.varlorg.unote.NoteMain.POPUP_TEXTSIZE_FACTOR;
import static app.varlorg.unote.NoteMain.TOAST_TEXTSIZE_FACTOR;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.*;

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
    private TextView titreL;
    private EditText titreNote;
    private TextView titreNoteTV;
    private Intent intent;

    void customToast(String msgToDisplay){
        LinearLayout linearLayout=new LinearLayout(getApplicationContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        GradientDrawable shape=new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(50);
        shape.setColor(getResources().getColor(android.R.color.background_light));
        shape.setStroke(3,getResources().getColor(android.R.color.transparent));

        TextView textView=new TextView(getApplicationContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        textView.setMaxWidth((int)(getResources().getDisplayMetrics().widthPixels*0.9));
        textView.setText(msgToDisplay);
        textView.setTextSize((int)(textSize*NoteMain.TOAST_TEXTSIZE_FACTOR));
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setAlpha(1f);
        textView.setBackground(shape);
        int pad_width=(int)(getResources().getDisplayMetrics().widthPixels*0.04);
        int pad_height=(int)(getResources().getDisplayMetrics().heightPixels*0.02);
        textView.setPadding(pad_width,pad_height,pad_width,pad_height);

        Toast toast=new Toast(getApplicationContext());

        linearLayout.addView(textView);
        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_LONG);

        toast.show();
    }

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
        titreL = (TextView)findViewById(R.id.TitreNoteLine);
        titreNote  = (EditText)findViewById(R.id.TitreNoteEdition);
        titreNoteTV = (TextView)findViewById(R.id.TitreNoteEditionTV);

        intent = getIntent();
        if (intent != null)
        {
            // Get intent, action and MIME type
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if ("text/plain".equals(type)) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    String sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
                    String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                    if (sharedSubject != null) {
                        titre.setText(sharedSubject);
                        titreNoteTV.setText(sharedSubject);
                    }
                    if (sharedTitle != null) {
                        titre.setText(sharedTitle);
                        titreNoteTV.setText(sharedTitle);
                    }
                    if (sharedText != null) {
                        note.setText(sharedText);
                        noteTV.setText(sharedText);
                    }
                    Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND rcv EXTRA_TITLE" + intent.getStringExtra(Intent.EXTRA_TITLE));
                    Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND rcv EXTRA_TEXT" + intent.getStringExtra(Intent.EXTRA_TEXT));
                }
            }else {
                titre.setText(intent.getStringExtra(EXTRA_TITLE));
                note.setText(intent.getStringExtra(EXTRA_NOTE));
                noteTV.setText(intent.getStringExtra(EXTRA_NOTE));
                edit = intent.getBooleanExtra(EXTRA_EDITION, false);
                id = intent.getIntExtra(EXTRA_ID, 0);
            }
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
                    if (titre.hasFocus()) {
                        titre.setTag("modified");
                        Log.d(BuildConfig.APPLICATION_ID, "titre setTag");
                    }
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
                    if (note.hasFocus()) {
                        note.setTag("modified");
                        Log.d(BuildConfig.APPLICATION_ID, "note setTag " + note.getTag());
                    }
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
        titre.getBackground().clearColorFilter();
        //titreNoteTV.setTextSize(textSize * (float) 1.3);
        titreNoteTV.setTextSize(textSize);
        note.setTextSize(textSize);
        noteTV.setTextSize(textSize);
        titreT.setTextSize(textSize);
        noteT.setTextSize(textSize);

        int colorTitle = pref.getInt("pref_note_text_color_title_edit",COLOR_TEXT_DEFAULT );
        int colorNote = pref.getInt("pref_note_text_color_note_edit",COLOR_TEXT_DEFAULT );
        int colorTitleDesc = pref.getInt("pref_note_text_color_title_edit_desc", COLOR_TEXT_DEFAULT);
        int colorAll = pref.getInt("pref_note_text_color_edit_all", COLOR_TEXT_DEFAULT);
        int colorNoteDesc = pref.getInt("pref_note_text_color_note_edit_desc", COLOR_TEXT_DEFAULT);
        boolean colorBool = pref.getBoolean("pref_note_text_color_edit_bool", false);

        if (colorBool) {
            titreT.setTextColor(colorTitleDesc);

            titre.setTextColor(colorTitle);
            titreNoteTV.setTextColor(colorTitle);

            noteT.setTextColor(colorNoteDesc);

            note.setTextColor(colorNote);
            noteTV.setTextColor(colorNote);

            Log.d(BuildConfig.APPLICATION_ID, String.format("colorTitleDesc x %08x",  colorTitleDesc) );
            Log.d(BuildConfig.APPLICATION_ID, String.format("colorTitle x %08x",  colorTitle) );
            Log.d(BuildConfig.APPLICATION_ID, String.format("colorNoteDesc x %08x",  colorNoteDesc) );
            Log.d(BuildConfig.APPLICATION_ID, String.format("colorNote x %08x",  colorNote) );
        } else {
            titreT.setTextColor(colorAll);
            titre.setTextColor(colorAll);
            titreNoteTV.setTextColor(colorAll);
            noteT.setTextColor(colorAll);
            note.setTextColor(colorAll);
            noteTV.setTextColor(colorAll);
            Log.d(BuildConfig.APPLICATION_ID, String.format("colorAll x %08x",  colorAll) );
        }

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

        note.requestFocus();
        if (pref.getBoolean("pref_edit_cursor_end", false)) {
            Log.d(BuildConfig.APPLICATION_ID, "setSelection  " +  note.getText().length() );
            note.postDelayed(new Runnable() {
                @Override
                public void run() {
                    note.setSelection(note.length());
                }
            }, 200);
        } else {
            note.postDelayed(new Runnable() {
                @Override
                public void run() {
                    note.setSelection(0);
                }
            }, 200);
        }
        if (pref.getBoolean("pref_edit_capitalize_note", false)) {
            note.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }
        if (pref.getBoolean("pref_edit_capitalize_title", false)) {
            titreNote.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }

        if (pref.getBoolean("pref_edit_mode_view", false) && (intent.getStringExtra(EXTRA_NOTE) != null ))
        {
            titreNoteTV.setMovementMethod(new ScrollingMovementMethod());
            titreNoteTV.setTypeface(null, Typeface.BOLD);

            noteTV.setMovementMethod(new ScrollingMovementMethod());

            titreNoteTV.setText(titreNote.getText());
            noteTV.setText(note.getText());
            noteTV.setVisibility(View.VISIBLE);
            titreNoteTV.setVisibility(View.VISIBLE);
            note.setVisibility(View.GONE);
            titreNote.setVisibility(View.GONE);

            note.setTextIsSelectable(false);
            titreNote.setTextIsSelectable(false);

            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(noteTV.getWindowToken(), 0);
            this.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            );

            noteT.setText(getString(R.string.TexteEdition) + " \uD83D\uDC41"); // 👁
            if (pref.getBoolean("pref_edit_mode_view_ui", true))
            {
                titreT.setVisibility(View.GONE);
                titreL.setVisibility(View.GONE);
                noteT.setVisibility(View.GONE);
                titreNoteTV.setTextSize((float) (textSize * 1.2 ));
            }
            else {
                titreNoteTV.setTextSize(textSize);
            }

        }
        else
        {
            noteTV.setVisibility(View.GONE);
            titreNoteTV.setVisibility(View.GONE);
            note.setVisibility(View.VISIBLE);
            titreNote.setVisibility(View.VISIBLE);
            note.setTextIsSelectable(true);
            titreNote.setTextIsSelectable(true);
            noteT.setText( getString(R.string.TexteEdition) + " \u270d\ufe0e" ); //✍  ✏️ ?
            if (pref.getBoolean("pref_edit_mode_edit_ui", false))
            {
                titreT.setVisibility(View.GONE);
                titreL.setVisibility(View.GONE);
                noteT.setVisibility(View.GONE);
                titreNote.setTextSize((float) (textSize * 1.2 ));
            }
            else {
                titreNote.setTextSize(textSize);
            }
            note.requestFocus();
            // Deplay keyboard show to let time the view to be served
            note.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) NoteEdition.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    NoteEdition.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    imm.showSoftInput(note, InputMethodManager.SHOW_IMPLICIT);
                }
            },200);
            titre.setTag(null);
            note.setTag(null);
        }
        titre.addTextChangedListener(new TextWatcher()
    {
        @Override
        public void afterTextChanged(Editable arg0)
        {
            // DO nothing
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1,
                                      int arg2, int arg3)
        {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
        {
            if ( titre.getText().length() != 0) {
                titre.getBackground().setColorFilter( new PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN));
            }
            else {
                titre.getBackground().clearColorFilter();
            }
        }
    });

        titre.setTag(null);
        note.setTag(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //ajoute les entrées de menu_test à l'ActionBar
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        optionsMenu = menu;

        if (pref.getBoolean("pref_edit_mode_menu_all", false))
        {
            this.setTitle("");
            optionsMenu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            optionsMenu.findItem(R.id.action_export).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            optionsMenu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            optionsMenu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            optionsMenu.findItem(R.id.action_return).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (pref.getBoolean("pref_edit_mode_view", false) && (intent.getStringExtra(EXTRA_NOTE) != null )) {
            MenuItem item = optionsMenu.findItem(R.id.action_switch_mode);
            item.setIcon(android.R.drawable.ic_menu_edit);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id_menu = item.getItemId();
        if (id_menu == R.id.action_save){
            save(getWindow().getDecorView().getRootView());
            return true;
        }
        if (id_menu == R.id.action_return){
            quit(getWindow().getDecorView().getRootView());
            return true;
        }
        if (id_menu == R.id.action_switch_mode){
            //switch_mode(getWindow().getDecorView().getRootView());
            EditText titreNote  = (EditText)findViewById(R.id.TitreNoteEdition);
            TextView titreNoteTV = (TextView)findViewById(R.id.TitreNoteEditionTV);
            EditText note  = (EditText)findViewById(R.id.NoteEdition);
            TextView noteTV = (TextView)findViewById(R.id.NoteEditionTV);
            TextView noteT  = (TextView)findViewById(R.id.NoteEditionTitre);

            if ( noteTV.getVisibility() == View.VISIBLE){
                item.setIcon(android.R.drawable.ic_menu_view);

                titreNoteTV.setVisibility(View.GONE);
                titreNote.setVisibility(View.VISIBLE);
                titreNote.setTextIsSelectable(true);

                titreT.setVisibility(View.VISIBLE);
                titreL.setVisibility(View.VISIBLE);

                noteTV.setVisibility(View.GONE);
                note.setVisibility(View.VISIBLE);
                note.setTextIsSelectable(true);

                noteT.setVisibility(View.VISIBLE);
                noteT.setText( getString(R.string.TexteEdition) + " \u270d\ufe0e" ); //✍  ✏️ ?  ✏︎
                if (pref.getBoolean("pref_edit_mode_edit_ui", false))
                {
                    titreT.setVisibility(View.GONE);
                    titreL.setVisibility(View.GONE);
                    noteT.setVisibility(View.GONE);
                    titreNote.setTextSize((float) (textSize * 1.2 ));
                }
                else {
                    titreNote.setTextSize(textSize);
                }
                note.requestFocus();
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                imm.showSoftInput( note, InputMethodManager.SHOW_IMPLICIT);
            }
            else {
                item.setIcon(android.R.drawable.ic_menu_edit);
                titreNote.setVisibility(View.GONE);
                titreNoteTV.setVisibility(View.VISIBLE);
                titreNoteTV.setMovementMethod(new ScrollingMovementMethod());
                titreNoteTV.setTypeface(null, Typeface.BOLD);

                noteTV.setVisibility(View.VISIBLE);
                noteTV.setMovementMethod(new ScrollingMovementMethod());

                note.setVisibility(View.GONE);

                titreNoteTV.setText(titreNote.getText());
                noteTV.setText(note.getText());

                note.setTextIsSelectable(false);
                titreNote.setTextIsSelectable(false);

                InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(noteTV.getWindowToken(), 0);
                this.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );

                noteT.setText(getString(R.string.TexteEdition) + " \uD83D\uDC41");
                //noteT.setText(getString(R.string.TexteEdition) + Html.fromHtml("\ud83d\udc41\ufe0e  \uD83D\uDD0F\ufe0e \uD83D\uDD12\ufe0e"));
                //noteT.setText("\uD83D\uDC40"); 👁︎ - 📄  -- 📄️ --- 𓁺 ;
                if (pref.getBoolean("pref_edit_mode_view_ui", true))
                {
                    titreT.setVisibility(View.GONE);
                    titreL.setVisibility(View.GONE);
                    noteT.setVisibility(View.GONE);
                    titreNoteTV.setTextSize((float) (textSize * 1.2 ));
                }
                else {
                    titreNoteTV.setTextSize(textSize);
                }
            }


            /*MenuItem menuItemView = (MenuItem) optionsMenu.findItem(R.id.action_view);
            menuItemView.setVisible(true);*/
            return true;
        }
        if (id_menu == R.id.action_share){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(BuildConfig.APPLICATION_ID, note.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_TEXT, note.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_TITLE, titreNote.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, titreNote.getText().toString());
            sendIntent.setType("text/plain");
            Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND EXTRA_TITLE" + sendIntent.getStringExtra(Intent.EXTRA_TITLE));
            Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND EXTRA_SUBJECT" + sendIntent.getStringExtra(Intent.EXTRA_SUBJECT));
            Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND EXTRA_TEXT" + sendIntent.getStringExtra(Intent.EXTRA_TEXT));
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        if (id_menu == R.id.action_copy){
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            // Creates a new text clip to put on the clipboard.
            ClipData clip = ClipData.newPlainText("uNote copy", String.valueOf(note.getText()));
            Log.d(BuildConfig.APPLICATION_ID, " menu_copy - " +  String.valueOf(note.getText()));
            // Set the clipboard's primary clip.
            clipboard.setPrimaryClip(clip);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                customToast(getString(android.R.string.copy));
        }
        if (id_menu == R.id.action_export){
            String n = String.valueOf(note.getText());
            String t = String.valueOf(titreNote.getText());

            /*final NotesBDD noteBdd = new NotesBDD(this);
            noteBdd.open();
            noteBdd.exportNote(getApplicationContext(), id);
            noteBdd.close();*/
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

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
            String outputDir = pref.getString("output_backup_dir", externalFilesDir.toString());
            String exportNoteFile = "unote_";
            if (exportTitle)
            {
                exportNoteFile += t.replaceAll("[^a-zA-Z0-9.-]", "_");
            }
            else {
                exportNoteFile += id ;
            }

            if (exportDate)
            {
                exportNoteFile += "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime()) ;
            }
            exportNoteFile += ".txt";

            File file = new File(outputDir, exportNoteFile);

            try {
                FileWriter w = new FileWriter(file,true);

                StringBuilder sb = new StringBuilder();

                sb.append(t +"\n\n");
                sb.append(n);
                w.append(sb.toString());

                w.close();
                Log.d(BuildConfig.APPLICATION_ID, "exportNote " + sb.toString());
                Log.d(BuildConfig.APPLICATION_ID, "exportNote" + file.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ( pref.getBoolean("pref_notifications", true))
            {
                customToast(getApplicationContext().getString(R.string.note_exported) + " " +
                        file.toString());
            }
        }
        if (id_menu == R.id.action_delete){
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
                                    customToast(NoteEdition.this.getString(R.string.note_deleted));
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
                    if ( pref.getBoolean("pref_notifications", true))
                    {
                        customToast(NoteEdition.this.getString(R.string.note_deleted));
                    }
                }
            }

            //returnMain();
            return true;
        }
        if (id_menu == R.id.action_search){
            final String noteContent = ((EditText)findViewById(R.id.NoteEdition)).getText().toString();

            searchNote = findViewById(R.id.search_note);
            final FrameLayout searchNote_lay = findViewById(R.id.search_within_note);
            final TextView searchNoteCountTV = (TextView)findViewById(R.id.search_note_count);
            final ImageButton btnClear = (ImageButton)findViewById(R.id.btn_clear_edition);
            searchNote.setTextSize(textSize);
            searchNoteCountTV.setTextSize(textSize);

            ViewGroup.LayoutParams params=btnClear.getLayoutParams();
            params.width=Math.max(textSize*2,40);
            params.height=params.width;
            btnClear.setLayoutParams(params);
            //set event for clear button
            btnClear.setOnClickListener(onClickListener());

            if (searchNote_lay.getVisibility() == View.VISIBLE)
            {
                searchNote.setText("");
                searchNoteCountTV.setText("");
                note.setText(note.getText().toString());
                searchNote_lay.setVisibility(View.GONE);
                searchNoteCountTV.setVisibility(View.GONE);
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
                            searchNoteCountTV.setVisibility(View.VISIBLE);
                            int patternFoundNb = highlightText(s.toString());
                            if ( pref.getBoolean("pref_search_note_count", true))
                                searchNoteCountTV.setText("" + patternFoundNb);
                        } else     //not include text
                        {
                            btnClear.setVisibility(View.GONE);
                            searchNoteCountTV.setVisibility(View.GONE);
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
        noteTV.setText(spannableString);
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
        long ret_id = 0;
        int ret_update = 1;
        if (!edit)
        {
            ret_id = noteBdd.insertNote(n);
            id = (int) ret_id;
            Log.d(BuildConfig.APPLICATION_ID, "insertNote rc " + ret_id);
        }
        else
        {
            ret_update = noteBdd.updateNote(id, n);
            Log.d(BuildConfig.APPLICATION_ID, "updateNote  rc " + ret_update);
        }

        Log.d(BuildConfig.APPLICATION_ID, "save id " + id);

        Note noteFromBdd = noteBdd.getNoteWithId(id);
        Log.d(BuildConfig.APPLICATION_ID, "noteFromBdd " + noteFromBdd);
        if (noteFromBdd != null && ret_update == 1)
        {
            if (!edit)
            {
                if ( pref.getBoolean("pref_notifications", true))
                {
                    customToast(NoteEdition.this.getString(R.string.toast_save));
                }
            }
            else
            {
                if ( pref.getBoolean("pref_notifications", true))
                {
                    customToast(NoteEdition.this.getString(R.string.toast_update));
                }
            }
        }
        else
        {
            if ( pref.getBoolean("pref_notifications", true))
            {
                customToast(NoteEdition.this.getString(R.string.toast_fail));
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
