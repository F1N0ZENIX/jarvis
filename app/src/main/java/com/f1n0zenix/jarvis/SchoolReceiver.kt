package com.f1n0zenix.jarvis
import android.app.*
import android.content.*
class SchoolReceiver:BroadcastReceiver() {
 override fun onReceive(context:Context,intent:Intent) {
  val pending=goAsync()
  SchoolChecker.check(context) { result ->
   result.onSuccess { message ->
    if(message.startsWith("O site Minha Sala foi atualizado")) {
     val nm=context.getSystemService(NotificationManager::class.java)
     nm.createNotificationChannel(NotificationChannel("school","Minha Sala",NotificationManager.IMPORTANCE_DEFAULT))
     nm.notify(90001,Notification.Builder(context,"school").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("JARVIS • Minha Sala").setContentText(message.substringBefore("\\n")).setStyle(Notification.BigTextStyle().bigText(message)).build())
    }
   }
   pending.finish()
  }
 }
}
object SchoolScheduler {
 fun set(context:Context,enabled:Boolean) {
  val am=context.getSystemService(AlarmManager::class.java)
  val pi=PendingIntent.getBroadcast(context,90001,Intent(context,SchoolReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  am.cancel(pi)
  if(enabled)am.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+60000,6L*60*60*1000,pi)
 }
}