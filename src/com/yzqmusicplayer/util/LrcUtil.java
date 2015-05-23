package com.yzqmusicplayer.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.yzqmusicplayer.model.MyLrc;

//对歌词进行解析
public class LrcUtil {

	public static void main(String[] args) {
		LrcUtil lrc = null;
		try {
			lrc = new LrcUtil(new FileInputStream("aaa.txt"));
			lrc.getWords();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static TreeSet<MyLrc> tree;

	// 将对应的lrc文件转化为treeMap,分别对应的时间以及歌词
	public LrcUtil(InputStream musicTitle) {
		TreeSet<MyLrc> treeset = new TreeSet<MyLrc>();
		// 用来存放歌曲的时间和对应的歌词
		InputStreamReader inReader = null;
		BufferedReader reader = null;
		try {
			inReader = new InputStreamReader(musicTitle);
			reader = new BufferedReader(inReader);
			String line = "";
			while ((line = reader.readLine()) != null) {
				// 对那行歌词进行分割,判断,然后存储
				String[] substr = line.split("\\]");
				for (String ss : substr) {
					if (ss.contains("[") && ss.contains(":")
							&& ss.contains(".")) {
						String sss = ss.replaceAll("\\[", "");
						String[] timeStart = sss.split(":");
						String[] timeEnd = timeStart[1].split("\\.");
						// 计算出当前的时间的毫秒数
						int time = (Integer.valueOf(timeStart[0]) * 60 + Integer
								.valueOf(timeEnd[0]))
								* 1000
								+ Integer.valueOf(timeEnd[1]) * 10;
						// 对应的时间放一个对应的歌词
						MyLrc lrc = new MyLrc();
						lrc.setTime(time);
						lrc.setLyric(substr[substr.length - 1]);
						treeset.add(lrc);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inReader.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		tree = treeset;
	}

	public List<String> getWords() {
		List<String> list = new ArrayList<String>();
		Iterator<MyLrc> it = tree.iterator();
		while (it.hasNext()) {
			MyLrc my = it.next();
			list.add(my.getLyric());
		}
		return list;
	}

	public List<Integer> getTimes() {
		List<Integer> list = new ArrayList<Integer>();
		Iterator<MyLrc> it = tree.iterator();
		while (it.hasNext()) {
			MyLrc my = it.next();
			list.add(my.getTime());
		}
		return list;
	}
}