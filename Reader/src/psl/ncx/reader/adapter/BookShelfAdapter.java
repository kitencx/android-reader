package psl.ncx.reader.adapter;

import java.util.ArrayList;

import psl.ncx.reader.views.CoverView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BookShelfAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<String[]> books;
	
	public BookShelfAdapter(Context context, ArrayList<String[]> data){
		this.context = context;
		this.books = data;
	}
	
	@Override
	public int getCount() {
		return books.size();
	}

	@Override
	public String[] getItem(int position) {
		return books.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CoverView cover;
		if(convertView == null){
			cover = new CoverView(context);
			cover.setAdjustViewBounds(true);
		}else{
			cover = (CoverView)convertView;
		}
		String bookname = getItem(position)[1];
		cover.setTitle(bookname.substring(0, bookname.lastIndexOf('.')));
		cover.setImageResource(Integer.parseInt(getItem(position)[0]));
		
		return cover;
	}

}
