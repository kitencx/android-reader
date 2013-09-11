package psl.ncx.reader.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChapterListAdapter extends BaseAdapter {
	private Context context;
	private int layout;
	private ArrayList<String[]> chapters;
	
	public ChapterListAdapter(Context context, int layout, ArrayList<String[]> chapters){
		this.context = context;
		this.layout = layout;
		this.chapters = chapters;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return chapters.size();
	}

	@Override
	public String[] getItem(int position) {
		// TODO Auto-generated method stub
		return chapters.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(this.context).inflate(layout, null);
		}
		((TextView)convertView).setText(getItem(position)[0]);
		
		return convertView;
	}

}
