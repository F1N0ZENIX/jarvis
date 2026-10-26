package com.f1n0zenix.jarvis
import android.app.*
import android.content.*
import android.os.Build
import java.util.Calendar
object ReminderScheduler {
 fun schedule(context:Context,id:Int) {
  val event=AgendaStore(context).get(id)?:return
  val cal=Calendar.getInstance().apply { timeInMillis=event.start;add(Calendar.DAY_OF_YEAR,-event.beforeDays) }
  val now=System.currentTimeMillis()
  if(event.everyDays>0) while(cal.timeInMillis<=now)cal.add(Calendar.DAY_OF_YEAR,event.everyDays)
  if(cal.timeInMillis<=now)return
  val alarm=context.getSystemService(AlarmManager::class.java)
  val intent=Intent(context,ReminderReceiver::class.java).putExtra("event_id",id)
  val pi=PendingIntent.getBroadcast(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  if(Build.VERSION.SDK_INT>=31&&alarm.canScheduleExactAlarms()) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.timeInMillis,pi)
  else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.timeInMillis,pi)
 }
}
class ReminderReceiver:BroadcastReceiver() {
 override fun onReceive(context:Context,intent:Intent) {
  val id=intent.getIntExtra("event_id",-1)
  val event=AgendaStore(context).get(id)?:return
  val manager=context.getSystemService(NotificationManager::class.java)
  manager.createNotificationChannel(NotificationChannel("agenda","Agenda JARVIS",NotificationManager.IMPORTANCE_HIGH))
  manager.notify(id,Notification.Builder(context,"agenda").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("JARVIS • compromisso").setContentText(event.title+" em "+event.beforeDays+" dia(s)").build())
  if(event.everyDays>0)ReminderScheduler.schedule(context,id)
 }
}
class BootReceiver:BroadcastReceiver() {
 override fun onReceive(context:Context,intent:Intent) { if(intent.action==Intent.ACTION_BOOT_COMPLETED) AgendaStore(context).all().forEach { ReminderScheduler.schedule(context,it.id) } }
}