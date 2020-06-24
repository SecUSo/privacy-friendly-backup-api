package org.secuso.privacyfriendlybackup.api.util

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author Christopher Beckmann
 */
@SuppressLint("PackageManagerGetSignatures")
fun getCertificates(context: Context, packageName: String) : List<String> {
    try {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo

            if(sig.hasMultipleSigners()) {
                sig.apkContentsSigners.map {it.toBase64()}
            } else {
                sig.signingCertificateHistory.map {it.toBase64()}
            }
        } else {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures.map {it.toBase64()}
        }

    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return emptyList()
}

fun Signature.toBase64() : String {
    val md = MessageDigest.getInstance("SHA")
    md.update(this.toByteArray())
    return Base64.encodeToString(md.digest(), Base64.DEFAULT)
}