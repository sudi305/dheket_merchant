package com.bgs.chat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bgs.chat.fragments.ChatContactFragment;
import com.bgs.chat.fragments.ChatContactHistoryFragment;

import java.util.Vector;

/**
 * Created by zhufre on 6/13/2016.
 */
public class ChatTabPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Chats", "Contacts" };

    Vector<Fragment> fragments = new Vector<Fragment>();

    public ChatTabPagerAdapter(FragmentManager fm) {
        super(fm);

        fragments.add(0, new ChatContactHistoryFragment());
        fragments.add(1, new ChatContactFragment());

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}


