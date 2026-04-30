package ru.health.stream.source.local.file.pdf

import android.content.Context
import androidx.collection.FloatFloatPair
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import ru.health.stream.core.monitor.logV
import ru.health.stream.core.ui.model.RUSSIAN_FULL
import ru.health.stream.data.personal.model.User
import ru.health.stream.data.report.model.MeasurementSummary
import ru.health.stream.data.report.usecase.CalculateMeasurementSummaryUseCase
import ru.health.stream.data.vitals.domain.estimation.MeasurementAnalyzer
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.MeasurementGroup
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.usecase.DateTransformerUseCase
import ru.health.stream.data.vitals.usecase.GroupMeasurementByPeriodUseCase
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.chart.model.path.DashPathEffect
import ru.health.stream.source.local.file.ReportGenerator
import ru.health.stream.source.local.file.model.ACCENT
import ru.health.stream.source.local.file.model.HEADER_BG
import ru.health.stream.source.local.file.model.MeasurementSection
import ru.health.stream.source.local.file.model.ReportEstimation
import ru.health.stream.source.local.file.model.STRIPE_BG
import ru.health.stream.source.local.file.model.TEXT_MUTED
import ru.health.stream.source.local.file.model.asMeasurementSection
import ru.health.stream.source.local.file.model.asReportEstimation
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal class PdfReportGenerator(
    @ApplicationContext private val context: Context,
    private val measurementAnalyzer: MeasurementAnalyzer,
    private val groupMeasurementByPeriodUseCase: GroupMeasurementByPeriodUseCase,
    private val calculateMeasurementSummaryUseCase: CalculateMeasurementSummaryUseCase,
) : ReportGenerator {

    override suspend fun generate(
        user: User?,
        outputStream: OutputStream,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>, // Уже отсортированный по created_at
    ) {
        logV("generate called: ${measurements.size}")

        val period = getPeriodByRange(dateRange)
        val timeZone = TimeZone.currentSystemDefault()
        val (measurementSections, measurementsSummary) = calculateMeasurementsData(
            period = period,
            timeZone = timeZone,
            measurements = measurements,
        )

        PdfWriter(outputStream).use { writer ->
            PdfDocument(writer).use { pdf ->
                val font = requireNotNull(
                    PdfFontFactory.createFont(
                        context.assets.open("fonts/arial.ttf").readBytes(),
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                    )
                )
                val fontBold = requireNotNull(
                    PdfFontFactory.createFont(
                        context.assets.open("fonts/arial_bolditalic.ttf").readBytes(),
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                    )
                )

                Document(pdf).use { doc ->
                    buildTitlePage(
                        pdf = pdf,
                        user = user,
                        font = font,
                        fontBold = fontBold,
                        timeZone = timeZone,
                        dateRange = dateRange,
                    )

                    doc.add(AreaBreak())
                    buildSummaryTable(
                        doc = doc,
                        font = font,
                        fontBold = fontBold,
                        timeZone = timeZone,
                        summaries = measurementsSummary,
                    )

                    measurementSections.forEach { (type, sections) ->
                        val levels = measurementAnalyzer
                            .levels(date = dateRange.endInclusive, type = type)
                            .mapKeys { (key, _) -> key.asReportEstimation() }

                        logV("CalculatedLevels: $levels")

                        doc.add(AreaBreak())
                        buildChartPage(
                            doc = doc,
                            pdf = pdf,
                            font = font,
                            areas = levels,
                            period = period,
                            fontBold = fontBold,
                            timeZone = timeZone,
                            sections = sections,
                            dateRange = dateRange,
                        )
                    }

                    measurementSections.forEach { (_, sections) ->
                        doc.add(AreaBreak())
                        buildBigTable(
                            doc = doc,
                            font = font,
                            fontBold = fontBold,
                            sections = sections.toList(),
                        )
                    }
                }
            }
        }
    }

    private fun buildTitlePage(
        user: User?,
        font: PdfFont,
        pdf: PdfDocument,
        fontBold: PdfFont,
        timeZone: TimeZone,
        dateRange: ClosedRange<Instant>,
    ) {
        val dateNow = Clock.System.todayIn(timeZone)

        val page = pdf.addNewPage()
        val c = PdfCanvas(page.newContentStreamBefore(), page.resources, pdf)
        val w = pdf.defaultPageSize.width
        val h = pdf.defaultPageSize.height

        c.setFillColor(HEADER_BG)
        c.rectangle(0.0, h * 0.55, w.toDouble(), h * 0.45)
        c.fill()

        c.setFillColor(ACCENT)
        c.rectangle(0.0, h * 0.55, w.toDouble(), 3.0)
        c.fill()

        c.setStrokeColor(DeviceRgb(255, 255, 255))
        c.setLineWidth(1f)
        c.circle(w - 60.0, h - 80.0, 90.0)
        c.stroke()
        c.setLineWidth(0.5f)
        c.circle(w - 120.0, h - 180.0, 50.0)
        c.stroke()

        c.setFillColor(ACCENT)
        c.rectangle(40.0, h * 0.42, 3.0, 120.0)
        c.fill()

        c.beginText()
        c.setFontAndSize(fontBold, 36f)
        c.setFillColor(DeviceRgb(255, 255, 255))
        c.moveText(60.0, h - 160.0)
        c.showText("Отчет здоровья")
        c.endText()

        c.beginText()
        c.setFontAndSize(font, 18f)
        c.setFillColor(DeviceRgb(200, 210, 220))
        c.moveText(60.0, h - 200.0)
        c.showText("Создан приложением HealthStream")
        c.endText()

        c.setFillColor(DeviceRgb(230, 235, 240))
        c.rectangle(60.0, h * 0.60, 300.0, 50.0)
        c.fill()

        val timeFormatter = LocalDateTime.Format {
            dayOfMonth(Padding.NONE)
            char(value = '.')
            monthNumber()
            char(value = '.')
            year()
            char(value = ' ')
            hour()
            char(value = ':')
            minute()
        }

        c.beginText()
        c.setFontAndSize(font, 11f)
        c.setFillColor(HEADER_BG)
        c.moveText(75.0, h * 0.60 + 28.0)
        c.showText("Отчет создан за период")
        c.moveText(0.0, -15.0)
        c.showText(
            "${
                dateRange.start.toLocalDateTime(timeZone).format(timeFormatter)
            } - ${
                dateRange.endInclusive.toLocalDateTime(timeZone).format(timeFormatter)
            }"
        )
        c.endText()

        val dateFormatter = LocalDate.Format {
            dayOfMonth(Padding.NONE)
            char(value = '.')
            monthNumber()
            char(value = '.')
            year()
        }

        user?.let {
            c.setFontAndSize(font, 12f)
            c.setFillColor(TEXT_MUTED)
            c.beginText()
            c.moveText(60.0, 100.0)
            c.showText(
                buildString {
                    append("Пользователь: ${user.fullName}.")
                    append(" ")
                    append("День рождения: ${user.birthday.format(dateFormatter)}")
                }
            )
            c.moveText(0.0, -20.0)
            c.showText(
                buildString {
                    append("Полных лет: ${user.datePeriodAfterBirthday(dateNow).years}.")
                    append(" ")
                    append("Пол: ")
                    append(if (user.gender) "мужской" else "женский")
                    append(". Рост: ${user.height.cm} сантиметров")
                }
            )
            c.endText()
        }

        c.release()
    }

    private fun buildSummaryTable(
        doc: Document,
        font: PdfFont,
        fontBold: PdfFont,
        timeZone: TimeZone,
        summaries: List<MeasurementSummary>,
    ) {
        doc.add(
            Paragraph("Сводка всех измерений")
                .setFont(fontBold).setFontSize(22f).setFontColor(HEADER_BG)
                .setMarginTop(20f).setMarginBottom(6f)
        )

        doc.add(
            Paragraph(
                "Здесь отображается общее количество отклонений, рассчитанное алгоритмом"
            ).setFont(font).setFontSize(10f).setFontColor(TEXT_MUTED).setMarginBottom(12f)
        )

        val entriesWeight = FloatArray(ReportEstimation.entries.size) { 50f }
        val cw = floatArrayOf(100f, 50f, *entriesWeight, 120f)

        val table = Table(UnitValue.createPointArray(cw)).apply {
            setWidth(UnitValue.createPercentValue(100f))
        }

        val entriesName = ReportEstimation.entries.map(ReportEstimation::text).toTypedArray()
        for (h in listOf("Показатель", "Всего", *entriesName, "Значения")) {
            table.addHeaderCell(
                Cell().add(
                    Paragraph(h).setFont(fontBold).setFontSize(9f)
                        .setFontColor(DeviceRgb(255, 255, 255))
                )
                    .setBackgroundColor(HEADER_BG).setPadding(6f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }

        summaries.forEachIndexed { i, summary ->
            val backgroundColor = if (i % 2 == 0) STRIPE_BG else null

            val section = summary.group.asMeasurementSection(timeZone)
            val group = section.measurementGroup

            val cs = listOf(
                Cell().add(
                    Paragraph(section.typeName)
                        .setFont(fontBold)
                        .setFontSize(9f)
                ).defaultCell(backgroundColor = backgroundColor),

                Cell().add(countParagraph(count = summary.counts, font = font))
                    .defaultCell(backgroundColor = backgroundColor),

                *Estimation.Level.entries.map { estimationLevel ->
                    val count = summary.estimationsCount[estimationLevel] ?: 0

                    Cell().add(
                        countParagraph(
                            count = count,
                            font = if (count > 0) fontBold else font,
                            fontColor = if (count > 0) estimationLevel.asReportEstimation().color else null,
                        )
                    ).defaultCell(backgroundColor = backgroundColor)
                }.toTypedArray(),

                Cell().add(
                    Paragraph(group.aggregateValue + " " + section.unit)
                        .setFont(font)
                        .setFontSize(9f)
                ).defaultCell(backgroundColor = backgroundColor),
            )

            cs.forEach { cell -> table.addCell(cell) }
        }

        val backgroundColor = if (summaries.size % 2 == 0) STRIPE_BG else null

        val cs = listOf(
            Cell().add(
                Paragraph("ИТОГО")
                    .setFont(fontBold)
                    .setFontSize(9f)
            ).defaultCell(backgroundColor = backgroundColor),

            Cell().add(
                countParagraph(
                    font = fontBold,
                    count = summaries.sumOf(MeasurementSummary::counts),
                )
            ).defaultCell(backgroundColor = backgroundColor),

            *Estimation.Level.entries.map { estimationLevel ->
                val count =
                    summaries.mapNotNull { cell -> cell.estimationsCount[estimationLevel] }.sum()

                Cell().add(
                    countParagraph(
                        count = count,
                        font = fontBold,
                        fontColor = if (count > 0) estimationLevel.asReportEstimation().color else null,
                    )
                ).defaultCell(backgroundColor = backgroundColor)
            }.toTypedArray(),

            Cell().defaultCell(backgroundColor = backgroundColor),
        )

        cs.forEach { cell -> table.addFooterCell(cell) }

        doc.add(table)
    }

    private fun buildBigTable(
        doc: Document,
        font: PdfFont,
        fontBold: PdfFont,
        sections: List<MeasurementSection>,
    ) {
        val firstSection = sections.first()

        doc.add(
            Paragraph("Таблица измерений: ${firstSection.typeName}")
                .setFontSize(22f)
                .setFont(fontBold)
                .setMarginTop(20f)
                .setMarginBottom(6f)
                .setFontColor(HEADER_BG)
        )
        doc.add(
            Paragraph("Таблица со всеми измерениями, отсортированными по порядку. Оценка в строке отображается с учётом приоритета: сначала показываются измерения с самым высоким приоритетом за заданный период")
                .setFont(font)
                .setFontSize(10f)
                .setMarginBottom(12f)
                .setFontColor(TEXT_MUTED)
        )

        val cw = floatArrayOf(20f, 80f, 80f, 60f, 120f)

        var i = 1

        sections.chunked(size = 1000).forEachIndexed { index, chunk ->
            val table = Table(UnitValue.createPointArray(cw)).apply {
                setWidth(UnitValue.createPercentValue(100f))
            }

            listOf("#", "Время", "Значение", "Оценка", "Заметка").forEach { header ->
                table.addHeaderCell(
                    Cell().add(
                        Paragraph(header)
                            .setFontSize(9f)
                            .setFont(fontBold)
                            .setFontColor(DeviceRgb(255, 255, 255))
                    )
                        .setPadding(6f)
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER)
                )
            }

            chunk.forEach { section ->
                val group = section.measurementGroup
                val defaultBackgroundColor = if (i % 2 == 0) STRIPE_BG else null
                val backgroundColor =
                    section.reportEstimation?.backgroundColor ?: defaultBackgroundColor

                val cs = listOf(
                    Cell().add(
                        Paragraph("$i")
                            .setFont(font)
                            .setFontSize(8f)
                            .setFontColor(TEXT_MUTED)
                    )
                        .setPadding(5f)
                        .setBackgroundColor(backgroundColor)
                        .setTextAlignment(TextAlignment.CENTER),

                    Cell().add(
                        Paragraph(section.date)
                            .setFont(font)
                            .setFontSize(9f)
                    )
                        .setPadding(5f)
                        .setBackgroundColor(backgroundColor)
                        .setTextAlignment(TextAlignment.CENTER),

                    Cell().add(
                        Paragraph(group.aggregateValue + " " + section.unit)
                            .setFont(font)
                            .setFontSize(9f)
                    )
                        .setPadding(5f)
                        .setBackgroundColor(backgroundColor)
                        .setTextAlignment(TextAlignment.CENTER),

                    Cell().add(
                        Paragraph(section.reportEstimation?.text ?: "")
                            .setFontSize(9f)
                            .setFont(fontBold)
                            .setFontColor(section.reportEstimation?.color)
                    )
                        .setPadding(5f)
                        .setBackgroundColor(backgroundColor)
                        .setTextAlignment(TextAlignment.CENTER),

                    Cell().add(
                        Paragraph(group.note ?: "")
                            .setFont(font)
                            .setFontSize(8f)
                            .setFontColor(TEXT_MUTED)
                    )
                        .setPadding(5f)
                        .setBackgroundColor(backgroundColor),
                )

                cs.forEach { cell -> table.addCell(cell) }
                i++
            }

            doc.add(table)
        }
    }

    private fun buildChartPage(
        doc: Document,
        font: PdfFont,
        period: Period,
        pdf: PdfDocument,
        fontBold: PdfFont,
        timeZone: TimeZone,
        dateRange: ClosedRange<Instant>,
        sections: List<MeasurementSection>,
        areas: Map<ReportEstimation, List<ClosedRange<Float>>> = emptyMap(),
    ) {
        val dateTransformerUseCase = DateTransformerUseCase(
            period = period,
            timeZone = timeZone,
            dateRange = dateRange,
        )
        val section = sections.first()
        val page = pdf.lastPage
        val c = PdfCanvas(page.newContentStreamBefore(), page.resources, pdf)
        val ps = pdf.defaultPageSize
        val pW = ps.width
        val pH = 400f

        var yMin = Float.MAX_VALUE
        var yMax = Float.MIN_VALUE
        val positionMap: MutableMap<Int, MutableList<ChartPosition>> = mutableMapOf()

        doc.add(
            Paragraph("График показаний: ${section.typeName}")
                .setFontSize(22f)
                .setMarginTop(20f)
                .setFont(fontBold)
                .setMarginBottom(6f)
                .setFontColor(HEADER_BG)
        )
        doc.add(
            Paragraph("Данные представлены в сгруппированном виде. Диапазоны показывают минимальные и максимальные значения, а линия их среднее арифметическое")
                .setFont(font)
                .setFontSize(10f)
                .setMarginBottom(16f)
                .setFontColor(TEXT_MUTED)
        )

        sections.forEach { section ->
            val group = section.measurementGroup

            val x = dateTransformerUseCase(
                group.dateRange.start.plus((group.dateRange.endInclusive - group.dateRange.start) / 2)
            )

            when (group) {
                is MeasurementGroup.BloodGlucose -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = group.mean.value.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = group.range.start.toFloat(),
                                second = group.range.endInclusive.toFloat(),
                            )
                        )
                    )

                    yMin = min(group.range.start.toFloat(), yMin)
                    yMax = max(group.range.endInclusive.toFloat(), yMax)
                }

                is MeasurementGroup.BodyWeight -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = group.mean.value.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = group.range.start,
                                second = group.range.endInclusive,
                            )
                        )
                    )

                    yMin = min(group.range.start, yMin)
                    yMax = max(group.range.endInclusive, yMax)
                }

                is MeasurementGroup.HeartRate -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = group.mean.value.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = group.range.start.toFloat(),
                                second = group.range.endInclusive.toFloat(),
                            )
                        )
                    )

                    yMin = min(group.range.start.toFloat(), yMin)
                    yMax = max(group.range.endInclusive.toFloat(), yMax)
                }

                is MeasurementGroup.OxygenSaturation -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = group.mean.value.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = group.range.start,
                                second = group.range.endInclusive,
                            )
                        )
                    )

                    yMin = min(group.range.start, yMin)
                    yMax = max(group.range.endInclusive, yMax)
                }

                is MeasurementGroup.RespirationRate -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = group.mean.value.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = group.range.start.toFloat(),
                                second = group.range.endInclusive.toFloat(),
                            )
                        )
                    )

                    yMin = min(group.range.start.toFloat(), yMin)
                    yMax = max(group.range.endInclusive.toFloat(), yMax)
                }

                is MeasurementGroup.BloodPressure -> {
                    val systolicScatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val systolicMeanPositions = positionMap.getOrPut(1) { mutableListOf() }
                    val diastolicScatterPositions = positionMap.getOrPut(2) { mutableListOf() }
                    val diastolicMeanPositions = positionMap.getOrPut(3) { mutableListOf() }

                    systolicMeanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = group.systolicMean.value.toFloat(),
                        )
                    )
                    diastolicMeanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = group.diastolicMean.value.toFloat(),
                        )
                    )
                    systolicScatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = group.systolicRange.start,
                                second = group.systolicRange.endInclusive,
                            )
                        )
                    )
                    diastolicScatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = group.diastolicRange.start,
                                second = group.diastolicRange.endInclusive,
                            )
                        )
                    )

                    yMin = min(group.systolicRange.start, yMin)
                    yMin = min(group.diastolicRange.start, yMin)
                    yMax = max(group.systolicRange.endInclusive, yMax)
                    yMax = max(group.diastolicRange.endInclusive, yMax)
                }
            }
        }

        val drawScope = PdfDrawScope(
            pdfCanvas = c,
            pageSize = Size(width = pW, height = pH),
            verticalMargin = Offset(x = ps.height - pH - 200f, y = 0f),
            horizontalMargin = Offset(x = 50f, y = 20f),
        )
        val chart = PdfChartDrawScopeImpl(
            drawScope = drawScope,
            widthRange = 0f..1f,
            heightRange = yMin..yMax,
        )

        val step = 10f
        val start = floor(yMin / step) * step
        val end = ceil(yMax / step) * step
        val yLabels = generateSequence(seed = start) { y -> y + step }
            .takeWhile { y -> y <= end }
            .toList()

        val dateTimeFormatter = when (period) {
            Period.OneHour -> LocalDateTime.Format {
                hour()
                char(value = ':')
                minute()
            }

            Period.SixHour -> LocalDateTime.Format {
                dayOfMonth()
                char('.')
                monthNumber()
                char(value = '\n')
                hour()
                char(value = ':')
                minute()
            }

            Period.Day -> LocalDateTime.Format {
                dayOfMonth(padding = Padding.NONE)
            }

            is Period.Week -> LocalDateTime.Format {
                dayOfWeek(names = RUSSIAN_ABBREVIATED)
            }

            Period.Month -> LocalDateTime.Format {
                monthName(names = MonthNames.RUSSIAN_FULL)
            }

            Period.Year -> LocalDateTime.Format {
                year(padding = Padding.NONE)
            }
        }

        val xLabels: MutableMap<Float, String> = mutableMapOf()
        val lastTime = period.calculateRange(dateRange.endInclusive, timeZone).start

        var lastRange = period.calculateRange(dateRange.start, timeZone)

        do {
            val x = dateTransformerUseCase(lastRange.start)
            val name = lastRange.start.toLocalDateTime(timeZone).format(dateTimeFormatter)

            xLabels[x] = name
            lastRange = period.calculateRange(lastRange.endInclusive.plus(1.minutes), timeZone)
        } while (lastRange.start <= lastTime)

        val middleLine = areas[ReportEstimation.LOW]?.first()?.endInclusive?.let { top ->
            ReportEstimation.entries.asReversed().asSequence()
                .mapNotNull { level -> areas[level]?.drop(1)?.lastOrNull()?.start }
                .firstOrNull { bottom -> top >= bottom }
                ?.let { bottom -> (top + bottom) / 2f }
        }

        with(chart) {
            val yLabelRange = yLabels.first()..yLabels.last()

            areas.forEach { (estimation, areaList) ->
                areaList.forEachIndexed { index, area ->
                    val colors = estimation.color.colorValue

                    if (area.start in yLabelRange || area.endInclusive in yLabelRange) {
                        var yMaxBound = min(area.endInclusive.yChart, yLabels.last().yChart)
                        var yMinBound = max(area.start.yChart, yLabels.first().yChart)

                        middleLine?.let { line ->
                            if (index == 0) {
                                yMinBound = max(yMinBound, line.yChart)
                            } else {
                                yMaxBound = min(yMaxBound, line.yChart)
                            }
                        }

                        drawRect(
                            size = size.copy(height = yMaxBound - yMinBound),
                            topLeft = Offset(x = 0f, y = yMaxBound),
                            color = Color(
                                red = colors[0],
                                green = colors[1],
                                blue = colors[2],
                                alpha = 0.2f,
                            ),
                        )
                    }
                }
            }

            yLabels.forEach { yPos ->
                drawLine(
                    strokeWidth = 1f,
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(x = -5f, y = yPos.yChart),
                    end = Offset(x = size.width, y = yPos.yChart),
                )
                drawText(
                    font = font,
                    fontSize = 10f,
                    text = yPos.toInt().toString(),
                    offset = Offset(x = -30f, y = yPos.yChart - 4f),
                )
            }
            xLabels.forEach { (xPos, name) ->
                drawLine(
                    strokeWidth = 1f,
                    color = Color.LightGray,
                    start = Offset(x = xPos.xChart, y = yLabels.first().yChart - 5f),
                    end = Offset(x = xPos.xChart, y = size.height),
                    pathEffect = DashPathEffect(intervals = floatArrayOf(5f, 5f), phase = 0f)
                )

                name.split('\n').forEachIndexed { index, line ->
                    drawText(
                        text = line,
                        font = font,
                        fontSize = 6f,
                        offset = Offset(
                            x = xPos.xChart - 8f,
                            y = yLabels.first().yChart - 15f - (index * 8f)
                        ),
                    )
                }
            }
            positionMap.forEach { (i, positions) ->
                if (i % 2 == 0) {
                    Scatter(
                        radiusPoint = 5.dp,
                        positions = positions,
                        pointColor = Color.Blue,
                        rangeColor = Color.Blue.copy(alpha = 0.2f),
                    ).run {
                        draw(1f)
                    }
                } else {
                    @Suppress("UNCHECKED_CAST")
                    val points = positions as List<ChartPosition.Point>

                    CubicLine(
                        color = Color.Gray,
                        style = Stroke(width = 2f),
                        points = points,
                    ).run {
                        draw(1f)
                    }

                    points.forEach { point ->
                        val color = areas.firstNotNullOfOrNull { (estimation, areas) ->
                            if (areas.drop(if (i % 3 != 0) 0 else 1 ).any { area -> point.y in area }) {
                                val colors = estimation.color.colorValue

                                Color(
                                    red = colors[0],
                                    green = colors[1],
                                    blue = colors[2],
                                )
                            } else {
                                null
                            }
                        }

                        drawCircle(
                            radius = 4.dp.toPx(),
                            color = color ?: Color.Cyan,
                            center = Offset(x = point.x.xChart, y = point.y.yChart)
                        )
                    }
                }
            }
        }

        c.release()

        doc.add(Paragraph("").setMarginTop(pH + 70f))
        doc.add(
            Paragraph("Оценки измерений могут быть неточными для вашего организма. Рекомендуем обратиться к врачу, если у вас возникают затруднения")
                .setFont(font)
                .setFontSize(15f)
                .setFontColor(TEXT_MUTED)
                .setMarginTop(10f)
                .setMarginBottom(8f)
        )
    }

    private fun getPeriodByRange(
        dateRange: ClosedRange<Instant>
    ): Period {
        val dateRangeDuration = dateRange.endInclusive - dateRange.start

        return when {
            dateRangeDuration <= 1.days -> Period.OneHour
            dateRangeDuration <= 7.days -> Period.SixHour
            dateRangeDuration <= 31.days -> Period.Day
            else -> Period.Month
        }
    }


    private fun calculateMeasurementsData(
        period: Period,
        timeZone: TimeZone,
        measurements: List<Measurement>,
    ): Pair<Map<KClass<out Measurement>, List<MeasurementSection>>, List<MeasurementSummary>> {
        val measurementGroupsWithType = groupMeasurementByPeriodUseCase(
            period = period,
            timeZone = timeZone,
            measurements = measurements,
        )

        val measurementsSummaries = measurementGroupsWithType.values
            .mapNotNull { measurementGroups ->
                calculateMeasurementSummaryUseCase(measurementGroups = measurementGroups)
            }
        val measurementSections = measurementGroupsWithType.mapValues { (_, measurementGroups) ->
            measurementGroups.map { measurementGroup ->
                measurementGroup.asMeasurementSection(timeZone = timeZone)
            }
        }

        return measurementSections to measurementsSummaries
    }

    private fun Cell.defaultCell(
        padding: Float = 5f,
        backgroundColor: DeviceRgb? = null,
    ): Cell = setBackgroundColor(backgroundColor)
        .setPadding(padding)
        .setTextAlignment(TextAlignment.CENTER)

    private fun countParagraph(
        count: Int,
        fontSize: Float = 9f,
        font: PdfFont,
        fontColor: DeviceRgb? = null,
    ): Paragraph = Paragraph(if (count <= 0) "0" else "$count")
        .setFont(font)
        .setFontSize(fontSize)
        .run { fontColor?.let { color -> setFontColor(color) } ?: this }
        .run { if (count <= 0) setFontColor(TEXT_MUTED) else this }
}

private val RUSSIAN_ABBREVIATED: DayOfWeekNames = DayOfWeekNames(
    listOf(
        "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"
    )
)
