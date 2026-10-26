package com.f1n0zenix.jarvis
object Safety {
 private val blocked=Regex("(?i)\\b(?:senha|password|otp|token|cpf|pix|2fa)\\b|\\b\\d{6}\\b")
 fun safe(text:String)=text.isNotBlank()&&text.length<=500&&!blocked.containsMatchIn(text)
}