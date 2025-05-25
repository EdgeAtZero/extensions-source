package eu.kanade.tachiyomi.extension.zh.copymanga

object Constants {

    var apiUrl = "https://mapi.copy20.com"
    var baseUrl = "https://www.copy20.com"

    const val MANGA_URL_PREFIX = "/comic/"
    const val MANGA_URL_PREFIX_2 = "/comic2/"
    const val CHAPTER_URL_PREFIX = "/chapter2/"
    const val CHAPTER_URL_PREFIX_2 = "/chapter2/"

    const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"

    val RESOLUTION_REGEX = "\\d+(?=x\\.(?:jpg|webp)$)".toRegex()

}
