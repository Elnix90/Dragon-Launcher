package org.elnix.dragonlauncher.base.model.models
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames

public object DateTimeFormats {
    public val dateShort: DateTimeFormat<LocalDate> =
        LocalDate.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
        }

    public val dateMedium: DateTimeFormat<LocalDate> =
        LocalDate.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(", ")
            year()
        }

    public val dateLong: DateTimeFormat<LocalDate> =
        LocalDate.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
            chars(", ")
            monthName(MonthNames.ENGLISH_FULL)
            chars(" ")
            day()
            chars(", ")
            year()
        }

    public val dateIso: DateTimeFormat<LocalDate> =
        LocalDate.Format {
            year()
            chars("-")
            monthNumber()
            chars("-")
            day()
        }

    public val dateUs: DateTimeFormat<LocalDate> =
        LocalDate.Format {
            monthNumber()
            chars("/")
            day()
            chars("/")
            year()
        }

    public val dateEu: DateTimeFormat<LocalDate> =
        LocalDate.Format {
            day()
            chars("/")
            monthNumber()
            chars("/")
            year()
        }

    public val time12Hour: DateTimeFormat<LocalTime> =
        LocalTime.Format {
            amPmHour()
            chars(":")
            minute()
            chars(" ")
            amPmMarker("AM", "PM")
        }

    public val time24Hour: DateTimeFormat<LocalTime> =
        LocalTime.Format {
            hour()
            chars(":")
            minute()
        }

    public val time12HourSeconds: DateTimeFormat<LocalTime> =
        LocalTime.Format {
            amPmHour()
            chars(":")
            minute()
            chars(":")
            second()
            chars(" ")
            amPmMarker("AM", "PM")
        }

    public val time24HourSeconds: DateTimeFormat<LocalTime> =
        LocalTime.Format {
            hour()
            chars(":")
            minute()
            chars(":")
            second()
        }

    public val time12HourShort: DateTimeFormat<LocalTime> =
        LocalTime.Format {
            amPmHour()
            chars(":")
            minute()
            chars(" ")
            amPmMarker("AM", "PM")
        }

    public val time24HourShort: DateTimeFormat<LocalTime> =
        LocalTime.Format {
            hour()
            chars(":")
            minute()
        }
}

public enum class DateFormat(
    public val pattern: String,
    public val format: DateTimeFormat<LocalDate>
) {
    Short("MMM dd", DateTimeFormats.dateShort),
    Medium("MMM dd, yyyy", DateTimeFormats.dateMedium),
    Long("EEEE, MMMM dd, yyyy", DateTimeFormats.dateLong),
    Iso("yyyy-MM-dd", DateTimeFormats.dateIso),
    Us("MM/dd/yyyy", DateTimeFormats.dateUs),
    Eu("dd/MM/yyyy", DateTimeFormats.dateEu),
    Custom("", DateTimeFormats.dateShort)
}

public enum class TimeFormat(
    public val pattern: String,
    public val format: DateTimeFormat<LocalTime>
) {
    H12("hh:mm a", DateTimeFormats.time12Hour),
    H24("HH:mm", DateTimeFormats.time24Hour),
    H12Seconds("hh:mm:ss a", DateTimeFormats.time12HourSeconds),
    H24Seconds("HH:mm:ss", DateTimeFormats.time24HourSeconds),
    H12Short("h:mm a", DateTimeFormats.time12HourShort),
    H24Short("H:mm", DateTimeFormats.time24HourShort),
    Custom("", DateTimeFormats.time24Hour)
}
