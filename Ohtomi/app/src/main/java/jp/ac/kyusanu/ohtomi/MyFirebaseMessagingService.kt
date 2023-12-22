package jp.ac.kyusanu.ohtomi

import android.Manifest
import android.R
import android.app.Notification
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 0
    }
}