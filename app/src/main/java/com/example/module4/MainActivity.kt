package com.example.module4

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.module4.ui.theme.Module4Theme
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box() {
                Task10Screen()
            }
        }
    }
}
@Composable
fun Task10Screen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var addressText by remember { mutableStateOf("") }
    var coordinatesText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            getLocation(fusedLocationClient, onStart = { isLoading = true }, onSuccess = { lat, lng ->
                coordinatesText = "Lat: %.6f, Lng: %.6f".format(lat, lng)
                reverseGeocode(context, lat, lng, onAddress = { address ->
                    addressText = address
                    isLoading = false
                }, onError = { error ->
                    errorMessage = error
                    isLoading = false
                })
            }, onError = { error ->
                errorMessage = error
                isLoading = false
            })
        } else {
            errorMessage = "Разрешение на местоположение не предоставлено"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Моё местоположение",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        if (coordinatesText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Координаты",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = coordinatesText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (addressText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Адрес",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = addressText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Button(
            onClick = {
                addressText = ""
                coordinatesText = ""
                errorMessage = null
                isLoading = true

                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Получить мой адрес")
        }
    }
}

private fun getLocation(
    client: FusedLocationProviderClient,
    onStart: () -> Unit,
    onSuccess: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    onStart()
    val cancellationTokenSource = CancellationTokenSource()
    val request = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setDurationMillis(10000) // ждём до 10 секунд
        .build()

    client.getCurrentLocation(request, cancellationTokenSource.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(location.latitude, location.longitude)
            } else {
                onError("Не удалось получить местоположение (location == null)")
            }
        }
        .addOnFailureListener { exception ->
            onError("Ошибка получения локации: ${exception.message}")
        }
}
private fun reverseGeocode(
    context: Context,
    lat: Double,
    lng: Double,
    onAddress: (String) -> Unit,
    onError: (String) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val fullAddress = arrayOf(
                        address.thoroughfare,
                        address.subThoroughfare,
                        address.locality,
                        address.adminArea,
                        address.countryName
                    ).filterNotNull().joinToString(", ")
                    onAddress(if (fullAddress.isNotBlank()) fullAddress else "Адрес не найден")
                } else {
                    onAddress("Адрес не найден")
                }
            }

            override fun onError(errorMessage: String?) {
                onError("Ошибка геокодирования: ${errorMessage ?: "неизвестная ошибка"}")
            }
        })
    } else {
        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val fullAddress = arrayOf(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.countryName
                ).filterNotNull().joinToString(", ")
                onAddress(if (fullAddress.isNotBlank()) fullAddress else "Адрес не найден")
            } else {
                onAddress("Адрес не найден")
            }
        } catch (e: Exception) {
            onError("Ошибка геокодирования: ${e.message}")
        }
    }
}