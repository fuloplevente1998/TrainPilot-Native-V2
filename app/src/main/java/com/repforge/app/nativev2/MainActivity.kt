package com.repforge.app.nativev2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.domain.TopMainTab
import com.repforge.app.nativev2.domain.TopMinorDestination
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import com.repforge.app.nativev2.ui.navigation.TopFivePlusThreeHeader
import com.repforge.app.nativev2.ui.screens.ActiveWorkoutPanel
import com.repforge.app.nativev2.ui.screens.CalendarScreen
import com.repforge.app.nativev2.ui.screens.CoachScreen
import com.repforge.app.nativev2.ui.screens.ExerciseLibraryScreen
import com.repforge.app.nativev2.ui.screens.HealthScreen
import com.repforge.app.nativev2.ui.screens.HomeScreen
import com.repforge.app.nativev2.ui.screens.JournalScreen
import com.repforge.app.nativev2.ui.screens.ProgressScreen
import com.repforge.app.nativev2.ui.screens.ProgramsScreen
import com.repforge.app.nativev2.ui.screens.SettingsScreen
import com.repforge.app.nativev2.ui.screens.TrainingLandingScreen
import com.repforge.app.nativev2.ui.screens.WorkoutDashboard
import com.repforge.app.nativev2.ui.screens.WorkoutRouteMapModal
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens
import com.repforge.app.nativev2.ui.theme.TrainPilotTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TrainPilotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainPilotAppHost(viewModel = viewModel)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun TrainPilotAppHost(
    viewModel: TrainPilotViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Sync actual OS location permission state on composition
    DisposableEffect(context) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            viewModel.updateLocationPermissionStatus(granted = true, systemEnabled = true)
        }
        onDispose { }
    }

    // Real Android LocationManager listener when outdoor GPS workout is active and permitted
    DisposableEffect(uiState.activeWorkout?.workoutId, uiState.activeWorkout?.gpsIntentEnabled, uiState.hasLocationPermission) {
        val active = uiState.activeWorkout
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        var listener: LocationListener? = null
        if (active != null && active.gpsIntentEnabled && uiState.hasLocationPermission && locationManager != null) {
            val locListener = LocationListener { location ->
                viewModel.onLiveLocationSample(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = location.accuracy
                )
            }
            listener = locListener
            runCatching {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        2f,
                        locListener
                    )
                }
            }
        }
        onDispose {
            if (listener != null && locationManager != null) {
                runCatching { locationManager.removeUpdates(listener) }
            }
        }
    }

    // BackHandler for secondary and minor destinations (returns to activeMainTab without losing state)
    BackHandler(enabled = uiState.activeMinorDestination != null || uiState.activeSubDestination != null) {
        viewModel.navigateBackToMainTab()
    }

    TrainPilotTheme(themeKey = uiState.themeKey) {
        val tokens = LocalTrainPilotTokens.current

        // Strictly NO bottomBar: Only Top 5+3 Navigation Header
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.bg),
            containerColor = tokens.bg,
            topBar = {
                TopFivePlusThreeHeader(
                    language = uiState.language,
                    activeMainTab = uiState.activeMainTab,
                    activeMinorDestination = uiState.activeMinorDestination,
                    activeSubDestination = uiState.activeSubDestination,
                    activeWorkout = uiState.activeWorkout,
                    statusBannerMessage = uiState.statusBannerMessage,
                    onSelectMainTab = { tab -> viewModel.selectMainTab(tab) },
                    onSelectMinorDestination = { minor -> viewModel.selectMinorDestination(minor) },
                    onReturnToMainTab = { viewModel.navigateBackToMainTab() },
                    onDismissBanner = { viewModel.clearStatusBanner() }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(tokens.bg)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val sub = uiState.activeSubDestination
                    val minor = uiState.activeMinorDestination
                    when {
                        sub != null -> {
                            when (sub) {
                                SubDestination.PROGRESS -> ProgressScreen(uiState = uiState, viewModel = viewModel)
                                SubDestination.EXERCISE_LIBRARY -> ExerciseLibraryScreen(uiState = uiState, viewModel = viewModel)
                            }
                        }
                        minor != null -> {
                            when (minor) {
                                TopMinorDestination.SETTINGS -> SettingsScreen(uiState = uiState, viewModel = viewModel)
                                TopMinorDestination.CALENDAR -> CalendarScreen(uiState = uiState, viewModel = viewModel)
                                TopMinorDestination.COACH -> CoachScreen(uiState = uiState, viewModel = viewModel)
                            }
                        }
                        else -> {
                            when (uiState.activeMainTab) {
                                TopMainTab.HOME -> HomeScreen(uiState = uiState, viewModel = viewModel)
                                TopMainTab.TRAINING -> {
                                    val activeDraft = uiState.activeWorkout
                                    if (activeDraft != null) {
                                        ActiveWorkoutPanel(
                                            uiState = uiState,
                                            draft = activeDraft,
                                            viewModel = viewModel,
                                            onRequestStartPlatformGps = { /* Handled by DisposableEffect */ }
                                        )
                                    } else {
                                        WorkoutDashboard(uiState = uiState, viewModel = viewModel)
                                    }
                                }
                                TopMainTab.JOURNAL -> JournalScreen(uiState = uiState, viewModel = viewModel)
                                TopMainTab.HEALTH -> HealthScreen(uiState = uiState, viewModel = viewModel)
                                TopMainTab.PROGRAMS -> ProgramsScreen(uiState = uiState, viewModel = viewModel)
                            }
                        }
                    }
                }
            }

            // V15: Full-screen private Route Map Modal (closing via red X NEVER finishes or stops measurement)
            val routeModalId = uiState.routeMapModalWorkoutId
            if (routeModalId != null) {
                val activeMatch = uiState.activeWorkout?.takeIf { it.workoutId == routeModalId }
                val completedMatch = uiState.workoutHistory.firstOrNull { it.id == routeModalId }
                val title = activeMatch?.title ?: completedMatch?.title ?: "Edzés útvonal"
                WorkoutRouteMapModal(
                    workoutTitle = title,
                    routePoints = uiState.routeMapPoints,
                    onCloseOnly = { viewModel.closePrivateRouteMapModal() }
                )
            }
        }
    }
}
