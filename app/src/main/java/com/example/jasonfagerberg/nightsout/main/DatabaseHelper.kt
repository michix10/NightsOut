package com.example.jasonfagerberg.nightsout.main

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.jasonfagerberg.nightsout.log.LogHeader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "DatabaseHelper"

class DatabaseHelper(val context: Context?, val name: String?, factory: SQLiteDatabase.CursorFactory?,
                            val version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    private val path = context!!.getDatabasePath(name).toString()
    private lateinit var db: SQLiteDatabase
    private lateinit var mMainActivity: MainActivity

    override fun onCreate(db: SQLiteDatabase?) { /* nothing to do */}

    fun openDatabase(){
        Log.v(TAG, "openDatabase()...called")
        //createDatabase()
        if(!exists()){
            createDatabase()
        }

        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)

        if (db.version != version) {
            onUpgrade(db, db.version, version)
            //db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        }

        mMainActivity = context as MainActivity
    }

    private fun createDatabase(){
        Log.v(TAG, "createDatabase()...called")
        try {
            copyDatabase()
        } catch (e: IOException) {
            throw Error("Error copying database")
        }

    }

    private fun copyDatabase(){
        Log.v(TAG, "copy")
        val inputStream = context!!.assets.open(name!!)
        val outputStream = FileOutputStream(context.getDatabasePath(name))

        val buffer = ByteArray(1024)
        while (inputStream.read(buffer) > 0) {
            outputStream.write(buffer)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    fun closeDatabase(){
        Log.v(TAG, "closeDatabase()...called")
        db.close()
    }

    private fun exists() : Boolean{
        return File(path).exists()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.v(TAG, "OnUpgrade....called")
        createDatabase()
        db!!.version = newVersion
    }

    fun pullDrinks(){
        Log.v(TAG, "pullDrinks()...called")
        mMainActivity.mDrinksList.clear()
        mMainActivity.mFavoritesList.clear()
        mMainActivity.mRecentsList.clear()

        val table = "drinks, current_session_drinks"
        val where = "drinks.id=current_session_drinks.drink_id"
        val cursor = db.query(table, null, where, null, null, null, null, null)
        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1

            val drink = Drink(id, drinkName, abv, amount, measurement, false, recent)
            drink.favorited = isFavoritedInDB(drinkName)
            mMainActivity.mDrinksList.add(drink)
            Log.v(TAG, "current session $drink")
        }
        cursor.close()
        pullFavoriteDrinks()
        pullRecentDrinks()
    }

    private fun pullFavoriteDrinks(){
        val table = "drinks, favorites"
        val where = "drinks.id=favorites.origin_id"
        val cursor = db.query(table, null, where, null, null, null, null, null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1

            val drink = Drink(id, drinkName, abv, amount, measurement, true, recent)

            mMainActivity.mFavoritesList.add(drink)
            Log.v(TAG, "favorited $drink")
        }

        cursor.close()
    }

    private fun pullRecentDrinks(){
        val table = "drinks"
        val where = "drinks.recent=1"
        val cursor = db.query(table, null, where, null, null, null, null, null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent")) == 1

            val drink = Drink(id, drinkName, abv, amount, measurement, true, recent)

            mMainActivity.mRecentsList.add(drink)
            Log.v(TAG, "recentD $drink")
        }

        cursor.close()
    }

    fun isFavoritedInDB(name: String): Boolean{
        val where = "drink_name = ?"
        val whereArgs = arrayOf(name)
        val cursor = db.query("favorites", null, where, whereArgs, null, null,null)
        val ret = cursor.count == 1
        cursor.close()
        return ret
    }

    fun pullLogHeaders(){
        val cursor = db.query("log", null, null, null, null, null, null)
        while (cursor.moveToNext()){
            val date = cursor.getLong(cursor.getColumnIndex("date"))
            val maxBac = cursor.getDouble(cursor.getColumnIndex("maxBac"))
            val duration = cursor.getInt(cursor.getColumnIndex("duration"))
            mMainActivity.mLogHeaders.add(LogHeader(date, maxBac, duration))
            //Log.v(TAG, logHeaders[logHeaders.size-1].toString())
        }
        cursor.close()
    }


    fun pushDrinks(){
        Log.v(TAG, "pushDrinks()...called")
        deleteAllRowsInCurrentSessionTable()
        for(drink in mMainActivity.mDrinksList){
            insertRowInCurrentSessionTable(drink.id)
            updateRowInDrinksTable(drink)
            // todo implement push favorite behavior
            if(drink.favorited && !isFavoritedInDB(drink.name)){
                insertRowInFavoritesTable(drink.name, drink.id)
            }else if(!drink.favorited && isFavoritedInDB(drink.name)){
                deleteRowInFavoritesTable(drink.name)
            }
        }
    }

    private fun updateRowInDrinksTable(drink: Drink){
        var recent = 0
        if (drink.recent) recent = 1
        val sql = "UPDATE drinks SET name=\"${drink.name}\", abv=${drink.abv}, amount=${drink.amount}," +
                " measurement=\"${drink.measurement}\", recent=$recent WHERE id=${drink.id}"
        db.execSQL(sql)
        Log.v(TAG, sql)
    }

    private fun insertRowInCurrentSessionTable(id: Int){
        val sql = "INSERT INTO current_session_drinks VALUES ($id)"
        db.execSQL(sql)
        Log.v(TAG, sql)
    }

    private fun deleteRowInFavoritesTable(name: String){
        val sql = "DELETE FROM favorites WHERE drink_name = \"$name\""
        db.execSQL(sql)
    }

    private fun insertRowInFavoritesTable(name: String, id: Int){
        val sql = "INSERT INTO favorites VALUES (\"$name\", $id)"
        db.execSQL(sql)
    }

    private fun deleteAllRowsInCurrentSessionTable(){
        val sql = "DELETE FROM current_session_drinks"
        db.execSQL(sql)
        Log.v(TAG, "DELETE FROM current_session_drinks")
    }

    fun insertDrinkIntoDrinksTable(drink: Drink){
        var fav = 0
        if (drink.favorited) fav =1
        val sql = "INSERT INTO drinks (name, abv, amount, measurement, recent) \n" +
                "VALUES (\"${drink.name}\", ${drink.abv}, ${drink.amount}, \"${drink.measurement}\"," +
                " $fav)"
        db.execSQL(sql)
        Log.v(TAG, sql)
    }

    fun getDrinkIdFromFullDrinkInfo(target: Drink): Int{
        val where = "name=? AND  abv=? AND amount=? AND measurement=?"
        val whereArgs = arrayOf(target.name, "${target.abv}", "${target.amount}", target.measurement)
        val cursor = db.query("drinks", null, where, whereArgs, null, null, null)

        //Log.v(TAG, sql)
        while (cursor.moveToNext()){
            val foundId = cursor.getInt(cursor.getColumnIndex("id"))
            cursor.close()
            return foundId
        }
        cursor.close()
        return -1
    }
}