package com.f1n0zenix.jarvis
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
class ChatActivity:Activity() {
 private lateinit var log:LinearLayout
 private lateinit var scroll:ScrollView
 private lateinit var send:TextView
 private lateinit var input:EditText
 private lateinit var store:ChatStore
 private val ink=0xFF142B3E.toInt()
 private val blue=0xFF1675DB.toInt()
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun shape(color:Int,r:Int)=GradientDrawable().apply { setColor(color);cornerRadius=dp(r).toFloat() }
 private fun label(s:String,size:Float,color:Int=ink)=TextView(this).apply { text=s;textSize=size;setTextColor(color) }
 private fun rowGap(h:Int)=View(this).apply { layoutParams=LinearLayout.LayoutParams(1,dp(h)) }
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  window.statusBarColor=Color.WHITE;window.navigationBarColor=Color.WHITE
  window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
  window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
  store=ChatStore(this)
  val root=LinearLayout(this).apply { orientation=1;setBackgroundColor(0xFFF4F7FF.toInt()) }
  val header=LinearLayout(this).apply { orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(22),dp(14),dp(18),dp(14));setBackgroundColor(Color.WHITE) }
  header.addView(label("JARVIS",24f).apply { typeface=Typeface.DEFAULT_BOLD },LinearLayout.LayoutParams(0,-2,1f))
  val settings=label("⚙",27f,blue).apply { gravity=Gravity.CENTER;background=shape(0xFFEDF4FF.toInt(),28);setOnClickListener { startActivity(Intent(this@ChatActivity,MainActivity::class.java)) } }
  header.addView(settings,LinearLayout.LayoutParams(dp(48),dp(48)))
  root.addView(header)
  scroll=ScrollView(this).apply { fillViewport=true;clipToPadding=false }
  log=LinearLayout(this).apply { orientation=1;setPadding(dp(18),dp(22),dp(18),dp(18)) }
  scroll.addView(log);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  val composer=LinearLayout(this).apply { orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(14),dp(8),dp(14),dp(8));background=shape(Color.WHITE,32);elevation=dp(6).toFloat() }
  input=EditText(this).apply { hint="Digite sua mensagem...";setHintTextColor(0xFF8793A1.toInt());setTextColor(ink);textSize=15f;minLines=1;maxLines=4;background=null;setPadding(dp(10),dp(10),dp(6),dp(10)) }
  composer.addView(input,LinearLayout.LayoutParams(0,-2,1f))
  send=label("➤",24f,Color.WHITE).apply { gravity=Gravity.CENTER;background=shape(blue,30);setOnClickListener { submit() } }
  composer.addView(send,LinearLayout.LayoutParams(dp(52),dp(52)))
  val wrap=LinearLayout(this).apply { setPadding(dp(18),dp(12),dp(18),dp(18));addView(composer,LinearLayout.LayoutParams(-1,-2)) }
  root.addView(wrap)
  setContentView(root)
  if(store.history(1).isEmpty()) welcome() else store.history(100).forEach { bubble(it.first,it.second) }
 }
 private fun welcome() {
  log.addView(rowGap(42))
  val card=LinearLayout(this).apply { orientation=1;setPadding(dp(24),dp(24),dp(24),dp(24));background=shape(Color.WHITE,28);elevation=dp(3).toFloat() }
  card.addView(label("✦",34f,blue))
  card.addView(rowGap(10))
  card.addView(label("Olá! Como posso ajudar você hoje?",23f).apply { typeface=Typeface.DEFAULT_BOLD })
  log.addView(card,LinearLayout.LayoutParams(-1,-2).apply { setMargins(dp(8),0,dp(8),dp(30)) })
  listOf("✎" to "Escrever um texto","?" to "Tirar uma dúvida","✧" to "Gerar ideias").forEach { (symbol,name) ->
   val action=LinearLayout(this).apply { orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),dp(15),dp(18),dp(15));background=shape(Color.WHITE,25);elevation=dp(2).toFloat() }
   action.addView(label(symbol,25f,blue).apply { gravity=Gravity.CENTER;background=shape(0xFFEDF4FF.toInt(),40) },LinearLayout.LayoutParams(dp(50),dp(50)))
   action.addView(label(name,17f).apply { setPadding(dp(18),0,0,0) },LinearLayout.LayoutParams(0,-2,1f))
   action.addView(label("›",29f,blue))
   action.setOnClickListener { input.setText(when(name) { "Escrever um texto"->"Me ajude a escrever um texto sobre "; "Tirar uma dúvida"->"Explique para mim ";else->"Me dê ideias para " });input.setSelection(input.length());input.requestFocus() }
   log.addView(action,LinearLayout.LayoutParams(-1,-2).apply { setMargins(dp(8),0,dp(8),dp(16)) })
  }
 }
 private fun bubble(role:String,content:String) {
  if(log.childCount>0 && store.history(2).size<=2 && log.getChildAt(0) is View) {
   if(log.findViewWithTag<View>("welcome")!=null) log.removeAllViews()
  }
  val t=label(content,16f,if(role=="user") Color.WHITE else ink).apply {
   setPadding(dp(17),dp(13),dp(17),dp(13));background=shape(if(role=="user") blue else Color.WHITE,20)
  }
  log.addView(t,LinearLayout.LayoutParams(-2,-2).apply { gravity=if(role=="user") Gravity.END else Gravity.START;setMargins(dp(5),dp(6),dp(5),dp(10)) })
  scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
 }
 private fun submit() {
  val message=input.text.toString().trim();if(message.isEmpty())return
  val key=getSharedPreferences("groq",MODE_PRIVATE).getString("api_key","").orEmpty()
  if(key.isBlank()) { Toast.makeText(this,"Configure a chave Groq nas configurações ⚙",Toast.LENGTH_LONG).show();return }
  if(store.history(1).isEmpty())log.removeAllViews()
  input.setText("");send.isEnabled=false;store.add("user",message);bubble("user",message)
  AssistantClient.ask(key,store.history(30)) { result -> runOnUiThread {
   send.isEnabled=true
   result.onSuccess { store.add("assistant",it);bubble("assistant",it) }
    .onFailure { Toast.makeText(this,"Erro na IA: "+it.message,Toast.LENGTH_LONG).show() }
  } }
 }
}