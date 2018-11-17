package com.wit.jasonfagerberg.nightsout.manageDB

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.databaseHelper.AddDrinkDatabaseHelper
import com.wit.jasonfagerberg.nightsout.dialogs.LightSimpleDialog
import com.wit.jasonfagerberg.nightsout.main.Drink
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class ManageDBFragment : Fragment() {
    private lateinit var mMainActivity: MainActivity
    private lateinit var mDrinkListAdapter: ManageDBDrinkListAdapter
    private lateinit var mDrinksList: ArrayList<Drink>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_db, container, false)
        mMainActivity = context as MainActivity
        mDrinksList = AddDrinkDatabaseHelper(mMainActivity).getSuggestedDrinks("", true)
        setupToolbar(view)
        setupRecycler(view)
        return view
    }

    private fun setupToolbar(view: View){
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_manage_db)
        toolbar.inflateMenu(R.menu.manage_db_menu)
        mMainActivity.setSupportActionBar(toolbar)
        mMainActivity.supportActionBar!!.setDisplayShowTitleEnabled(true)
        setHasOptionsMenu(true)
        toolbar.setNavigationIcon(R.drawable.arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { activity!!.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.manage_db_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val resId = item?.itemId
        when (resId) {
            R.id.btn_reset_db -> {
                val dialog = LightSimpleDialog(context!!)
                val posAction = {
                    mMainActivity.mDatabaseHelper.copyDatabase()
                    mMainActivity.mDatabaseHelper.pullDrinks()
                    mMainActivity.mDatabaseHelper.pullLogHeaders()

                    mDrinksList.clear()
                    mDrinksList.addAll(AddDrinkDatabaseHelper(mMainActivity).getSuggestedDrinks(""))
                    mDrinkListAdapter.notifyDataSetChanged()
                }
                dialog.setActions(posAction, {})
                dialog.show("Are you sure? You will lose everything.")
            }
        }
        return true
    }

    private fun setupRecycler(view: View){
        // mDrinkList recycler view setup
        val drinksListView: RecyclerView = view.findViewById(R.id.recycler_manage_db_list)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        drinksListView.layoutManager = linearLayoutManager

        val itemDecor = DividerItemDecoration(drinksListView.context, DividerItemDecoration.VERTICAL)
        drinksListView.addItemDecoration(itemDecor)

        // set adapter
        mDrinkListAdapter = ManageDBDrinkListAdapter(context!!, mDrinksList)
        //update list
        drinksListView.adapter = mDrinkListAdapter //Update display with new list
        drinksListView.layoutManager!!.scrollToPosition(mDrinksList.size - 1) //Nav to end of list
    }
}