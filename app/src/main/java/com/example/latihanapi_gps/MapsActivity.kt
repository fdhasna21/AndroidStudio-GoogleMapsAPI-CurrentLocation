package com.example.latihanapi_gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(){
    private lateinit var mMap : GoogleMap
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var myLocation : LatLng    //location based GPS
    private lateinit var myAddress : String     //address based GPS
    private var myMarker : Marker? = null       //marker based GPS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        getCurrentLocation()
    }

    @SuppressLint("RestrictedApi")
    fun getCurrentLocation() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest()
            .setInterval(3000)
            .setFastestInterval(3000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for(location in p0.locations){
                        mapFragment.getMapAsync{
                            mMap = it

                            if (ActivityCompat.checkSelfPermission(
                                    this@MapsActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                    this@MapsActivity,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                                return@getMapAsync
                            }

                            mMap.isMyLocationEnabled = true
                            mMap.uiSettings.isMapToolbarEnabled = true
                            mMap.uiSettings.isZoomControlsEnabled = true
                            //METODE 1
//            val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//            val provider = locationManager.getBestProvider(Criteria(), true)
//            val location: Location? = locationManager.getLastKnownLocation(provider!!)
//
//            if (location != null) {
//                myLocation = LatLng(location.latitude, location.longitude)
//                mMap.addMarker(MarkerOptions().position(myLocation).title("Start")).showInfoWindow()
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
//            }
//            else{
//                val sydney = LatLng(-34.0, 151.0)
//                mMap.addMarker(MarkerOptions().position(sydney).title("My Location")).showInfoWindow()
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))
//            }

                            //METODE 2
                            val locationResult = LocationServices.getFusedLocationProviderClient(this@MapsActivity).lastLocation
                            locationResult.addOnCompleteListener(this@MapsActivity){
                                if(it.isSuccessful && (it.result !=null)){
                                    myLocation = LatLng(it.result.latitude, it.result.longitude)
                                    val geocoder = Geocoder(this@MapsActivity)
                                    val geoAddress = geocoder.getFromLocation(it.result.latitude, it.result.longitude, 1)
                                    myAddress = geoAddress[0].getAddressLine(0)

                                    val newMarker = mMap.addMarker(MarkerOptions().position(myLocation).title(myAddress))
                                    newMarker.showInfoWindow()
                                    if(newMarker != myMarker){
                                        myMarker?.remove()
                                    }
                                    myMarker = newMarker
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
                                }
                            }
                        }
                    }
                }
            }, Looper.myLooper()
        )
    }
}