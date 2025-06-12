package eu.kanade.tachiyomi.extension.zh.copy20

object Constants {

    var apiUrl = "https://mapi.copy20.com"
    var baseUrl = "https://www.copy20.com"

    const val LIMIT = 30
    const val LIMIT_LARGE = 500

    fun offset(page: Int, limit: Int = LIMIT) = (page - 1) * limit

    const val GENRE_FILTER_LABEL = "题材"

    const val SEARCH_FILTER_LABEL = "搜索范围"
    val SEARCH_FILTER = arrayOf(
        "全部" to "",
        "名称" to "name",
        "作者" to "author",
        "汉化组" to "local"
    )

    const val RANK_FILTER_LABEL = "排行榜"
    val RANK_FILTER = arrayOf(
        "不查看" to "",
        "日榜(上升最快)" to "day",
        "周榜(最近7天)" to "week",
        "月榜(最近30天)" to "month",
        "总榜单(即热门排序)" to "total"
    )

    const val RANK_WEB_FILTER_LABEL = "排行榜"
    val RANK_WEB_FILTER = arrayOf(
        "不查看" to "",
        "日榜(上升最快)" to "day",
        "周榜(最近7天)" to "week",
        "月榜(最近30天)" to "month",
        "总榜单" to "total"
    )

    const val RANK_TYPE_FILTER_LABEL = "类型"
    val RANK_TYPE_FILTER = arrayOf(
        "男频" to "male",
        "女频" to "female"
    )

    const val SORT_FILTER_LABEL = "排序"
    val SORT_FILTER = arrayOf(
        "热门" to "popular",
        "更新时间" to "datetime_updated"
    )

    const val REGION_FILTER_LABEL = "地区/状态"
    val REGION_FILTER = arrayOf(
        "全部" to "",
        "日本" to "japan",
        "韩国" to "korea",
        "欧美" to "west",
        "已完结" to "finish"
    )

    const val REGION_WEB_FILTER_LABEL = "地区"
    val REGION_WEB_FILTER = arrayOf(
        "全部" to "",
        "日漫" to "0",
        "韩漫" to "1",
        "美漫" to "2",
    )

    const val STATUS_FILTER_LABEL = "状态"
    val STATUS_FILTER = arrayOf(
        "全部" to "",
        "连载中" to "0",
        "已完结" to "1",
        "短篇" to "2",
    )

    const val MANGA_URL_PREFIX = "/comic/"
    const val MANGA_URL_PREFIX_2 = "/comic2/"
    const val CHAPTER_URL_PREFIX = "/chapter/"
    const val CHAPTER_URL_PREFIX_2 = "/chapter2/"

    const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"

    val RESOLUTION_REGEX = "\\d+(?=x\\.(?:jpg|webp)$)".toRegex()

}
