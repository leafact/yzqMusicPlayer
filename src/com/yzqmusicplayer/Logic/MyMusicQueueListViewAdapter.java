package com.yzqmusicplayer.Logic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.yzqmusicplayer.R;
import com.yzqmusicplayer.model.Music;

public class MyMusicQueueListViewAdapter extends BaseAdapter {

	private List<Music> listData=new ArrayList<Music>();
	
	private Context context;
	
	public MyMusicQueueListViewAdapter(Context context, List<Music> MusicQueueListData)
	{
		this.context=context;
		listData= MusicQueueListData;
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
			convertView=LayoutInflater.from(context).inflate(R.layout.musicqueuelistlitem, null);
			holder=new Holder();
			holder.textview1=(TextView) convertView.findViewById(R.id.queueitemsong);
			holder.textview2=(TextView) convertView.findViewById(R.id.queueitemsinger);
			holder.textview3=(TextView) convertView.findViewById(R.id.queueitemtime);
			holder.delbtn=(Button) convertView.findViewById(R.id.queueitemdelete);
			convertView.setTag(holder);
		}
		holder=(Holder) convertView.getTag();
		holder.textview1.setText(listData.get(position).getTitle());
		holder.textview2.setText(listData.get(position).getArtist());
		holder.textview3.setText(listData.get(position).getStringDuration());
		//监听移除，然后notifyDataSetChanged();
		holder.delbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					listData.remove(position);
					notifyDataSetChanged();
			}
		});
		
		return convertView;
	}
	class Holder
	{
		TextView textview1;
		TextView textview2;
		TextView textview3;
		Button delbtn;
	}

}
