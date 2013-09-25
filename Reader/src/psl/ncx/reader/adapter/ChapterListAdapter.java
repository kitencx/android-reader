package psl.ncx.reader.adapter;

import java.util.ArrayList;

import psl.ncx.reader.R;

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
	private int selectedItem;
	
	public ChapterListAdapter(Context context, int layout, ArrayList<String[]> chapters){
		this.context = context;
		this.layout = layout;
		this.chapters = chapters;
		//初始化为-1，即默认没有条目被选中
		selectedItem = -1;
	}
	
	@Override
	public int getCount() {
		return chapters.size();
	}

	@Override
	public String[] getItem(int position) {
		return chapters.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setSelectedPosition(int position){
		this.selectedItem = position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView chapter;
		if(convertView == null){
			chapter = (TextView) LayoutInflater.from(this.context).inflate(layout, null);
		}else{
			chapter = (TextView) convertView;
		}
		chapter.setText(getItem(position)[0]);
		if (position == selectedItem) {
			chapter.setBackgroundColor(0xff3366ff);
			chapter.setTextColor(0xffffffff);
		}else{
			chapter.setBackgroundResource(R.drawable.selector_chapter);
			chapter.setTextColor(context.getResources().getColorStateList(R.drawable.selector_chapter_text));
		}
		
		return chapter;
	}

}
