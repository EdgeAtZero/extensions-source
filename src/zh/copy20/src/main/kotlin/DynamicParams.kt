package eu.kanade.tachiyomi.extension.zh.copy20

data class DynamicParams(
    val countApi: String,
    val genres: List<Pair<String, String>>
) {

    companion object{

        val EMPTY = DynamicParams("", listOf("全部" to ""))

    }

}
