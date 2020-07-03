package cm.ubuea.judie

import android.annotation.TargetApi
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import java.io.IOException
import java.sql.SQLException

class AlarmReceiver : BroadcastReceiver() {
    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        var sr: SingleRowReminder? = null
        val db = DataBase(context)
        try {
            db.Open()
            sr = db.getROW(intent.getStringExtra("id"))
            db.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        val mBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(sr!!.tittle)
                .setContentText(sr.des)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, MainActivity::class.java)
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build())
        val m = MediaPlayer()
        var f: AssetFileDescriptor? = null
        try {
            f = context.assets.openFd("ringtone.mp3")
            m.setDataSource(f.fileDescriptor)
            m.prepare()
            m.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}