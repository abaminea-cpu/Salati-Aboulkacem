package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.location.ResolvedLocation
import com.example.domain.model.CityCatalog
import com.example.domain.model.CityPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    currentCityName: String,
    searchResults: List<ResolvedLocation>,
    onDismiss: () -> Unit,
    onRequestGps: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectPresetCity: (CityPreset) -> Unit,
    onSelectResolvedLocation: (ResolvedLocation) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val filteredPresets = remember(query) {
        if (query.isBlank()) {
            CityCatalog.cities
        } else {
            CityCatalog.cities.filter {
                it.arabicName.contains(query, ignoreCase = true) ||
                    it.countryArabic.contains(query, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "تحديد الموقع والمدينة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onRequestGps()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("gps_detect_button")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تحديد موقعي تلقائياً عبر GPS")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onSearchQueryChange(it)
                },
                placeholder = { Text("ابحث عن مدينتك (مثلاً: الرياض، الجزائر، الدوحة)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (searchResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "نتائج البحث الجغرافي",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    items(searchResults) { loc ->
                        ListItem(
                            headlineContent = { Text(loc.cityName) },
                            supportingContent = {
                                Text(
                                    String.format(
                                        java.util.Locale.US,
                                        "%.2f° , %.2f°",
                                        loc.latitude,
                                        loc.longitude
                                    )
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                onSelectResolvedLocation(loc)
                                onDismiss()
                            }
                        )
                        HorizontalDivider()
                    }
                }

                item {
                    Text(
                        text = "المدن المعتمدة (تعمل دون إنترنت)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(filteredPresets, key = { it.id }) { city ->
                    val isSelected = city.arabicName == currentCityName
                    ListItem(
                        headlineContent = {
                            Text(
                                text = city.arabicName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        supportingContent = { Text(city.countryArabic) },
                        leadingContent = {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        modifier = Modifier
                            .clickable {
                                onSelectPresetCity(city)
                                onDismiss()
                            }
                            .testTag("city_item_${city.id}")
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}
