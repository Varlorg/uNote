package app.varlorg.unote;

import static app.varlorg.unote.NoteMain.customToastGeneric;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderActivity extends Activity {

    private final String LOG_TAG = getClass().getSimpleName();
    private static final String EXTRA_TITLE   = "TitreNoteEdition";
    private static final String EXTRA_NOTE    = "NoteEdition";
    private static final String EXTRA_EDITION = "edition";
    private static final String EXTRA_PWD = "pwd";
    private static final String EXTRA_ID      = "id";

    /*****
     * Variables for alarm
     ****/
    private TimePicker timePicker;
    private DatePicker datePicker;
    private ImageButton setAlarmButton;
    private ImageButton setCancelAlarmButton;
    private ImageButton setReturnAlarmButton;
    private TextView alarmStatus;

    private SharedPreferences pref;
    private Intent intent;
    private int id;
    private boolean pwd;
    private String title;
    private String note;
    private int textSize;
    void customToast(String s){
        customToastGeneric(this, this.getResources(), s);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        NoteMain.setUi(this, pref, this, getWindow());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        Log.d(LOG_TAG, "onCreate: ");
        timePicker = this.findViewById(R.id.alarmTimePicker);
        datePicker = this.findViewById(R.id.alarmDatePicker);
        setAlarmButton = this.findViewById(R.id.setAlarmButton);
        alarmStatus = this.findViewById(R.id.alarmStatus);
        setCancelAlarmButton = this.findViewById(R.id.setCancelAlarmButton);
        setReturnAlarmButton = findViewById(R.id.setReturnAlarmButton);
        textSize = Integer.parseInt(pref.getString("pref_sizeNote", "18"));
        alarmStatus.setTextSize(textSize);
        View editionAlarmLayout = findViewById(R.id.editionAlarm);

        intent = getIntent();
        id = intent.getIntExtra(EXTRA_ID, 0);
        title = intent.getStringExtra(EXTRA_TITLE);
        note = intent.getStringExtra(EXTRA_NOTE);
        pwd = intent.getBooleanExtra(EXTRA_PWD, false);

        Log.d(LOG_TAG, "id " + id);
        /*if (editionAlarmLayout.getVisibility() == View.VISIBLE) {
            findViewById(R.id.editionAlarm).setVisibility(View.GONE);
        }
        else {*/
            timePicker.setIs24HourView(DateFormat.is24HourFormat(this));
            //findViewById(R.id.editionAlarm).setVisibility(View.VISIBLE);
            if (isAlarmSet(id)) {
                Log.d(LOG_TAG, "Alarm is already set.");
                // Optionally, you can cancel the existing alarm here if you want to reset it.
                // You can use the following code to cancel the alarm:
                // PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                // alarmManager.cancel(pendingIntent);
                Calendar date = getAlarmDateFromPreferences(id);
                Log.d(LOG_TAG,  "" + Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault());
                String formattedDate = dateFormat.format(date.getTime());
                alarmStatus.setText(formattedDate);
            } else {
                Log.d(LOG_TAG, "Alarm is not set. Setting it now with id " + id );
                alarmStatus.setText("/");
            }
        //}

        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "setAlarmButton onClick ");
                setAlarm();
            }
        });
        setCancelAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "setCancelAlarmButton onClick ");
                cancelAlarm(id);
            }
        });
        setReturnAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "setReturnAlarmButton onClick ");
                //findViewById(R.id.editionAlarm).setVisibility(View.GONE);
                quit();
            }
        });

        if (!hasExactAlarmPermission(this)) {
            // Request the permission
            showExactAlarmPermissionDialog();
        }
        if (!hasScheduleExactAlarmPermission(this)) {
            // Request the permission
            showScheduleExactAlarmPermissionDialog();
        }
    }
    public void quit()
    {
        this.finish();
    }
    private boolean isAlarmSet(int requestCode) {
        Intent alarmIntent = new Intent(this, ReminderBroadcastHandler.class);
        // Use FLAG_NO_CREATE to check if a PendingIntent already exists without creating a new one.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        Log.d(LOG_TAG, "isAlarmSet " + pendingIntent + requestCode);
        return pendingIntent != null;
    }
    private void storeAlarmDetailsInPreferences(int requestCode, Calendar calendar) {
        SharedPreferences prefs = this.getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store the alarm time as a long (milliseconds since epoch)
        editor.putLong("alarm_time_" + requestCode, calendar.getTimeInMillis());
        editor.apply();
    }
    private Calendar getAlarmDateFromPreferences(int requestCode) {
        SharedPreferences prefs = this.getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        long alarmTimeMillis = prefs.getLong("alarm_time_" + requestCode, -1); // -1 is a default value if not found

        if (alarmTimeMillis != -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alarmTimeMillis);
            return calendar;
        } else {
            return null; // Alarm not found
        }
    }
    private void cancelAlarm(int requestCode) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, ReminderBroadcastHandler.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            removeAlarmDetailsFromPreferences(requestCode);
            Log.d(LOG_TAG, "cancelAlarm");
            alarmStatus.setText("/");
        }
    }

    private void removeAlarmDetailsFromPreferences(int requestCode) {
        SharedPreferences prefs = this.getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("alarm_time_" + requestCode);
        editor.apply();
    }
    private void setAlarm() {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0); // Set seconds to 0 for consistency

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, ReminderBroadcastHandler.class);

        alarmIntent.putExtra(EXTRA_ID, id);
        alarmIntent.putExtra(EXTRA_TITLE, title);
        if(!pwd) {
            alarmIntent.putExtra(EXTRA_NOTE, note);
        }else {
            alarmIntent.putExtra(EXTRA_NOTE,"");
            // alarmIntent.putExtra(EXTRA_NOTE, getString(R.string.pwd_protected));
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id,
                alarmIntent, PendingIntent.FLAG_IMMUTABLE |PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        final int takeFlags = intent.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // hold android.permission.SCHEDULE_EXACT_ALARM or android.permission.USE_EXACT_ALARM to set exact alarms.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasExactAlarmPermission(this)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                showExactAlarmPermissionDialog();
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        Log.d(LOG_TAG, "alarmManager " + alarmManager);
        Log.d(LOG_TAG, "alarmManager " + id + " " + alarmIntent + " - " + pendingIntent);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());
        alarmStatus.setText(formattedDate);

        // Store alarm details in SharedPreferences
        storeAlarmDetailsInPreferences(id, calendar);

        Log.d(LOG_TAG, "Extras sent: " +
                "\n  " + EXTRA_TITLE + title +
                "\n  " + EXTRA_NOTE + note +
                "\n  " + EXTRA_EDITION + ": true" +
                "\n  " + EXTRA_ID + id);

        Log.d(LOG_TAG, "Alarm set for " + calendar.getTime());
    }
    public boolean hasExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true; // On older versions, this permission is not needed
    }
    public boolean hasScheduleExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(
                    Manifest.permission.SCHEDULE_EXACT_ALARM
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // On older versions, this permission is not needed
    }
    private void showExactAlarmPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exact Alarm Permission Required");
        builder.setMessage("This app needs the ability to set exact alarms to function properly. Please grant the permission in the settings.");
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the case where the user cancels
                //Toast.makeText(Activity.this, "Exact alarm permission is required.", Toast.LENGTH_SHORT).show();
                customToast("Exact alarm permission is required.");
            }
        });
        builder.show();
    }
    private void showScheduleExactAlarmPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Schedule Exact Alarm Permission Required");
        builder.setMessage("This app needs the ability to set exact alarms to function properly. Please grant the permission in the settings.");
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the case where the user cancels
                //Toast.makeText(Activity.this,"Schedule exact alarm permission is required." , Toast.LENGTH_SHORT).show();
                customToast("Schedule exact alarm permission is required.");
            }
        });
        builder.show();
    }

}