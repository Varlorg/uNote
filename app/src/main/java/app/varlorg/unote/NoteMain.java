package app.varlorg.unote;

import java.util.List;
import java.security.MessageDigest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.app.Instrumentation;
import android.os.Parcelable;

public class NoteMain extends Activity
{
    private static final String EXTRA_TITLE      = "TitreNoteEdition";
    private static final String EXTRA_NOTE       = "NoteEdition";
    private static final String EXTRA_EDITION    = "edition";
    private static final String EXTRA_ID         = "id";
    private static final String SEARCH_CONTENT   = "contentSearch";
    private static final String SEARCH_SENSITIVE = "sensitiveSearch";
    private static final String PREF_SORT        = "pref_tri";
    private static final String PREF_SORT_ORDER  = "pref_ordretri";

    private static final String HEX = "0123456789ABCDEF";
    private ArrayAdapter <Note> simpleAdpt;
    private EditText editsearch;
    private ImageButton btnClear;
    private List <Note> listeNotes;
    private CheckBox cbSearchContent;
    private CheckBox cbSearchCase;
    private ListView lv;
    private SharedPreferences pref;
    private Parcelable state;
    private int textSize;

    @Override
    public void onPause()
    {
        super.onPause();
        // Save ListView state @ onPause
        state = lv.onSaveInstanceState();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        final NotesBDD noteBdd = new NotesBDD(this);
        noteBdd.open();
        String text = editsearch.getText().toString();

        if ("".equals(text))
        {
            listeNotes = noteBdd.getAllNotes(Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
        }
        else
        {
            listeNotes = noteBdd.getSearchedNotes(text, cbSearchContent.isChecked(), !cbSearchCase.isChecked(), Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
        }
        noteBdd.close();
        simpleAdpt.clear();
        simpleAdpt.addAll(listeNotes);
        simpleAdpt.notifyDataSetChanged();
        if (pref.getBoolean("pref_scroll", false) && state != null)
        {
            lv.onRestoreInstanceState(state);
        }
        else
        {
            lv.setAdapter(simpleAdpt);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!pref.getBoolean("pref_theme", false))
        {
            setTheme(android.R.style.Theme_DeviceDefault);
        }
        else
        {
            setTheme(android.R.style.Theme_DeviceDefault_Light);
        }

        setContentView(R.layout.activity_notemain);

        final NotesBDD noteBdd = new NotesBDD(this);
        noteBdd.open();
        listeNotes = noteBdd.getAllNotes(Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
        /****************************************************************************************/
        // The data to show
        lv = (ListView)findViewById(R.id.listView);

        simpleAdpt = new ArrayAdapter <Note>(this, R.layout.notelist, listeNotes)
        {
            @Override
            public View getView(int position, View view, ViewGroup viewGroup)
            {
                View v = super.getView(position, view, viewGroup);

                Note n = this.getItem(position);

                return(getViewCustom(position, v, viewGroup, n));
            }
        };
        lv.setAdapter(simpleAdpt);
        // React to user clicks on item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView <?> parentAdapter, View view, int position,
                                    long id)
            {
                final Note n    = (Note)parentAdapter.getItemAtPosition(position);
                boolean canEdit = false;
                if (n.getPassword() != null)
                {
                    final EditText input         = new EditText(NoteMain.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteMain.this);
                    builder
                    .setTitle(NoteMain.this.getString(R.string.dialog_pwd_title))
                    .setMessage(NoteMain.this.getString(R.string.dialog_pwd_msg))
                    .setView(input)
                    .setPositiveButton(NoteMain.this.getString(R.string.dialog_pwd_submit), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            String password = input.getText().toString();
                            if (n.getPassword().equals(SHA1(password)))
                            {
                                Intent intentTextEdition = new Intent(NoteMain.this,
                                                                      NoteEdition.class);
                                intentTextEdition.putExtra(EXTRA_TITLE, n.getTitre());
                                intentTextEdition.putExtra(EXTRA_NOTE, n.getNote());
                                intentTextEdition.putExtra(EXTRA_EDITION, true);
                                intentTextEdition.putExtra(EXTRA_ID, n.getId());
                                NoteMain.this.startActivity(intentTextEdition);
                            }
                            else
                            {
                                Toast toast = Toast.makeText(NoteMain.this, NoteMain.this.getString(R.string.toast_pwd_error), Toast.LENGTH_LONG);
                                ((TextView)((LinearLayout) toast.getView()).getChildAt(0)).setTextSize(textSize);
                                toast.show();
                            }
                        }
                    })
                    .setNegativeButton(NoteMain.this.getString(R.string.dialog_pwd_cancel), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    })
                    .show();
                }
                else
                {
                    canEdit = true;
                }
                if (canEdit)
                {
                    Intent intentTextEdition = new Intent(NoteMain.this,
                                                          NoteEdition.class);
                    intentTextEdition.putExtra(EXTRA_TITLE, n.getTitre());
                    intentTextEdition.putExtra(EXTRA_NOTE, n.getNote());
                    intentTextEdition.putExtra(EXTRA_EDITION, true);
                    intentTextEdition.putExtra(EXTRA_ID, n.getId());
                    NoteMain.this.startActivity(intentTextEdition);
                }
            }
        });

        // we register for the contextmneu
        registerForContextMenu(lv);

        cbSearchContent = (CheckBox)findViewById(R.id.search_content_cb);
        cbSearchCase    = (CheckBox)findViewById(R.id.search_case_cb);
        cbSearchContent.setChecked(pref.getBoolean(SEARCH_CONTENT, false));
        cbSearchCase.setChecked(pref.getBoolean(SEARCH_SENSITIVE, false));

        final Button buttonAddNote = (Button)findViewById(R.id.addNoteButton);
        final Button buttonSearch  = (Button)findViewById(R.id.returnSearch);
        final Button buttonReturn  = (Button)findViewById(R.id.returnButton);

        textSize = Integer.parseInt(pref.getString("pref_sizeNote", "18"));
        int textSizeButton = textSize < 15 ? textSize - 1: textSize - 4;
        if ( textSize == -1 )
        {
            textSize = Integer.parseInt(pref.getString("pref_sizeNote_custom", "18"));
            textSizeButton = Integer.parseInt(pref.getString("pref_sizeNote_button", "14" ));
        }

        cbSearchContent.setTextSize(textSize);
        cbSearchCase.setTextSize(textSize);
        buttonAddNote.setTextSize(textSizeButton);
        buttonSearch.setTextSize(textSizeButton);
        buttonReturn.setTextSize(textSizeButton);

        final LinearLayout buttonsBar = (LinearLayout)findViewById(R.id.buttons);
        buttonsBar.post(new Runnable()
        {
            @Override
            public void run()
            {
                Boolean forceButtons_horizontal = pref.getBoolean("pref_forceButtonsH", false);
                if ((buttonAddNote.getLineCount() > 1 || buttonSearch.getLineCount() > 1 || buttonReturn.getLineCount() > 1) && !forceButtons_horizontal )
                {
                    buttonsBar.setOrientation(LinearLayout.VERTICAL);
                    buttonAddNote.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;
                    buttonSearch.getLayoutParams().width  = ActionBar.LayoutParams.MATCH_PARENT;
                    buttonReturn.getLayoutParams().width  = ActionBar.LayoutParams.MATCH_PARENT;
                }
            }
        });

        // Locate the EditText in listview_main.xml
        editsearch = (EditText)findViewById(R.id.search);
        editsearch.setVisibility(View.GONE);
        editsearch.setTextSize(textSize);
        // Capture Text in EditText
        editsearch.addTextChangedListener(new TextWatcher()
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
                String text = editsearch.getText().toString();
                List <Note> listeNotesRecherche = noteBdd.getSearchedNotes(text, pref.getBoolean(SEARCH_CONTENT, false), pref.getBoolean(SEARCH_SENSITIVE, false), Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
                simpleAdpt = new ArrayAdapter <Note>     (getApplicationContext(), R.layout.notelist, listeNotesRecherche)
                {
                    @Override
                    public View getView(int position, View view, ViewGroup viewGroup)
                    {
                        view = super.getView(position, view, viewGroup);
                        Note n = this.getItem(position);
                        return(getViewCustom(position, view, viewGroup, n));
                    }
                };
                lv.setAdapter(simpleAdpt);
            }
        });

        cbSearchContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                String text = editsearch.getText().toString();
                List <Note> listeNotesRecherche = noteBdd.getSearchedNotes(text, isChecked, !cbSearchCase.isChecked(), Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
                simpleAdpt = new ArrayAdapter <Note>(getApplicationContext(), R.layout.notelist, listeNotesRecherche)
                {
                    @Override
                    public View getView(int position, View view, ViewGroup viewGroup)
                    {
                        view = super.getView(position, view, viewGroup);
                        Note n = this.getItem(position);
                        return(getViewCustom(position, view, viewGroup, n));
                    }
                };
                lv.setAdapter(simpleAdpt);
            }
        });


        cbSearchCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                String text = editsearch.getText().toString();
                List <Note> listeNotesRecherche = noteBdd.getSearchedNotes(text, cbSearchContent.isChecked(), !isChecked, Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
                simpleAdpt = new ArrayAdapter <Note>(getApplicationContext(), R.layout.notelist, listeNotesRecherche)
                {
                    @Override
                    public View getView(int position, View view, ViewGroup viewGroup)
                    {
                        view = super.getView(position, view, viewGroup);
                        Note n = this.getItem(position);
                        return(getViewCustom(position, view, viewGroup, n));
                    }
                };
                lv.setAdapter(simpleAdpt);
            }
        });

        btnClear = (ImageButton)findViewById(R.id.btn_clear);

        EditText ee = (EditText)findViewById(R.id.search);
        ViewGroup.MarginLayoutParams paramsb= (ViewGroup.MarginLayoutParams) ee.getLayoutParams();
        paramsb.setMargins(0,0,Math.max(textSize*3,60),0);
        ee.setLayoutParams(paramsb);

        ViewGroup.LayoutParams params=btnClear.getLayoutParams();
        params.width=Math.max(textSize*2,40);
        params.height=params.width;
        btnClear.setLayoutParams(params);
        //set on text change listener for edittext
        editsearch.addTextChangedListener(textWatcher());
        //set event for clear button
        btnClear.setOnClickListener(onClickListener());

        final LinearLayout searchOptBar = (LinearLayout)findViewById(R.id.search_options);
        Boolean bCheckboxesVertical = pref.getBoolean("pref_searchCheckboxV", false);
        if ( bCheckboxesVertical )
        {
            searchOptBar.setOrientation(LinearLayout.VERTICAL);
            cbSearchContent.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;
            cbSearchCase.getLayoutParams().width  = ActionBar.LayoutParams.MATCH_PARENT;
        }
        noteBdd.close();
    }

    public View getViewCustom(int position, View view, ViewGroup viewGroup, Note n)
    {
        String noteSummary;

        if (n.getPassword() != null)
        {
            noteSummary = "<b>" + n.getTitre() + "</b> <br/>" + NoteMain.this.getString(R.string.pwd_protected);
        }
        else
        {
            noteSummary = "<b>" + n.getTitre() + "</b> <br/>" + n.getNoteHead(Integer.parseInt(pref.getString("pref_preview_char_limit", "30")));
            if (pref.getBoolean("pref_date", false))
            {
                noteSummary += "<br/>" + n.getDateCreationFormated();
            }
            if (pref.getBoolean("pref_date_mod", false))
            {
                noteSummary += "<br/>modif: " + n.getDateModificationFormated();
            }
        }
        ((TextView)view).setText(Html.fromHtml(noteSummary));
        ((TextView)view).setTextSize(textSize);
        return(view);
    }

    private View.OnClickListener onClickListener()
    {
        return(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editsearch.setText(""); //clear edittext
            }
        });
    }

    private TextWatcher textWatcher()
    {
        return(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (!editsearch.getText().toString().equals(""))   //if edittext include text
                {
                    btnClear.setVisibility(View.VISIBLE);
                }
                else     //not include text
                {
                    btnClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // Do nothing
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intentPreference = new Intent(NoteMain.this,
                                                 Preference.class);
            intentPreference.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            NoteMain.this.startActivity(intentPreference);
            finish();
            return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(this.getString(R.string.menu_title));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_edit));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_passwd));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_delete));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_detail));
    }

    public static String SHA1(String text)
    {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(text.getBytes("UTF-8"),
                      0, text.length());
            byte[] sha1hash = md.digest();

            return(toHex(sha1hash));
        } catch (Exception e) {
            Log.e(BuildConfig.APPLICATION_ID, "Exception SHA1", e);
        }
        return(null);
    }

    private static void appendHex(StringBuilder sb, byte b)
    {
        sb.append(HEX.charAt((b >> 4) & 0x0f))
        .append(HEX.charAt(b & 0x0f));
    }

    public static String toHex(byte[] buf)
    {
        if (buf == null)
        {
            return("");
        }

        int           l      = buf.length;
        StringBuilder result = new StringBuilder(2 * l);

        for (int i = 0; i < buf.length; i++)
        {
            appendHex(result, buf[i]);
        }
        return(result.toString());
    }

    public void deleteNote(final Note note)
    {
        simpleAdpt.remove(note);
        NotesBDD noteBdd = new NotesBDD(NoteMain.this);
        noteBdd.open();
        noteBdd.removeNoteWithID(note.getId());
        Toast toast = Toast.makeText(NoteMain.this, NoteMain.this.getString(R.string.note_deleted), Toast.LENGTH_LONG);
        ((TextView)((LinearLayout) toast.getView()).getChildAt(0)).setTextSize(textSize);
        toast.show();
        simpleAdpt.notifyDataSetChanged();
        noteBdd.close();
    }

    public boolean launchMenu(MenuItem item, final Note note)
    {
        if (item.getTitle().equals(this.getString(R.string.menu_edit)))
        {
            Intent intentTextEdition = new Intent(NoteMain.this,
                                                  NoteEdition.class);
            intentTextEdition.putExtra(EXTRA_TITLE, note.getTitre());
            intentTextEdition.putExtra(EXTRA_NOTE, note.getNote());
            intentTextEdition.putExtra(EXTRA_EDITION, true);
            intentTextEdition.putExtra(EXTRA_ID, note.getId());
            NoteMain.this.startActivity(intentTextEdition);
        }
        else
        if (item.getTitle().equals(this.getString(R.string.menu_passwd)))
        {
            final EditText            input = new EditText(NoteMain.this);
            LinearLayout.LayoutParams lp    = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
            .setTitle(NoteMain.this.getString(R.string.dialog_add_pwd_title))
            .setMessage(NoteMain.this.getString(R.string.dialog_add_pwd_msg))
            .setView(input)
            .setNegativeButton(NoteMain.this.getString(R.string.dialog_add_pwd_remove), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    NotesBDD noteBdd = new NotesBDD(NoteMain.this);
                    noteBdd.open();
                    note.setPassword(null);
                    noteBdd.updatePassword(note.getId(), null);
                    noteBdd.close();
                    simpleAdpt.notifyDataSetChanged();
                }
            })
            .setPositiveButton(NoteMain.this.getString(R.string.dialog_add_pwd_add), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    String password = input.getText().toString();

                    NotesBDD noteBdd = new NotesBDD(NoteMain.this);
                    noteBdd.open();
                    noteBdd.updatePassword(note.getId(), SHA1(password));
                    noteBdd.close();
                    note.setPassword(SHA1(password));
                    simpleAdpt.notifyDataSetChanged();
                    Toast toast = Toast.makeText(NoteMain.this, NoteMain.this.getString(R.string.toast_pwd_added), Toast.LENGTH_LONG);
                    ((TextView)((LinearLayout) toast.getView()).getChildAt(0)).setTextSize(textSize);
                    toast.show();
                }
            })
            .setNeutralButton(NoteMain.this.getString(R.string.dialog_add_pwd_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.cancel();
                }
            })
            .show();
        }
        else if (item.getTitle().equals(this.getString(R.string.menu_delete)))
        {
            pref = PreferenceManager.getDefaultSharedPreferences(this);

            if (pref.getBoolean("pref_del", false))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                .setTitle(NoteMain.this.getString(R.string.dialog_delete_title) + " " + note.getTitre())
                .setMessage(NoteMain.this.getString(R.string.dialog_delete_msg))
                .setPositiveButton(NoteMain.this.getString(R.string.dialog_delete_yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        deleteNote(note);
                    }
                })
                .setNegativeButton(NoteMain.this.getString(R.string.dialog_delete_no), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                })
                .show();
            }
            else
            {
                deleteNote(note);
            }
        }
        if (item.getTitle().equals(this.getString(R.string.menu_detail)))
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(this.getString(R.string.menu_detail));
            String dateC       = note.getDateCreation();
            String dateM       = note.getDateModification();
            String noteDetails = "<b>" + this.getString(R.string.detail_title) + ": " + note.getTitre() +
                                 "</b> <br/>" + note.getNoteHead(Integer.parseInt(pref.getString("pref_preview_char_limit", "30"))) +
                                 "<br/>" + this.getString(R.string.detail_nb_char) + " : " + note.getNote().length() +
                                 "<br/><i>" + this.getString(R.string.detail_created) + " " + note.getDateCreationFormated() + "</i>";
            if (dateC.equals(dateM))
            {
                noteDetails += "<br/><i>" + this.getString(R.string.detail_not_modified) + " </i>";
            }
            else
            {
                noteDetails += "<br/><i>" + this.getString(R.string.detail_modified) + " " + note.getDateModificationFormated() + "</i>";
            }
            alertDialog.setMessage(Html.fromHtml(noteDetails));
            alertDialog.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    // Do nothing
                }
            });
            // Set the Icon for the Dialog
            alertDialog.show();

            //Change popup content text size
            ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize(textSize);
        }
        else
        {
            return(false);
        }
        simpleAdpt.notifyDataSetChanged();
        return(true);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        NotesBDD noteBdd = new NotesBDD(this);

        noteBdd.open();
        final MenuItem         itemf = item;
        AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo)item.getMenuInfo();
        final Note             note  = simpleAdpt.getItem(aInfo.position);
        noteBdd.close();
        if (note.getPassword() != null)
        {
            final EditText            input = new EditText(NoteMain.this);
            LinearLayout.LayoutParams lp    = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteMain.this);
            builder
            .setTitle(NoteMain.this.getString(R.string.dialog_pwd_title))
            .setMessage(NoteMain.this.getString(R.string.dialog_pwd_msg))
            .setView(input)
            .setPositiveButton(NoteMain.this.getString(R.string.dialog_pwd_submit), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    String password = input.getText().toString();
                    if (note.getPassword().equals(SHA1(password)))
                    {
                        launchMenu(itemf, note);
                    }
                    else
                    {
                        Toast toast = Toast.makeText(NoteMain.this, NoteMain.this.getString(R.string.toast_pwd_error), Toast.LENGTH_LONG);
                        ((TextView)((LinearLayout) toast.getView()).getChildAt(0)).setTextSize(textSize);
                        toast.show();
                    }
                }
            })
            .setNegativeButton(NoteMain.this.getString(R.string.dialog_pwd_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.cancel();
                }
            })
            .show();
        }
        else
        {
            launchMenu(item, note);
        }
        simpleAdpt.notifyDataSetChanged();
        return(true);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
        case KeyEvent.KEYCODE_SEARCH:
            if (editsearch.getVisibility() == View.VISIBLE)
            {
                //Clear research text
                editsearch.setText("");

                editsearch.setVisibility(View.GONE);
                cbSearchCase    = (CheckBox)findViewById(R.id.search_case_cb);
                cbSearchContent = (CheckBox)findViewById(R.id.search_content_cb);
                cbSearchCase.setVisibility(View.GONE);
                cbSearchContent.setVisibility(View.GONE);
                btnClear.setVisibility(View.GONE);
            }
            else
            {
                editsearch.setVisibility(View.VISIBLE);
                if (pref.getBoolean("displaySearchOptions",true))
                {
                    cbSearchCase    = (CheckBox)findViewById(R.id.search_case_cb);
                    cbSearchContent = (CheckBox)findViewById(R.id.search_content_cb);
                    cbSearchCase.setVisibility(View.VISIBLE);
                    cbSearchContent.setVisibility(View.VISIBLE);
                    cbSearchCase.setChecked(!pref.getBoolean(SEARCH_SENSITIVE, false));
                    cbSearchContent.setChecked(pref.getBoolean(SEARCH_CONTENT, false));
                }
                // Button btn_clear is display only when text is typed

            }
            return(true);

        case KeyEvent.KEYCODE_MENU:
            Intent i = new Intent(this, Preference.class);
            startActivity(i);
            return(true);

        default:
            return(super.onKeyUp(keyCode, event));
        }
    }

    public void addNote(View v)
    {
        Intent intentTextEdition = new Intent(NoteMain.this, NoteEdition.class);

        intentTextEdition.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        NoteMain.this.startActivity(intentTextEdition);
    }

    public void quit(View v)
    {
        this.finish();
    }

    public void search(View v)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_SEARCH);
            }
        }).start();
    }
}
