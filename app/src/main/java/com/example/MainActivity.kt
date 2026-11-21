package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.components.CityPickerSheet
import com.example.ui.navigation.AdhkarDetailRoute
import com.example.ui.navigation.AdhkarRoute
import com.example.ui.navigation.AlertsRoute
import com.example.ui.navigation.PrayersRoute
import com.example.ui.navigation.QiblaRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.screens.AdhkarDetailScreen
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.PrayersScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MawaqitTheme
import com.example.ui.viewmodel.MainViewModel

private data class BottomNavTab<T : Any>(
    val route: T,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val deepLinkCategory = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkCategory.value = intent?.getStringExtra(EXTRA_OPEN_ADHKAR_CATEGORY)

        setContent {
            val viewModel: MainViewModel = viewModel()
            val prefs by viewModel.preferences.collectAsStateWithLifecycle()

            MawaqitTheme(themeMode = prefs.themeMode) {
                MawaqitRootScreen(
                    viewModel = viewModel,
                    deepLinkAdhkarCategory = deepLinkCategory.value,
                    onConsumeDeepLink = { deepLinkCategory.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkCategory.value = intent.getStringExtra(EXTRA_OPEN_ADHKAR_CATEGORY)
    }

    companion object {
        const val EXTRA_OPEN_ADHKAR_CATEGORY = "extra_open_adhkar_category"
    }
}

@Composable
private fun MawaqitRootScreen(
    viewModel: MainViewModel,
    deepLinkAdhkarCategory: String?,
    onConsumeDeepLink: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val todaySchedule by viewModel.todaySchedule.collectAsStateWithLifecycle()
    val countdown by viewModel.countdownState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val citySearchResults by viewModel.citySearchResults.collectAsStateWithLifecycle()
    val permissionStatus by viewModel.permissionStatus.collectAsStateWithLifecycle()
    val compassState by viewModel.compassState.collectAsStateWithLifecycle()
    val adhkarCategories by viewModel.adhkarCategories.collectAsStateWithLifecycle()
    val allAdhkar by viewModel.allAdhkar.collectAsStateWithLifecycle()
    val adhkarPlayback by viewModel.adhkarPlaybackState.collectAsStateWithLifecycle()
    val isAdhanPlaying by viewModel.isAdhanPlaying.collectAsStateWithLifecycle()
    val playingMuezzinId by viewModel.playingMuezzinId.collectAsStateWithLifecycle()

    var showCityPicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectGpsLocation()
        } else {
            showCityPicker = true
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissionStatus()
    }

    // Request notification permission once gracefully on Android 13+
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(statusMessage) {
        val msg = statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearStatusMessage()
    }

    LaunchedEffect(deepLinkAdhkarCategory) {
        val cat = deepLinkAdhkarCategory ?: return@LaunchedEffect
        navController.navigate(AdhkarDetailRoute(cat))
        onConsumeDeepLink()
    }

    val tabs = remember {
        listOf(
            BottomNavTab(
                route = PrayersRoute,
                label = "المواقيت",
                selectedIcon = Icons.Filled.Mosque,
                unselectedIcon = Icons.Outlined.Mosque,
                testTag = "nav_tab_prayers"
            ),
            BottomNavTab(
                route = QiblaRoute,
                label = "القبلة",
                selectedIcon = Icons.Filled.Explore,
                unselectedIcon = Icons.Outlined.Explore,
                testTag = "nav_tab_qibla"
            ),
            BottomNavTab(
                route = AlertsRoute,
                label = "التنبيهات",
                selectedIcon = Icons.Filled.NotificationsActive,
                unselectedIcon = Icons.Outlined.Notifications,
                testTag = "nav_tab_alerts"
            ),
            BottomNavTab(
                route = AdhkarRoute,
                label = "الأذكار",
                selectedIcon = Icons.Filled.MenuBook,
                unselectedIcon = Icons.Outlined.MenuBook,
                testTag = "nav_tab_adhkar"
            )
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination
    val isTopLevel = currentDest?.let { dest ->
        dest.hasRoute<PrayersRoute>() ||
            dest.hasRoute<QiblaRoute>() ||
            dest.hasRoute<AlertsRoute>() ||
            dest.hasRoute<AdhkarRoute>()
    } ?: true

    val isExpandedWidth = LocalConfiguration.current.screenWidthDp >= 700

    val requestGpsOrAskPermission: () -> Unit = {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            viewModel.detectGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (isTopLevel && !isExpandedWidth) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentDest?.hasRoute(tab.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isTopLevel && isExpandedWidth) {
                NavigationRail {
                    tabs.forEach { tab ->
                        val selected = currentDest?.hasRoute(tab.route::class) == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = PrayersRoute
                ) {
                    composable<PrayersRoute> {
                        PrayersScreen(
                            schedule = todaySchedule,
                            countdown = countdown,
                            prefs = prefs,
                            qiblaBearingDeg = compassState.qiblaInfo.bearingDegrees,
                            isRefreshing = isRefreshing,
                            onOpenCityPicker = { showCityPicker = true },
                            onOpenSettings = { navController.navigate(SettingsRoute) },
                            onOpenQibla = {
                                navController.navigate(QiblaRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenDailyAdhkar = { catId ->
                                navController.navigate(AdhkarDetailRoute(catId))
                            },
                            onRefreshSync = { viewModel.refreshSchedulesFromApi() },
                            onTogglePrayerAlert = { type, enabled ->
                                viewModel.setPrayerAlertEnabled(type, enabled)
                            }
                        )
                    }

                    composable<QiblaRoute> {
                        QiblaScreen(
                            cityName = prefs.cityName,
                            compassState = compassState,
                            onStartCompass = { viewModel.startCompass() },
                            onStopCompass = { viewModel.stopCompass() }
                        )
                    }

                    composable<AlertsRoute> {
                        AlertsScreen(
                            prefs = prefs,
                            permissionStatus = permissionStatus,
                            isAdhanPlaying = isAdhanPlaying,
                            playingMuezzinId = playingMuezzinId,
                            onRefreshPermissions = { viewModel.refreshPermissionStatus() },
                            onTogglePrayerAlert = { type, enabled ->
                                viewModel.setPrayerAlertEnabled(type, enabled)
                            },
                            onSelectPreAdhanMinutes = { viewModel.setPreAdhanMinutes(it) },
                            onSelectMuezzin = { viewModel.selectMuezzin(it) },
                            onPreviewOrStopMuezzin = { viewModel.previewOrStopMuezzin(it) },
                            onUpdateMorningAdhkar = { enabled, h, m ->
                                viewModel.setMorningAdhkarReminder(enabled, h, m)
                            },
                            onUpdateEveningAdhkar = { enabled, h, m ->
                                viewModel.setEveningAdhkarReminder(enabled, h, m)
                            }
                        )
                    }

                    composable<AdhkarRoute> {
                        AdhkarScreen(
                            categories = adhkarCategories,
                            allItems = allAdhkar,
                            tasbihCount = prefs.tasbihCount,
                            tasbihPhraseIndex = prefs.tasbihPhraseIndex,
                            onOpenCategory = { catId ->
                                navController.navigate(AdhkarDetailRoute(catId))
                            },
                            onIncrementTasbih = { viewModel.incrementTasbih() },
                            onResetTasbih = { viewModel.resetTasbih() },
                            onSelectTasbihPhrase = { viewModel.selectTasbihPhrase(it) }
                        )
                    }

                    composable<AdhkarDetailRoute> { entry ->
                        val route = entry.toRoute<AdhkarDetailRoute>()
                        val categoryItems by viewModel.observeAdhkarCategory(route.categoryId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        AdhkarDetailScreen(
                            categoryId = route.categoryId,
                            items = categoryItems,
                            playbackState = adhkarPlayback,
                            onBack = { navController.popBackStack() },
                            onIncrement = { viewModel.incrementDhikr(it) },
                            onResetItem = { viewModel.resetDhikr(it) },
                            onResetCategory = { viewModel.resetAdhkarCategory(route.categoryId) },
                            onToggleFavorite = { viewModel.toggleDhikrFavorite(it) },
                            onPlayItem = { viewModel.playDhikr(it, categoryItems) },
                            onTogglePlayPause = { viewModel.toggleAdhkarPlayPause(categoryItems) },
                            onPlayNext = { viewModel.playNextDhikr() },
                            onPlayPrevious = { viewModel.playPreviousDhikr() },
                            onToggleAutoAdvance = { viewModel.toggleAdhkarAutoAdvance() },
                            onStopAudio = { viewModel.stopAdhkarAudio() }
                        )
                    }

                    composable<SettingsRoute> {
                        SettingsScreen(
                            prefs = prefs,
                            onBack = { navController.popBackStack() },
                            onOpenCityPicker = { showCityPicker = true },
                            onSelectMethod = { viewModel.setCalculationMethod(it) },
                            onToggleAutoMethodByCountry = { viewModel.setAutoMethodByCountry(it) },
                            onSelectMadhab = { viewModel.setMadhab(it) },
                            onAdjustManualOffset = { type, delta ->
                                viewModel.adjustManualOffset(type, delta)
                            },
                            onAdjustHijriDays = { viewModel.setHijriAdjustment(it) },
                            onTogglePreferApiSync = { viewModel.setPreferApiSync(it) },
                            onSelectThemeMode = { viewModel.setThemeMode(it) }
                        )
                    }
                }
            }
        }
    }

    if (showCityPicker) {
        CityPickerSheet(
            currentCityName = prefs.cityName,
            searchResults = citySearchResults,
            onDismiss = { showCityPicker = false },
            onRequestGps = requestGpsOrAskPermission,
            onSearchQueryChange = { viewModel.searchCity(it) },
            onSelectPresetCity = { viewModel.selectPresetCity(it) },
            onSelectResolvedLocation = { viewModel.selectResolvedLocation(it) }
        )
    }
}
