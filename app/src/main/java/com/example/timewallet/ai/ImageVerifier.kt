package com.example.timewallet.ai

import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Legacy verifier kept for compatibility with older callers. */
class ImageVerifier {
    private val productiveLabels = setOf(
        "laptop", "computer", "keyboard", "book", "desk",
        "paper", "document", "office", "classroom", "workstation"
    )

    suspend fun isProductive(photoPath: String): Boolean = withContext(Dispatchers.Default) {
        val bitmap = BitmapFactory.decodeFile(photoPath) ?: return@withContext false
        if (bitmap.width < 1 || bitmap.height < 1) {
            bitmap.recycle()
            return@withContext false
        }

        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        try {
            val labels = Tasks.await(labeler.process(InputImage.fromBitmap(bitmap, 0)))
            labels.any { it.text.lowercase() in productiveLabels }
        } finally {
            labeler.close()
            bitmap.recycle()
        }
    }
}
