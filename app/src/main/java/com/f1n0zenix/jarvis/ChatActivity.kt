package com.f1n0zenix.jarvis
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
class ChatActivity:Activity() {
 private lateinit var log:LinearLayout
 private lateinit var scroll:ScrollView
 private lateinit var send:Button
 private lateinit var input:EditText
 private lateinit var store:ChatStore
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  store=ChatStore(this)
  val root=LinearLayout(this).apply { orientation=1;setPadding(18,28,18,18);setBackgroundColor(Color.BLACK) }
  root.addView(TextView(this).apply { text="JARVIS  /  CHAT";setTextColor(Color.WHITE);textSize=26f;typeface=Typeface.DEFAULT_BOLD;setPadding(4,4,4,22) })
  scroll=ScrollView(this)
  log=LinearLayout(this).apply { orientation=1 }
  scroll.addView(log)
  root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  val bottom=LinearLayout(this).apply { orientation=0 }
  input=EditText(this).apply { hint="Pergunte qualquer coisa...";setHintTextColor(Color.LTGRAY);setTextColor(Color.WHITE);setSingleLine(false);maxLines=4 }
  bottom.addView(input,LinearLayout.LayoutParams(0,-2,1f))
  send=Button(this).apply { text="Enviar";setOnClickListener { submit() } }
  bottom.addView(send)
  root.addView(bottom);setContentView(root)
  store.history(100).forEach { bubble(it.first,it.second) }
 }
 private fun bubble(role:String,content:String) {
  val t=TextView(this).apply {
   text=content;textColor(if(role=="user") Color.BLACK else Color.WHITE)
   textSize=16f;setPadding(20,14,20,14)
   background=android.graphics.drawable.GradientDrawable().apply { setColor(if(role=="user") Color.WHITE else 0xFF252525.toInt());cornerRadius=22f }
  }
  val params=LinearLayout.LayoutParams(-2,-2).apply { gravity=if(role=="user") Gravity.END else Gravity.START;setMargins(0,6,0,12) }
  log.addView(t,params);scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
 }
 private fun submit() {
  val text=input.text.toString().trim();if(text.isEmpty())return
  val key=getSharedPreferences("groq",MODE_PRIVATE).getString("api_key","").orEmpty()
  if(key.isEmpty()) { Toast.makeText(this,"Salve sua chave Groq na tela inicial",Toast.LENGTH_LONG).show();return }
  input.setText("");send.isEnabled=false;store.add("user",text);bubble("user",text)
  AssistantClient.ask(key,store.history(30)) { result -> runOnUiThread {
   send.isEnabled=true
   result.onSuccess { store.add("assistant",it);bubble("assistant",it) }.onFailure { Toast.makeText(this,"Erro: "+it.message,Toast.LENGTH_LONG).show() }
  } }
 }
}