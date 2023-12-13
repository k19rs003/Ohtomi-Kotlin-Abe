package jp.ac.kyusanu.ohtomi

import android.R
import android.app.Notification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["message"]
//        val title = remoteMessage.notification!!.title
//        val body = remoteMessage.notification!!.body

        val builder = NotificationCompat.Builder(this, "OhtomiAppPush")
            .setSmallIcon(R.drawable.ic_menu_report_image)
            .setContentTitle(title)
            .setContentText(body)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

//        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
//            .setSmallIcon(R.drawable.ic_menu_report_image)
//            .setContentTitle(title)
//            .setContentText(body)

        val manager = NotificationManagerCompat.from(this)
//        val manager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 0
    }
}