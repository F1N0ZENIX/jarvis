package com.f1n0zenix.jarvis

import android.app.*
import android.content.*
import android.os.*
import android.speech.*
import java.util.Locale

class WakeWordService : Service(), RecognitionListener {
 private var recognizer: SpeechRecognizer? = null
 private val handler = Handler(Looper.getMainLooper())
 private var listening = false
 private val channel = "jarvis_wake"
 override fun onBind(intent: Intent?): IBinder? = null
 override fun onCreate() {
  super.onCreate()
  getSystemService(NotificationManager::class.java).createNotificationChannel(
   NotificationChannel(channel, "Ativação por voz", NotificationManager.IMPORTANCE_LOW))
 }
 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
  if(intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
  val stop = PendingIntent.getService(this, 1,
   Intent(this, WakeWordService::class.java).setAction("STOP"),
   PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val notification = Notification.Builder(this, channel)
   .setSmallIcon(android.R.drawable.ic_btn_speak_now)
   .setContentTitle("JARVIS: escuta ativada")
   .setContentText("Diga Jarvis. Toque em Parar para desligar.")
   .addAction(Notification.Action.Builder(null, "Parar", stop).build())
   .build()
  if(Build.VERSION.SDK_INT >= 29) startForeground(501, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
  else startForeground(501, notification)
  listening = true
  handler.post { listen() }
  return START_NOT_STICKY
 }
 private fun listen() {
  if(!listening) return
  if(!SpeechRecognizer.isRecognitionAvailable(this)) { stopSelf(); return }
  try {
   recognizer?.destroy()
   recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
    setRecognitionListener(this@WakeWordService)
    startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
     putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
     putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
     putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    })
   }
  } catch(_: Exception) { restart() }
 }
 private fun restart() {
  if(listening) handler.postDelayed({ listen() }, 1200)
 }
 private fun detect(bundle: Bundle?) {
  val phrases = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
  if(phrases.any { it.lowercase(Locale("pt","BR")).contains("jarvis") }) {
   listening = false
   val launch = Intent(this, ChatActivity::class.java)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
   val open = PendingIntent.getActivity(this, 2, launch,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
   getSystemService(NotificationManager::class.java).notify(502,
    Notification.Builder(this, channel).setSmallIcon(android.R.drawable.ic_btn_speak_now)
     .setContentTitle("JARVIS ativado").setContentText("Toque para conversar")
     .setContentIntent(open).setAutoCancel(true).build())
   try { startActivity(launch) } catch(_: Exception) {}
   stopSelf()
  }
 }
 override fun onReadyForSpeech(params: Bundle?) {}
 override fun onBeginningOfSpeech() {}
 override fun onRmsChanged(rmsdB: Float) {}
 override fun onBufferReceived(buffer: ByteArray?) {}
 override fun onEndOfSpeech() {}
 override fun onError(error: Int) { restart() }
 override fun onResults(results: Bundle?) { detect(results); restart() }
 override fun onPartialResults(partialResults: Bundle?) { detect(partialResults) }
 override fun onEvent(eventType: Int, params: Bundle?) {}
 override fun onDestroy() {
  listening = false
  handler.removeCallbacksAndMessages(null)
  recognizer?.cancel()
  recognizer?.destroy()
  super.onDestroy()
 }
}
