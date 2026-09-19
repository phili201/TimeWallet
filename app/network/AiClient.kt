package com.example.timewallet.network

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import kotlinx.coroutines.tasks.await

object AiClient {

    suspend fun verifyPhoto(taskType: String, photoPath: String): Float {
        val bitmap = BitmapFactory.decodeFile(photoPath)
        val image = InputImage.fromBitmap(bitmap, 0)

        val labeler = ImageLabeling.getClient()

        val labels = labeler.process(image).await()

        val productiveKeywords = listOf("Laptop", "Computer", "Book", "Desk", "Document")

        val score = labels
            .filter { it.text in productiveKeywords }
            .maxOfOrNull { it.confidence } ?: 0f

        return score
    }
}
