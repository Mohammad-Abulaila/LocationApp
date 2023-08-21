package com.example.locationsdk.view


import android.annotation.SuppressLint

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locationsdk.R
import com.example.locationsdk.viewModel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var fineBtn: MaterialButton
    private lateinit var coarseBtn: MaterialButton
    private lateinit var backGroundBtn: MaterialButton
    private lateinit var currentBtn: MaterialButton
    private lateinit var mapBtn: MaterialButton
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var finePermission: Boolean = false
    private var coarsePermission: Boolean = false
    private var backPermission: Boolean = false
    private val locationViewModel: LocationViewModel by activityViewModels()
    private var num: Int = 0

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fineBtn = view.findViewById(R.id.Fine_location)
        coarseBtn = view.findViewById(R.id.Coarse_location)
        backGroundBtn = view.findViewById(R.id.Background_location)
        currentBtn = view.findViewById(R.id.Current_location)
        mapBtn = view.findViewById(R.id.Open_Map)
        lifecycleScope.launch {
            locationViewModel.getData.collect() {
                num = when (it) {
                    0 -> 0
                    1 -> 1
                    else -> 2
                }
            }
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        fineBtn.setOnClickListener {
            finePermission()
        }
        coarseBtn.setOnClickListener {
            coarsePermission()
        }
        backGroundBtn.setOnClickListener {
            backPermission()
        }
        currentBtn.setOnClickListener {

            finePermission()
            if (finePermission) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Location:${it.latitude},${it.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Precise location permission needed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        mapBtn.setOnClickListener {
            finePermission()
            if (finePermission) {
                val action = MainFragmentDirections.actionMainFragmentToMapFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun finePermission() {

        viewLifecycleOwner.lifecycleScope.launch {
            locationViewModel.permissionCheck(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                num,
                requireContext(),
                requireActivity()
            ).collectLatest { finePermission = it }
            if (!finePermission) num = num!! + 1
        }
    }

    private fun coarsePermission() {

        viewLifecycleOwner.lifecycleScope.launch {
            locationViewModel.permissionCheck(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                num,
                requireContext(),
                requireActivity()
            ).collectLatest {
                coarsePermission = it
            }
            if (!coarsePermission) num = num!! + 1
        }

    }

    private fun backPermission() {

        viewLifecycleOwner.lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                locationViewModel.permissionCheck(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    num,
                    requireContext(),
                    requireActivity()
                ).collectLatest {
                    backPermission = it
                }
            }
            if (!backPermission) num = num!! + 1
        }
    }
}
