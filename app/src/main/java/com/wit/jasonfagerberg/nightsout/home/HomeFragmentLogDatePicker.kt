package com.wit.jasonfagerberg.nightsout.home

import android.app.DatePickerDialog
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.log.LogHeader
import com.wit.jasonfagerberg.nightsout.converter.Converter
import com.wit.jasonfagerberg.nightsout.databaseHelper.LogDatabaseHelper
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import com.wit.jasonfagerberg.nightsout.dialogs.SimpleDialog
import java.util.*

class HomeFragmentLogDatePicker(private val homeFragment: HomeFragment,
                                private val mainActivity: MainActivity, private val converter: Converter) {
    private val logDatabaseHelper = LogDatabaseHelper(mainActivity.mDatabaseHelper, mainActivity)

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dp = DatePickerDialog(homeFragment.context!!, null, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        dp.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            val logYear = dp.datePicker.year
            val logMonth = dp.datePicker.month
            val logDay = dp.datePicker.dayOfMonth
            val logDate = Integer.parseInt(converter.yearMonthDayTo8DigitString(logYear, logMonth, logDay))

            val testHeader = LogHeader(logDate, 0.0, 0.0)
            if (testHeader in mainActivity.mLogHeaders) {
                showOverrideLogDialog(logDate)
            } else {
                mainActivity.mLogHeaders.add(LogHeader(logDate, homeFragment.bac, homeFragment.drinkingDuration))
                logDatabaseHelper.pushDrinksToLogDrinks(logDate)
                val message = "Log created on ${testHeader.monthName} ${testHeader.day}, ${testHeader.year}"
                mainActivity.showToast(message)
            }
        }

        dp.setTitle("Log Day")
        dp.show()
    }

    private fun showOverrideLogDialog(logDate: Int) {
        val headerIndex = mainActivity.mLogHeaders.indexOf(LogHeader(logDate, 0.0, 0.0))
        val header = mainActivity.mLogHeaders[headerIndex]

        val simpleDialog = SimpleDialog(homeFragment.context!!, mainActivity.layoutInflater)

        simpleDialog.setTitle(mainActivity.resources.getString(R.string.update_log))
        var message = "There is already a log on ${header.monthName} ${header.day}," +
                " ${header.year}.\nWould you like to update the old log?"
        simpleDialog.setBody(message)

        simpleDialog.setNegativeButtonText(mainActivity.resources.getString(R.string.cancel))
        simpleDialog.setNegativeFunction { _ ->
            showDatePicker()
            simpleDialog.dismiss()
        }

        simpleDialog.setPositiveButtonText(mainActivity.resources.getString(R.string.update))
        simpleDialog.setPositiveFunction { _ ->
            logDatabaseHelper.deleteLog(header.date)
            mainActivity.mLogHeaders[headerIndex] = LogHeader(header.date, homeFragment.bac, homeFragment.drinkingDuration)
            homeFragment.mDrinkListAdapter.notifyDataSetChanged()
            logDatabaseHelper.pushDrinksToLogDrinks(header.date)
            message = "Log on ${header.monthName} ${header.day}, ${header.year} was updated"
            mainActivity.showToast(message)
            simpleDialog.dismiss()
        }
    }
}