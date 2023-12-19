package jp.ac.kyusanu.ohtomi

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.ac.kyusanu.ohtomi.databinding.ListItemCo2Binding
import kotlinx.android.parcel.Parcelize

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


class Co2Adapter(private val context: Context, private val arrayList: ArrayList<Co2List>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val binding = if (convertView == null) {
            val binding = ListItemCo2Binding.inflate(layoutInflater)
            binding.root.tag = binding
            binding
        } else {
            convertView.tag as ListItemCo2Binding
        }

        val co2Int: Int = (arrayList[position].co2).toInt()
//        when {
//            co2Int >= 1536 -> binding.alertImageView.setImageResource(R.drawable.co2_alert4)
//            co2Int >= 1024 -> binding.alertImageView.setImageResource(R.drawable.co2_alert3)
//            co2Int >= 768 -> binding.alertImageView.setImageResource(R.drawable.co2_alert2)
//            else -> binding.alertImageView.setImageResource(R.drawable.co2_alert1)
//        }

        binding.locationTextView.text = arrayList[position].location
//        binding.co2TitleTextView.text = context.getString(R.string.co2_title)
//        binding.co2DataTextView.text = context.getString(R.string.co2_data, arrayList[position].co2)
//        binding.temperatureTitleTextView.text = context.getString(R.string.temperature_title)
//        binding.temperatureDataTextView.text = context.getString(R.string.temperature_data, arrayList[position].temperature)
//        binding.humidityTitleTextView.text = context.getString(R.string.humidity_title)
//        binding.humidityDataTextView.text = context.getString(R.string.humidity_data, arrayList[position].humidity)
//        binding.pressureTitleTextView.text = context.getString(R.string.pressure_title)
//        binding.pressureDataTextView.text = context.getString(R.string.pressure_data, arrayList[position].pressure)
//        binding.dateTextView.text = arrayList[position].modified
//        binding.systemVersionTextView.text = context.getString(R.string.system_version, arrayList[position].systemVersion, arrayList[position].build)

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