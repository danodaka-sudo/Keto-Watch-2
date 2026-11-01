package com.ketowatch.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

object Images {
    /** Long edge sent to the AI. Big enough to recognise food, small enough for slow mobile data. */
    private const val MAX_EDGE = 1024

    /**
     * Reads a photo, fixes its rotation, shrinks it, saves a copy in the app's private
     * meal-photo folder and returns (savedFile, jpegBytes).
     */
    fun prepare(context: Context, uri: Uri): Pair<File, ByteArray> {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: error("Can't open photo")
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2

        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("Can't read photo")

        val rotation = runCatching {
            resolver.openInputStream(uri)?.use { s ->
                when (ExifInterface(s).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        }.getOrDefault(0f)

        val scale = MAX_EDGE.toFloat() / max(decoded.width, decoded.height)
        val matrix = Matrix().apply {
            if (scale < 1f) postScale(scale, scale)
            if (rotation != 0f) postRotate(rotation)
        }
        val finalBmp = if (scale < 1f || rotation != 0f) {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true).also {
                if (it !== decoded) decoded.recycle()
            }
        } else decoded

        val bytes = ByteArrayOutputStream().use { out ->
            finalBmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.toByteArray()
        }
        finalBmp.recycle()

        val dir = File(context.filesDir, "meals").apply { mkdirs() }
        val file = File(dir, "meal_${System.currentTimeMillis()}.jpg")
        file.writeBytes(bytes)
        return file to bytes
    }

    /** Small, memory-friendly bitmap for list thumbnails. */
    fun thumbnail(path: String, targetPx: Int): Bitmap? = runCatching {
        val b = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, b)
        var sample = 1
        while (max(b.outWidth, b.outHeight) / (sample * 2) >= targetPx) sample *= 2
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }.getOrNull()
}
