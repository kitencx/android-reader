package psl.ncx.reader.adapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import psl.ncx.reader.R;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.service.DownloadService.DownloadServiceBinder;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.views.CoverView;
import psl.ncx.reader.views.IndexButton;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

public class BookShelfAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Book> mBooks;
	private DownloadServiceBinder mBinder;
	/**停止按钮事件监听*/
	private View.OnClickListener mListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			IndexButton stop = (IndexButton) v;
			int index = stop.getPosition();
			String id = getItem(index).bookid;
			mBinder.interruptDownloadThreadById(id);
		}
	};
	
	public BookShelfAdapter(Context context, ArrayList<Book> books, DownloadServiceBinder binder){
		this.context = context;
		this.mBooks = books;
		this.mBinder = binder;
	}
	
	@Override
	public int getCount() {
		return mBooks.size();
	}

	@Override
	public Book getItem(int position) {
		return mBooks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * 删除指定位置的Book，并从存储空间中删除
	 * @param position 要删除Book的索引
	 * @return true成功，否则false
	 * */
	public boolean remove(int position) {
		//并且在数据库中删除
		if (removeBook(position)) {
			mBooks.remove(position);
			notifyDataSetChanged();
			return true;
		}
		return false;
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
		if (mBinder.getDownloadStatusById(book.bookid)) {
			stop.setVisibility(View.VISIBLE);
		} else {
			stop.setVisibility(View.INVISIBLE);
		}
		stop.setOnClickListener(mListener);
		
		cover.setPercent(book.percent);
		cover.setTitle(book.bookname);
		
		Bitmap bitmap = null;
		if (book.cover != null) bitmap = DataAccessUtil.loadCoverImage(context, book.cover);
		if (bitmap != null) {
			cover.setImageBitmap(bitmap);
		} else {
			cover.setImageResource(R.drawable.cover);
		}
		
		return frame;
	}

	/**
	 * 删除指定的Book
	 * @param 指定Book的索引
	 * @return true删除成功，false失败
	 * */
	private boolean removeBook(int position) {
		final Book delBook = getItem(position);
		if (delBook != null) {
			//删除Book信息
			if (DBAccessHelper.removeBookById(context, delBook.bookid)) {
				//删除封面
				if (delBook.cover != null) context.deleteFile(delBook.cover);
				//删除缓存
				File dir = context.getCacheDir();
				dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if (pathname.getName().startsWith(delBook.bookid + "-")) {
							pathname.delete();
						}
						return false;
					}
				});
			} else {
				return false;
			}
		}
		return true;
	}
}
