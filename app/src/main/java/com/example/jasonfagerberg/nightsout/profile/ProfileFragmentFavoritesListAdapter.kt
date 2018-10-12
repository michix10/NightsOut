package com.example.jasonfagerberg.nightsout.profile

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.jasonfagerberg.nightsout.main.Drink
import com.example.jasonfagerberg.nightsout.R
import java.util.ArrayList

class ProfileFragmentFavoritesListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<ProfileFragmentFavoritesListAdapter.ViewHolder>() {
    // vars
    private val mFavoriteDrinksList: MutableList<Drink> = drinksList

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.favorites_item, parent, false)
        return ViewHolder(view)
    }

    // When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mFavoriteDrinksList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener { _ ->
            holder.favorited = !holder.favorited

            if(holder.favorited){
                // make the toast
                val toast = Toast.makeText(mContext, "${holder.name.text} favorited", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 450)
                toast.show()

                holder.image.setImageResource(R.drawable.favorite_white_24dp)
                //mFavoriteDrinksList.add(drink)
            }else{
                // make the toast
                val toast = Toast.makeText(mContext, "${holder.name.text} unfavored", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 450)
                toast.show()

                holder.image.setImageResource(R.drawable.favorite_border_white_24dp)
                //mFavoriteDrinksList.remove(drink)
            }
        }
    }

    override fun getItemCount(): Int {
        return mFavoriteDrinksList.size
    }

    // ViewHolder for each item in list
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.text_profile_favorite_drink_name)
        internal val card: CardView = itemView.findViewById(R.id.card_profile_favorite_item)
        internal var image: ImageView = itemView.findViewById(R.id.image_profile_favorite)
        internal var favorited: Boolean = true
    }
}