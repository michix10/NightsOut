package com.wit.jasonfagerberg.nightsout.addDrink.drinkSuggestion

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class DrinkSuggestionArrayAdapter(private var mContext: Context, private var layoutResourceId: Int,
                                  var data: ArrayList<Drink>) : ArrayAdapter<Drink>(mContext, layoutResourceId, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            // inflate the layout
            val inflater = (mContext as MainActivity).layoutInflater
            view = inflater.inflate(layoutResourceId, parent, false)
        }

        // object item based on the position
        val drink = data[position]

        // get the TextView and then set the text (item name) and tag (item ID) values
        val nameTextView = view!!.findViewById<TextView>(R.id.text_add_drink_suggestion_name)
        val abvTextView = view.findViewById<TextView>(R.id.text_add_drink_suggestion_abv)
        val amountTextView = view.findViewById<TextView>(R.id.text_add_drink_suggestion_amount)

        view.findViewById<ImageView>(R.id.imgBtn_add_drink_suggestion_cancel).setOnClickListener {
            val dialog = LightSimpleDialog(mContext)
            val posAction = {
                remove(drink)
                notifyDataSetInvalidated()
            }
            val neuAction = {
                remove(drink)
                notifyDataSetChanged()
                (mContext as MainActivity).showRemoveSuggestionDialog = false
            }
            dialog.setActions(posAction, {}, neuAction)
            dialog.showNeutralButton = true
            if ((mContext as MainActivity).showRemoveSuggestionDialog){
                dialog.show("Are you sure you want to remove ${drink.name} from your suggestion list? This action can be reversed in the 'Manage Database' page.",
                        neuText = "Don't Show Again")
            } else posAction.invoke()

        }

        nameTextView.text = drink.name
        val abv = "${"%.2f".format(drink.abv)}%"
        abvTextView.text = abv
        val amount = "${"%.1f".format(drink.amount)} ${drink.measurement}"
        amountTextView.text = amount

        return view
    }

    override fun remove(`object`: Drink?) {
        data.remove(`object`)
        val mainActivity = (mContext as MainActivity)
        mainActivity.mDatabaseHelper.updateDrinkSuggestionStatus(`object`!!.id, true)
        mainActivity.addDrinkFragment.autoCompleteView.setAdapter(DrinkSuggestionArrayAdapter(mContext, layoutResourceId, data))
        if (!data.isEmpty()) mainActivity.addDrinkFragment.autoCompleteView.showDropDown()
        else mainActivity.addDrinkFragment.autoCompleteView.dismissDropDown()
    }
}