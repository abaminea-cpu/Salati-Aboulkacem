package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object PrayersRoute

@Serializable
object QiblaRoute

@Serializable
object AlertsRoute

@Serializable
object AdhkarRoute

@Serializable
data class AdhkarDetailRoute(val categoryId: String)

@Serializable
object SettingsRoute
