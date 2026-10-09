package ru.health.stream.feature.healthconnect.impl.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.health.stream.core.ui.component.SectionHeader
import ru.health.stream.core.ui.theme.HealthStreamTheme
import ru.health.stream.core.ui.theme.ThemePreviews
import ru.health.stream.feature.healthconnect.impl.R
import ru.health.stream.feature.healthconnect.impl.presentation.model.DataTypeRationale
import ru.health.stream.feature.healthconnect.impl.presentation.model.DefaultDataTypeRationales

@Composable
fun PermissionsRationaleContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.permissions_rationale_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.permissions_rationale_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SectionHeader(
                text = stringResource(R.string.permissions_rationale_section_data_types),
            )
        }

        items(
            items = DefaultDataTypeRationales,
            key = { it.titleRes },
        ) { item ->
            DataTypeCard(item)
        }

        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_disclaimer,
                bodyRes = R.string.permissions_rationale_disclaimer_body,
            )
        }
        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_reports_disclaimer,
                bodyRes = R.string.permissions_rationale_reports_disclaimer_body,
            )
        }
        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_purpose,
                bodyRes = R.string.permissions_rationale_purpose_body,
            )
        }
        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_processing,
                bodyRes = R.string.permissions_rationale_processing_body,
            )
        }
        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_sharing,
                bodyRes = R.string.permissions_rationale_sharing_body,
            )
        }
        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_storage,
                bodyRes = R.string.permissions_rationale_storage_body,
            )
        }
        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_rights,
                bodyRes = R.string.permissions_rationale_rights_body,
            )
        }
        item {
            TextSection(
                titleRes = R.string.permissions_rationale_section_contact,
                bodyRes = R.string.permissions_rationale_contact_body,
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.permissions_rationale_last_updated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DataTypeCard(item: DataTypeRationale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(item.bodyRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.hasRead) {
                        PermissionChip(stringResource(R.string.permissions_rationale_permission_read))
                    }
                    if (item.hasWrite) {
                        PermissionChip(stringResource(R.string.permissions_rationale_permission_write))
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionChip(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            text = label,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun TextSection(
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader(text = stringResource(titleRes))
        Text(
            text = stringResource(bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
