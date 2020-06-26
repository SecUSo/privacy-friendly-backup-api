package org.secuso.privacyfriendlybackup.api.util

import android.content.Intent

object ApiFormatter {

    fun formatIntent(intent: Intent) : String {
        val sb = StringBuilder()

        sb.append("[\n")

        // action
        sb.append("\tAction:\t")
        if(intent.action.isNullOrEmpty()) {
            sb.append("No Action.")
        } else {
            sb.append("${intent.action}")
        }
        sb.append('\n')

        // extras
        val keySet = intent.extras?.keySet()
        if(!keySet.isNullOrEmpty()) {
            for (key in keySet) {
                sb.append("\tExtra:\t$key:\t${intent.extras!!.get(key)}")
            }
        }

        sb.append("]")
        return sb.toString()
    }

}