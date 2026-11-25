package com.ghostbug.heavyliftsapp.presentation.screens.root

sealed interface AuthGate {
    data object Loading : AuthGate
    data object RestoringSession : AuthGate
    data object ProfileUnavailable : AuthGate
    data object SignedOut : AuthGate
    data object NeedsOnboarding : AuthGate
    data object Ready : AuthGate
}
