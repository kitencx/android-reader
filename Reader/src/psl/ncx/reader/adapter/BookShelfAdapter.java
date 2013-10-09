package psl.ncx.reader.adapter;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import psl.ncx.reader.R;
import psl.ncx.reader.async.DownloadThread;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.service.DownloadService;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.views.CoverView;
import psl.ncx.reader.views.IndexButton;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

public class BookShelfAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Book> mBooks;
	private IBinder mBinder;
	private ServiceConnection mServConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBinder = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = service;
		}
	};
	
	public BookShelfAdapter(Context context, ArrayList<Book> books){
		this.context = context;
		this.mBooks = books;
		
		this.context.bindService(new Intent(this.context, DownloadService.class), mServConn, Context.BIND_AUTO_CREATE);
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
	
	/**
	 * 解除绑定的下载服务
	 * */
	public void unBindService() {
		if (mBinder != null) this.context.unbindService(mServConn);
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
		
		if (isDownloading(position)) {
			//下载中，显示停止按钮
			stop.setVisibility(View.VISIBLE);
			stop.setOnClickListener(mBtnListener);
		} else {
			stop.setVisibility(View.GONE);
		}
		
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
	
	private View.OnClickListener mBtnListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = ((IndexButton) v).getPosition();
			//根据position停止对应的Book下载，获取服务Binder
			ArrayList<WeakReference<Thread>> allTask = ((DownloadService.MyBinder) mBinder).allRunningTask();
			String cid = getItem(position).bookid;
			for (int i = 0; i < allTask.size(); i++) {
				DownloadThread task = (DownloadThread) allTask.get(i).get();
				if (task != null && task.isAlive()) {
					String lid = task.getBook().bookid;
					if (lid.equals(cid)) {
						task.interrupt();
					}
				}
			}
		}
	};
	
	/**
	 * 判断给定索引位置的Book是否正在后台下载，若后台服务没有成功绑定，则一直返回false
	 * */
	private boolean isDownloading(int position) {
		if (mBinder != null) {
			Book book = getItem(position);
			ArrayList<WeakReference<Thread>> allTask = ((DownloadService.MyBinder) mBinder).allRunningTask();
			int count = allTask.size();
			for (int i = 0; i < count; i++) {
				DownloadThread task = (DownloadThread) allTask.get(i).get();
				if (task != null && task.isAlive()) {
					Book b = task.getBook();
					if (b.bookid.equals(book.bookid)) {
						return true;
					}
				}
			}
		}
		return false;
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
