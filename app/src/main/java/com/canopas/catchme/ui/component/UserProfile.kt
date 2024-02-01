package com.canopas.catchme.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.canopas.catchme.data.models.user.ApiUser
import com.canopas.catchme.ui.theme.AppTheme

@Composable
fun UserProfile(
    modifier: Modifier,
    user: ApiUser
) {
    val profileUrl = user.profile_image

    Box(
        modifier = modifier
            .background(
                AppTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                AppTheme.colorScheme.primary.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!profileUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(
                        profileUrl
                    ).build()
                ),
                contentScale = ContentScale.Crop,
                contentDescription = "ProfileImage"
            )
        } else {
            Text(
                text = user.fullName.take(1).uppercase(),
                style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp
                )
            )
        }
    }
}