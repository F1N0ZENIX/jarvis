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
}