package eu.kanade.tachiyomi.extension.zh.copy20

import android.content.SharedPreferences

@Suppress("PropertyName")
sealed class Preference<T>(
    val KEY: String,
    val DEFAULT: T
) {

    data object FetchByWeb : Preference<Boolean>(
        "get_by_web",
        false
    )

    data object Resolution : Preference<String>(
        "resolution",
        Resolutions[0]
    )

    data object Translate : Preference<Boolean>(
        "translate",
        true
    )

    data object OnlyDefault : Preference<Boolean>(
        "only_default",
        false
    )

    data object OnlyDefaultOppositeList : Preference<String>(
        "only_default_opposite_list",
        ""
    )

    data object UserAgent : Preference<String>(
        "user_agent",
        Constants.DEFAULT_USER_AGENT
    )

    data object Cookies : Preference<String>(
        "cookies",
        ""
    )

}

fun SharedPreferences.getBoolean(preference: Preference<Boolean>): Boolean =
    getBoolean(preference.KEY, preference.DEFAULT)!!

fun SharedPreferences.getString(preference: Preference<String>): String =
    getString(preference.KEY, preference.DEFAULT)!!

fun SharedPreferences.getStringSet(preference: Preference<Set<String>>): Set<String> =
    getStringSet(preference.KEY, preference.DEFAULT)!!
