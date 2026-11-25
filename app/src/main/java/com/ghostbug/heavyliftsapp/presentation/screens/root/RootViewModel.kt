package com.ghostbug.heavyliftsapp.presentation.screens.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.domain.repository.UserProfileRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootViewModel(
    sessionStatus: StateFlow<SessionStatus>,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _gate = MutableStateFlow<AuthGate>(AuthGate.Loading)
    val gate: StateFlow<AuthGate> = _gate.asStateFlow()
    private val profileRetry = MutableStateFlow(0)
    private var hasAuthenticatedSession = false
    private var checkedUserId: String? = null

    init {
        viewModelScope.launch {
            combine(sessionStatus, profileRetry) { status, _ -> status }.collectLatest { status ->
                when (status) {
                    SessionStatus.Initializing -> Unit
                    is SessionStatus.RefreshFailure -> {
                        if (_gate.value == AuthGate.Loading) {
                            _gate.value = AuthGate.RestoringSession
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        // SDK 3.4.1 can report a false sign-out while resuming an expired session.
                        if (status.isSignOut || !hasAuthenticatedSession) {
                            hasAuthenticatedSession = false
                            checkedUserId = null
                            _gate.value = AuthGate.SignedOut
                        }
                    }
                    is SessionStatus.Authenticated -> {
                        hasAuthenticatedSession = true
                        val userId = status.session.user?.id
                        val profileResolved = _gate.value == AuthGate.Ready || _gate.value == AuthGate.NeedsOnboarding
                        if (checkedUserId != userId || !profileResolved) {
                            checkProfile(userId)
                        }
                    }
                }
            }
        }
    }

    fun retryProfile() {
        profileRetry.update { it + 1 }
    }

    fun onProfileCompleted() {
        if (_gate.value == AuthGate.NeedsOnboarding) {
            _gate.value = AuthGate.Ready
        }
    }

    private suspend fun checkProfile(userId: String?) {
        _gate.value = AuthGate.Loading
        try {
            val complete = userProfileRepository.isProfileComplete()
            currentCoroutineContext().ensureActive()
            checkedUserId = userId
            _gate.value = if (complete) AuthGate.Ready else AuthGate.NeedsOnboarding
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            _gate.value = AuthGate.ProfileUnavailable
        }
    }
}

class RootViewModelFactory(
    private val auth: Auth,
    private val userProfileRepository: UserProfileRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(RootViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return RootViewModel(auth.sessionStatus, userProfileRepository) as T
    }
}
