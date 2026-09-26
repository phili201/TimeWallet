package com.example.timewallet.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.device.AppEntry
import com.example.timewallet.device.installedLaunchableApps
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {
    private val app get() = application as TimeWalletApp
    private val repo get() = app.repository
    private lateinit var wallet: TextView
    private lateinit var social: TextView
    private lateinit var status: TextView
    private lateinit var executor: Executor
    override fun onCreate(state: Bundle?) { super.onCreate(state); executor=ContextCompat.getMainExecutor(this); build(); refresh() }
    override fun onResume(){super.onResume();repo.tickSocialUse();refresh()}
    private fun build(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,24,20,20);setBackgroundColor(Color.rgb(8,10,14))}
        setContentView(ScrollView(this).apply{addView(root)})
        root.addView(TextView(this).apply{text="TIMEWALLET";textSize=14f;setTextColor(Color.CYAN)})
        root.addView(TextView(this).apply{text="Deine Zeit. Dein Wallet.";textSize=30f;setTextColor(Color.WHITE)})
        wallet=TextView(this).apply{textSize=34f;setTextColor(Color.WHITE)};root.addView(wallet)
        social=TextView(this).apply{textSize=17f;setTextColor(Color.LTGRAY)};root.addView(social)
        status=TextView(this).apply{textSize=14f;setTextColor(Color.LTGRAY)};root.addView(status)
        button(root,"Konto / Login"){accountDialog()};button(root,"Apps auswählen / blockieren"){appPicker()};button(root,"Nutzungsdaten erlauben"){startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))};button(root,"Accessibility-Blocking aktivieren"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))};button(root,"Produktivitäts-Session"){sessionDialog()};button(root,"Session beenden"){if(!repo.endSessionIfAllowed()) { toast("Während einer aktiven Session ist ein Nachweis erforderlich") } else refresh() };button(root,"Social-Zeit kaufen"){buyDialog()};button(root,"Aufgabe + Foto-Nachweis"){startActivity(Intent(this,EvidenceActivity::class.java))};button(root,"PIN setzen"){pinDialog()};button(root,"Biometrie"){biometricDialog()};button(root,"Notfall-Switch"){emergencyDialog()};button(root,"Datenschutz / Bedingungen"){legalDialog()}
    }
    private fun refresh(){repo.tickSocialUse();wallet.text="${repo.coins()} Coins";social.text="${repo.socialSeconds()/60} Minuten Social-Zeit";status.text=if(repo.isProductivitySessionRunning())"Fokus aktiv: ${repo.currentTask()} • ${repo.remainingSessionSeconds()} s" else "Bereit • ${if(app.supabase.isLoggedIn())"Online-Konto" else "Offline"}"}
    private fun accountDialog(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};val e=EditText(this).apply{hint="E-Mail"};val p=EditText(this).apply{hint="Passwort";inputType=129};box.addView(e);box.addView(p);AlertDialog.Builder(this).setTitle("TimeWallet Konto").setView(box).setPositiveButton("Login"){_,_->app.supabase.signIn(e.text.toString(),p.text.toString()){ok,msg->runOnUiThread{toast(msg);refresh()}}}.setNeutralButton("Registrieren"){_,_->app.supabase.signUp(e.text.toString(),p.text.toString()){_,msg->runOnUiThread{toast(msg)}}}.setNegativeButton("Passwort vergessen"){_,_->app.supabase.resetPassword(e.text.toString()){_,msg->runOnUiThread{toast(msg)}}}.show()}
    private fun appPicker(){val apps=installedLaunchableApps(this);val selected=repo.blockedPackages().toMutableSet();val labels=apps.map{it.label}.toTypedArray();val checked=apps.map{selected.contains(it.packageName)}.toBooleanArray();AlertDialog.Builder(this).setTitle("Apps sperren").setMultiChoiceItems(labels,checked){_,which,isChecked->if(isChecked)selected.add(apps[which].packageName) else selected.remove(apps[which].packageName)}.setPositiveButton("Speichern"){_,_->repo.setBlockedPackages(selected);refresh()}.setNegativeButton("Abbrechen",null).show()}
    private fun sessionDialog(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};val t=EditText(this).apply{hint="Aufgabe"};val m=EditText(this).apply{hint="Minuten";setText("25");inputType=2};box.addView(t);box.addView(m);AlertDialog.Builder(this).setTitle("Fokus starten").setView(box).setPositiveButton("Start"){_,_->repo.startSession(t.text.toString().ifBlank{"Produktive Aufgabe"},m.text.toString().toIntOrNull()?.coerceIn(1,180)?:25);refresh()}.setNegativeButton("Abbrechen",null).show()}
    private fun buyDialog(){val x=arrayOf(15 to 15,30 to 28,60 to 50);AlertDialog.Builder(this).setTitle("Social-Zeit").setItems(arrayOf("15 min / 15 Coins","30 min / 28 Coins","60 min / 50 Coins")){_,i->if(!repo.buySocialTime(x[i].first,x[i].second))toast("Zu wenig Coins oder Fokus aktiv") else refresh()}.show()}
    private fun pinDialog(){val p=EditText(this).apply{hint="4–8 Ziffern";inputType=2};AlertDialog.Builder(this).setTitle("PIN").setView(p).setPositiveButton("Speichern"){_,_->toast(if(app.security.setPin(p.text.toString()))"PIN gespeichert" else "Ungültige PIN")}.setNegativeButton("Abbrechen",null).show()}
    private fun biometricDialog(){if(!app.security.canUseBiometric()){toast("Biometrie auf diesem Gerät nicht verfügbar");return};BiometricPrompt(this,executor,object:BiometricPrompt.AuthenticationCallback(){override fun onAuthenticationSucceeded(r:BiometricPrompt.AuthenticationResult){app.security.setBiometricEnabled(true);toast("Biometrie aktiviert")}}).authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("TimeWallet Biometrie").setSubtitle("Biometrie für TimeWallet aktivieren").setNegativeButtonText("Abbrechen").build())}
    private fun emergencyDialog(){val n=!repo.isEmergencySwitchEnabled();AlertDialog.Builder(this).setTitle("Notfall-Switch").setMessage(if(n)"Notfallmodus aktivieren?" else "Notfallmodus deaktivieren?").setPositiveButton("Bestätigen"){_,_->repo.setEmergencySwitchEnabled(n);refresh()}.setNegativeButton("Abbrechen",null).show()}
    private fun legalDialog(){AlertDialog.Builder(this).setTitle("Datenschutz / Bedingungen").setMessage("Kontodaten werden für Anmeldung und Synchronisierung verarbeitet. Usage Access und Accessibility sind Systemberechtigungen. Die mitgelieferten Rechtstexte sind Vorlagen und müssen vor Veröffentlichung rechtlich geprüft und an den Betreiber angepasst werden.").setPositiveButton("OK",null).show()}
    private fun button(root:LinearLayout,s:String,a:()->Unit){root.addView(Button(this).apply{text=s;setOnClickListener{a()}})}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
