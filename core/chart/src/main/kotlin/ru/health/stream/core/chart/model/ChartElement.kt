package ru.health.stream.core.chart.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Represents visual elements that can be drawn on chart
 *
 * Supports both uniform and mixed styling options
 */
@Immutable
sealed interface ChartElement {

    /**
     * Element with uniform appearance
     *
     * @property color single color for entire element
     * @property position location and z-index on chart
     */
    @Immutable
    data class Uniform(
        val color: Color = Color.White,
        val position: ChartPosition,
    ) : ChartElement

    /**
     * Element with different start and end appearances
     *
     * @property colors different colors for start and end
     * @property position range position with start and end coordinates
     */
    @Immutable
    data class Mixed(
        val colors: Pair<Color, Color> = Color.White to Color.White,
        val position: ChartPosition.Range,
    ) : ChartElement

    companion object {

        /**
         * Position of chart element
         *
         * Retrieves position property regardless of element type
         */
        val ChartElement.position
            get() = when (this) {
                is Mixed -> position
                is Uniform -> position
            }
    }
}
