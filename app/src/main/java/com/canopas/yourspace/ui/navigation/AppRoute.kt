package com.canopas.yourspace.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface AppRoute {
    val arguments: List<NamedNavArgument>
    val path: String
}

object AppDestinations {
    val home = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "home"
    }

    val settings = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "settings"
    }

    val contactSupport = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "contact-support"
    }

    val editProfile = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "edit-profile"
    }

    object SpaceProfileScreen {
        const val KEY_SPACE_ID = "space_id"

        private const val PATH = "space-settings"
        const val path = "$PATH/{$KEY_SPACE_ID}"

        fun spaceSettings(
            spaceId: String
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_SPACE_ID) { type = NavType.StringType }
            )

            override val path = "$PATH/$spaceId"
        }
    }

    val intro = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "intro"
    }

    val onboard = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "onboard"
    }

    val enablePermissions = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "enable-permissions"
    }

    val signIn = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "sign-in"
    }

    val phoneSignIn = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "phone-sign-in"
    }

    object OtpVerificationNavigation {
        const val KEY_PHONE_NO = "phone_no"
        const val KEY_VERIFICATION_ID = "verification_id"

        private const val PATH = "otp-verification"
        const val path = "$PATH/{$KEY_PHONE_NO}/{$KEY_VERIFICATION_ID}"

        fun otpVerification(
            verificationId: String,
            phoneNo: String
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_PHONE_NO) { type = NavType.StringType },
                navArgument(KEY_VERIFICATION_ID) { type = NavType.StringType }
            )

            override val path = "$PATH/$phoneNo/$verificationId"
        }
    }

    val map = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "map"
    }

    val places = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "places"
    }

    val activity = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "activity"
    }

    val createSpace = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "create-space"
    }
    val joinSpace = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "join-space"
    }

    object SpaceInvitation {
        const val KEY_INVITE_CODE = "invite_code"
        const val KEY_SPACE_NAME = "space_name"

        private const val PATH = "space-invite"
        const val path = "$PATH/{$KEY_INVITE_CODE}/{$KEY_SPACE_NAME}"

        fun spaceInvitation(
            inviteCode: String,
            spaceName: String
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_INVITE_CODE) { type = NavType.StringType },
                navArgument(KEY_SPACE_NAME) { type = NavType.StringType }
            )

            override val path = "$PATH/$inviteCode/$spaceName"
        }
    }

    val spaceThreads = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "space-threads"
    }

    object ThreadMessages {
        const val KEY_THREAD_ID = "thread_id"

        private const val PATH = "messages"
        const val path = "$PATH?$KEY_THREAD_ID={$KEY_THREAD_ID}"

        fun messages(
            threadId: String? = ""
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_THREAD_ID) { type = NavType.StringType }
            )

            override val path = "$PATH?$KEY_THREAD_ID=$threadId"
        }
    }

    object LocateOnMap {
        const val KEY_SELECTED_NAME = "place_name"

        private const val PATH = "locate-on-map"
        const val path =
            "$PATH?$KEY_SELECTED_NAME={$KEY_SELECTED_NAME}"

        fun setArgs(
            placeName: String
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_SELECTED_NAME) {
                    type = NavType.StringType
                    nullable = true
                }
            )

            override val path = "$PATH?$KEY_SELECTED_NAME=$placeName"
        }
    }

    object ChoosePlaceName {
        const val KEY_SELECTED_LAT = "lat"
        const val KEY_SELECTED_LONG = "long"
        const val KEY_SELECTED_NAME = "place_name"

        private const val PATH = "choose-place-name"
        const val path =
            "$PATH?$KEY_SELECTED_LAT={$KEY_SELECTED_LAT}&$KEY_SELECTED_LONG={$KEY_SELECTED_LONG}&$KEY_SELECTED_NAME={$KEY_SELECTED_NAME}"

        fun setArgs(
            latitude: Double,
            longitude: Double,
            placeName: String = ""
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_SELECTED_LAT) { type = NavType.StringType },
                navArgument(KEY_SELECTED_LONG) { type = NavType.StringType },
                navArgument(KEY_SELECTED_NAME) {
                    type = NavType.StringType
                    nullable = true
                }
            )

            override val path = "$PATH?$KEY_SELECTED_LAT=$latitude&$KEY_SELECTED_LONG=$longitude&$KEY_SELECTED_NAME=$placeName"
        }
    }

    object EditPlace {
        const val KEY_PLACE_ID = "placeId"

        private const val PATH = "edit-place"
        const val path = "$PATH/{$KEY_PLACE_ID}"

        fun setArgs(placeId: String) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_PLACE_ID) { type = NavType.StringType }
            )

            override val path = "$PATH/$placeId"
        }
    }

    val addNewPlace = object : AppRoute {
        override val arguments: List<NamedNavArgument> = emptyList()
        override val path: String = "add-new-place"
    }

    object UserJourneyDetails {
        const val KEY_JOURNEY_ID = "journey_id"
        const val KEY_SELECTED_USER_ID = "selected_user_id"

        private const val PATH = "user-journey"
        const val path = "$PATH/{$KEY_SELECTED_USER_ID}/{$KEY_JOURNEY_ID}"

        fun args(
            selectedUserId: String,
            journeyId: String
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_JOURNEY_ID) { type = NavType.StringType },
                navArgument(KEY_SELECTED_USER_ID) { type = NavType.StringType }
            )

            override val path = "$PATH/$selectedUserId/$journeyId"
        }
    }

    object JourneyTimeline {
        const val KEY_SELECTED_USER_ID = "selected_user_id"

        private const val PATH = "user-journey-timeline"
        const val path = "$PATH/{$KEY_SELECTED_USER_ID}"

        fun args(
            selectedUserId: String
        ) = object : AppRoute {

            override val arguments = listOf(
                navArgument(KEY_SELECTED_USER_ID) { type = NavType.StringType }
            )

            override val path = "$PATH/$selectedUserId"
        }
    }
}
