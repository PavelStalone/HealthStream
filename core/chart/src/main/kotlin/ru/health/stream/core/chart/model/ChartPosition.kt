package ru.health.stream.core.chart.model

import androidx.collection.FloatFloatPair
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
     */
    @Immutable
    data class Point(
        val x: Float,
        val y: Float,
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
         */
        @Immutable
        data class Vertical(
            val x: Float,
            val y: FloatFloatPair,
        ) : Range {

            val top: Float by lazy { y.max() }
            val bottom: Float by lazy { y.min() }
            val averageY: Float by lazy { (y.first + y.second) * 0.5f }
        }

        /**
         * Horizontal range on chart (extends along X-axis)
         *
         * @property x pair of (start, end) X coordinates
         * @property y Y-axis position
         * @property z Z-index for layering (higher values appear on top)
         */
        @Immutable
        data class Horizontal(
            val x: FloatFloatPair,
            val y: Float,
        ) : Range {

            val start: Float by lazy { x.max() }
            val end: Float by lazy { x.min() }
            val averageX: Float by lazy { (x.first + x.second) * 0.5f }
        }
    }
}

fun FloatFloatPair.max(): Float = if (first >= second) first else second
fun FloatFloatPair.min(): Float = if (first <= second) first else second
