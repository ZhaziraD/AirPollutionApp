package com.example.airpollutionapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.airpollutionapp.R
import com.example.airpollutionapp.models.PollutionResponse
import com.example.airpollutionapp.network.ForecastService
import com.example.airpollutionapp.network.HistoricalService
import com.example.airpollutionapp.network.PollutionService
import com.example.airpollutionapp.utils.Constants
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

//    lateinit var textView: TextView
    lateinit var iv: ImageView

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned OFF. Please turn it on",
                Toast.LENGTH_SHORT
            ).show()

            //redirect to the settings from where you need to turn on the location provider
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this)
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
                                    this@MainActivity,
                                    "You have denied location permission, Please allow it is mandatory",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread()
                    .check()
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions required for this feature. It can be enabled under Application S")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
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

    private fun isLocationEnabled() : Boolean {
        //provides access to the system location services
        val locationManager : LocationManager = getSystemService(
            Context.LOCATION_SERVICE
        )  as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.
        isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
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
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude", "$longitude")

            //7777
            //call the api calling function here
            var start = 1650674820
            var end = 1650682020
            // Esil
            // Almaty
            // Baikonyr
            // ...
            getLocationPollutionDetails(latitude, longitude, start, end)
//            loadDailyForecast(latitude, longitude)
        }
    }


    private fun getLocationPollutionDetails(
        latitude: Double,
        longitude: Double,
        start: Int = 0,
        end: Int = 0
    ) {
        if(Constants.isNetworkAvailable(this)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: PollutionService = retrofit.create<PollutionService>(PollutionService::class.java)
            val forecastService: ForecastService = retrofit.create<ForecastService>(ForecastService::class.java)
            val historicalService: HistoricalService = retrofit.create<HistoricalService>(
                HistoricalService::class.java
            )

            val listCall: Call<PollutionResponse> = service.getPollution(
                latitude, longitude, Constants.APP_ID
            )

            val forecastListCall: Call<PollutionResponse> = forecastService.getForecast(
                latitude, longitude, Constants.APP_ID
            )

            val historicalListCall: Call<PollutionResponse> = historicalService.getHistory(
                latitude, longitude, start, end, Constants.APP_ID
            )

            listCall.enqueue(object : Callback<PollutionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<PollutionResponse>,
                    retrofit: Retrofit
                ) {
                    if (response.isSuccess) {

                        val pollutionList: PollutionResponse = response.body()
                        Log.i("Response Result2", "$pollutionList")


                        //use timezone instead of dt
                        var dateFormat = SimpleDateFormat("EEE, d MMM  HH:mm")
                        var localDate =
                            Date(/*Date().time +*/ pollutionList.list!![0].dt!!.toLong() * 1000)
                        Log.i("Response Result3", dateFormat.format(localDate))

                        val city = CoordToName(
                            pollutionList.coord!!.getLat(),
                            pollutionList.coord!!.getLon()
                        )
                        Log.i("Response Result4", city)

                        // start | Chaquopy Python

//                        textView = findViewById(R.id.tvRes)
//                        if (!Python.isStarted()) Python.start(AndroidPlatform(this@MainActivity))
//
//                        val py = Python.getInstance()
//                        val pyobj: PyObject? = py.getModule("script")
//
//                        var obj: PyObject? = null
//
//                        //func name //1st arg //2nd arg
//                        obj = pyobj!!.callAttr("main",  pollutionList.coord!!.getLat().toInt().toString(),  pollutionList.coord!!.getLon().toInt().toString())
//
//                        textView.text = obj.toString()

                        // plots

                        iv = findViewById(R.id.image_view)

                        if (!Python.isStarted()) Python.start(AndroidPlatform(this@MainActivity))

                        // create python object.. to load script
                        val py = Python.getInstance()
                        val pyo: PyObject? = py.getModule("script")
                        val obj: PyObject? = pyo!!.callAttr("main", "3,1,5", "4,4,8")

                        val str = obj.toString()
                        val data = android.util.Base64.decode(str, android.util.Base64.DEFAULT)//android.util.Base64.decode(str, Base64.DEFAULT)

                        val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)

                        iv.setImageBitmap(bmp)

                        // create  script


                        // end | Chaquopy Python


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

            historicalListCall.enqueue(object : Callback<PollutionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<PollutionResponse>,
                    retrofit: Retrofit
                ) {
                    if (response.isSuccess) {
                        val pollutionList: PollutionResponse = response.body()
                        for (i in 0 until pollutionList.list!!.size) { // i in 1..pollutionList.list!!.size-1
                            Log.i("Response Result5", pollutionList.list!![i].toString())
                        }

                        Log.i("Response Result2", pollutionList.list!!.size.toString())

                        //use timezone instead of dt
                        var dateFormat = SimpleDateFormat("EEE, d MMM  HH:mm")
                        var localDate =
                            Date(/*Date().time +*/ pollutionList.list!![0].dt!!.toLong() * 1000)
                        Log.i("Response Result3", dateFormat.format(localDate))

                        val city = CoordToName(
                            pollutionList.coord!!.getLat(),
                            pollutionList.coord!!.getLon()
                        )
                        Log.i("Response Result4", city)

                        // ggplot2


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

            forecastListCall.enqueue(object : Callback<PollutionResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<PollutionResponse>,
                    retrofit: Retrofit
                ) {
                    if (response.isSuccess) {
                        val pollutionList: PollutionResponse = response.body()
                        for (i in 1..5) { // i in 1..pollutionList.list!!.size-1
                            Log.i("Response Result2", pollutionList.list!![i].toString())
                        }

                        //use timezone instead of dt
                        var dateFormat = SimpleDateFormat("EEE, d MMM  HH:mm")
                        var localDate =
                            Date(/*Date().time +*/ pollutionList.list!![0].dt!!.toLong() * 1000)
                        Log.i("Response Result3", dateFormat.format(localDate))

                        val city = CoordToName(
                            pollutionList.coord!!.getLat(),
                            pollutionList.coord!!.getLon()
                        )
                        Log.i("Response Result4", city)


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
        } else {
            Toast.makeText(
                this@MainActivity,
                "No internet connection available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun CoordToName(latitude: Double, longitude: Double): String {
        val addresses: List<Address>
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        addresses = geocoder.getFromLocation(latitude, longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


        val address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        val city = addresses[0].locality
//        val state = addresses[0].adminArea
        val country = addresses[0].countryName
//        val postalCode = addresses[0].postalCode
//        val knownName = addresses[0].featureName // Only if available else return NULL


//        val res = "$city, $state, $postalCode, $knownName"
        return city
//        I/Current Latitude: 51.1605217
//        I/Current Longitude: 71.470355

//        var city = ""
//        try {
//            val geocoder = Geocoder(context, Locale.getDefault())
//            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
//            if (addresses != null && addresses.size > 0) {
//                val address: String = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                city = addresses[0].getLocality()
//                val state: String = addresses[0].getAdminArea()
//                val country: String = addresses[0].getCountryName()
//                val postalCode: String = addresses[0].getPostalCode()
//                val knownName: String = addresses[0].getFeatureName() // Only if available else return NULL
//                Log.d(TAG, "getAddress:  address$address")
//                Log.d(TAG, "getAddress:  city$city")
//                Log.d(TAG, "getAddress:  state$state")
//                Log.d(TAG, "getAddress:  postalCode$postalCode")
//                Log.d(TAG, "getAddress:  knownName$knownName")
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return city
    }
}