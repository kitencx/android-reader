package psl.ncx.reader.adapter;

import java.util.ArrayList;
import psl.ncx.reader.R;
import psl.ncx.reader.views.CoverView;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BookShelfAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Object[]> books;
	
	public BookShelfAdapter(Context context, ArrayList<Object[]> data){
		this.context = context;
		this.books = data;
	}
	
	@Override
	public int getCount() {
		return books.size();
	}

	@Override
	public Object[] getItem(int position) {
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
			cover.setPadding(2, 2, 2, 2);
			cover.setAdjustViewBounds(true);
		}else{
			cover = (CoverView)convertView;
		}
		String bookname = (String) getItem(position)[0];
		cover.setTitle(bookname.substring(0, bookname.lastIndexOf('.')));
		Object coverimg = getItem(position)[1];
		if(coverimg != null){
			cover.setImageBitmap((Bitmap)coverimg);
		}else{
			cover.setImageResource(R.drawable.cover);
		}
		
		return cover;
	}

}
