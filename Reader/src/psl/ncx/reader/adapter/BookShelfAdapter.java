package psl.ncx.reader.adapter;

import java.util.ArrayList;

import psl.ncx.reader.R;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.service.DownloadService;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.views.CoverView;
import psl.ncx.reader.views.IndexButton;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

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
		FrameLayout frame = null;
		if (convertView == null) {
			frame = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.listitem_bookshelf, null);
		} else {
			frame = (FrameLayout) convertView;
		}
		
		CoverView cover = (CoverView) frame.findViewById(R.id.coverview);
		IndexButton stop = (IndexButton) frame.findViewById(R.id.imagebutton_stop);
		stop.setPosition(position);
		Book book = getItem(position);
		
		if (book.percent > 0 && book.percent < 100) {
			//下载中，显示停止按钮
			stop.setVisibility(View.VISIBLE);
			stop.setOnClickListener(mBtnListener);
		} else {
			stop.setVisibility(View.GONE);
		}
		
		cover.setTitle(book.bookname);
		cover.setPercent(book.percent);
		Bitmap bitmap = null;
		if (book.cover != null) bitmap = DataAccessUtil.loadCoverImage(context, book.cover);
		if (bitmap != null) {
			cover.setImageBitmap(bitmap);
		} else {
			cover.setImageResource(R.drawable.cover);
		}
		
		return frame;
	}
	
	private View.OnClickListener mBtnListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			IndexButton btn = (IndexButton) v;
			int position = btn.getPosition();
			//根据position停止对应的Book下载
			System.err.println(position);
			context.stopService(new Intent(context, DownloadService.class));
		}
	};  
}
