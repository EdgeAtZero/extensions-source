package eu.kanade.tachiyomi.extension.zh.copy20

import okio.ByteString.Companion.decodeHex
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object Crypto {

    private val KEY = byteArrayOf(
        0x78, 0x78, 0x78, 0x6D, 0x61, 0x6E, 0x67, 0x61,
        0x2E, 0x77, 0x6F, 0x6F, 0x2E, 0x6B, 0x65, 0x79
    )

    fun decrypt(source: String): String {
        try {
            val iv = source.slice(0..15).encodeToByteArray()
            val input = source.slice(16..source.lastIndex).decodeHex().toByteArray()
            val secretKey = SecretKeySpec(KEY, "AES")
            val ivParameterSpec = IvParameterSpec(iv)
            return with(Cipher.getInstance("AES/CBC/PKCS5Padding")) {
                init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
                doFinal(input).decodeToString()
            }
        } catch (e: Exception) {
            throw IllegalStateException("decrypt failed", e)
        }
    }

}
