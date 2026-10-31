package com.repforge.app.nativev2.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.repforge.app.nativev2.data.db.WorkoutHistoryEntity
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility for generating and exporting individual workout summary reports as PDF documents
 * using Android's native PdfDocument framework.
 */
object WorkoutPdfExporter {

    /**
     * Generates an A4 PDF document for the given workout history entity and returns the generated File path.
     */
    fun generateWorkoutPdf(context: Context, workout: WorkoutHistoryEntity): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Color palette
        val primaryGold = Color.parseColor("#E5B800")
        val darkBg = Color.parseColor("#121318")
        val cardBg = Color.parseColor("#1E2028")
        val textWhite = Color.parseColor("#F0F2F5")
        val textMuted = Color.parseColor("#8A8F9E")
        val successGreen = Color.parseColor("#2EA043")

        // Draw background
        val bgPaint = Paint().apply { color = darkBg }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Header Banner Card
        val headerPaint = Paint().apply { color = cardBg }
        canvas.drawRect(20f, 20f, 575f, 110f, headerPaint)

        // Header Title
        val titlePaint = Paint().apply {
            color = primaryGold
            textSize = 22f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("TRAINPILOT NATIVE V2", 36f, 52f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = textWhite
            textSize = 14f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("EDZÉS ÖSSZEFOGLALÓ JELENTÉS", 36f, 74f, subtitlePaint)

        // Date formatting
        val formattedDate = runCatching {
            val instant = Instant.ofEpochMilli(workout.finishedAtEpochMs)
            DateTimeFormatter.ofPattern("yyyy. MMMM dd. HH:mm", Locale("hu", "HU"))
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }.getOrDefault("2026. október 30.")

        val datePaint = Paint().apply {
            color = textMuted
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText("Dátum: $formattedDate", 36f, 95f, datePaint)

        // Workout Title & Metric Highlights Banner
        var yPos = 140f

        val workoutTitlePaint = Paint().apply {
            color = textWhite
            textSize = 18f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(workout.title, 36f, yPos, workoutTitlePaint)
        yPos += 30f

        // 3 Key Metric Tiles (Duration, Volume, Exercises)
        val durationMin = workout.durationSeconds / 60
        val totalVolKg = workout.totalVolumeKg.toInt()

        val tilePaint = Paint().apply { color = cardBg }
        canvas.drawRect(36f, yPos, 200f, yPos + 60f, tilePaint)
        canvas.drawRect(210f, yPos, 380f, yPos + 60f, tilePaint)
        canvas.drawRect(390f, yPos, 560f, yPos + 60f, tilePaint)

        val labelPaint = Paint().apply {
            color = textMuted
            textSize = 10f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = primaryGold
            textSize = 16f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Tile 1
        canvas.drawText("EDZÉSIDŐ", 48f, yPos + 22f, labelPaint)
        canvas.drawText("$durationMin perc", 48f, yPos + 46f, valuePaint)

        // Tile 2
        canvas.drawText("ÖSSZES VOLUMEN", 222f, yPos + 22f, labelPaint)
        canvas.drawText("$totalVolKg kg", 222f, yPos + 46f, valuePaint)

        // Tile 3
        canvas.drawText("PULZUS / CÉL", 402f, yPos + 22f, labelPaint)
        val hrValuePaint = Paint(valuePaint).apply { color = successGreen }
        canvas.drawText("Optimal HR", 402f, yPos + 46f, hrValuePaint)

        yPos += 85f

        // Table Header
        canvas.drawText("GYAKORLATOK ÉS MUNKASOROZATOK", 36f, yPos, subtitlePaint)
        yPos += 15f

        // Divider Line
        val linePaint = Paint().apply {
            color = primaryGold
            strokeWidth = 2f
        }
        canvas.drawLine(36f, yPos, 560f, yPos, linePaint)
        yPos += 25f

        // Parse & Draw Summary Content Text
        val summaryLines = workout.notes.ifBlank { "Sikeresen befejezett edzésmunkamenet." }
            .split("\n")

        val bodyPaint = Paint().apply {
            color = textWhite
            textSize = 11f
            isAntiAlias = true
        }

        summaryLines.forEach { line ->
            if (yPos > 800f) return@forEach // Fit to single page
            val trimmed = line.trim()
            if (trimmed.startsWith("•") || trimmed.startsWith("Gyakorlat") || trimmed.contains(":")) {
                bodyPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                bodyPaint.color = primaryGold
            } else {
                bodyPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                bodyPaint.color = textWhite
            }
            canvas.drawText(trimmed.take(80), 36f, yPos, bodyPaint)
            yPos += 18f
        }

        // Footer Brand Stamp
        val footerPaint = Paint().apply {
            color = textMuted
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("Generálva a TrainPilot Native V2 alkalkamazásból • Multi-device Health Connect Sync", 36f, 820f, footerPaint)

        pdfDocument.finishPage(page)

        // Save PDF file to cache directory
        val exportDir = File(context.cacheDir, "pdf_exports").apply { mkdirs() }
        val fileName = "TrainPilot_Edzes_${workout.id}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(exportDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Generates the PDF report and launches the Android System Share / View Intent.
     */
    fun exportAndShareWorkoutPdf(context: Context, workout: WorkoutHistoryEntity): File {
        val file = generateWorkoutPdf(context, workout)
        runCatching {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "TrainPilot Edzés Jelentés - ${workout.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Edzés PDF megosztása"))
        }
        return file
    }
}
