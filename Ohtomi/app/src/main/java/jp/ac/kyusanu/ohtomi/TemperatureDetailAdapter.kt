package jp.ac.kyusanu.ohtomi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import jp.ac.kyusanu.ohtomi.databinding.ListItemTemperatureDetailBinding
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Parcelize
class TemperatureList(
    var location: String,
    var co2: String,
    var temperature: String,
    var humidity: String,
    var pressure: String,
    var build: String,
    var systemVersion: String,
    var created: String
) : Parcelable


class TemperatureDetailAdapter(private val context: Context, private val arrayList: ArrayList<TemperatureList>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var chartList = ArrayList<ChartData>()
    private val dateTime = DateTimeCtrl()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val binding = if (convertView == null) {
            val binding = ListItemTemperatureDetailBinding.inflate(layoutInflater)
            binding.root.tag = binding
            binding
        } else {
            convertView.tag as ListItemTemperatureDetailBinding
        }

        setupLineChart(binding.lineChart)
        setDataToLineChart(binding.lineChart)

        return binding.root
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return 3
    }

    private fun setupLineChart(lineChart: LineChart) {

        lineChart.setBackgroundColor(Color.BLACK)
//        hide grid lines
        lineChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = lineChart.xAxis
        val yAxis: YAxis = lineChart.axisLeft
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lineChart.axisRight.isEnabled = false
        //remove legend
        lineChart.legend.isEnabled = false
        //remove description label
        lineChart.description.isEnabled = false
        //add animation
        lineChart.animateX(2000, Easing.EaseInSine)

        // to draw label on xAxis
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
//        xAxis.position = XAxis.XAxisPosition.TOP
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
//        xAxis.labelRotationAngle = +90f
        xAxis.labelRotationAngle = +0f
        xAxis.textSize = 15f
        xAxis.textColor = Color.WHITE
        yAxis.textSize = 15f
        yAxis.textColor = Color.WHITE
    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()

            return if (index < chartList.size) {

                val parsedDateTime = dateTime.parse(arrayList[index].created, "yyyy-MM-dd HH:mm:ss")
                val timeString = dateTime.format(parsedDateTime, "HH:mm")
                Log.d("[get] co2 created: ", timeString)
                Log.d("[get] co2 created index: ", index.toString())
                timeString

            } else {

                Log.d("[get] co2 created index: ", index.toString())
                ""
            }
        }
    }

    private fun setDataToLineChart(lineChart: LineChart) {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        chartList = getScoreList()

        //you can replace this data object with  your custom object
        for (i in chartList.indices) {
            val score = chartList[i]
            entries.add(Entry(i.toFloat(), score.score.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")
//        lineDataSet.color = Color.RED
        lineDataSet.valueTextSize = 12f
        lineDataSet.valueTextColor = Color.WHITE
        lineDataSet.lineWidth = 10f
//        lineDataSet.formLineWidth = 30f
        lineDataSet.highlightLineWidth = 10f

        val data = LineData(lineDataSet)
        lineChart.data = data

        lineChart.invalidate()
    }

    // simulate api call
    // we are initialising it directly
    private fun getScoreList(): ArrayList<ChartData> {

        for (i in arrayList.indices) {
            chartList.add(ChartData("", arrayList[i].co2.toInt()))
        }

        return chartList
    }

}

data class ChartData(
    val date: String,
    val score: Int
)

class DateTimeCtrl {

    @SuppressLint("NewApi")
    fun format(date: LocalDateTime, pattern: String): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
        return date.format(formatter)
    }

    @SuppressLint("NewApi")
    fun parse(str: String, pattern: String): LocalDateTime {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(str, formatter)
    }
}