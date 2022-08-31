package com.arashforus.nearroom

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arashforus.nearroom.databinding.ActivityCreateRoomBinding
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject

class CreateRoomActivity : AppCompatActivity() , OnMapReadyCallback , LocationListener  {

    private lateinit var binding : ActivityCreateRoomBinding
    private var gmap: GoogleMap? = null
    private val accessFineLocationPermissionCode = 200
    private var canLocate : Boolean = false
    private val defaultZoom = 17f
    private val defaultLocation : LatLng = LatLng(35.700090, 51.398798)
    private val updateIntervalInMilliseconds : Long = 10000
    private val fastestUpdateIntervalInMilliseconds : Long = 5000

    private var mLastLocation: Location? = null
    private var mPreviousLocation: Location = Location("")
    private var mLocationMarker : Marker? = null
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var settingsClient: SettingsClient? = null
    //private var locationSettingsRequest: LocationSettingsRequest? = null

    private lateinit var myAlertDialog : AlertDialog
    private lateinit var myAlert : AlertDialog.Builder
    private lateinit var accessLocationAlert : AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //onTransformationEndContainer(intent.getParcelableExtra("TransformationParams"))
        MyTools(this).forceHideKeyboard(this)
        binding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeGpsAlertDialogue()
        makeLocationConfiguration()
        createAndReadyGoogleMap()
        makeListeners()

        // Gps BroadcastReceiver ///////////////////////////////////////////////////////////////////////
        val br: BroadcastReceiver = LocationProviderChangedReceiver(object : LocationProviderChangedReceiver.gpsChanged {
            override fun gpsStatus(status: Boolean) {
                canLocate = if (!status) {
                    myAlertDialog.show()
                    false
                } else {
                    myAlertDialog.dismiss()
                    true
                }
                startLocationUpdate()
            }
        })
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(br, filter)

        getLocationPermission()

    }

    override fun onResume() {
        super.onResume()
        getLocationPermission()
    }

    private fun createAndReadyGoogleMap() {
        // Create and Ready Google map /////////////////////////////////////////////////////////////
        val mapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun makeListeners() {
        // Create Room Button Listener /////////////////////////////////////////////////////////////
        binding.CreateRoomActivityCreateButton.setOnClickListener {
            MyTools(this).closeKeyboard()
            if (mLastLocation != null ) {
                if (binding.CreateRoomActivityRoomName.text.length in MyRules.minRoomNameLength..MyRules.maxRoomNameLength){
                    Volley_CreateNewRoom(this,
                        binding.CreateRoomActivityRoomName.text.toString(),
                        binding.CreateRoomActivityCategory.selectedItem.toString(),
                        binding.CreateRoomActivityMemberNumber.selectedItem.toString(),
                        mLastLocation!!.latitude,
                        mLastLocation!!.longitude,
                        object : Volley_CreateNewRoom.ServerCallBack {
                            override fun isSuccessful(result: JSONObject) {
                                if (result.getBoolean("isSuccess") && !result.isNull("participant") && !result.isNull("chat") ){
                                    DB_NearroomMessages(this@CreateRoomActivity).saveToDB(result)
                                    DB_NearoomsParticipant(this@CreateRoomActivity).saveToDB(result)
                                    val roomActivity = Intent(this@CreateRoomActivity, RoomActivity::class.java)
                                    roomActivity.putExtra("roomName",binding.CreateRoomActivityRoomName.text.toString())
                                    roomActivity.putExtra("roomPic","")
                                    startActivity(roomActivity)
                                    finish()
                                }else{
                                    binding.CreateRoomActivityStatus.visibility = View.VISIBLE
                                    if (result.getBoolean("roomExist")){
                                        binding.CreateRoomActivityStatus.text = "This room name is existed ,Please select unique one"
                                    }else{
                                        binding.CreateRoomActivityStatus.text = "Something wrong ,Try again"
                                    }
                                }
                            }
                        }).check()
                }else{
                    binding.CreateRoomActivityStatus.visibility = View.VISIBLE
                    binding.CreateRoomActivityStatus.text = "Room name must be between ${MyRules.minRoomNameLength} and ${MyRules.maxRoomNameLength} length"
                }

            }
        }
    }

    private fun makeLocationConfiguration() {
        // Location Finder Configuration ///////////////////////////////////////////////////////////
        mPreviousLocation.latitude = 35.700090
        mPreviousLocation.latitude = 51.398798

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = updateIntervalInMilliseconds
        mLocationRequest.fastestInterval = fastestUpdateIntervalInMilliseconds
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mLastLocation = locationResult.lastLocation
                updateLocationUI()
                updateMarker(mLastLocation)
            }
        }
    }

    private fun makeGpsAlertDialogue() {
        // Gps Alert Dialogue Configuration ////////////////////////////////////////////////////////
        myAlert = AlertDialog.Builder(this)
        myAlert.setTitle("Turn on GPS")
        myAlert.setMessage("For continue you should turn on your gps")
        myAlert.setCancelable(false)
        myAlert.setPositiveButton("Go to setting") { _, _ ->
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        myAlert.setNegativeButton("Back to home") { dialog, _ ->
            //startActivity(Intent(this@CreateRoomActivity, MainActivity::class.java))
            dialog.dismiss()
            dialog.cancel()
            onBackPressed()
        }
        myAlertDialog = myAlert.create()
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission( this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGPSisEnabled()
        } else {
            ActivityCompat.requestPermissions( this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),accessFineLocationPermissionCode)
            //ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        canLocate = false
        if (requestCode == accessFineLocationPermissionCode ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                checkGPSisEnabled()
            }else{
                accessLocationAlert = AlertDialog.Builder(this)
                accessLocationAlert.setTitle("Permission needed")
                accessLocationAlert.setMessage("You should access the app for locating to continue ")
                accessLocationAlert.setPositiveButton("Give permission"){ _, _ ->
                    getLocationPermission()
                }
                accessLocationAlert.setNegativeButton("Back to home"){ dialog, _ ->
                    //startActivity(Intent(this, MainActivity::class.java))
                    dialog.dismiss()
                    dialog.cancel()
                    onBackPressed()
                }
                accessLocationAlert.setCancelable(false)
                accessLocationAlert.show()
            }
        }
        updateLocationUI()
    }

    // Check GPS is on or off //////////////////////////////////////////////////////////////////////
    private fun checkGPSisEnabled() {
        val service: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val enabled: Boolean = service.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!enabled) {
            myAlertDialog.show()
            canLocate = false
        }else{
            canLocate = true
        }
        startLocationUpdate()
    }



    private fun startLocationUpdate() {
        if (canLocate) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,Looper.getMainLooper())
            }
        }else{
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        }
    }


    override fun onMapReady(googlemap: GoogleMap) {
        gmap = googlemap
        gmap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom))
        mLocationMarker = gmap?.addMarker( MarkerOptions().position(defaultLocation).title("Your Location").snippet("Your room base location is here" )
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)))
        gmap?.animateCamera(CameraUpdateFactory.zoomTo(defaultZoom))
    }


    private fun updateLocationUI() {
        if (gmap == null) {
            return
        }
        try {
            if (canLocate) {
                gmap?.isMyLocationEnabled = true
                gmap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                gmap?.isMyLocationEnabled = false
                gmap?.uiSettings?.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun updateMarker( p0 : Location?){
        if ( p0 != null ) {
            if ( p0.distanceTo(mPreviousLocation) > 20f ) {
                mLocationMarker?.remove()
                gmap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p0.latitude,p0.longitude), defaultZoom))
                mLocationMarker = gmap?.addMarker( MarkerOptions().position(LatLng(p0.latitude,p0.longitude)).title("Your Location").snippet("Your nearoom base location is here" ).visible(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)))
                gmap?.animateCamera(CameraUpdateFactory.zoomTo(defaultZoom))
                mPreviousLocation.latitude = p0.latitude
                mPreviousLocation.longitude = p0.longitude
            }
        }
    }



    override fun onLocationChanged(p0: Location) {
        //println(" on Location Changed is active now ..........................")
    }


}