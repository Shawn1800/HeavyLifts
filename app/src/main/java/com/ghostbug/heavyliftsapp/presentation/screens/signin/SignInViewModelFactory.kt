package com.ghostbug.heavyliftsapp.presentation.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.domain.repository.AuthRepository
import com.ghostbug.heavyliftsapp.domain.repository.UserProfileRepository

class SignInViewModelFactory(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return SignInViewModel(authRepository, userProfileRepository) as T
    }
}
