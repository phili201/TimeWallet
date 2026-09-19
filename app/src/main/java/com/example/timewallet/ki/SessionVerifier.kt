package com.example.timewallet.ki

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class SessionVerifier {

    suspend fun verifySession(bitmap: Bitmap): Boolean {

        // --- Gesichtserkennung ---
        val faceDetector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )

        val faces = faceDetector.process(InputImage.fromBitmap(bitmap, 0)).await()
        val faceDetected = faces.isNotEmpty()

        // --- Szenenerkennung ---
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val labels = labeler.process(InputImage.fromBitmap(bitmap, 0)).await()

        val labelNames = labels.map { it.text.lowercase() }

        val productiveObjects = listOf("laptop", "computer", "desk", "book", "notebook", "paper")

        val productiveDetected = labelNames.any { it in productiveObjects }

        // --- Entscheidungslogik ---
        return faceDetected && productiveDetected
    }
}

suspend fun calculateScore(bitmap: Bitmap): ProductivityScore {

    var score = 0
    val reasons = mutableListOf<String>()

    // --- Gesichtserkennung ---
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )
    val faces = faceDetector.process(InputImage.fromBitmap(bitmap, 0)).await()
    val faceDetected = faces.isNotEmpty()

    if (faceDetected) {
        score += 40
        reasons.add("Gesicht erkannt")
    } else {
        reasons.add("Kein Gesicht erkannt")
    }

    // --- Szenenerkennung ---
    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    val labels = labeler.process(InputImage.fromBitmap(bitmap, 0)).await()
    val labelNames = labels.map { it.text.lowercase() }

    val productiveObjects = listOf("laptop", "computer", "desk", "book", "notebook", "paper")

    val productiveDetected = labelNames.any { it in productiveObjects }

    if (productiveDetected) {
        score += 40
        reasons.add("Produktive Umgebung erkannt")
    } else {
        reasons.add("Keine produktive Umgebung erkannt")
    }

    // --- Dunkelheits-Check ---
    val brightness = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2) and 0xFF
    if (brightness > 40) {
        score += 10
        reasons.add("Bild ausreichend hell")
    } else {
        reasons.add("Bild zu dunkel")
    }

    // --- Unschärfe-Check ---
    val sharpEnough = bitmap.width > 200 && bitmap.height > 200
    if (sharpEnough) {
        score += 10
        reasons.add("Bild scharf genug")
    } else {
        reasons.add("Bild zu unscharf")
    }

    return ProductivityScore(score, reasons.joinToString(", "))
}

try {
    // KI Code
} catch (e: Exception) {
    return ProductivityScore(0, "KI-Fehler: ${e.message}")
}

try {
    // KI Code
} catch (e: Exception) {
    return ProductivityScore(0, "KI-Fehler: ${e.message}")
}
