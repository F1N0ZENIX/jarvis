package com.f1n0zenix.jarvis
import android.app.*
import android.content.Intent
import android.service.notification.*
class WhatsAppListener:NotificationListenerService() {
 override fun onNotificationPosted(s:StatusBarNotification) {
  if(!getSharedPreferences("settings",MODE_PRIVATE).getBoolean("notifications_enabled",true)) return
  if(s.packageName!="com.whatsapp"&&s.packageName!="com.whatsapp.w4b") return
  val n=s.notification
  val name=n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?:return
  val body=n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?:return
  if(body.isBlank()) return
  val action=n.actions?.firstOrNull { it.remoteInputs?.isNotEmpty()==true }?:return
  val input=action.remoteInputs?.firstOrNull()?:return
  val id=ReplyRegistry.put(ReplyRegistry.Item(s.key,name,body,action,input))
  Store(this).log(name,"review")
  val manager=getSystemService(NotificationManager::class.java)
  manager.createNotificationChannel(NotificationChannel("reviews","Revisões",NotificationManager.IMPORTANCE_HIGH))
  val pi=PendingIntent.getActivity(this,id.hashCode(),Intent(this,ApprovalActivity::class.java).putExtra("id",id),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
  manager.notify(id.hashCode(),Notification.Builder(this,"reviews").setSmallIcon(android.R.drawable.ic_dialog_email).setContentTitle("JARVIS: revisar mensagem").setContentText(name).setContentIntent(pi).setAutoCancel(true).build())
 }
 override fun onNotificationRemoved(s:StatusBarNotification) { ReplyRegistry.invalidate(s.key) }
}