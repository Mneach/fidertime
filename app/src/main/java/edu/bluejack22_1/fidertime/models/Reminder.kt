package edu.bluejack22_1.fidertime.models

import edu.bluejack22_1.fidertime.common.RelativeDateAdapter
import java.util.Date

data class Reminder(
    var title: String = "",
    var date: Date = Date(),
    var remindBefore: ArrayList<Date> = arrayListOf(),
) {
    override fun toString(): String {

        val relativeDate = RelativeDateAdapter(date).getFullRelativeString()

        var remindBeforeText = ""
        for (i in 0 until remindBefore.size) {
            remindBeforeText += RelativeDateAdapter(date).getFullRelativeString(remindBefore[i])
            if (i < remindBefore.size - 1) {
                remindBeforeText += ", "
            }
        }

        return "$title $relativeDate, remind before deadline $remindBeforeText"
    }
}