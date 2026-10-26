package com.f1n0zenix.jarvis
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class ChatStore(c:Context):SQLiteOpenHelper(c,"chat.db",null,1) {
 override fun onCreate(db:SQLiteDatabase) { db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT,role TEXT NOT NULL,content TEXT NOT NULL,created INTEGER NOT NULL)") }
 override fun onUpgrade(db:SQLiteDatabase,a:Int,b:Int) {}
 fun add(role:String,content:String) { writableDatabase.execSQL("INSERT INTO messages(role,content,created) VALUES(?,?,?)",arrayOf(role,content,System.currentTimeMillis())) }
 fun history(limit:Int=30):List<Pair<String,String>> {
  val out=mutableListOf<Pair<String,String>>()
  readableDatabase.rawQuery("SELECT role,content FROM (SELECT id,role,content FROM messages ORDER BY id DESC LIMIT ?) ORDER BY id ASC",arrayOf(limit.toString())).use { c -> while(c.moveToNext()) out.add(c.getString(0) to c.getString(1)) }
  return out
 }
 fun clear() { writableDatabase.delete("messages",null,null) }
}