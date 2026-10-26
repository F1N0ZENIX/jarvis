package com.f1n0zenix.jarvis
import android.content.Context
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
object SchoolChecker {
 fun check(context:Context,done:(Result<String>)->Unit) {
  Thread {
   try {
    val conn=URL("https://api.github.com/repos/F1N0ZENIX/minha-sala/commits?path=index.html&per_page=1").openConnection() as HttpURLConnection
    conn.connectTimeout=12000;conn.readTimeout=12000
    conn.setRequestProperty("Accept","application/vnd.github+json")
    conn.setRequestProperty("User-Agent","JARVIS-Android")
    try {
     if(conn.responseCode!=200) error("GitHub HTTP "+conn.responseCode)
     val obj=JSONArray(conn.inputStream.bufferedReader().use { it.readText() }).getJSONObject(0)
     val sha=obj.getString("sha")
     val message=obj.getJSONObject("commit").getString("message").lineSequence().first()
     val prefs=context.getSharedPreferences("school",Context.MODE_PRIVATE)
     val previous=prefs.getString("last_sha",null)
     prefs.edit().putString("last_sha",sha).apply()
     val status=when { previous==null->"Primeira verificação concluída";previous==sha->"Nenhuma alteração desde a última verificação";else->"O site Minha Sala foi atualizado" }
     done(Result.success(status+"\\nÚltima alteração: "+message))
    } finally { conn.disconnect() }
   } catch(e:Exception) { done(Result.failure(e)) }
  }.start()
 }

 fun upcoming(done:(Result<String>)->Unit) {
  Thread {
   try {
    val conn=URL("https://raw.githubusercontent.com/F1N0ZENIX/minha-sala/main/index.html").openConnection() as HttpURLConnection
    conn.connectTimeout=12000
    conn.readTimeout=12000
    try {
     if(conn.responseCode!=200)error("HTTP "+conn.responseCode)
     val html=conn.inputStream.bufferedReader().use { it.readText() }
     val cards=Regex("""<div class="wanted-poster" data-deadline="([^"]+)"[^>]*>(.*?)(?=<div class="wanted-poster"|</section>|</main>|<!--)""",RegexOption.DOT_MATCHES_ALL)
     val title=Regex("""<div class="subject-name">([^<]+)</div>""")
     val subject=Regex("""<div class="subject-teacher">([^<]+)</div>""")
     val description=Regex("""<div class="wanted-desc">([^<]+)</div>""")
     val dateFormat=java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",java.util.Locale.ROOT)
     val output=cards.findAll(html).mapNotNull { card ->
      val date=runCatching { dateFormat.parse(card.groupValues[1]) }.getOrNull()?:return@mapNotNull null
      if(date.time<System.currentTimeMillis())return@mapNotNull null
      val name=title.find(card.value)?.groupValues?.get(1)?:return@mapNotNull null
      val subjectName=subject.find(card.value)?.groupValues?.get(1).orEmpty()
      val detail=description.find(card.value)?.groupValues?.get(1).orEmpty()
      Triple(date.time,name,"$subjectName — $detail")
     }.sortedBy { it.first }.take(12).toList()
     val formatted=java.text.SimpleDateFormat("dd/MM",java.util.Locale("pt","BR"))
     val answer=if(output.isEmpty())"Não encontrei atividades futuras no Minha Sala."
      else "Próximas atividades do Minha Sala:\\n"+output.joinToString("\\n") {
       "• "+formatted.format(java.util.Date(it.first))+" — "+it.second+" ("+it.third+")"
      }
     done(Result.success(answer.replace("\\\\n","\\n")))
    } finally { conn.disconnect() }
   }catch(e:Exception){done(Result.failure(e))}
  }.start()
 }
}