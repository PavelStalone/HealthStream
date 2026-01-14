package ru.health.stream.feature.settings.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import ru.health.stream.feature.settings.GeneralSettings
import ru.health.stream.feature.settings.model.CategoryUi
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor() : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoriesFlow = GeneralSettings.categories.flatMapLatest { categories ->
        combine(categories.map { (_, cells) ->
            combine(cells.map { cell -> cell.isEnabled }) { enables ->
                CategoryUi(cells = cells.filterIndexed { index, _ -> enables[index] }.toSet())
            }
        }) { categoryArray ->
            categoryArray.filter { category -> category.cells.isNotEmpty() }
        }
    }
}
