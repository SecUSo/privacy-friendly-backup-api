package org.secuso.privacyfriendlybackup.api.pfa

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import org.secuso.privacyfriendlybackup.api.IPFAService
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.ERROR_AUTH_CERT_MISMATCH
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.MSG_AUTHENTICATE
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.MSG_BACKUP
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.MSG_RESTORE
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.REPLY_AUTHENTICATION_ERROR
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.REPLY_AUTHENTICATION_OK
import org.secuso.privacyfriendlybackup.api.pfa.PfaApi.REPLY_NOT_AUTHENTICATED
import org.secuso.privacyfriendlybackup.api.util.AuthenticationUtil

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

    private val mBinder : IPFAService.Stub = object : IPFAService.Stub() {

        override fun execute(data: Intent?): Intent {
            TODO("Not yet implemented")
        }

    }

    internal class PFAHandler(val context: Context,
                              val applicationContext : Context = context.applicationContext) : Handler() {

        val mAuthenticatedIds : MutableSet<Int> = HashSet()

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_AUTHENTICATE -> handleAuthentication(msg)
                MSG_BACKUP -> handleBackup(msg)
                MSG_RESTORE -> handleRestore(msg)
                else -> super.handleMessage(msg)
            }
        }

        private fun handleAuthentication(msg : Message) {
            if(AuthenticationUtil.authenticate(applicationContext, msg.sendingUid)) {
                mAuthenticatedIds.add(msg.sendingUid)
                msg.replyTo.send(Message.obtain(null, REPLY_AUTHENTICATION_OK, 0, 0))
            } else {
                msg.replyTo.send(Message.obtain(null, REPLY_AUTHENTICATION_ERROR, ERROR_AUTH_CERT_MISMATCH, 0))
            }
        }

        private fun handleBackup(msg: Message) {
            if(isAuthenticated(msg.sendingUid)) {
                msg.replyTo.send(Message.obtain(null, REPLY_NOT_AUTHENTICATED, 0, 0))
                return
            }

            // TODO logic for scheduling of backup
        }

        private fun handleRestore(msg: Message) {
            if(isAuthenticated(msg.sendingUid)) {
                msg.replyTo.send(Message.obtain(null, REPLY_NOT_AUTHENTICATED, 0, 0))
                return
            }
        }

        private fun isAuthenticated(uid : Int) : Boolean = !mAuthenticatedIds.contains(uid)
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