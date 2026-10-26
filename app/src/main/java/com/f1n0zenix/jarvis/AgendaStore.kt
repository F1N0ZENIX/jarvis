package com.f1n0zenix.jarvis
import android.content.Context
import android.database.sqlite.*
class AgendaStore(c:Context):SQLiteOpenHelper(c,"agenda.db",null,1) {
 data class Event(val id:Int,val title:String,val start:Long,val everyDays:Int,val beforeDays:Int)
 override fun onCreate(db:SQLiteDatabase) { db.execSQL("CREATE TABLE events(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,start INTEGER NOT NULL,every_days INTEGER NOT NULL,before_days INTEGER NOT NULL)") }
 override fun onUpgrade(db:SQLiteDatabase,a:Int,b:Int) {}
 fun add(title:String,start:Long,everyDays:Int,beforeDays:Int):Int {
  writableDatabase.execSQL("INSERT INTO events(title,start,every_days,before_days) VALUES(?,?,?,?)",arrayOf(title,start,everyDays,beforeDays))
  readableDatabase.rawQuery("SELECT last_insert_rowid()",null).use { it.moveToFirst();return it.getInt(0) }
 }
 fun update(id:Int,title:String,start:Long,everyDays:Int,beforeDays:Int) {
  writableDatabase.execSQL("UPDATE events SET title=?,start=?,every_days=?,before_days=? WHERE id=?",arrayOf(title,start,everyDays,beforeDays,id))
 }
 fun delete(id:Int) {
  writableDatabase.delete("events","id=?",arrayOf(id.toString()))
 }
 fun get(id:Int):Event? { readableDatabase.rawQuery("SELECT id,title,start,every_days,before_days FROM events WHERE id=?",arrayOf(id.toString())).use { if(it.moveToFirst())return Event(it.getInt(0),it.getString(1),it.getLong(2),it.getInt(3),it.getInt(4)) };return null }
 fun all():List<Event> { val list=mutableListOf<Event>();readableDatabase.rawQuery("SELECT id,title,start,every_days,before_days FROM events",null).use { while(it.moveToNext())list.add(Event(it.getInt(0),it.getString(1),it.getLong(2),it.getInt(3),it.getInt(4))) };return list }
}