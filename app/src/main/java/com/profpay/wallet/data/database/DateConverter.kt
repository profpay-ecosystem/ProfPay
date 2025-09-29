package com.profpay.wallet.data.database

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun toDate(timestamp: Long): Date = Date(timestamp)

    @TypeConverter
    fun toTimestamp(date: Date): Long = date.time
}
