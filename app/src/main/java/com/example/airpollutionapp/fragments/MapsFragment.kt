package com.example.airpollutionapp.fragments

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context.LOCATION_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.airpollutionapp.R
import com.example.airpollutionapp.models.PollutionResponse
import com.example.airpollutionapp.network.PollutionService
import com.example.airpollutionapp.utils.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit.*
import java.lang.Math.round
import java.math.BigDecimal
import java.math.RoundingMode


class MapsFragment : Fragment(R.layout.fragment_maps),
    GoogleMap.OnMyLocationButtonClickListener,
    OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener {

    lateinit var mapFragment: SupportMapFragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val mapFragment = activity?.supportFragmentManager
//            ?.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        client = LocationServices.getFusedLocationProviderClient(requireActivity())

        // start | Chaquopy Python
        if (!Python.isStarted()) Python.start(AndroidPlatform(requireActivity()))
        var py = Python.getInstance()
        var pyobj: PyObject? = py.getModule("forecastYear")
        var regionName = ""
        var latitude = 0.0
        var longitude = 0.0
        var arrRegionAQIs = ArrayList<Int>()
        for (a in positionsRegions.keys) {
            regionName = a
            latitude = positionsRegions.get(a)!!.latitude
            longitude = positionsRegions.get(a)!!.longitude

            var obj: PyObject? = pyobj!!.callAttr("regionAQI", latitude, longitude)
            regionAQIs[regionName] = obj.toString().toInt()
            arrRegionAQIs.add(obj.toString().toInt())
        }


        var AlmatyDSum = 0.0
        var AlmatyDCount = 0.0
        for(i in 0..16) {
            AlmatyDSum += arrRegionAQIs[i]!!
            AlmatyDCount += 1
        }
        AlmatyDMean = round((AlmatyDSum / AlmatyDCount)).toInt()

        var EsilDSum = 0.0
        var EsilDCount = 0.0
        for(i in 17..19) {
            EsilDSum += arrRegionAQIs[i]!!
            EsilDCount += 1
        }
        EsilDMean = round((EsilDSum / EsilDCount)).toInt()

        var SaryarkaDSum = 0.0
        var SaryarkaDCount = 0.0
        for(i in 20..33) {
            SaryarkaDSum += arrRegionAQIs[i]!!
            SaryarkaDCount += 1
        }
        SaryarkaDMean = round((SaryarkaDSum / SaryarkaDCount)).toInt()

        super.onViewCreated(view, savedInstanceState)
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)

//        val location = LatLng(googleMap.myLocation.latitude, googleMap.myLocation.longitude)

        val service = activity?.getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = service.getBestProvider(criteria, false)
        val location = service.getLastKnownLocation(provider!!)
        val myLoc: LatLng

        if (!Python.isStarted()) Python.start(AndroidPlatform(requireActivity()))
        var py = Python.getInstance()
        var pyobj: PyObject? = py.getModule("forecastYear")
        var obj2: PyObject?// = pyobj!!.callAttr("regionAQI", location!!.latitude, location!!.longitude)

        var lat = 0.0
        var lon = 0.0
        if(location != null) {
            myLoc = LatLng(location!!.latitude, location!!.longitude)
            obj2 = pyobj!!.callAttr("regionAQI", location!!.latitude, location!!.longitude)
            lat = location!!.latitude
            lon = location!!.longitude
        }  else {
            myLoc = LatLng(51.177601, 71.432999)
            obj2 = pyobj!!.callAttr("regionAQI", 51.177601, 71.432999)
            lon = 51.177601
            lon = 71.432999
        }

//

        m = map.addMarker(
                MarkerOptions()
                        .position(myLoc)
                        .title("My Location")
                        .snippet("aqi: $obj2")
                        .draggable(true)
        )

        map.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {
                lat = p0.position.latitude
                lon = p0.position.longitude
                p0.title = "${BigDecimal(lat).setScale(4, RoundingMode.HALF_EVEN)}, ${BigDecimal(lon).setScale(4, RoundingMode.HALF_EVEN)}"
                obj2 = pyobj!!.callAttr("regionAQI", lat, lon)
                p0.snippet = "aqi: $obj2"
            }

            override fun onMarkerDragEnd(p0: Marker) {
                lat = p0.position.latitude
                lon = p0.position.longitude
                p0.title = "${BigDecimal(lat).setScale(4, RoundingMode.HALF_EVEN)}, ${BigDecimal(lon).setScale(4, RoundingMode.HALF_EVEN)}"
                obj2 = pyobj!!.callAttr("regionAQI", lat, lon)
                p0.snippet = "aqi: $obj2"
            }

            override fun onMarkerDragStart(p0: Marker) {
                lat = p0.position.latitude
                lon = p0.position.longitude
                p0.title = "${BigDecimal(lat).setScale(4, RoundingMode.HALF_EVEN)}, ${BigDecimal(lon).setScale(4, RoundingMode.HALF_EVEN)}"
                obj2 = pyobj!!.callAttr("regionAQI", lat, lon)
                p0.snippet = "aqi: $obj2"
            }
        })




        addPolyEsil()
        addPolySaryarka()
        addPolyAlmatinskiy()


        var regionName = ""
        var latitude = 0.0
        var longitude = 0.0
        for (a in positionsRegions.keys) {
            regionName = a
            latitude = positionsRegions.get(a)!!.latitude
            longitude = positionsRegions.get(a)!!.longitude

            getLocationPollutionDetails(latitude, longitude, regionName)
        }

        //1
        val nur_sultan = LatLng(51.1801, 71.44598) //51.5072167  -0.127585 //35.00116, 135.7681
        // Set the map type none, normal, hybrid, satellite и terrain.
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Move the camera to the map coordinates and zoom in closer.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(nur_sultan))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(10f)) //15f
    }

    // start: current location
    override fun onMyLocationClick(location: Location) {
        var myLoc = LatLng(50.0,50.0)
        m!!.position = myLoc
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(activity, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }
    // end: current location




    private fun addMarkers(
            position: LatLng,
            title: String,
            icon: BitmapDescriptor = BitmapDescriptorFactory.defaultMarker(),
            snippet: String? = null,
            infoWindowAnchorX: Float = 0.5F,
            infoWindowAnchorY: Float = 0F,
            zIndex: Float = 0F
    ) {
        map.addMarker(
                MarkerOptions()
                        .position(position)
                        .title(title)
                        .snippet(snippet)
                        .icon(icon)
                        .zIndex(zIndex)
                        .infoWindowAnchor(infoWindowAnchorX, infoWindowAnchorY)

        )
    }

    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int = 0): BitmapDescriptor {
        val vectorDrawable: Drawable? = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null) {
            Log.e(TAG, "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getLocationPollutionDetails(
            latitude: Double,
            longitude: Double,
            regionName: String = ""
    ) {
        if(activity?.let { Constants.isNetworkAvailable(it) } == true) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: PollutionService = retrofit.create<PollutionService>(PollutionService::class.java)

            val listCall: Call<PollutionResponse> = service.getPollution(
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

                        // change color of marker by aqi and add snippet
                        when (pollutionList.list!![0].main!!.aqi) {
                            1 -> {
                                addMarkers(
                                        LatLng(latitude, longitude),
                                        regionName,
                                        vectorToBitmap(
                                                R.drawable.ic_marker_aqi_1,
                                                resources.getColor(R.color.aqi1M)
                                        ),
                                        "aqi: ${pollutionList.list[0].main!!.aqi}"//, Color.parseColor("#A4C639"))
                                )
                            }
                            2 -> {
                                addMarkers(
                                        LatLng(latitude, longitude),
                                        regionName,
                                        vectorToBitmap(
                                                R.drawable.ic_marker_aqi_1,
                                                resources.getColor(R.color.aqi2M)
                                        ),
                                        "aqi: ${pollutionList.list[0].main!!.aqi}"//, Color.parseColor("#A4C639"))
                                )
                            }
                            2 -> {
                                addMarkers(
                                        LatLng(latitude, longitude),
                                        regionName,
                                        vectorToBitmap(
                                                R.drawable.ic_marker_aqi_1,
                                                resources.getColor(R.color.aqi3M)
                                        ),
                                        "aqi: ${pollutionList.list[0].main!!.aqi}"//, Color.parseColor("#A4C639"))
                                )
                            }
                            2 -> {
                                addMarkers(
                                        LatLng(latitude, longitude),
                                        regionName,
                                        vectorToBitmap(
                                                R.drawable.ic_marker_aqi_1,
                                                resources.getColor(R.color.aqi4M)
                                        ),
                                        "aqi: ${pollutionList.list[0].main!!.aqi}"//, Color.parseColor("#A4C639"))
                                )
                            }
                            2 -> {
                                addMarkers(
                                        LatLng(latitude, longitude),
                                        regionName,
                                        vectorToBitmap(
                                                R.drawable.ic_marker_aqi_1,
                                                resources.getColor(R.color.aqi5M)
                                        ),
                                        "aqi: ${pollutionList.list[0].main!!.aqi}"//, Color.parseColor("#A4C639"))
                                )
                            }
                            else -> { // Note the block
//                                print("x is neither 1 nor 2")
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
        } else {
            Toast.makeText(
                    activity,
                    "No internet connection available",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addPolyEsil() {
        map.addPolygon(
                PolygonOptions()
                        .add(*POLYGON_ESIL)
                        .fillColor(fillColoR(EsilDMean))
                        .strokeColor(strokeColoR(EsilDMean))
                        .strokeWidth(10f)
        )
    }

    private fun addPolySaryarka() {
        map.addPolygon(
                PolygonOptions()
                        .add(*POLYGON_SARYARKA)
                        .fillColor(fillColoR(SaryarkaDMean))
                        .strokeColor(strokeColoR(SaryarkaDMean))
                        .strokeWidth(10f)
        )
    }

    private fun addPolyAlmatinskiy() {
        map.addPolygon(
                PolygonOptions()
                        .add(*POLYGON_ALNATINSKIY)
                        .fillColor(fillColoR(AlmatyDMean))
                        .strokeColor(strokeColoR(AlmatyDMean))
                        .strokeWidth(10f)
        )
    }

    private fun fillColoR(aqi: Int): Int {
        return when(aqi) {
            1 -> resources.getColor(R.color.aqi1P)
            2 -> resources.getColor(R.color.aqi2P)
            3 -> resources.getColor(R.color.aqi3P)
            5 -> resources.getColor(R.color.aqi4P)
            6 -> resources.getColor(R.color.aqi5P)
            else -> resources.getColor(R.color.default_color)
        }
    }

    private fun strokeColoR(aqi: Int): Int {
        return when(aqi) {
            1 -> resources.getColor(R.color.aqi1_1)
            2 -> resources.getColor(R.color.aqi2)
            3 -> resources.getColor(R.color.aqi3)
            5 -> resources.getColor(R.color.aqi4)
            6 -> resources.getColor(R.color.aqi5)
            else -> resources.getColor(R.color.default_color)
        }
    }



    //static members
    companion object {
        private lateinit var map: GoogleMap
        private val regionAQIs = HashMap<String, Int?>()

        private var AlmatyDMean = 0
        private var EsilDMean = 0
        private var SaryarkaDMean = 0

        private lateinit var client: FusedLocationProviderClient

        private var m: Marker? = null

        private val positionsRegions =
            mapOf(
                    "Chugunka" to LatLng(51.179049, 71.478587), //А i=0
                    "Yugo-Vostok(right)" to LatLng(51.140015, 71.504756), //A
                    "Yugo-Vostok(left)" to LatLng(51.148539, 71.518069), //A
                    "Akbulak 3" to LatLng(51.140606, 71.451886), //A
                    "Tselinnyi" to LatLng(51.158375, 71.450908), //A
                    "Akbulak 4" to LatLng(51.13706, 71.458796), //A
                    "Otau" to LatLng(51.12967, 71.55622), //A
                    "Microraion 4A" to LatLng(51.263577, 71.632863), //A
                    "The 4th microraion" to LatLng(51.046264, 71.793074), //A
                    "The 9th microraion" to LatLng(51.271116, 71.661008), //A
                    "Microraion 4B" to LatLng(51.263223, 71.637542), //A
                    "Yubeleinyi" to LatLng(51.082936, 71.746572), //A
                    "Zhetigen" to LatLng(51.265258, 71.641769), //A
                    "Naberezhnyi" to LatLng(51.069519, 71.753788), //A
                    "Zhana auyl" to LatLng(51.085897, 71.722884), //A
                    "The 7th microraion" to LatLng(51.276204, 71.663677), //A
                    "The 11th microraion" to LatLng(51.265972, 71.669415), //A i=16


                    "Samal" to LatLng(51.156002, 71.429466), // S i=17
                    "Microrain the 804th km" to LatLng(51.21369, 71.361973), //S
                    "The 12th microraion" to LatLng(51.282754, 71.627487), //Si=19

                    "Urker" to LatLng(51.1268, 71.249842), //E 1=20
                    "Shubar" to LatLng(51.143235, 71.430295), //E
                    "Karaotkel 1" to LatLng(51.140904, 71.405006), //E
                    "The 3rd microraion" to LatLng(51.140704, 71.436664), //E
                    "Zhagalau" to LatLng(51.133675, 71.364327), //E
                    "Microraion 5B" to LatLng(50.97333, 71.39115), //E
                    "The 19th microraion" to LatLng(51.132173, 71.195666), //E
                    "Microraion 3A" to LatLng(50.969244, 71.35915), //E
                    "The 2nd microraion" to LatLng(51.087497, 71.215712), //E
                    "The 6th microraion" to LatLng(50.960472, 71.389142), //E
                    "The 20th microraion" to LatLng(50.950153, 71.353537), //E
                    "Kultegin" to LatLng(50.960187, 71.378422), //E
                    "VIP gorodok" to LatLng(50.986704, 71.363021), //E
                    "The 21st microraion" to LatLng(50.961815, 71.335721) //E i=33
            )

        private val POLYGON_ESIL = arrayOf(
                LatLng(51.001234, 71.454279),
                LatLng(51.048992, 71.555236),
                LatLng(51.091373, 71.598503),
                LatLng(51.104159, 71.523846),
                LatLng(51.100963, 71.513665),
                LatLng(51.105225, 71.499667),
                LatLng(51.100221, 71.490216),
                LatLng(51.105795, 71.472461),
                LatLng(51.108678, 71.450726),
                LatLng(51.122322, 71.445216),
                LatLng(51.126549, 71.451339),
                LatLng(51.147294, 71.438175),
                LatLng(51.151711, 71.427461),
                LatLng(51.159583, 71.420420),
                LatLng(51.157279, 71.403890),
                LatLng(51.165726, 71.391339),
                LatLng(51.168413, 71.368992),
                LatLng(51.163230, 71.358583),
                LatLng(51.171484, 71.346951),
                LatLng(51.188755, 71.291236),
                LatLng(51.186453, 71.269808),
                LatLng(51.179353, 71.279604),
                LatLng(51.174555, 71.266440),
                LatLng(51.174747, 71.253277),
                LatLng(51.160735, 71.253277),
                LatLng(51.136841, 71.235431),
                LatLng(51.134618, 71.235828),
                LatLng(51.114240, 71.224529),
                LatLng(51.110092, 71.216600),
                LatLng(51.071704, 71.243361),
                LatLng(51.064645, 71.255915),
                LatLng(51.051147, 71.254263),
                LatLng(51.017906, 71.312079)
        )

        private val POLYGON_SARYARKA = arrayOf(
                LatLng(51.247660, 71.262720),
                LatLng(51.234048, 71.301016),
                LatLng(51.268409, 71.339955),
                LatLng(51.270805, 71.365489),
                LatLng(51.304340, 71.352403),
                LatLng(51.310127, 71.387831),
                LatLng(51.270206, 71.405067),
                LatLng(51.274799, 71.433792),
                LatLng(51.271404, 71.450070),
                LatLng(51.278792, 71.468263),
                LatLng(51.287177, 71.477519),
                LatLng(51.290370, 71.498584),
                LatLng(51.284182, 71.498265),
                LatLng(51.261619, 71.470816),
                LatLng(51.245238, 71.548695),
                LatLng(51.259222, 71.565930),
                LatLng(51.279191, 71.595932),
                LatLng(51.268609, 71.606465),
                LatLng(51.233665, 71.555319),
                LatLng(51.186808, 71.447410),
                LatLng(51.180215, 71.442345),
                LatLng(51.178017, 71.424036),
                LatLng(51.155161, 71.431291),
                LatLng(51.152382, 71.427494),
                LatLng(51.160322, 71.421481),
                LatLng(51.157940, 71.403759),
                LatLng(51.166276, 71.391417), // граница с есиоь
                LatLng(51.169252, 71.368631),
                LatLng(51.163894, 71.358504),
                LatLng(51.171038, 71.347428),
                LatLng(51.189488, 71.291729),
                LatLng(51.190083, 71.255969),
                LatLng(51.209120, 71.237613),
                LatLng(51.223590, 71.246791),
                LatLng(51.224978, 71.244892)
        )

        private val POLYGON_ALNATINSKIY = arrayOf(
                LatLng(51.267931, 71.607123),
                LatLng(51.233538, 71.554745),
                LatLng(51.185224, 71.447098),
                LatLng(51.179785, 71.441635),
                LatLng(51.177166, 71.423319),
                LatLng(51.155378, 71.430002),
                LatLng(51.152480, 71.428143),
                LatLng(51.147891, 71.439129),
                LatLng(51.140297, 71.441612),
                LatLng(51.129219, 71.448496),
                LatLng(51.126006, 71.452672),
                LatLng(51.122132, 71.446501),
                LatLng(51.115441, 71.449306),
                LatLng(51.109276, 71.451270),
                LatLng(51.106282, 71.458283),
                LatLng(51.105754, 71.471749),
                LatLng(51.102759, 71.476238),
                LatLng(51.100117, 71.489143),
                LatLng(51.105225, 71.500645),
                LatLng(51.100645, 71.512988),
                LatLng(51.103288, 71.526454),
                LatLng(51.093598, 71.591539),
                LatLng(51.089545, 71.606969),
                LatLng(51.099060, 71.615946),
                LatLng(51.086021, 71.673737),
                LatLng(51.090426, 71.676543),
                LatLng(51.093069, 71.685800),
                LatLng(51.092893, 71.695058),
                LatLng(51.089898, 71.695619),
                LatLng(51.090250, 71.698986),
                LatLng(51.092365, 71.698986),
                LatLng(51.093069, 71.707963),
                LatLng(51.096417, 71.707402),
                LatLng(51.101350, 71.721709),
                LatLng(51.104345, 71.724515),
                LatLng(51.103464, 71.727601),
                LatLng(51.100821, 71.728723),
                LatLng(51.096769, 71.735456),
                LatLng(51.098179, 71.739103),
                LatLng(51.101878, 71.740786),
                LatLng(51.101702, 71.743030),
                LatLng(51.105225, 71.741347),
                LatLng(51.113856, 71.705719),
                LatLng(51.131288, 71.714976),
                LatLng(51.142026, 71.686922),
                LatLng(51.140969, 71.653538),
                LatLng(51.188644, 71.620154),
                LatLng(51.199017, 71.587331),
                LatLng(51.203060, 71.583123),
                LatLng(51.219931, 71.574707),
                LatLng(51.252777, 71.623240)
        )
    }

}