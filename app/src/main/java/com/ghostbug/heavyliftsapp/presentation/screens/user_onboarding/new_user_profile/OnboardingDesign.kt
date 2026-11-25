package com.ghostbug.heavyliftsapp.presentation.screens.user_onboarding.new_user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.presentation.ui.theme.HeavyLiftsType

internal object OnboardingColors {
    val Bg = Color(0xFFF5F8FD)
    val Ink = Color(0xFF091637)
    val Muted = Color(0xFF4C6087)
    val Field = Color(0xFFEFF5FD)
    val Border = Color(0xFFB8D5FC)
    val Soft = Color(0xFFDEEBFC)
    val Selected = Color(0xFFBDDAFB)
    val Blue = Color(0xFF0858DC)
    val UnitBlue = Color(0xFF126BC9)
}

@Composable
internal fun OnboardingLayout(
    step: Int,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    footer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = OnboardingColors.Bg,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 20.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(width = 54.dp, height = 48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = OnboardingColors.Soft,
                                contentColor = OnboardingColors.Ink,
                            ),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }

                        OnboardingStepProgress(current = step)
                    }

                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = title,
                        modifier = Modifier.semantics { heading() },
                        color = OnboardingColors.Ink,
                        fontFamily = HeavyLiftsType.Display,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        lineHeight = 42.sp,
                        letterSpacing = (-1).sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = subtitle,
                        color = OnboardingColors.Muted,
                        fontFamily = HeavyLiftsType.Body,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    )
                    Spacer(Modifier.height(30.dp))
                    content()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 28.dp)
                ) {
                    footer()
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepProgress(current: Int) {
    Row(
        modifier = Modifier.semantics { stateDescription = "Step ${current + 1} of 3" },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .width(if (active) 32.dp else 24.dp)
                    .background(
                        if (active) OnboardingColors.Blue else OnboardingColors.Border,
                        CircleShape,
                    )
            )
        }
    }
}

@Composable
internal fun OnboardingActions(
    primaryLabel: String,
    onPrimary: () -> Unit,
    onBack: (() -> Unit)? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) {
            Button(
                onClick = onBack,
                enabled = !loading,
                modifier = Modifier.weight(0.42f).heightIn(min = 50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnboardingColors.Soft,
                    contentColor = OnboardingColors.Ink,
                ),
            ) {
                OnboardingButtonLabel("Back")
            }
        }

        Button(
            onClick = onPrimary,
            enabled = enabled && !loading,
            modifier = Modifier.weight(if (onBack == null) 1f else 0.58f).heightIn(min = 50.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = OnboardingColors.Blue,
                contentColor = Color.White,
                disabledContainerColor = OnboardingColors.Soft,
                disabledContentColor = OnboardingColors.Muted,
            ),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = OnboardingColors.Blue,
                    strokeWidth = 2.dp,
                )
            } else {
                OnboardingButtonLabel(primaryLabel)
            }
        }
    }
}

@Composable
private fun OnboardingButtonLabel(text: String) {
    Text(text, fontFamily = HeavyLiftsType.Body, fontWeight = FontWeight.Bold, fontSize = 17.sp)
}

@Composable
internal fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, fontFamily = HeavyLiftsType.Body, fontSize = 16.sp)
        },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        textStyle = TextStyle(
            fontFamily = HeavyLiftsType.Body,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OnboardingColors.Ink,
            unfocusedTextColor = OnboardingColors.Ink,
            focusedBorderColor = OnboardingColors.Blue,
            unfocusedBorderColor = OnboardingColors.Border,
            focusedPlaceholderColor = OnboardingColors.Muted,
            unfocusedPlaceholderColor = OnboardingColors.Muted,
            cursorColor = OnboardingColors.Blue,
            errorBorderColor = HeavyLiftsColors.Danger,
            focusedContainerColor = OnboardingColors.Field,
            unfocusedContainerColor = OnboardingColors.Field,
            errorContainerColor = OnboardingColors.Field,
        ),
    )
}

@Composable
internal fun OnboardingUnitToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .background(OnboardingColors.Soft, RoundedCornerShape(17.dp))
            .selectableGroup()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .size(width = 47.dp, height = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) OnboardingColors.UnitBlue else Color.Transparent)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(index) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else OnboardingColors.Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = HeavyLiftsType.Body,
                )
            }
        }
    }
}

@Composable
internal fun OnboardingSectionLabel(text: String) {
    Text(
        text = text,
        color = OnboardingColors.Ink,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        fontFamily = HeavyLiftsType.Body,
    )
    Spacer(Modifier.height(8.dp))
}
