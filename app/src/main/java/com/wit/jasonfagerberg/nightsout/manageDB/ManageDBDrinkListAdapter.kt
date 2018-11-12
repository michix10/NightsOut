package com.wit.jasonfagerberg.nightsout.manageDB

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class ManageDBDrinkListAdapter(private val mContext: Context,private val mDrinksList: ArrayList<Drink>):
        androidx.recyclerview.widget.RecyclerView.Adapter<ManageDBDrinkListAdapter.ViewHolder>() {

    private lateinit var mMainActivity: MainActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.fragment_manage_db_item, parent, false)
        mMainActivity = mContext as MainActivity
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mDrinksList[position]

        holder.name.text = drink.name
        val abv = "ABV: " + "%.1f".format(drink.abv) + "%"
        holder.abv.text = abv
        val amount = "%.1f".format(drink.amount) + " " + drink.measurement
        holder.amount.text = amount

        drink.favorited = mMainActivity.mFavoritesList.contains(drink)

        if (drink.favorited) holder.favorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.favorite_red_36dp))
        else holder.favorite.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.favorite_border_red_36dp))

        holder.delete.setOnClickListener {
            val dialog = LightSimpleDialog(mContext)
            val loss = getLostReferenceString(drink)

            val posAction = {
                mMainActivity.mDatabaseHelper.deleteRowsInTable("drinks", "id = ${drink.id}")
                mDrinksList.remove(drink)
                removeCurrentSessionReference(drink)
                removeOrUpdateFavoritesReference(drink)
                removeOrUpdateRecentsReference(drink)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, mDrinksList.size)
            }
            dialog.setActions(posAction, {})
            dialog.show("Are you sure that you want to delete \"${drink.name}\"" +
                    " from database, this will remove all references to the drink.\n\nReferences Lost:\n$loss")
        }

        holder.favorite.setOnClickListener {
            drink.favorited = !drink.favorited
            drink.modifiedTime = mMainActivity.getLongTimeNow()
            for (d in mDrinksList){
                if (d == drink) d.favorited = drink.favorited
            }
            notifyDataSetChanged()

            if (!drink.favorited) mMainActivity.mFavoritesList.remove(drink)
            else mMainActivity.mFavoritesList.add(0, drink)

            for (d in mMainActivity.mDrinksList){
                if (d == drink) d.favorited = drink.favorited
            }
            if (drink.favorited) mMainActivity.showToast("${drink.name} favorited")
            else mMainActivity.showToast("${drink.name} unfavorited")
        }
    }

    override fun getItemCount(): Int {
        return mDrinksList.size
    }

    private fun getLostReferenceString(drink: Drink): String{
        var loss = ""

        for (d in mMainActivity.mDrinksList){
            if (d.isExactDrink(drink)){
                loss += "Drink in Current Drinks List\n"
                break
            }
        }
        for (f in mMainActivity.mFavoritesList){
            if (f.isExactDrink(drink)){
                loss += "Favorite Drink Reference\n"
                break
            }
        }
        for (r in mMainActivity.mRecentsList){
            if (r.isExactDrink(drink)){
                loss += "Recent Drink Reference\n"
                break
            }
        }

        if (mMainActivity.mDatabaseHelper.isLoggedDrink(drink.id)) loss += "Logged Drink Reference"

        return loss
    }

    private fun removeCurrentSessionReference(drink: Drink){
        for (i in mMainActivity.mDrinksList.indices){
            val d = mMainActivity.mDrinksList[i]
            if (d.isExactDrink(drink)){
                mMainActivity.mDrinksList.removeAt(i)
                break
            }
        }
    }

    private fun removeOrUpdateFavoritesReference(drink: Drink){
        for (i in mMainActivity.mFavoritesList.indices){
            val f = mMainActivity.mFavoritesList[i]
            if (f.isExactDrink(drink)){
                mMainActivity.mFavoritesList.removeAt(i)
                val drinks = mMainActivity.mDatabaseHelper.getDrinksFromName(drink.name)
                if (!drinks.isEmpty()){
                    mMainActivity.mFavoritesList.add(i, drinks[0])
                }
                break
            }
        }
    }

    private fun removeOrUpdateRecentsReference(drink: Drink){
        for (i in mMainActivity.mRecentsList.indices){
            val r = mMainActivity.mRecentsList[i]
            if (r.isExactDrink(drink)){
                mMainActivity.mRecentsList.removeAt(i)
                val drinks = mMainActivity.mDatabaseHelper.getDrinksFromName(drink.name)
                if (!drinks.isEmpty()){
                    mMainActivity.mRecentsList.add(i, drinks[0])
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_manage_db_drink_name)
        internal var abv: TextView = itemView.findViewById(R.id.text_manage_db_drink_abv)
        internal var amount: TextView = itemView.findViewById(R.id.text_manage_db_drink_amount)
        internal var delete: ImageView = itemView.findViewById(R.id.imgBtn_manage_db_delete)
        internal var favorite: ImageView = itemView.findViewById(R.id.imgBtn_manage_db_favorite)
    }
}