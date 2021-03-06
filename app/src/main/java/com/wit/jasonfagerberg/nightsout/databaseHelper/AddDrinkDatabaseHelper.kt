package com.wit.jasonfagerberg.nightsout.databaseHelper

import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.util.UUID

class AddDrinkDatabaseHelper(private val mainActivity: MainActivity) {

    fun buildDrinkAndAddToList(
        name: String,
        abv: Double,
        amount: Double,
        measurement: String,
        favorited: Boolean,
        canUnfavorite: Boolean
    ) {
        val drink = Drink(UUID.randomUUID(), name, abv, amount, measurement, favorited, true, mainActivity.getLongTimeNow())

        setDrinkId(drink)
        mainActivity.mDatabaseHelper.updateDrinkModifiedTime(drink.id, drink.modifiedTime)
        mainActivity.mDatabaseHelper.updateDrinkSuggestionStatus(drink.id, false)
        setDrinkFavorited(drink, favorited)

        if (canUnfavorite) {
            mainActivity.addDrinkFragment.addToDrinkList(drink)
        } else {
            drink.recent = false
            mainActivity.addDrinkFragment.addToFavoritesList(drink)
        }
    }

    private fun setDrinkId(drink: Drink) {
        val id = mainActivity.mDatabaseHelper.getDrinkIdFromFullDrinkInfo(drink)
        drink.id = id
        if (!mainActivity.mDatabaseHelper.idInDb(drink.id)) mainActivity.mDatabaseHelper.insertDrinkIntoDrinksTable(drink)
    }

    private fun setDrinkFavorited(drink: Drink, favorited: Boolean) {
        drink.favorited = mainActivity.mFavoritesList.contains(drink)

        if (mainActivity.mDrinksList.contains(drink)) {
            val index = mainActivity.mDrinksList.indexOf(drink)
            val drinkInSession = mainActivity.mDrinksList[index]
            drink.favorited = drinkInSession.favorited
        }

        if (favorited) {
            drink.favorited = true
            for (d in mainActivity.mDrinksList) {
                if (d == drink) {
                    d.favorited = true
                }
            }
        }
    }

    fun getSuggestedDrinks(filter: String, ignoreDontShow: Boolean = false): ArrayList<Drink> {
        val res = ArrayList<Drink>()
        val cursor = mainActivity.mDatabaseHelper.db.query(true, "drinks", null,
                "name LIKE ?", arrayOf("%$filter%"), null, null, "name, modifiedTime DESC", null)
        while (cursor.moveToNext()) {
            val id = UUID.fromString(cursor.getString(cursor.getColumnIndex("id")))
            val drinkName = cursor.getString(cursor.getColumnIndex("name"))
            val abv = cursor.getDouble(cursor.getColumnIndex("abv"))
            val amount = cursor.getDouble(cursor.getColumnIndex("amount"))
            val measurement = cursor.getString(cursor.getColumnIndex("measurement"))
            val modifiedTime = cursor.getLong(cursor.getColumnIndex("modifiedTime"))
            val recent = cursor.getInt(cursor.getColumnIndex("recent"))
            val favorited = mainActivity.mDatabaseHelper.isFavoritedInDB(drinkName)
            val dontSuggest = cursor.getInt(cursor.getColumnIndex("dontSuggest")) == 1

            if (!dontSuggest || ignoreDontShow) res.add(Drink(id, drinkName, abv, amount, measurement, favorited, recent == 1, modifiedTime))
        }
        cursor.close()
        return res
    }

    fun updateDrinkFavoriteStatus(drink: Drink) {
        val recent = if (drink.recent) 1 else 0
        val name = "\"${drink.name}\""
        val sql = "UPDATE drinks SET recent = $recent WHERE name = $name"
        mainActivity.mDatabaseHelper.db.execSQL(sql)
    }
}