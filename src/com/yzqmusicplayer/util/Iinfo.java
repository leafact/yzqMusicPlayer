package com.yzqmusicplayer.util;

/**
 * 
 * @author leaf
 *所有的常量
 */
public interface Iinfo {
	//分别对应播放的三种方式：顺序播放，单曲循环，随机播放
	int PLAY_RULE_ORDER=0x10;
	int PLAY_RULE_SINGLE=0x11;
	int PLAY_RULE_RANDOM=0x12;
	//timertask的週期
	
	int PLAY_TIMERTASK_SEEKBARTIME=500;
}
