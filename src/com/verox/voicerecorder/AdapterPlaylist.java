package com.verox.voicerecorder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterPlaylist extends BaseAdapter {

	private Activity mContext;  
	private String [] playListTitle;
	private String [] playListSize;
	private String [] playListDuration;
	private String [] playListDate;
	LayoutInflater inflater;
	Typeface thin, medium, light;
	Context c;

	public AdapterPlaylist(Context context, String [] title, String [] size, String [] duration, String [] date) {  

		playListTitle = null;
		playListSize = null;
		playListDuration = null;
		playListDate = null;

		playListTitle = title; 
		playListSize = size;
		playListDuration = duration;
		playListDate = date;
		
		c = context;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return playListTitle.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		View row;
		row = inflater.inflate(R.layout.playlist_row, parent, false);

		TextView file_name, date_added, file_duration, file_size;

		light = Typeface.createFromAsset(c.getAssets(),"fonts/roboto-light.ttf"); 
		medium = Typeface.createFromAsset(c.getAssets(),"fonts/roboto-medium.ttf");
		thin = Typeface.createFromAsset(c.getAssets(),"fonts/roboto-thin.ttf");

		file_name = (TextView) row.findViewById(R.id.file_name);  
		file_name.setTypeface(medium);
		date_added = (TextView) row.findViewById(R.id.date_added);  
		date_added.setTypeface(light);
		file_duration = (TextView) row.findViewById(R.id.file_duration);  
		file_duration.setTypeface(light);
		file_size = (TextView) row.findViewById(R.id.file_size);
		file_size.setTypeface(light);
		
		file_name.setText(playListTitle[position]);
		date_added.setText(playListDate[position]);
		file_duration.setText(playListDuration[position]);
		file_size.setText(playListSize[position]+" KB");


		return (row);
	}

}
