package com.f1n0zenix.jarvis
import android.app.*
import android.os.Bundle
import android.content.Intent
import android.widget.*
class ApprovalActivity:Activity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val id=intent.getStringExtra("id")?:run { finish();return }
  val item=ReplyRegistry.get(id)?:run { finish();return }
  val root=LinearLayout(this).apply { orientation=1;setPadding(35,60,35,20) }
  root.addView(TextView(this).apply { text=item.sender+": "+item.message;textSize=18f })
  val field=EditText(this).apply { hint="Sua resposta";minLines=2 }
  root.addView(field)
  root.addView(Button(this).apply { text="Sugerir com Groq";setOnClickListener {
   val key=getSharedPreferences("groq",MODE_PRIVATE).getString("api_key","").orEmpty()
   if(key.isBlank()) Toast.makeText(this@ApprovalActivity,"Configure sua chave no início",Toast.LENGTH_LONG).show()
   else GroqClient.suggest(key,item.message) { result -> runOnUiThread { result.onSuccess { field.setText(it) }.onFailure { Toast.makeText(this@ApprovalActivity,"Falha na Groq",Toast.LENGTH_LONG).show() } } }
  } })
  root.addView(Button(this).apply { text="Confirmar e enviar";setOnClickListener {
   val value=field.text.toString().trim()
   if(!Safety.safe(value)) { Toast.makeText(this@ApprovalActivity,"Resposta bloqueada pelo filtro",Toast.LENGTH_LONG).show();return@setOnClickListener }
   val pending=ReplyRegistry.take(id)?:run { finish();return@setOnClickListener }
   try {
    val fill=Intent()
    RemoteInput.addResultsToIntent(arrayOf(pending.input),fill,Bundle().apply { putCharSequence(pending.input.resultKey,value) })
    pending.action.actionIntent.send(this@ApprovalActivity,0,fill)
    Store(this@ApprovalActivity).log(pending.sender,"send_requested")
   } catch(e:Exception) { Toast.makeText(this@ApprovalActivity,"Envio não realizado",Toast.LENGTH_LONG).show() }
   finish()
  } })
  root.addView(Button(this).apply { text="Descartar";setOnClickListener { ReplyRegistry.take(id);finish() } })
  setContentView(root)
 }
}