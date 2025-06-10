@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("DuplicatedCode")

package eu.kanade.tachiyomi.extension.zh.copy20

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.CHAPTER_URL_PREFIX
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.CHAPTER_URL_PREFIX_2
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.MANGA_URL_PREFIX
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.MANGA_URL_PREFIX_2
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.apiUrl
import eu.kanade.tachiyomi.lib.json.buildJsonParsing
import eu.kanade.tachiyomi.lib.json.getJsonObject
import eu.kanade.tachiyomi.lib.json.getString
import eu.kanade.tachiyomi.lib.t2s.T2S
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Element
import rx.Observable
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


@Suppress("SameParameterValue")
class Copy20 : ConfigurableSource, HttpSource() {
    override val lang = "zh"
    override val supportsLatest = true
    override val name = "拷貝漫畫"
    override val baseUrl get() = Constants.baseUrl

    private val preferences: SharedPreferences
    private var fetchByWeb by Delegates.notNull<Boolean>()
    private var resolution by Delegates.notNull<String>()
    private var translate by Delegates.notNull<Boolean>()
    private var onlyDefault by Delegates.notNull<Boolean>()
    private var apiHeaders by Delegates.notNull<Headers>()
    private var webHeaders by Delegates.notNull<Headers>()
    private val onlyDefaultOppositeList = mutableListOf<String>()
    private val t2sTransform: (String) -> String = { if (translate) T2S.convert(it) else it }

    private var isRefreshGenreFailed = false

    init {
        val application = Injekt.get<Application>()
        @SuppressLint("WrongConstant")
        preferences = application.getSharedPreferences("source_$id", Context.MODE_PRIVATE)
        updatePreferences()
        preferences.registerOnSharedPreferenceChangeListener { _, _ -> updatePreferences() }
        Thread(::updateGenres).start()
    }

    private fun updatePreferences() {
        fetchByWeb = preferences.getBoolean(Preference.FetchByWeb)
        resolution = preferences.getString(Preference.Resolution)
        translate = preferences.getBoolean(Preference.Translate)
        onlyDefault = preferences.getBoolean(Preference.OnlyDefault)
        onlyDefaultOppositeList.clear()
        onlyDefaultOppositeList += preferences.getString(Preference.OnlyDefaultOppositeList).trim().lines()
        val baseHeaders = with(Headers.Builder()) {
            add("Cookie", preferences.getString(Preference.Cookies))
            add("User-Agent", preferences.getString(Preference.UserAgent))
            build()
        }
        apiHeaders = with(baseHeaders.newBuilder()) {
            add("Accept", "application/json")
            build()
        }
        webHeaders = with(baseHeaders.newBuilder()) {
            add("Accept", "text/html")
            build()
        }
    }

    private fun updateGenres() {
        try {
            val request = GET(url = "${apiUrl}/api/v3/theme/comic/count?limit=500", headers = apiHeaders)
            val response = client.newCall(request).execute()
            genreFilter = buildJsonParsing {
                val init = mutableListOf("全部" to "")
                Json.decodeFromStream<JsonObject>(response.body.byteStream()).jsonObject("results").jsonArray("list")
                    .mapTo(init) { it.jsonObject.string("name").let(t2sTransform) to it.jsonObject.string("path_word") }
                    .toTypedArray()
            }
            isRefreshGenreFailed = false
        } catch (_: Exception) {
            isRefreshGenreFailed = true
        }
    }

    override fun imageRequest(page: Page): Request = GET(
        url = Constants.RESOLUTION_REGEX.replaceFirst(requireNotNull(page.imageUrl), resolution),
        headers = apiHeaders
    )

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        with(SwitchPreferenceCompat(screen.context)) {
            key = Preference.FetchByWeb.KEY
            title = "部分数据从网页获取"
            summary = "当你访问手机端网页提示下载客户端时使用\n注意：获取漫画章节时会丢失上传时间"
            setDefaultValue(Preference.FetchByWeb.DEFAULT)
            screen.addPreference(this)
        }
        with(SwitchPreferenceCompat(screen.context)) {
            key = Preference.Translate.KEY
            title = "繁体转简体"
            setDefaultValue(Preference.Translate.DEFAULT)
            screen.addPreference(this)
        }
        with(ListPreference(screen.context)) {
            key = Preference.Resolution.KEY
            title = "图片分辨率 (像素)"
            summary = "阅读过的部分需要清空缓存才能生效"
            entries = Resolutions.toTypedArray()
            entryValues = entries
            setDefaultValue(Preference.Resolution.DEFAULT)
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preference.UserAgent.KEY
            title = "UserAgent（需要非手机版的）"
            setDefaultValue(Preference.UserAgent.DEFAULT)
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preference.Cookies.KEY
            title = "Cookies"
            setDefaultValue(Preference.Cookies.DEFAULT)
            screen.addPreference(this)
        }
        with(SwitchPreferenceCompat(screen.context)) {
            key = Preference.OnlyDefault.KEY
            title = "只保留默认"
            summary = "漫画章节列表只保留默认，不获取单行本等其他的"
            setDefaultValue(Preference.OnlyDefault.DEFAULT)
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preference.OnlyDefaultOppositeList.KEY
            title = "只保留默认相反列表"
            summary =
                "如果上面的选项开了，这个就是上面的功能的禁用列表，否则就是单独的启用列表（一行一个漫画名称，注意简繁，建议直接复制）"
            setDefaultValue(Preference.OnlyDefaultOppositeList.DEFAULT)
            screen.addPreference(this)
        }
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> =
        if (fetchByWeb) client
            .newCall(
                GET(
                    url = "$baseUrl/comicdetail/${manga.url.removePrefix(MANGA_URL_PREFIX)}/chapters",
                    headers = apiHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                val comic = manga.url
                val isContained = onlyDefaultOppositeList.contains(manga.title)
                val shouldAll = onlyDefault && isContained || !onlyDefault && !isContained
                buildJsonParsing {
                    Json.decodeFromString<JsonObject>(response.body.string())
                        .string("results")
                        .let(Crypto::decrypt)
                        .let { Json.decodeFromString<JsonObject>(it) }
                        .jsonObject("groups")
                        .map { (key, value) ->
                            val jsonObject = value.jsonObject
                            val default = key == "default"
                            val scan = if (default) null else jsonObject.string("name")
                            if (shouldAll || default) {
                                jsonObject.jsonArray("chapters").map { parseChapter(it.jsonObject, comic, scan) }
                            } else {
                                emptyList()
                            }
                        }
                        .flatten()
                        .asReversed()
                }
            }
        else Observable.fromCallable {
            buildList {
                val groups = mutableMapOf<String, String?>("default" to null)
                val isContained = onlyDefaultOppositeList.contains(manga.title)
                val shouldAll = onlyDefault && isContained || !onlyDefault && !isContained
                if (shouldAll) {
                    val response = client.newCall(mangaDetailsRequest(manga)).execute()
                    buildJsonParsing {
                        Json.decodeFromStream<JsonObject>(response.body.byteStream()).jsonObject("results")
                            .jsonObject("groups").filterNot { it.key in groups }
                            .forEach { groups += it.value.jsonObject.string("path_word") to it.value.jsonObject.string("name") }
                    }
                }
                for ((path, name) in groups) {
                    val limit = 500
                    var offset = 0
                    var total = -1
                    var loop = true
                    while (loop) {
                        val request = GET(
                            url = "${apiUrl}/api/v3${manga.url}/group/${path}/chapters?limit=$limit&offset=$offset",
                            headers = apiHeaders
                        )
                        val response = client.newCall(request).execute()
                        buildJsonParsing {
                            val results = Json.decodeFromStream<JsonObject>(response.body.byteStream())
                                .jsonObject("results")
                            results.jsonArray("list")
                                .forEach { add(0, parseChapter(it.jsonObject, name)) }
                            if (total == -1) {
                                total = results.int("total")
                            }
                            loop = offset + limit < total
                            offset += limit
                        }
                    }

                }
            }
        }

    override fun chapterListRequest(manga: SManga): Request =
        throw UnsupportedOperationException()

    override fun chapterListParse(response: Response): List<SChapter> =
        throw UnsupportedOperationException()

    override fun imageUrlRequest(page: Page): Request =
        throw UnsupportedOperationException()

    override fun imageUrlParse(response: Response): String =
        throw UnsupportedOperationException()

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> =
        if (fetchByWeb) client
            .newCall(
                GET(
                    url = "$baseUrl${manga.url}",
                    headers = webHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                parseComicDetail(response.asJsoup(), manga)
            }
        else client
            .newCall(
                GET(
                    url = "${apiUrl}/api/v3${manga.url.replace(MANGA_URL_PREFIX, MANGA_URL_PREFIX_2)}",
                    headers = apiHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                buildJsonParsing {
                    Json.decodeFromStream<JsonObject>(response.body.byteStream())
                        .jsonObject("results")
                        .let { parseComicDetail(it.jsonObject("comic")) }
                }
            }

    override fun mangaDetailsRequest(manga: SManga): Request =
        throw UnsupportedOperationException()

    override fun mangaDetailsParse(response: Response): SManga =
        throw UnsupportedOperationException()

    override fun getMangaUrl(manga: SManga): String = "${Constants.baseUrl}${manga.url}"

    override fun getChapterUrl(chapter: SChapter): String = "${Constants.baseUrl}${chapter.url}"

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> =
        if (fetchByWeb) client
            .newCall(
                GET(
                    url = "$baseUrl${chapter.url}",
                    headers = webHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                buildJsonParsing {
                    val document = response.asJsoup()
                    document.selectFirst("div.imageData")!!
                        .attr("contentkey")
                        .let(Crypto::decrypt)
                        .let { Json.decodeFromString<JsonArray>(it) }
                        .mapIndexed { index, element ->
                            Page(
                                index = index,
                                imageUrl = element.jsonObject.string("url")
                            )
                        }
                }
            }
        else client
            .newCall(
                GET(
                    url = "${apiUrl}/api/v3${chapter.url.replace(CHAPTER_URL_PREFIX, CHAPTER_URL_PREFIX_2)}",
                    headers = apiHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                buildJsonParsing {
                    val chapter =
                        Json.decodeFromStream<JsonObject>(response.body.byteStream())
                            .jsonObject("results")
                            .jsonObject("chapter")
                    val contents = chapter.jsonArray("contents")
                    chapter.jsonArray("words")
                        .mapIndexed { i, t ->
                            Page(
                                index = t.jsonPrimitive.int,
                                imageUrl = contents.jsonObject(i).string("url")
                            )
                        }
                        .sortedBy { it.index }
                }
            }

    override fun pageListRequest(chapter: SChapter): Request = throw UnsupportedOperationException()

    override fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException()

    private fun fetchMangaShared(limit: Int, offset: Int, response: Response): MangasPage =
        buildJsonParsing {
            val results = Json.decodeFromStream<JsonObject>(response.body.byteStream())
                .jsonObject("results")
            val list = results.jsonArray("list").map { parseComic(it.jsonObject.jsonObject("comic")) }
            val total = results.int("total")
            MangasPage(
                mangas = list, hasNextPage = offset + limit < total
            )
        }

    override fun fetchPopularManga(page: Int): Observable<MangasPage> =
        if (fetchByWeb) {
            val limit = 60
            val offset = (page - 1) * limit
            client
                .newCall(
                    GET(
                        url = "$baseUrl/recommend?type=3200102&limit=$limit&offset=$offset",
                        headers = webHeaders
                    )
                )
                .asObservableSuccess()
                .map { response ->
                    val document = response.asJsoup()
                    val list = document.select("div.col-auto").map { parseComic(it) }
                    val total = document.select(".page-total")[1]!!.text().removePrefix("/").toInt()
                    MangasPage(
                        mangas = list,
                        hasNextPage = page < total
                    )
                }
        } else {
            val limit = 30
            val offset = (page - 1) * limit
            client
                .newCall(
                    GET(
                        url = "${apiUrl}/api/v3/recs?pos=3200102&limit=$limit&offset=$offset",
                        headers = apiHeaders
                    )
                )
                .asObservableSuccess()
                .map { response ->
                    fetchMangaShared(limit, offset, response)
                }
        }

    override fun popularMangaRequest(page: Int): Request =
        throw UnsupportedOperationException()

    override fun popularMangaParse(response: Response): MangasPage =
        throw UnsupportedOperationException()

    override fun fetchLatestUpdates(page: Int): Observable<MangasPage> =
        if (fetchByWeb) {
            val limit = 50
            val offset = (page - 1) * limit
            client
                .newCall(
                    GET(
                        url = "${baseUrl}/comics?ordering=-datetime_updated&limit=$limit&offset=$offset",
                        headers = webHeaders
                    )
                )
                .asObservableSuccess()
                .map { response ->
                    val document = response.asJsoup()
                    val box = document.selectFirst(".exemptComic-box")!!
                    val listRaw = box.attr("list").replace("'", "\"").replace("\\x", "\\u00")
                    val list = Json.decodeFromString<JsonArray>(listRaw)
                        .map { parseComic(it.jsonObject) }
                    val total = box.attr("total").toInt()
                    MangasPage(
                        mangas = list,
                        hasNextPage = offset + limit < total
                    )
                }
        } else {
            val limit = 30
            val offset = (page - 1) * limit
            client
                .newCall(
                    GET(
                        url = "${apiUrl}/api/v3/update/newest?limit=$limit&offset=$offset",
                        headers = apiHeaders
                    )
                )
                .asObservableSuccess()
                .map { response ->
                    fetchMangaShared(limit, offset, response)
                }
        }

    override fun latestUpdatesRequest(page: Int): Request =
        throw UnsupportedOperationException()

    override fun latestUpdatesParse(response: Response): MangasPage =
        throw UnsupportedOperationException()

    private val searchFilter = arrayOf(
        "全部" to "", "名称" to "name", "作者" to "author", "汉化组" to "local"
    )

    private val rankFilter = arrayOf(
        "不查看" to "",
        "日榜(上升最快)" to "day",
        "周榜(最近7天)" to "week",
        "月榜(最近30天)" to "month",
        "总榜单(即热门排序)" to "total"
    )

    private val sortFilter = arrayOf(
        "热门" to "popular", "更新时间" to "datetime_updated"
    )

    private val regionFilter = arrayOf(
        "全部" to "", "日本" to "japan", "韩国" to "korea", "欧美" to "west", "已完结" to "finish"
    )

    private lateinit var genreFilter: Array<Pair<String, String>>

    internal inner class SearchFilter : Filter.Select<String>(
        name = "文本搜索范围", values = searchFilter.map { it.first }.toTypedArray()
    )

    internal inner class RankFilter : Filter.Select<String>(
        name = "排行榜", values = rankFilter.map { it.first }.toTypedArray()
    )

    internal inner class SortFilter : Filter.Sort(
        name = "排序", values = sortFilter.map { it.first }.toTypedArray()
    )

    internal inner class RegionFilter : Filter.Select<String>(
        name = "地区/状态", values = regionFilter.map { it.first }.toTypedArray()
    )

    internal inner class GenreFilter : Filter.Select<String>(
        name = "题材", values = genreFilter.map { it.first }.toTypedArray()
    )

    override fun getFilterList(): FilterList {
        return mutableListOf(
            SearchFilter(),
            Filter.Separator(),
            RankFilter(),
            Filter.Separator(),
            Filter.Header(name = "分类（搜索文本、查看排行榜时无效）"),
            SortFilter(),
            RegionFilter()
        ).apply {
            if (isRefreshGenreFailed) {
                add(Filter.Header("获取题材失败了，已在后台重新获取，请点击“重置”刷新"))
                Thread(::updateGenres).start()
            } else {
                if (::genreFilter.isInitialized) {
                    add(GenreFilter())
                } else {
                    add(Filter.Header("正在获取题材，请点击“重置”刷新"))
                }
            }
        }.let(::FilterList)
    }

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        val search = filters.filterIsInstance<SearchFilter>().first().state
        val rank = filters.filterIsInstance<RankFilter>().first().state
        return when {
            query.isNotBlank() -> fetchSearchManga(
                page = page,
                query = query,
                type = searchFilter[search].second
            )

            rank > 0 -> fetchSearchManga(
                page = page,
                rank = rankFilter[rank].second
            )

            else -> fetchSearchManga(
                page = page,
                sort = filters.filterIsInstance<SortFilter>()
                    .first()
                    .state
                    ?.let { "${if (!it.ascending) "-" else ""}${sortFilter[it.index].second}" },
                region = regionFilter[filters.filterIsInstance<RegionFilter>().first().state].second,
                theme = genreFilter[filters.filterIsInstance<GenreFilter>().first().state].second
            )
        }
    }

    private fun fetchSearchMangaShared(response: Response): MangasPage =
        buildJsonParsing {
            val results = Json.decodeFromStream<JsonObject>(response.body.byteStream())
                .jsonObject("results")
            MangasPage(
                mangas = results.jsonArray("list")
                    .map { parseComic(it.jsonObject.getJsonObject("comic") ?: it.jsonObject) },
                hasNextPage = results.int("offset") + results.int("limit") < results.int("total")
            )
        }

    private fun fetchSearchManga(page: Int, query: String, type: String): Observable<MangasPage> =
        client
            .newCall(
                GET(
                    url = with(apiUrl.toHttpUrl().newBuilder()) {
                        addEncodedPathSegments("api/kb/web/searchba/comics")
                        addQueryParameter("q", query)
                        addQueryParameter("q_type", type)
                        addQueryParameter("limit", "30")
                        addQueryParameter("offset", ((page - 1) * 30).toString())
                        build()
                    },
                    headers = apiHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                fetchSearchMangaShared(response)
            }

    private fun fetchSearchManga(page: Int, rank: String): Observable<MangasPage> =
        client
            .newCall(
                GET(
                    url = with(apiUrl.toHttpUrl().newBuilder()) {
                        addEncodedPathSegments("api/v3/ranks")
                        addQueryParameter("type", "1")
                        addQueryParameter("date_type", rank)
                        addQueryParameter("limit", "30")
                        addQueryParameter("offset", ((page - 1) * 30).toString())
                        build()
                    },
                    headers = apiHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                fetchSearchMangaShared(response)
            }

    private fun fetchSearchManga(page: Int, sort: String?, region: String?, theme: String?): Observable<MangasPage> =
        client
            .newCall(
                GET(
                    url = with(apiUrl.toHttpUrl().newBuilder()) {
                        addEncodedPathSegments("api/v3/comics")
                        if (sort != null) {
                            addQueryParameter("ordering", sort)
                        }
                        addQueryParameter("top", region)
                        addQueryParameter("theme", theme)
                        addQueryParameter("limit", "30")
                        addQueryParameter("offset", ((page - 1) * 30).toString())
                        build()
                    },
                    headers = apiHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                fetchSearchMangaShared(response)
            }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        throw UnsupportedOperationException()

    override fun searchMangaParse(response: Response): MangasPage =
        throw UnsupportedOperationException()

    private fun parseComic(source: JsonObject): SManga = buildJsonParsing {
        SManga.create().apply {
            url = "${MANGA_URL_PREFIX}${source.getString("path_word")}"
            title = source.string("name").let(t2sTransform)
            author = source.jsonArray("author").joinToString { it.jsonObject.string("name").let(t2sTransform) }
            thumbnail_url = source.string("cover")
        }
    }

    private fun parseComic(source: Element): SManga = SManga.create().apply {
        val box = source.selectFirst("div.exemptComicItem-txt-box")!!
        url = box.selectFirst("a")!!.attr("href")
        title = box.selectFirst("p")!!.text().let(t2sTransform)
        author = source.select("span.exemptComicItem-txt-span > a").joinToString { it.text().let(t2sTransform) }
        thumbnail_url = source.selectFirst("img")!!.attr("data-src")
    }

    private fun parseComicDetail(source: JsonObject): SManga = buildJsonParsing {
        parseComic(source).apply {
            description = source.string("brief").let(t2sTransform)
            genre = source.jsonArray("theme").joinToString { it.jsonObject.string("name").let(t2sTransform) }
            status = when (source.jsonObject("status").int("value")) {
                in 1..2 -> SManga.COMPLETED
                0 -> SManga.ONGOING
                else -> SManga.UNKNOWN
            }
            initialized = true
        }
    }

    private fun parseComicDetail(source: Element, appended: SManga): SManga =
        SManga.create().apply {
            url = appended.url
            title = appended.title
            author = appended.author
            thumbnail_url = appended.thumbnail_url
            description = source.selectFirst("p.intro")!!.text().let(t2sTransform)
            genre = source.select("span.comicParticulars-tag > a").joinToString {
                it.text().removePrefix("#").let(t2sTransform)
            }
            status = when (source.selectFirst("li:nth-child(6) > span.comicParticulars-right-txt")!!.text()) {
                "已完結", "短篇" -> SManga.COMPLETED
                "連載中" -> SManga.ONGOING
                else -> SManga.UNKNOWN
            }
            initialized = true
        }

    private val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    private fun parseChapter(source: JsonObject, scan: String?): SChapter = buildJsonParsing {
        SChapter.create().apply {
            val comic = "${MANGA_URL_PREFIX}${source.string("comic_path_word")}"
            url = "${comic}${CHAPTER_URL_PREFIX}${source.string("uuid")}"
            name = source.string("name").let(t2sTransform)
            scanlator = scan?.let(t2sTransform)
            date_upload = source.string("datetime_created").let { date.parse(it)?.time ?: 0L }
        }
    }

    private fun parseChapter(source: JsonObject, comic: String, scan: String?): SChapter = buildJsonParsing {
        SChapter.create().apply {
            url = "${comic}${CHAPTER_URL_PREFIX}${source.string("id")}"
            name = source.string("name").let(t2sTransform)
            scanlator = scan?.let(t2sTransform)
        }
    }

}
