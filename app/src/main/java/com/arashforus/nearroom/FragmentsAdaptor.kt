package com.arashforus.nearroom

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentsAdaptor(fa : FragmentActivity) :  FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ContactsFragment()
            1 -> ChatsFragment()
            2 -> NearoomsFragment()
            else -> ChatsFragment()
        }
    }

}