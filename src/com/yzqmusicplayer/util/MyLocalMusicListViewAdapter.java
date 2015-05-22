package com.yzqmusicplayer.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.yzqmusicplayer.R;
import com.yzqmusicplayer.model.Music;

public class MyLocalMusicListViewAdapter extends BaseAdapter {

	private List<Music> listData=new ArrayList<Music>();
	
	private Context context;
	
	public MyLocalMusicListViewAdapter(Context context, List<Music> localMusicListData)
	{
		this.context=context;
		listData= localMusicListData;
	} 
	
	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder=null;
		if(convertView==null)
		{
			convertView=LayoutInflater.from(context).inflate(R.layout.localmusiclistlitem, null);
			holder=new Holder();
			holder.textview1=(TextView) convertView.findViewById(R.id.itemsong);
			holder.textview2=(TextView) convertView.findViewById(R.id.itemsinger);
			holder.textview3=(TextView) convertView.findViewById(R.id.itemtime);
			holder.addbtn=(Button) convertView.findViewById(R.id.itemaddqueue);
			convertView.setTag(holder);
		}
		holder=(Holder) convertView.getTag();
		holder.textview1.setText(listData.get(position).getTitle());
		holder.textview2.setText(listData.get(position).getArtist());
		holder.textview3.setText(listData.get(position).getStringDuration());
		
		
		//添加到队列中以后发送广播
		holder.addbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					Intent musicIntent=new Intent();
					musicIntent.setAction("com.yzqmusicplayer.activity.addmusic");
					musicIntent.putExtra("addmusic", listData.get(position));
					context.sendBroadcast(musicIntent);
			}
		});
		
		return convertView;
	}
	class Holder
	{
		TextView textview1;
		TextView textview2;
		TextView textview3;
		Button addbtn;
	}

}
