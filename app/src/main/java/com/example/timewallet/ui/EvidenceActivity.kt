package com.example.timewallet.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.timewallet.TimeWalletApp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class EvidenceActivity : ComponentActivity() {
    private val app get() = application as TimeWalletApp
    private val pick = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let(::inspect) }
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val b = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,24,24,24) }
        b.addView(TextView(this).apply { text = "Nachweis prüfen"; textSize = 26f })
        b.addView(TextView(this).apply { text = "Foto oder Bildnachweis wird lokal analysiert. Die Datei wird nicht automatisch hochgeladen." })
        b.addView(Button(this).apply { text = "Foto/Bild auswählen"; setOnClickListener { pick.launch("image/*") } })
        setContentView(b)
    }
    private fun inspect(uri: Uri) {
        val image = runCatching { InputImage.fromFilePath(this, uri) }.getOrNull() ?: return toast("Datei nicht lesbar")
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        var labelsText = ""
        var ocrText = ""
        var done = 0
        fun finishOne() { done++; if (done < 2) return; val combined = (labelsText + " " + ocrText).trim(); val credible = combined.isNotBlank(); AlertDialog.Builder(this).setTitle("Lokale KI-Prüfung").setMessage(if (credible) "Nachweis analysiert. Erkannter Inhalt:\n${combined.take(1000)}" else "Kein verwertbarer Inhalt erkannt.").setPositiveButton(if (credible) "Nachweis bestätigen" else "Schließen") { _, _ -> if (credible) { app.repository.awardCoins(25,"Lokaler Nachweis bestätigt"); toast("25 Coins gutgeschrieben") }; finish() }.setNegativeButton("Ablehnen", null).show() }
        labeler.process(image).addOnSuccessListener { labelsText = it.take(8).joinToString(", ") { l -> l.text }; finishOne() }.addOnFailureListener { finishOne() }
        recognizer.process(image).addOnSuccessListener { ocrText = it.text; finishOne() }.addOnFailureListener { finishOne() }
    }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
