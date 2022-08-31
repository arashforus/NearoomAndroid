package com.arashforus.nearroom

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.*
import com.arashforus.nearroom.databinding.FragmentNearoomsBinding
import com.google.android.gms.location.*
import org.json.JSONObject

class NearoomsFragment : Fragment() {

    private var _binding : FragmentNearoomsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentActivity : Activity
    private lateinit var fragmentContext : Context
    private val accessFineLocationPermissionCode = 201
    private val updateIntervalInMilliseconds : Long = 10000
    private val fastestUpdateIntervalInMilliseconds : Long = 5000
    private lateinit var myAlertDialog : AlertDialog
    private lateinit var myAlert : AlertDialog.Builder
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNearoomsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAlertDialogue()
        makeFindingLocationFunctions()
    }

    override fun onStart() {
        makeListener()
        super.onStart()
    }

    override fun onAttach(activity: Activity) {
        fragmentActivity = activity
        fragmentContext = activity.applicationContext
        super.onAttach(activity)
    }

    private fun makeFindingLocationFunctions() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(fragmentContext)
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = updateIntervalInMilliseconds
        mLocationRequest.fastestInterval = fastestUpdateIntervalInMilliseconds
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation = locationResult.lastLocation
                Volley_FindNearooms(fragmentContext, lastLocation.latitude.toString(), lastLocation.longitude.toString(), object : Volley_FindNearooms.ServerCallBack {
                    override fun findSuccess(result: JSONObject) {
                        val nearooms = DB_Nearrooms(fragmentContext).loadRooms()
                        nearooms.sortBy {
                            it.distance
                        }
                        val adapter = Adapter_Rooms(nearooms)
                        binding.NearoomsFragmentRecyclerView.layoutManager = LinearLayoutManager(fragmentContext,LinearLayoutManager.VERTICAL,false)
                        binding.NearoomsFragmentRecyclerView.adapter = adapter
                    }
                }).find()
                mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            }
        }
    }

    private fun createAlertDialogue() {
        // Gps Alert Dialogue Configuration ////////////////////////////////////////////////////////
        myAlert = AlertDialog.Builder(fragmentActivity)
        myAlert.setTitle("Turn on GPS")
        myAlert.setMessage("For continue this app need your gps location , so turn it on to continue")
        myAlert.setCancelable(false)
        myAlert.setPositiveButton("Go to GPS setting") { _, _ ->
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        myAlert.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            dialog.cancel()
        }
        myAlertDialog = myAlert.create()
    }


    private fun makeListener() {
        binding.NearoomsFragmentFindButton.setOnClickListener {
            getLocationPermission()
        }

        binding.NearoomsFragmentCreateNearoomButton.setOnClickListener {
            startActivity(Intent(fragmentContext,CreateRoomActivity::class.java))
        }

        binding.NearoomsFragmentLastResultButton.setOnClickListener {
            startAnim(false)
            val nearooms = DB_Nearrooms(fragmentContext).loadRooms()
            nearooms.sortBy {
                it.distance
            }
            val adapter = Adapter_Rooms(nearooms)
            binding.NearoomsFragmentRecyclerView.layoutManager = LinearLayoutManager(fragmentContext,LinearLayoutManager.VERTICAL,false)
            binding.NearoomsFragmentRecyclerView.adapter = adapter
        }
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission( fragmentContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGPSisEnabled()
        } else {
            requestPermissions( arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),accessFineLocationPermissionCode)
        }
    }


    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        if (requestCode == accessFineLocationPermissionCode ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                checkGPSisEnabled()
            }else{
                val alert = AlertDialog.Builder(fragmentContext)
                alert.setTitle("Permission needed")
                alert.setMessage("You should access the app for gps to continue in this section")
                alert.setPositiveButton("Give permission"){ _, _ ->
                    getLocationPermission()
                }
                alert.setNegativeButton("Back"){ dialog, _ ->
                    dialog.dismiss()
                    dialog.cancel()
                }
                alert.setCancelable(false)
                alert.show()
            }
        }
    }

    // Check GPS is on or off //////////////////////////////////////////////////////////////////////
    private fun checkGPSisEnabled() {
        val service: LocationManager = fragmentActivity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val enabled: Boolean = service.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!enabled) {
            myAlertDialog.show()
        }else{
            if (ContextCompat.checkSelfPermission(fragmentContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,Looper.getMainLooper())
                startAnim(true)
            }else{
                getLocationPermission()
            }
        }
    }

    private fun startAnim(isNew : Boolean) {
        val constraint1 = ConstraintSet()
        constraint1.clone(fragmentContext,R.layout.fragment_nearooms_final)
        TransitionManager.beginDelayedTransition(binding.root)
        constraint1.applyTo(binding.root)

        if ( isNew ){
            binding.NearoomsFragmentFindButton.text = "SEARCH AGAIN"
        }

        binding.NearoomsFragmentFindButton.setTextColor(Color.WHITE)

        binding.NearoomsFragmentCircles.animateDown(500L)

    }


}