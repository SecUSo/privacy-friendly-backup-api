package org.secuso.privacyfriendlybackup.api.pfa

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.ERROR_AUTH_CERT_MISMATCH
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.MSG_AUTHENTICATE
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.REPLY_AUTHENTICATION_ERROR
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.REPLY_AUTHENTICATION_OK

/**
 * This class is meant to be extended by the PFA. Also it should then be included in the PFA's
 * AndroidManifest.xml file.
 *
 * <pre>
 *     {@code
 *      <service
 *           android:name=".PFAAuthService"
 *           android:enabled="true"
 *           android:exported="true"
 *           android:process=":backup"
 *           tools:ignore="ExportedService">
 *           <intent-filter>
 *               <action android:name="org.secuso.privacyfriendlybackup.api.pfa.PFAAuthService" />
 *           </intent-filter>
 *      </service>
 *      }
 * </pre>
 *
 * @author Christopher Beckmann
 */
abstract class PFAAuthService : Service() {
    private lateinit var mMessenger: Messenger

    internal class PFAHandler(context: Context) : Handler() {

        val mAuthenticatedIds : MutableSet<Int> = HashSet()

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_AUTHENTICATE -> {
                    // TODO Authentication logic missing
                    val successful = true
                    if(successful) {
                        mAuthenticatedIds.add(msg.sendingUid)
                        msg.replyTo.send(Message.obtain(null, REPLY_AUTHENTICATION_OK, 0, 0))
                    } else {
                        msg.replyTo.send(Message.obtain(null, REPLY_AUTHENTICATION_ERROR, ERROR_AUTH_CERT_MISMATCH, 0))
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        mMessenger = Messenger(
            PFAHandler(
                this
            )
        )
        return mMessenger.binder
    }
}