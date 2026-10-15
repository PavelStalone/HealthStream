package ru.health.stream.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.health.stream.core.ui.R
import ru.health.stream.core.ui.icon.Icons
import ru.health.stream.core.ui.icon.default.ArrowDropDown
import ru.health.stream.presentation.viewmodel.LanguageViewModel

/**
 * Settings field for choosing the application language.
 *
 * The selection is stored in [AppLocale]; activities pick it up in
 * `attachBaseContext` and are recreated to re-resolve resources.
 */
@Composable
fun LanguageSettingsField(
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<LanguageViewModel>()
    val languages = viewModel.supportedLanguages

    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val currentLanguage = languages.firstOrNull { it.tag == selectedTag }
        ?: languages.first()

    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "language_rotation"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = null,
                ) { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = stringResource(R.string.settings_language))
                Text(
                    text = currentLanguage.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                modifier = Modifier.rotate(degrees = rotation),
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
            )
        }

        languages.forEach { language ->
            AnimatedVisibility(expanded) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = null,
                        ) {
                            expanded = false
                            viewModel.changeLanguage(language.tag)
                        }
                ) {
                    Text(text = language.tag ?: "System")
                    Text(
                        text = language.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
