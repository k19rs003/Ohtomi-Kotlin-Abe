package jp.ac.kyusanu.ohtomi

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import jp.ac.kyusanu.ohtomi.databinding.ActivityTemperatureBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException

class TemperatureActivity : AppCompatActivity() {

    private val format = Json {
        isLenient = true
        ignoreUnknownKeys = true
        useArrayPolymorphism = true
        allowSpecialFloatingPointValues = true
    }

    private var _binding: ActivityTemperatureBinding? = null
    private val binding get() = _binding!!

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
//        @SerialName("deviceId") val deviceId: String,
//        @SerialName("ssid") val ssid: String,
        val location: String,
//        @SerialName("flag") val flag: String,
        val modified: String
        //        val created: String
    )

    private lateinit var json: List<Container>

    private val uri = "http://ksu.apps.kyusan-u.ac.jp:8086/co2/dbread.php"
    private val arrayList: ArrayList<Co2List> = ArrayList()
    var resultList: ArrayList<Co2List> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTemperatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
        setupOnRefreshListeners()
        getContents()
        setupSearchView()
    }

    private  fun setup() {
        supportActionBar?.title = "温度モニター"
    }

    private fun setupOnRefreshListeners() {

        binding.swipeRefreshLayout.setOnRefreshListener {
            getContents()
            // Search Viewの行をクリア
//            binding.searchView.setQuery("", true)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            Log.d("aaaa","aaaa")
            intent = Intent(this, TemperatureDetailActivity::class.java)
            intent.putParcelableArrayListExtra("RESULT_LIST", resultList)
            intent.putExtra("POSITION", position)
            Log.d("[get] co2 RESULT: A", resultList[0].location)
            startActivity(intent)
        }
    }

    private fun getContents() {

        binding.progressBar.visibility = View.VISIBLE

        // DeviceIdをPOSTで送信
        val sharedPreferences = getSharedPreferences("ohtomi", Context.MODE_PRIVATE)
        val storedDeviceToken = sharedPreferences?.getString("deviceToken", "0")

        val handler = Handler(Looper.getMainLooper())

        val body: FormBody = FormBody.Builder()
            .add("deviceId", storedDeviceToken.toString())
            .build()
        val request = Request.Builder().url(uri).post(body).build()

        // 非同期通信（enqueue），同期の時は，execute
        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                val responseBody = response.body.string()

                // 戻り値をuserIdとする．
                getSharedPreferences("ohtomi", Context.MODE_PRIVATE).edit().apply {
                    putString("co2Contents", responseBody)
                    apply()
                }
                Log.d("[set] co2Contents: ", responseBody)

                // JSONデコード
                json = format.decodeFromString(
                    ListSerializer(Container.serializer()),
                    responseBody
                )

                if (arrayList.isNotEmpty()) {
                    arrayList.clear()
                }

                for (i in json.indices) {

                    arrayList.add(
                        Co2List(
                            json[i].location,
                            json[i].co2,
                            json[i].temperature,
                            json[i].humidity,
                            json[i].pressure,
                            json[i].build,
                            json[i].systemVersion,
                            json[i].modified
                        )
                    )
                }

                resultList = arrayList


                // listView表示
                handler.post(kotlinx.coroutines.Runnable {
                    binding.listView.adapter =
                        TemperatureAdapter(this@TemperatureActivity, arrayList)
                    Toast.makeText(this@TemperatureActivity, "更新しました", Toast.LENGTH_SHORT).show()
                })

                binding.progressBar.visibility = View.INVISIBLE
//                    binding.searchView.setQuery("", true) // これがあるとシミュレータでおちる？　（23/4/16）
                binding.swipeRefreshLayout.isRefreshing = false

                // Try-catchを外してみた．おかしい気がする．Debugだけ？
//                try {
//                } catch (e: Exception) {
//                    Log.e("catch()", e.toString())
//
//                    binding.progressBar.visibility = View.INVISIBLE
//                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("onFailure()", e.toString())

                binding.progressBar.visibility = View.INVISIBLE
                binding.searchView.setQuery("", true)
                binding.swipeRefreshLayout.isRefreshing = false

            }
        })

    }

    private fun setupSearchView() {

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                val regex = Regex(newText.toString())
                resultList.clear()

                // 検索時は，キャッシュした値を使用
                val sharedPreferences = getSharedPreferences("ohtomi", Context.MODE_PRIVATE)
                val contents = sharedPreferences?.getString("co2Contents", "[]")
                Log.d("[get] co2Contents: ", contents!!)

                // JSONデコード
                val json = contents.let {
                    format.decodeFromString(
                        ListSerializer(Container.serializer()),
                        it
                    )
                }

                for (i in json.indices) {
                    if (regex.containsMatchIn(json[i].location)) {

                        resultList.add(
                            Co2List(
                                json[i].location,
                                json[i].co2,
                                json[i].temperature,
                                json[i].humidity,
                                json[i].pressure,
                                json[i].build,
                                json[i].systemVersion,
                                json[i].modified
                            )
                        )
                    }
                }

                binding.listView.adapter =
                    TemperatureAdapter(this@TemperatureActivity, resultList)
                return true
            }
        })
    }

}