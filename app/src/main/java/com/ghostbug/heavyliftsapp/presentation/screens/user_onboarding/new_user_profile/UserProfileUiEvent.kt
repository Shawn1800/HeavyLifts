package com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile


sealed interface UserProfileUiEvent {
    object NavToHome: UserProfileUiEvent
    object NavToScreen2: UserProfileUiEvent
    object NavToScreen3: UserProfileUiEvent
    data class SendSnackbar(val message:String) : UserProfileUiEvent
}