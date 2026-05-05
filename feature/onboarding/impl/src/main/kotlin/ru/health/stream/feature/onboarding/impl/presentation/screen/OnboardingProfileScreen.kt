package ru.health.stream.feature.onboarding.impl.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.component.SectionHeader
import ru.health.stream.core.ui.component.TopBar
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.ArrowBack
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.onboarding.impl.presentation.component.onboardingTarget
import ru.health.stream.feature.onboarding.impl.presentation.composition.LocalOnboardingScope
import ru.health.stream.feature.onboarding.impl.presentation.viewmodel.OnboardingViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OnboardingProfileScreen(
    viewModel: OnboardingViewModel
) {
    val onboardingScope = LocalOnboardingScope.current
    val scrollState = rememberScrollState()

    val currentStep by viewModel.currentStepFlow.collectAsState()

    LaunchedEffect(currentStep.targetKey) {
        if (currentStep.targetKey == "profile_save_button") {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            title = UiText.NonTranslatable(value = "Профиль"),
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.ArrowBack,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                text = "Пожалуйста, укажите информацию о себе, чтобы алгоритмы могли учитывать ваши индивидуальные характеристики",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SectionHeader(text = "Основная информация")
                OnboardingInputField(
                    value = "Иван",
                    label = "Имя",
                    placeholder = "Введите ваше имя"
                )
                OnboardingInputField(
                    value = "Иванов",
                    label = "Фамилия",
                    placeholder = "Введите вашу фамилию"
                )
                OnboardingInputField(
                    value = "ivan@example.com",
                    label = "Почта",
                    placeholder = "example@mail.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Дата рождения",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = "1 января 1990",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SectionHeader(text = "Физические данные")
                OnboardingInputField(
                    value = "180",
                    label = "Рост (см)",
                    placeholder = "175",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Column {
                    Text(
                        text = "Пол",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = "Мужской") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text(text = "Женский") }
                        )
                    }
                }
            }

            Button(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .onboardingTarget(key = "profile_save_button", scope = onboardingScope)
                    .padding(all = 8.dp)
                    .height(OutlinedTextFieldDefaults.MinHeight),
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Text(
                    text = "Сохранить",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
internal fun OnboardingInputField(
    value: String,
    label: String,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = {},
            placeholder = { Text(text = placeholder) },
            keyboardOptions = keyboardOptions,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            )
        )
    }
}
