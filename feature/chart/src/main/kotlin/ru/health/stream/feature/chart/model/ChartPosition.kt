package ru.health.stream.feature.chart.model

import androidx.compose.runtime.Immutable

/**
 * Represents different types of positions for chart elements
 *
 * Supports single points and ranges in both vertical and horizontal orientations
 */
@Immutable
sealed interface ChartPosition {

    /**
     * Single point on chart with coordinates
     *
     * @property x X-axis coordinate
     * @property y Y-axis coordinate
     * @property z Z-index for layering (higher values appear on top)
     */
    @Immutable
    data class Point(
        val x: Float,
        val y: Float,
        val z: Float,
    ) : ChartPosition

    /**
     * Base interface for range positions
     *
     * Represents ranges along either axis
     */
    @Immutable
    sealed interface Range : ChartPosition {

        /**
         * Vertical range on chart (extends along Y-axis)
         *
         * @property x X-axis position
         * @property y pair of (start, end) Y coordinates
         * @property z Z-index for layering (higher values appear on top)
         */
        @Immutable
        data class Vertical(
            val x: Float,
            val y: Pair<Float, Float>,
            val z: Float,
        ) : Range

        /**
         * Horizontal range on chart (extends along X-axis)
         *
         * @property x pair of (start, end) X coordinates
         * @property y Y-axis position
         * @property z Z-index for layering (higher values appear on top)
         */
        @Immutable
        data class Horizontal(
            val x: Pair<Float, Float>,
            val y: Float,
            val z: Float,
        ) : Range
    }

    companion object {

        // region X coordinates
        /**
         * Starting X coordinate of position
         *
         * For Point and Vertical - X value
         * For Horizontal - minimum of X range
         */
        val ChartPosition.start
            get() = when (this) {
                is Point -> x
                is Range.Horizontal -> x.min()
                is Range.Vertical -> x
            }

        /**
         * Ending X coordinate of position
         *
         * For Point and Vertical - X value
         * For Horizontal - maximum of X range
         */
        val ChartPosition.end
            get() = when (this) {
                is Point -> x
                is Range.Horizontal -> x.max()
                is Range.Vertical -> x
            }
        // endregion

        // region Y coordinates
        /**
         * Top Y coordinate of position
         *
         * For Point and Horizontal - Y value
         * For Vertical - maximum of Y range
         */
        val ChartPosition.top
            get() = when (this) {
                is Point -> y
                is Range.Horizontal -> y
                is Range.Vertical -> y.max()
            }

        /**
         * Bottom Y coordinate of position
         *
         * For Point and Horizontal - Y value
         * For Vertical - minimum of Y range
         */
        val ChartPosition.bottom
            get() = when (this) {
                is Point -> y
                is Range.Horizontal -> y
                is Range.Vertical -> y.min()
            }

        /**
         * Average Y coordinate of position
         *
         * For Point and Horizontal - Y value
         * For Vertical - mean of Y range
         */
        val ChartPosition.average
            get() = when (this) {
                is Point -> y
                is Range.Horizontal -> y
                is Range.Vertical -> (y.max() + y.min()) / 2f
            }
        // endregion

        // region Z coordinate
        /**
         * Z-index of position
         */
        val ChartPosition.z
            get() = when (this) {
                is Point -> z
                is Range.Horizontal -> z
                is Range.Vertical -> z
            }
        // endregion
    }
}

fun <A : Comparable<A>> Pair<A, A>.max(): A = if (first >= second) first else second
fun <A : Comparable<A>> Pair<A, A>.min(): A = if (first <= second) first else second
