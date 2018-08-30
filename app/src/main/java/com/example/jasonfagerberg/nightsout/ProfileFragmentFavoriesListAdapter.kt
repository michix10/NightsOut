package com.example.jasonfagerberg.nightsout

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.ArrayList

class ProfileFragmentFavoriesListAdapter(private val mContext: Context, drinksList: ArrayList<Drink>) :
        RecyclerView.Adapter<ProfileFragmentFavoriesListAdapter.ViewHolder>() {
    // vars
    private val mFavoriteDrinksList: MutableList<Drink> = drinksList

    private val mMainActivity: MainActivity = mContext as MainActivity

    // set layout inflater & inflate layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.fragment_profile_favorites_item, parent, false)
        return ViewHolder(view)
    }

    // When view is rendered bind the correct holder to it
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = mFavoriteDrinksList[position]
        holder.name.text = drink.name
        holder.card.setOnClickListener { _ ->
            holder.favorited = !holder.favorited

            if(holder.favorited){
                Toast.makeText(mContext, "Drink: ${holder.name.text} favorited", Toast.LENGTH_SHORT).show()
                holder.image.setImageResource(R.drawable.favorite_white_24dp)
                mFavoriteDrinksList.add(drink)
            }else{
                Toast.makeText(mContext, "Drink: ${holder.name.text} unfavorited", Toast.LENGTH_SHORT).show()
                holder.image.setImageResource(R.drawable.favorite_border_white_24dp)
                mFavoriteDrinksList.remove(drink)
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