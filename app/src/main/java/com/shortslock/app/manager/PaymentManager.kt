package com.shortslock.app.manager

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Handles UPI payment intent for unlocking Shorts
 */
class PaymentManager(private val context: Context) {

    companion object {
        private const val UPI_ID = "nitronitish@fam"
        private const val AMOUNT = "49"
        private const val CURRENCY = "INR"
        private const val NOTE = "Unlock Shorts - 10 min"
    }

    /**
     * L
     * Returns true if intent was successfully launched
     */aunch UPI payment intent
    fun launchPayment(): Boolean {
        return try {
            val upiUri = buildUpiUri()
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = upiUri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Build UPI deep link URI
     * Format: upi://pay?pa=<UPI_ID>&pn=<NAME>&am=<AMOUNT>&cu=<CURRENCY>&tn=<NOTE>
     */
    private fun buildUpiUri(): Uri {
        return Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", UPI_ID)
            .appendQueryParameter("pn", "ShortsLock")
            .appendQueryParameter("am", AMOUNT)
            .appendQueryParameter("cu", CURRENCY)
            .appendQueryParameter("tn", NOTE)
            .build()
    }
}
