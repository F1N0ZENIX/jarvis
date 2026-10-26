package com.f1n0zenix.jarvis
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.widget.*
class MainActivity:Activity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val p=getSharedPreferences("groq",MODE_PRIVATE)
  val root=LinearLayout(this).apply { orientation=1;setPadding(35,60,35,30) }
  root.addView(TextView(this).apply { text="JARVIS 0.2.0\nWhatsApp: ligado\nTelegram: desligado\nTodos os contatos e grupos\nModo: aprovação obrigatória";textSize=19f })
  val field=EditText(this).apply { hint="Chave API Groq";inputType=129;setText(p.getString("api_key","")) }
  root.addView(field)
  root.addView(Button(this).apply { text="Salvar chave";setOnClickListener { p.edit().putString("api_key",field.text.toString()).apply() } })
  root.addView(Button(this).apply { text="Autorizar acesso a notificações";setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) } })
  setContentView(root)
 }
}