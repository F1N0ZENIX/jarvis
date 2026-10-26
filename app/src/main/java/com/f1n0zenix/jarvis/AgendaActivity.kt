package com.f1n0zenix.jarvis
import android.app.*
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
class AgendaActivity:Activity() {
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply { orientation=1;setPadding(32,40,32,24) }
  root.addView(TextView(this).apply { text="AGENDA • JARVIS";textSize=24f })
  val title=EditText(this).apply { hint="Nome do compromisso" }
  root.addView(title)
  val date=EditText(this).apply { hint="Data inicial: dd/MM/yyyy HH:mm" }
  root.addView(date)
  val repeat=Switch(this).apply { text="Repetir a cada 14 dias";isChecked=true }
  root.addView(repeat)
  val before=EditText(this).apply { hint="Avisar quantos dias antes?";inputType=2;setText("2") }
  root.addView(before)
  root.addView(Button(this).apply { text="Salvar compromisso";setOnClickListener {
   try {
    val format=SimpleDateFormat("dd/MM/yyyy HH:mm",Locale("pt","BR")).apply { isLenient=false }
    val time=format.parse(date.text.toString())!!.time
    val days=before.text.toString().toInt()
    require(title.text.isNotBlank()&&days in 0..30&&time>System.currentTimeMillis())
    val id=AgendaStore(this@AgendaActivity).add(title.text.toString(),time,if(repeat.isChecked)14 else 0,days)
    ReminderScheduler.schedule(this@AgendaActivity,id)
    Toast.makeText(this@AgendaActivity,"Compromisso salvo",Toast.LENGTH_LONG).show()
    finish()
   } catch(e:Exception) { Toast.makeText(this@AgendaActivity,"Confira título, data e antecedência",Toast.LENGTH_LONG).show() }
  } })
  setContentView(ScrollView(this).apply { addView(root) })
 }
}