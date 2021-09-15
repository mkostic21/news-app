package com.example.newsapp.util

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import java.util.*

object FlagKit {

    fun getDrawable(context: Context, flagName: String): Drawable? {
        val resourceId = getResId(context, flagName)
        return try {
            ContextCompat.getDrawable(context, resourceId)!!
        } catch (e: Resources.NotFoundException){
            null
        }

    }

    fun getResId(context: Context, flagName: String): Int {
        return context.resources.getIdentifier("ic_"+fixResId(flagName), "drawable", context.packageName)
    }

    private fun fixResId(resId: String): String{
        return resId.lowercase(Locale.ENGLISH)
    }

    fun getAvailableCodes(): List<String>{
        return listOf(
            "ae",
            "ar",
            "at",
            "au",
            "be",
            "bg",
            "br",
            "ca",
            "ch",
            "cn",
            "co",
            "cu",
            "cz",
            "de",
            "eg",
            "fr",
            "gb",
            "gr",
            "hk",
            "hu",
            "id",
            "ie",
            "il",
            "in",
            "it",
            "jp",
            "kr",
            "lt",
            "lv",
            "ma",
            "mx",
            "my",
            "ng",
            "nl",
            "no",
            "nz",
            "ph",
            "pl",
            "pt",
            "ro",
            "rs",
            "ru",
            "sa",
            "se",
            "sg",
            "si",
            "sk",
            "th",
            "tr",
            "tw",
            "ua",
            "us",
            "ve",
            "za"
        )
    }


}