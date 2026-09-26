package com.example.timewallet.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.timewallet.TimeWalletApp

class MainActivity : ComponentActivity() {
    private val app get() = application as TimeWalletApp
    private val repo get() = app.repository
    private lateinit var root: LinearLayout
    private lateinit var wallet: TextView
    private lateinit var social: TextView
    private lateinit var status: TextView
    private val white=Color.rgb(245,247,250); private val muted=Color.rgb(160,170,184); private val bg=Color.rgb(8,10,14)
    override fun onCreate(state: Bundle?) { super.onCreate(state); build(); refresh() }
    override fun onResume(){super.onResume();repo.tickSocialUse();refresh()}
    private fun build(){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,24,20,20);setBackgroundColor(bg)}
        setContentView(ScrollView(this).apply{addView(root)})
        text("TIMEWALLET",14,Color.CYAN,true);text("Deine Zeit. Dein Wallet.",30,white,true);text("Aufgaben → Coins → Social-Zeit",15,muted,false)
        wallet=TextView(this).apply{textSize=34f;setTextColor(white)};root.addView(wallet)
        social=TextView(this).apply{textSize=16f;setTextColor(muted)};root.addView(social)
        status=TextView(this).apply{textSize=14f;setTextColor(muted)};root.addView(status)
        button("Konto / Login"){accountDialog()};button("Produktivitäts-Session"){sessionDialog()};button("Social-Zeit kaufen"){buyDialog()};button("Aufgabe + Foto-Nachweis"){startActivity(Intent(this,EvidenceActivity::class.java))};button("Apps blockieren"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))};button("Nutzungsdaten erlauben"){startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))};button("PIN setzen"){pinDialog()};button("Notfall-Switch"){emergencyDialog()};button("Datenschutz / Bedingungen"){legalDialog()}
    }
    private fun refresh(){wallet.text="${repo.coins()} Coins";social.text="${repo.socialSeconds()/60} Minuten Social-Zeit";status.text=if(repo.isProductivitySessionRunning())"Fokus aktiv: ${repo.currentTask()} • ${repo.remainingSessionSeconds()} s" else "Bereit • ${if(app.supabase.userId()!=null)"Online-Konto" else "Offline"}"}
    private fun accountDialog(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};val e=EditText(this).apply{hint="E-Mail"};val p=EditText(this).apply{hint="Passwort";inputType=129};box.addView(e);box.addView(p);AlertDialog.Builder(this).setTitle("TimeWallet Konto").setView(box).setPositiveButton("Login"){_,_->app.supabase.signIn(e.text.toString(),p.text.toString()){ok,msg->runOnUiThread{toast(msg);refresh()}}}.setNeutralButton("Registrieren"){_,_->app.supabase.signUp(e.text.toString(),p.text.toString()){_,msg->runOnUiThread{toast(msg)}}}.setNegativeButton("Passwort vergessen"){_,_->app.supabase.resetPassword(e.text.toString()){_,msg->runOnUiThread{toast(msg)}}}.show()}
    private fun sessionDialog(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};val t=EditText(this).apply{hint="Aufgabe"};val m=EditText(this).apply{hint="Minuten";setText("25");inputType=2};box.addView(t);box.addView(m);AlertDialog.Builder(this).setTitle("Fokus starten").setView(box).setPositiveButton("Start"){_,_->repo.startSession(t.text.toString().ifBlank{"Produktive Aufgabe"},m.text.toString().toIntOrNull()?.coerceIn(1,180)?:25);refresh()}.setNegativeButton("Abbrechen",null).show()}
    private fun buyDialog(){val x=arrayOf(15 to 15,30 to 28,60 to 50);AlertDialog.Builder(this).setTitle("Social-Zeit").setItems(arrayOf("15 min / 15 Coins","30 min / 28 Coins","60 min / 50 Coins")){_,i->if(!repo.buySocialTime(x[i].first,x[i].second))toast("Zu wenig Coins oder Fokus aktiv") else refresh()}.show()}
    private fun pinDialog(){val p=EditText(this).apply{hint="4–8 Ziffern";inputType=2};AlertDialog.Builder(this).setTitle("PIN").setView(p).setPositiveButton("Speichern"){_,_->toast(if(app.security.setPin(p.text.toString()))"PIN gespeichert" else "Ungültige PIN")}.setNegativeButton("Abbrechen",null).show()}
    private fun emergencyDialog(){val n=!repo.isEmergencySwitchEnabled();AlertDialog.Builder(this).setTitle("Notfall-Switch").setMessage(if(n)"Notfallmodus aktivieren?" else "Notfallmodus deaktivieren?").setPositiveButton("Bestätigen"){_,_->repo.setEmergencySwitchEnabled(n);refresh()}.setNegativeButton("Abbrechen",null).show()}
    private fun legalDialog(){AlertDialog.Builder(this).setTitle("Datenschutz").setMessage("Kontodaten werden für Anmeldung und Synchronisierung verarbeitet. Android Usage Access und Accessibility sind Systemberechtigungen für Blocking. Die mitgelieferten Rechtstexte müssen vor einer Veröffentlichung rechtlich geprüft und auf den Betreiber angepasst werden.").setPositiveButton("OK",null).show()}
    private fun button(s:String,a:()->Unit){root.addView(Button(this).apply{text=s;setTextColor(white);setOnClickListener{a()}})}
    private fun text(s:String,z:Int,c:Int,b:Boolean){root.addView(TextView(this).apply{text=s;textSize=z.toFloat();setTextColor(c);if(b)typeface=android.graphics.Typeface.DEFAULT_BOLD})}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
