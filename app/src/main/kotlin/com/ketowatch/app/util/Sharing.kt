package com.ketowatch.app.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object Sharing {
    private fun authority(context: Context) = "${context.packageName}.files"

    fun shareText(context: Context, text: String, title: String = "Share via") {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        start(context, Intent.createChooser(send, title))
    }

    /** Shares text plus the meal photo (WhatsApp, Telegram, etc. show both). */
    fun shareMeal(context: Context, text: String, photoPath: String?) {
        val photo = photoPath?.let { File(it) }?.takeIf { it.exists() }
        if (photo == null) return shareText(context, text, "Share meal")
        val uri = FileProvider.getUriForFile(context, authority(context), photo)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        start(context, Intent.createChooser(send, "Share meal"))
    }

    /**
     * Shares the installed KETO WATCH app itself as an .apk file, so friends can install it
     * straight from WhatsApp, Bluetooth, Nearby Share, Xender/ShareIt, email or Drive.
     */
    fun shareApp(context: Context) {
        try {
            val dir = File(context.cacheDir, "share").apply { mkdirs() }
            val apk = File(dir, "KETO-WATCH.apk")
            File(context.applicationInfo.sourceDir).copyTo(apk, overwrite = true)
            val uri = FileProvider.getUriForFile(context, authority(context), apk)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Try KETO WATCH – snap a photo of your food (even matooke, rolex or luwombo!) " +
                        "to get calories and see if it's keto friendly. Open the attached file to install.",
                )
                clipData = ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            start(context, Intent.createChooser(send, "Share KETO WATCH app"))
        } catch (e: Exception) {
            Toast.makeText(context, "Couldn't prepare the app file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun start(context: Context, intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No app available to share with", Toast.LENGTH_SHORT).show()
        }
    }
}
