package app.varlorg.unote;

import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.util.regex.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.os.Parcelable;
import android.graphics.Color;
import android.app.UiModeManager;

public class NoteMain extends Activity
{
    private static final String EXTRA_TITLE      = "TitreNoteEdition";
    private static final String EXTRA_NOTE       = "NoteEdition";
    private static final String EXTRA_EDITION    = "edition";
    private static final String EXTRA_PWD    = "pwd";
    private static final String EXTRA_ID         = "id";
    private static final String SEARCH_CONTENT   = "contentSearch";
    private static final String SEARCH_SENSITIVE = "sensitiveSearch";
    private static final String PREF_SORT        = "pref_tri";
    private static final String PREF_SORT_ORDER  = "pref_ordretri";
    public  static final double POPUP_TEXTSIZE_FACTOR    = 0.9;
    public  static final double TOAST_TEXTSIZE_FACTOR    = 0.9;
    public  static final int COLOR_TEXT_DEFAULT    = 0xff999999;

    private static final String HEX = "0123456789ABCDEF";
    private int colorBackground;
    private ArrayAdapter <Note> simpleAdpt;
    private EditText editsearch;
    private TextView searchCount;
    private ImageButton btnClear;
    private List <Note> listeNotes;
    private CheckBox cbSearchContent;
    private CheckBox cbSearchCase;
    private ListView lv;
    private SharedPreferences pref;
    private Parcelable state;
    static private int textSize;
    private int themeID;
    private int menuColor;
    static public void customToastGeneric(Context c, Resources r, String msgToDisplay){
        LinearLayout linearLayout=new LinearLayout(c);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        GradientDrawable shape=new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(50);
        shape.setColor(r.getColor(android.R.color.background_light));
        shape.setStroke(3, r.getColor(android.R.color.transparent));

        TextView textView=new TextView(c);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        textView.setMaxWidth((int)(r.getDisplayMetrics().widthPixels*0.9));
        textView.setText(msgToDisplay);
        textView.setTextSize((int)(textSize*NoteMain.TOAST_TEXTSIZE_FACTOR));
        textView.setTextColor(r.getColor(android.R.color.black));
        textView.setAlpha(1f);
        textView.setBackground(shape);
        int pad_width=(int)(r.getDisplayMetrics().widthPixels*0.04);
        int pad_height=(int)(r.getDisplayMetrics().heightPixels*0.02);
        textView.setPadding(pad_width,pad_height,pad_width,pad_height);

        Toast toast=new Toast(c);

        linearLayout.addView(textView);
        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_LONG);

        toast.show();
    }
    void customToast(String s){
        customToastGeneric(NoteMain.this, NoteMain.this.getResources(), s);
    }
    private LinearLayout passwordPopup(){
        final EditText            input = new EditText(NoteMain.this);
        ImageButton togglePasswordVisibilityButton = new ImageButton(NoteMain.this);
        LinearLayout layout = new LinearLayout(NoteMain.this);

        LinearLayout.LayoutParams lp    = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        input.setTextSize(textSize);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Create an ImageButton for toggling password visibility

        togglePasswordVisibilityButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        togglePasswordVisibilityButton.setImageResource(android.R.drawable.ic_menu_view); // Set your own image resource

        // Add a click listener to toggle password visibility
        togglePasswordVisibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
        input.requestFocus();
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(togglePasswordVisibilityButton);
        layout.addView(input);
        return layout;
    }
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

            if ( pref.getBoolean("pref_search_note_count", true))
                searchCount.setText("" + listeNotes.size());
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
    static public boolean isNightThemeEnabled(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        int nightMode = uiModeManager.getNightMode();
        Log.d(BuildConfig.APPLICATION_ID, " uiModeManager " + nightMode);
        switch (nightMode) {
            case UiModeManager.MODE_NIGHT_YES:
                return true;
            case UiModeManager.MODE_NIGHT_NO:
                return false;
            case UiModeManager.MODE_NIGHT_CUSTOM:
            case UiModeManager.MODE_NIGHT_AUTO:
                int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
            default:
                return false;
        }
    }

    static public void setUi(Activity a, SharedPreferences pref, Context c, Window window){
        if (pref.getBoolean("pref_theme_system", false)) {
            if (isNightThemeEnabled(c)) {
                a.setTheme(android.R.style.Theme_DeviceDefault);
            } else {
                a.setTheme(android.R.style.Theme_DeviceDefault_Light);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }else {
            if (!pref.getBoolean("pref_theme", false))
            {
                a.setTheme(android.R.style.Theme_DeviceDefault);
                //themeID = android.R.style.Theme_DeviceDefault;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View view = window.getDecorView();
                    view.setSystemUiVisibility(view.getSystemUiVisibility());
                    a.getWindow().setStatusBarColor(Color.BLACK);
                }
            }
            else
            {
                a.setTheme(android.R.style.Theme_DeviceDefault_Light);
                //themeID = android.R.style.Theme_DeviceDefault_Light;
                //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                //window.setStatusBarColor(Color.BLACK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View view = window.getDecorView();
                    view.setSystemUiVisibility(view.getSystemUiVisibility() |  View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    a.getWindow().setStatusBarColor(Color.WHITE);
                }
                //}
                /*ActionBar actionBar = this.getActionBar();
                if(actionBar != null) {
                    //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
                    actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                    Log.d(BuildConfig.APPLICATION_ID, " actionBar title " + actionBar.getTitle());
                    actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>uNote</font>"));
                }*/
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setUi(this, pref, getApplicationContext(), getWindow());

        TypedValue tv = new TypedValue();
        getApplicationContext().getTheme().resolveAttribute(android.R.attr.colorBackground, tv, true);
        colorBackground = tv.resourceId;

        setContentView(R.layout.activity_notemain);

        final NotesBDD noteBdd = new NotesBDD(this);
        noteBdd.open();
        listeNotes = noteBdd.getAllNotes(Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
        noteBdd.close();
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

                if (v != null) {
                    if (n.isSelected()) {
                        v.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    } else {
                        v.setBackgroundColor(colorBackground);
                    }
                }
                return(getViewCustom(position, v, viewGroup, n));
            }
        };

        // Draw line separator between note
        /* use xml ?
        <?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" >
    <solid android:color="#FF0000" />
    <size android:height="1dp" />
</shape>
         */
        ShapeDrawable noteSeparator = new ShapeDrawable();
        noteSeparator.setShape(new RectShape());
        menuColor = pref.getInt("pref_note_button_main", 0xff8F8F8F);
        int heightInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        Paint paint = new Paint();
        paint.setColor(menuColor);
        paint.setStrokeWidth(heightInPx);
        noteSeparator.getPaint().set(paint);
        lv.setDivider(noteSeparator);
        lv.setDividerHeight(heightInPx);

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
                    LinearLayout layout = passwordPopup();
                    EditText input = (EditText) layout.getChildAt(1);
                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteMain.this);
                    builder
                    .setTitle(NoteMain.this.getString(R.string.dialog_pwd_title))
                    .setMessage(NoteMain.this.getString(R.string.dialog_pwd_msg))
                    .setView(layout)
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
                                intentTextEdition.putExtra(EXTRA_PWD, n.getPassword()!=null);
                                intentTextEdition.putExtra(EXTRA_ID, n.getId());
                                NoteMain.this.startActivity(intentTextEdition);
                            }
                            else
                            {
                                if ( pref.getBoolean("pref_notifications", true)) {
                                    customToast( NoteMain.this.getString(R.string.toast_pwd_error));
                                }
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
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
                    ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
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
                    intentTextEdition.putExtra(EXTRA_PWD, n.getPassword()!=null);
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
        cbSearchContent.setTextSize((int)(POPUP_TEXTSIZE_FACTOR * textSize));
        cbSearchCase.setTextSize((int)(POPUP_TEXTSIZE_FACTOR * textSize));
        buttonAddNote.setTextSize(textSizeButton);
        buttonSearch.setTextSize(textSizeButton);
        buttonReturn.setTextSize(textSizeButton);
        ((TextView)findViewById(R.id.search_count)).setTextSize(textSizeButton);

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
        searchCount = (TextView) findViewById(R.id.search_count);
        editsearch.setVisibility(View.GONE);
        searchCount.setVisibility(View.GONE);
        editsearch.setTextSize(textSize);
        searchCount.setTextSize(textSize);
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
                updateSearch();
            }
        });

        cbSearchContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                updateSearch();
            }
        });


        cbSearchCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                updateSearch();
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
    }

    public View getViewCustom(int position, View view, ViewGroup viewGroup, Note n)
    {

        String htmlTitleColorAttributeStart = "";
        String htmlTitleColorAttributeEnd = "";
        String htmlNoteColorAttributeStart = "";
        String htmlNoteColorAttributeEnd = "";
        String htmlDetailsColorAttributeStart = "";
        String htmlDetailsColorAttributeEnd = "";

        int colorTitle = pref.getInt("pref_note_text_color_title", COLOR_TEXT_DEFAULT);
        int colorNote = pref.getInt("pref_note_text_color_note", COLOR_TEXT_DEFAULT);
        int colorDetails = pref.getInt("pref_note_text_color_details", COLOR_TEXT_DEFAULT);
        boolean colorBool = pref.getBoolean("pref_note_text_color_main_bool", false);

        // Regex to check valid hexadecimal color code.
        String regex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
        Pattern p = Pattern.compile(regex);

        if (colorBool) {
                htmlTitleColorAttributeStart = "<font color='" + colorTitle + "'>";
                htmlTitleColorAttributeEnd = "</font>";
                htmlNoteColorAttributeStart = "<font color='" + colorNote + "'>";
                htmlNoteColorAttributeEnd = "</font>";
                htmlDetailsColorAttributeStart = "<font color='" + colorDetails + "'>";
                htmlDetailsColorAttributeEnd = "</font>";
        } else {
            int colorAll = pref.getInt("pref_note_text_color_main", COLOR_TEXT_DEFAULT);
            ((TextView)view).setTextColor(colorAll);
        }

        Log.d(BuildConfig.APPLICATION_ID, "htmlTitleColorAttribute  " +  htmlTitleColorAttributeStart);
        String title = n.getTitre();
        String noteSummary = htmlTitleColorAttributeStart + "<b>" + title + "</b>" + htmlTitleColorAttributeEnd;

        if (n.getPassword() != null)
        {
            if (Integer.parseInt(pref.getString("pref_preview_char_limit", "30")) != 0)
                noteSummary += "<br/>" + NoteMain.this.getString(R.string.pwd_protected);
            //noteSummary += "\uD83D\uDD12";
        }
        else
        {
            String noteHeader = n.getNoteHead(Integer.parseInt(pref.getString("pref_preview_char_limit", "30")));
            String noteToDisplay;
            if (noteHeader != "") {
                if (pref.getBoolean("pref_preview_formatting", false)) {
                    noteToDisplay = noteHeader.replace("\n", "<br/>");
                    noteToDisplay = noteToDisplay.replace("   ", "&nbsp;&nbsp;&nbsp;");
                    noteToDisplay = noteToDisplay.replace("  ", "&nbsp;&nbsp;");
                }
                else {
                    noteToDisplay = noteHeader;
                }
                noteSummary += "<br/>" + htmlNoteColorAttributeStart + noteToDisplay  +
                    htmlNoteColorAttributeEnd + htmlDetailsColorAttributeStart;
            }
            if (pref.getBoolean("pref_date", false))
            {
                noteSummary += "<br/>" + n.getDateCreationFormated();
            }
            if (pref.getBoolean("pref_date_mod", false))
            {
                noteSummary += "<br/>modif: " + n.getDateModificationFormated();
            }
        }
        noteSummary += htmlDetailsColorAttributeEnd;

        Log.d(BuildConfig.APPLICATION_ID, "noteSummary  " +  noteSummary);
        ((TextView)view).setText(Html.fromHtml(noteSummary));
        ((TextView)view).setTextSize(textSize);


        ((TextView)view).setPaddingRelative(Integer.parseInt(pref.getString("pref_note_padding_main", "16")),
            ((TextView)view).getPaddingTop(),
            ((TextView)view).getPaddingTop(),
            ((TextView)view).getPaddingTop());
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
        if (pref.getBoolean("pref_main_mode_menu_all", true))
        {
            menu.findItem(R.id.action_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_multi).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        // Set black color to menu item when light theme
        // render diff between svg and png resources when changing color ...
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            menu.findItem(R.id.action_add).getIcon().applyTheme(getTheme());
            menu.findItem(R.id.action_search).getIcon().applyTheme(getTheme());
            menu.findItem(R.id.action_multi).getIcon().applyTheme(getTheme());
            menu.findItem(R.id.action_settings).getIcon().applyTheme(getTheme());
        }*/
        menuColor = pref.getInt("pref_note_button_main", 0xff8F8F8F);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            PorterDuffColorFilter menuColorFilter = new PorterDuffColorFilter(menuColor, PorterDuff.Mode.SRC_IN);
            menu.findItem(R.id.action_add).getIcon()
                    .setColorFilter(menuColorFilter);
            menu.findItem(R.id.action_search).getIcon()
                    .setColorFilter(menuColorFilter);
            menu.findItem(R.id.action_multi).getIcon()
                    .setColorFilter(menuColorFilter);
            menu.findItem(R.id.action_settings).getIcon()
                    .setColorFilter(menuColorFilter);
            getResources().getDrawable(R.drawable.baseline_dynamic_feed_24).setColorFilter(menuColorFilter);
            getResources().getDrawable(R.drawable.baseline_feed_24).setColorFilter(menuColorFilter);
        }
        else {
            menu.findItem(R.id.action_add).getIcon().setTint(menuColor);
            menu.findItem(R.id.action_search).getIcon().setTint(menuColor);
            menu.findItem(R.id.action_multi).getIcon().setTint(menuColor);
            menu.findItem(R.id.action_settings).getIcon().setTint(menuColor);
            getResources().getDrawable(R.drawable.baseline_dynamic_feed_24).setTint(menuColor);
            getResources().getDrawable(R.drawable.baseline_feed_24).setTint(menuColor);
        }
        return super.onCreateOptionsMenu(menu);
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
        else if (id == R.id.action_add) {
            addNote(getWindow().getDecorView().getRootView());
        }
        else if (id == R.id.action_search) {
            search(getWindow().getDecorView().getRootView());
        }
        else if ( id == R.id.action_multi) {
            item.setIcon(R.drawable.baseline_dynamic_feed_24);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                PorterDuffColorFilter menuColorFilter = new PorterDuffColorFilter(menuColor, PorterDuff.Mode.SRC_IN);
                item.getIcon().setColorFilter(menuColorFilter);
            }
            else {
                item.getIcon().setTint(menuColor);
            }
            lv.getChoiceMode();
            Log.d(BuildConfig.APPLICATION_ID, "ListView ChoiceMode " +  lv.getChoiceMode() );

            if (lv.getChoiceMode() != AbsListView.CHOICE_MODE_MULTIPLE_MODAL) {
                if ( pref.getBoolean("pref_notifications", true))
                    customToast(this.getString(R.string.mode_selection));

                item.setIcon(R.drawable.baseline_feed_24);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    PorterDuffColorFilter menuColorFilter = new PorterDuffColorFilter(menuColor, PorterDuff.Mode.SRC_IN);
                    item.getIcon().setColorFilter(menuColorFilter);
                }
                else {
                    item.getIcon().setTint(menuColor);
                }
                lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
                lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                        mode.setTitle(lv.getCheckedItemCount() + " " + getString(R.string.item_selected));
                        final Note note = simpleAdpt.getItem(position);
                        note.setSelected(checked);
                        simpleAdpt.notifyDataSetChanged();
                        Log.d(BuildConfig.APPLICATION_ID, "note clicked " + checked + " " + note.getId() + note.getTitre());
                    }

                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        MenuInflater menuInflater = mode.getMenuInflater();
                        menuInflater.inflate(R.menu.menu_contextual_actionbar, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        //if (item.getTitle().equals(getString(R.string.menu_delete))) {
                        if (item.getItemId() == R.id.menu_delete ) {
                            SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
                            Log.d(BuildConfig.APPLICATION_ID, "checkedItems size " + checkedItems.size());
                            Log.d(BuildConfig.APPLICATION_ID, "checkedItems ts " + checkedItems);
                            Log.d(BuildConfig.APPLICATION_ID, "lv  size " + lv.getCount());
                            Log.d(BuildConfig.APPLICATION_ID, "lv adpt  size " + lv.getAdapter().getCount());
                            List<Note> notesToDelete = new ArrayList<>();
                            for (int i = 0; i < checkedItems.size(); i++) {
                                Log.d(BuildConfig.APPLICATION_ID, "checkedItems heyat " + checkedItems.keyAt(i));
                                Log.d(BuildConfig.APPLICATION_ID, "checkedItems index " + i);
                                Note n =  ((Note)lv.getAdapter().getItem(checkedItems.keyAt(i)));
                                String it = n.getId() + "";
                                Log.d(BuildConfig.APPLICATION_ID, "checkedItems " + it);
                                if (checkedItems.valueAt(i) == true) {
                                    n.setSelected(false);
                                    notesToDelete.add(n);
                                }
                            }
                            if (pref.getBoolean("pref_del", true))
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(NoteMain.this);
                                builder
                                        .setTitle(NoteMain.this.getString(R.string.pref_delete_confirmation) + " " +
                                                checkedItems.size() + " " + NoteMain.this.getString(R.string.item_selected))
                                        .setMessage(NoteMain.this.getString(R.string.dialog_delete_msg))
                                        .setPositiveButton(NoteMain.this.getString(R.string.dialog_delete_yes), new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                for(Note n: notesToDelete)
                                                {
                                                    deleteNote(n, false);
                                                }
                                                lv.clearChoices();
                                                if (pref.getBoolean("pref_notifications", true))
                                                {
                                                    customToast(notesToDelete.size() + " " + getString(R.string.selected_notes_deleted));
                                                }
                                                mode.finish();
                                            }
                                        })
                                        .setNegativeButton(NoteMain.this.getString(R.string.dialog_delete_no), new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                dialog.cancel();
                                            }
                                        });
                                //.show();
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
                                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
                                ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
                            }
                            else
                            {
                                for(Note n: notesToDelete)
                                {
                                    deleteNote(n, false);
                                }
                                lv.clearChoices();
                                if (pref.getBoolean("pref_notifications", true))
                                {
                                    customToast(notesToDelete.size() + " " + getString(R.string.selected_notes_deleted));
                                }
                                mode.finish();
                            }

                        }
                        else if (item.getItemId() == R.id.menu_all ) {
                            if(lv.getCheckedItemCount() == lv.getCount()) {
                                for (int i = 0; i < lv.getCount(); i++) {
                                    lv.setItemChecked(i, false);
                                }
                            } else {
                                for (int i = 0; i < lv.getCount(); i++) {
                                    lv.setItemChecked(i, true);
                                }
                            }
                         }
                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        if (lv.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE_MODAL) {
                            SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
                            Log.d(BuildConfig.APPLICATION_ID, "checkedItems size " + checkedItems.size());
                            Log.d(BuildConfig.APPLICATION_ID, "checkedItems ts" + checkedItems.toString());
                            Log.d(BuildConfig.APPLICATION_ID, "lv  size " + lv.getCount());
                            Log.d(BuildConfig.APPLICATION_ID, "lv adpt  size " + lv.getAdapter().getCount());
                            if (lv.getCount() != 0) {
                                for (int i = 0; i < checkedItems.size(); i++) {
                                    String it = ((Note) lv.getAdapter().getItem(checkedItems.keyAt(i))).getId() + "";
                                    if (checkedItems.valueAt(i)) {
                                        final Note note = ((Note) lv.getAdapter().getItem(checkedItems.keyAt(i)));
                                        note.setSelected(false);
                                        simpleAdpt.notifyDataSetChanged();
                                        Log.d(BuildConfig.APPLICATION_ID, "checkedItems reset" + it);
                                    }
                                }
                            }
                        }
                    }
                });
            }
            else {
                lv.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
                if ( pref.getBoolean("pref_notifications", true))
                    customToast(this.getString(R.string.mode_normal));
            }
        }
        //noinspection SimplifiableIfStatement
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(this.getString(R.string.menu_title));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_edit));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_passwd));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_export));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_delete));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_detail));
        menu.add(0, v.getId(), 0, this.getString(R.string.action_set_alarm));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_share));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_copy));
        menu.add(0, v.getId(), 0, this.getString(R.string.menu_duplicate));
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

    public void exportNote(final Note note)
    {
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
        NotesBDD noteBdd = new NotesBDD(NoteMain.this);
        noteBdd.open();
        String ret = noteBdd.exportNote(getApplicationContext(), note.getId(), exportDate, exportTitle);
        if ( pref.getBoolean("pref_notifications", true))
        {
            customToast(getApplicationContext().getString(R.string.note_exported) + " " + ret);
        }
        noteBdd.close();
    }

    public void deleteNote(final Note note, boolean notification)
    {
        simpleAdpt.remove(note);
        NotesBDD noteBdd = new NotesBDD(NoteMain.this);
        noteBdd.open();
        noteBdd.removeNoteWithID(note.getId());
        noteBdd.close();
        if (editsearch.getVisibility() == View.VISIBLE){
            updateSearch();
        } else {
            listeNotes = noteBdd.getAllNotes(Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
            simpleAdpt.clear();
            simpleAdpt.addAll(listeNotes);
            simpleAdpt.notifyDataSetChanged();
        }
        if (notification)
        {
            customToast(NoteMain.this.getString(R.string.note_deleted));
        }
    }

    public void deleteNote(final Note note)
    {
        deleteNote(note,pref.getBoolean("pref_notifications", true) );
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
            intentTextEdition.putExtra(EXTRA_PWD, note.getPassword()!= null);
            intentTextEdition.putExtra(EXTRA_ID, note.getId());
            NoteMain.this.startActivity(intentTextEdition);
            Log.d("NoteMain", "id " +  note.getId());
        }
        else
        if (item.getTitle().equals(this.getString(R.string.menu_passwd)))
        {
            /*
            final EditText            input = new EditText(NoteMain.this);
            ImageButton togglePasswordVisibilityButton = new ImageButton(NoteMain.this);
            LinearLayout layout = new LinearLayout(NoteMain.this);

            LinearLayout.LayoutParams lp    = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

            input.setLayoutParams(lp);
            input.setTextSize(textSize);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // Create an ImageButton for toggling password visibility

            togglePasswordVisibilityButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
            togglePasswordVisibilityButton.setImageResource(android.R.drawable.ic_menu_view); // Set your own image resource

            // Add a click listener to toggle password visibility
            togglePasswordVisibilityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (input.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });

            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.addView(togglePasswordVisibilityButton);
            layout.addView(input);*/
            LinearLayout layout = passwordPopup();
            EditText input = (EditText) layout.getChildAt(1);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
            .setTitle(NoteMain.this.getString(R.string.dialog_add_pwd_title))
            .setMessage(NoteMain.this.getString(R.string.dialog_add_pwd_msg))
            .setView(layout)
            //.setView(menuPwdView)
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
                    if ( pref.getBoolean("pref_notifications", true))
                    {
                        customToast(NoteMain.this.getString(R.string.toast_pwd_added));
                    }
                }
            })
            .setNeutralButton(NoteMain.this.getString(R.string.dialog_add_pwd_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.cancel();
                }
            });
            //.show();
            final AlertDialog alertDialog = builder.create();
            // Bug on Lollipop when large text size to display the 3 buttons
            // https://stackoverflow.com/questions/27187353/dialog-buttons-with-long-text-not-wrapping-squeezed-out-material-theme-on-an
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    try {
                        LinearLayout linearLayout = (LinearLayout) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).getParent();
                        int wPos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).getWidth();
                        int wNeg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).getWidth();
                        int wNeu = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).getWidth();
                        if (linearLayout != null && wPos + wNeg + wNeu > linearLayout.getWidth()) {
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            linearLayout.setGravity(Gravity.RIGHT);
                        }
                    } catch (Exception ignored) {

                    }
                }
            });
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(Math.min(36,(int)(textSize * POPUP_TEXTSIZE_FACTOR)));
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(Math.min(36,(int)(textSize * POPUP_TEXTSIZE_FACTOR)));
            alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(Math.min(36,(int)(textSize * POPUP_TEXTSIZE_FACTOR)));
            ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
        }
        else if (item.getTitle().equals(this.getString(R.string.action_set_alarm))){
            Intent intentAlarm = new Intent(NoteMain.this,
                ReminderActivity.class);
            intentAlarm.putExtra(EXTRA_TITLE, note.getTitre());
            intentAlarm.putExtra(EXTRA_NOTE, note.getNote());
            intentAlarm.putExtra(EXTRA_EDITION, true);
            intentAlarm.putExtra(EXTRA_PWD, note.getPassword()!= null);
            intentAlarm.putExtra(EXTRA_ID, note.getId());
            Log.d(getClass().getSimpleName(),  "intentAlarm " + intentAlarm);
            NoteMain.this.startActivity(intentAlarm);
        }
        else if (item.getTitle().equals(this.getString(R.string.menu_share)))
        {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, note.getNote());
            sendIntent.putExtra(Intent.EXTRA_TITLE, note.getTitre());
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitre());
            sendIntent.setType("text/plain");
            Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND EXTRA_TITLE" + sendIntent.getStringExtra(Intent.EXTRA_TITLE));
            Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND EXTRA_SUBJECT" + sendIntent.getStringExtra(Intent.EXTRA_SUBJECT));
            Log.d(BuildConfig.APPLICATION_ID, "ACTION_SEND EXTRA_TEXT" + sendIntent.getStringExtra(Intent.EXTRA_TEXT));
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        else if (item.getTitle().equals(this.getString(R.string.menu_export)))
        {
                exportNote(note);
        }
        else if (item.getTitle().equals(this.getString(R.string.menu_copy)))
        {
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            // Creates a new text clip to put on the clipboard.
            ClipData clip = ClipData.newPlainText("uNote copy", note.getNote());
            Log.d(BuildConfig.APPLICATION_ID, " menu_copy - " +  note.getNote());
            // Set the clipboard's primary clip.
            clipboard.setPrimaryClip(clip);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                customToast(this.getString(R.string.note_copied));
                //customToast(getString(android.R.string.copy));

        }
        else if (item.getTitle().equals(this.getString(R.string.menu_duplicate)))
        {
            Note new_note = new Note(note.getTitre(), note.getNote());
            NotesBDD noteBdd = new NotesBDD(NoteMain.this);
            noteBdd.open();
            long rc= noteBdd.insertNote(new_note);
            noteBdd.close();
            if (editsearch.getVisibility() == View.VISIBLE){
                updateSearch();
            } else {
                listeNotes = noteBdd.getAllNotes(Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
                simpleAdpt.clear();
                simpleAdpt.addAll(listeNotes);
                simpleAdpt.notifyDataSetChanged();
            }
            if(rc != -1 ){
                customToast(this.getString(R.string.note_duplicated));
            }
        }
        else if (item.getTitle().equals(this.getString(R.string.menu_delete)))
        {
            pref = PreferenceManager.getDefaultSharedPreferences(this);

            if (pref.getBoolean("pref_del", true))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                /*TextView tv_delete = new TextView(this);
                tv_delete.setText(NoteMain.this.getString(R.string.dialog_delete_title) + " " + note.getTitre().substring(0,32));
                tv_delete.setHeight(textSize*5);
                tv_delete.setGravity(Gravity.CENTER);
                tv_delete.setTextSize((int)(textSize*0.85));*/

                builder
                //.setCustomTitle(tv_delete)
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
                });
                //.show();
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
                ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
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
            /*TextView tv_details = new TextView(this);
            tv_details.setText(this.getString(R.string.menu_detail));
            tv_details.setHeight(textSize*5);
            tv_details.setGravity(Gravity.CENTER);
            tv_details.setTextSize((int)(textSize*0.85));
            alertDialog.setCustomTitle(tv_details);*/
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
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    // Do nothing
                }
            });

            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
            ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
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

        final MenuItem         itemf = item;
        AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo)item.getMenuInfo();
        final Note             note  = simpleAdpt.getItem(aInfo.position);
        if (note.getPassword() != null)
        {
            LinearLayout layout = passwordPopup();
            EditText input = (EditText) layout.getChildAt(1);
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteMain.this);
            builder
            .setTitle(NoteMain.this.getString(R.string.dialog_pwd_title))
            .setMessage(NoteMain.this.getString(R.string.dialog_pwd_msg))
            .setView(layout)
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
                        if ( pref.getBoolean("pref_notifications", true))
                        {
                            customToast(NoteMain.this.getString(R.string.toast_pwd_error));
                        }
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
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
            ((TextView)alertDialog.findViewById(android.R.id.message)).setTextSize((int)(textSize * POPUP_TEXTSIZE_FACTOR));
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
            search(getWindow().getDecorView().getRootView());
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

    public void updateSearch(){
        NotesBDD noteBdd = new NotesBDD(this);
        String text = editsearch.getText().toString();
        noteBdd.open();
        listeNotes = noteBdd.getSearchedNotes(text, cbSearchContent.isChecked(), !cbSearchCase.isChecked(), Integer.parseInt(pref.getString(PREF_SORT, "1")), pref.getBoolean(PREF_SORT_ORDER, false));
        // Clean selected note from old view (selection mode)
        for (int i = 0; i < lv.getCount(); i++) {
            lv.setItemChecked(i, false);
        }
        noteBdd.close();
        simpleAdpt.clear();
        simpleAdpt.addAll(listeNotes);
        lv.setAdapter(simpleAdpt);
        if ( pref.getBoolean("pref_search_note_count", true))
            searchCount.setText("" + listeNotes.size());
    }
    public void search(View v)
    {
        if (editsearch.getVisibility() == View.VISIBLE){
            //Clear research text
            editsearch.setText("");

            editsearch.setVisibility(View.GONE);
            searchCount.setVisibility(View.GONE);
            cbSearchCase    = (CheckBox)findViewById(R.id.search_case_cb);
            cbSearchContent = (CheckBox)findViewById(R.id.search_content_cb);
            cbSearchCase.setVisibility(View.GONE);
            cbSearchContent.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
        } else {
            editsearch.setVisibility(View.VISIBLE);
            searchCount.setVisibility(View.VISIBLE);
            editsearch.requestFocus();
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
    }
}
