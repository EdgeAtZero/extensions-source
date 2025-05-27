package eu.kanade.tachiyomi.lib.json

inline fun <R> buildJsonParsing(block: ParsingScope.() -> R): R =
    try {
        block(ParsingScope.INSTANCE)
    } catch (e: NullPointerException) {
        throw IllegalArgumentException("parsing json failed", e)
    }
