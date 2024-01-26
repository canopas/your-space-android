package com.canopas.catchme.ui.flow.home.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canopas.catchme.data.models.space.SpaceInfo
import com.canopas.catchme.data.repository.SpaceRepository
import com.canopas.catchme.data.service.location.LocationManager
import com.canopas.catchme.data.utils.AppDispatcher
import com.canopas.catchme.ui.navigation.AppDestinations
import com.canopas.catchme.ui.navigation.HomeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val navigator: HomeNavigator,
    private val locationManager: LocationManager,
    private val spaceRepository: SpaceRepository,
    private val appDispatcher: AppDispatcher
) : ViewModel() {

    val navActions = navigator.navigationChannel

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state

    init {
        getAllSpaces()
    }

    fun onTabChange(index: Int) {
        _state.value = _state.value.copy(currentTab = index)
    }

    fun shouldAskForBackgroundLocationPermission(ask: Boolean) {
        _state.value = _state.value.copy(shouldAskForBackgroundLocationPermission = ask)
    }

    fun startTracking() {
        shouldAskForBackgroundLocationPermission(false)
        locationManager.startService()
    }

    private fun getAllSpaces() = viewModelScope.launch(appDispatcher.IO) {
        try {
            _state.emit(_state.value.copy(isLoadingSpaces = _state.value.spaces.isEmpty()))
            spaceRepository.getAllSpaceInfo().collectLatest { spaces ->
                if(spaceRepository.currentSpaceId.isEmpty()){
                    spaceRepository.currentSpaceId = spaces.firstOrNull()?.space?.id ?: ""
                }
                _state.emit(_state.value.copy(spaces = spaces, isLoadingSpaces = false, selectedSpaceId = spaceRepository.currentSpaceId))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get all spaces")
            _state.emit(_state.value.copy(error = e.message, isLoadingSpaces = false))
        }
    }

    fun toggleSpaceSelection() {
        _state.value =
            _state.value.copy(showSpaceSelectionPopup = !state.value.showSpaceSelectionPopup)
    }

    fun navigateToCreateSpace() {
        toggleSpaceSelection()
        navigator.navigateTo(AppDestinations.createSpace.path)
    }
}

data class HomeScreenState(
    val currentTab: Int = 0,
    val shouldAskForBackgroundLocationPermission: Boolean = false,
    val spaces: List<SpaceInfo> = emptyList(),
    val selectedSpaceId: String = "",
    val isLoadingSpaces: Boolean = false,
    val showSpaceSelectionPopup: Boolean = false,
    val error: String? = null
)
