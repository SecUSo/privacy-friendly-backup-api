package org.secuso.privacyfriendlybackup.api.util

import android.content.pm.Signature
import android.util.Base64
import java.io.InputStream
import java.security.MessageDigest

fun InputStream.readString() : String {
    return this.use {
        val buffer = it.readBytes()
        return@use String(buffer, Charsets.UTF_8)
    }
}

fun Signature.toBase64() : String {
    val md = MessageDigest.getInstance("SHA")
    md.update(this.toByteArray())
    return Base64.encodeToString(md.digest(), Base64.DEFAULT)
}

fun Signature.toHex() : String {
    val md = MessageDigest.getInstance("SHA")
    md.update(this.toByteArray())
    return md.digest().bytesToHex()
}

fun ByteArray.bytesToHex() : String {
    val HEX_CHARS = "0123456789ABCDEF"
    val result = StringBuilder(this.size * 2)
    this.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }
    return result.toString()
}