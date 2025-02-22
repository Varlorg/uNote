package app.varlorg.unote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ReminderBroadcastHandler extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String EXTRA_TITLE   = "TitreNoteEdition";
    private static final String EXTRA_NOTE    = "NoteEdition";
    private static final String EXTRA_ID      = "id";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(BuildConfig.APPLICATION_ID, "AlarmReceiver onReceive");

        // Create the notification channel (for Android Oreo and above)
        createNotificationChannel(context);

        // Create the notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }
        // Retrieve extras from the Intent
        String title = intent.getStringExtra(EXTRA_TITLE);
        String note = intent.getStringExtra(EXTRA_NOTE);
        if (note != null) {
            note = note.substring(0, Math.min(note.length(), 20));
        }
        int id = intent.getIntExtra(EXTRA_ID, 0);

        Log.d(BuildConfig.APPLICATION_ID, "AlarmReceiver Extra: " + title
                + "\nnote: " + note + "\nid: " +   id);

        builder.setSmallIcon(R.drawable.ic_notification) // Replace with your icon
            .setContentTitle(title)
            .setContentText(note)
            .setAutoCancel(true);
        //.setContentIntent(pendingIntent);

        // Create an Intent to open when the notification is clicked
        Intent notificationIntent = new Intent(context, NoteMain.class);
        /*Intent notificationIntent = new Intent(context, NoteEdition.class);
        notificationIntent.putExtra(EXTRA_TITLE, title);
        notificationIntent.putExtra(EXTRA_NOTE, note);
        notificationIntent.putExtra(EXTRA_EDITION,true);
        notificationIntent.putExtra(EXTRA_ID, id);*/
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        // Send the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel"; // Replace with your channel name
            String description = "Channel for alarm notifications"; // Replace with your channel description
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}