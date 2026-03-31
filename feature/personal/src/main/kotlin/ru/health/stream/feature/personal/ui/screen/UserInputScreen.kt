package ru.health.stream.feature.personal.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import ru.health.stream.core.ui.model.RUSSIAN_FULL
import ru.health.stream.feature.personal.ui.component.UserInputField

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UserInputScreen(
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserInputViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.birthday
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        )
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                            viewModel.onBirthdayChange(date)
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Данные пользователя",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Пожалуйста, заполните информацию о себе",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(text = "Основная информация")

            UserInputField(
                value = uiState.firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = "Имя",
                placeholder = "Введите ваше имя"
            )

            UserInputField(
                value = uiState.lastName,
                onValueChange = viewModel::onLastNameChange,
                label = "Фамилия",
                placeholder = "Введите вашу фамилию"
            )

            UserInputField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                placeholder = "example@mail.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            val birthdayFormatter = remember {
                LocalDate.Format {
                    dayOfMonth(Padding.NONE)
                    char(' ')
                    monthName(MonthNames.RUSSIAN_FULL)
                    char(' ')
                    year()
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Дата рождения",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.birthday.format(birthdayFormatter),
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
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(text = "Физические данные")

            UserInputField(
                value = uiState.heightCm,
                onValueChange = viewModel::onHeightChange,
                label = "Рост (см)",
                placeholder = "175",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Пол",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.gender,
                        onClick = { viewModel.onGenderChange(true) },
                        label = { Text("Мужской") }
                    )
                    FilterChip(
                        selected = !uiState.gender,
                        onClick = { viewModel.onGenderChange(false) },
                        label = { Text("Женский") }
                    )
                }
            }
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.saveUser(onSuccess = onSuccess) },
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
        ) {
            Text(text = "Сохранить", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        ),
    )
}
