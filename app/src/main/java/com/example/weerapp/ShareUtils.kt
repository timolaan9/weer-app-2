package com.example.weerapp

import android.content.Context
import android.graphics.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun addWeatherOverlay(base: Bitmap, lines: List<String>): Bitmap {
    val result = base.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)

    val padding = 32f
    val boxPaint = Paint().apply {
        color = Color.argb(170, 0, 0, 0)
        isAntiAlias = true
    }
    val titlePaint = Paint().apply {
        color = Color.WHITE
        textSize = 44f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    val lineHeight = 52f
    val boxHeight = padding + (lines.size * lineHeight) + padding
    val boxWidth = result.width - (padding * 2)

    val rect = RectF(padding, padding, padding + boxWidth, padding + boxHeight)
    canvas.drawRoundRect(rect, 24f, 24f, boxPaint)

    var y = padding + 70f
    lines.forEachIndexed { i, line ->
        val p = if (i == 0) titlePaint else textPaint
        canvas.drawText(line, padding + 24f, y, p)
        y += lineHeight
    }

    return result
}

fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap) = run {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "weer_share.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
