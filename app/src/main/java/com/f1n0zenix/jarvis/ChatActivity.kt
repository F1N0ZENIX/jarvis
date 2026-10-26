package com.f1n0zenix.jarvis
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.view.Gravity
import android.view.View
import android.widget.*
class ChatActivity:Activity() {
 private var speaker:TextToSpeech?=null
 private var speakAnswers=false
 private lateinit var log:LinearLayout
 private lateinit var scroll:ScrollView
 private lateinit var send:TextView
 private lateinit var input:EditText
 private lateinit var store:ChatStore
 private val dark by lazy { getSharedPreferences("settings",MODE_PRIVATE).getBoolean("dark_mode",true) }
 private val ink get()=if(dark) Color.WHITE else 0xFF142B3E.toInt()
 private val surface get()=if(dark) 0xFF1D1D1D.toInt() else Color.WHITE
 private val backdrop get()=if(dark) Color.BLACK else 0xFFF4F7FF.toInt()
 private val blue=0xFF1675DB.toInt()
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun shape(color:Int,r:Int)=GradientDrawable().apply { setColor(color);cornerRadius=dp(r).toFloat() }
 private fun label(s:String,size:Float,color:Int=ink)=TextView(this).apply { text=s;textSize=size;setTextColor(color) }
 private fun rowGap(h:Int)=View(this).apply { layoutParams=LinearLayout.LayoutParams(1,dp(h)) }
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  window.statusBarColor=surface;window.navigationBarColor=backdrop
  window.decorView.systemUiVisibility=if(dark) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
  window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
  window.decorView.setOnApplyWindowInsetsListener { view,insets -> view.setPadding(0,insets.systemWindowInsetTop,0,insets.systemWindowInsetBottom);insets }
  store=ChatStore(this)
  speaker=TextToSpeech(this) { status -> if(status==TextToSpeech.SUCCESS)speaker?.language=Locale("pt","BR") }
  val root=LinearLayout(this).apply { orientation=1;setBackgroundColor(backdrop) }
  val header=LinearLayout(this).apply { orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(22),dp(14),dp(18),dp(14));setBackgroundColor(surface) }
  header.addView(label("JARVIS",24f).apply { typeface=Typeface.DEFAULT_BOLD },LinearLayout.LayoutParams(0,-2,1f))
  val voice=label("🎙",24f,blue).apply { gravity=Gravity.CENTER;setOnClickListener { startVoiceInput() } }
  header.addView(voice,LinearLayout.LayoutParams(dp(48),dp(48)))
  val audio=label("♫",24f,blue).apply { gravity=Gravity.CENTER;setOnClickListener { speakAnswers=!speakAnswers;Toast.makeText(this@ChatActivity,if(speakAnswers)"Leitura de respostas ativada" else "Leitura de respostas desativada",Toast.LENGTH_SHORT).show() } }
  header.addView(audio,LinearLayout.LayoutParams(dp(48),dp(48)))
  val settings=label("⚙",27f,blue).apply { gravity=Gravity.CENTER;background=shape(if(dark) 0xFF292929.toInt() else 0xFFEDF4FF.toInt(),28);setOnClickListener { startActivity(Intent(this@ChatActivity,MainActivity::class.java)) } }
  header.addView(settings,LinearLayout.LayoutParams(dp(48),dp(48)))
  root.addView(header)
  scroll=ScrollView(this).apply { isFillViewport=true;clipToPadding=false }
  log=LinearLayout(this).apply { orientation=1;setPadding(dp(18),dp(22),dp(18),dp(18)) }
  scroll.addView(log);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  val composer=LinearLayout(this).apply { orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(14),dp(8),dp(14),dp(8));background=shape(surface,32);elevation=dp(6).toFloat() }
  input=EditText(this).apply { hint="Digite sua mensagem...";setHintTextColor(if(dark) Color.LTGRAY else 0xFF8793A1.toInt());setTextColor(ink);textSize=15f;minLines=1;maxLines=4;background=null;setPadding(dp(10),dp(10),dp(6),dp(10)) }
  composer.addView(input,LinearLayout.LayoutParams(0,-2,1f))
  send=label("➤",24f,Color.WHITE).apply { gravity=Gravity.CENTER;background=shape(blue,30);setOnClickListener { submit() } }
  composer.addView(send,LinearLayout.LayoutParams(dp(52),dp(52)))
  val wrap=LinearLayout(this).apply { setPadding(dp(18),dp(12),dp(18),dp(18));addView(composer,LinearLayout.LayoutParams(-1,-2)) }
  root.addView(wrap)
  setContentView(root)
  if(store.history(1).isEmpty()) welcome() else store.history(100).forEach { bubble(it.first,it.second) }
  if(getSharedPreferences("groq",MODE_PRIVATE).getString("api_key","").isNullOrBlank()) showFirstRunSetup()
 }
 private fun showFirstRunSetup() {
  val field=EditText(this).apply {
   hint="gsk_..."
   inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
   setSingleLine(true)
   setPadding(dp(18),dp(12),dp(18),dp(12))
  }
  val box=LinearLayout(this).apply {
   orientation=1
   setPadding(dp(20),dp(8),dp(20),dp(4))
   addView(TextView(this@ChatActivity).apply {
    text="Bem-vindo ao JARVIS!\\n\\nO assistente funciona diretamente no Android, sem Gateway ou QR Code. Insira sua chave Groq para começar."
    textSize=16f
    setPadding(0,0,0,dp(16))
   })
   addView(field)
  }
  val dialog=android.app.AlertDialog.Builder(this)
   .setTitle("Configurar Groq")
   .setView(box)
   .setPositiveButton("Salvar e começar",null)
   .setNegativeButton("Mais tarde") { _,_-> }
   .create()
  dialog.setOnShowListener {
   dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
    val key=field.text.toString().trim()
    if(!key.startsWith("gsk_") || key.length<12) {
     field.error="Insira uma chave Groq válida (gsk_...)"
    } else {
     getSharedPreferences("groq",MODE_PRIVATE).edit().putString("api_key",key).apply()
     Toast.makeText(this,"Chave salva no aparelho",Toast.LENGTH_SHORT).show()
     dialog.dismiss()
    }
   }
  }
  dialog.show()
 }
 private fun welcome() {
  log.addView(rowGap(42))
  val card=LinearLayout(this).apply { orientation=1;setPadding(dp(24),dp(24),dp(24),dp(24));background=shape(surface,28);elevation=dp(3).toFloat() }
  card.addView(label("✦",34f,blue))
  card.addView(rowGap(10))
  card.addView(label("Olá! Como posso ajudar você hoje?",23f).apply { typeface=Typeface.DEFAULT_BOLD })
  log.addView(card,LinearLayout.LayoutParams(-1,-2).apply { setMargins(dp(8),0,dp(8),dp(30)) })
  listOf("✎" to "Escrever um texto","?" to "Tirar uma dúvida","✧" to "Gerar ideias").forEach { (symbol,name) ->
   val action=LinearLayout(this).apply { orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),dp(15),dp(18),dp(15));background=shape(surface,25);elevation=dp(2).toFloat() }
   action.addView(label(symbol,25f,blue).apply { gravity=Gravity.CENTER;background=shape(if(dark) 0xFF292929.toInt() else 0xFFEDF4FF.toInt(),40) },LinearLayout.LayoutParams(dp(50),dp(50)))
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
   setPadding(dp(17),dp(13),dp(17),dp(13));background=shape(if(role=="user") blue else surface,20)
  }
  log.addView(t,LinearLayout.LayoutParams(-2,-2).apply { gravity=if(role=="user") Gravity.END else Gravity.START;setMargins(dp(5),dp(6),dp(5),dp(10)) })
  scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
 }
 private fun startVoiceInput() {
  try {
   val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE,"pt-BR")
    putExtra(RecognizerIntent.EXTRA_PROMPT,"Fale com o JARVIS")
   }
   startActivityForResult(intent,201)
  } catch(e:Exception) { Toast.makeText(this,"Reconhecimento de voz indisponível neste dispositivo",Toast.LENGTH_LONG).show() }
 }
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
  super.onActivityResult(requestCode,resultCode,data)
  if(requestCode==201&&resultCode==RESULT_OK) {
   val spoken=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
   if(!spoken.isNullOrBlank()) { input.setText(spoken);input.setSelection(input.length()) }
  }
 }
 override fun onDestroy() { speaker?.stop();speaker?.shutdown();super.onDestroy() }
 private fun handleSchool(message:String):Boolean {
  val lower=message.lowercase(java.util.Locale("pt","BR"))
  if(!lower.contains("minha sala") && !lower.contains("minha-sala")) return false
  SchoolChecker.check(this) { result -> runOnUiThread {
   val reply=result.getOrElse { "Não foi possível consultar o Minha Sala: "+it.message }
   store.add("assistant",reply)
   bubble("assistant",reply)
  } }
  return true
 }
 private fun handleReminder(message:String):Boolean {
  val lower=message.lowercase(java.util.Locale("pt","BR"))
  if(!Regex("""\b(lembrete|lembre|marque|agende|agendar|compromisso)\b""").containsMatchIn(lower)) return false
  val isTomorrow="amanhã" in lower || "amanha" in lower
  val isToday="hoje" in lower
  val explicitDate=Regex("""\b(\d{1,2})/(\d{1,2})(?:/(\d{4}))?\b""").find(lower)
  if(!isTomorrow&&!isToday&&explicitDate==null) return false
  val title=Regex("""(?i)\b(?:com o nome|chamado|chamada|intitulado|intitulada)\s+(.+)$""").find(message)?.groupValues?.get(1)?.trim()
   ?: message.replace(Regex("""(?i)\b(?:marque|crie|agende|um|uma|lembrete|para|me|amanhã|amanha|hoje|às|as|com|o|nome)\b""")," ").trim().ifBlank { "Lembrete" }
  val cal=java.util.Calendar.getInstance().apply {
   if(isTomorrow)add(java.util.Calendar.DAY_OF_YEAR,1)
   if(explicitDate!=null) {
    val day=explicitDate.groupValues[1].toIntOrNull()?:return false
    val month=explicitDate.groupValues[2].toIntOrNull()?:return false
    val year=explicitDate.groupValues[3].toIntOrNull()?:get(java.util.Calendar.YEAR)
    if(day !in 1..31||month !in 1..12)return false
    set(year,month-1,day)
   }
  }
  val time=Regex("""(?i)\b(?:às|as|pelas)\s*(\d{1,2})(?:[:h](\d{2}))?\s*h?\b""").find(lower)
  val initialHour=time?.groupValues?.get(1)?.toIntOrNull()
  val initialMinute=time?.groupValues?.get(2)?.toIntOrNull()?:0
  if(initialHour!=null && initialHour !in 0..23)return false
  val pickHour=initialHour?:9
  val pickMinute=initialMinute
  val save:(Int,Int)->Unit={ hour,minute ->
   cal.set(java.util.Calendar.HOUR_OF_DAY,hour);cal.set(java.util.Calendar.MINUTE,minute)
   cal.set(java.util.Calendar.SECOND,0);cal.set(java.util.Calendar.MILLISECOND,0)
   if(cal.timeInMillis<=System.currentTimeMillis()) {
    bubble("assistant","Esse horário já passou. Escolha um horário futuro.")
   } else {
    val id=AgendaStore(this).add(title,cal.timeInMillis,0,0)
    ReminderScheduler.schedule(this,id)
    val whenText=java.text.SimpleDateFormat("dd/MM/yyyy 'às' HH:mm",java.util.Locale("pt","BR")).format(cal.time)
    val reply="Lembrete \"$title\" criado para $whenText. Você pode conferir na Agenda."
    store.add("assistant",reply);bubble("assistant",reply)
   }
  }
  if(initialHour!=null) {
   android.app.AlertDialog.Builder(this).setTitle("Confirmar lembrete")
    .setMessage("Criar \"$title\" para ${java.text.SimpleDateFormat("dd/MM/yyyy",java.util.Locale("pt","BR")).format(cal.time)} às ${"%02d:%02d".format(pickHour,pickMinute)}?")
    .setNegativeButton("Cancelar",null).setPositiveButton("Criar") { _,_->save(pickHour,pickMinute) }.show()
  } else {
   android.app.TimePickerDialog(this,{ _,hour,minute ->save(hour,minute) },pickHour,pickMinute,true).apply {
    setTitle("Que horas devo lembrar de $title?")
    show()
   }
  }
  return true
 }
 private fun submit() {
  val message=input.text.toString().trim();if(message.isEmpty())return
  if(handleSchool(message)) { input.setText("");store.add("user",message);bubble("user",message);return }
  if(handleReminder(message)) { input.setText("");store.add("user",message);bubble("user",message);return }
  val key=getSharedPreferences("groq",MODE_PRIVATE).getString("api_key","").orEmpty()
  if(key.isBlank()) { Toast.makeText(this,"Configure a chave Groq nas configurações ⚙",Toast.LENGTH_LONG).show();return }
  if(store.history(1).isEmpty())log.removeAllViews()
  input.setText("");send.isEnabled=false;store.add("user",message);bubble("user",message)
  AssistantClient.ask(key,store.history(30)) { result -> runOnUiThread {
   send.isEnabled=true
   result.onSuccess { store.add("assistant",it);bubble("assistant",it);if(speakAnswers)speaker?.speak(it,TextToSpeech.QUEUE_FLUSH,null,"jarvis") }
    .onFailure { Toast.makeText(this,"Erro na IA: "+it.message,Toast.LENGTH_LONG).show() }
  } }
 }
}