package com.example.storyapp.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.R
import com.example.storyapp.api.ListStoryResponse
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.preferences.AppPreferences
import com.example.storyapp.ui.viewmodel.StoryViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var storyWithLocation = listOf<ListStoryResponse>()
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val appPreferences = AppPreferences(this)
        val token = appPreferences.authToken

        if (token != null) {
            storyViewModel.getStories(token)
        }
        storyViewModel.message.observe(this) {
            getLocationUser(storyViewModel.story)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val indonesia = LatLng(0.143136, 118.7371783)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(indonesia))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()

    }

    private fun getLocationUser(story: List<ListStoryResponse>) {
        if (story.isNotEmpty()) {
            for (stories in story) {
                if (stories.lat != null && stories.lon != null){
                    val position = LatLng(stories.lat, stories.lon)
                    storyWithLocation = storyWithLocation + stories
                    mMap.addMarker(
                        MarkerOptions().position(position).title(stories.name)
                    )
                }
            }
        }
        if(storyWithLocation.isNotEmpty()) {
            val latLng = LatLng(storyWithLocation[0].lat!!, storyWithLocation[0].lon!!)
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng, INITIAL_ZOOM
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }



    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }
    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        const val TAG = "MAP"
        const val DEFAULT_ZOOM = 15f
        const val INITIAL_ZOOM = 6f
    }
}