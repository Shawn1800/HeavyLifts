package com.ghostbug.heavyliftsapp.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.domain.repository.AuthRepository
import com.ghostbug.heavyliftsapp.domain.repository.UserProfileRepository

class ProfileViewModelFactory(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(userProfileRepository, authRepository) as T
    }
}
