package edu.bluejack22_1.fidertime.common

import android.content.Context
import android.icu.text.RelativeDateTimeFormatter
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class RelativeDateAdapter(private val date: Date) {

    fun getRelativeString(): String {
        return DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString()
    }

    fun getFullRelativeString(): String {
        return DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.MINUTE_IN_MILLIS).toString()
    }

    fun getFullRelativeString(toDate: Date): String {
        return DateUtils.getRelativeTimeSpanString(date.time, toDate.time, DateUtils.MINUTE_IN_MILLIS).toString()
    }

    fun getHourMinuteFormat(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return formatter.format(date)
    }

    fun getDateFormat(): String {
        val formatter = SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)
        return formatter.format(date)
    }

    fun getTimeFormat(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return formatter.format(date)
    }

}