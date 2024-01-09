package jp.ac.kyusanu.ohtomi

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.ac.kyusanu.ohtomi.databinding.ListItemTemperatureBinding
import kotlinx.android.parcel.Parcelize
import kotlin.math.roundToInt

@Parcelize
class Co2List(
    var location: String,
    var co2: String,
    var temperature: String,
    var humidity: String,
    var pressure: String,
    var build: String,
    var systemVersion: String,
    var modified: String
) : Parcelable


class TemperatureAdapter(private val context: Context, private val arrayList: ArrayList<Co2List>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val binding = if (convertView == null) {
            val binding = ListItemTemperatureBinding.inflate(layoutInflater)
            binding.root.tag = binding
            binding
        } else {
            convertView.tag as ListItemTemperatureBinding
        }


        binding.locationTextView.text = arrayList[position].location
        binding.temperatureTextView.text = context.getString(R.string.temperature_data, arrayList[position].temperature)
        binding.humidityTextView.text = context.getString(R.string.humidity_data, arrayList[position].humidity)
        binding.pressureTextView.text = context.getString(R.string.pressure_data, (arrayList[position].pressure.toDouble()
            .roundToInt()).toString())

        if (arrayList[position].temperature.toDouble() <= 0) {
            binding.temperatureImageView.setImageResource(R.drawable.blue_temperature)
        } else if (arrayList[position].temperature.toDouble() >= 10){
            binding.temperatureImageView.setImageResource(R.drawable.orange_temperature)
        } else {
            binding.temperatureImageView.setImageResource(R.drawable.green_temperature)
        }

        binding.timeTextView.text = arrayList[position].modified
        binding.systemVersionTextView.text = context.getString(R.string.system_version, arrayList[position].systemVersion, arrayList[position].build)

        return binding.root
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return arrayList.size
    }
}