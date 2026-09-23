package com.example.timewallet.ki

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

data class ProductivityScore(val score: Int, val reason: String)

class SessionVerifier {
    private suspend fun detectFace(bitmap: Bitmap): Boolean {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )
        return try {
            detector.process(InputImage.fromBitmap(bitmap, 0)).await().isNotEmpty()
        } finally { detector.close() }
    }

    private suspend fun detectScene(bitmap: Bitmap): Set<String> {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        return try {
            labeler.process(InputImage.fromBitmap(bitmap, 0)).await()
                .map { it.text.lowercase() }.toSet()
        } finally { labeler.close() }
    }

    suspend fun calculateScore(bitmap: Bitmap, task: String = ""): ProductivityScore {
        if (bitmap.width < 200 || bitmap.height < 200)
            return ProductivityScore(0, "Bild ist zu klein")

        val reasons = mutableListOf<String>()
        var score = 0
        if (detectFace(bitmap)) { score += 35; reasons += "Gesicht erkannt" }
        else reasons += "Kein Gesicht erkannt"

        val labels = detectScene(bitmap)
        val productive = setOf("laptop", "computer", "keyboard", "desk", "book", "notebook", "paper", "document", "office", "classroom", "workstation")
        val matches = labels.intersect(productive)
        if (matches.isNotEmpty()) { score += 45; reasons += "Arbeitsumgebung erkannt (${matches.take(3).joinToString()})" }
        else reasons += "Keine typische Arbeitsumgebung erkannt"

        val taskLower = task.lowercase()
        val taskKeywords = when {
            "vokabel" in taskLower || "lesen" in taskLower -> setOf("book", "notebook", "paper", "document")
            "coden" in taskLower || "programm" in taskLower -> setOf("laptop", "computer", "keyboard", "workstation")
            "schule" in taskLower || "mathe" in taskLower -> setOf("book", "notebook", "paper", "document", "calculator")
            else -> emptySet()
        }
        if (taskKeywords.isNotEmpty() && labels.intersect(taskKeywords).isNotEmpty()) {
            score += 10
            reasons += "Aufgabe passend zum Bild"
        } else if (taskKeywords.isNotEmpty()) {
            reasons += "Aufgabe nicht eindeutig im Bild erkennbar"
        }

        val center = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        val brightness = ((center shr 16 and 0xFF) + (center shr 8 and 0xFF) + (center and 0xFF)) / 3
        if (brightness > 35) { score += 10; reasons += "Bild ausreichend hell" }
        else reasons += "Bild sehr dunkel"

        score += 10
        reasons += "Bildqualität ausreichend"
        return ProductivityScore(score.coerceIn(0, 100), reasons.joinToString(", "))
    }
}
