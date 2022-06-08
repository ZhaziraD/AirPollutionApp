package com.example.airpollutionapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.airpollutionapp.R
import com.example.airpollutionapp.adapters.DailyForecastAdapter
import com.example.airpollutionapp.adapters.HourlyForecastAdapter
import com.example.airpollutionapp.models.*
import com.example.airpollutionapp.network.ForecastService
import com.example.airpollutionapp.network.PollutionService
import com.example.airpollutionapp.utils.Constants
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlinx.coroutines.*
import java.io.IOException


class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRvDailyForecast = requireView().findViewById(R.id.rvDailyForecast)
        mRvHourlyForecast = requireView().findViewById(R.id.rvHourlyForecast)

        mFusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!

        if (!isLocationEnabled()) {
            Toast.makeText(
                    activity,
                    "Your location provider is turned OFF. Please turn it on",
                    Toast.LENGTH_SHORT
            ).show()

            //redirect to the settings from where you need to turn on the location provider
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(activity)
                    .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                requestLocationData()
                            }

                            if (report.isAnyPermissionPermanentlyDenied) {
                                Toast.makeText(
                                        activity,
                                        "You have denied location permission, Please allow it is mandatory",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                        ) {
                            GlobalScope.launch {
                                showRationalDialogForPermissions()
                            }
                            Thread.sleep(500L)

                        }
                    }).onSameThread()
                    .check()
        }
        super.onViewCreated(view, savedInstanceState)
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private suspend fun showRationalDialogForPermissions() {
        delay(250L)
        activity?.let {
            AlertDialog.Builder(it)
                    .setMessage("It looks like you have turned off permissions required for this feature. It can be enabled under Application Setting")
                    .setPositiveButton(
                            "GO TO SETTINGS"
                    ) { _, _->
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireActivity().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                    .setNegativeButton("Cancel") { dialog,
                                                   _ ->
                        dialog.dismiss()
                    }.show()
        }
    }

    private fun isLocationEnabled() : Boolean {
        //provides access to the system location services
        val locationManager : LocationManager = activity?.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.
        isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (activity?.let {
                    ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED
                && activity?.let {
                    ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
                != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallBack,
                Looper.myLooper()
        )
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation: Location = locationResult!!.lastLocation
            val latitude = mLastLocation.latitude
            val longitude = mLastLocation.longitude
            getLocationPollutionDetails(latitude, longitude)
            dailyForecast(latitude, longitude)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun getLocationPollutionDetails(
            latitude: Double,
            longitude: Double
    ) {
        if(activity?.let { Constants.isNetworkAvailable(it) } == true) {
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val service: PollutionService = retrofit.create(PollutionService::class.java)
            val forecastService: ForecastService = retrofit.create(ForecastService::class.java)


            val listCall: Call<PollutionResponse> = service.getPollution(
                    latitude, longitude, Constants.APP_ID
            )

            val forecastListCallHourly: Call<PollutionResponse> = forecastService.getForecast(
                    latitude, longitude, Constants.APP_ID
            )
            
            listCall.enqueue(object : Callback<PollutionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                        response: Response<PollutionResponse>,
                        retrofit: Retrofit
                ) {
                    if (response.isSuccess) {

                        val pollutionList: PollutionResponse = response.body()

                        var lat: Double = pollutionList.coord!!.lat!!
                        var lon: Double = pollutionList.coord!!.lon!!

                        var no2 = pollutionList.list!![0].components!!.no2
                        var o3 = pollutionList.list[0].components!!.o3
                        var pm10 = pollutionList.list[0].components!!.pm10
                        var pm25 = pollutionList.list!![0].components!!.pm2_5

                        var aqi = pollutionList.list[0].main!!.aqi

                        var dateFormat: DateFormat =
                                SimpleDateFormat("HH:mm", Locale.ENGLISH) //dd-MMMM-yyyy

                        var date = Date(pollutionList.list!![0].dt!!.toLong() * 1000)


                        var cityName = ""
                        var districtName = ""
                        GlobalScope.launch {
                            cityName = CoordToName(lat, lon)[0]
                            districtName = CoordToName(lat, lon)[1]
                        }
                        Thread.sleep(500L)

                        tvCity.text = cityName
                        tvDistrict.text = districtName

                        gd = GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                intArrayOf(resources.getColor(R.color.white), aqicolor(aqi!!))
                        )

                        mainLayout.setBackgroundDrawable(gd)

                        tvNO2.setTextColor(no2color(no2!!))
                        tvO3.setTextColor(o3color(o3!!))
                        tvPM10.setTextColor(pm10color(pm10!!))
                        tvPM25.setTextColor(pm25color(pm25!!))

                        tvCurrentAqi.text = aqi.toString()
                        tvCurrentAqi.setTextColor(aqicolor(aqi))
                        tvEstimation.text = qualitativeName(aqi!!).toString()
                        tvTime.text = dateFormat.format(date)

                        tvNO2.text = "$no2\nPPB"
                        tvO3.text = "$o3\nPPB"
                        tvPM10.text = "$pm10\nμg/m3"
                        tvPM25.text = "$pm25\nμg/m3"

                        // change progress bar color by aqi
                        var unwrappedDrawable = progressBar.progressDrawable
                        progressBar.progress = aqi
                        var wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
                        DrawableCompat.setTint(wrappedDrawable, aqicolor(aqi!!))

                        // change cardview color by aqi
                        val gdCard = GradientDrawable(
                                GradientDrawable.Orientation.RIGHT_LEFT,
                                intArrayOf(gradient2(aqi!!), aqicolor(aqi!!))
                        )

                        gdCard.setCornerRadius(20f)

                        bottomSheetContainer.setBackgroundDrawable(gdCard)




                    } else {
                        ErrorLog(response)
                    }
                }


                override fun onFailure(t: Throwable?) {
                    Log.e("Error (onFailure)", t!!.message.toString()) //
                }
            })

            forecastListCallHourly.enqueue(object : Callback<PollutionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                        response: Response<PollutionResponse>,
                        retrofit: Retrofit
                ) {
                    if (response.isSuccess) {
                        val pollutionList: PollutionResponse = response.body()

                        var pollutionListForecast: ArrayList<PollutionResponse> = ArrayList()

                        var lat: Double
                        var lon: Double
                        var no2: Double?
                        var o3: Double?
                        var pm10: Double?
                        var pm25: Double?
                        var so2: Double?
                        var aqi: Int?
                        var dt: Int?

                        for (i in 0..23) { //pollutionList.list.size - 1
                            lat = pollutionList.coord!!.lat!!
                            lon = pollutionList.coord!!.lon!!
                            no2 = pollutionList.list!![i].components!!.no2
                            o3 = pollutionList.list[i].components!!.o3
                            pm10 = pollutionList.list[i].components!!.pm10
                            pm25 = pollutionList.list[i].components!!.pm2_5
                            so2 = pollutionList.list[i].components!!.so2
                            aqi = pollutionList.list[i].main!!.aqi
                            dt = pollutionList.list[i].dt

                            pollutionListForecast.add(
                                    PollutionResponse(
                                            Coord(lat, lon),
                                            listOf(com.example.airpollutionapp.models.List(Components(no2, o3, pm10, pm25, so2), dt, Main(aqi))))
                            )
                        }


                        var hourlyForecastAdapter = activity?.let {
                            HourlyForecastAdapter(
                                    pollutionListForecast,
                                    it
                            )
                        }
                        mRvHourlyForecast.layoutManager = GridLayoutManager(
                                activity,
                                2,
                                GridLayoutManager.HORIZONTAL,
                                false
                        )
                        mRvHourlyForecast.adapter = hourlyForecastAdapter
                    } else {
                        ErrorLog(response)
                    }
                }


                override fun onFailure(t: Throwable?) {
                    Log.e("Error (onFailure)", t!!.message.toString()) //
                }
            })
        } else {
            Toast.makeText(
                    activity,
                    "No internet connection available",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun dailyForecast(latitude: Double, longitude: Double) {
        if (!Python.isStarted()) Python.start(AndroidPlatform(requireActivity()))
        var py = Python.getInstance()
        var pyobj: PyObject? = py.getModule("forecastYear")
        var obj: MutableMap<PyObject, PyObject> = pyobj!!.callAttr("get_airPollutionDay", latitude, longitude).asMap()
        var lengthDay = obj.get("dt")!!.asList().size

        var pollutionListForecast: ArrayList<DailyForecast> = ArrayList()


        var dtt: String
        var aqii: Int
        for (i in 0..lengthDay-1) {
            dtt = obj.get("dt")!!.asList()[i].toString()
            aqii = obj.get("aqi")!!.asList()[i].toString().toInt()
            pollutionListForecast.add(DailyForecast(dtt, aqii))
        }


        var dailyForecastAdapter = activity?.let {
            DailyForecastAdapter(
                    pollutionListForecast,
                    it
            )
        }
        mRvDailyForecast.layoutManager = LinearLayoutManager(activity)
        mRvDailyForecast.adapter = dailyForecastAdapter
    }


    private fun ErrorLog(response: Response<PollutionResponse>) {
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

    private suspend fun CoordToName(latitude: Double, longitude: Double): Array<String> {
        delay(250L)
        var arr: Array<String> = arrayOf()
        try {
            val addresses: List<Address>
            val geocoder = Geocoder(activity, Locale.getDefault())

            addresses = geocoder.getFromLocation(latitude, longitude, 1) //or increase to 10 if errors keep appearing// Here 1 represent max location result to returned, by documents it recommended 1 to 5
            var city = addresses[0].locality
            var subLocality = addresses[0].subLocality

            if (subLocality == null) {
                subLocality = "Undefined"
            } else if(city == null){
                city = "UNdefined"
            } else if(subLocality == null && city == null){
                city = "UNdefined"
                subLocality = "UNdefined"
            }
            arr = arrayOf(city, subLocality)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return arr
    }

    private fun qualitativeName(aqi: Int): String {
        return when(aqi) {
            1 -> "Good"
            2 -> "Fair"
            3 -> "Moderate"
            4 -> "Poor"
            5 -> "Very poor"
            else -> "Not found"
        }
    }

    private fun no2color(no2: Double): Int {
        return when(no2) {
            in 0.0..50.0 -> resources.getColor(R.color.aqi1_1)
            in 51.0..100.0 -> resources.getColor(R.color.aqi2)
            in 101.0..200.0 -> resources.getColor(R.color.aqi3)
            in 201.0..400.0 -> resources.getColor(R.color.aqi4)
            else -> resources.getColor(R.color.aqi5)
        }
    }

    private fun pm10color(pm10: Double): Int {
        return when(pm10) {
            in 0.0..25.0 -> resources.getColor(R.color.aqi1_1)
            in 26.0..50.0 -> resources.getColor(R.color.aqi2)
            in 51.0..90.0 -> resources.getColor(R.color.aqi3)
            in 91.0..180.0 -> resources.getColor(R.color.aqi4)
            else -> resources.getColor(R.color.aqi5)
        }
    }

    private fun o3color(o3: Double): Int {
        return when(o3) {
            in 0.0..60.0 -> resources.getColor(R.color.aqi1_1)
            in 61.0..120.0 -> resources.getColor(R.color.aqi2)
            in 121.0..180.0 -> resources.getColor(R.color.aqi3)
            in 181.0..240.0 -> resources.getColor(R.color.aqi4)
            else -> resources.getColor(R.color.aqi5)
        }
    }

    private fun pm25color(pm25: Double): Int {
        return when(pm25) {
            in 0.0..15.0 -> resources.getColor(R.color.aqi1_1)
            in 16.0..30.0 -> resources.getColor(R.color.aqi2)
            in 31.0..55.0 -> resources.getColor(R.color.aqi3)
            in 56.0..110.0 -> resources.getColor(R.color.aqi4)
            else -> resources.getColor(R.color.aqi5)
        }
    }

    private fun aqicolor(aqi: Int): Int {
        return when(aqi) {
            1 -> resources.getColor(R.color.aqi1_1)
            2 -> resources.getColor(R.color.aqi2)
            3 -> resources.getColor(R.color.aqi3)
            5 -> resources.getColor(R.color.aqi4)
            6 -> resources.getColor(R.color.aqi5)
            else -> resources.getColor(R.color.default_color)
        }
    }

    private fun gradient2(aqi: Int): Int {
        return when(aqi) {
            1 -> resources.getColor(R.color.aqi1P)
            2 -> resources.getColor(R.color.aqi2P)
            3 -> resources.getColor(R.color.aqi3P)
            5 -> resources.getColor(R.color.aqi4P)
            6 -> resources.getColor(R.color.aqi5P)
            else -> resources.getColor(R.color.default_color)
        }
    }

    companion object {
        private lateinit var mFusedLocationClient: FusedLocationProviderClient
        private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
        private lateinit var gd: GradientDrawable
        private lateinit var mRvDailyForecast: RecyclerView
        private lateinit var mRvHourlyForecast: RecyclerView

    }
}