package com.ghostbug.heavyliftsapp.presentation.screens.signup

sealed class SignUpEvent {
    data class OnEmailChange(val email: String) : SignUpEvent()
    data class OnPasswordChange(val password: String) : SignUpEvent()
    object OnSignUpClick : SignUpEvent()
    object OnNavigateToSignIn : SignUpEvent()
    object OnNavigateToScreen1: SignUpEvent()

}