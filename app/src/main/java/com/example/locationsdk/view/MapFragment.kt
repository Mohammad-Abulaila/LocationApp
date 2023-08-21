package com.example.locationsdk.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.locationsdk.R
import com.example.locationsdk.viewModel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var typeBtn: ImageButton
    private val locationViewModel: LocationViewModel by activityViewModels()


    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.clear()
        var cameraUpdate: CameraUpdate

        val markerOptions = MarkerOptions()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            val latLng = LatLng(it.latitude, it.longitude)
            markerOptions.position(latLng)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15F)
            googleMap.moveCamera(cameraUpdate)

        } // 1-world 5-continents 10-city 15-street 20-building

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true

        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(GoogleMap.OnMyLocationButtonClickListener() {

            locationViewModel.gpsCheck(requireContext(), requireActivity())

            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                val latLng = LatLng(it.latitude, it.longitude)
                markerOptions.position(latLng)
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15F)
                googleMap.animateCamera(cameraUpdate)

            }
            return@OnMyLocationButtonClickListener true
        })
        googleMap.uiSettings.isCompassEnabled = true

        googleMap.setOnMapClickListener {
            markerOptions.position(it)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15F)
            markerOptions.title("pin")
            markerOptions.position(it)
            googleMap.addMarker(markerOptions)
            googleMap.animateCamera(cameraUpdate)
        }
        googleMap.setOnMarkerClickListener {
            it.remove()
            return@setOnMarkerClickListener true
        }
        googleMap.setOnMapLongClickListener {
            val circleOptions: CircleOptions =
                CircleOptions().center(it).radius(100.0).fillColor(R.color.blue50)
                    .strokeColor(R.color.blue300).visible(true).clickable(true)
            googleMap.addCircle(circleOptions)
        }
        googleMap.setOnCircleClickListener {
            it.remove()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.mapView)
        typeBtn = view.findViewById(R.id.typeBtn)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())



        mapView.onCreate(savedInstanceState)
        lifecycleScope.launch {
            locationViewModel.googleAvailable(requireContext()).collectLatest {
                if (it)
                    mapView.getMapAsync(this@MapFragment)
            }
        }

        typeBtn.setOnClickListener {
            val menu = PopupMenu(requireContext(), typeBtn)
            menu.menuInflater.inflate(R.menu.map_menu, menu.menu)

            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.none -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_NONE

                    }

                    R.id.normal -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

                    }

                    R.id.hybrid -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

                    }

                    R.id.terrain -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

                    }

                    R.id.satellite -> {
                        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

                    }

                }
                true
            }
            menu.show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()

    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

