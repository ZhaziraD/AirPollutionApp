package com.example.airpollutionapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
import com.anychart.enums.Anchor
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.airpollutionapp.R
import com.example.airpollutionapp.models.PollutionResponse
import com.example.airpollutionapp.network.ForecastService
import com.example.airpollutionapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_charts.*
import retrofit.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChartsFragment : Fragment(R.layout.fragment_charts) {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapterItems = activity?.let { ArrayAdapter(it, R.layout.list_item, items) }!!
        autoCompleteTxt.setAdapter(adapterItems)
        autoCompleteTxtYear.setAdapter(adapterItems)

        history()

        super.onViewCreated(view, savedInstanceState)

        if(Constants.isNetworkAvailable(requireActivity())) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val forecastService: ForecastService = retrofit.create<ForecastService>(ForecastService::class.java)
            val forecastListCall: Call<PollutionResponse> = forecastService.getForecast(
                51.177601, 71.432999, Constants.APP_ID
            )

            forecastListCall.enqueue(object : Callback<PollutionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<PollutionResponse>,
                    retrofit: Retrofit
                ) {
                    if (response.isSuccess) {
                        pollutionList = response.body()

                        var aqi: Int?
                        var date: Date
                        for (i in 0..pollutionList.list!!.size - 1) { //pollutionList.list!!.size-1
                            aqi = pollutionList.list!![i].main!!.aqi
                            date = Date(pollutionList.list!![i].dt!!.toLong() * 1000)
                            series.add(ValueDataEntry(dateFormat.format(date), aqi))
                        }

                        var anyChartView: AnyChartView =
                            requireView().findViewById(R.id.any_chart_view)
                        APIlib.getInstance().setActiveAnyChartView(anyChartView)
                        var cartesian: Cartesian = AnyChart.column()
                        anyChartView.setProgressBar(requireView().findViewById(R.id.progress_bar))
                        var column: Column = cartesian.column(series)
                        column.tooltip()
                            .titleFormat("{%X}")
                            .position(Position.CENTER_BOTTOM)
                            .anchor(Anchor.CENTER_BOTTOM)
                            .offsetX(0.0)
                            .offsetY(5.0)
                            .format("index {%Value}{groupsSeparator: }")
                        cartesian.animation(true)
                        cartesian.title("Hourly forecast $componentName")
                        cartesian.yScale().minimum(0.0)
                        // scroller
                        cartesian.xScroller(true)
                        cartesian.xZoom().setTo(0, 0.3)
                        // bar colors
                        column.fill(filterByColorAQI)
                            .stroke(filterByColorAQI)
                        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                        anyChartView.setChart(cartesian)

                        autoCompleteTxt.setOnItemClickListener { parent, view, pos, l ->
                            val item = parent.getItemAtPosition(pos).toString()
                            if (item.equals("no2")) {
                                APIlib.getInstance().setActiveAnyChartView(anyChartView)
                                cartesian.title("Hourly forecast no2")
                                val data4: MutableList<DataEntry> = ArrayList()
                                var no2: Double?
                                var date: Date
                                for (i in 0..pollutionList.list!!.size - 1) { //pollutionList.list!!.size-1
                                    no2 = pollutionList.list!![i].components!!.no2
                                    date = Date(pollutionList.list!![i].dt!!.toLong() * 1000)
                                    data4.add(ValueDataEntry(dateFormat.format(date), no2))
                                }
                                cartesian.data(data4)
                                column.fill(filterByColorNO2)
                                    .stroke(filterByColorNO2)
                            } else if (item.equals("o3")) {
                                APIlib.getInstance().setActiveAnyChartView(anyChartView)
                                cartesian.title("Hourly forecast o3")
                                val data5: MutableList<DataEntry> = ArrayList()
                                var o3: Double?
                                var date: Date
                                for (i in 0..pollutionList.list!!.size - 1) { //pollutionList.list!!.size-1
                                    o3 = pollutionList.list!![i].components!!.o3
                                    date = Date(pollutionList.list!![i].dt!!.toLong() * 1000)
                                    data5.add(ValueDataEntry(dateFormat.format(date), o3))
                                }
                                cartesian.data(data5)
                                column.fill(filterByColorO3)
                                    .stroke(filterByColorO3)
                            } else if (item.equals("pm10")) {
                                APIlib.getInstance().setActiveAnyChartView(anyChartView)
                                cartesian.title("Hourly forecast pm10")
                                val data6: MutableList<DataEntry> = ArrayList()
                                var pm10: Double?
                                var date: Date
                                for (i in 0..pollutionList.list!!.size - 1) { //pollutionList.list!!.size-1
                                    pm10 = pollutionList.list!![i].components!!.pm10
                                    date = Date(pollutionList.list!![i].dt!!.toLong() * 1000)
                                    data6.add(ValueDataEntry(dateFormat.format(date), pm10))
                                }
                                cartesian.data(data6)
                                column.fill(filterByColorPM10)
                                    .stroke(filterByColorPM10)
                            } else if (item.equals("pm25")) {
                                APIlib.getInstance().setActiveAnyChartView(anyChartView)
                                cartesian.title("Hourly forecast pm25")
                                val data7: MutableList<DataEntry> = ArrayList()
                                var pm25: Double?
                                var date: Date
                                for (i in 0..pollutionList.list!!.size - 1) { //pollutionList.list!!.size-1
                                    pm25 = pollutionList.list!![i].components!!.pm2_5
                                    date = Date(pollutionList.list!![i].dt!!.toLong() * 1000)
                                    data7.add(ValueDataEntry(dateFormat.format(date), pm25))
                                }
                                cartesian.data(data7)
                                column.fill(filterByColorPM25)
                                    .stroke(filterByColorPM25)
                            } else if (item.equals("aqi")) {
                                APIlib.getInstance().setActiveAnyChartView(anyChartView)
                                cartesian.title("Hourly forecast aqi")
                                val data9: MutableList<DataEntry> = ArrayList()
                                var aqi: Int?
                                var date: Date
                                for (i in 0..pollutionList.list!!.size - 1) {
                                    aqi = pollutionList.list!![i].main!!.aqi
                                    date = Date(pollutionList.list!![i].dt!!.toLong() * 1000)
                                    data9.add(ValueDataEntry(dateFormat.format(date), aqi))
                                }
                                cartesian.data(data9)
                                column.fill(filterByColorAQI)
                                    .stroke(filterByColorAQI)
                            }
                        }
                    } else {
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable?) {
                    Log.e("Error (onFailure)", t!!.message.toString()) //
                }
            })
        }
    }

    private fun history() {
        if (!Python.isStarted()) Python.start(AndroidPlatform(requireActivity()))
        var py = Python.getInstance()
        var pyobj: PyObject? = py.getModule("forecastYear")
        var obj: MutableMap<PyObject, PyObject> = pyobj!!.callAttr("get_airPollutionYear").asMap()

        var lengthYear = obj.get("dt")!!.asList().size//.values//!!.get("dt")

        var dtYear: Int
        var aqiYear: Int
        for (i in 0..lengthYear-1) {
            dtYear = obj.get("dt")!!.asList()[i].toString().toInt()
            aqiYear = obj.get("aqi")!!.asList()[i].toString().toInt()
            seriesYear.add(ValueDataEntry(dtYear, aqiYear))
        }

        var anyChartViewYear: AnyChartView = requireView().findViewById(R.id.any_chart_view_year)//findViewById<AnyChartView>(R.id.any_chart_view)
        APIlib.getInstance().setActiveAnyChartView(anyChartViewYear)
        var cartesianYear: Cartesian = AnyChart.column()
        anyChartViewYear.setProgressBar(requireView().findViewById(R.id.progress_bar_year))


        var columnYear: Column = cartesianYear.column(seriesYear)

        columnYear.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0.0)
            .offsetY(5.0)
            .format("index {%Value}{groupsSeparator: }")

        cartesianYear.animation(true)
        cartesianYear.title("History statistics $componentName")
        cartesianYear.yScale().minimum(0.0)
        cartesianYear.xScroller(true)
        columnYear.fill(filterByColorAQI)
            .stroke(filterByColorAQI)
        cartesianYear.tooltip().positionMode(TooltipPositionMode.POINT)

        anyChartViewYear.setChart(cartesianYear)

        autoCompleteTxtYear.setOnItemClickListener { parent, view, pos, l ->
            val item = parent.getItemAtPosition(pos).toString()
            if (item.equals("no2")) {
                APIlib.getInstance().setActiveAnyChartView(anyChartViewYear)
                cartesianYear.title("History statistics no2")
                val data4Year: MutableList<DataEntry> = ArrayList()
                var dtYear: Int
                var no2Year: Double
                for (i in 0..lengthYear-1) {
                    dtYear = obj.get("dt")!!.asList()[i].toString().toInt()
                    no2Year = obj.get("no2")!!.asList()[i].toString().toDouble()
                    data4Year.add(ValueDataEntry(dtYear, no2Year))
                }
                cartesianYear.data(data4Year)
                columnYear.fill(filterByColorNO2)
                    .stroke(filterByColorNO2)
            } else if (item.equals("o3")) {
                APIlib.getInstance().setActiveAnyChartView(anyChartViewYear)
                cartesianYear.title("History statistics o3")
                val data5Year: MutableList<DataEntry> = ArrayList()
                var dtYear: Int
                var o3Year: Double
                for (i in 0..lengthYear-1) {
                    dtYear = obj.get("dt")!!.asList()[i].toString().toInt()
                    o3Year = obj.get("o3")!!.asList()[i].toString().toDouble()
                    data5Year.add(ValueDataEntry(dtYear, o3Year))
                }
                cartesianYear.data(data5Year)
                columnYear.fill(filterByColorO3)
                    .stroke(filterByColorO3)
            } else if (item.equals("pm10")) {
                APIlib.getInstance().setActiveAnyChartView(anyChartViewYear)
                cartesianYear.title("History statistics pm10")
                val data6Year: MutableList<DataEntry> = ArrayList()
                var dtYear: Int
                var pm10Year: Double
                for (i in 0..lengthYear-1) {
                    dtYear = obj.get("dt")!!.asList()[i].toString().toInt()
                    pm10Year = obj.get("pm10")!!.asList()[i].toString().toDouble()
                    data6Year.add(ValueDataEntry(dtYear, pm10Year))
                }
                cartesianYear.data(data6Year)
                columnYear.fill(filterByColorPM10)
                    .stroke(filterByColorPM10)
            } else if (item.equals("pm25")) {
                APIlib.getInstance().setActiveAnyChartView(anyChartViewYear)
                cartesianYear.title("History statistics pm25")
                val data7Year: MutableList<DataEntry> = ArrayList()
                var dtYear: Int
                var pm25Year: Double
                for (i in 0..lengthYear-1) {
                    dtYear = obj.get("dt")!!.asList()[i].toString().toInt()
                    pm25Year = obj.get("pm25")!!.asList()[i].toString().toDouble()
                    data7Year.add(ValueDataEntry(dtYear, pm25Year))
                }
                cartesianYear.data(data7Year)
                columnYear.fill(filterByColorPM25)
                    .stroke(filterByColorPM25)
            } else if (item.equals("aqi")) {
                APIlib.getInstance().setActiveAnyChartView(anyChartViewYear)
                cartesianYear.title("History statistics aqi")
                val data9Year: MutableList<DataEntry> = ArrayList()
                var dtYear: Int
                var aqiYear: Int
                for (i in 0..lengthYear-1) {
                    dtYear = obj.get("dt")!!.asList()[i].toString().toInt()
                    aqiYear = obj.get("aqi")!!.asList()[i].toString().toInt()
                    data9Year.add(ValueDataEntry(dtYear, aqiYear))
                }
                cartesianYear.data(data9Year)
                columnYear.fill(filterByColorAQI)
                    .stroke(filterByColorAQI)
            }
        }
    }

    companion object {
        private lateinit var pollutionList: PollutionResponse
        private var series: MutableList<DataEntry> = ArrayList()
        private var seriesYear: MutableList<DataEntry> =  ArrayList()
        private var dateFormat: DateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        private var componentName = "aqi"
        private val items = arrayOf("aqi", "no2", "o3", "pm10", "pm25")
        private lateinit var adapterItems: ArrayAdapter<String> //adapter
        
        private val filterByColorAQI = "function() {" +
                "            if (this.value == 1) {" +
                "                return '#388E3C';" +
                "            } else if (this.value == 2) {" +
                "                return '#4CB0F6';" +
                "            } else if (this.value == 3) {" +
                "                return '#FFB816';" +
                "            } else if (this.value == 4) {" +
                "                return '#E64A19';" +
                "            } else if (this.value == 5){" +
                "                return '#FF3209';" +
                "            } else {" +
                "            return 'blue'; }" +
                "        }"

        private val filterByColorPM25 = "function() {" +
                "            if (0.0 <= this.value && this.value <= 15.0) {" +
                "                return '#388E3C';" +
                "            } else if (15.0 < this.value && this.value <= 30.0) {" +
                "                return '#4CB0F6';" +
                "            } else if (30.0 < this.value && this.value <= 55.0) {" +
                "                return '#FFB816';" +
                "            } else if (55.0 < this.value && this.value <= 110.0) {" +
                "                return '#E64A19';" +
                "            } else if (110.0 < this.value){" +
                "                return '#FF3209';" +
                "            } else {" +
                "            return 'blue'; }" +
                "        }"


        private val filterByColorO3 = "function() {" +
                "            if (0.0 <= this.value && this.value <= 60.0) {" +
                "                return '#388E3C';" +
                "            } else if (this.value > 60 && this.value <= 120.0) {" +
                "                return '#4CB0F6';" +
                "            } else if (120.0 < this.value && this.value <= 180.0) {" +
                "                return '#FFB816';" +
                "            } else if (180.0 < this.value && this.value <= 240.0) {" +
                "                return '#E64A19';" +
                "            } else if (240.0 < this.value){" +
                "                return '#FF3209';" +
                "            } else {" +
                "            return 'blue'; }" +
                "        }"

        private val filterByColorNO2 = "function() {" +
                "            if (0.0 <= this.value && this.value <= 50.0) {" +
                "                return '#388E3C';" +
                "            } else if (50.0 < this.value && this.value <= 100.0) {" +
                "                return '#4CB0F6';" +
                "            } else if (100.0 < this.value && this.value <= 200.0) {" +
                "                return '#FFB816';" +
                "            } else if (200.0 < this.value && this.value <= 400.0) {" +
                "                return '#E64A19';" +
                "            } else if (400.0 < this.value){" +
                "                return '#FF3209';" +
                "            } else {" +
                "            return 'blue'; }" +
                "        }"

        private val filterByColorPM10 = "function() {" +
                "            if (0.0 <= this.value && this.value <= 25.0) {" +
                "                return '#388E3C';" +
                "            } else if (25.0 < this.value && this.value <= 50.0) {" +
                "                return '#4CB0F6';" +
                "            } else if (50.0 < this.value && this.value <= 90.0) {" +
                "                return '#FFB816';" +
                "            } else if (90.0 < this.value && this.value <= 100.0) {" +
                "                return '#E64A19';" +
                "            } else if (100.0 < this.value){" +
                "                return '#FF3209';" +
                "            } else {" +
                "            return 'blue'; }" +
                "        }"
    }
}
