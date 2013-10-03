package psl.ncx.reader.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import psl.ncx.reader.async.DownloadThread;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.model.Book;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DownloadService extends Service {
	/**
	 * 所有下载线程引用
	 * */
	private ArrayList<WeakReference<Thread>> mAllTask;
	
	private MyBinder binder = new MyBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		mAllTask = new ArrayList<WeakReference<Thread>>();
	}
	
	@Override
	public void onDestroy() {
		for (int i = 0; i < mAllTask.size(); i++) {
			Thread task = mAllTask.get(i).get();
			task.interrupt();
		}
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Book book = (Book) intent.getSerializableExtra(IntentConstant.BOOK_INFO);
		Thread task = new DownloadThread(this, book);
		mAllTask.add(new WeakReference<Thread>(task));
		task.start();
		return Service.START_NOT_STICKY;
	}
	
	public class MyBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
		
		public ArrayList<WeakReference<Thread>> allRunningTask() {
			return mAllTask;
		}
	}
	
}
