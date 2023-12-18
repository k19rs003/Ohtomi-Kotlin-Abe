package jp.ac.kyusanu.ohtomi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.TextView
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import android.os.StrictMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.messaging.FirebaseMessaging
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.android.gms.tasks.OnCompleteListener
import jp.ac.kyusanu.ohtomi.databinding.ActivityMainBinding

//lateinit var communicationTextView: TextView
//lateinit var informationTextView: TextView
//lateinit var gpsGapTextView: TextView
//var successCount: Int = 0
//var failureCount: Int = 0
//var previousLatitude: Double = 0.0
//var previousLongitude: Double = 0.0
//var previousTimeMillis: Long = 0
//var sensorX: Float = 0.toFloat()
//var sensorY: Float = 0.toFloat()
//var sensorZ: Float = 0.toFloat()
//var heartRateSum = 0
//var heartRateCount = 0

//lateinit var sharedPreferences: SharedPreferences

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {

        lateinit var sharedPreferences: SharedPreferences

        @SuppressLint("StaticFieldLeak")
        lateinit var communicationTextView: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var informationTextView: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var gpsGapTextView: TextView

        var sensorX: Float = 0.toFloat()
        var sensorY: Float = 0.toFloat()
        var sensorZ: Float = 0.toFloat()

        var heartRateSum = 0
        var heartRateCount = 0
    }

    // wear
    private val TAG = "MainActivity"
    private var dataPath = "verify_remote_example_phone_app"

    private var sensorManager: SensorManager? = null
    private var accelometer: Sensor? = null

    private val REQUEST_MULTI_PERMISSIONS = 101

    private var isAllowedLocation = false
    private var isAllowedReadPhoneState = false

    val REQUEST_CODE = 5432

    private val version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})  "

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
        setTextView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /// レイアウトXMLからメニュー実装
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        val item = menu.findItem(R.id.action_sensor)
        item.icon?.apply {
            mutate() // Drawableを変更可能にする
            colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN) // アイコンを白くする
        }
        return true
    }


    private fun setTextView() {

        communicationTextView = binding.communicationTextView
        informationTextView = binding.informationTextView
        gpsGapTextView = binding.gpsGapTextView
    }

    @SuppressLint("SetTextI18n")

    private fun setup() {

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //ACCELEROMETER -> TYPE_LINEAR_ACCELERATION
        accelometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        sensorManager!!.registerListener(
            this, accelometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_key), MODE_PRIVATE)

        // アプリの設定のチェック
        checkMultiPermissions()

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // 通信状態を表示
//        communicationTextView = findViewById<View>(R.id.communication_text_view) as TextView

        // デバイス番号，収集時間，緯度経度を表示
//        informationTextView = findViewById<View>(R.id.information_text_view) as TextView

        // GPSの差分表示
//        gpsGapTextView = findViewById<View>(R.id.gps_gap_text_view) as TextView

        // アプリのバージョンを表示
//        val textViewVersion = findViewById<View>(R.id.version_text_view) as TextView
//        textViewVersion.text = version
        binding.versionTextView.text = version

        // 端末のサイズを収集
        //        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels

        // 会社のモットーを表示
//        val imageViewMotto: ImageView = findViewById<View>(R.id.motto_image_view) as ImageView
        val layoutParams = binding.mottoImageView.layoutParams
        layoutParams.height = (java.lang.Double.valueOf(height / 3.0)).toInt()
        binding.mottoImageView.layoutParams = layoutParams

        // スタートボタン
//        val startButton: Button = findViewById<View>(R.id.start_button) as Button
        binding.startButton.height = (java.lang.Double.valueOf(height / 8.0)).toInt()
        binding.startButton.setOnClickListener(listener)
        Log.d("debug", "onButtonStartCommand()")

        // ストップボタン
//        val stopButton: Button = findViewById<View>(R.id.stop_button) as Button
        binding.stopButton.height = (java.lang.Double.valueOf(height / 8.0)).toInt()
        binding.stopButton.setOnClickListener(listener)
        Log.d("debug", "onButtonStopCommand()")

        // アクションバーの色
        if (runnable != null || locationCallback != null) {
            supportActionBar?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorPrimary
                    )
                )
            )
        } else {
            binding.communicationTextView.text = "Success:(0), Failure:(0)  "
            supportActionBar?.setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorAccent
                    )
                )
            )
        }

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        Log.d("build", "ID:" + Build.ID)
        val channel = NotificationChannel(
            "OhtomiAppPush",
            "Push notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("token", token)

            "http://133.17.165.165:8086/ohtomi/deviceTokenA.php".httpPost()
                .body(token.toByteArray()).response { request, response, result ->
                    Log.d("debug", request.toString())

                    when (result) {
                        is Result.Success -> {
                            Log.d("post 成功", String(response.data))
                        }
                        is Result.Failure -> {
                            Log.e("post 失敗", String(response.data))
                        }
                    }
                }
        })
    }

//    override fun onResume() {
//        super.onResume()
////        Wearable.getMessageClient(this).addListener(this)
//    }
//
//    override fun onPause() {
//        super.onPause()
////        Wearable.getMessageClient(this).removeListener(this)
//    }

    override fun onDestroy() {
        super.onDestroy()

        ForegroundService().stopLocationUpdates()
        // Listenerを解除
//        sensorManager?.unregisterListener(this)
        Wearable.getMessageClient(this@MainActivity).removeListener(wearListener)
    }

    override fun onSensorChanged(event: SensorEvent) {
        //ここで変数宣言すると，起動中は破棄されずメモリリークするそうな
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (sensorX < event.values[0]) sensorX = event.values[0]
            if (sensorY < event.values[1]) sensorY = event.values[1]
            if (sensorZ < event.values[2]) sensorZ = event.values[2]
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

//    override fun onMessageReceived(messageEvent: MessageEvent) {
//
////        if (messageEvent.path == datapath) {
////            val data = messageEvent.data.toString(Charsets.UTF_8) //文字列に変換
//////            Log.d("debug", data)
////            /* 受け取ったデータを処理 */
////            Log.d("debug", data)
////            heatbeat = data
////            Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
////        }
//    }

    // 位置情報、IMEI読込許可の確認
    private fun checkMultiPermissions() {
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionReadPhoneState = ContextCompat.checkSelfPermission(
            this,

            Manifest.permission.READ_PHONE_STATE
        )

        val reqPermissions = arrayListOf<String>()

        // permission が許可されているか確認
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            isAllowedLocation = true
            Log.d("debug", "permissionLocation:GRANTED")
        } else {
            reqPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissionReadPhoneState == PackageManager.PERMISSION_GRANTED) {
            isAllowedReadPhoneState = true
            Log.d("debug", "permissionExtStorage:GRANTED")
        } else {
            reqPermissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (reqPermissions.isNotEmpty()) {

            val array = arrayOfNulls<String>(reqPermissions.size)
            //           reqPermissions.toArray(array)

            ActivityCompat.requestPermissions(
                this,
                reqPermissions.toArray(array),
                REQUEST_MULTI_PERMISSIONS
            )
        } else {
            //           startLocationService()
        }
    }

    // 結果の受け取り
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {

        if (requestCode == REQUEST_MULTI_PERMISSIONS) {
            if (grantResults.isNotEmpty()) {
                for (i in permissions.indices) {
                    if (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isAllowedLocation = true
                        } else {
                            // それでも拒否された時の対応
                            //                           message.append("位置情報の許可がないので計測できません\n")
                            Toast.makeText(
                                this@MainActivity,
                                "位置情報の許可がないので計測できません",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (permissions[i] == Manifest.permission.READ_PHONE_STATE) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isAllowedReadPhoneState = true
                        } else {
                            // それでも拒否された時の対応
                            //                           message.append("IMEI読込の許可がないので識別できません\n")
                            Toast.makeText(
                                this@MainActivity,
                                "IMEI読込の許可がないので識別できません",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

//                startLocationService()

            }
        }
    }

    private val listener = View.OnClickListener { view ->
        when (view!!.id) {
            R.id.start_button -> {

                val intent = Intent(this@MainActivity, ForegroundService::class.java)
                intent.putExtra("REQUEST_CODE", 1)
                Log.d("debug", "Tapped")
                ContextCompat.startForegroundService(this@MainActivity, intent)

                println(
                    "aaaaaaa : ${
                        Wearable.getMessageClient(this@MainActivity)
                            .addListener(wearListener).isSuccessful
                    }"
                )
                Wearable.getMessageClient(this@MainActivity).addListener(wearListener)

                supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.colorPrimary
                        )
                    )
                )

            }

            R.id.stop_button -> {

                Toast.makeText(this@MainActivity, "計測を終了します", Toast.LENGTH_SHORT).show()
                ForegroundService().stopLocationUpdates()

                Wearable.getMessageClient(this@MainActivity).removeListener(wearListener)

                supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.colorAccent
                        )
                    )
                )
            }
        }
    }

    private val wearListener = MessageClient.OnMessageReceivedListener { messageEvent ->
        if (messageEvent.path == dataPath) {
            val data = messageEvent.data.toString(Charsets.UTF_8) //文字列に変換
            /* 受け取ったデータを処理 */
            Log.d("debug wearListener", data)
            heartRateSum += data.toFloat().toInt()
            heartRateCount += 1
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_sensor -> {
            intent = Intent(this, Co2Activity::class.java)
            startActivity(intent)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}
