package ru.health.stream.feature.user.impl.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import ru.health.stream.core.ui.R
import ru.health.stream.data.personal.model.Email
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.personal.model.UserValidationException
import ru.health.stream.data.personal.model.cm
import ru.health.stream.data.personal.repository.UserRepository
import ru.health.stream.feature.user.impl.presentation.model.UserUiState

@HiltViewModel
internal class UserInputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getUser()?.asUiState() ?: UserUiState()

            _uiState.value = user
        }
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, error = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, error = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onHeightChange(value: String) {
        _uiState.update { it.copy(heightCm = value, error = null) }
    }

    fun onGenderChange(isMale: Boolean) {
        _uiState.update { it.copy(gender = isMale, error = null) }
    }

    fun onBirthdayChange(date: LocalDate) {
        _uiState.update { it.copy(birthday = date, error = null) }
    }

    fun saveUser(onSuccess: () -> Unit) {
        val state = _uiState.value

        viewModelScope.launch {
            runCatching {
                val user = User(
                    email = Email(state.email),
                    height = state.heightCm.toDoubleOrNull()?.cm ?: 0.cm,
                    gender = state.gender,
                    lastName = state.lastName,
                    firstName = state.firstName,
                    birthday = state.birthday
                )

                userRepository.saveUser(user)
            }.onSuccess {
                onSuccess()
            }.onFailure { throwable ->
                val errorMessage = when (val exception = throwable) {
                    is UserValidationException -> when (exception.reason) {
                        UserValidationException.Reason.FIRST_NAME_EMPTY ->
                            context.getString(R.string.validation_error_first_name_empty)
                        UserValidationException.Reason.LAST_NAME_EMPTY ->
                            context.getString(R.string.validation_error_last_name_empty)
                        UserValidationException.Reason.BIRTHDAY_FUTURE ->
                            context.getString(R.string.validation_error_birthday_future)
                    }
                    else -> exception.message ?: exception.toString()
                }
                _uiState.update { state -> state.copy(error = errorMessage) }
            }
        }
    }

    private fun User.asUiState(): UserUiState = UserUiState(
        gender = gender,
        email = email.value,
        birthday = birthday,
        lastName = lastName,
        firstName = firstName,
        heightCm = height.cm.toString(),
    )
}
