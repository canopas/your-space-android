package com.canopas.yourspace.ui.flow.journey.detail

import android.location.Address
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.canopas.yourspace.R
import com.canopas.yourspace.data.models.location.LocationJourney
import com.canopas.yourspace.domain.utils.getPlaceAddress
import com.canopas.yourspace.ui.flow.journey.components.DottedTimeline
import com.canopas.yourspace.ui.flow.journey.components.JourneyMap
import com.canopas.yourspace.ui.flow.journey.components.PlaceInfo
import com.canopas.yourspace.ui.flow.journey.components.formattedTitle
import com.canopas.yourspace.ui.flow.journey.components.getDistanceString
import com.canopas.yourspace.ui.flow.journey.components.getFormattedLocationTime
import com.canopas.yourspace.ui.theme.AppTheme
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun UserJourneyDetailScreen() {
    val viewModel = hiltViewModel<UserJourneyDetailViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = Unit) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = state.selectedTimeFrom
            set(Calendar.HOUR_OF_DAY, 0)
        }
        val startTimeStamp = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        viewModel.onDateSelected(calendar.timeInMillis, startTimeStamp)
    }

    Scaffold(
        topBar = { JourneyTopBar(state.journey, viewModel::navigateBack) }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            MapView(state.journey)
            FooterContent(state.isLoading, state.journey)
        }
    }
}

@Composable
private fun FooterContent(loading: Boolean, journey: LocationJourney?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        journey?.let { location ->
            JourneyInfo(location)
        }
        if (loading) {
            CircularProgressIndicator(color = AppTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun JourneyInfo(journey: LocationJourney) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        val context = LocalContext.current
        var fromAddress by remember { mutableStateOf<Address?>(null) }
        var toAddress by remember { mutableStateOf<Address?>(null) }

        var fromAddressStr by remember { mutableStateOf("") }
        var toAddressStr by remember { mutableStateOf("") }

        LaunchedEffect(journey) {
            withContext(Dispatchers.IO) {
                val latLng =
                    LatLng(journey.from_latitude, journey.from_longitude)
                val toLatLng =
                    LatLng(journey.to_latitude ?: 0.0, journey.to_longitude ?: 0.0)
                fromAddress = latLng.getPlaceAddress(context)
                toAddress = toLatLng.getPlaceAddress(context)
                fromAddressStr = fromAddress?.getAddressLine(0) ?: ""
                toAddressStr = toAddress?.getAddressLine(0) ?: ""
            }
        }

        val distance = getDistanceString(journey.route_distance ?: 0.0)
        val duration = getRouteDurationString(journey.route_duration ?: 0)

        Text(
            text = "$distance - $duration",
            color = AppTheme.colorScheme.textPrimary,
            style = AppTheme.appTypography.header2,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 24.dp)
                .padding(horizontal = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .height(90.dp)
        ) {
            DottedTimeline(isSteadyLocation = true, isLastItem = false)
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                PlaceInfo(fromAddressStr, getFormattedLocationTime(journey.created_at!!))
            }
        }

        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .height(90.dp)
        ) {
            DottedTimeline(isSteadyLocation = false, isLastItem = true)
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                PlaceInfo(toAddressStr, getFormattedLocationTime(journey.update_at!!))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JourneyTopBar(journey: LocationJourney?, navigateBack: () -> Unit) {
    var fromAddress by remember { mutableStateOf<Address?>(null) }
    var toAddress by remember { mutableStateOf<Address?>(null) }
    val context = LocalContext.current

    LaunchedEffect(journey) {
        if (journey == null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val latLng =
                LatLng(journey.from_latitude, journey.from_longitude)
            val toLatLng = LatLng(journey.to_latitude ?: 0.0, journey.to_longitude ?: 0.0)
            fromAddress = latLng.getPlaceAddress(context)
            toAddress = toLatLng.getPlaceAddress(context)
        }
    }

    val title = fromAddress?.formattedTitle(toAddress) ?: ""

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colorScheme.surface),
        title = {
            Text(
                text = title,
                style = AppTheme.appTypography.header3
            )
        },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = ""
                )
            }
        }
    )
}

@Composable
private fun ColumnScope.MapView(journey: LocationJourney?) {
    JourneyMap(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        location = journey,
        true,
        fromMarkerContent = {
            JourneyMarker(bgColor = AppTheme.colorScheme.primary) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tab_places_filled),
                    contentDescription = null,
                    tint = AppTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        toMarkerContent = {
            JourneyMarker(
                AppTheme.colorScheme.successColor
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_journey_destination),
                    contentDescription = null,
                    tint = AppTheme.colorScheme.successColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        polyLineWidth = 8f,
        anchor = Offset(0.5f, 0.5f)
    )
}

@Composable
private fun JourneyMarker(bgColor: Color, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                bgColor.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .border(0.5.dp, bgColor.copy(alpha = 0.8f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    AppTheme.colorScheme.surface,
                    shape = CircleShape
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

internal fun getRouteDurationString(
    routeDuration: Long
): String {
    val hours = TimeUnit.MILLISECONDS.toHours(routeDuration)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(routeDuration) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(routeDuration) % 60
    return when {
        hours > 0 -> {
            "$hours hr $minutes mins"
        }

        minutes > 0 -> {
            "$minutes mins"
        }

        else -> {
            "$seconds sec"
        }
    }
}
