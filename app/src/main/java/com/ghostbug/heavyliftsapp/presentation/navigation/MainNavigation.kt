package com.ghostbug.heavyliftsapp.presentation.navigation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.auth
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ghostbug.heavyLifts.R
import com.ghostbug.heavyliftsapp.di.HeavyLifts
import com.ghostbug.heavyliftsapp.presentation.screens.profile.ProfileViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection.ExerciseSelectionScreen
import com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection.ExerciseViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.exercise_selection.ExerciseViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.home.HomeScreen
import com.ghostbug.heavyliftsapp.presentation.screens.home.HomeViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.home.HomeViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.log_workout.LogWorkoutScreen
import com.ghostbug.heavyliftsapp.presentation.screens.log_workout.LogWorkoutViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.log_workout.LogWorkoutViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.profile.ProfileScreen
import com.ghostbug.heavyliftsapp.presentation.screens.profile.ProfileViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.progress.ProgressScreen
import com.ghostbug.heavyliftsapp.presentation.screens.progress.ProgressViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.progress.progressViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.root.AuthGate
import com.ghostbug.heavyliftsapp.presentation.screens.root.RootViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.root.RootViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.signin.SignInScreen
import com.ghostbug.heavyliftsapp.presentation.screens.signin.SignInViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.signin.SignInViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.signup.SignUpViewModelFactory
import com.ghostbug.heavyliftsapp.presentation.screens.signup.SignUpScreen
import com.ghostbug.heavyliftsapp.presentation.screens.signup.SignUpViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile.UserProfileScreen1
import com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile.UserProfileScreen2
import com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile.UserProfileScreen3
import com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile.UserProfileViewModel
import com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile.UserProfileViewModelFactory
@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,

) {
    val context = LocalContext.current
    val app = context.applicationContext as HeavyLifts
    val container = app.appContainer

    val rootViewModel: RootViewModel = viewModel(
        factory = RootViewModelFactory(container.supabase.auth, container.userProfileRepository)
    )
    val gate by rootViewModel.gate.collectAsStateWithLifecycle()

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            container.workoutRepository,
            container.oneRepMaxRepository
        )
    )

    val exerciseViewModel: ExerciseViewModel = viewModel(
        factory = ExerciseViewModelFactory(container.exerciseRepository)
    )

    val logWorkoutViewModel: LogWorkoutViewModel = viewModel(
        factory = LogWorkoutViewModelFactory(
            container.workoutRepository,
            container.oneRepMaxRepository,
            container.oneRepMaxUseCase,
            container.exerciseRepository,
            container.getExerciseProgress
        )
    )

    val signInViewModel: SignInViewModel = viewModel(
        factory = SignInViewModelFactory(container.authRepository)
    )
    val signUpViewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModelFactory(container.authRepository, container.userProfileRepository)
    )

    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(
            container.userProfileRepository
        )
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(container.userProfileRepository, container.authRepository)
    )
    val navigationState = rememberNavigationState(
        startRoute = Route.HomeScreen,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys
    )

    val navigator = remember(navigationState) {
        Navigator(navigationState)
    }

    when (gate) {
        AuthGate.Loading, AuthGate.RestoringSession, AuthGate.ProfileUnavailable -> {
            Column(
                modifier = modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (gate) {
                    AuthGate.ProfileUnavailable -> {
                        Text(stringResource(R.string.auth_profile_unavailable))
                        Button(onClick = rootViewModel::retryProfile) {
                            Text(stringResource(R.string.auth_retry))
                        }
                    }
                    AuthGate.RestoringSession -> {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.auth_reconnecting))
                    }
                    else -> CircularProgressIndicator()
                }
            }
            return
        }
        else -> Unit
    }

    val currentRoute = navigationState.currentRoute
    val onAuthScreen = currentRoute == Route.SignInScreen || currentRoute == Route.SignUpScreen
    val onOnboarding = currentRoute == Route.UserProfileScreen1 ||
            currentRoute == Route.UserProfileScreen2 ||
            currentRoute == Route.UserProfileScreen3
    val redirect = when (gate) {
        AuthGate.SignedOut -> Route.SignInScreen.takeUnless { onAuthScreen }
        AuthGate.NeedsOnboarding -> Route.UserProfileScreen1.takeUnless { onOnboarding }
        AuthGate.Ready -> Route.HomeScreen.takeIf { onAuthScreen || onOnboarding }
        else -> null
    }
    var appliedGate by remember { mutableStateOf<AuthGate?>(null) }
    LaunchedEffect(gate) {
        redirect?.let { route ->
            if (route == Route.UserProfileScreen1) userProfileViewModel.reload()
            navigator.clearAll()
            navigator.navigate(route)
        }
        appliedGate = gate
    }
    if (appliedGate != gate && redirect != null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val showBottomNav = currentRoute == navigationState.topLevelRoute

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomNav || currentRoute == Route.ExerciseScreen) {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    BottomNavigationBar(
                        selectedKey = navigationState.topLevelRoute,
                        onSelectKey = navigator::navigate
                    )
                }
            }
        }
    ) { innerPadding ->
        val bottomPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottomPadding)
                .consumeWindowInsets(bottomPadding),

            onBack = {
                if (gate != AuthGate.SignedOut || currentRoute != Route.SignInScreen) {
                    navigator.goBack()
                }
            },

            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut()
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut()
            },


            entries = navigationState.toEntries(
                entryProvider {
                    entry<Route.HomeScreen> {
                        HomeScreen(
                            homeViewModel = homeViewModel,
                            onNavigateToLogWorkout = { exerciseId ->
                                val date = homeViewModel.state.value.selectedDateMillis
                                    ?: System.currentTimeMillis()
                                navigator.navigate(Route.LogWorkoutScreen(exerciseId, date))
                            },
                            onNavigateToExerciseSelection = {
                                navigator.navigate(Route.ExerciseScreen)
                            }
                        )
                    }


                    entry<Route.ExerciseScreen> {
                        ExerciseSelectionScreen(
                            viewModel = exerciseViewModel,
                            onNavigateToLogWorkout = { exerciseId ->
                                val date = homeViewModel.state.value.selectedDateMillis
                                    ?: System.currentTimeMillis()
                                navigator.navigate(Route.LogWorkoutScreen(exerciseId, date))
                            },
                            onBack = { navigator.clearCurrentStack() },
                        )
                    }


                    entry<Route.LogWorkoutScreen> { entry ->
                        LogWorkoutScreen(
                            exerciseId = entry.exerciseId,
                            dateMillis = entry.dateMillis,
                            logWorkoutViewModel = logWorkoutViewModel,
                            onBack = { navigator.clearCurrentStack() }
                        )
                    }



                    entry<Route.SignInScreen> {
                        val state by signInViewModel.state.collectAsStateWithLifecycle()
                        SignInScreen(
                            state = state,
                            onEvent = signInViewModel::onEvent,
                            uiEvent = signInViewModel.uiEvent,
                            onNavigateToSignUp = {
                                navigator.navigate(Route.SignUpScreen)
                            }
                        )
                    }


                    entry<Route.SignUpScreen> {
                        val state by signUpViewModel.state.collectAsStateWithLifecycle()
                        SignUpScreen(
                            state = state,
                            onEvent = signUpViewModel::onEvent,
                            uiEvent = signUpViewModel.uiEvent,
                            onNavigateToUserProfileScreen1 = {
                                navigator.clearAll()
                                navigator.navigate(Route.UserProfileScreen1)
                            },
                            onNavigateToSignIn = {
                                navigator.clearAll()
                                navigator.navigate(Route.SignInScreen)
                            }
                        )
                    }


                    entry<Route.UserProfileScreen1> {
                        UserProfileScreen1(
                            viewModel = userProfileViewModel,
                            onNext = {
                                navigator.navigate(Route.UserProfileScreen2)
                            },
                            onBack = {
                                navigator.clearCurrentStack()
                            }

                        )

                    }


                    entry<Route.UserProfileScreen2> {
                        UserProfileScreen2(
                            viewModel = userProfileViewModel,
                            onNext = {
                                navigator.navigate(Route.UserProfileScreen3)
                            },
                            onBack = navigator::goBack
                        )

                    }


                    entry<Route.UserProfileScreen3> {
                        UserProfileScreen3(
                            viewModel = userProfileViewModel,
                            onBack = navigator::goBack,
                            onNext = rootViewModel::onProfileCompleted

                        )
                    }


                    entry<Route.ProfileScreen> {
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onBackClick = {
                                navigator.navigate(Route.HomeScreen)
                            },
                            onLogoutClick = {
                                navigator.clearAll()
                                navigator.navigate(Route.SignInScreen)
                            }
                        )
                    }

                    entry<Route.ProgressScreen> {
                        val progressViewModel: ProgressViewModel = viewModel(
                            factory = progressViewModelFactory(
                                container.getExerciseProgress,
                                container.exerciseRepository,
                            )
                        )
                        ProgressScreen(progressViewModel = progressViewModel)
                    }
                }
            ).let { entries ->
                // The Home entry anchors the back stack but should be hidden during sign-in.
                if (gate == AuthGate.SignedOut) entries.drop(1) else entries
            }
        )
    }
}
