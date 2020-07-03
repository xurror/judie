package cm.ubuea.judie

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*

@Suppress("DEPRECATION")
class MapModule : FragmentActivity(), OnMapReadyCallback, LocationListener, OnInitListener {
    private var mMap: GoogleMap? = null
    private var tts: TextToSpeech? = null
    var lm: LocationManager? = null
    var add = "Please check your connection and location services..."
    var tv: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        tv = findViewById<View>(R.id.a) as TextView
        tts = TextToSpeech(this, this)
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
    var tower: String? = null
    var lati = 0.0
    var longi = 0.0
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val crit = Criteria()
        tower = lm!!.getBestProvider(crit, false)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val location = location
        if (location != null) {
            lati = location.latitude
            longi = location.longitude
            add = getAddress(lati, longi)
            speakOut()
        }
        // Add a marker in Sydney and move the camera
        val marker = LatLng(lati, longi)
        mMap!!.addMarker(MarkerOptions().position(marker).title(add))
        print(add)
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 14f))

        //Emergeny MOdule
        if (intent.getBooleanExtra(Commands.EMERGENCY, false)) {
            val smsManager = SmsManager.getDefault()
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val num = preferences.getString(Needs.EMER, null)!!.split(",".toRegex()).toTypedArray()
            for (nu in num) smsManager.sendTextMessage(nu.trim { it <= ' ' }, null, add, null, null)
            Toast.makeText(this, "Message Sent", Toast.LENGTH_LONG)
            finish()
        }
    }

    fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var add = ""
        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val obj = addresses[0]
            add = obj.getAddressLine(0)
            add = """
                $add
                ${obj.subLocality}
                """.trimIndent()
            add = """
                $add
                ${obj.countryName}
                """.trimIndent()
            add = """
                $add
                ${obj.countryCode}
                """.trimIndent()
            add = """
                $add
                ${obj.adminArea}
                """.trimIndent()
            add = """
                $add
                0${obj.postalCode}
                """.trimIndent()
            add = """
                $add
                ${obj.subAdminArea}
                """.trimIndent()
            tv!!.text = add
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        return add
    }// TODO: Consider calling
    //    ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
    //                                          int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
    // if GPS Enabled get lat/long using GPS Services
// no network provider is enabled

    // getting GPS status
    val location: Location?
        get() {
            var location: Location? = null
            var longitude = 0.0
            var latitude = 0.0
            try {
                val locationManager = this
                        .getSystemService(Context.LOCATION_SERVICE) as LocationManager

                // getting GPS status
                val isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)

                // getting network status
                val isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                    add = "Location or Network Services is disabled ,please enable it and try again"
                    tv!!.text = add
                    speakOut()
                } else {
                    val canGetLocation = true
                    if (isNetworkEnabled) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return null
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                1000, 10f, this)
                        Log.d("Network", "Network Enabled")
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (location != null) {
                                latitude = location.latitude
                                longitude = location.longitude
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER, 1000, 10f, this)
                            Log.d("GPS", "GPS Enabled")
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return location
        }

    override fun onLocationChanged(location: Location) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    //Speak Out
    private fun speakOut() {
        val text = add
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                speakOut()
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    public override fun onDestroy() {
        // Shuts Down TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        private const val REQ_CODE = 1000
    }
}