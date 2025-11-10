package ru.health.stream.feature.chart.api

import androidx.compose.ui.graphics.Color
import ru.health.stream.feature.chart.api.ChartTransformer.Companion.transformElements
import ru.health.stream.feature.chart.model.ChartElement
import ru.health.stream.feature.chart.model.ChartElement.Companion.position
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.chart.model.ChartPosition.Companion.bottom
import ru.health.stream.feature.chart.model.ChartPosition.Companion.start
import ru.health.stream.feature.chart.model.ChartPosition.Companion.top
import ru.health.stream.feature.chart.model.ChartPosition.Companion.z
import ru.health.stream.feature.chart.model.max
import java.util.LinkedList
import kotlin.math.abs

/**
 * Interface for transforming and manipulating chart elements
 *
 * Provides operations for grouping, styling, and combining chart elements
 */
interface ChartTransformer {

    /**
     * Groups chart elements based on proximity and segment parameters
     *
     * @param segmentCount number of segments per section
     * @param sectionLength length of each section
     * @param groupingDistance maximum distance for grouping elements
     */
    fun group(
        segmentCount: Float,
        sectionLength: Float = 1f,
        groupingDistance: Float,
    ): ChartTransformer

    /**
     * Applies custom transformation to each chart element
     *
     * @param transform function to transform elements
     */
    fun map(transform: (ChartElement) -> ChartElement): ChartTransformer

    // region Colors
    /**
     * Changes color of all elements to specified uniform color
     */
    fun changeColor(color: Color): ChartTransformer

    /**
     * Changes colors of mixed elements to specified pair of colors
     */
    fun changeColor(colors: Pair<Color, Color>): ChartTransformer
    // endregion

    // region Combine transformers
    /**
     * Combines elements from another transformer with current
     */
    fun combine(chartTransformer: ChartTransformer): ChartTransformer

    /**
     * Adds collection of elements to current transformer
     */
    fun addAll(chartElements: Iterable<ChartElement>): ChartTransformer
    // endregion

    /**
     * Builds final list of transformed elements
     *
     * @return list of chart elements after all transformations
     */
    fun calculate(): List<ChartElement>

    companion object {

        /**
         * Creates transformer from collection of chart elements
         *
         * Allows to start transformation chain for existing chart elements
         *
         * @return transformer instance initialized with provided elements
         */
        fun Iterable<ChartElement>.transformElements(): ChartTransformer {

            return ChartTransformerImpl.fromChartElements(this)
        }

        /**
         * Creates transformer from collection of chart positions
         *
         * Converts positions to uniform chart elements and starts transformation chain
         *
         * @return transformer instance initialized with elements created from positions
         */
        fun Iterable<ChartPosition>.transformPositions(): ChartTransformer {

            return ChartTransformerImpl.fromChartPositions(this)
        }
    }
}

internal class ChartTransformerImpl private constructor(
    chartElements: Iterable<ChartElement>
) : ChartTransformer {

    private val chartElements: MutableList<ChartElement> = LinkedList()

    init {
        this.chartElements.addAll(chartElements)
    }

    override fun group(
        segmentCount: Float,
        sectionLength: Float,
        groupingDistance: Float,
    ): ChartTransformer {
        // Grouping by position type for separating blocked elements
        val groupByPositionType = chartElements.groupBy { chartElement -> chartElement.position }

        // Separating blocked elements
        val blockedElements = groupByPositionType
            .filterKeys { position -> position is ChartPosition.Range.Horizontal }
            .flatMap { (_, value) -> value }
        val forGroupingElements = groupByPositionType
            .filterKeys { position -> position !is ChartPosition.Range.Horizontal }
            .flatMap { (_, value) -> value }

        // Separations into Z layers and segmentation
        val segmentedElementsWithDifferentZLayer = forGroupingElements
            .groupBy { element -> element.position.z }
            .mapValues { (_, chartElements) ->
                chartElements.groupBy { element ->
                    val x = element.position.start - BOUND_OFFSET

                    val segmentLength = sectionLength / segmentCount
                    val sectionIndex = (x / sectionLength).toInt()

                    val nearSection = sectionLength * sectionIndex
                    val relativeX = x - nearSection
                    val segmentIndex = (relativeX / segmentLength).toInt()

                    sectionLength * sectionIndex + segmentLength * segmentIndex + segmentLength / 2f
                }
            }

        val groupedElements =
            segmentedElementsWithDifferentZLayer.mapValues { (_, segmentedElements) ->
                segmentedElements.mapValues { (xPosition, elements) ->
                    elements.sortedByDescending { element -> element.position.top } // Sort by Y for top to bottom grouping
                        .fold(LinkedList<ChartElement>()) { acc, next -> // Grouping process
                            acc.lastOrNull()
                                ?.let { previous ->
                                    val nextPosition = next.position
                                    val prevPosition = previous.position

                                    val nextTop = nextPosition.top
                                    val nextBottom = nextPosition.bottom

                                    val prevTop = prevPosition.top
                                    val prevBottom = prevPosition.bottom
                                    val prevRange = prevBottom..prevTop

                                    // Absorption test
                                    if (nextTop in prevRange && nextBottom in prevRange) {
                                        return@fold acc
                                    }

                                    if (abs(prevBottom - nextTop) <= groupingDistance) {
                                        acc.removeLast()
                                        acc.addLast(
                                            previous.changePosition(
                                                pointChange = {
                                                    ChartPosition.Range.Vertical(
                                                        x = xPosition,
                                                        y = y to nextBottom,
                                                        z = z,
                                                    )
                                                },
                                                verticalRangeChange = {
                                                    copy(
                                                        x = xPosition,
                                                        y = y.max() to nextBottom,
                                                    )
                                                },
                                            )
                                        )
                                    } else {
                                        acc.addLast(
                                            next.changePosition(
                                                pointChange = { copy(x = xPosition) },
                                                verticalRangeChange = { copy(x = xPosition) },
                                            )
                                        )
                                    }
                                }
                                ?: acc.add( // First initializing
                                    next.changePosition(
                                        pointChange = { copy(x = xPosition) },
                                        verticalRangeChange = { copy(x = xPosition) },
                                    )
                                )

                            acc
                        }
                }.flatMap { (_, values) -> values } // Flat all segments
            }.flatMap { (_, values) -> values } // Flat all Z layers

        chartElements.clear()
        chartElements.addAll(blockedElements)
        chartElements.addAll(groupedElements)

        return this
    }

    override fun map(transform: (ChartElement) -> ChartElement): ChartTransformer {
        chartElements.replaceAll(transform)
        return this
    }

    override fun changeColor(color: Color): ChartTransformer {
        return map { element ->
            element.change(
                uniformChange = { copy(color = color) },
                mixedChange = { copy(colors = color to color) },
            )
        }
    }

    override fun changeColor(colors: Pair<Color, Color>): ChartTransformer {
        return map { element ->
            element.change(
                mixedChange = { copy(colors = colors) },
            )
        }
    }

    override fun combine(chartTransformer: ChartTransformer): ChartTransformer {
        return calculate().transformElements().addAll(chartTransformer.calculate())
    }

    override fun addAll(chartElements: Iterable<ChartElement>): ChartTransformer {
        this.chartElements.addAll(chartElements)
        return this
    }

    override fun calculate(): List<ChartElement> {
        return chartElements
    }

    private fun ChartElement.change(
        mixedChange: ChartElement.Mixed.() -> ChartElement.Mixed = { this },
        uniformChange: ChartElement.Uniform.() -> ChartElement.Uniform = { this },
    ): ChartElement {
        return when (this) {
            is ChartElement.Mixed -> mixedChange()
            is ChartElement.Uniform -> uniformChange()
        }
    }

    private fun ChartElement.changePosition(
        pointChange: ChartPosition.Point.() -> ChartPosition = { this },
        verticalRangeChange: ChartPosition.Range.Vertical.() -> ChartPosition.Range = { this },
        horizontalRangeChange: ChartPosition.Range.Horizontal.() -> ChartPosition.Range = { this },
    ): ChartElement {
        return when (this) {
            is ChartElement.Mixed -> when (val pos = position) {
                is ChartPosition.Range.Horizontal -> copy(position = pos.horizontalRangeChange())
                is ChartPosition.Range.Vertical -> copy(position = pos.verticalRangeChange())
            }

            is ChartElement.Uniform -> when (val pos = position) {
                is ChartPosition.Point -> copy(position = pos.pointChange())
                is ChartPosition.Range.Horizontal -> copy(position = pos.horizontalRangeChange())
                is ChartPosition.Range.Vertical -> copy(position = pos.verticalRangeChange())
            }
        }
    }

    companion object {

        private const val BOUND_OFFSET = 0.000001f

        /**
         * Creates transformer from existing chart elements
         *
         * @param chartElements collection of chart elements to transform
         * @return new transformer instance initialized with provided elements
         */
        internal fun fromChartElements(chartElements: Iterable<ChartElement>): ChartTransformerImpl {
            return ChartTransformerImpl(chartElements)
        }

        /**
         * Creates transformer from chart positions
         *
         * Implementation details:
         * - Each position is converted to ChartElement.Uniform
         * - Default visual properties are applied
         *
         * @param chartPositions collection of positions to convert to elements
         * @return new transformer instance initialized with converted positions
         */
        internal fun fromChartPositions(chartPositions: Iterable<ChartPosition>): ChartTransformerImpl {
            return ChartTransformerImpl(chartPositions.map { position ->
                ChartElement.Uniform(
                    position = position
                )
            })
        }
    }
}
