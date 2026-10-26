package com.f1n0zenix.jarvis
import android.app.Notification
import android.app.RemoteInput
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
object ReplyRegistry {
 data class Item(val source:String,val sender:String,val message:String,val action:Notification.Action,val input:RemoteInput,val time:Long=System.currentTimeMillis())
 private val items=ConcurrentHashMap<String,Item>()
 fun put(i:Item):String { items.entries.removeIf { it.value.source==i.source };return UUID.randomUUID().toString().also { items[it]=i } }
 fun get(id:String)=items[id]?.takeIf { System.currentTimeMillis()-it.time<900000 }
 fun take(id:String)=get(id)?.let { items.remove(id) }
 fun invalidate(source:String) { items.entries.removeIf { it.value.source==source } }
}