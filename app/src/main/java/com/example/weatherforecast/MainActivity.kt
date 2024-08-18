package com.example.weatherforecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView


import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherforecast.ui.theme.WeatherForecastTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.squareup.picasso.Picasso
import org.json.JSONException
import java.util.Locale


class MainActivity : ComponentActivity() {

    private lateinit var homeRL: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var cityName: TextView
    private lateinit var cityText: EditText
    private lateinit var backIv: ImageView
    private lateinit var iconIv: ImageView
    private lateinit var searchIcon: ImageView
    private lateinit var tempearture: TextView
    private lateinit var conditionIv: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var weatherRvModalArrayList: ArrayList<weatherRVModal>
    private lateinit var weatherAdapter: weatherRVAdavter
    private lateinit var locationManager: LocationManager
    private lateinit var currentcityName: String

    var location: Location? = null
    private var Permission_Code = 1


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_main)
//        setContent {
//            WeatherForecastTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }

        homeRL = findViewById(R.id.idRLHome)
        progressBar = findViewById(R.id.idLoading)
        cityName = findViewById(R.id.idTextView)
        cityText = findViewById(R.id.idETCityName)
        backIv = findViewById(R.id.idImageView)
        iconIv = findViewById(R.id.idWeatherImage)
        searchIcon = findViewById(R.id.idIVsearch)
        tempearture = findViewById(R.id.idTeTV)
        conditionIv = findViewById(R.id.TextViewWC)
        recyclerView = findViewById(R.id.idTDWeather)
        weatherRvModalArrayList = ArrayList(listOf(weatherRVModal("", "", " ", "")))

        weatherAdapter = weatherRVAdavter(this, weatherRvModalArrayList)
        recyclerView.adapter = weatherAdapter
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Permission_Code

            )


        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location == null) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L, // Minimum time interval between updates, in milliseconds
                0f  // Minimum distance between updates, in meters
            ) { locate ->
                // Handle the new location here
                locate?.let {
                    location = locate

                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L, // Minimum time interval between updates, in milliseconds
                0f  // Minimum distance between updates, in meters
            ) { locate ->
                // Handle the new location here
                locate?.let {
                    location = locate

                }
            }
        }





        currentcityName = ""

        if (location != null) {

            currentcityName = getCityName(location!!.latitude, location!!.longitude)
        };
        else {
            Toast.makeText(
                this,
                "Please Switch on the location as well as kill and relaunch the app",
                Toast.LENGTH_LONG
            ).show();
        }

        getWeatherInfo()
        searchIcon.setOnClickListener {
            val city = cityText.text.toString();
            if (city.isNotEmpty()) {
                cityName.text = city
                currentcityName = city
                getWeatherInfo()
            } else {
                Toast.makeText(this, "Please Enter Valid City", Toast.LENGTH_LONG).show();
            }
        }


    }

    fun getCityName(lat: Double, lon: Double): String {

        var cityName = "";
        val geocoder = Geocoder(baseContext, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {

                geocoder.getFromLocation(lat, lon, 10, Geocoder.GeocodeListener {
                    fun onGeocode(addresses: MutableList<Address>) {
                        for (add in addresses) {
                            if (!add.equals("")) {
                                cityName = add.locality
                            } else {
                                Toast.makeText(this, "City is Not found", Toast.LENGTH_LONG).show();
                            }

                        }
                        // code
                    }
                })

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 10)
                if (addresses != null) {
                    for (add in addresses) {
                        if (!add.equals("")) {
                            cityName = add.locality
                        } else {
                            Toast.makeText(this, "City is Not found", Toast.LENGTH_LONG).show();
                        }

                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return cityName;


    }


    private fun getWeatherInfo() {
        if (currentcityName == "") {
            currentcityName = "Giridih"
        }
        var url =
            "https://api.weatherapi.com/v1/forecast.json?key=c24da0a4c1df4d2589370512241608&q=$currentcityName&days=1&aqi=no&alerts=yes";
        cityName = cityText;

        val requestqueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    progressBar.visibility = View.GONE
                    homeRL.visibility = View.VISIBLE
                    weatherRvModalArrayList.clear()
                    val temp = response.getJSONObject("current").getString("temp_c")
                    tempearture.text = temp + "°C";
                    val isDay = response.getJSONObject("current").getInt("is_day")
                    val condition = response.getJSONObject("current").getJSONObject("condition")
                        .getString("text")
                    val icon = response.getJSONObject("current").getJSONObject("condition")
                        .getString("icon")

                    Picasso.get().load("https:" + icon).into(iconIv)
                    conditionIv.text = condition
                    if (isDay == 1) {
                        Picasso.get()
                            .load("https://plus.unsplash.com/premium_photo-1673002094058-8598519236db?fm=jpg&q=60&w=3000&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTN8fG1vdW50YWluJTIwdmlld3xlbnwwfHwwfHx8MA%3D%3D")
                            .into(backIv)
                    } else {
                        Picasso.get()
                            .load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRvLJKjoiamnCCZNi3ebFx1pBM8Yoahdut_mg&s")
                            .into(backIv)
                    }

                    val forcastObj = response.getJSONObject("forecast")
                    val forecastArray = forcastObj.getJSONArray("forecastday").getJSONObject(0)
                    val hourArray = forecastArray.getJSONArray("hour")
                    for (i in 0 until hourArray.length()) {
                        val hr = hourArray.getJSONObject(i)
                        val text = hr.getJSONObject("condition").getString("text")
                        val icon = hr.getJSONObject("condition").getString("icon")
                        val temper = hr.getString("temp_c")
                        val time = hr.getString("time")
                        val wind = hr.getString("wind_kph")

                        weatherRvModalArrayList.add(weatherRVModal(time, temper, icon, wind))

                    }
                    weatherAdapter.notifyDataSetChanged()


                } catch (e: JSONException) {

                    e.printStackTrace()
                }
            }) { error -> error.printStackTrace() }

        requestqueue.add(jsonObjectRequest)


    }


}

