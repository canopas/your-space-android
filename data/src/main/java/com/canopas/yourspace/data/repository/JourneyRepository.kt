package com.canopas.yourspace.data.repository

import android.location.Location
import com.canopas.yourspace.data.models.location.ApiLocation
import com.canopas.yourspace.data.models.location.LocationJourney
import com.canopas.yourspace.data.models.location.LocationTable
import com.canopas.yourspace.data.models.location.UserState
import com.canopas.yourspace.data.models.location.isSteadyLocation
import com.canopas.yourspace.data.models.location.toApiLocation
import com.canopas.yourspace.data.models.location.toLocation
import com.canopas.yourspace.data.models.location.toLocationFromMovingJourney
import com.canopas.yourspace.data.models.location.toLocationFromSteadyJourney
import com.canopas.yourspace.data.models.location.toRoute
import com.canopas.yourspace.data.service.location.ApiJourneyService
import com.canopas.yourspace.data.service.location.ApiLocationService
import com.canopas.yourspace.data.storage.room.LocationTableDatabase
import com.canopas.yourspace.data.utils.LocationConverters
import kotlinx.coroutines.flow.toList
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

const val MIN_DISTANCE = 100.0
const val MIN_TIME_DIFFERENCE = 5 * 60 * 1000

@Singleton
class JourneyRepository @Inject constructor(
    private val locationTableDatabase: LocationTableDatabase,
    private val journeyService: ApiJourneyService,
    private val locationService: ApiLocationService,
    private val converters: LocationConverters
) {

    suspend fun saveLocationJourney(
        userState: Int,
        extractedLocation: Location,
        userId: String
    ) {
        try {
            val locationData = getLocationData(userId)
            val lastJourney = getLastJourneyLocation(userId, locationData)

            when {
                lastJourney == null -> {
                    journeyService.saveCurrentJourney(
                        userId = userId,
                        fromLatitude = extractedLocation.latitude,
                        fromLongitude = extractedLocation.longitude
                    )
                }

                userState == UserState.STEADY.value -> {
                    Timber.tag("LAT_LONG").d("saveLocationJourney: Steady User")
                    saveJourneyForSteadyUser(
                        currentUserId = userId,
                        extractedLocation = extractedLocation,
                        lastKnownJourney = lastJourney
                    )
                }

                userState == UserState.MOVING.value -> {
                    Timber.tag("LAT_LONG").d("saveLocationJourney: Moving User")
                    saveJourneyForMovingUser(
                        currentUserId = userId,
                        extractedLocation = extractedLocation,
                        lastKnownJourney = lastJourney
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while saving location journey")
        }
    }

    private fun getLocationData(
        userid: String
    ): LocationTable? {
        return locationTableDatabase.locationTableDao().getLocationData(userid)
    }

    private suspend fun getLastJourneyLocation(
        userId: String,
        locationData: LocationTable?
    ): LocationJourney? {
        return locationData?.lastLocationJourney?.let {
            return converters.journeyFromString(it)
        } ?: run {
            val lastJourneyLocation = journeyService.getLastJourneyLocation(userId)
            locationData?.copy(lastLocationJourney = converters.journeyToString(lastJourneyLocation))
                ?.let {
                    locationTableDatabase.locationTableDao().updateLocationTable(it)
                }
            return lastJourneyLocation
        }
    }

    /**
     * Get last five minute locations from local database ~ 1 location per minute
     * */
    private fun getLastFiveMinuteLocations(locationData: LocationTable?): List<ApiLocation> {
        return locationData?.lastFiveMinutesLocations?.let {
            converters.locationListFromString(it)
        } ?: emptyList()
    }

    private suspend fun checkAndUpdateLastFiveMinLocations(
        userId: String,
        locationData: LocationTable,
        extractedLocation: Location
    ) {
        val locations =
            locationData.lastFiveMinutesLocations?.let { converters.locationListFromString(it) }
        if (locations.isNullOrEmpty()) {
            val lastFiveMinLocations =
                locationService.getLastFiveMinuteLocations(userId).toList().flatten()
            updateLocationData(locationData, lastFiveMinLocations)
        } else {
            val latest = locations.maxByOrNull { it.created_at!! }
            if (latest == null || latest.created_at!! < System.currentTimeMillis() - 60000) {
                val updated = locations.toMutableList()
                updated.removeAll { extractedLocation.time - it.created_at!! > MIN_TIME_DIFFERENCE }
                updated.add(extractedLocation.toApiLocation(userId))
                updateLocationData(locationData, updated)
            }
        }
    }

    private suspend fun updateLocationData(
        locationData: LocationTable,
        updatedLocations: List<ApiLocation>
    ) {
        val updatedData = locationData.copy(
            lastFiveMinutesLocations = converters.locationListToString(updatedLocations)
        )
        locationTableDatabase.locationTableDao().updateLocationTable(updatedData)
    }

    suspend fun getUserState(userId: String, extractedLocation: Location): Int {
        var locationData =
            locationTableDatabase.locationTableDao().getLocationData(userId)

        locationData?.let { checkAndUpdateLastFiveMinLocations(userId, it, extractedLocation) }

        locationData =
            locationTableDatabase.locationTableDao().getLocationData(userId)

        val userState = getUserState(locationData, extractedLocation)

        return userState
    }

    private fun getUserState(
        locationData: LocationTable?,
        extractedLocation: Location
    ): Int {
        val lastFiveMinuteLocations = getLastFiveMinuteLocations(locationData)
        if (lastFiveMinuteLocations.isMoving(extractedLocation)) return UserState.MOVING.value
        return UserState.STEADY.value
    }

    /**
     * Save journey for moving user
     * */
    private suspend fun saveJourneyForMovingUser(
        currentUserId: String,
        extractedLocation: Location,
        lastKnownJourney: LocationJourney
    ) {
        if (lastKnownJourney.isSteadyLocation()) {
            val updatedRoutes = lastKnownJourney.routes.toMutableList()
            updatedRoutes.add(extractedLocation.toRoute())
            journeyService.saveCurrentJourney(
                userId = currentUserId,
                fromLatitude = lastKnownJourney.from_latitude,
                fromLongitude = lastKnownJourney.from_longitude,
                toLatitude = extractedLocation.latitude,
                toLongitude = extractedLocation.longitude,
                routeDistance = distanceBetween(
                    extractedLocation,
                    lastKnownJourney.toLocationFromSteadyJourney()
                ).toDouble(),
                routeDuration = extractedLocation.time - lastKnownJourney.update_at!!
            )
        } else {
            val distance = lastKnownJourney.toLocationFromMovingJourney().distanceTo(
                extractedLocation
            ).toDouble()
            val updatedRoutes = lastKnownJourney.routes.toMutableList()
            updatedRoutes.add(extractedLocation.toRoute())
            journeyService.updateLastLocationJourney(
                userId = currentUserId,
                lastKnownJourney.copy(
                    to_latitude = extractedLocation.latitude,
                    to_longitude = extractedLocation.longitude,
                    route_distance = (lastKnownJourney.route_distance ?: 0.0) + distance,
                    route_duration = extractedLocation.time - lastKnownJourney.created_at!!,
                    routes = updatedRoutes,
                    update_at = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Save journey as steady when user state is steady
     * */
    private suspend fun saveJourneyForSteadyUser(
        currentUserId: String,
        extractedLocation: Location,
        lastKnownJourney: LocationJourney
    ) {
        val lastLatLong =
            if (lastKnownJourney.isSteadyLocation()) {
                lastKnownJourney.toLocationFromSteadyJourney()
            } else {
                lastKnownJourney.toLocationFromMovingJourney()
            }

        val distance = distanceBetween(extractedLocation, lastLatLong)
        val timeDifference = extractedLocation.time - lastKnownJourney.created_at!!

        when {
            timeDifference > MIN_TIME_DIFFERENCE && distance > MIN_DISTANCE -> {
                if (lastKnownJourney.isSteadyLocation()) {
                    journeyService.updateLastLocationJourney(
                        userId = currentUserId,
                        lastKnownJourney.copy(update_at = System.currentTimeMillis())
                    )
                } else {
                    journeyService.saveCurrentJourney(
                        currentUserId,
                        fromLatitude = lastKnownJourney.to_latitude!!,
                        fromLongitude = lastKnownJourney.to_longitude!!,
                        createdAt = lastKnownJourney.update_at
                    )
                }

                journeyService.saveCurrentJourney(
                    userId = currentUserId,
                    fromLatitude = lastKnownJourney.to_latitude ?: lastKnownJourney.from_latitude,
                    fromLongitude = lastKnownJourney.to_longitude
                        ?: lastKnownJourney.from_longitude,
                    toLatitude = extractedLocation.latitude,
                    toLongitude = extractedLocation.longitude,
                    routeDistance = distance.toDouble(),
                    routeDuration = extractedLocation.time - lastKnownJourney.update_at!!,
                    createdAt = lastKnownJourney.update_at,
                    updateAt = System.currentTimeMillis()
                )
            }

            timeDifference < MIN_TIME_DIFFERENCE && distance > MIN_DISTANCE -> {
                val updatedRoutes = lastKnownJourney.routes.toMutableList()
                updatedRoutes.add(extractedLocation.toRoute())
                journeyService.updateLastLocationJourney(
                    userId = currentUserId,
                    journey = lastKnownJourney.copy(
                        to_longitude = extractedLocation.longitude,
                        to_latitude = extractedLocation.latitude,
                        route_distance = lastKnownJourney.toLocationFromSteadyJourney().distanceTo(
                            extractedLocation
                        ).toDouble(),
                        routes = updatedRoutes,
                        route_duration = extractedLocation.time - lastKnownJourney.created_at,
                        update_at = System.currentTimeMillis()
                    )
                )
            }

            timeDifference > MIN_TIME_DIFFERENCE && distance < MIN_DISTANCE -> {
                if (lastKnownJourney.isSteadyLocation()) {
                    journeyService.updateLastLocationJourney(
                        userId = currentUserId,
                        lastKnownJourney.copy(
                            update_at = System.currentTimeMillis()
                        )
                    )
                } else {
                    journeyService.saveCurrentJourney(
                        currentUserId,
                        fromLatitude = extractedLocation.latitude,
                        fromLongitude = extractedLocation.longitude,
                        createdAt = System.currentTimeMillis()
                    )
                }
            }

            timeDifference < MIN_TIME_DIFFERENCE && distance < MIN_DISTANCE -> {
                journeyService.updateLastLocationJourney(
                    userId = currentUserId,
                    lastKnownJourney.copy(update_at = System.currentTimeMillis())
                )
            }
        }
    }

    /**
     * Calculate distance between two locations
     * */
    private fun distanceBetween(location1: Location, location2: Location): Float {
        val distance = FloatArray(1)
        Location.distanceBetween(
            location1.latitude,
            location1.longitude,
            location2.latitude,
            location2.longitude,
            distance
        )
        return distance[0]
    }
}

/**
 * Check if user is moving or steady
 * */
fun List<ApiLocation>.isMoving(currentLocation: Location): Boolean {
    return any {
        val distance = currentLocation.distanceTo(it.toLocation()).toDouble()
        distance > MIN_DISTANCE
    }
}
