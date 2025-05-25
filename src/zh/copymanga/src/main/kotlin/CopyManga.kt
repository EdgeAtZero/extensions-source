@file:OptIn(ExperimentalSerializationApi::class)

package eu.kanade.tachiyomi.extension.zh.copymanga

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import com.github.liuyueyi.quick.transfer.ChineseUtils
import eu.kanade.tachiyomi.extension.zh.copymanga.Constants.CHAPTER_URL_PREFIX
import eu.kanade.tachiyomi.extension.zh.copymanga.Constants.CHAPTER_URL_PREFIX_2
import eu.kanade.tachiyomi.extension.zh.copymanga.Constants.MANGA_URL_PREFIX
import eu.kanade.tachiyomi.extension.zh.copymanga.Constants.MANGA_URL_PREFIX_2
import eu.kanade.tachiyomi.extension.zh.copymanga.Constants.apiUrl
import eu.kanade.tachiyomi.lib.json.getInt
import eu.kanade.tachiyomi.lib.json.getJsonArray
import eu.kanade.tachiyomi.lib.json.getJsonObject
import eu.kanade.tachiyomi.lib.json.getString
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get


class CopyManga : ConfigurableSource, HttpSource() {
    override val lang = "zh"
    override val supportsLatest = true
    override val name = "拷贝漫画"
    override val baseUrl get() = Constants.baseUrl

    private val preferences: SharedPreferences
    private var resolution: String
    private var _headers: Headers

    init {
        val application = Injekt.get<Application>()
        @SuppressLint("WrongConstant")
        preferences = application.getSharedPreferences("source_$id", Context.MODE_PRIVATE)
        resolution = preferences.getString(Preference.Resolution)
        _headers = headersBuilder().add("User-Agent", preferences.getString(Preference.UserAgent)).build()
        Thread {
            try {
                val request = GET(url = "${apiUrl}/api/v3/theme/comic/count?limit=500", headers = _headers)
                val response = client.newCall(request).execute()
                genreFilter = Json.decodeFromStream<JsonObject>(response.body.byteStream())
                    .getJsonObject("results")!!
                    .getJsonArray("list")!!
                    .map {
                        it.jsonObject.getString("name")!!.let(ChineseUtils::t2s) to it.jsonObject.getString("path_word")!!
                    }
                    .toTypedArray()
            } catch (_: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(application, "$name: 刷新题材失败", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.start()
    }

    override fun imageRequest(page: Page): Request =
        GET(
            url = Constants.RESOLUTION_REGEX.replaceFirst(page.imageUrl!!, resolution),
            headers = _headers
        )

    override fun headersBuilder(): Headers.Builder =
        with(Headers.Builder()) {
            add("Accept", "application/json")
            add("Cookie", preferences.getString(Preference.Cookies))
            add("User-Agent", preferences.getString(Preference.UserAgent))
            add("platform", "1")
            add("webp", "1")
            add("region", "0")
        }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        with(ListPreference(screen.context)) {
            key = Preference.Resolution.KEY
            title = "图片分辨率 (像素)"
            summary = "阅读过的部分需要清空缓存才能生效"
            entries = Resolutions.toTypedArray()
            entryValues = entries
            setDefaultValue(Preference.Resolution.DEFAULT)
            setOnPreferenceChangeListener { _, _ ->
                resolution = preferences.getString(Preference.Resolution)
                true
            }
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preference.UserAgent.KEY
            title = "UserAgent（需要非手机版的）"
            setDefaultValue(Preference.UserAgent.DEFAULT)
            setOnPreferenceChangeListener { _, _ ->
                _headers = headersBuilder().build()
                true
            }
            screen.addPreference(this)
        }
        with(EditTextPreference(screen.context)) {
            key = Preference.Cookies.KEY
            title = "Cookies"
            setDefaultValue(Preference.Cookies.DEFAULT)
            setOnPreferenceChangeListener { _, _ ->
                _headers = headersBuilder().build()
                true
            }
            screen.addPreference(this)
        }
    }

    override fun chapterListRequest(manga: SManga): Request =
        throw UnsupportedOperationException()

    override fun chapterListParse(response: Response): List<SChapter> =
        throw UnsupportedOperationException()

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> =
        Observable.fromCallable {
            buildList {
                var response = client.newCall(mangaDetailsRequest(manga)).execute()
                val groups = Json.decodeFromStream<JsonObject>(response.body.byteStream())
                    .getJsonObject("results")!!
                    .let { results ->
                        results.getJsonObject("groups")!!
                            .map { it.value.jsonObject.getString("path_word") }
                    }
                for (group in groups) {
                    var offset = 0
                    var loop = true
                    while (loop) {
                        val request = GET(
                            url = "${apiUrl}/api/v3${manga.url}/group/${group}/chapters?limit=500&offset=${offset}",
                            headers = _headers
                        )
                        response = client.newCall(request).execute()
                        val results = Json.decodeFromStream<JsonObject>(response.body.byteStream())
                            .getJsonObject("results")!!
                        results.getJsonArray("list")!!.forEach {
                            add(0, JsonUtils.parseChapter(it.jsonObject))
                            offset++
                        }
                        loop = offset < results.getInt("total")!!
                    }
                }
            }
        }

    override fun imageUrlRequest(page: Page): Request =
        throw UnsupportedOperationException()

    override fun imageUrlParse(response: Response): String =
        throw UnsupportedOperationException()

    override fun latestUpdatesRequest(page: Int): Request =
        GET(
            url = "${apiUrl}/api/v3/update/newest?limit=30&offset=${(page - 1) * 30}",
            headers = _headers
        )

    override fun latestUpdatesParse(response: Response): MangasPage =
        popularMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request =
        GET(
            url = "${apiUrl}/api/v3${manga.url.replace(MANGA_URL_PREFIX, MANGA_URL_PREFIX_2)}",
            headers = _headers
        )

    override fun getMangaUrl(manga: SManga): String =
        "${Constants.baseUrl}${manga.url}"

    override fun getChapterUrl(chapter: SChapter): String =
        "${Constants.baseUrl}${chapter.url}"

    override fun mangaDetailsParse(response: Response): SManga =
        Json.decodeFromStream<JsonObject>(response.body.byteStream()).getJsonObject("results")!!.let { results ->
            JsonUtils.parseComicDetail(results.getJsonObject("comic")!!)
        }

    override fun pageListRequest(chapter: SChapter): Request =
        GET(
            url = "${apiUrl}/api/v3${chapter.url.replace(CHAPTER_URL_PREFIX, CHAPTER_URL_PREFIX_2)}",
            headers = _headers
        )

    override fun pageListParse(response: Response): List<Page> {
        val results = Json.decodeFromStream<JsonObject>(response.body.byteStream())
            .getJsonObject("results")!!
        val chapter = results.getJsonObject("chapter")!!
        val words = chapter.getJsonArray("words")!!
        val contents = chapter.getJsonArray("contents")!!
        return words.mapIndexed { i, word ->
            val url = contents.getJsonObject(i)?.getString("url") ?: return@mapIndexed null
            Page(index = word.jsonPrimitive.int, imageUrl = url)
        }.filterNotNull().sortedBy { it.index }
    }

    override fun popularMangaRequest(page: Int): Request =
        GET(
            url = "${apiUrl}/api/v3/recs?pos=3200102&limit=30&offset=${(page - 1) * 30}",
            headers = _headers
        )

    override fun popularMangaParse(response: Response): MangasPage =
        Json.decodeFromStream<JsonObject>(response.body.byteStream()).getJsonObject("results")!!.let { results ->
            MangasPage(
                mangas = results
                    .getJsonArray("list")!!
                    .map { JsonUtils.parseComic(it.jsonObject.getJsonObject("comic")!!) },
                hasNextPage = results.getInt("offset")!! + results.getInt("limit")!! < results.getInt("total")!!
            )
        }

    private val searchFilter = arrayOf(
        "全部" to "",
        "名称" to "name",
        "作者" to "author",
        "汉化组" to "local"
    )

    private val rankFilter = arrayOf(
        "不查看" to "",
        "日榜(上升最快)" to "day",
        "周榜(最近7天)" to "week",
        "月榜(最近30天)" to "month",
        "总榜单(即热门排序)" to "total"
    )

    private val sortFilter = arrayOf(
        "热门" to "popular",
        "更新时间" to "datetime_updated"
    )

    private val regionFilter = arrayOf(
        "全部" to "",
        "日本" to "japan",
        "韩国" to "korea",
        "欧美" to "west",
        "已完结" to "finish"
    )

    private lateinit var genreFilter: Array<Pair<String, String>>

    internal inner class SearchFilter : Filter.Select<String>(
        name = "文本搜索范围",
        values = searchFilter.map { it.first }.toTypedArray()
    )

    internal inner class RankFilter : Filter.Select<String>(
        name = "排行榜",
        values = rankFilter.map { it.first }.toTypedArray()
    )

    internal inner class SortFilter : Filter.Sort(
        name = "排序",
        values = sortFilter.map { it.first }.toTypedArray()
    )

    internal inner class RegionFilter : Filter.Select<String>(
        name = "地区/状态",
        values = regionFilter.map { it.first }.toTypedArray()
    )

    internal inner class GenreFilter : Filter.Select<String>(
        name = "题材",
        values = genreFilter.map { it.first }.toTypedArray()
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
            ::genreFilter.isInitialized || return@apply
            add(GenreFilter())
        }.let(::FilterList)
    }

    override fun searchMangaRequest(
        page: Int,
        query: String,
        filters: FilterList
    ): Request = with(apiUrl.toHttpUrl().newBuilder()) {
        val search = filters.filterIsInstance<SearchFilter>().first().state
        val rank = filters.filterIsInstance<RankFilter>().first().state
        when {
            query.isNotBlank() -> {
                addEncodedPathSegments("api/v3/search/comic")
                addQueryParameter("limit", "30")
                addQueryParameter("offset", ((page - 1) * 30).toString())
                addQueryParameter("q", query)
                addQueryParameter("q_type", searchFilter[search].second)
            }

            rank > 0 -> {
                addEncodedPathSegments("api/v3/ranks")
                addQueryParameter("type", "1")
                addQueryParameter("date_type", rankFilter[rank].second)
            }

            else -> {
                addEncodedPathSegments("api/v3/comics")
                val sort = filters.filterIsInstance<SortFilter>().first().state
                val region = filters.filterIsInstance<RegionFilter>().first().state
                val genre = filters.filterIsInstance<GenreFilter>().first().state
                if (sort != null) {
                    addQueryParameter("ordering", "${if (!sort.ascending) "-" else ""}${sortFilter[sort.index].second}")
                }
                addQueryParameter("top", regionFilter[region].second)
                addQueryParameter("theme", genreFilter[genre].second)
            }
        }
        GET(url = build(), headers = _headers)
    }

    override fun searchMangaParse(response: Response): MangasPage =
        Json.decodeFromStream<JsonObject>(response.body.byteStream()).getJsonObject("results")!!.let { results ->
            MangasPage(
                mangas = results
                    .getJsonArray("list")!!
                    .map { JsonUtils.parseComic(it.jsonObject) },
                hasNextPage = results.getInt("offset")!! + results.getInt("limit")!! < results.getInt("total")!!
            )
        }

}
