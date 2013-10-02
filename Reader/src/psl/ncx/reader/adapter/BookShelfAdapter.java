package psl.ncx.reader.adapter;

import java.util.ArrayList;

import psl.ncx.reader.R;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.views.CoverView;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BookShelfAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Book> books;
	
	public BookShelfAdapter(Context context, ArrayList<Book> data){
		this.context = context;
		this.books = data;
	}
	
	@Override
	public int getCount() {
		return books.size();
	}

	@Override
	public Book getItem(int position) {
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
		Book book = getItem(position);
		cover.setTitle(book.bookname);
		if (book.percent != 0){
			//如果有百分比，绘制背景渐变
			cover.setPercent(book.percent);
		}
		Bitmap img = null;
		if(book.cover != null) img = DataAccessUtil.loadCoverImage(context, book.cover);
		if(img == null){
			cover.setImageResource(R.drawable.cover);
		}else{
			cover.setImageBitmap(img);
		}
		
		return cover;
	}
}
