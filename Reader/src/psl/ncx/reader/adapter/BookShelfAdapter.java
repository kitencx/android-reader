package psl.ncx.reader.adapter;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import psl.ncx.reader.MainActivity;
import psl.ncx.reader.R;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.views.CoverView;
import psl.ncx.reader.views.IndexButton;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

public class BookShelfAdapter extends BaseAdapter {
	private Context mContext;
	/**Book数据*/
	private List<Book> mData;
	/**停止按钮事件监听*/
	private View.OnClickListener mListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v instanceof IndexButton) {
				int position = ((IndexButton) v).getPosition();
				String id = getItem(position).bookid;
				MainActivity.stopDownloadById(id);
			} 
		}
	};
	
	public BookShelfAdapter(Context context){
		this.mContext = context;
		this.mData = MainActivity.BOOKS;
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Book getItem(int position) {
		return mData.get(position);
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
			mData.remove(position);
			notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FrameLayout frame = null;
		if (convertView == null) {
			frame = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.listitem_bookshelf, null);
		} else {
			frame = (FrameLayout) convertView;
		}
		CoverView cover = (CoverView) frame.findViewById(R.id.coverview);
		IndexButton stop = (IndexButton) frame.findViewById(R.id.imagebutton_stop);
		stop.setPosition(position);
		//判断当前View所显示Book是否正在下载中
		Book book = getItem(position);
		if (MainActivity.getDownloadStatusById(book.bookid)) {
			stop.setVisibility(View.VISIBLE);
		} else {
			stop.setVisibility(View.INVISIBLE);
		}
		
		stop.setOnClickListener(mListener);
		
		cover.setPercent(book.percent);
		cover.setTitle(book.bookname);
		
		Bitmap bitmap = null;
		if (book.cover != null) bitmap = DataAccessUtil.loadCoverImage(mContext, book.cover);
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
			if (DBAccessHelper.removeBookById(mContext, delBook.bookid)) {
				//删除封面
				if (delBook.cover != null) mContext.deleteFile(delBook.cover);
				//删除缓存
				File dir = mContext.getCacheDir();
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
