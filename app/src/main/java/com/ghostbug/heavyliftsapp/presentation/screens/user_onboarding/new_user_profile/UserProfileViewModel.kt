package com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.domain.model.Gender
import com.ghostbug.heavyliftsapp.domain.model.UserProfileEntity
import com.ghostbug.heavyliftsapp.domain.repository.UserProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Clock

class UserProfileViewModel(
    val repository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UserProfileUiEvent>()
    val uiEvent: SharedFlow<UserProfileUiEvent> = _uiEvent.asSharedFlow()

    private var checkNameJob: Job? = null

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            val googlePic = repository.getGoogleProfilePic()
            val existingProfile = repository.getOwnProfile()

            _state.update {
                UserProfileState(
                    profilePic = googlePic,
                    userName = existingProfile?.userName ?: "",
                    isUserNameAvailable = !existingProfile?.userName.isNullOrBlank(),
                    age = existingProfile?.age,
                    height = existingProfile?.height,
                    userWeight = existingProfile?.userWeight,
                    gender = existingProfile?.gender,
                    city = existingProfile?.city,
                    country = existingProfile?.country,
                    heightUnit = it.heightUnit,
                    weightUnit = it.weightUnit,
                    heightInputText = formatHeightInput(existingProfile?.height, it.heightUnit),
                    weightInputText = formatWeightInput(existingProfile?.userWeight, it.weightUnit)
                )
            }
        }
    }

    fun onEvent(event: UserProfileEvent) {
        when (event) {
            is UserProfileEvent.OnAgeChanged -> handleAgeChanged(event.age)
            is UserProfileEvent.OnHeightChanged -> handleHeightChanged(event.height)
            is UserProfileEvent.OnUserWeightChanged -> handleWeightChanged(event.userWeight)
            is UserProfileEvent.OnHeightUnitChanged -> handleHeightUnitChanged(event.unit)
            is UserProfileEvent.OnWeightUnitChanged -> handleWeightUnitChanged(event.unit)
            is UserProfileEvent.OnGenderChanged -> handleGenderChanged(event.gender)
            is UserProfileEvent.OnCityChanged -> _state.update { it.copy(city = event.city) }
            is UserProfileEvent.OnCountryChanged -> _state.update { it.copy(country = event.country) }
            is UserProfileEvent.OnDobChanged -> _state.update { it.copy(dob = event.dob) }
            is UserProfileEvent.OnProfilePicChanged -> _state.update { it.copy(profilePic = event.profilePic) }
            is UserProfileEvent.OnUserNameChanged -> handleUserNameChanged(event.userName)
            UserProfileEvent.OnSubmitProfile -> handleSubmitProfile()
            UserProfileEvent.NavtoScreen2 -> handleNavToScreen2()
            UserProfileEvent.NavtoScreen3 -> handleNavToScreen3()
            is UserProfileEvent.message -> sendSnackbar(event.message)
        }
    }

    private fun handleAgeChanged(ageStr: String) {
        val age = ageStr.toIntOrNull()
        val error = when {
            ageStr.isBlank() -> null
            age == null -> "Please enter a valid age"
            age < 12 -> "You must be at least 12 years old"
            age > 120 -> "Please enter a valid age"
            else -> null
        }
        _state.update { it.copy(age = age, ageError = error) }
    }

    private fun handleHeightChanged(input: String) {
        val unit = _state.value.heightUnit
        val digits = if (unit == HeightUnit.FEET) input.filter(Char::isDigit) else ""
        val feet = digits.take(1).toIntOrNull()
        val inches = digits.drop(1).toIntOrNull() ?: 0
        val formatted = if (unit == HeightUnit.FEET && digits.length > 1) {
            "${digits.first()}'${digits.drop(1)}"
        } else {
            if (unit == HeightUnit.FEET) digits else input
        }
        val heightInCm = when (unit) {
            HeightUnit.CM -> input.replace(',', '.').toFloatOrNull()?.takeIf(Float::isFinite)
            HeightUnit.FEET -> feet?.takeIf { inches in 0..11 }?.let {
                (it * 12 + inches) * CENTIMETERS_PER_INCH
            }
        }
        val error = when {
            formatted.isBlank() -> null
            unit == HeightUnit.FEET && inches !in 0..11 -> "Inches must be 0–11"
            heightInCm == null -> "Please enter a valid height"
            heightInCm < 91.44f -> "Height must be more than 3 feet"
            heightInCm > 272f -> "Please enter a valid height"
            else -> null
        }
        _state.update { it.copy(heightInputText = formatted, height = heightInCm, heightError = error) }
    }

    private fun handleWeightChanged(input: String) {
        val value = input.replace(',', '.').toFloatOrNull()?.takeIf(Float::isFinite)
        val unit = _state.value.weightUnit
        val error = when {
            input.isBlank() -> null
            value == null -> "Please enter a valid weight"
            unit == WeightUnit.KG && value < 30f -> "Weight must be at least 30 kg"
            unit == WeightUnit.KG && value > 300f -> "Weight must be less than 300 kg"
            unit == WeightUnit.LBS && value < 66f -> "Weight must be at least 66 lbs (30 kg)"
            unit == WeightUnit.LBS && value > 661f -> "Weight must be less than 661 lbs (300 kg)"
            else -> null
        }
        val weightInKg = value?.let { if (unit == WeightUnit.LBS) it * KILOGRAMS_PER_POUND else it }
        _state.update { it.copy(userWeight = weightInKg, weightInputText = input, weightError = error) }
    }

    private fun handleHeightUnitChanged(unit: HeightUnit) {
        _state.update { state ->
            if (state.heightUnit == unit) return@update state
            state.copy(
                heightUnit = unit,
                heightInputText = formatHeightInput(state.height, unit),
                heightError = state.height?.let { state.heightError }
            )
        }
    }

    private fun handleWeightUnitChanged(unit: WeightUnit) {
        _state.update { state ->
            if (state.weightUnit == unit) return@update state
            state.copy(
                weightUnit = unit,
                weightInputText = formatWeightInput(state.userWeight, unit),
                weightError = state.userWeight?.let { state.weightError }
            )
        }
    }

    private fun formatHeightInput(height: Float?, unit: HeightUnit): String {
        if (height == null || !height.isFinite()) return ""
        return when (unit) {
            HeightUnit.CM -> "%.1f".format(height)
            HeightUnit.FEET -> cmToFeetInches(height).removeSuffix("\"")
        }
    }

    private fun formatWeightInput(weight: Float?, unit: WeightUnit): String {
        if (weight == null || !weight.isFinite()) return ""
        val value = if (unit == WeightUnit.LBS) weight / KILOGRAMS_PER_POUND else weight
        return "%.1f".format(value)
    }

    private fun handleGenderChanged(gender: Gender) {
        _state.update { it.copy(gender = gender) }
    }

    private fun handleUserNameChanged(userName: String) {
        checkNameJob?.cancel()
        val trimmedName = userName.trim()
        _state.update {
            it.copy(
                userName = userName,
                isUserNameAvailable = false,
                userNameError = when {
                    trimmedName.isBlank() -> null
                    trimmedName.length < 3 -> "Username must be at least 3 characters"
                    trimmedName.length > 20 -> "Username must be 20 characters or less"
                    else -> null
                }
            )
        }
        if (trimmedName.length in 3..20) {
            checkNameJob = viewModelScope.launch {
                delay(350)
                val isTaken = repository.isUserNameTaken(trimmedName)
                _state.update {
                    if (it.userName != userName) it else it.copy(
                        isUserNameAvailable = !isTaken,
                        userNameError = if (isTaken) "Username is already taken" else null,
                    )
                }
            }
        }
    }

    fun handleNavToScreen2() {
        viewModelScope.launch {
            val userName = _state.value.userName.trim()
            when {
                userName.isBlank() -> {
                    _state.update { it.copy(userNameError = "Please enter a username") }
                    sendSnackbar("Please enter a username")
                }
                userName.length < 3 -> {
                    _state.update { it.copy(userNameError = "Username must be at least 3 characters") }
                }
                userName.length > 20 -> {
                    _state.update { it.copy(userNameError = "Username must be 20 characters or less") }
                }
                _state.value.userNameError == "Username is already taken" -> {
                    sendSnackbar("Please choose a different username")
                }
                else -> {
                    checkNameJob?.cancel()
                    _state.update { it.copy(isLoading = true) }
                    val isTaken = repository.isUserNameTaken(userName)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isUserNameAvailable = !isTaken,
                            userNameError = if (isTaken) "Username is already taken" else null,
                        )
                    }
                    if (isTaken) {
                        sendSnackbar("Username is already taken")
                    } else {
                        _uiEvent.emit(UserProfileUiEvent.NavToScreen2)
                    }
                }
            }
        }
    }

    private fun handleNavToScreen3() {
        viewModelScope.launch {
            val s = _state.value
            val ageErr = s.ageError
            val heightErr = s.heightError
            val weightErr = s.weightError
            when {
                ageErr != null -> sendSnackbar(ageErr)
                heightErr != null -> sendSnackbar(heightErr)
                weightErr != null -> sendSnackbar(weightErr)
                else -> _uiEvent.emit(UserProfileUiEvent.NavToScreen3)
            }
        }
    }

    private fun handleSubmitProfile() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val s = _state.value
                if (s.userName.isBlank() || s.userNameError != null) {
                    _state.update { it.copy(isLoading = false) }
                    sendSnackbar("Valid username is required")
                    return@launch
                }
                val existingProfile = repository.getOwnProfile()
                val now = Clock.System.now()
                val profile = UserProfileEntity(
                    userId = "",
                    userName = s.userName.trim(),
                    age = s.age,
                    height = s.height,
                    userWeight = s.userWeight,
                    gender = s.gender,
                    city = s.city?.trim()?.ifEmpty { null },
                    country = s.country?.trim()?.ifEmpty { null },
                    profilePic = s.profilePic,
                    dob = s.dob,
                    accountCreated = existingProfile?.accountCreated ?: now,
                    accountLastUpdated = now,
                )
                repository.upsertProfile(profile)

                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(UserProfileUiEvent.NavToHome)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                sendSnackbar("Failed to save profile: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun cmToFeetInches(cm: Float): String {
        val totalInches = (cm / CENTIMETERS_PER_INCH).roundToInt()
        val feet = totalInches / 12
        val inches = totalInches % 12
        return "$feet'$inches\""
    }



    private fun sendSnackbar(message: String) {
        viewModelScope.launch { _uiEvent.emit(UserProfileUiEvent.SendSnackbar(message)) }
    }

    private companion object {
        const val CENTIMETERS_PER_INCH = 2.54f
        const val KILOGRAMS_PER_POUND = 0.45359237f
    }
}
