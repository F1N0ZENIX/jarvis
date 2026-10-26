package com.f1n0zenix.jarvis
import java.net.*
import org.json.*
object GroqClient {
 fun suggest(key:String,message:String,callback:(Result<String>)->Unit) {
  Thread {
   var last:Exception=IllegalStateException("Groq indisponível")
   for(model in listOf("openai/gpt-oss-20b","openai/gpt-oss-120b")) {
    try {
     val c=URL("https://api.groq.com/openai/v1/chat/completions").openConnection() as HttpURLConnection
     try {
      c.requestMethod="POST";c.connectTimeout=12000;c.readTimeout=25000;c.doOutput=true
      c.setRequestProperty("Authorization","Bearer "+key)
      c.setRequestProperty("Content-Type","application/json")
      val messages=JSONArray().put(JSONObject().put("role","system").put("content","Sugira resposta curta em português; nunca invente dados ou inclua informações sensíveis. Retorne apenas a mensagem.")).put(JSONObject().put("role","user").put("content",message))
      val json=JSONObject().put("model",model).put("messages",messages).put("max_completion_tokens",160)
      c.outputStream.use { it.write(json.toString().toByteArray()) }
      if(c.responseCode==200) {
       val output=JSONObject(c.inputStream.bufferedReader().use { it.readText() }).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
       if(Safety.safe(output)) { callback(Result.success(output));return@Thread }
       last=IllegalArgumentException("Conteúdo bloqueado")
      } else last=IllegalStateException("HTTP "+c.responseCode)
     } finally { c.disconnect() }
    } catch(e:Exception) { last=e }
   }
   callback(Result.failure(last))
  }.start()
 }
}