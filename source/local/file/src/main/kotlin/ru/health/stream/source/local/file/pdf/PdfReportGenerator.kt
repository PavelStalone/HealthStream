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
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.Note
import ru.health.stream.data.vitals.model.Period
import ru.health.stream.data.vitals.model.measurement.BloodGlucose
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.data.vitals.model.measurement.DiastolicPressure
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.data.vitals.model.measurement.Measurement
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.data.vitals.model.measurement.RespirationRate
import ru.health.stream.data.vitals.model.measurement.SystolicPressure
import ru.health.stream.data.vitals.usecase.DateTransformerUseCase
import ru.health.stream.feature.chart.core.drawable.CubicLine
import ru.health.stream.feature.chart.core.drawable.Scatter
import ru.health.stream.feature.chart.model.ChartPosition
import ru.health.stream.feature.chart.model.path.DashPathEffect
import ru.health.stream.source.local.file.ReportGenerator
import ru.health.stream.source.local.file.model.ACCENT
import ru.health.stream.source.local.file.model.Area
import ru.health.stream.source.local.file.model.HEADER_BG
import ru.health.stream.source.local.file.model.Mean
import ru.health.stream.source.local.file.model.MeasurementSection
import ru.health.stream.source.local.file.model.MeasurementSummary
import ru.health.stream.source.local.file.model.ReportEstimation
import ru.health.stream.source.local.file.model.STRIPE_BG
import ru.health.stream.source.local.file.model.TEXT_MUTED
import java.io.File
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal class PdfReportGenerator(
    @ApplicationContext private val context: Context,
) : ReportGenerator {

    private fun <T : Comparable<T>> ClosedRange<T>.changeRange(value: T): ClosedRange<T> {
        val newStart = if (value < start) value else start
        val newEnd = if (value > endInclusive) value else endInclusive

        return newStart..newEnd
    }

    private fun <T : Comparable<T>> ClosedRange<T>.changeRange(
        value: ClosedRange<T>
    ): ClosedRange<T> = changeRange(value.start).changeRange(value.endInclusive)

    private fun String?.merge(other: String?): String? = when {
        this == null -> other
        other == null -> this
        else -> "$this | $other"
    }

    private fun ReportEstimation?.changeByPriority(other: ReportEstimation?): ReportEstimation? =
        when {
            this == null -> other
            other == null -> this
            (ordinal < other.ordinal && other != ReportEstimation.NORMAL) -> other
            else -> this
        }

    private fun FloatFloatPair.changeByMin(other: FloatFloatPair): FloatFloatPair = when {
        first - second < other.first - other.second -> this
        else -> other
    }

    private fun FloatFloatPair.changeByMax(other: FloatFloatPair): FloatFloatPair = when {
        first - second > other.first - other.second -> this
        else -> other
    }

    override suspend fun generateFile(
        user: User?,
        outputFile: File,
        dateRange: ClosedRange<Instant>,
        measurements: List<Measurement>, // Уже отсортированный по created_at
    ): File {
        logV("generateFile called: ${measurements.size}")

        val timeZone = TimeZone.currentSystemDefault()

        val measurementsSummary: MutableMap<KClass<out MeasurementSection>, MeasurementSummary> =
            mutableMapOf()
        val measurementSections: MutableMap<KClass<out Measurement>, MutableList<MeasurementSection>> =
            mutableMapOf()

        val dateRangeDuration = dateRange.endInclusive - dateRange.start
        val period = when {
            dateRangeDuration <= 1.days -> Period.OneHour
            dateRangeDuration <= 7.days -> Period.SixHour
            dateRangeDuration <= 31.days -> Period.Day
            else -> Period.Month
        }

        measurements.forEach { measurement ->
            val measurementDateRange = period.calculateRange(
                date = measurement.createdAt,
                timeZone = timeZone,
            )

            val sections =
                measurementSections.getOrPut(key = measurement::class) { mutableListOf() }
            val lastSection = sections.lastOrNull()

            val mNote = measurement[Note]?.description
            val mEstimation = measurement[Estimation]?.let { estimation ->
                when (estimation.level) {
                    Estimation.Level.LOW -> ReportEstimation.LOW
                    Estimation.Level.NORMAL -> ReportEstimation.NORMAL
                    Estimation.Level.HIGH -> ReportEstimation.HIGH
                    Estimation.Level.EXTRA_HIGH -> ReportEstimation.EXTRA_HIGH
                }
            }

            val newSection =
                if (lastSection != null && lastSection.dateRange == measurementDateRange) {
                    sections.removeAt(sections.lastIndex)

                    with(lastSection) {
                        when (this) {
                            is MeasurementSection.BloodGlucose -> copy(
                                note = note.merge(mNote),
                                reportEstimation = reportEstimation.changeByPriority(mEstimation),
                                levelMean = levelMean.add((measurement as BloodGlucose).level),
                                levelRange = levelRange.changeRange(measurement.level),
                            )

                            is MeasurementSection.BloodPressure -> copy(
                                note = note.merge(mNote),
                                reportEstimation = reportEstimation.changeByPriority(mEstimation),
                                systolicMean = systolicMean.add((measurement as BloodPressure).systolic.toDouble()),
                                diastolicMean = systolicMean.add(measurement.diastolic.toDouble()),
                                systolicRange = systolicRange.changeRange(measurement.systolic),
                                diastolicRange = diastolicRange.changeRange(measurement.diastolic),
                                minBpByDifference = minBpByDifference.changeByMin(measurement.run {
                                    FloatFloatPair(systolic, diastolic)
                                }),
                                maxBpByDifference = maxBpByDifference.changeByMax(measurement.run {
                                    FloatFloatPair(systolic, diastolic)
                                }),
                            )

                            is MeasurementSection.BodyWeight -> copy(
                                note = note.merge(mNote),
                                reportEstimation = reportEstimation.changeByPriority(mEstimation),
                                weightMean = weightMean.add((measurement as BodyWeight).weight.kg.toDouble()),
                                weightRange = weightRange.changeRange(measurement.weight.kg),
                            )

                            is MeasurementSection.HeartRate -> copy(
                                note = note.merge(mNote),
                                reportEstimation = reportEstimation.changeByPriority(mEstimation),
                                pulseMean = pulseMean.add((measurement as HeartRate).pulse.toDouble()),
                                pulseRange = pulseRange.changeRange(measurement.pulse),
                            )

                            is MeasurementSection.OxygenSaturation -> copy(
                                note = note.merge(mNote),
                                reportEstimation = reportEstimation.changeByPriority(mEstimation),
                                saturationMean = saturationMean.add((measurement as OxygenSaturation).saturation.toDouble()),
                                saturationRange = saturationRange.changeRange(measurement.saturation),
                            )

                            is MeasurementSection.RespirationRate -> copy(
                                note = note.merge(mNote),
                                reportEstimation = reportEstimation.changeByPriority(mEstimation),
                                rateMean = rateMean.add((measurement as RespirationRate).rate),
                                rateRange = rateRange.changeRange(measurement.rate),
                            )
                        }
                    }
                } else {
                    with(measurement) {
                        when (this) {
                            is HeartRate -> MeasurementSection.HeartRate(
                                note = mNote,
                                timeZone = timeZone,
                                reportEstimation = mEstimation,
                                pulseRange = pulse..pulse,
                                dateRange = measurementDateRange,
                                pulseMean = Mean(pulse.toDouble()),
                            )

                            is BodyWeight -> MeasurementSection.BodyWeight(
                                note = mNote,
                                timeZone = timeZone,
                                reportEstimation = mEstimation,
                                dateRange = measurementDateRange,
                                weightMean = Mean(weight.kg.toDouble()),
                                weightRange = weight.kg..weight.kg,
                            )

                            is BloodGlucose -> MeasurementSection.BloodGlucose(
                                note = mNote,
                                timeZone = timeZone,
                                levelMean = Mean(level),
                                reportEstimation = mEstimation,
                                levelRange = level..level,
                                dateRange = measurementDateRange,
                            )

                            is BloodPressure -> MeasurementSection.BloodPressure(
                                note = mNote,
                                timeZone = timeZone,
                                reportEstimation = mEstimation,
                                dateRange = measurementDateRange,
                                systolicRange = systolic..systolic,
                                diastolicRange = diastolic..diastolic,
                                systolicMean = Mean(systolic.toDouble()),
                                diastolicMean = Mean(diastolic.toDouble()),
                                minBpByDifference = FloatFloatPair(systolic, diastolic),
                                maxBpByDifference = FloatFloatPair(systolic, diastolic),
                            )

                            is RespirationRate -> MeasurementSection.RespirationRate(
                                note = mNote,
                                timeZone = timeZone,
                                rateMean = Mean(rate),
                                rateRange = rate..rate,
                                reportEstimation = mEstimation,
                                dateRange = measurementDateRange,
                            )

                            is OxygenSaturation -> MeasurementSection.OxygenSaturation(
                                note = mNote,
                                timeZone = timeZone,
                                reportEstimation = mEstimation,
                                dateRange = measurementDateRange,
                                saturationMean = Mean(saturation.toDouble()),
                                saturationRange = saturation..saturation,
                            )

                            is SystolicPressure -> TODO()
                            is DiastolicPressure -> TODO()
                        }
                    }
                }

            sections.add(newSection)

            val summary = measurementsSummary[newSection::class]
            val newSummary = if (summary == null) {
                MeasurementSummary(
                    counts = 1,
                    section = newSection,
                    estimationsCount = mEstimation?.let { estimation -> mapOf(estimation to 1) }
                        ?: emptyMap()
                )
            } else {
                val summaryMap = summary.estimationsCount

                MeasurementSummary(
                    counts = summary.counts + 1,
                    section = with(summary.section) {
                        when (this) {
                            is MeasurementSection.BloodGlucose -> copy(
                                levelRange = levelRange.changeRange((newSection as MeasurementSection.BloodGlucose).levelRange),
                            )

                            is MeasurementSection.BloodPressure -> copy(
                                systolicRange = systolicRange.changeRange((newSection as MeasurementSection.BloodPressure).systolicRange),
                                diastolicRange = diastolicRange.changeRange(newSection.diastolicRange),
                                minBpByDifference = minBpByDifference.changeByMin(newSection.minBpByDifference),
                                maxBpByDifference = maxBpByDifference.changeByMax(newSection.maxBpByDifference),
                            )

                            is MeasurementSection.BodyWeight -> copy(
                                weightRange = weightRange.changeRange((newSection as MeasurementSection.BodyWeight).weightRange),
                            )

                            is MeasurementSection.HeartRate -> copy(
                                pulseRange = pulseRange.changeRange((newSection as MeasurementSection.HeartRate).pulseRange),
                            )

                            is MeasurementSection.OxygenSaturation -> copy(
                                saturationRange = saturationRange.changeRange((newSection as MeasurementSection.OxygenSaturation).saturationRange),
                            )

                            is MeasurementSection.RespirationRate -> copy(
                                rateRange = rateRange.changeRange((newSection as MeasurementSection.RespirationRate).rateRange),
                            )
                        }
                    },
                    estimationsCount = mEstimation?.let { estimation ->
                        summaryMap + (estimation to (summaryMap[estimation] ?: 0) + 1)
                    } ?: summaryMap
                )
            }

            measurementsSummary[newSection::class] = newSummary
        }

        PdfWriter(outputFile).use { writer ->
            PdfDocument(writer).use { pdf ->
                Document(pdf).use { doc ->
                    buildTitlePage(
                        pdf = pdf,
                        user = user,
                        timeZone = timeZone,
                        dateRange = dateRange,
                    )

                    doc.add(AreaBreak())
                    buildSummaryTable(
                        doc = doc,
                        summaries = measurementsSummary.values.toList(),
                    )

                    measurementSections.forEach { (_, sections) ->
                        doc.add(AreaBreak())
                        buildChartPage(
                            doc = doc,
                            pdf = pdf,
                            period = period,
                            timeZone = timeZone,
                            sections = sections,
                            dateRange = dateRange,
                            areas = mapOf(
                                ReportEstimation.LOW to listOf(Area(yRange = 0f..60f)),
                                ReportEstimation.NORMAL to listOf(Area(yRange = 60f..140f)),
                                ReportEstimation.HIGH to listOf(Area(yRange = 140f..170f)),
                                ReportEstimation.EXTRA_HIGH to listOf(Area(yRange = 170f..220f)),
                            )
                        )
                    }

                    measurementSections.forEach { (_, sections) ->
                        doc.add(AreaBreak())
                        buildBigTable(doc = doc, sections = sections.toList())
                    }
                }
            }
        }

        return outputFile
    }

    val font = PdfFontFactory.createFont(
        context.assets.open("fonts/arial.ttf").readBytes(),
        PdfEncodings.IDENTITY_H,
        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
    )!!
    val fontBold = PdfFontFactory.createFont(
        context.assets.open("fonts/arial_bolditalic.ttf").readBytes(),
        PdfEncodings.IDENTITY_H,
        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
    )!!

    // ── Title Page ────────────────────────────────────────────────────
    private fun buildTitlePage(
        user: User?,
        pdf: PdfDocument,
        timeZone: TimeZone,
        dateRange: ClosedRange<Instant>,
    ) {
        val dateNow = Clock.System.todayIn(timeZone)

        val page = pdf.addNewPage()
        val c = PdfCanvas(page.newContentStreamBefore(), page.resources, pdf)
        val w = pdf.defaultPageSize.width
        val h = pdf.defaultPageSize.height

        // Dark top block
        c.setFillColor(HEADER_BG)
        c.rectangle(0.0, h * 0.55, w.toDouble(), h * 0.45)
        c.fill()

        // Accent line
        c.setFillColor(ACCENT)
        c.rectangle(0.0, h * 0.55, w.toDouble(), 3.0)
        c.fill()

        // Decorative circles
        c.setStrokeColor(DeviceRgb(255, 255, 255))
        c.setLineWidth(1f)
        c.circle(w - 60.0, h - 80.0, 90.0)
        c.stroke()
        c.setLineWidth(0.5f)
        c.circle(w - 120.0, h - 180.0, 50.0)
        c.stroke()

        // Vertical accent
        c.setFillColor(ACCENT)
        c.rectangle(40.0, h * 0.42, 3.0, 120.0)
        c.fill()

        // Title text
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

        // Info block
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

        // Bottom text
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

    private fun Cell.defaultCell(
        padding: Float = 5f,
        backgroundColor: DeviceRgb? = null,
    ): Cell = setBackgroundColor(backgroundColor)
        .setPadding(padding)
        .setTextAlignment(TextAlignment.CENTER)

    private fun countParagraph(
        count: Int,
        fontSize: Float = 9f,
        font: PdfFont = this.font,
        fontColor: DeviceRgb? = null,
    ): Paragraph = Paragraph(if (count <= 0) "0" else "$count")
        .setFont(font)
        .setFontSize(fontSize)
        .run { fontColor?.let { color -> setFontColor(color) } ?: this }
        .run { if (count <= 0) setFontColor(TEXT_MUTED) else this }

    private fun buildSummaryTable(
        doc: Document,
        summaries: List<MeasurementSummary>,
    ) {
        doc.add(
            Paragraph("Сводка всех измерений")
                .setFont(fontBold).setFontSize(22f).setFontColor(HEADER_BG)
                .setMarginTop(20f).setMarginBottom(6f)
        )

        doc.add(
            Paragraph(
                "Здесь можно увидеть общее количество отклонений, рассчитанными алгоритмом"
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

            val cs = listOf(
                Cell().add(
                    Paragraph(summary.section.typeName)
                        .setFont(fontBold)
                        .setFontSize(9f)
                ).defaultCell(backgroundColor = backgroundColor),

                Cell().add(countParagraph(count = summary.counts))
                    .defaultCell(backgroundColor = backgroundColor),

                *ReportEstimation.entries.map { estimation ->
                    val count = summary.estimationsCount[estimation] ?: 0

                    Cell().add(
                        countParagraph(
                            count = count,
                            font = if (count > 0) fontBold else font,
                            fontColor = if (count > 0) estimation.color else null,
                        )
                    ).defaultCell(backgroundColor = backgroundColor)
                }.toTypedArray(),

                Cell().add(
                    Paragraph(summary.section.valueText)
                        .setFont(font)
                        .setFontSize(9f)
                ).defaultCell(backgroundColor = backgroundColor),
            )

            cs.forEach { table.addCell(it) }
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

            *ReportEstimation.entries.map { estimation ->
                val count = summaries.mapNotNull { cell -> cell.estimationsCount[estimation] }.sum()

                Cell().add(
                    countParagraph(
                        count = count,
                        font = fontBold,
                        fontColor = if (count > 0) estimation.color else null,
                    )
                ).defaultCell(backgroundColor = backgroundColor)
            }.toTypedArray(),

            Cell().defaultCell(backgroundColor = backgroundColor),
        )

        cs.forEach { table.addFooterCell(it) }

        doc.add(table)
    }

    // ── Big Multi-Page Table ──────────────────────────────────────────
    private fun buildBigTable(
        doc: Document,
        sections: List<MeasurementSection>,
    ) {
        val firstSection = sections.first()

        doc.add(
            Paragraph("Таблица измерений: ${firstSection.typeName}")
                .setFont(fontBold).setFontSize(22f).setFontColor(HEADER_BG)
                .setMarginTop(20f).setMarginBottom(6f)
        )

        doc.add(
            Paragraph("Таблица со всеми измерениями в отсортированном порядке. Оценка измерений в строке показывается сперва с самым высоким приоритетом за заданный период")
                .setFont(font)
                .setFontSize(10f)
                .setFontColor(TEXT_MUTED)
                .setMarginBottom(12f)
        )

        val cw = floatArrayOf(30f, 90f, 80f, 65f, 120f)

        var i = 1

        sections.chunked(size = 1000).forEachIndexed { index, chunk ->
            logV("Calculate table: $index section - ${i}")

            val table = Table(UnitValue.createPointArray(cw)).apply {
                setWidth(UnitValue.createPercentValue(100f))
            }

            for (h in listOf("#", "Время", "Значение", "Оценка", "Заметка")) {
                table.addHeaderCell(
                    Cell().add(
                        Paragraph(h).setFont(fontBold).setFontSize(9f)
                            .setFontColor(DeviceRgb(255, 255, 255))
                    )
                        .setBackgroundColor(HEADER_BG).setPadding(6f)
                        .setTextAlignment(TextAlignment.CENTER)
                )
            }

            chunk.forEach { section ->
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
                        .setBackgroundColor(backgroundColor)
                        .setPadding(5f)
                        .setTextAlignment(TextAlignment.CENTER),

                    Cell().add(
                        Paragraph(section.date)
                            .setFont(font)
                            .setFontSize(9f)
                    )
                        .setBackgroundColor(backgroundColor)
                        .setPadding(5f),

                    Cell().add(
                        Paragraph(section.valueText)
                            .setFont(font)
                            .setFontSize(9f)
                    )
                        .setBackgroundColor(backgroundColor)
                        .setPadding(5f)
                        .setTextAlignment(TextAlignment.CENTER),

                    Cell().add(
                        Paragraph(section.reportEstimation?.text ?: "")
                            .setFont(fontBold)
                            .setFontSize(9f)
                            .setFontColor(section.reportEstimation?.color)
                    )
                        .setBackgroundColor(backgroundColor)
                        .setPadding(5f)
                        .setTextAlignment(TextAlignment.CENTER),

                    Cell().add(
                        Paragraph(section.note ?: "")
                            .setFont(font)
                            .setFontSize(8f)
                            .setFontColor(TEXT_MUTED)
                    )
                        .setBackgroundColor(backgroundColor)
                        .setPadding(5f),
                )

                cs.forEach { table.addCell(it) }
                i++
            }

            doc.add(table)
        }
    }

    // ── Chart Page (PdfCanvas) ───────────────────────────────────────
    private fun buildChartPage(
        doc: Document,
        period: Period,
        pdf: PdfDocument,
        timeZone: TimeZone,
        dateRange: ClosedRange<Instant>,
        sections: List<MeasurementSection>,
        areas: Map<ReportEstimation, List<Area>> = emptyMap(),
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
            Paragraph("Данные представлены в группированном виде. Диапазоны показывают минимумы и максимумы значений, а линия их среднее арифметическое")
                .setFont(font)
                .setFontSize(10f)
                .setMarginBottom(16f)
                .setFontColor(TEXT_MUTED)
        )

        sections.forEach { section ->
            val x = dateTransformerUseCase(
                section.dateRange.start.plus((section.dateRange.endInclusive - section.dateRange.start) / 2)
            )

            when (section) {
                is MeasurementSection.BloodGlucose -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = section.levelMean.mean.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = section.levelRange.start.toFloat(),
                                second = section.levelRange.endInclusive.toFloat(),
                            )
                        )
                    )

                    yMin = min(section.levelRange.start.toFloat(), yMin)
                    yMax = max(section.levelRange.endInclusive.toFloat(), yMax)
                }

                is MeasurementSection.BodyWeight -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = section.weightMean.mean.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = section.weightRange.start,
                                second = section.weightRange.endInclusive,
                            )
                        )
                    )

                    yMin = min(section.weightRange.start, yMin)
                    yMax = max(section.weightRange.endInclusive, yMax)
                }

                is MeasurementSection.HeartRate -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = section.pulseMean.mean.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = section.pulseRange.start.toFloat(),
                                second = section.pulseRange.endInclusive.toFloat(),
                            )
                        )
                    )

                    yMin = min(section.pulseRange.start.toFloat(), yMin)
                    yMax = max(section.pulseRange.endInclusive.toFloat(), yMax)
                }

                is MeasurementSection.OxygenSaturation -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = section.saturationMean.mean.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = section.saturationRange.start,
                                second = section.saturationRange.endInclusive,
                            )
                        )
                    )

                    yMin = min(section.saturationRange.start, yMin)
                    yMax = max(section.saturationRange.endInclusive, yMax)
                }

                is MeasurementSection.RespirationRate -> {
                    val scatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val meanPositions = positionMap.getOrPut(1) { mutableListOf() }

                    meanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = section.rateMean.mean.toFloat(),
                        )
                    )
                    scatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = section.rateRange.start.toFloat(),
                                second = section.rateRange.endInclusive.toFloat(),
                            )
                        )
                    )

                    yMin = min(section.rateRange.start.toFloat(), yMin)
                    yMax = max(section.rateRange.endInclusive.toFloat(), yMax)
                }

                is MeasurementSection.BloodPressure -> {
                    val systolicScatterPositions = positionMap.getOrPut(0) { mutableListOf() }
                    val systolicMeanPositions = positionMap.getOrPut(1) { mutableListOf() }
                    val diastolicScatterPositions = positionMap.getOrPut(2) { mutableListOf() }
                    val diastolicMeanPositions = positionMap.getOrPut(3) { mutableListOf() }

                    systolicMeanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = section.systolicMean.mean.toFloat(),
                        )
                    )
                    diastolicMeanPositions.add(
                        ChartPosition.Point(
                            x = x,
                            y = section.diastolicMean.mean.toFloat(),
                        )
                    )
                    systolicScatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = section.systolicRange.start,
                                second = section.systolicRange.endInclusive,
                            )
                        )
                    )
                    diastolicScatterPositions.add(
                        ChartPosition.Range.Vertical(
                            x = x,
                            y = FloatFloatPair(
                                first = section.diastolicRange.start,
                                second = section.diastolicRange.endInclusive,
                            )
                        )
                    )

                    yMin = min(section.systolicRange.start, yMin)
                    yMin = min(section.diastolicRange.start, yMin)
                    yMax = max(section.systolicRange.endInclusive, yMax)
                    yMax = max(section.diastolicRange.endInclusive, yMax)
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
        val yLabels = generateSequence(seed = start) { it + step }
            .takeWhile { it <= end }
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
                dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            }

            Period.Month -> LocalDateTime.Format {
                monthName(names = MonthNames.RUSSIAN_FULL)
            }

            Period.Year -> LocalDateTime.Format {
                year(padding = Padding.NONE)
            }
        }
        val xLabels: MutableMap<Float, String> = mutableMapOf()

        var lastRange = period.calculateRange(dateRange.start, timeZone)
        val lastTime = period.calculateRange(dateRange.endInclusive, timeZone).start

        do {
            val x = dateTransformerUseCase(lastRange.start)
            val name = lastRange.start.toLocalDateTime(timeZone).format(dateTimeFormatter)
            xLabels[x] = name

            lastRange = period.calculateRange(lastRange.endInclusive.plus(1.minutes), timeZone)
        } while (lastRange.start <= lastTime)

        with(chart) {
            val yLabelRange = yLabels.first()..yLabels.last()

            areas.forEach { (estimation, areaList) ->
                areaList.forEach { area ->
                    val colors = estimation.color.colorValue

                    if (area.yRange.start in yLabelRange || area.yRange.endInclusive in yLabelRange) {
                        val yMaxBound = min(area.yRange.endInclusive.yChart, yLabels.last().yChart)
                        val yMinBound = max(area.yRange.start.yChart, yLabels.first().yChart)

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
                            if (areas.any { area -> point.y in area.yRange }) {
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
            Paragraph("Оценки измерений могут быть не точными для вашего организма. Рекомендуем обратиться к врачу, если у вас возникают трудности")
                .setFont(font)
                .setFontSize(15f)
                .setFontColor(TEXT_MUTED)
                .setMarginTop(10f)
                .setMarginBottom(8f)
        )
    }
}
