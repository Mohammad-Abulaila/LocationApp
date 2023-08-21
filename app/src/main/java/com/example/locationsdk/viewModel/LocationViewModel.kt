package com.example.locationsdk.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationsdk.Model.Permissions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var locationRequest: LocationRequest

    private val data = Permissions(application)

    val getData = data.getNum()

    fun permissionCheck(type: String, num: Int, context: Context, activity: Activity) =
        flow<Boolean> {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    type
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "you have the permission", Toast.LENGTH_SHORT).show()
                emit(true)
            } else if (num in 0..1) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(type),
                    1
                )

                emit(false)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts(
                    "package", context.packageName,
                    context.toString()
                )
                intent.data = uri
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                emit(false)
            }
            data.incFine(num + 1)
        }

    fun gpsCheck(context: Context, activity: Activity) {
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(100)
                .build()

        val locationSettingsRequest: LocationSettingsRequest.Builder =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)


        val task: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(context)
                .checkLocationSettings(locationSettingsRequest.build())

        task.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse>() {
            try {
                val response: LocationSettingsResponse =
                    it.getResult(ApiException::class.java)
                Toast.makeText(context, "Gps is already enable", Toast.LENGTH_SHORT)
            } catch (exception: ApiException) {
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    val resolvableApiException: ResolvableApiException =
                        exception as ResolvableApiException
                    try {
                        resolvableApiException.startResolutionForResult(activity, 8)
                    } catch (Exception: IntentSender.SendIntentException) {
                        Exception.printStackTrace()
                    }
                }
                if (exception.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(context, "Setting not available", Toast.LENGTH_SHORT)
                }
            }
        })
    }

    fun googleAvailable(context: Context) = flow<Boolean> {
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(context)
        if (result == ConnectionResult.SUCCESS) {
            Toast.makeText(context, "Google is available", Toast.LENGTH_SHORT).show()
            emit(true)
        } else {
            Toast.makeText(context, "Google isn't available $result", Toast.LENGTH_SHORT)
                .show()
            emit(false)
        }
    }
}


//    var click = 0
//    fun Inc() {
//        click++
//    }


//    fun permissionCheck(type: String, context: Context, activity: Activity) =
//        flow<Boolean> {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    type
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                emit(true)
//            } else if (click <= 1) {
//                ActivityCompat.requestPermissions(
//                    activity,
//                    arrayOf(type),
//                    1
//                )
//                emit(false)
//            } else {
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                val uri = Uri.fromParts(
//                    "package", context.packageName,
//                    context.toString()
//                )
//                intent.data = uri
//                intent.addCategory(Intent.CATEGORY_DEFAULT)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                context.startActivity(intent)
//                emit(false)
//            }
//        }