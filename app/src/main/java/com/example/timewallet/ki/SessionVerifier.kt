package com.example.timewallet.ki

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

data class ProductivityScore(
    val score: Int,
    val reason: String
)

class SessionVerifier {

    private suspend fun detectFace(bitmap: Bitmap): Boolean {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )
        val faces = detector.process(InputImage.fromBitmap(bitmap, 0)).await()
        return faces.isNotEmpty()
    }

    private suspend fun detectScene(bitmap: Bitmap): Boolean {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val labels = labeler.process(InputImage.fromBitmap(bitmap, 0)).await()
        val names = labels.map { it.text.lowercase() }
        val productive = listOf("laptop", "computer", "desk", "book", "notebook", "paper")
        return names.any { it in productive }
    }

    suspend fun calculateScore(bitmap: Bitmap): ProductivityScore {
        var score = 0
        val reasons = mutableListOf<String>()

        val face = detectFace(bitmap)
        if (face) {
            score += 40
            reasons.add("Gesicht erkannt")
        } else {
            reasons.add("Kein Gesicht erkannt")
        }

        val scene = detectScene(bitmap)
        if (scene) {
            score += 40
            reasons.add("Produktive Umgebung erkannt")
        } else {
            reasons.add("Keine produktive Umgebung erkannt")
        }

        val brightness = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2) and 0xFF
        if (brightness > 40) {
            score += 10
            reasons.add("Bild hell genug")
        } else {
            reasons.add("Bild zu dunkel")
        }

        val sharp = bitmap.width > 200 && bitmap.height > 200
        if (sharp) {
            score += 10
            reasons.add("Bild scharf genug")
        } else {
            reasons.add("Bild unscharf")
        }

        return ProductivityScore(score, reasons.joinToString(", "))
    }
}
