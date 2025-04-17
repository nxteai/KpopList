package com.example.kpoplist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign


// Define the KpopArtist Data Class
data class KpopArtist(
    val stageName: String,
    val fullName: String,
    val koreanName: String,
    val dateOfBirth: String,
    val country: String,
    val group: String,
    val gender: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content for the activity
        setContent {
            // Calling the KpopApp composable function here
            KpopApp()
        }
    }
}

@Composable
fun KpopApp() {
    val artists = remember { mutableStateListOf<KpopArtist>() }
    val context = LocalContext.current  // ✅ This gets the context properly

    // Load data when the screen is created
    LaunchedEffect(Unit) {
        val data = readCsvFile(context)
        artists.addAll(data)
    }

    Scaffold(
        content = {
            KpopArtistList(artists = artists)
        }
    )
}


@Composable
fun KpopArtistList(artists: List<KpopArtist>) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        items(artists) { artist ->
            KpopArtistItem(artist = artist)
        }
    }
}

@Composable
fun KpopArtistItem(artist: KpopArtist) {
    Column(
        modifier = Modifier
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally // 👈 center the column's content
    ) {
        Text("Stage Name: ${artist.stageName}", textAlign = TextAlign.Center)
        Text("Full Name: ${artist.fullName}", textAlign = TextAlign.Center)
        Text("Korean Name: ${artist.koreanName}", textAlign = TextAlign.Center)
        Text("Date of Birth: ${artist.dateOfBirth}", textAlign = TextAlign.Center)
        Text("Country: ${artist.country}", textAlign = TextAlign.Center)
        Text("Group: ${artist.group}", textAlign = TextAlign.Center)
        Text("Gender: ${artist.gender}", textAlign = TextAlign.Center)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}


fun readCsvFile(context: Context): List<KpopArtist> {
    val assetManager = context.assets
    val inputStream = assetManager.open("kpop_idol_database.csv")
    val reader = BufferedReader(InputStreamReader(inputStream))

    val artists = mutableListOf<KpopArtist>()
    reader.readLine()  // Skip header

    reader.forEachLine { line ->
        val data = line.split(",").filter { it.isNotBlank() }  // Ignore empty columns
        if (data.size >= 7) {  // Still ensure at least 7 fields
            val artist = KpopArtist(
                stageName = data[0],
                fullName = data[1],
                koreanName = data[2],
                dateOfBirth = data[3],
                country = data[4],
                group = data[5],
                gender = data[6]
            )
            println("Loaded artist: $artist")
            artists.add(artist)
        } else {
            println("Invalid row: $line")
        }
    }

    return artists
}
