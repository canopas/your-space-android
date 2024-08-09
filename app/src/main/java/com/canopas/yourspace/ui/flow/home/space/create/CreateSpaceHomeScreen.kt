package com.canopas.yourspace.ui.flow.home.space.create

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.canopas.yourspace.R
import com.canopas.yourspace.ui.component.AppBanner
import com.canopas.yourspace.ui.component.CreateSpace
import com.canopas.yourspace.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpaceHomeScreen() {
    val viewModel = hiltViewModel<CreateSpaceHomeViewModel>()
    val state by viewModel.state.collectAsState()

    if (state.error != null) {
        AppBanner(msg = state.error!!) {
            viewModel.resetErrorState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colorScheme.surface),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nav_back_arrow_icon),
                            contentDescription = ""
                        )
                    }
                }
            )
        }
    ) { it ->
        CreateSpace(
            modifier = Modifier.padding(it),
            spaceName = state.spaceName,
            showLoader = state.creatingSpace,
            onSpaceNameChanged = { viewModel.onSpaceNameChange(it) },
            onNext = {
                viewModel.createSpace()
            }
        )
    }
}
