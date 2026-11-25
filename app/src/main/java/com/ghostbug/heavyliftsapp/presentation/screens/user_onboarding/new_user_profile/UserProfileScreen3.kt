package com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen3(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UserProfileUiEvent.NavToHome -> onNext()
                UserProfileUiEvent.NavToScreen2 -> onBack()
                is UserProfileUiEvent.SendSnackbar -> {
                    snackbarScope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                UserProfileUiEvent.NavToScreen3 -> Unit
            }
        }
    }

    OnboardingLayout(
        step = 2,
        title = "",
        subtitle = "",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        footer = {
            OnboardingActions(
                primaryLabel = "Done",
                onPrimary = {
                    focusManager.clearFocus()
                    viewModel.onEvent(UserProfileEvent.OnSubmitProfile)
                },
                onBack = onBack,
                loading = state.isLoading,
            )
        },
    ) {
        ProfileAvatar(state.profilePic)
        Spacer(Modifier.height(22.dp))

        ProfileSummary(state.userName.trim())

        Spacer(Modifier.height(28.dp))

        OnboardingSectionLabel("LOCATION (OPTIONAL)")
        OnboardingTextField(
            value = state.city.orEmpty(),
            onValueChange = { viewModel.onEvent(UserProfileEvent.OnCityChanged(it)) },
            placeholder = "City",
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "City" },
            enabled = !state.isLoading,
            leadingIcon = {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = OnboardingColors.Muted,
                )
            },
        )
        Spacer(Modifier.height(12.dp))

        OnboardingTextField(
            value = state.country.orEmpty(),
            onValueChange = { viewModel.onEvent(UserProfileEvent.OnCountryChanged(it)) },
            placeholder = "Country",
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Country" },
            enabled = !state.isLoading,
            leadingIcon = {
                Icon(
                    Icons.Outlined.Language,
                    contentDescription = null,
                    tint = OnboardingColors.Muted,
                )
            },
        )
    }
}

@Composable
private fun ProfileAvatar(profilePic: String?) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .clip(CircleShape)
                .background(OnboardingColors.Selected),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.PersonOutline,
                contentDescription = null,
                modifier = Modifier.size(58.dp),
                tint = OnboardingColors.Ink,
            )

            if (!profilePic.isNullOrBlank()) {
                AsyncImage(
                    model = profilePic,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
private fun ProfileSummary(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Text(
                text = "@$userName",
                modifier = Modifier.padding(start = 12.dp),
                color = OnboardingColors.Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = HeavyLiftsType.Body,
            )
        }
    }

