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
import android.widget.Toast;

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

    private static final int SET_ALARM_PERMISSION_REQUEST_CODE = 100;
    private static final int POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 101;

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
        checkNotificationPermissions();
        checkAlarmPermissions();
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
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

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
    private void checkAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and above: Check for SCHEDULE_EXACT_ALARM
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Request SCHEDULE_EXACT_ALARM permission
                showScheduleExactAlarmPermissionDialog();
            } else {
                // SCHEDULE_EXACT_ALARM permission is granted
                //Toast.makeText(this, "SCHEDULE_EXACT_ALARM permission granted", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "SCHEDULE_EXACT_ALARM permission granted");
            }
        } else {
            // Below Android 12: Check for SET_ALARM
            if (checkSelfPermission(Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {
                // Request SET_ALARM permission
                requestPermissions(new String[]{Manifest.permission.SET_ALARM}, SET_ALARM_PERMISSION_REQUEST_CODE);
            } else {
                // SET_ALARM permission is granted
                //Toast.makeText(this, "SET_ALARM permission granted", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "SET_ALARM permission granted");
            }
        }
    }

    private void showScheduleExactAlarmPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Schedule Exact Alarm Permission Required");
        builder.setMessage("This app needs the ability to set exact alarms to function properly. Please grant the permission in the settings.");
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
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
    private void checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) and above: Check for POST_NOTIFICATIONS
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request POST_NOTIFICATIONS permission
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE);
            } else {
                // POST_NOTIFICATIONS permission is granted
                //Toast.makeText(this, "POST_NOTIFICATIONS permission granted", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "POST_NOTIFICATIONS permission granted");
            }
        } else {
            // Below Android 13: No need to check for POST_NOTIFICATIONS
            // Notifications are allowed by default
            //Toast.makeText(this, "Notifications are allowed by default on this Android version", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "Notifications are allowed by default on this Android version");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SET_ALARM_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow in your app.
                //Toast.makeText(this, "SET_ALARM permission granted", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "SET_ALARM permission granted");
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied.
                Toast.makeText(this, "SET_ALARM permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow in your app.
                //Toast.makeText(this,"POST_NOTIFICATIONS permission granted" , Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "POST_NOTIFICATIONS permission granted");
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied.
                Toast.makeText(this, "POST_NOTIFICATIONS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}