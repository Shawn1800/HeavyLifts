package com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen1(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val navigateNext by rememberUpdatedState(onNext)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UserProfileUiEvent.NavToScreen2 -> navigateNext()
                is UserProfileUiEvent.SendSnackbar -> this@LaunchedEffect.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> Unit
            }
        }
    }

    OnboardingLayout(
        step = 0,
        title = "Choose your\nhandle.",
        subtitle = "This is how others will find you.",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        footer = {
            OnboardingActions(
                primaryLabel = "Continue",
                onPrimary = {
                    focusManager.clearFocus()
                    viewModel.onEvent(UserProfileEvent.NavtoScreen2)
                },
                loading = state.isLoading,
            )
        },
    ) {
        Spacer(Modifier.height(28.dp))
        OnboardingSectionLabel("USERNAME")
        OnboardingTextField(
            value = state.userName,
            onValueChange = { viewModel.onEvent(UserProfileEvent.OnUserNameChanged(it)) },
            placeholder = "e.g. KingLuffy",
            modifier = Modifier.fillMaxWidth(),
            error = state.userNameError,
            keyboardType = KeyboardType.Ascii,
            enabled = !state.isLoading,
            trailingIcon = if (state.isUserNameAvailable) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Username available",
                        tint = OnboardingColors.Ink,
                    )
                }
            } else null,
        )

        if (state.userNameError == null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "3–20 characters • must be unique",
                color = OnboardingColors.Muted,
                fontSize = 14.sp,
                fontFamily = HeavyLiftsType.Body,
            )
        }
    }
}
