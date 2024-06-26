package com.canopas.yourspace.ui.flow.home.map.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.canopas.yourspace.ui.component.ActionIconButton
import com.canopas.yourspace.ui.theme.AppTheme

@Composable
fun MapControlBtn(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    containerColor: Color = AppTheme.colorScheme.surface,
    contentColor: Color = AppTheme.colorScheme.primary,
    show: Boolean = true,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .padding(bottom = 10.dp, end = 10.dp)
    ) {
        ActionIconButton(
            icon = icon,
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
}
