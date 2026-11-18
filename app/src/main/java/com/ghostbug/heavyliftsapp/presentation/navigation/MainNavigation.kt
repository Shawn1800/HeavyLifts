package com.ghostbug.heavyliftsapp.presentation.navigation


import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.auth
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
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
import com.ghostbug.heavyliftsapp.presentation.screens.leaderboard.LeaderboardScreen
import com.ghostbug.heavyliftsapp.presentation.screens.profile.ProfileScreen
import com.ghostbug.heavyliftsapp.presentation.screens.profile.ProfileViewModel
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
        factory = ExerciseViewModelFactory(
            container.exerciseRepository,
            container.workoutRepository
        )
    )

    val logWorkoutViewModel: LogWorkoutViewModel = viewModel(
        factory = LogWorkoutViewModelFactory(
            container.workoutRepository,
            container.oneRepMaxRepository,
            container.oneRepMaxUseCase,
            container.exerciseRepository
        )
    )

    val signInViewModel: SignInViewModel = viewModel(
        factory = SignInViewModelFactory(container.authRepository, container.userProfileRepository)
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

    if (gate is AuthGate.Loading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var initialGateApplied by remember { mutableStateOf(false) }
    LaunchedEffect(gate) {
        if (initialGateApplied) return@LaunchedEffect
        val current = navigationState.currentRoute
        val onAuthScreen = current is Route.SignInScreen || current is Route.SignUpScreen
        val onOnboarding = current is Route.UserProfileScreen1 ||
                current is Route.UserProfileScreen2 ||
                current is Route.UserProfileScreen3
        when (gate) {
            AuthGate.SignedOut -> if (!onAuthScreen) {
                navigator.clearAll()
                navigator.navigate(Route.SignInScreen)
            }
            AuthGate.NeedsOnboarding -> if (!onOnboarding) {
                navigator.clearAll()
                navigator.navigate(Route.UserProfileScreen1)
            }
            AuthGate.Ready -> { /* HomeScreen is already the start route */ }
            AuthGate.Loading -> return@LaunchedEffect
        }
        initialGateApplied = true
    }

    val showBottomNav = navigationState.currentRoute.let { current ->
        current !is Route.SignInScreen &&
                current !is Route.SignUpScreen &&
                current !is Route.UserProfileScreen1 &&
                current !is Route.UserProfileScreen2 &&
                current !is Route.UserProfileScreen3
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    selectedKey = navigationState.topLevelRoute,
                    onSelectKey = {
                        navigator.navigate(it)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),

            onBack = navigator::goBack,

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
                            signUpViewModel = signUpViewModel,
                            onNavigateToExerciseSelection = {
                                val selectedDate = homeViewModel.state.value.selectedDateMillis
                                exerciseViewModel.setSelectedDate(
                                    selectedDate ?: System.currentTimeMillis()
                                )
                                navigator.navigate(Route.ExerciseScreen)
                            },
                            onNavigateToLogIn = {
                                navigator.navigate(Route.SignInScreen)
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
                            onNavigateToHome = {
                                navigator.clearAll()
                                navigator.navigate(Route.HomeScreen)
                            },
                            onNavigateToOnboarding = {
                                userProfileViewModel.reload()
                                navigator.clearAll()
                                navigator.navigate(Route.UserProfileScreen1)
                            },
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
                            onBack = {
                                navigator.navigate(Route.UserProfileScreen1)
                            }
                        )

                    }


                    entry<Route.UserProfileScreen3> {
                        UserProfileScreen3(
                            viewModel = userProfileViewModel,
                            onBack = {
                                navigator.navigate(Route.UserProfileScreen2)
                            },
                            onNext = {
                                navigator.clearAll()
                                navigator.navigate(Route.HomeScreen)
                            }

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


                    entry<Route.LeaderBoards> {
                        LeaderboardScreen()
                    }
                }
            )
        )
    }
}
