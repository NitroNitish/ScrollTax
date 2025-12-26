
package com.example.shortslock

import android.content.Context
import android.content.Intent
import android.net.Uri

class PaymentManager(private val context: Context) {

    fun launchUpiIntent() {
        val uri = Uri.parse("upi://pay?pa=nitronitish@fam&pn=Nitish&am=49&cu=INR&tn=Unlock%20Shorts%20for%2010%20min")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}
