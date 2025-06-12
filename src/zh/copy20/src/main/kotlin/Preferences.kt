package eu.kanade.tachiyomi.extension.zh.copy20

import android.content.SharedPreferences

@Suppress("PropertyName")
sealed class Preferences<T>(
    val KEY: String,
    val DEFAULT: T
) {

    data object FetchByWeb : Preferences<Boolean>(
        "get_by_web",
        false
    )

    data object Resolution : Preferences<String>(
        "resolution",
        Resolutions[0]
    )

    data object Translate : Preferences<Boolean>(
        "translate",
        true
    )

    data object OnlyDefault : Preferences<Boolean>(
        "only_default",
        false
    )

    data object OnlyDefaultOppositeList : Preferences<String>(
        "only_default_opposite_list",
        ""
    )

    data object UserAgent : Preferences<String>(
        "user_agent",
        Constants.DEFAULT_USER_AGENT
    )

    data object Cookies : Preferences<String>(
        "cookies",
        ""
    )

}

fun SharedPreferences.getBoolean(preference: Preferences<Boolean>): Boolean =
    getBoolean(preference.KEY, preference.DEFAULT)!!

fun SharedPreferences.getString(preference: Preferences<String>): String =
    getString(preference.KEY, preference.DEFAULT)!!

fun SharedPreferences.getStringSet(preference: Preferences<Set<String>>): Set<String> =
    getStringSet(preference.KEY, preference.DEFAULT)!!
