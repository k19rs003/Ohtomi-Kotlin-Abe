package jp.ac.kyusanu.ohtomi

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.android.gms.location.*
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.communicationTextView
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.gpsGapTextView
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.heartRateCount
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.heartRateSum
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.informationTextView
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.sensorX
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.sensorY
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.sensorZ
import jp.ac.kyusanu.ohtomi.MainActivity.Companion.sharedPreferences
import java.text.SimpleDateFormat
import java.util.*

lateinit var fusedLocationProviderClient: FusedLocationProviderClient
lateinit var fusedLocationProviderClientCallback: FusedLocationProviderClient

var locationCallback: LocationCallback? = null

val handler = Handler()
var runnable: Runnable? = null

var deviceNumber: Int = 0

var calculatedSpeed: Double = 0.0
var distance: Double = 0.0
var timeGap: Long = 0

var heartRate = "" // 心拍数

class ForegroundService : Service() {

//    private lateinit var communicationTextView: TextView
//    private lateinit var informationTextView: TextView
//    private lateinit var gpsGapTextView: TextView

    private var successCount: Int = 0
    private var failureCount: Int = 0

    private var previousLatitude: Double = 0.0
    private var previousLongitude: Double = 0.0
    private var previousTimeMillis: Long = 0

    val REQUEST_CODE = 5432

    private var imeiNumber: String = "999999999999999"

    override fun onCreate() {
        super.onCreate()
        Log.d("DEBUG", "onCreate")

        // アプリ起動時にチャンネル作成するほうがいい
        ForegroundServiceNotification.createNotificationChannel(applicationContext)

        setTextView()
    }

    private fun setTextView() {

//        communicationTextView = MainActivity.communicationTextView
//        informationTextView = MainActivity.informationTextView
//        gpsGapTextView = MainActivity.gpsGapTextView
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    // フォラグランドサービスの開始
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun startForegroundService() {

        Log.d("DEBUG", "startForegroundService()")

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(applicationContext, REQUEST_CODE, intent, FLAG_MUTABLE)

        // startForegroundServiceでサービス起動から5秒以内にstartForegroundして通知を表示しないとANRエラーになる
        val notification = ForegroundServiceNotification.createServiceNotification(
            applicationContext,
            pendingIntent
        )
        startForeground(
            ForegroundServiceNotification.FOREGROUND_SERVICE_NOTIFICATION_ID,
            notification
        )

    }

    private fun checkPermission() {

        val imei: String? = sharedPreferences.getString(getString(R.string.preferences_key), null)

        if (imei == null) {

            Log.d("DEBUG", "imei null")

            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED
            ) {
//            // TANAKA
//            // Permission is  granted
//            val imei: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                telephonyManager.imei
//            } else { // older OS  versions
//                telephonyManager.getDeviceId()
//            }

                // TAISEI
                val imei: String? = when {
                    // Android 10(29)以上
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Settings.Secure.getString(
                        this.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    // Android 8(26)以上 Android 9(28)以下
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> telephonyManager.imei
                    // Android 8(26)より下
                    else -> telephonyManager.getDeviceId()
                }

                imei?.let {
                    Log.d("debug", "DeviceId=$imei")

                    val editor = sharedPreferences.edit()
                    editor.putString(getString(R.string.preferences_key), imei)
                    editor.apply()

                    imeiNumber = imei
                }

            } else {  // Permission is not granted
                Log.d("debug", "Permission is not granted")
            }
        } else {
            Log.d("DEBUG", "imei non null")

            imeiNumber = imei
        }
//        val uuidString = UUID.randomUUID().toString()
//        Log.d("debug", uuidString)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.d("debug", "onStartCommand()")

        checkPermission()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // API 24 or more

            if (locationCallback == null) {     // Callback

                startForegroundService()
                goLocationCallback()
                Toast.makeText(this, "計測を開始します", Toast.LENGTH_SHORT).show()

                successCount = 0
                failureCount = 0
                communicationTextView.setText("Success:(${successCount}), Failure:(${failureCount})  ")

            } else {

                Toast.makeText(this, "すでに計測を開始しています", Toast.LENGTH_SHORT).show()

            }

        } else {                                            // API 23 or less

            if (runnable == null) {             // Handler
                goReceiveLocation()
                Toast.makeText(this, "計測を開始します", Toast.LENGTH_SHORT).show()

                successCount = 0
                failureCount = 0
                communicationTextView.setText("Success:(${successCount}), Failure:(${failureCount})  ")

            } else {

                Toast.makeText(this, "すでに計測を開始しています", Toast.LENGTH_SHORT).show()

            }

        }

        return START_STICKY

    }

    private fun goReceiveLocation() {

        runnable = object : Runnable {
            override fun run() {

                receiveLocation()
                handler.postDelayed(this, 15000)

            }
        }
//        handler.post(runnable)
        runnable?.let { handler.post(it) }

    }

    private fun goLocationCallback() {

//        fusedLocationProviderClientCallback = FusedLocationProviderClient(this)
        fusedLocationProviderClientCallback = LocationServices.getFusedLocationProviderClient(this)

        // どのような取得方法を要求
        val locationRequest = LocationRequest().apply {
            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
            // 今回は公式のサンプル通りにする。
            interval = 15000                                    // 最遅の更新間隔(但し正確ではない。)
            fastestInterval = 15000                             // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY   // 精度重視
        }

        // コールバック
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                // 更新直後の位置が格納されているはず
                val location = p0?.lastLocation ?: return

                Log.d(
                    "debug",
                    "(コールバック)緯度:${location.latitude}, 経度:${location.longitude}, 速度${location.speed}, 方位角${location.bearing}"
                )
                http(8, location.latitude, location.longitude, location.speed, location.bearing)

            }
        }

        // 位置情報を更新
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
        Looper.myLooper()?.let {
            fusedLocationProviderClientCallback.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                it
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun receiveLocation() {

        // Google Play Services APIの確認。Play Serviceが入っていない端末やバージョンが古い端末は位置情報を取得しない。
        // 位置情報が取得できなくても地図表示はさせたいのでreturnする。ここで終わりたいならfinishなど呼べば良いと思う。
        // そしてDeveloperサイトに従って判定しているが実際ちゃんと判定されるかは未検証。。
//        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
//            // デフォルト位置とか設定するかマップ自体表示させず画面閉じるか何かする。
//            return
//       }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.let { client ->
            client.lastLocation.addOnSuccessListener { location ->

                if (location != null) {

                    http(
                        0,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getSpeed(),
                        location.getBearing()
                    )
                    Log.d(
                        "debug",
                        "(addOnSuccessListener)緯度:${location.getLatitude()}, 経度:${location.getLongitude()}"
                    )

                } else {

                    http(0, 33.68, 130.4, 0.0f, 0.0f)
                    Log.d("debug", "Location Failure")

                }
            }
        }
    }

    fun stopLocationUpdates() {

//        runnable?.let { handler.removeCallbacks(runnable) }
        runnable?.let { handler.removeCallbacks(it) }
        locationCallback?.let {
            fusedLocationProviderClientCallback.removeLocationUpdates(
                locationCallback!!
            )
        }

        runnable = null
        locationCallback = null
        Log.d("debug", "stopLocationUpdates()")

    }

    private fun http(
        count: Int,
        latitude: Double,
        longitude: Double,
        speed: Float,
        bearing: Float
    ) {

        var wayLatitude: Double = latitude
        var wayLongitude: Double = longitude

        if (deviceNumber == 0) {

            "http://133.17.165.177:8086/ocs_insertIMEI.php?IMEI=${imeiNumber}".httpGet()
                .response { _, response, result ->
                    when (result) {
                        is Result.Success -> {
                            deviceNumber = Integer.parseInt(String(response.data).trim())
                            putLocationUpdates(count, wayLatitude, wayLongitude, speed, bearing)
                        }
                        is Result.Failure -> {
                            failureCount += 1
                            communicationTextView.setText("Success:(${successCount}), Failure:(${failureCount})  ")
                            Log.d("debug", "Failure")
                            println("===========通信に失敗しました。===========")
                        }
                    }
                }

        } else {

            putLocationUpdates(count, wayLatitude, wayLongitude, speed, bearing)

        }
    }

    private fun getBattery(): Int {

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this.registerReceiver(null, ifilter)
        }

        val batteryPct = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level / scale.toFloat() * 100
        }

        return batteryPct!!.toInt()
    }

    private fun putLocationUpdates(
        count: Int,
        latitude: Double,
        longitude: Double,
        speed: Float,
        bearing: Float
    ) {

        var wayLatitude: Double = latitude
        var wayLongitude: Double = longitude

        val hourlySpeed = speed * 3.6

        if (deviceNumber >= 0 && deviceNumber < 100) {
            wayLatitude -= 0.0123444
            wayLongitude += 0.0736568
        }

        if (count != 0) { // コールバック
            deviceNumber += 1
            if (deviceNumber >= 80 && deviceNumber < 100) {
//                wayLatitude -= 0.0123444
//                wayLongitude += 0.0736568
            }
        }
        Log.d("debug", "deviceNumber = $deviceNumber")

        val date = Date()
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val localTime: String = format.format(date).toString()

        Log.d("debug", localTime)

        val finalDeviceNumber = deviceNumber

        if (heartRateCount != 0) {
            val heardRateAverage = heartRateSum / heartRateCount
            heartRate = heardRateAverage.toString()
        } else {
            heartRate = "0"
        }
        println("COUNT: ${heartRateCount}")
        println("HEART: ${heartRate}")
        heartRateSum = 0
        heartRateCount = 0

        // 非同期処理
        val rate = ""
        "http://133.17.165.177:8086/ocs_insert0.php?bearing=${bearing.toInt()}&timeGap=${timeGap.toInt()}&speed=${hourlySpeed.toInt()}&calculatedSpeed=${calculatedSpeed.toInt()}&distance=${distance.toInt()}&localTime=${localTime}&t_num=${finalDeviceNumber}&lat=${wayLatitude}&lon=${wayLongitude}&user_acceleration_x=${sensorX}&user_acceleration_y=${sensorY}&user_acceleration_z=${sensorZ}&rate=$heartRate&battery=${getBattery()}".httpGet()
            .response { _, _, result ->
                when (result) {
                    is Result.Success -> {

                        sensorX = 0.toFloat()
                        sensorY = 0.toFloat()
                        sensorZ = 0.toFloat()

                        if (previousLatitude != 0.0) {

                            val currentTimeMillis = System.currentTimeMillis()

                            distance = HubenyDistance.calcDistance(
                                previousLatitude,
                                previousLongitude,
                                wayLatitude,
                                wayLongitude
                            )

                            val latitudeGapAbs = Math.abs(wayLatitude - previousLatitude)
                            val longitudeGapAbs = Math.abs(wayLongitude - previousLongitude)

                            if (previousTimeMillis.toInt() == 0) {

                                gpsGapTextView.setText("Gap:(${latitudeGapAbs.toFloat()}, ${longitudeGapAbs.toFloat()})(${distance.toInt()}m)  ")

                            } else {

                                val timeMillsGap = currentTimeMillis - previousTimeMillis
                                timeGap = timeMillsGap / 1000
                                calculatedSpeed = (distance / timeGap) * 3.6
                                gpsGapTextView.setText("Gap:(${latitudeGapAbs.toFloat()}, ${longitudeGapAbs.toFloat()})(${hourlySpeed.toInt()}km/h, ${calculatedSpeed.toInt()}km/h, ${distance.toInt()}m, ${timeGap.toInt()}sec, ${bearing.toInt()})  ")

                            }

                            previousTimeMillis = currentTimeMillis

                        }

                        previousLatitude = wayLatitude
                        previousLongitude = wayLongitude

                        informationTextView.setText("(${finalDeviceNumber}), ${localTime}, (${wayLatitude.toFloat()}, ${wayLongitude.toFloat()})  ")

                        successCount += 1
                        communicationTextView.setText("Success:(${successCount}), Failure:(${failureCount})  ")

                        Log.d("debug", "Success")
                        // レスポンスボディを表示
                        //             println("===========非同期処理の結果==========" + String(response.data))
                    }
                    is Result.Failure -> {
                        failureCount += 1
                        communicationTextView.setText("Success:(${successCount}), Failure:(${failureCount})  ")
                        Log.d("debug", "Failure")
                        //             println("===========通信に失敗しました。===========")
                    }
                }
            }

        if (count != 0) {
            deviceNumber -= 1
        }

    }
}
