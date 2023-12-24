package jp.ac.kyusanu.ohtomi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


class TemperatureDetailAdapter(private val context: Context, private val arrayList: ArrayList<Co2List>) : BaseAdapter() {

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


        setup(binding, position)
        setupLineChart(binding.lineChart)
        setDataToLineChart(binding.lineChart, position)

        return binding.root
    }

    override fun getItem(position: Int): Any {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return 3
    }

    private fun setup(binding: ListItemTemperatureDetailBinding ,position: Int) {
        when (position) {
            0 -> {
                binding.titleImageView.setImageResource(R.drawable.notemperature)
                binding.titleTextView.text = "気温"
            }
            1 -> {
                binding.titleImageView.setImageResource(R.drawable.humidity)
                binding.titleTextView.text = "湿度"
            }
            2 -> {
                binding.titleImageView.setImageResource(R.drawable.pressure)
                binding.titleTextView.text = "気圧"
            }

        }
    }

    private fun setupLineChart(lineChart: LineChart) {

        lineChart.setBackgroundColor(Color.WHITE)
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
        xAxis.textColor = Color.BLACK
        yAxis.textSize = 15f
        yAxis.textColor = Color.BLACK
    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {

//            return "aaaa"
            val index = value.toInt()

            return if (index < chartList.size) {

                val parsedDateTime = dateTime.parse(arrayList[index].modified, "yyyy-MM-dd HH:mm:ss")
                val timeString = dateTime.format(parsedDateTime, "HH:mm")
                Log.d("[get] co2 created: ", timeString)
                Log.d("[get] co2 created index: ", index.toString())
                timeString

            } else {
                Log.d("aaa!","bbbbb")
                Log.d("[get] co2 created index: ", index.toString())
                ""
            }
        }
    }

    private fun setDataToLineChart(lineChart: LineChart, position: Int) {
        Log.d("aaa!", "setDataToLineChart")
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        chartList = getScoreList(position)

        for (i in chartList.indices) {
            val score = chartList[i]
            entries.add(Entry(i.toFloat(), score.score.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")
//        lineDataSet.color = Color.RED
        lineDataSet.valueTextSize = 0f
//        lineDataSet.valueTextColor = Color.BLACK
        lineDataSet.lineWidth = 5f
//        lineDataSet.formLineWidth = 30f
        lineDataSet.highlightLineWidth = 0f

        val data = LineData(lineDataSet)
        lineChart.data = data

        lineChart.invalidate()
    }

    // simulate api call
    // we are initialising it directly
    private fun getScoreList(position: Int): ArrayList<ChartData> {

        chartList.clear()

        if (position == 0) {

            for (i in arrayList.indices) {
                chartList.add(ChartData("", arrayList[i].temperature.toDouble().roundToInt()))
            }

        } else if (position == 1) {

            for (i in arrayList.indices) {
                chartList.add(ChartData("", arrayList[i].humidity.toDouble().roundToInt()))
            }

        } else if (position == 2) {

            for (i in arrayList.indices) {
                chartList.add(ChartData("", arrayList[i].pressure.toDouble().roundToInt()))
            }

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