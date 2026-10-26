package com.f1n0zenix.jarvis
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
object AssistantClient {
 fun ask(key:String,history:List<Pair<String,String>>,done:(Result<String>)->Unit) {
  Thread {
   var last:Exception=IllegalStateException("Serviço indisponível")
   for(model in listOf("openai/gpt-oss-20b","openai/gpt-oss-120b")) {
    try {
     val c=URL("https://api.groq.com/openai/v1/chat/completions").openConnection() as HttpURLConnection
     try {
      c.requestMethod="POST";c.connectTimeout=15000;c.readTimeout=35000;c.doOutput=true
      c.setRequestProperty("Authorization","Bearer "+key)
      c.setRequestProperty("Content-Type","application/json")
      val messages=JSONArray().put(JSONObject().put("role","system").put("content","Você é JARVIS, assistente pessoal em português brasileiro. Seja útil, claro e objetivo. Não afirme ter enviado mensagens, consultado Gmail, criado lembretes ou acessado arquivos se não executou essas ações. Não invente informações pessoais."))
      history.takeLast(24).forEach { (role,content) -> messages.put(JSONObject().put("role",role).put("content",content)) }
      val payload=JSONObject().put("model",model).put("messages",messages).put("max_completion_tokens",800)
      c.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
      if(c.responseCode==200) {
       val response=JSONObject(c.inputStream.bufferedReader().use { it.readText() }).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
       if(response.isNotBlank()) { done(Result.success(response));return@Thread }
      } else {
       last=IllegalStateException("Groq HTTP "+c.responseCode)
       if(c.responseCode==401||c.responseCode==403) break
      }
     } finally { c.disconnect() }
    } catch(e:Exception) { last=e }
   }
   done(Result.failure(last))
  }.start()
 }
}