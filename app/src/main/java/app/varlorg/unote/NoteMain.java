package app.varlorg.unote;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.*;//ViewGroup;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.app.Instrumentation;

public class NoteMain extends Activity
{

    final String EXTRA_TITLE = "TitreNoteEdition";
    final String EXTRA_NOTE = "NoteEdition";
    final String EXTRA_EDITION = "edition";
    final String EXTRA_ID = "id";
    ArrayAdapter<Note> simpleAdpt;
    private EditText editsearch;
    private Button btnClear;
    List<Note> listeNotes;
    NotesBDD noteBdd;
    ListView lv;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            if (pref.getBoolean("pref_theme",false) == false) {
                setTheme(android.R.style.Theme_DeviceDefault);
            } else {
                setTheme(android.R.style.Theme_DeviceDefault_Light);
            }
        } else{
            if (pref.getBoolean("pref_theme",false) == false) {
                setTheme(android.R.style.Theme_Black);
            } else {
                setTheme(android.R.style.Theme_Light);
            }
        }

        setContentView(R.layout.activity_notemain);

        noteBdd = new NotesBDD(this);
        noteBdd.open();
        listeNotes = noteBdd.getAllNotes(Integer.parseInt(pref.getString("pref_tri", "1")), pref.getBoolean("pref_ordretri", false));
        /****************************************************************************************/
     // The data to show
        lv = (ListView) findViewById(R.id.listView);
        simpleAdpt = new ArrayAdapter<Note>(this, R.layout.notelist, listeNotes ){
        		public View getView(int position, View view, ViewGroup viewGroup)
        		{
        			view = super.getView(position, view, viewGroup);
                    Note n = (Note) this.getItem(position);
                    String note_summary;
                    if (n.getPassword()!= null )
                    {
                        note_summary = new String("<b>" + n.getTitre() + "</b> <br/>Password protected");
                    }
                    else {
                        note_summary = new String("<b>" + n.getTitre() + "</b> <br/>" + n.getNoteHead());
                        if (pref.getBoolean("pref_date", false) == true)
                            note_summary += "<br/>" + n.getDateCreationFormated();
                        if (pref.getBoolean("pref_date_mod", false) == true)
                            note_summary += "<br/>modif: " + n.getDateModificationFormated();
                    }
                    ((TextView) view).setText(Html.fromHtml(note_summary));
                    return view;
        		}
        };
        lv.setAdapter(simpleAdpt);
     // React to user clicks on item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         
             public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                     long id) {
            	final Note n = (Note) parentAdapter.getItemAtPosition(position);
                 // We know the View is a TextView so we can cast it
                //TextView clickedView = (TextView) view
                boolean can_edit = false;
                if (n.getPassword()!= null ) {
                    final EditText input = new EditText(NoteMain.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteMain.this);
                    builder
                            .setTitle("Asking password")
                            .setMessage("Enter Password")
                            .setView(input)
                            .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    String password = input.getText().toString();
                                    NotesBDD noteBdd = new NotesBDD(NoteMain.this);
                                    if ( n.getPassword().equals(SHA1(password)) ) {
                                        Intent intentTextEdition = new Intent(NoteMain.this ,
                                                NoteEdition.class);
                                        intentTextEdition.putExtra(EXTRA_TITLE, n.getTitre());
                                        intentTextEdition.putExtra(EXTRA_NOTE, n.getNote());
                                        intentTextEdition.putExtra(EXTRA_EDITION, true);
                                        intentTextEdition.putExtra(EXTRA_ID, n.getId());
                                        NoteMain.this.startActivity(intentTextEdition);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .show();

                }else {
                    can_edit = true;
                }
                 if (can_edit) {
                    Intent intentTextEdition = new Intent(NoteMain.this ,
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
               
        // Locate the EditText in listview_main.xml
        editsearch = (EditText) findViewById(R.id.search);
        editsearch.setVisibility(View.GONE);
        // Capture Text in EditText
        editsearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

               // adapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String text = editsearch.getText().toString().toLowerCase(Locale.getDefault());
                ArrayList<Note> listeNotesRecherche = noteBdd.getSearchedNotes(text, pref.getBoolean("contentSearch", false));
                simpleAdpt = new ArrayAdapter<Note>	(getApplicationContext(), R.layout.notelist, listeNotesRecherche ){
                    public View getView(int position, View view, ViewGroup viewGroup)
                    {
                        view = super.getView(position, view, viewGroup);
                        Note n = (Note) this.getItem(position);
                        String note_summary;
                        if (n.getPassword()!= null )
                        {
                            note_summary = new String("<b>" + n.getTitre() + "</b> <br/>Password protected");

                        }
                        else {
                            note_summary = new String("<b>" + n.getTitre() + "</b> <br/>" + n.getNoteHead());
                            note_summary += "<br/>" + n.getPassword();
                            if (pref.getBoolean("pref_date", false) == true)
                                note_summary += "<br/>" + n.getDateCreationFormated();
                            if (pref.getBoolean("pref_date_mod", false) == true)
                                note_summary += "<br/>modif: " + n.getDateModificationFormated();
                        }
                        ((TextView) view).setText(Html.fromHtml(note_summary));
                        return view;
                    }
            };
            lv.setAdapter(simpleAdpt);
                //simpleAdpt.notifyDataSetChanged();
            }
        });
        btnClear = (Button)findViewById(R.id.btn_clear);
        //set on text change listener for edittext
        editsearch.addTextChangedListener(textWatcher());
        //set event for clear button
        btnClear.setOnClickListener(onClickListener());
        noteBdd.close();
    }

    private View.OnClickListener onClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editsearch.setText(""); //clear edittext
            }
        };
    }

    private TextWatcher textWatcher() {
        return new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!editsearch.getText().toString().equals("")) { //if edittext include text
                    btnClear.setVisibility(View.VISIBLE);
                } else { //not include text
                    btnClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intentPreference = new Intent(NoteMain.this,
                    Preference.class);
            intentPreference.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            NoteMain.this.startActivity(intentPreference);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Note Menu");
        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0, "Password");
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Details");
    }

    private final static String HEX = "0123456789ABCDEF";
    public static String SHA1(String text) {

        try {

            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(text.getBytes("UTF-8"),
                    0, text.length());
            byte[] sha1hash = md.digest();

            return toHex(sha1hash);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void appendHex(StringBuffer sb, byte b) {

        sb.append(HEX.charAt((b >> 4) & 0x0f))
                .append(HEX.charAt(b & 0x0f));

    }
    public static String toHex(byte[] buf) {

        if (buf == null) return "";

        int l = buf.length;
        StringBuffer result = new StringBuffer(2 * l);

        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }

        return result.toString();

    }

    public boolean launchMenu(MenuItem item,final Note note)
    {
        if(item.getTitle().equals("Edit"))
        {
            Intent intentTextEdition = new Intent(NoteMain.this ,
                    NoteEdition.class);
            intentTextEdition.putExtra(EXTRA_TITLE, note.getTitre());
            intentTextEdition.putExtra(EXTRA_NOTE, note.getNote());
            intentTextEdition.putExtra(EXTRA_EDITION, true);
            intentTextEdition.putExtra(EXTRA_ID, note.getId());
            NoteMain.this.startActivity(intentTextEdition);
        }
        else
        if(item.getTitle().equals("Password")) {
            final EditText input = new EditText(NoteMain.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Adding password")
                    .setMessage("Enter Password")
                    .setView(input)
                    .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            NotesBDD noteBdd = new NotesBDD(NoteMain.this);
                            noteBdd.open();
                            note.setPassword(null);
                            noteBdd.updatePassword(note.getId(), null);
                            noteBdd.close();
                            simpleAdpt.notifyDataSetChanged();
                        }
                    })
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String password = input.getText().toString();
                            String passwordHashed = SHA1(password);
                            /******************************************************************/

                            NotesBDD noteBdd = new NotesBDD(NoteMain.this);
                            noteBdd.open();
                            noteBdd.updatePassword(note.getId(), passwordHashed);
                            noteBdd.close();
                            note.setPassword(passwordHashed);
                            simpleAdpt.notifyDataSetChanged();
                            /*simpleAdpt.remove(note);
                            simpleAdpt.add(note);
                            listeNotes = noteBdd.getAllNotes(Integer.parseInt(pref.getString("pref_tri", "1")), pref.getBoolean("pref_ordretri", false));
                            simpleAdpt = new ArrayAdapter<Note>(NoteMain.this, R.layout.notelist, listeNotes );
                            lv.setAdapter(simpleAdpt);*/

                            Toast.makeText(NoteMain.this, "Password added ! ", Toast.LENGTH_LONG).show();

                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();

        }
        else if(item.getTitle().equals("Delete"))
        {
            pref = PreferenceManager.getDefaultSharedPreferences(this);

            if (pref.getBoolean("pref_del",false) == true) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setTitle("Deleting Note")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                simpleAdpt.remove(note);
                                NotesBDD noteBdd = new NotesBDD(NoteMain.this);
                                noteBdd.open();
                                noteBdd.removeNoteWithID(note.getId());
                                Toast.makeText(NoteMain.this, "Note deleted ! ", Toast.LENGTH_LONG).show();
                                simpleAdpt.notifyDataSetChanged();
                                noteBdd.close();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
            else
            {
                simpleAdpt.remove(note);
                noteBdd = new NotesBDD(NoteMain.this);
                noteBdd.open();
                noteBdd.removeNoteWithID(note.getId());
                Toast.makeText(NoteMain.this, "Note deleted ! ", Toast.LENGTH_LONG).show();
                simpleAdpt.notifyDataSetChanged();
                noteBdd.close();
            }
            // Refresh main activity upon close of dialog box
           /*Intent refresh = new Intent(this, NoteMain.class);
            startActivity(refresh);
            this.finish(); //*/
            //onCreate(null);
        }
        if(item.getTitle().equals("Details"))
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Details");
            String dateC = note.getDateCreation();
            String dateM = note.getDateModification();
            if (dateC.equals(dateM))
            {
                alertDialog.setMessage(Html.fromHtml("<b>Title : "+note.getTitre() +
                        "</b> <br/>"+note.getNoteHead() +
                        "<br/>Nombre de caractères : " +note.getNote().length() +
                        "<br/><i>Created the "+ note.getDateCreationFormated()+"</i>"+
                        "<br/><i>Not modified </i>"));
            }
            else
            {
                alertDialog.setMessage(Html.fromHtml("<b>Title : "+note.getTitre() +
                        "</b> <br/>"+note.getNoteHead() +
                        "<br/>Nombre de caractères : " +note.getNote().length() +
                        "<br/><i>Created the "+ note.getDateCreationFormated()+"</i>" +
                        "<br/><i>Modified the "+ note.getDateModificationFormated() +"</i>"));
            }

            alertDialog.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    // TODO Add your code for the button here.
                }
            });
            // Set the Icon for the Dialog
            alertDialog.show();

        }
        else
        {
            return false;
        }
        simpleAdpt.notifyDataSetChanged();
        return true;
    }
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        NotesBDD noteBdd = new NotesBDD(this);
        noteBdd.open();
        final MenuItem itemf = item;
        AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        int noteid = (int) item.getItemId();
        final Note note = (Note) simpleAdpt.getItem(aInfo.position);
        noteBdd.close();
        if (note.getPassword()!= null ) {
            final EditText input = new EditText(NoteMain.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteMain.this);
            builder
                    .setTitle("Asking password")
                    .setMessage("Enter Password")
                    .setView(input)
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String password = input.getText().toString();
                            NotesBDD noteBdd = new NotesBDD(NoteMain.this);
                            if ( note.getPassword().equals(SHA1(password)) ) {
                                launchMenu(itemf,note);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();

        }
        else {
            launchMenu(item, note);
        }
        simpleAdpt.notifyDataSetChanged();
        return true;

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SEARCH:
                if(editsearch.getVisibility() == View.VISIBLE)
                {
                    editsearch.setVisibility(View.GONE);
                }
                else
                {
                    editsearch.setVisibility(View.VISIBLE);
                }
                return true;
            case KeyEvent.KEYCODE_MENU:
                Intent i = new Intent(this, Preference.class);
                startActivity(i);
                return true;

            default:
                return super.onKeyUp(keyCode, event);
        }
    }
	public void addNote(View v )
    {
        Intent intentTextEdition = new Intent(NoteMain.this, NoteEdition.class);
        intentTextEdition.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        NoteMain.this.startActivity(intentTextEdition);
    }
    public void quit(View v)
    {
        this.finish();
    }

    public void search(View v){
        new Thread(new Runnable() {
            @Override
            public void run() {
            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_SEARCH);
            }
        }).start();
    }
}


