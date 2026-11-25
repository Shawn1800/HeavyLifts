package com.ghostbug.heavyliftsapp.presentation.screens.root

import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ghostbug.heavyliftsapp.domain.model.UserProfileEntity
import com.ghostbug.heavyliftsapp.domain.repository.UserProfileRepository
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RootViewModelTest {
    @Test
    fun recoversExpiredSessionAndHonorsRealSignOut() = runBlocking {
        withContext(Dispatchers.Main) {
            val sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
            val viewModel = RootViewModel(sessionStatus, ProfileRepository())
            try {
                val refreshFailure = SessionStatus.RefreshFailure(
                    RefreshFailureCause.NetworkError(IOException("Offline")),
                )
                sessionStatus.value = refreshFailure
                awaitGate(viewModel, AuthGate.RestoringSession)

                sessionStatus.value = authenticatedSession()
                awaitGate(viewModel, AuthGate.Ready)

                sessionStatus.value = refreshFailure
                yield()
                assertEquals(AuthGate.Ready, viewModel.gate.value)

                sessionStatus.value = SessionStatus.NotAuthenticated()
                yield()
                assertEquals(AuthGate.Ready, viewModel.gate.value)

                sessionStatus.value = SessionStatus.NotAuthenticated(isSignOut = true)
                awaitGate(viewModel, AuthGate.SignedOut)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
    }

    @Test
    fun failedProfileLookupCanBeRetriedWithoutEnteringOnboarding() = runBlocking {
        withContext(Dispatchers.Main) {
            val repository = ProfileRepository(Result.failure(IOException("Offline")))
            val sessionStatus = MutableStateFlow<SessionStatus>(authenticatedSession())
            val viewModel = RootViewModel(sessionStatus, repository)
            try {
                awaitGate(viewModel, AuthGate.ProfileUnavailable)

                repository.profileComplete = Result.success(false)
                viewModel.retryProfile()
                awaitGate(viewModel, AuthGate.NeedsOnboarding)

                viewModel.onProfileCompleted()
                assertEquals(AuthGate.Ready, viewModel.gate.value)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
    }

    private suspend fun awaitGate(viewModel: RootViewModel, expected: AuthGate) {
        withTimeout(5_000) {
            viewModel.gate.first { it == expected }
        }
    }

    private fun authenticatedSession() = SessionStatus.Authenticated(
        UserSession(
            accessToken = "fake-access-token",
            refreshToken = "fake-refresh-token",
            expiresIn = 3_600,
            tokenType = "bearer",
            user = UserInfo(aud = "authenticated", id = "test-user"),
        )
    )

    private class ProfileRepository(
        var profileComplete: Result<Boolean> = Result.success(true),
    ) : UserProfileRepository {
        override suspend fun isProfileComplete(): Boolean = profileComplete.getOrThrow()
        override suspend fun getOwnProfile(): UserProfileEntity? = error("Unexpected profile fetch")
        override suspend fun upsertProfile(profile: UserProfileEntity): Unit = error("Unexpected profile update")
        override suspend fun deleteOwnProfile(): Unit = error("Unexpected profile deletion")
        override suspend fun isUserNameTaken(userName: String): Boolean = error("Unexpected username check")
        override fun getGoogleProfilePic(): String? = null
    }
}
