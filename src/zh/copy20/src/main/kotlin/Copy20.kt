@file:OptIn(ExperimentalAtomicApi::class, ExperimentalSerializationApi::class)

package eu.kanade.tachiyomi.extension.zh.copy20

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import app.cash.quickjs.QuickJs
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.CHAPTER_URL_PREFIX
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.CHAPTER_URL_PREFIX_2
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.GENRE_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.LIMIT
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.LIMIT_LARGE
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.MANGA_URL_PREFIX
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.MANGA_URL_PREFIX_2
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.RANK_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.RANK_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.RANK_TYPE_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.RANK_TYPE_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.RANK_WEB_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.RANK_WEB_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.REGION_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.REGION_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.REGION_WEB_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.REGION_WEB_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.SEARCH_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.SEARCH_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.SORT_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.SORT_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.STATUS_FILTER
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.STATUS_FILTER_LABEL
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.apiUrl
import eu.kanade.tachiyomi.extension.zh.copy20.Constants.offset
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
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.properties.Delegates


@Suppress("SameParameterValue")
class Copy20 : HttpSource(), ConfigurableSource {
    override val lang = "zh"
    override val supportsLatest = true
    override val name = "拷貝漫畫"
    override val baseUrl get() = Constants.baseUrl

    private val preferences: SharedPreferences
    private var fetchByWeb by Delegates.notNull<Boolean>()
    private var resolution by Delegates.notNull<String>()
    private var translate by Delegates.notNull<Boolean>()
    private var baseHeaders by Delegates.notNull<Headers>()
    private var apiHeaders by Delegates.notNull<Headers>()
    private var webHeaders by Delegates.notNull<Headers>()
    private var dynamicParams by Delegates.notNull<DynamicParams>()
    private var onlyDefault by Delegates.notNull<Boolean>()
    private val onlyDefaultOppositeList = mutableListOf<String>()
    private val t2sTransform: (String) -> String = { if (translate) T2S.convert(it) else it }

    init {
        val application = Injekt.get<Application>()
        @SuppressLint("WrongConstant")
        preferences = application.getSharedPreferences("source_$id", Context.MODE_PRIVATE)
        updatePreferences()
        Thread(::updateParams).start()
        preferences.registerOnSharedPreferenceChangeListener { _, _ -> updatePreferences() }
    }

    private fun updatePreferences() {
        fetchByWeb = preferences.getBoolean(Preferences.FetchByWeb)
        resolution = preferences.getString(Preferences.Resolution)
        translate = preferences.getBoolean(Preferences.Translate)
        onlyDefault = preferences.getBoolean(Preferences.OnlyDefault)
        onlyDefaultOppositeList.clear()
        onlyDefaultOppositeList += preferences.getString(Preferences.OnlyDefaultOppositeList).trim().lines()
        baseHeaders = with(Headers.Builder()) {
            add("Cookie", preferences.getString(Preferences.Cookies))
            add("User-Agent", preferences.getString(Preferences.UserAgent))
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

    private fun updateParams() {
        dynamicParams = DynamicParams.EMPTY.copy(
            countApi = client
                .newCall(
                    GET(
                        url = "$baseUrl/search?q=Hello",
                        headers = webHeaders
                    )
                )
                .execute()
                .body
                .string()
                .substringAfter("countApi = \"")
                .substringBefore("\""),
            genres = DynamicParams.EMPTY.genres + if (fetchByWeb) client
                .newCall(
                    GET(
                        url = "$baseUrl/filter",
                        headers = webHeaders
                    )
                )
                .execute()
                .asJsoup()
                .select("div.screenAll-clarity-all > a")
                .map {
                    it.text().substringBefore('(').let(t2sTransform) to it.attr("href").substringAfterLast("theme=")
                }
            else client
                .newCall(
                    GET(
                        url = "${apiUrl}/api/v3/theme/comic/count?limit=$LIMIT_LARGE",
                        headers = apiHeaders
                    )
                )
                .execute()
                .let { response ->
                    buildJsonParsing {
                        Json.decodeFromStream<JsonObject>(response.body.byteStream())
                            .jsonObject("results")
                            .jsonArray("list")
                            .map {
                                it.jsonObject.string("name").let(t2sTransform) to it.jsonObject.string("path_word")
                            }
                    }
                }
        )
    }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        with(SwitchPreferenceCompat(screen.context)) {
            key = ""
            title = "刷新动态参数"
            summary = "改变开关状态即可"
            setDefaultValue(false)
            setOnPreferenceChangeListener { _, _ -> Thread(::updateParams).start(); true }
            screen.addPreference(this)
        }
        with(SwitchPreferenceCompat(screen.context)) {
            key = Preferences.FetchByWeb.KEY
            title = "部分数据从网页获取"
            summary = "当你访问手机端网页提示下载客户端时使用\n注意：获取漫画章节时会丢失上传时间，排行榜看不了全部"
            setDefaultValue(Preferences.FetchByWeb.DEFAULT)
            screen.addPreference(this)
        }
        with(SwitchPreferenceCompat(screen.context)) {
            key = Preferences.Translate.KEY
            title = "繁体转简体"
            setDefaultValue(Preferences.Translate.DEFAULT)
            screen.addPreference(this)
        }
        with(ListPreference(screen.context)) {
            key = Preferences.Resolution.KEY
            title = "图片分辨率 (像素)"
            summary = "阅读过的部分需要清空缓存才能生效"
            entries = Resolutions.toTypedArray()
            entryValues = entries
            setDefaultValue(Preferences.Resolution.DEFAULT)
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preferences.UserAgent.KEY
            title = "UserAgent（需要非手机版的）"
            setDefaultValue(Preferences.UserAgent.DEFAULT)
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preferences.Cookies.KEY
            title = "Cookies"
            setDefaultValue(Preferences.Cookies.DEFAULT)
            screen.addPreference(this)
        }
        with(SwitchPreferenceCompat(screen.context)) {
            key = Preferences.OnlyDefault.KEY
            title = "只保留默认"
            summary = "漫画章节列表只保留默认，不获取单行本等其他的"
            setDefaultValue(Preferences.OnlyDefault.DEFAULT)
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preferences.OnlyDefaultOppositeList.KEY
            title = "只保留默认相反列表"
            summary =
                "如果上面的选项开了，这个就是上面的功能的禁用列表，否则就是单独的启用列表\n注意：一行一个漫画名称，注意简繁，建议直接复制"
            setDefaultValue(Preferences.OnlyDefaultOppositeList.DEFAULT)
            screen.addPreference(this)
        }
    }

    override fun fetchPopularManga(page: Int): Observable<MangasPage> = if (fetchByWeb) client
        .newCall(
            GET(
                url = "$baseUrl/recommend?type=3200102&limit=$LIMIT&offset=${offset(page)}",
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
    else client
        .newCall(
            GET(
                url = "${apiUrl}/api/v3/recs?pos=3200102&limit=$LIMIT&offset=${offset(page)}",
                headers = apiHeaders
            )
        )
        .asObservableSuccess()
        .map { response ->
            parseJsonResponse(page, response)
        }


    override fun popularMangaRequest(page: Int): Request =
        throw UnsupportedOperationException()

    override fun popularMangaParse(response: Response): MangasPage =
        throw UnsupportedOperationException()

    override fun fetchLatestUpdates(page: Int): Observable<MangasPage> = if (fetchByWeb) client
        .newCall(
            GET(
                url = "${baseUrl}/comics?ordering=-datetime_updated&limit=$LIMIT&offset=$${offset(page)}",
                headers = webHeaders
            )
        )
        .asObservableSuccess()
        .map { response ->
            parseWebResponse(page, response)
        }
    else client
        .newCall(
            GET(
                url = "${apiUrl}/api/v3/update/newest?limit=$LIMIT&offset=${offset(page)}",
                headers = apiHeaders
            )
        )
        .asObservableSuccess()
        .map { response ->
            parseJsonResponse(page, response)
        }

    override fun latestUpdatesRequest(page: Int): Request =
        throw UnsupportedOperationException()

    override fun latestUpdatesParse(response: Response): MangasPage =
        throw UnsupportedOperationException()

    internal inner class ParamFilter(name: String, values: Array<String>) : Filter.Select<String>(name, values)

    internal inner class SortFilter : Filter.Sort(
        SORT_FILTER_LABEL,
        SORT_FILTER.map { it.first }.toTypedArray()
    )

    override fun getFilterList(): FilterList {
        return if (fetchByWeb) FilterList(
            ParamFilter(
                SEARCH_FILTER_LABEL,
                SEARCH_FILTER.map { it.first }.toTypedArray()
            ),
            Filter.Separator(),
            ParamFilter(
                RANK_WEB_FILTER_LABEL,
                RANK_FILTER.map { it.first }.toTypedArray()
            ),
            ParamFilter(
                RANK_TYPE_FILTER_LABEL,
                RANK_TYPE_FILTER.map { it.first }.toTypedArray()
            ),
            Filter.Separator(),
            SortFilter(),
            ParamFilter(
                REGION_WEB_FILTER_LABEL,
                REGION_WEB_FILTER.map { it.first }.toTypedArray()
            ),
            ParamFilter(
                STATUS_FILTER_LABEL,
                STATUS_FILTER.map { it.first }.toTypedArray()
            ),
            ParamFilter(
                GENRE_FILTER_LABEL,
                dynamicParams.genres.map { it.first }.toTypedArray()
            )
        ) else FilterList(
            ParamFilter(
                SEARCH_FILTER_LABEL,
                SEARCH_FILTER.map { it.first }.toTypedArray()
            ),
            Filter.Separator(),
            ParamFilter(
                RANK_FILTER_LABEL,
                RANK_FILTER.map { it.first }.toTypedArray()
            ),
            Filter.Separator(),
            SortFilter(),
            ParamFilter(
                REGION_FILTER_LABEL,
                REGION_FILTER.map { it.first }.toTypedArray()
            ),
            ParamFilter(
                GENRE_FILTER_LABEL,
                dynamicParams.genres.map { it.first }.toTypedArray()
            )
        )
    }

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        val params = filters.filterIsInstance<ParamFilter>()
        val rank = params.firstOrNull { it.name == RANK_FILTER_LABEL }?.state
            ?: params.firstOrNull { it.name == RANK_WEB_FILTER_LABEL }?.state
        val sort = filters.filterIsInstance<SortFilter>().first()
        return when {
            query.isNotBlank() -> fetchSearchManga(
                page = page,
                query = query,
                type = SEARCH_FILTER[params.first { it.name == SEARCH_FILTER_LABEL }.state].second
            )

            rank != null && rank > 0 -> if (fetchByWeb) fetchSearchMangaByWeb(
                rank = RANK_WEB_FILTER[rank].second,
                type = RANK_TYPE_FILTER[params.first { it.name == RANK_TYPE_FILTER_LABEL }.state].second
            ) else fetchSearchManga(
                page = page,
                rank = RANK_FILTER[rank].second
            )

            else -> if (fetchByWeb) fetchSearchMangaByWeb(
                page = page,
                sort = sort.state?.let { "${if (!it.ascending) "-" else ""}${SORT_FILTER[it.index].second}" },
                region = REGION_WEB_FILTER[params.first { it.name == REGION_WEB_FILTER_LABEL }.state].second,
                status = STATUS_FILTER[params.first { it.name == STATUS_FILTER_LABEL }.state].second,
                theme = dynamicParams.genres[params.first { it.name == GENRE_FILTER_LABEL }.state].second
            ) else fetchSearchManga(
                page = page,
                sort = sort.state?.let { "${if (!it.ascending) "-" else ""}${SORT_FILTER[it.index].second}" },
                region = REGION_FILTER[params.first { it.name == REGION_FILTER_LABEL }.state].second,
                theme = dynamicParams.genres[params.first { it.name == GENRE_FILTER_LABEL }.state].second
            )
        }
    }

    private fun fetchSearchManga(page: Int, query: String, type: String): Observable<MangasPage> = client
        .newCall(
            GET(
                url = with("$baseUrl${dynamicParams.countApi}".toHttpUrl().newBuilder()) {
                    addQueryParameter("q", query)
                    addQueryParameter("q_type", type)
                    addQueryParameter("limit", LIMIT.toString())
                    addQueryParameter("offset", offset(page).toString())
                    build()
                },
                headers = apiHeaders
            )
        )
        .asObservableSuccess()
        .map { response ->
            parseJson2Response(response)
        }

    private fun fetchSearchManga(page: Int, rank: String): Observable<MangasPage> = client
        .newCall(
            GET(
                url = with("$apiUrl/api/v3/ranks".toHttpUrl().newBuilder()) {
                    addQueryParameter("type", "1")
                    addQueryParameter("date_type", rank)
                    addQueryParameter("limit", LIMIT.toString())
                    addQueryParameter("offset", offset(page).toString())
                    build()
                },
                headers = apiHeaders
            )
        )
        .asObservableSuccess()
        .map { response ->
            parseJson2Response(response)
        }

    private fun fetchSearchMangaByWeb(rank: String, type: String): Observable<MangasPage> = client
        .newCall(
            GET(
                url = with("$baseUrl/rank".toHttpUrl().newBuilder()) {
                    addQueryParameter("table", rank)
                    addQueryParameter("type", type)
                    build()
                },
                headers = webHeaders
            )
        )
        .asObservableSuccess()
        .map { response ->
            MangasPage(
                mangas = response.asJsoup()
                    .select("div.ranking-all-box")
                    .map {
                        parseComicRank(it)
                    },
                hasNextPage = false
            )
        }

    private fun fetchSearchManga(page: Int, sort: String?, region: String?, theme: String?): Observable<MangasPage> =
        client
            .newCall(
                GET(
                    url = with("$apiUrl/api/v3/comics".toHttpUrl().newBuilder()) {
                        if (sort != null) {
                            addQueryParameter("ordering", sort)
                        }
                        addQueryParameter("top", region)
                        addQueryParameter("theme", theme)
                        addQueryParameter("limit", LIMIT.toString())
                        addQueryParameter("offset", offset(page).toString())
                        build()
                    },
                    headers = apiHeaders
                )
            )
            .asObservableSuccess()
            .map { response ->
                parseJson2Response(response)
            }

    private fun fetchSearchMangaByWeb(
        page: Int,
        sort: String?,
        region: String,
        status: String,
        theme: String
    ): Observable<MangasPage> = client
        .newCall(
            GET(
                url = with("$baseUrl/comics".toHttpUrl().newBuilder()) {
                    if (sort != null) {
                        addQueryParameter("ordering", sort)
                    }
                    addQueryParameter("region", region)
                    addQueryParameter("status", status)
                    addQueryParameter("theme", theme)
                    addQueryParameter("limit", LIMIT.toString())
                    addQueryParameter("offset", offset(page).toString())
                    build()
                },
                headers = webHeaders
            )
        )
        .asObservableSuccess()
        .map { response ->
            parseWebResponse(page, response)
        }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        throw UnsupportedOperationException()

    override fun searchMangaParse(response: Response): MangasPage =
        throw UnsupportedOperationException()

    override fun getMangaUrl(manga: SManga): String =
        "${Constants.baseUrl}${manga.url}"

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

    override fun getChapterUrl(chapter: SChapter): String =
        "${Constants.baseUrl}${chapter.url}"

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
                    var offset = 0
                    var total = -1
                    var loop = true
                    while (loop) {
                        val request = GET(
                            url = "${apiUrl}/api/v3${manga.url}/group/${path}/chapters?limit=$LIMIT_LARGE&offset=$offset",
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
                            loop = offset + LIMIT_LARGE < total
                            offset += LIMIT_LARGE
                        }
                    }

                }
            }
        }

    override fun chapterListRequest(manga: SManga): Request =
        throw UnsupportedOperationException()

    override fun chapterListParse(response: Response): List<SChapter> =
        throw UnsupportedOperationException()

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

    override fun fetchImageUrl(page: Page): Observable<String> = Observable.fromCallable {
        checkNotNull(page.imageUrl)
    }

    override fun imageUrlRequest(page: Page): Request =
        throw UnsupportedOperationException()

    override fun imageUrlParse(response: Response): String =
        throw UnsupportedOperationException()

    override fun imageRequest(page: Page): Request = GET(
        url = Constants.RESOLUTION_REGEX.replaceFirst(requireNotNull(page.imageUrl), resolution),
        headers = baseHeaders
    )

    private fun parseJsonResponse(page: Int, response: Response): MangasPage = buildJsonParsing {
        val results = Json.decodeFromStream<JsonObject>(response.body.byteStream())
            .jsonObject("results")
        val list = results.jsonArray("list").map { parseComic(it.jsonObject.jsonObject("comic")) }
        val total = results.int("total")
        MangasPage(
            mangas = list, hasNextPage = page * LIMIT < total
        )
    }

    private fun parseJson2Response(response: Response): MangasPage = buildJsonParsing {
        val results = Json.decodeFromStream<JsonObject>(response.body.byteStream())
            .jsonObject("results")
        MangasPage(
            mangas = results.jsonArray("list")
                .map { parseComic(it.jsonObject.getJsonObject("comic") ?: it.jsonObject) },
            hasNextPage = results.int("offset") + results.int("limit") < results.int("total")
        )
    }

    private fun parseWebResponse(page: Int, response: Response): MangasPage {
        val document = response.asJsoup()
        val box = document.selectFirst(".exemptComic-box")!!
        val list = Json.decodeFromString<JsonArray>(jsObjectToJson(box.attr("list")))
            .map { parseComic(it.jsonObject) }
        val total = box.attr("total").toInt()
        return MangasPage(
            mangas = list,
            hasNextPage = page * LIMIT < total
        )
    }

    private fun jsObjectToJson(source: String): String {
        val context = QuickJs.create()
        try {
            return context.evaluate("JSON.stringify($source);") as String
        } finally {
            context.close()
        }
    }

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

    private fun parseComicRank(source: Element): SManga = SManga.create().apply {
        val box = source.selectFirst("div.ranking-all-topThree-txt")!!
        url = source.selectFirst("a")!!.attr("href")
        title = box.selectFirst("a > p")!!.text().let(t2sTransform)
        author = box.select("span > a").joinToString { it.text().let(t2sTransform) }
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
