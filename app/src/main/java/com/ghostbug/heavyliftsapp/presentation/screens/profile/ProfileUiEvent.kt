package com.ghostbug.heavyliftsapp.presentation.screens.profile

sealed interface ProfileUiEvent {
    data object NavigateToSignIn : ProfileUiEvent
    data object ProfileSaved : ProfileUiEvent
    data object AccountDeleted : ProfileUiEvent
    data class ShowSnackbar(val message: String) : ProfileUiEvent
}