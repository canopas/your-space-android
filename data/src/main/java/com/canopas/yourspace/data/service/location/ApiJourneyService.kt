package com.canopas.yourspace.data.service.location

import com.canopas.yourspace.data.models.location.LocationJourney
import com.canopas.yourspace.data.models.location.LocationTable
import com.canopas.yourspace.data.storage.room.LocationTableDatabase
import com.canopas.yourspace.data.utils.Config
import com.canopas.yourspace.data.utils.LocationConverters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiJourneyService @Inject constructor(
    db: FirebaseFirestore,
    private val locationTableDatabase: LocationTableDatabase,
    private val converters: LocationConverters
) {
    private val userRef = db.collection(Config.FIRESTORE_COLLECTION_USERS)
    private fun journeyRef(userId: String) =
        userRef.document(userId)
            .collection(Config.FIRESTORE_COLLECTION_USER_JOURNEYS)

    suspend fun saveCurrentJourney(
        userId: String,
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double? = null,
        toLongitude: Double? = null,
        routeDistance: Double? = null,
        routeDuration: Long? = null,
        createdAt: Long? = null,
        updateAt: Long? = null
    ) {
        val docRef = journeyRef(userId).document()

        val journey = LocationJourney(
            id = docRef.id,
            user_id = userId,
            from_latitude = fromLatitude,
            from_longitude = fromLongitude,
            to_latitude = toLatitude,
            to_longitude = toLongitude,
            route_distance = routeDistance,
            route_duration = routeDuration,
            routes = emptyList(),
            created_at = createdAt ?: System.currentTimeMillis(),
            update_at = updateAt ?: createdAt ?: System.currentTimeMillis()
        )

        val location = LocationTable(
            userId = userId,
            lastFiveMinutesLocations = routeDistance.toString(),
            lastLocationJourney = routeDuration.toString()
        )

        locationTableDatabase.locationTableDao().insertLocationData(location)
        journey.updateLocationJourney(userId)

        docRef.set(journey).await()
    }

    suspend fun updateLastLocationJourney(userId: String, journey: LocationJourney) {
        try {
            journey.updateLocationJourney(userId)
            journeyRef(userId).document(journey.id).set(journey).await()
        } catch (e: Exception) {
            Timber.e(e, "Error while updating last location journey")
        }
    }

    private suspend fun LocationJourney.updateLocationJourney(userId: String) {
        locationTableDatabase.locationTableDao().getLocationData(userId)?.let { locationTable ->

            val newLocationData =
                locationTable.copy(lastLocationJourney = converters.journeyToString(this))

            newLocationData.let {
                try {
                    locationTableDatabase.locationTableDao().updateLocationTable(it)
                } catch (e: Exception) {
                    Timber.e(e, "Error while updating location journey")
                }
            }
        }
    }

    suspend fun getLastJourneyLocation(userId: String) = try {
        journeyRef(userId).whereEqualTo("user_id", userId)
            .orderBy("created_at", Query.Direction.DESCENDING).limit(1)
            .get().await().documents.firstOrNull()?.toObject(LocationJourney::class.java)
    } catch (e: Exception) {
        Timber.e(e, "Error while getting last location journey")
        null
    }

    suspend fun getMoreJourneyHistory(
        userId: String,
        from: Long?
    ): List<LocationJourney> {
        if (from == null) {
            return journeyRef(userId).whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(20)
                .get().await().documents.mapNotNull { it.toObject<LocationJourney>() }
        }
        return journeyRef(userId).whereEqualTo("user_id", userId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .whereLessThan("created_at", from)
            .limit(20)
            .get().await().documents.mapNotNull { it.toObject<LocationJourney>() }
    }

    suspend fun getJourneyHistory(
        userId: String,
        from: Long? = null,
        to: Long? = null
    ): List<LocationJourney> {
        if (from == null) {
            return journeyRef(userId).whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(20)
                .get().await().documents.mapNotNull { it.toObject<LocationJourney>() }
        } else if (to == null) {
            return journeyRef(userId).whereEqualTo("user_id", userId)
                .whereGreaterThanOrEqualTo("created_at", from)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(20)
                .get().await().documents.mapNotNull { it.toObject<LocationJourney>() }
        } else {
            return journeyRef(userId).whereEqualTo("user_id", userId)
                .whereGreaterThanOrEqualTo("created_at", from)
                .whereLessThanOrEqualTo("created_at", to)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(20)
                .get().await().documents.mapNotNull { it.toObject<LocationJourney>() }
        }
    }

    suspend fun getLocationJourneyFromId(userId: String, journeyId: String): LocationJourney? {
        return journeyRef(userId).document(journeyId).get().await()
            .toObject(LocationJourney::class.java)
    }
}
