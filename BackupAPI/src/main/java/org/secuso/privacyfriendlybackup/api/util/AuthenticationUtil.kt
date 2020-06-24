package org.secuso.privacyfriendlybackup.api.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author Christopher Beckmann
 */
object AuthenticationUtil {

    fun authenticate(context: Context, uid : Int) : Boolean {
        // if no uid is sent - we can not authenticate
        if(uid == -1)
            return false

        val packageNameArray = context.packageManager.getPackagesForUid(uid)

        if(packageNameArray == null || packageNameArray.isEmpty())
            return false

        for (packageName in packageNameArray) {
            val certs = getCertificates(context, packageName)
            if(certs.isEmpty())
                return false

            TODO("")
            // TODO authentication logic -> get certificates and compare with saved certificates
        }

        return true
    }

    /**
     * Returns the certificates for the given packageName encoded as Base64 or an empty list if no certificates were found
     *
     * @param context The context that is used to query the package manager
     * @param packageName the package name for which the certificates are being loaded
     */
    @SuppressLint("PackageManagerGetSignatures")
    fun getCertificates(context: Context, packageName: String): List<String> {
        try {

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sig = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo

                if (sig.hasMultipleSigners()) {
                    sig.apkContentsSigners.map { it.toBase64() }
                } else {
                    sig.signingCertificateHistory.map { it.toBase64() }
                }
            } else {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures.map { it.toBase64() }
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