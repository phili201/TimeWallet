package com.example.timewallet.ui

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.timewallet.TimeWalletApp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class EvidenceActivity : ComponentActivity() {
    private val app get() = application as TimeWalletApp
    private val pick = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { inspect(it) } }
    override fun onCreate(state: Bundle?) { super.onCreate(state); val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,24,24,24)}; val title=TextView(this).apply{text="Nachweis prüfen";textSize=26f};b.addView(title);b.addView(TextView(this).apply{text="Wähle ein Foto als lokalen Nachweis. Das Bild wird nicht automatisch hochgeladen."});b.addView(Button(this).apply{text="Foto auswählen";setOnClickListener{pick.launch("image/*")}});setContentView(b) }
    private fun inspect(uri:Uri){ val image=runCatching{InputImage.fromFilePath(this,uri)}.getOrNull()?:return toast("Datei nicht lesbar"); ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS).process(image).addOnSuccessListener{labels->val summary=labels.take(8).joinToString(", "){it.text};AlertDialog.Builder(this).setTitle("Lokale KI-Prüfung").setMessage(if(summary.isBlank())"Kein eindeutiger Inhalt erkannt." else "Erkannt: $summary\n\nDie KI liefert nur eine Hilfestellung. Die Gutschrift erfordert deine Bestätigung.").setPositiveButton("Bestätigen"){_,_->app.repository.awardCoins(25,"Nachweis bestätigt");toast("25 Coins gutgeschrieben");finish()}.setNegativeButton("Ablehnen",null).show()}.addOnFailureListener{toast("Lokale Prüfung fehlgeschlagen")}}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
