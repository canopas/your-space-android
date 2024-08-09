package com.canopas.yourspace.data.repository

import com.canopas.yourspace.data.service.auth.AuthService
import com.canopas.yourspace.data.service.place.ApiPlaceService
import com.canopas.yourspace.data.service.place.GeoFenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRepository @Inject constructor(
    private val apiPlaceService: ApiPlaceService,
    private val spaceRepository: SpaceRepository,
    private val geoFenceService: GeoFenceService,
    private val authService: AuthService
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun init() {
        listenForSpaceChange()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listenForSpaceChange() {
        scope.launch {
            val currentUser = authService.currentUser?.id ?: return@launch
            spaceRepository.getUserSpaces(currentUser).flatMapConcat { spaces ->
                val flows = spaces.map { space ->
                    apiPlaceService.listenAllPlaces(space.id)
                }
                if (flows.isEmpty()) {
                    emptyFlow()
                } else {
                    combine(flows) { it.toList().flatten() }
                }
            }.collectLatest { places ->
                geoFenceService.deregisterGeofence()
                geoFenceService.addGeofence(places)
            }
        }
    }

    suspend fun registerAllPlaces() {
        try {
            val currentUser = authService.currentUser?.id ?: return
            val spaces = spaceRepository.getUserSpaces(currentUser).first()
            spaces.forEach { space ->
                val places = apiPlaceService.getPlaces(space.id)
                geoFenceService.addGeofence(places)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while registering all places")
        }
    }
}
