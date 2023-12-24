package jp.ac.kyusanu.ohtomi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import jp.ac.kyusanu.ohtomi.databinding.ActivityTemperatureDetailBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder

class TemperatureDetailActivity : AppCompatActivity() {

    private val format = Json {
        isLenient = true
        ignoreUnknownKeys = true
        useArrayPolymorphism = true
        allowSpecialFloatingPointValues = true
    }

    private var _binding: ActivityTemperatureDetailBinding? = null
    private val binding get() = _binding!!
    private var position = 0
    private val detailArrayList: ArrayList<Co2List> = ArrayList()
    private lateinit var resultList: ArrayList<Co2List>


    // JSON使うときの宣言
    @Serializable
    data class Container(
//        @SerialName("id") val id: String,
        val co2: String,
        val temperature: String,
        val humidity: String,
        val pressure: String,
        val build: String,
        val systemVersion: String,
        val location: String,
        val created: String
    )

    private lateinit var json: List<Container>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTemperatureDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getExtras()
        setup()
        getContents()
    }

    private fun getExtras() {

        position = intent.getIntExtra("POSITION", 0)
        resultList =
                // ここの記述だと，Google Playの実機だけおかしくなる．
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // ここのif文がだめ？
//            intent.getParcelableArrayListExtra(
//                "RESULT_LIST",
//                Co2List::class.java
//            ) as ArrayList<Co2List>
//        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<Co2List>(
                "RESULT_LIST"
            ) as ArrayList<Co2List>
//        }
        Log.d("[get] co2 POSITION: ", position.toString())
        Log.d("[get] co2 RESULT: ", resultList[position].location)
    }

    private  fun setup() {
        supportActionBar?.title = resultList[position].location
    }

    private fun getContents() {

//        binding.progressBar.visibility = View.VISIBLE

        val period = 12
        val encodedLocation = URLEncoder.encode(resultList[position].location, "utf-8")
        val baseUrl = "http://ksu.apps.kyusan-u.ac.jp:8086/co2/dbreadlocation.php"

        val url = "$baseUrl?location=$encodedLocation&period=$period"


        val client = OkHttpClient()
        val body: FormBody = FormBody.Builder()
            .build()
        val request = Request.Builder().url(url).post(body).build()

        // 非同期通信（enqueue），同期の時は，execute
        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                try {
                    //　サーバから受信
//                    val responseBody = response.body?.string().orEmpty()
                    //　サーバから受信　?いらないって言われた．
                    val responseBody = response.body.string()
                    Log.d("[get] co2DetailContents: ", responseBody)

                    // JSONデコード
                    json = format.decodeFromString(
                        ListSerializer(Container.serializer()),
                        responseBody
                    )

                    if (detailArrayList.isNotEmpty()) {
                        detailArrayList.clear()
                    }

                    for (i in json.indices) {

                        detailArrayList.add(
                            Co2List(
                                json[i].location,
                                json[i].co2,
                                json[i].temperature,
                                json[i].humidity,
                                json[i].pressure,
                                json[i].build,
                                json[i].systemVersion,
                                json[i].created
                            )
                        )
                    }
                    handler.post(kotlinx.coroutines.Runnable {
                        binding.listView.adapter =
                            TemperatureDetailAdapter(this@TemperatureDetailActivity, detailArrayList)
                        Toast.makeText(this@TemperatureDetailActivity, "更新しました", Toast.LENGTH_SHORT).show()
                    })
//                    setDataToLineChart()

//                    binding.progressBar.visibility = View.INVISIBLE

                } catch (e: Exception) {
                    Log.e("catch()", e.toString())

//                    binding.progressBar.visibility = View.INVISIBLE
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("onFailure()", e.toString())

//                binding.progressBar.visibility = View.INVISIBLE
            }
        })
    }
}