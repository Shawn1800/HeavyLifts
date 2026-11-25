package com.ghostbug.heavyliftsapp


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ghostbug.heavyliftsapp.presentation.navigation.MainNavigation
import com.ghostbug.heavyliftsapp.presentation.ui.theme.Demo103Theme

class MainActivity : ComponentActivity() {
    private var onResetPasswordLinkReceived: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        handleAuthLink(intent)

        setContent {
            Demo103Theme {
                MainNavigation()
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthLink(intent)
    }

    private fun handleAuthLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "heavylifts ") {
            // This tells Supabase to process the session from the URL
            // and allows the user to update their password
            onResetPasswordLinkReceived?.invoke()
        }
    }
}



