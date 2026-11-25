package com.ghostbug.heavyliftsapp.presentation.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.domain.repository.AuthRepository

class SignInViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return SignInViewModel(authRepository) as T
    }
}
