package ru.health.stream.feature.personal.ui.screen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ru.health.stream.core.monitor.logV
import ru.health.stream.feature.personal.data.model.Email
import ru.health.stream.feature.personal.data.model.User
import ru.health.stream.feature.personal.data.model.cm
import ru.health.stream.feature.personal.data.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class UserInputViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserInputUiState())
    val uiState = _uiState.asStateFlow()

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
            try {
                val user = User(
                    email = Email(state.email),
                    height = state.heightCm.toDoubleOrNull()?.cm ?: 0.cm,
                    gender = state.gender,
                    lastName = state.lastName,
                    firstName = state.firstName,
                    birthday = state.birthday
                )
                userRepository.saveUser(user)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {

    override suspend fun getUser(): User {
        TODO("Not yet implemented")
    }

    override suspend fun saveUser(user: User) {
        logV("save user called: $user")
    }
}

@Immutable
data class UserInputUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val heightCm: String = "",
    val gender: Boolean = true, // true for male, false for female
    val birthday: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val error: String? = null
)
