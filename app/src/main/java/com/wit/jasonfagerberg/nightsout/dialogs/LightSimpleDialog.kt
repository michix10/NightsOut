package com.wit.jasonfagerberg.nightsout.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class LightSimpleDialog(context: Context) {
    private var dialogClickListener = DialogInterface.OnClickListener { _, _ -> }
    private var builder = AlertDialog.Builder(context)
    private var showNeutralButton = false

    fun show(message: String, posText: String = "Yes", negText: String = "No", neuText: String = "Neutral") {
        builder.setMessage(message)
        builder.setPositiveButton(posText, dialogClickListener)
        builder.setNegativeButton(negText, dialogClickListener)
        if (showNeutralButton) builder.setNeutralButton(neuText, dialogClickListener)
        builder.show()
    }

    fun setActions(posAction: () -> Unit, negAction: () -> Unit, neuAction: () -> Unit = {}) {
        dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> posAction.invoke()
                DialogInterface.BUTTON_NEGATIVE -> negAction.invoke()
                DialogInterface.BUTTON_NEUTRAL -> neuAction.invoke()
            }
        }
    }
}