package com.yzqmusicplayer.model;

import java.io.Serializable;
import java.text.DecimalFormat;

import android.provider.CalendarContract.Instances;

public class Music implements Serializable
{
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//歌曲编号
private int id;
	//歌曲ID
private	int tractid;
	//歌曲标题
private	String title;
	//歌曲的专辑名
private	String album;
	//歌曲的歌手名
private	String artist;
	//歌曲文件的路径
private	String url;
	  //歌曲的总播放时长
private	int duration;
	//歌曲文件的大小 
private	long size;
	//歌曲文件显示名字
private	String disName;
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public int getTractid() {
	return tractid;
}
public void setTractid(int tractid) {
	this.tractid = tractid;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
public String getAlbum() {
	return album;
}
public void setAlbum(String album) {
	this.album = album;
}
public String getArtist() {
	if("<unknown>".equals(artist))
	{
		return "未知";
	}
	return artist;
}
public void setArtist(String artist) {
	this.artist = artist;
}
public String getUrl() {
	return url;
}
public void setUrl(String url) {
	this.url = url;
}
public int getDuration() {
	return duration;
}
public void setDuration(int duration) {
	this.duration = duration;
}
public long getSize() {
	return size;
}
public void setSize(long size) {
	this.size = size;
}
public String getDisName() {
	return disName;
}
public void setDisName(String disName) {
	this.disName = disName;
}
//返回对应显示的时长
public String getStringDuration()
{
	int time=duration/1000;
	int min=time/60;
	int second=time-min*60;
	String minStr=min<10?("0"+min):(min+"");
	String sedStr=second<10?("0"+second):(second+"");
	return minStr+":"+sedStr;	
}

public String getStringSize()
{
	DecimalFormat decimal=new DecimalFormat("#.##");
	String result=	decimal.format((size/10240)/102.4)+"MB";
	return result;
}
}
