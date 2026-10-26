package com.f1n0zenix.jarvis
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.widget.*
class MainActivity:Activity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val prefs=getSharedPreferences("groq",MODE_PRIVATE)
  val settingsPrefs=getSharedPreferences("settings",MODE_PRIVATE)
  val dark=settingsPrefs.getBoolean("dark_mode",false)
  val background=if(dark) Color.BLACK else 0xFFF4F7FF.toInt()
  val foreground=if(dark) Color.WHITE else 0xFF142B3E.toInt()
  window.statusBarColor=background;window.navigationBarColor=background
  val root=LinearLayout(this).apply { orientation=1;setPadding(24,40,24,24);setBackgroundColor(background) }
  fun text(value:String,size:Float=16f)=TextView(this).apply { text=value;setTextColor(foreground);textSize=size;setPadding(0,8,0,12) }
  fun button(value:String,click:()->Unit)=Button(this).apply { text=value;isAllCaps=false;setOnClickListener { click() } }
  root.addView(text("JARVIS.",34f).apply { typeface=Typeface.DEFAULT_BOLD })
  root.addView(text("ASSISTENTE PESSOAL  •  0.7.0",13f))
  root.addView(Switch(this).apply { text="Modo escuro (AMOLED)";setTextColor(foreground);isChecked=dark;setOnCheckedChangeListener { _,checked -> settingsPrefs.edit().putBoolean("dark_mode",checked).apply();recreate() } })
  root.addView(button("✦  Abrir chat com JARVIS") { startActivity(Intent(this,ChatActivity::class.java)) })
  root.addView(text("WHATSAPP  •  MODO APPROVE\nTodos os contatos e grupos. Envio só após confirmação.",15f))
  root.addView(text("CONFIGURAÇÃO DA IA",13f))
  val field=EditText(this).apply { hint="Chave Groq";setHintTextColor(if(dark) Color.LTGRAY else Color.GRAY);setTextColor(Color.WHITE);inputType=129;setText(prefs.getString("api_key","")) }
  root.addView(field)
  root.addView(button("Salvar chave Groq") { prefs.edit().putString("api_key",field.text.toString().trim()).apply();Toast.makeText(this,"Chave salva",Toast.LENGTH_SHORT).show() })
  val notifyPrefs=getSharedPreferences("settings",MODE_PRIVATE)
  root.addView(Switch(this).apply { text="Monitoramento de WhatsApp ativo";setTextColor(Color.WHITE);isChecked=notifyPrefs.getBoolean("notifications_enabled",true);setOnCheckedChangeListener { _,on -> notifyPrefs.edit().putBoolean("notifications_enabled",on).apply();Toast.makeText(this@MainActivity,if(on)"Monitoramento ativado" else "Monitoramento pausado",Toast.LENGTH_SHORT).show() } })
  root.addView(button("Permitir leitura de notificações") { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) })
  if(Build.VERSION.SDK_INT>=33)root.addView(button("Permitir avisos") { requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1) })
  root.addView(button("Agenda e compromissos") { startActivity(Intent(this,AgendaActivity::class.java)) })
  root.addView(text("MEMÓRIA LOCAL\nO histórico do chat fica no dispositivo. Não é enviado à nuvem, exceto o contexto necessário a cada consulta Groq.",14f))
  root.addView(button("Apagar histórico de chat") { ChatStore(this).clear();Toast.makeText(this,"Histórico apagado",Toast.LENGTH_SHORT).show() })
  root.addView(text("Minha Sala, Gmail, voz e lembretes automáticos exigem módulos e permissões adicionais; ainda não estão ativos.",13f))
  setContentView(ScrollView(this).apply { addView(root) })
 }
}