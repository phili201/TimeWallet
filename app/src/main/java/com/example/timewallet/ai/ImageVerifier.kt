package com.example.timewallet.ai

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabelerOptions
import com.google.mlkit.vision.label.ImageLabeling
import kotlinx.coroutines.tasks.await
import com.google.mlkit.vision.label.ImageLabelerOptions
import kotlinx.coroutines.tasks.await


class ImageVerifier {

    private val productiveLabels = listOf(
        "Laptop", "Computer", "Keyboard", "Book", "Desk",
        "Paper", "Document", "Office", "Classroom", "Workstation"
    )

    suspend fun isProductive(photoPath: String): Boolean {
        val bitmap = BitmapFactory.decodeFile(photoPath)
        val image = InputImage.fromBitmap(bitmap, 0)

        val labeler = ImageLabeling.getClient(
            ImageLabelerOptions.DEFAULT_OPTIONS
        )

        val labels = labeler.process(image).await()

        for (label in labels) {
            if (productiveLabels.contains(label.text)) {
                return true
            }
        }

        return false
    }
}
