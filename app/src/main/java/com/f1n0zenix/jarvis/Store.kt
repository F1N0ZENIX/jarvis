package com.f1n0zenix.jarvis
import android.content.Context
import android.database.sqlite.*
class Store(c:Context):SQLiteOpenHelper(c,"jarvis.db",null,1) {
 override fun onCreate(db:SQLiteDatabase) { db.execSQL("CREATE TABLE events(id INTEGER PRIMARY KEY AUTOINCREMENT,sender TEXT,state TEXT,time INTEGER)") }
 override fun onUpgrade(db:SQLiteDatabase,a:Int,b:Int) {}
 fun log(sender:String,state:String) { writableDatabase.execSQL("INSERT INTO events(sender,state,time) VALUES(?,?,?)",arrayOf(sender,state,System.currentTimeMillis())) }
}