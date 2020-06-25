package org.secuso.privacyfriendlybackup.api.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.JsonReader
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author Christopher Beckmann
 */
object AuthenticationHelper {

    fun getPackageNames(context: Context, uid: Int) : Array<String> {
        val packageNameArray = context.packageManager.getPackagesForUid(uid)

        if (packageNameArray == null || packageNameArray.isEmpty()) {
            return emptyArray()
        }

        return packageNameArray
    }

    fun authenticate(context: Context, uid: Int): Boolean {
        // if no uid is sent - we can not authenticate
        if (uid == -1)
            return false

        val packageNameArray = getPackageNames(context, uid)
        if(packageNameArray.isEmpty())
            return false

        // if we are binding our own service, always grant access
        if(packageNameArray[0] == context.packageName) {
            return true
        }

        val validSignatures : MutableList<String> = ArrayList()

        try {
            //var jsonArray = JSONArray(context.assets.open(context.assets.list("")!![0]).readJson())
            var signatureJson = JSONArray(context.assets.open("Signatures.json").readJson())
            for(i in 0 until signatureJson.length()) {
                val sig = signatureJson.get(i) as String
                validSignatures.add(sig)
            }
        } catch (e : IOException) {
            return false
        } catch (e : JSONException) {
            return false
        }

        for (packageName in packageNameArray) {
            val sigs = getSignatures(context, packageName)
            if (sigs.isEmpty())
                return false

            for(sig in sigs) {
                if(!validSignatures.contains(sig)) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Returns the signatures for the given packageName encoded as Base64 or an empty list if no signatures were found
     *
     * @param context The context that is used to query the package manager
     * @param packageName the package name for which the signatures are being loaded
     */
    @SuppressLint("PackageManagerGetSignatures")
    private fun getSignatures(context: Context, packageName: String): List<String> {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sig = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo

                if (sig.hasMultipleSigners()) {
                    sig.apkContentsSigners.map { it.toHex() }
                } else {
                    sig.signingCertificateHistory.map { it.toHex() }
                }
            } else {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures.map { it.toHex() }
            }

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return emptyList()
    }
}

/**
 * Extension function to convert Signature to Base64
 */
fun Signature.toBase64() : String {
    val md = MessageDigest.getInstance("SHA")
    md.update(this.toByteArray())
    return Base64.encodeToString(md.digest(), Base64.DEFAULT)
}

/**
 * Extension function to convert Signature to Hex
 */
fun Signature.toHex() : String {
    val md = MessageDigest.getInstance("SHA")
    md.update(this.toByteArray())
    return bytesToHex(md.digest())
}

fun bytesToHex(bytes: ByteArray) : String {
    val HEX_CHARS = "0123456789ABCDEF"
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
}

fun InputStream.readJson() : String {
    return this.use {
        val buffer = ByteArray(it.available())
        read(buffer)
        return@use String(buffer, Charsets.UTF_8)
    }
}