package com.example.newsapp.db

import androidx.room.TypeConverter
import com.example.newsapp.models.Source

/**
 * Contains methods for converting a custom type [Source] to a primitive, [String], which can be used with *retrofit (JsonConverter)*
 */
class Converters {

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}