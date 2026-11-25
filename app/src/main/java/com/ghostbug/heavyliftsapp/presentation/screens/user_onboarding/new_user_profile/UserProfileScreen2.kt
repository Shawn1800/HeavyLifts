package com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostbug.heavyliftsapp.domain.model.Gender
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen2(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navigateNext by rememberUpdatedState(onNext)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UserProfileUiEvent.NavToScreen3 -> navigateNext()
                is UserProfileUiEvent.SendSnackbar -> this@LaunchedEffect.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> Unit
            }
        }
    }

    OnboardingLayout(
        step = 1,
        title = "Body stats.",
        subtitle = "Helps us personalize your experience.",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        footer = {
            OnboardingActions(
                primaryLabel = "Continue",
                onPrimary = { viewModel.onEvent(UserProfileEvent.NavtoScreen3) },
                onBack = onBack,
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column {
                OnboardingSectionLabel("AGE")
                OnboardingTextField(
                    value = state.age?.toString().orEmpty(),
                    onValueChange = { input ->
                        if (input.length <= 3 && input.all(Char::isDigit)) {
                            viewModel.onEvent(UserProfileEvent.OnAgeChanged(input))
                        }
                    },
                    placeholder = "e.g., 25",
                    error = state.ageError,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Age" },
                )
            }

            Column {
                OnboardingSectionLabel("HEIGHT")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    val inCentimeters = state.heightUnit == HeightUnit.CM
                    OnboardingTextField(
                        value = state.heightInputText,
                        onValueChange = { input ->
                            if (inCentimeters || input.count(Char::isDigit) <= 3) {
                                viewModel.onEvent(UserProfileEvent.OnHeightChanged(input))
                            }
                        },
                        placeholder = if (inCentimeters) "e.g., 170" else "e.g., 5'6",
                        error = state.heightError,
                        keyboardType = if (inCentimeters) KeyboardType.Decimal else KeyboardType.Number,
                        modifier = Modifier.weight(1f).semantics { contentDescription = "Height" },
                    )
                    OnboardingUnitToggle(
                        options = listOf("cm", "ft"),
                        selected = if (inCentimeters) 0 else 1,
                        onSelect = {
                            val unit = if (it == 0) HeightUnit.CM else HeightUnit.FEET
                            viewModel.onEvent(UserProfileEvent.OnHeightUnitChanged(unit))
                        },
                    )
                }
            }

            Column {
                OnboardingSectionLabel("WEIGHT")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    val inKilograms = state.weightUnit == WeightUnit.KG
                    OnboardingTextField(
                        value = state.weightInputText,
                        onValueChange = { viewModel.onEvent(UserProfileEvent.OnUserWeightChanged(it)) },
                        placeholder = if (inKilograms) "e.g., 70" else "e.g., 154",
                        error = state.weightError,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f).semantics { contentDescription = "Weight" },
                    )
                    OnboardingUnitToggle(
                        options = listOf("kg", "lbs"),
                        selected = if (inKilograms) 0 else 1,
                        onSelect = {
                            val unit = if (it == 0) WeightUnit.KG else WeightUnit.LBS
                            viewModel.onEvent(UserProfileEvent.OnWeightUnitChanged(unit))
                        },
                    )
                }
            }

            Column {
                OnboardingSectionLabel("GENDER")
                GenderOptions(
                    selected = state.gender,
                    onSelect = { viewModel.onEvent(UserProfileEvent.OnGenderChanged(it)) },
                )
            }
        }
    }
}

@Composable
private fun GenderOptions(selected: Gender?, onSelect: (Gender) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    FlowRow(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Gender.entries.forEach { gender ->
            val isSelected = gender == selected
            val label = when (gender) {
                Gender.MALE -> "Male"
                Gender.FEMALE -> "Female"
                Gender.OTHER -> "Other"
                Gender.PREFER_NOT_TO_SAY -> "Prefer not to say"
            }
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(if (isSelected) OnboardingColors.Selected else OnboardingColors.Field)
                    .border(1.dp, if (isSelected) OnboardingColors.UnitBlue else OnboardingColors.Border, shape)
                    .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(gender) }
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = OnboardingColors.UnitBlue,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Text(
                    text = label,
                    color = if (isSelected) OnboardingColors.Ink else OnboardingColors.Muted,
                    fontFamily = HeavyLiftsType.Body,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
        }
    }
}
