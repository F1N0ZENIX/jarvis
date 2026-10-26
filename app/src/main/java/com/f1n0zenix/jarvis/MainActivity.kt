package com.f1n0zenix.jarvis
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
class MainActivity:Activity() {
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val settings=getSharedPreferences("settings",MODE_PRIVATE)
  val dark=settings.getBoolean("dark_mode",true)
  val bg=if(dark)Color.BLACK else 0xFFF4F7FF.toInt()
  val card=if(dark)0xFF202020.toInt() else Color.WHITE
  val fg=if(dark)Color.WHITE else 0xFF142B3E.toInt()
  val blue=0xFF1675DB.toInt()
  fun shape(color:Int)=GradientDrawable().apply { setColor(color);cornerRadius=dp(22).toFloat() }
  fun text(value:String,size:Float=16f)=TextView(this).apply { text=value;textSize=size;setTextColor(fg);setPadding(dp(6),dp(9),dp(6),dp(9)) }
  fun button(value:String,action:()->Unit)=Button(this).apply { text=value;isAllCaps=false;setOnClickListener { action() } }
  window.statusBarColor=bg;window.navigationBarColor=bg
  window.decorView.systemUiVisibility=if(dark)0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
  val root=LinearLayout(this).apply { orientation=1;setPadding(dp(20),dp(22),dp(20),dp(32));setBackgroundColor(bg) }
  root.addView(text("Configurações",28f).apply { typeface=Typeface.DEFAULT_BOLD })
  root.addView(text("JARVIS 0.9.0 • Android nativo • Sem Gateway",13f))
  fun section(title:String,fill:(LinearLayout)->Unit) {
   root.addView(text(title,14f).apply { setTextColor(blue);typeface=Typeface.DEFAULT_BOLD })
   val panel=LinearLayout(this).apply { orientation=1;setPadding(dp(16),dp(12),dp(16),dp(12));background=shape(card) }
   fill(panel);root.addView(panel)
   root.addView(View(this).apply { layoutParams=LinearLayout.LayoutParams(1,dp(18)) })
  }
  section("APARÊNCIA") { p ->
   p.addView(Switch(this).apply { text="Modo escuro AMOLED";setTextColor(fg);isChecked=dark;setOnCheckedChangeListener { _,value -> settings.edit().putBoolean("dark_mode",value).apply();recreate() } })
  }
  section("ATIVAÇÃO POR VOZ") { p ->
   p.addView(text("Diga Jarvis para chamar o assistente. O Android pode interromper a escuta em segundo plano.",13f))
   p.addView(Switch(this).apply {
    text="Ouvir a palavra Jarvis"
    setTextColor(fg)
    isChecked=settings.getBoolean("wake_enabled",false)
    setOnCheckedChangeListener { _,enabled ->
     if(enabled) {
      if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=android.content.pm.PackageManager.PERMISSION_GRANTED) {
       isChecked=false
       requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),302)
      } else {
       try {
        startForegroundService(Intent(this@MainActivity,WakeWordService::class.java))
        settings.edit().putBoolean("wake_enabled",true).apply()
       } catch(e:Exception) {
        isChecked=false
        Toast.makeText(this@MainActivity,"Não foi possível iniciar a escuta: "+e.message,Toast.LENGTH_LONG).show()
       }
      }
     } else {
      stopService(Intent(this@MainActivity,WakeWordService::class.java))
      settings.edit().putBoolean("wake_enabled",false).apply()
     }
    }
   })
  }
  section("INTELIGÊNCIA ARTIFICIAL") { p ->
   p.addView(text("Chave Groq",14f))
   val prefs=getSharedPreferences("groq",MODE_PRIVATE)
   val field=EditText(this).apply { hint="Chave de API";setTextColor(fg);setHintTextColor(if(dark)Color.LTGRAY else Color.GRAY);inputType=129;setSingleLine(true);setText(prefs.getString("api_key","")) }
   p.addView(field)
   p.addView(button("Salvar chave") { prefs.edit().putString("api_key",field.text.toString().trim()).apply();Toast.makeText(this,"Chave salva",Toast.LENGTH_SHORT).show() })
  }
  section("NOTIFICAÇÕES E WHATSAPP") { p ->
   p.addView(Switch(this).apply { text="Monitorar WhatsApp";setTextColor(fg);isChecked=settings.getBoolean("notifications_enabled",true);setOnCheckedChangeListener { _,value -> settings.edit().putBoolean("notifications_enabled",value).apply() } })
   p.addView(button("Autorizar leitura de notificações") { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) })
   if(Build.VERSION.SDK_INT>=33)p.addView(button("Permitir avisos") { requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),101) })
  }
  section("MINHA SALA") { p ->
   p.addView(text("Verifica atualizações do index.html no GitHub; ainda não interpreta os prazos.",13f))
   p.addView(Switch(this).apply { text="Verificar a cada 6 horas";setTextColor(fg);isChecked=settings.getBoolean("school_enabled",false);setOnCheckedChangeListener { _,value -> settings.edit().putBoolean("school_enabled",value).apply();SchoolScheduler.set(this@MainActivity,value) } })
   p.addView(button("Verificar agora") { SchoolChecker.check(this@MainActivity) { result -> runOnUiThread { Toast.makeText(this@MainActivity,result.getOrElse { "Falha: "+it.message },Toast.LENGTH_LONG).show() } } })
  }
  section("AGENDA E DADOS") { p ->
   p.addView(button("Abrir agenda e lembretes") { startActivity(Intent(this,AgendaActivity::class.java)) })
   p.addView(button("Apagar histórico do chat") { ChatStore(this).clear();Toast.makeText(this,"Histórico apagado",Toast.LENGTH_SHORT).show() })
  }
  setContentView(ScrollView(this).apply { isFillViewport=true;addView(root) })
 }
}