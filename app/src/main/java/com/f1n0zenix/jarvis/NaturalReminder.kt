package com.f1n0zenix.jarvis

import java.util.Calendar
import java.util.Locale

/** Parser local e determinístico: não envia texto à IA nem cria compromissos sem confirmação. */
object NaturalReminder {
 fun parse(raw:String):Pair<String,Long>? {
  val text=raw.trim()
  if(text.isBlank())return null
  val lower=text.lowercase(Locale("pt","BR"))
  val time=Regex("""(?:às|as|pelas|para as)\s*(\d{1,2})(?:[:h](\d{2}))?\s*h?\b""").find(lower)
  val hour=time?.groupValues?.get(1)?.toIntOrNull() ?: return null
  val minute=time.groupValues[2].toIntOrNull() ?: 0
  if(hour !in 0..23 || minute !in 0..59)return null
  val cal=Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hour);set(Calendar.MINUTE,minute);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0) }
  val day=Regex("""\bdia\s+(\d{1,2})/(\d{1,2})(?:/(\d{4}))?""").find(lower)
  when {
   "amanhã" in lower || "amanha" in lower -> cal.add(Calendar.DAY_OF_YEAR,1)
   day!=null -> {
    val d=day.groupValues[1].toIntOrNull()?:return null
    val m=day.groupValues[2].toIntOrNull()?:return null
    val y=day.groupValues[3].toIntOrNull()?:cal.get(Calendar.YEAR)
    if(d !in 1..31 || m !in 1..12)return null
    cal.isLenient=false
    try { cal.set(y,m-1,d);cal.timeInMillis } catch(_:Exception) { return null }
    if(day.groupValues[3].isBlank() && cal.timeInMillis<System.currentTimeMillis())cal.add(Calendar.YEAR,1)
   }
   "hoje" in lower -> Unit
   else -> return null
  }
  if(cal.timeInMillis<=System.currentTimeMillis())return null
  val title=text.replace(Regex("""(?i)\b(?:me lembre de|me lembre da|lembre de|lembre da|criar lembrete|para)\b"""),"")
   .replace(Regex("""(?i)\b(?:hoje|amanhã|amanha|dia\s+\d{1,2}/\d{1,2}(?:/\d{4})?)\b"""),"")
   .replace(Regex("""(?i)(?:às|as|pelas|para as)\s*\d{1,2}(?:[:h]\d{2})?\s*h?"""),"")
   .trim(' ','.',',',':','-')
  return (title.ifBlank { "Lembrete" }.replaceFirstChar { it.uppercase() }) to cal.timeInMillis
 }
}
