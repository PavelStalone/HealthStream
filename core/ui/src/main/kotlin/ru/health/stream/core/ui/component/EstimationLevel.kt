package ru.health.stream.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EstimationLevel(
    text: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .background(
                color = containerColor,
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = contentColor,
            fontWeight = FontWeight.Bold,
        ),
    )
}
