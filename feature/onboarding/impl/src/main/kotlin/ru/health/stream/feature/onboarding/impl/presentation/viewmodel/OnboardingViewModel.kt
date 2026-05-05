package ru.health.stream.feature.onboarding.impl.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.health.stream.core.ui.model.UiText
import ru.health.stream.feature.onboarding.impl.presentation.model.OnboardingStep
import javax.inject.Inject

@HiltViewModel
internal class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val onboardingSteps = sequenceOf(
        // Home Screen Steps
        OnboardingStep(
            id = "home",
            pageIndex = 0,
            text = UiText.NonTranslatable("Добро пожаловать! Давайте познакомимся с функциями приложения")
        ),
        OnboardingStep(
            id = "home",
            pageIndex = 0,
            targetKey = "vitals_card",
            text = UiText.NonTranslatable("Здесь будут появляться ваши измерения за неделю. Пока данных нет, вы видите это сообщение")
        ),
        OnboardingStep(
            id = "home_filled",
            pageIndex = 0,
            targetKey = "vitals_card",
            text = UiText.NonTranslatable("А вот так будут выглядеть данные. Линия на графике показывает среднее арифметическое, а диапазоны - минимальные и максимальные значения")
        ),
        OnboardingStep(
            id = "home_filled",
            pageIndex = 0,
            targetKey = "estimation",
            text = UiText.NonTranslatable("Здесь отображается оценка состояния для последнего измерения (Норма, Высоко и др.)")
        ),
        OnboardingStep(
            id = "home_filled",
            pageIndex = 0,
            targetKey = "vitals_card",
            text = UiText.NonTranslatable("Оценка ваших измерений может появиться не сразу. В течение 15 минут приложение произведёт расчёт и добавит новые оценки")
        ),
        OnboardingStep(
            id = "home_filled",
            pageIndex = 0,
            targetKey = "vitals_card",
            text = UiText.NonTranslatable("Эта карточка отображает общую картину за неделю. Чтобы изучить данные подробнее, нажмите на неё")
        ),
        // Measurement Screen Steps
        OnboardingStep(
            id = "measurement",
            pageIndex = 1,
            targetKey = "measurement_chart",
            text = UiText.NonTranslatable("На этом графике можно быстро оценить динамику ваших измерений")
        ),
        OnboardingStep(
            id = "measurement",
            pageIndex = 1,
            targetKey = "measurement_data",
            text = UiText.NonTranslatable("Здесь можно подробнее изучить значения и их оценку")
        ),
        OnboardingStep(
            id = "measurement",
            pageIndex = 1,
            targetKey = "measurement_data_title",
            text = UiText.NonTranslatable("Нажмите на заголовок даты, чтобы развернуть список измерений")
        ),
        OnboardingStep(
            id = "measurement_expand",
            pageIndex = 1,
            targetKey = "measurement_card",
            text = UiText.NonTranslatable("Это карточка измерения: здесь отображаются значение, заметка и оценка")
        ),
        OnboardingStep(
            id = "measurement_expand_edit",
            pageIndex = 1,
            targetKey = "measurement_card",
            text = UiText.NonTranslatable("Чтобы отредактировать или удалить измерение, смахните карточку влево")
        ),
        OnboardingStep(
            id = "measurement_expand",
            pageIndex = 1,
            targetKey = "measurement_add_button",
            text = UiText.NonTranslatable("Чтобы внести измерения вручную, нажмите на эту кнопку")
        ),
        // Report Screen Steps
        OnboardingStep(
            id = "report",
            pageIndex = 2,
            text = UiText.NonTranslatable("В этом разделе можно настроить и сформировать отчёт для анализа")
        ),
        OnboardingStep(
            id = "report_expand",
            pageIndex = 2,
            targetKey = "report_date_range",
            text = UiText.NonTranslatable("Выберите нужный диапазон дат для формирования отчёта")
        ),
        OnboardingStep(
            id = "report_expand",
            pageIndex = 2,
            targetKey = "report_type_selection",
            text = UiText.NonTranslatable("Здесь можно изменить формат файла")
        ),
        OnboardingStep(
            id = "report_expand",
            pageIndex = 2,
            targetKey = "report_measurement_selection",
            text = UiText.NonTranslatable("Выберите типы измерений, которые должны войти в отчёт")
        ),
        OnboardingStep(
            id = "report_expand",
            pageIndex = 2,
            targetKey = "report_title_exclude",
            text = UiText.NonTranslatable("Вы можете убирать целые блоки записей из отчёта")
        ),
        OnboardingStep(
            id = "report_expand",
            pageIndex = 2,
            targetKey = "report_card",
            text = UiText.NonTranslatable("Нажав на карточку, можно исключить конкретную запись из отчёта")
        ),
        OnboardingStep(
            id = "report_expand",
            pageIndex = 2,
            targetKey = "report_generate_button",
            text = UiText.NonTranslatable("Нажмите кнопку генерации, чтобы получить готовый отчёт")
        ),
        // Profile Screen Steps
        OnboardingStep(
            id = "profile",
            pageIndex = 3,
            text = UiText.NonTranslatable("В профиле нужно указать свои данные, чтобы алгоритмы оценки показателей работали точнее")
        ),
        OnboardingStep(
            id = "profile",
            pageIndex = 3,
            text = UiText.NonTranslatable("Если оставить профиль незаполненным, некоторые оценки будут недоступны")
        ),
        OnboardingStep(
            id = "profile",
            pageIndex = 3,
            targetKey = "profile_save_button",
            text = UiText.NonTranslatable("Не забудьте нажать кнопку сохранения, чтобы применить изменения")
        ),
        OnboardingStep(
            id = "profile",
            pageIndex = 3,
            text = UiText.NonTranslatable("За здоровьем важно следить регулярно, а HealthStream поможет вам делать это просто и эффективно. Желаем отличного самочувствия!")
        )
    ).iterator()

    private val _finishEvent = MutableSharedFlow<Unit>()
    val finishEvent = _finishEvent.asSharedFlow()

    val currentStepFlow = MutableStateFlow(onboardingSteps.next())

    fun nextStep() {
        if (onboardingSteps.hasNext()) {
            currentStepFlow.value = onboardingSteps.next()
        } else {
            onFinish()
        }
    }

    private fun onFinish() {
        viewModelScope.launch {
            _finishEvent.emit(Unit)
        }
    }
}
