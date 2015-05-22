package com.yzqmusicplayer.model;

public class MyLrc implements Comparable{
	private int time;
	private String lyric;
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getLyric() {
		return lyric;
	}
	public void setLyric(String lyric) {
		this.lyric = lyric;
	}
	//放在set集合中可以看下面要求进行排序
	@Override
	public int compareTo(Object arg0) {
		int later=0;
		if(arg0 instanceof MyLrc)
		{
			later=((MyLrc)arg0).getTime();
		}
		return this.time-later;
	}
	@Override
	public String toString() {
		return this.time+""+this.lyric;
	}
	
}
