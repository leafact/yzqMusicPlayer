package com.yzqmusicplayer.Logic;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MyViewPagerAdapter extends PagerAdapter {

	private List<View> data;
	public MyViewPagerAdapter(List<View> data) {
		this.data=data;
	}
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		((ViewPager)container).addView(data.get(position));
		return data.get(position);
	}
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager)container).removeView(data.get(position));
	}
	@Override
	public int getCount() {
		return data.size();
	}
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0==arg1;
	}

}
