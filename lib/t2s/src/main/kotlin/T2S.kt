package eu.kanade.tachiyomi.lib.t2s

import com.github.liuyueyi.quick.transfer.dictionary.DictionaryFactory

object T2S {

    private val dictionary by lazy {
        DictionaryFactory.loadDictionary("assets/t2s.txt", false)
    }

    /**
     * translate traditional chinese to simplified chinese
     */
    @JvmStatic
    fun convert(source: String): String =
        dictionary.convert(source)

}
