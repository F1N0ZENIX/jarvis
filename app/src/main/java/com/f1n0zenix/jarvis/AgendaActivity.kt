package com.f1n0zenix.jarvis

import android.app.*
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class AgendaActivity : Activity() {
 private val events by lazy { AgendaStore(this) }
 private val shownMonth=Calendar.getInstance()
 private val formatter = SimpleDateFormat("EEE, dd 'de' MMM • HH:mm", Locale("pt","BR"))
 private fun dp(n:Int) = (n*resources.displayMetrics.density).toInt()
 private fun rounded(color:Int, radius:Int=20) = GradientDrawable().apply {
  setColor(color); cornerRadius=dp(radius).toFloat()
 }
 private val white=Color.WHITE
 private val muted=0xFFB5B9C4.toInt()
 private val panel=0xFF17191E.toInt()
 private val accent=0xFFB5C9FF.toInt()
 private fun text(value:String, size:Float=16f, color:Int=white, bold:Boolean=false) = TextView(this).apply {
  text=value; textSize=size;setTextColor(color)
  if(bold) typeface=Typeface.DEFAULT_BOLD
 }
 private fun gap(h:Int)=Space(this).apply { minimumHeight=dp(h) }
 private fun button(label:String, click:()->Unit)=Button(this).apply {
  text=label;isAllCaps=false;setTextColor(Color.BLACK);background=rounded(accent,18)
  setOnClickListener { click() }
 }
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  window.statusBarColor=Color.BLACK;window.navigationBarColor=Color.BLACK
  showAgenda()
 }
 private fun showAgenda() {
  val root=LinearLayout(this).apply { orientation=1;setBackgroundColor(Color.BLACK);setPadding(dp(22),dp(20),dp(22),dp(24)) }
  root.addView(text("Minha agenda",30f,bold=true))
  root.addView(gap(5))
  root.addView(text("Organize seus compromissos e lembretes",14f,muted))
  root.addView(gap(24))
  root.addView(button("+ Novo compromisso") { editEvent() },LinearLayout.LayoutParams(-1,dp(54)))
  root.addView(gap(16))
  val quick=EditText(this).apply {
   hint="Ex.: prova amanhã às 8h"
   setHintTextColor(muted);setTextColor(white)
   setPadding(dp(14),dp(10),dp(14),dp(10));background=rounded(panel)
   setSingleLine(true)
  }
  root.addView(quick)
  root.addView(gap(8))
  root.addView(button("Criar lembrete por texto") {
   val parsed=NaturalReminder.parse(quick.text.toString())
   if(parsed==null) Toast.makeText(this,"Use: tarefa amanhã às 8h, hoje às 15h ou dia 29/10 às 14h",Toast.LENGTH_LONG).show()
   else editEvent(null,parsed.first,parsed.second)
  })
  root.addView(gap(20))
  val nav=LinearLayout(this).apply { gravity=Gravity.CENTER_VERTICAL }
  nav.addView(text("‹",32f,accent).apply { setPadding(dp(10),0,dp(16),0);setOnClickListener { shownMonth.add(Calendar.MONTH,-1);showAgenda() } })
  nav.addView(text(SimpleDateFormat("MMMM yyyy",Locale("pt","BR")).format(shownMonth.time).replaceFirstChar { it.uppercase() },19f,white,true),LinearLayout.LayoutParams(0,-2,1f))
  nav.addView(text("›",32f,accent).apply { setPadding(dp(16),0,dp(10),0);setOnClickListener { shownMonth.add(Calendar.MONTH,1);showAgenda() } })
  root.addView(nav)
  val calendar=CalendarView(this).apply {
   date=shownMonth.timeInMillis
   minDate=Calendar.getInstance().apply { add(Calendar.YEAR,-3) }.timeInMillis
   maxDate=Calendar.getInstance().apply { add(Calendar.YEAR,5) }.timeInMillis
   setOnDateChangeListener { _,year,month,day ->
    val picked=Calendar.getInstance().apply { set(year,month,day,9,0);set(Calendar.SECOND,0) }
    editEvent(null,"",picked.timeInMillis)
   }
  }
  root.addView(calendar,LinearLayout.LayoutParams(-1,dp(310)))
  root.addView(gap(18))
  val list=events.all().sortedBy { it.start }
  root.addView(text("PRÓXIMOS",13f,accent,true))
  root.addView(gap(12))
  if(list.isEmpty()) {
   val empty=LinearLayout(this).apply { orientation=1;gravity=Gravity.CENTER;setPadding(dp(20),dp(40),dp(20),dp(40));background=rounded(panel) }
   empty.addView(text("✦",32f,accent))
   empty.addView(gap(10))
   empty.addView(text("Sua agenda está livre",20f,bold=true))
   empty.addView(gap(6))
   empty.addView(text("Toque em Novo compromisso para começar.",14f,muted))
   root.addView(empty)
  } else list.forEach { e ->
   val card=LinearLayout(this).apply { orientation=1;setPadding(dp(18),dp(16),dp(18),dp(16));background=rounded(panel) }
   card.addView(text(e.title,18f,bold=true))
   card.addView(gap(6))
   card.addView(text(formatter.format(Date(e.start)).replaceFirstChar { it.uppercase() },14f,accent))
   card.addView(gap(4))
   val repeat=when(e.everyDays) { 1->"Diariamente";7->"Semanalmente";14->"A cada 14 dias";30->"A cada 30 dias";else->"Não se repete" }
   card.addView(text("$repeat • Aviso: ${e.beforeDays} dia(s) antes",13f,muted))
   card.setOnClickListener { editEvent(e) }
   root.addView(card)
   root.addView(gap(12))
  }
  setContentView(ScrollView(this).apply { isFillViewport=true;addView(root) })
 }
 private fun editEvent(existing:AgendaStore.Event?=null,prefill:String="",whenMillis:Long?=null) {
  val now=Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY,1) }
  val selected=Calendar.getInstance().apply { timeInMillis=existing?.start ?: whenMillis ?: now.timeInMillis }
  val layout=LinearLayout(this).apply { orientation=1;setPadding(dp(22),dp(14),dp(22),dp(12)) }
  val title=EditText(this).apply { hint="Ex.: Estudar para a prova";setSingleLine(true);setText(existing?.title ?: prefill) }
  layout.addView(title)
  val whenLabel=text("Data: selecione abaixo",15f,Color.DKGRAY)
  layout.addView(whenLabel)
  fun update() { whenLabel.text=SimpleDateFormat("dd/MM/yyyy 'às' HH:mm",Locale("pt","BR")).format(selected.time) }
  update()
  layout.addView(Button(this).apply { text="Escolher data e horário";isAllCaps=false;setOnClickListener {
   DatePickerDialog(this@AgendaActivity,{ _,year,month,day ->
    selected.set(Calendar.YEAR,year);selected.set(Calendar.MONTH,month);selected.set(Calendar.DAY_OF_MONTH,day)
    TimePickerDialog(this@AgendaActivity,{ _,hour,minute ->
     selected.set(Calendar.HOUR_OF_DAY,hour);selected.set(Calendar.MINUTE,minute);selected.set(Calendar.SECOND,0);update()
    },selected.get(Calendar.HOUR_OF_DAY),selected.get(Calendar.MINUTE),true).show()
   },selected.get(Calendar.YEAR),selected.get(Calendar.MONTH),selected.get(Calendar.DAY_OF_MONTH)).show()
  } })
  val repeats=arrayOf("Não repetir","Todos os dias","Toda semana","A cada 14 dias","A cada 30 dias")
  layout.addView(text("Repetição",14f,Color.DKGRAY))
  val repeat=Spinner(this).apply { adapter=ArrayAdapter(this@AgendaActivity,android.R.layout.simple_spinner_dropdown_item,repeats) }
  layout.addView(repeat)
  repeat.setSelection(intArrayOf(0,1,7,14,30).indexOf(existing?.everyDays ?: 0).coerceAtLeast(0))
  layout.addView(text("Lembrar com antecedência",14f,Color.DKGRAY))
  val options=arrayOf("Na hora","1 dia antes","2 dias antes","7 dias antes")
  val before=Spinner(this).apply { adapter=ArrayAdapter(this@AgendaActivity,android.R.layout.simple_spinner_dropdown_item,options);setSelection(2) }
  layout.addView(before)
  before.setSelection(intArrayOf(0,1,2,7).indexOf(existing?.beforeDays ?: 2).coerceAtLeast(0))
  val dialog=AlertDialog.Builder(this).setTitle(if(existing==null)"Novo compromisso" else "Editar compromisso").setView(ScrollView(this).apply { addView(layout) })
   .setNegativeButton("Cancelar",null).setPositiveButton("Salvar",null).create()
  if(existing!=null) dialog.setButton(AlertDialog.BUTTON_NEUTRAL,"Excluir") { _,_->
   AlertDialog.Builder(this).setTitle("Excluir compromisso?").setMessage(existing.title)
    .setNegativeButton("Cancelar",null).setPositiveButton("Excluir") { _,_->
     ReminderScheduler.cancel(this,existing.id);events.delete(existing.id);showAgenda()
    }.show()
  }
  dialog.setOnShowListener {
   dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
    val name=title.text.toString().trim()
    if(name.isEmpty()) { title.error="Informe um título";return@setOnClickListener }
    if(selected.timeInMillis<=System.currentTimeMillis()) {
     Toast.makeText(this,"Escolha uma data futura",Toast.LENGTH_LONG).show();return@setOnClickListener
    }
    val every=intArrayOf(0,1,7,14,30)[repeat.selectedItemPosition]
    val days=intArrayOf(0,1,2,7)[before.selectedItemPosition]
    val id=if(existing==null) events.add(name,selected.timeInMillis,every,days) else {
     ReminderScheduler.cancel(this,existing.id)
     events.update(existing.id,name,selected.timeInMillis,every,days)
     existing.id
    }
    ReminderScheduler.schedule(this,id)
    dialog.dismiss();showAgenda()
   }
  }
  dialog.show()
 }
}
