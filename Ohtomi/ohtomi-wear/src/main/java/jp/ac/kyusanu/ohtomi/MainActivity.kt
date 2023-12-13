package jp.ac.kyusanu.ohtomi

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.Node
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

class MainActivity : WearableActivity(),
    SensorEventListener, View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = "MainActivity"
    var record = false
//    var datapath = "message_path"
    var datapath = "verify_remote_example_phone_app"

    private val Type_AC = 0
    private val TYPE_HR = 1

    var nodeSet: MutableSet<Node>? = null

    private var sensorManager: SensorManager? = null
    private var accelometer: Sensor? = null

    private var data: String? = null

    private val ac_responses = mutableMapOf<Date, FloatArray>()
    private val hr_responses = mutableMapOf<Date, FloatArray>()

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.i(TAG,"Starting")
        // Keep the Wear screen always on (for testing only!)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG,"Requesting Permission")
            requestPermissions(
                Array<String>(1) { Manifest.permission.BODY_SENSORS },
                152)
        }
        else{
            Log.i(TAG,"ALREADY GRANTED")
            registerSensorListeners()
            var button = findViewById<Button>(R.id.button)
            button.setOnClickListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Listenerを解除
        sensorManager?.unregisterListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            152 -> {
                Log.i(TAG,"Feedback for Permission")
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG,"Granted Permission")

                    registerSensorListeners()
                    var button = findViewById<Button>(R.id.button)
                    button.setOnClickListener(this)
                } else {
                    Toast.makeText(this, "許可してないですよ", Toast.LENGTH_SHORT).show()
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.button) {
            var button = findViewById<Button>(R.id.button)
            record = !record
            button.text = "Finish"
            if(!record) {
                button.text = "Start"
                registerSensorListeners()
//                SendThread(datapath, arrayOf(ac_responses, hr_responses)).start()
            }
        }
    }

    private fun registerSensorListeners() {

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE) ?: return

        accelometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        sensorManager!!.registerListener(
            this, accelometer,
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        )
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.i(TAG, "onAccuracyChanged - accuracy: $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val date = Date()
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        println(format.format(date))

        if (!record) {
            return
        } else {
            // SENSOR_STATUS_ACCURACY_HIGH 最大限の精度でデータを報告
            // && event.accuracy == SENSOR_STATUS_ACCURACY_HIGH && event.values[0] > 0
            if (event!!.sensor.type == Sensor.TYPE_HEART_RATE) {

                data = event.values!!.contentToString()

                val heartRate = event.values[0].toString()

                Log.i("心拍数", heartRate)
                data?.let { SendThread(datapath, heartRate.toByteArray()).start() }

            } else {
                Log.i("心拍数", "0")
//                data?.let { SendThread(datapath, "200".toByteArray()).start() }
            }
        }
    }

    internal inner class SendThread//constructor
        (var path: String, var message: ByteArray) : Thread() {

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        override fun run() {

            //first get all the nodes, ie connected wearable devices.
            val nodeListTask = Wearable.getNodeClient(applicationContext).connectedNodes
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                val nodes = Tasks.await<List<Node>>(nodeListTask)

                //Now send the message to each device.
                for (node in nodes) {
                    val sendMessageTask = Wearable.getMessageClient(this@MainActivity)
                        .sendMessage(node.id, path, message)

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        val result = Tasks.await(sendMessageTask)
                        Log.v("blub", "SendThread: message send to " + node.displayName)

                    } catch (exception: ExecutionException) {
                        Log.e("blub", "Task failed: $exception")

                    } catch (exception: InterruptedException) {
                        Log.e("blub", "Interrupt occurred: $exception")
                    }
                }

            } catch (exception: ExecutionException) {
                Log.e("blub", "Task failed: $exception")

            } catch (exception: InterruptedException) {
                Log.e("blub", "Interrupt occurred: $exception")
            }

        }
    }

//    internal inner class SendThread//constructor
//        (var path: String, var message: Array<MutableMap<Date, FloatArray>>) : Thread() {
//
//        fun toByteArray(message: Array<MutableMap<Date, FloatArray>>):ByteArray {
//            var bos:ByteArrayOutputStream = ByteArrayOutputStream()
//            var out:ObjectOutput
//            try {
//                out = ObjectOutputStream(bos)
//                out.writeObject(message)
//                out.flush()
//                return bos.toByteArray()
//            } finally {
//                try {
//                    bos.close()
//                } catch (ex: IOException) {
//                    // ignore close exception
//                }
//            }
//        }
//
//        //sends the message via the thread.  this will send to all wearables connected, but
//        //since there is (should only?) be one, so no problem.
//        override fun run() {
//            //first get all the nodes, ie connected wearable devices.
//            // 送信可能な接続済みノードをすべて取得
//            val nodeListTask = Wearable.getNodeClient(applicationContext).connectedNodes
//
//            try {
//                // Block on a task and get the result synchronously (because this is on a background
//                // thread).
//                val nodes = Tasks.await<List<Node>>(nodeListTask)
//
//                //Now send the message to each device.
//                for (node in nodes) {
//                    val sendMessageTask = Wearable.getMessageClient(this@MainActivity)
//                        .sendMessage(node.id, path, toByteArray(message))
//
//                    try {
//                        // Block on a task and get the result synchronously (because this is on a background
//                        // thread).
//                        val result = Tasks.await(sendMessageTask)
//                        Log.v("blub", "SendThread: message send to " + node.displayName)
//
//                    } catch (exception: ExecutionException) {
//                        Log.e("blub", "Task failed: $exception")
//
//                    } catch (exception: InterruptedException) {
//                        Log.e("blub", "Interrupt occurred: $exception")
//                    }
//
//                }
//
//            } catch (exception: ExecutionException) {
//                Log.e("blub", "Task failed: $exception")
//
//            } catch (exception: InterruptedException) {
//                Log.e("blub", "Interrupt occurred: $exception")
//            }
//
//        }
//    }
}
