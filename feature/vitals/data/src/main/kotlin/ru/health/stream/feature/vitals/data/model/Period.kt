package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.Padding
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Интерфейс, представляющий временной период для группировки и отображения данных
 *
 * Позволяет вычислять временные диапазоны и получать метки для осей графиков
 */
sealed interface Period {

    /**
     * Вычисляет временной диапазон (начало и конец) для заданного момента времени [date]
     * с учетом часового пояса [timeZone]
     *
     * @param date Точка во времени, входящая в искомый период
     * @param timeZone Часовой пояс для корректного расчета границ периода
     * @return Диапазон [ClosedRange] от начала до конца периода
     */
    fun calculateRange(date: Instant, timeZone: TimeZone): ClosedRange<Instant>

    /**
     * Представляет период в один день (24 часа)
     */
    data object Day : Period {

        /**
         * Генерирует карту меток времени для отображения
         *
         * @param partsCount Количество меток (частей), на которые нужно разбить день
         * @param format Лямбда-выражение для настройки формата отображения времени
         * @return Карта, где ключ - позиция от 0.0 до 1.0, а значение - отформатированная строка времени
         */
        fun getDisplay(
            partsCount: Int = 6,
            format: DateTimeFormatBuilder.WithTime.() -> Unit = { hour(padding = Padding.NONE) },
        ): Map<Float, String> {
            val totalMinutesInDay = 24 * 60
            val minuteStep = totalMinutesInDay.toFloat() / partsCount
            val timeFormatter = LocalTime.Format(builder = format)

            return (0..partsCount).associate { index ->
                val totalMinutes = (index * minuteStep).toInt()
                val position = totalMinutes.toFloat() / totalMinutesInDay

                val label = if (index == partsCount && totalMinutes >= totalMinutesInDay) {
                    val formatted = LocalTime(0, 0).format(timeFormatter)

                    when {
                        formatted.startsWith("00") -> formatted.replaceFirst("00", "24")
                        formatted.startsWith("0") -> formatted.replaceFirst("0", "24")
                        else -> formatted
                    }
                } else {
                    val timeMinutes = totalMinutes % totalMinutesInDay
                    val time = LocalTime(
                        hour = (timeMinutes / 60),
                        minute = timeMinutes % 60
                    )

                    time.format(timeFormatter)
                }

                position to label
            }
        }

        override fun calculateRange(
            date: Instant,
            timeZone: TimeZone
        ): ClosedRange<Instant> {
            val localDate = date.toLocalDateTime(timeZone).date
            val start = localDate.atStartOfDayIn(timeZone)
            val end = start.plus(1, DateTimeUnit.DAY, timeZone)

            return start..end
        }
    }

    /**
     * Представляет период в одну неделю (7 дней)
     *
     * @property firstDayOfWeek День недели, с которого начинается отсчет
     */
    data class Week(val firstDayOfWeek: DayOfWeek) : Period {

        /**
         * Генерирует карту названий дней недели
         *
         * @param locale Локаль для перевода названий дней
         * @param textStyle Стиль текста (например, короткий "Пн" или полный "Понедельник")
         * @return Карта, где ключ - позиция дня в неделе (0.0..1.0), а значение - название дня
         */
        fun getDisplay(
            locale: Locale,
            textStyle: TextStyle = TextStyle.SHORT,
        ): Map<Float, String> {
            val daysInWeek = 7

            return (0 until daysInWeek).associate { dayOffset ->
                val position = dayOffset.toFloat() / daysInWeek
                val displayName = firstDayOfWeek
                    .plus(dayOffset.toLong())
                    .getDisplayName(textStyle, locale)

                position to displayName
            }
        }

        override fun calculateRange(
            date: Instant,
            timeZone: TimeZone
        ): ClosedRange<Instant> {
            val localDate = date.toLocalDateTime(timeZone).date

            val offset = (localDate.dayOfWeek.ordinal - firstDayOfWeek.ordinal)
                .let { if (it < 0) it + 7 else it }

            val start = localDate.minus(offset, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
            val end = start.plus(7, DateTimeUnit.DAY, timeZone)

            return start..end
        }
    }

    /**
     * Представляет период в один календарный месяц
     */
    data object Month : Period {

        /**
         * Генерирует карту меток дней месяца, соответствующих определенному дню недели
         *
         * @param date Точка во времени для определения конкретного месяца
         * @param locale Локаль для перевода названия месяца
         * @param timeZone Часовой пояс
         * @param firstDayOfWeek День недели, который нужно отображать (например, каждый понедельник)
         * @param textStyle Стиль текста для названия месяца (например, короткий "мар.")
         * @return Карта, где ключ - позиция дня (0.0..1.0), а значение - строка вида "1 мар."
         */
        fun getDisplay(
            date: Instant,
            locale: Locale,
            timeZone: TimeZone,
            firstDayOfWeek: DayOfWeek,
            textStyle: TextStyle? = null,
        ): Map<Float, String> {
            val localDate = date.toLocalDateTime(timeZone).date
            val month = localDate.month

            val startOfMonth = LocalDate(localDate.year, month, 1)
            val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH)
            val totalDaysInMonth = endOfMonth.toEpochDays() - startOfMonth.toEpochDays()

            val dayOffset = (firstDayOfWeek.ordinal - startOfMonth.dayOfWeek.ordinal + 7) % 7
            val firstOccurrenceDay = dayOffset + 1

            val monthDisplayName = textStyle?.let { month.getDisplayName(it, locale) }

            return (firstOccurrenceDay..totalDaysInMonth step 7).associate { dayOfMonth ->
                val position = (dayOfMonth - 1).toFloat() / totalDaysInMonth
                val label = monthDisplayName?.let { "$dayOfMonth $it" } ?: dayOfMonth.toString()

                position to label
            }
        }

        override fun calculateRange(
            date: Instant,
            timeZone: TimeZone
        ): ClosedRange<Instant> {
            val localDate = date.toLocalDateTime(timeZone).date

            val start = LocalDate(localDate.year, localDate.month, 1).atStartOfDayIn(timeZone)
            val end = start.plus(1, DateTimeUnit.MONTH, timeZone)

            return start..end
        }
    }

    /**
     * Представляет период в один календарный год
     */
    data object Year : Period {

        /**
         * Генерирует карту названий месяцев в году
         *
         * @param locale Локаль для перевода названий месяцев
         * @param textStyle Стиль текста (например, "Янв" или "Январь")
         * @param monthStep Шаг отображения месяцев (например, 3 для квартального отображения)
         * @return Карта, где ключ - позиция месяца (0.0..1.0), а значение - его название
         */
        fun getDisplay(
            locale: Locale,
            textStyle: TextStyle = TextStyle.SHORT,
            monthStep: Int = 2,
        ): Map<Float, String> {
            val monthsInYear = 12

            return (1..monthsInYear step monthStep).associate { monthValue ->
                val month = kotlinx.datetime.Month.entries[monthValue - 1]
                val position = (monthValue - 1).toFloat() / monthsInYear

                position to month.getDisplayName(textStyle, locale)
            }
        }

        override fun calculateRange(
            date: Instant,
            timeZone: TimeZone
        ): ClosedRange<Instant> {
            val localDate = date.toLocalDateTime(timeZone).date

            val start = LocalDate(localDate.year, 1, 1).atStartOfDayIn(timeZone)
            val end = start.plus(1, DateTimeUnit.YEAR, timeZone)

            return start..end
        }
    }
}
