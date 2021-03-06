package com.example.sam.nytimessearch.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.sam.nytimessearch.PageFragment;

/**
 * Created by Sam on 7/31/16.
 */

public class searchFragmentPagerAdapter extends SmartFragmentStatePagerAdapter {

    private String tabTitles[] = new String[] { "All", "Politics", "Tech"};
    final int PAGE_COUNT = 3;

    public searchFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


}
