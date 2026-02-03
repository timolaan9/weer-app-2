package com.example.weerapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.weerapp.network.KnmiItem
import com.example.weerapp.network.RetrofitClient
import com.example.weerapp.network.fetchKnmiEarthquakes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

enum class Screen { WEATHER, KNMI, USGS }

@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.WEATHER) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { screen = Screen.WEATHER }) { Text("Weer") }
            Button(onClick = { screen = Screen.KNMI }) { Text("KNMI") }
            Button(onClick = { screen = Screen.USGS }) { Text("USGS") }
        }

        when (screen) {
            Screen.WEATHER -> WeatherScreen()
            Screen.KNMI -> KnmiScreen()
            Screen.USGS -> UsgsScreen()
        }
    }
}

@Composable
fun WeatherScreen() {
    val context = LocalContext.current
    val apiKey = "5093528487900a646de20fe75184a953"

    var text by remember { mutableStateOf("Weer laden...") }
    var shareLines by remember { mutableStateOf(listOf("Weerinfo")) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) photoBitmap = bitmap
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val drawable = android.graphics.drawable.Drawable.createFromStream(input, uri.toString())
                photoBitmap = drawable?.toBitmap()
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val result = RetrofitClient.openWeatherApi.getWeather("Amsterdam", apiKey)
            val desc = result.weather.firstOrNull()?.description ?: "-"

            text = """
                Plaats: ${result.name}
                Temperatuur: ${result.main.temp} °C
                Weer: $desc
                Wind: ${result.wind.speed} m/s
                Luchtvochtigheid: ${result.main.humidity}%
            """.trimIndent()

            shareLines = listOf(
                result.name,
                "${result.main.temp} °C, $desc",
                "Wind ${result.wind.speed} m/s",
                "Luchtvochtigheid ${result.main.humidity}%"
            )
        } catch (e: Exception) {
            text = "Fout bij laden: ${e.message}"
            shareLines = listOf("Weerinfo niet beschikbaar")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { cameraLauncher.launch() }
            ) { Text("Maak foto") }

            Button(
                modifier = Modifier.weight(1f),
                onClick = { galleryLauncher.launch("image/*") }
            ) { Text("Kies foto") }
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            val base = photoBitmap ?: Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888).apply {
                eraseColor(android.graphics.Color.DKGRAY)
            }

            val finalBitmap = addWeatherOverlay(base, shareLines)
            val uri = saveBitmapToCacheAndGetUri(context, finalBitmap)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Deel via"))
        }) {
            Text("Deel afbeelding met weerinfo")
        }
    }
}

@Composable
fun UsgsScreen() {
    var text by remember { mutableStateOf("USGS laden...") }

    LaunchedEffect(Unit) {
        try {
            val items = RetrofitClient.usgsApi.allDay().features.take(20)
            text = items.joinToString("") {
                val p = it.properties
                "M ${p.mag ?: "-"} - ${p.place ?: "-"}"
            }
        } catch (e: Exception) {
            text = "Fout bij laden: ${e.message}"
        }
    }

    Text(text, modifier = Modifier.padding(16.dp))
}

@Composable
fun KnmiScreen() {
    var itemsList by remember { mutableStateOf<List<KnmiItem>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            itemsList = fetchKnmiEarthquakes(RetrofitClient.clientForRawCalls())
        } catch (e: Exception) {
            error = e.message
        }
    }

    if (error != null) {
        Text("Fout bij laden: $error", modifier = Modifier.padding(16.dp))
        return
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(itemsList) { item ->
            Text(item.title)
            Text(item.date, modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}
