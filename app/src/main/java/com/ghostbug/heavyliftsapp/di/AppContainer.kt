package com.ghostbug.heavyliftsapp.di


import android.content.Context
import com.ghostbug.heavyLifts.BuildConfig
import com.ghostbug.heavyliftsapp.domain.usecase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.domain.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository_impl.AuthRepositoryImpl
import com.ghostbug.heavyliftsapp.domain.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.data.repository_impl.ExerciseRepositoryImpl
import com.ghostbug.heavyliftsapp.domain.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository_impl.OneRepMaxRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository_impl.UserProfileRepositoryImpl
import com.ghostbug.heavyliftsapp.domain.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.domain.repository.UserProfileRepository
import com.ghostbug.heavyliftsapp.data.repository_impl.WorkoutRepositoryImpl
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class AppContainer(private val context: Context) {

    // single supabase client
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    // repositories
    val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepositoryImpl(
            postgrest = supabase.postgrest,
            auth = supabase.auth
        )
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(supabase.postgrest, supabase.auth)
    }

    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(supabase.postgrest, supabase.auth)
    }

    val oneRepMaxRepository: OneRepMaxRepository by lazy {
        OneRepMaxRepositoryImpl(supabase.postgrest, supabase.auth)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(auth = supabase.auth,supabase.postgrest)
    }

    val oneRepMaxUseCase: OneRepMaxUseCase by lazy {
        OneRepMaxUseCase(oneRepMaxRepository, workoutRepository)
    }
}
