package jp.ac.kyusanu.ohtomi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.ac.kyusanu.ohtomi.databinding.ActivityCo2Binding
import jp.ac.kyusanu.ohtomi.databinding.ActivityMainBinding

class Co2Activity : AppCompatActivity() {

    private var _binding: ActivityCo2Binding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCo2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private  fun setup() {
        supportActionBar?.title = "温度モニター"
    }

}