package eu.kanade.tachiyomi.extension.zh.copy20

import okhttp3.internal.toImmutableList

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
object Resolutions : List<String> by listOf("800", "1200", "1500").toImmutableList()
