package app.dinar.core.database.util

import androidx.room.TypeConverter
import java.time.Instant

internal class InstantConverters {
    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilli()
}
