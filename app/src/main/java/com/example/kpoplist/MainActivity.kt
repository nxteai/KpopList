@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.kpoplist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

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
        setContent {
            KpopApp()
        }
    }
}

@Composable
fun KpopApp() {
    val context = LocalContext.current
    val allArtists = remember { mutableStateListOf<KpopArtist>() }
    val searchQuery = remember { mutableStateOf("") }
    val groupFilter = remember { mutableStateOf("") }
    val sortOption = remember { mutableStateOf("Name") }

    // Load CSV data
    LaunchedEffect(Unit) {
        val data = readCsvFile(context)
        allArtists.addAll(data)
    }

    // Unique groups for filter tabs
    val groups = allArtists.map { it.group }.distinct().sorted()

    // Filter and sort logic
    val filteredArtists = allArtists.filter { artist ->
        val matchSearch = artist.stageName.contains(searchQuery.value, true) || artist.group.contains(searchQuery.value, true)
        val matchGroup = groupFilter.value.isEmpty() || artist.group == groupFilter.value
        matchSearch && matchGroup
    }.sortedWith(
        when (sortOption.value) {
            "Age" -> compareByDescending { parseDate(it.dateOfBirth) }
            "Group" -> compareBy { it.group }
            else -> compareBy { it.stageName }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K-pop Idol Finder", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFFAE6FA)
                )
            )
        },
        containerColor = Color(0xFFFDF4FF)
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            // Search
            TextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                label = { Text("Search by Stage Name or Group") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort by:")
                DropdownMenuBox(
                    options = listOf("Name", "Age", "Group"),
                    selected = sortOption.value,
                    onSelected = { sortOption.value = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group filter tabs
            val selectedTabIndex = when {
                groupFilter.value.isEmpty() -> 0 // "All" tab is selected
                else -> groups.indexOf(groupFilter.value) + 1 // Group tab is selected, +1 to account for "All" tab
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp
            ) {
                // "All" tab for no group filter
                Tab(
                    selected = groupFilter.value.isEmpty(),
                    onClick = { groupFilter.value = "" },
                    text = { Text("All") }
                )
                // Tabs for each group
                groups.forEach { group ->
                    Tab(
                        selected = groupFilter.value == group,
                        onClick = { groupFilter.value = group },
                        text = { Text(group) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredArtists.isEmpty()) {
                Text(
                    "No results found",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn {
                    items(filteredArtists) { artist ->
                        KpopArtistCard(artist)
                    }
                }
            }
        }
    }
}






@SuppressLint("NewApi")
@Composable
fun KpopArtistCard(artist: KpopArtist) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎤 ${artist.stageName}", fontWeight = FontWeight.Bold)
            Text("Full Name: ${artist.fullName}")
            Text("Korean Name: ${artist.koreanName}")
            Text("DOB: ${artist.dateOfBirth} (${getAge(artist.dateOfBirth)} yrs)")
            Text("🌍 ${artist.country}")
            Text("Group: ${artist.group}")
            Text("Gender: ${if (artist.gender == "F") "👩‍🎤 Female" else "👨‍🎤 Male"}")
        }
    }
}

@Composable
fun DropdownMenuBox(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun readCsvFile(context: Context): List<KpopArtist> {
    val assetManager = context.assets
    val inputStream = assetManager.open("kpop_idol_database.csv")
    val reader = BufferedReader(InputStreamReader(inputStream))

    val artists = mutableListOf<KpopArtist>()
    reader.readLine()  // Skip header

    reader.forEachLine { line ->
        val data = line.split(",").map { it.trim() }  // Remove extra spaces
        if (data.size >= 7) {
            val group = data[5].ifBlank { "Solo" }

            val artist = KpopArtist(
                stageName = data[0],
                fullName = data[1],
                koreanName = data[2],
                dateOfBirth = data[3],
                country = data[4],
                group = group,
                gender = data[6]
            )
            artists.add(artist)
        } else {
            println("Invalid row: $line")
        }
    }

    return artists
}


// Utility functions
@SuppressLint("NewApi")
fun parseDate(date: String): LocalDate {
    return try {
        LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
    } catch (e: Exception) {
        LocalDate.of(2000, 1, 1)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getAge(dob: String): Long {
    val birthDate = parseDate(dob)
    return ChronoUnit.YEARS.between(birthDate, LocalDate.now())
}
