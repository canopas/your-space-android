package com.canopas.yourspace.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.canopas.yourspace.R
import com.canopas.yourspace.ui.component.AppAlertDialog
import com.canopas.yourspace.ui.flow.auth.methods.SignInMethodViewModel
import com.canopas.yourspace.ui.flow.auth.methods.SignInMethodsScreen
import com.canopas.yourspace.ui.flow.auth.phone.EXTRA_RESULT_IS_NEW_USER
import com.canopas.yourspace.ui.flow.auth.phone.SignInWithPhoneScreen
import com.canopas.yourspace.ui.flow.auth.verification.PhoneVerificationScreen
import com.canopas.yourspace.ui.flow.geofence.add.addnew.AddNewPlaceScreen
import com.canopas.yourspace.ui.flow.geofence.add.locate.LocateOnMapScreen
import com.canopas.yourspace.ui.flow.geofence.add.locate.LocateOnMapViewModel
import com.canopas.yourspace.ui.flow.geofence.add.placename.ChoosePlaceNameScreen
import com.canopas.yourspace.ui.flow.geofence.edit.EditPlaceScreen
import com.canopas.yourspace.ui.flow.geofence.places.EXTRA_RESULT_PLACE_LATITUDE
import com.canopas.yourspace.ui.flow.geofence.places.EXTRA_RESULT_PLACE_LONGITUDE
import com.canopas.yourspace.ui.flow.geofence.places.EXTRA_RESULT_PLACE_NAME
import com.canopas.yourspace.ui.flow.geofence.places.PlacesListScreen
import com.canopas.yourspace.ui.flow.geofence.places.PlacesListViewModel
import com.canopas.yourspace.ui.flow.home.home.HomeScreen
import com.canopas.yourspace.ui.flow.home.space.create.CreateSpaceHomeScreen
import com.canopas.yourspace.ui.flow.home.space.create.SpaceInvite
import com.canopas.yourspace.ui.flow.home.space.join.JoinSpaceScreen
import com.canopas.yourspace.ui.flow.intro.IntroScreen
import com.canopas.yourspace.ui.flow.journey.detail.UserJourneyDetailScreen
import com.canopas.yourspace.ui.flow.journey.timeline.JourneyTimelineScreen
import com.canopas.yourspace.ui.flow.messages.chat.MessagesScreen
import com.canopas.yourspace.ui.flow.messages.thread.ThreadsScreen
import com.canopas.yourspace.ui.flow.onboard.OnboardScreen
import com.canopas.yourspace.ui.flow.permission.EnablePermissionsScreen
import com.canopas.yourspace.ui.flow.settings.SettingsScreen
import com.canopas.yourspace.ui.flow.settings.profile.EditProfileScreen
import com.canopas.yourspace.ui.flow.settings.space.SpaceProfileScreen
import com.canopas.yourspace.ui.flow.settings.support.SupportScreen
import com.canopas.yourspace.ui.navigation.AppDestinations
import com.canopas.yourspace.ui.navigation.AppNavigator
import com.canopas.yourspace.ui.navigation.KEY_RESULT
import com.canopas.yourspace.ui.navigation.RESULT_OKAY
import com.canopas.yourspace.ui.navigation.slideComposable
import com.canopas.yourspace.ui.theme.CatchMeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatchMeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = hiltViewModel<MainViewModel>()
                    MainApp(viewModel)

                    LaunchedEffect(Unit) {
                        viewModel.handleIntentData(intent)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.handleIntentData(intent)
        intent?.extras?.clear()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    if (state.isSessionExpired) {
        SessionExpiredAlertPopup()
    }

    if (state.showSpaceNotFoundPopup) {
        SpaceNotFoundPopup()
    }

    AppNavigator(navController = navController, viewModel.navActions)

    NavHost(navController = navController, startDestination = state.initialRoute) {
        slideComposable(AppDestinations.intro.path) {
            IntroScreen()
        }
        slideComposable(AppDestinations.onboard.path) {
            OnboardScreen()
        }
        slideComposable(AppDestinations.signIn.path) {
            val signInModel = hiltViewModel<SignInMethodViewModel>()

            val result = navController.currentBackStackEntry
                ?.savedStateHandle?.get<Int>(KEY_RESULT)
            result?.let {
                val isNewUSer = navController.currentBackStackEntry
                    ?.savedStateHandle?.get<Boolean>(EXTRA_RESULT_IS_NEW_USER) ?: true
                navController.currentBackStackEntry
                    ?.savedStateHandle?.apply {
                        remove<Int>(KEY_RESULT)
                        remove<Boolean>(EXTRA_RESULT_IS_NEW_USER)
                    }

                LaunchedEffect(key1 = result) {
                    if (result == RESULT_OKAY) {
                        signInModel.onSignUp(isNewUSer)
                    }
                }
            }
            SignInMethodsScreen()
        }

        slideComposable(AppDestinations.phoneSignIn.path) {
            SignInWithPhoneScreen()
        }

        slideComposable(AppDestinations.OtpVerificationNavigation.path) {
            PhoneVerificationScreen()
        }

        slideComposable(AppDestinations.home.path) {
            navController.currentBackStackEntry
                ?.savedStateHandle?.apply {
                    remove<Int>(KEY_RESULT)
                    remove<Double>(EXTRA_RESULT_PLACE_LATITUDE)
                    remove<Double>(EXTRA_RESULT_PLACE_LONGITUDE)
                    remove<String>(EXTRA_RESULT_PLACE_NAME)
                }
            HomeScreen(state.verifyingSpace)
        }

        slideComposable(AppDestinations.createSpace.path) {
            CreateSpaceHomeScreen()
        }

        slideComposable(AppDestinations.joinSpace.path) {
            JoinSpaceScreen()
        }

        slideComposable(AppDestinations.SpaceInvitation.path) {
            SpaceInvite()
        }

        slideComposable(AppDestinations.enablePermissions.path) {
            EnablePermissionsScreen()
        }

        slideComposable(AppDestinations.settings.path) {
            SettingsScreen()
        }

        slideComposable(AppDestinations.editProfile.path) {
            EditProfileScreen()
        }

        slideComposable(AppDestinations.SpaceProfileScreen.path) {
            SpaceProfileScreen()
        }

        slideComposable(AppDestinations.spaceThreads.path) {
            ThreadsScreen()
        }

        slideComposable(AppDestinations.ThreadMessages.path) {
            MessagesScreen()
        }

        slideComposable(AppDestinations.contactSupport.path) {
            SupportScreen()
        }

        slideComposable(AppDestinations.places.path) {
            val placesListViewModel = hiltViewModel<PlacesListViewModel>()

            val result = navController.currentBackStackEntry
                ?.savedStateHandle?.get<Int>(KEY_RESULT)

            result?.let {
                val latitude = navController.currentBackStackEntry
                    ?.savedStateHandle?.get<Double>(EXTRA_RESULT_PLACE_LATITUDE) ?: 0.0
                val longitude = navController.currentBackStackEntry
                    ?.savedStateHandle?.get<Double>(EXTRA_RESULT_PLACE_LONGITUDE) ?: 0.0
                val placeName = navController.currentBackStackEntry
                    ?.savedStateHandle?.get<String>(EXTRA_RESULT_PLACE_NAME) ?: ""
                navController.currentBackStackEntry
                    ?.savedStateHandle?.apply {
                        remove<Int>(KEY_RESULT)
                        remove<Double>(EXTRA_RESULT_PLACE_LATITUDE)
                        remove<Double>(EXTRA_RESULT_PLACE_LONGITUDE)
                        remove<String>(EXTRA_RESULT_PLACE_NAME)
                    }

                LaunchedEffect(key1 = result) {
                    if (result == RESULT_OKAY) {
                        placesListViewModel.showPlaceAddedPopup(latitude, longitude, placeName)
                    }
                }
            }
            PlacesListScreen()
        }

        slideComposable(AppDestinations.LocateOnMap.path) {
            val locateOnMapViewModel = hiltViewModel<LocateOnMapViewModel>()

            val result = navController.currentBackStackEntry
                ?.savedStateHandle?.get<Int>(KEY_RESULT)

            LaunchedEffect(key1 = result) {
                if (result == RESULT_OKAY) {
                    val latitude = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<Double>(EXTRA_RESULT_PLACE_LATITUDE) ?: 0.0
                    val longitude = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<Double>(EXTRA_RESULT_PLACE_LONGITUDE) ?: 0.0
                    val placeName = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<String>(EXTRA_RESULT_PLACE_NAME) ?: ""
                    locateOnMapViewModel.navigateBack(latitude, longitude, placeName)
                }
            }
            LocateOnMapScreen()
        }

        slideComposable(AppDestinations.ChoosePlaceName.path) {
            ChoosePlaceNameScreen()
        }

        slideComposable(AppDestinations.EditPlace.path) {
            EditPlaceScreen()
        }

        slideComposable(AppDestinations.addNewPlace.path) {
            AddNewPlaceScreen()
        }

        slideComposable(AppDestinations.UserJourneyDetails.path) {
            UserJourneyDetailScreen()
        }

        slideComposable(AppDestinations.JourneyTimeline.path) {
            JourneyTimelineScreen()
        }
    }
}

@Composable
fun SessionExpiredAlertPopup() {
    val viewModel = hiltViewModel<MainViewModel>()

    AppAlertDialog(
        title = stringResource(id = R.string.common_session_expired_title),
        subTitle = stringResource(id = R.string.common_session_expired_message),
        confirmBtnText = stringResource(id = R.string.common_btn_ok),
        onConfirmClick = {
            viewModel.signOut()
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Composable
fun SpaceNotFoundPopup() {
    val viewModel = hiltViewModel<MainViewModel>()

    AppAlertDialog(
        title = stringResource(id = R.string.common_space_not_found),
        subTitle = stringResource(id = R.string.common_space_not_found_message),
        confirmBtnText = stringResource(id = R.string.common_btn_ok),
        onConfirmClick = {
            viewModel.dismissSpaceNotFoundPopup()
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
