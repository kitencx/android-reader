package psl.ncx.reader.service;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

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
	private Map<String, WeakReference<Thread>> mAllTask;
	/**binder for DownloadService*/
	private DownloadServiceBinder mBinder;
	
	@Override
	public IBinder onBind(Intent intent) {
		if (mBinder == null) {
			mBinder = new DownloadServiceBinder();
		}
		return mBinder;
	}

	@Override
	public void onCreate() {
		mAllTask = new HashMap<String, WeakReference<Thread>>();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Book target = (Book) intent.getSerializableExtra(IntentConstant.BOOK_INFO);
		if (target != null) {
			WeakReference<Thread> wrt = mAllTask.get(target.bookid);
			Thread oldtask = wrt == null ? null : wrt.get();
			if (oldtask == null || !oldtask.isAlive()) {
				mAllTask.remove(target.bookid);	//先移除已经终止的下载线程
				//创建新的下载任务
				DownloadThread task = new DownloadThread(this, target);
				mAllTask.put(target.bookid, new WeakReference<Thread>(task));
				task.start();
			} 
		}
		return Service.START_NOT_STICKY;
	}
	
	/**
	 * Binder，用于服务的外部访问
	 * */
	public class DownloadServiceBinder extends Binder{
		/**
		 * 获取指定id的书籍当前的下载状态
		 * @return true:正在下载，false:其它
		 * */
		public boolean getDownloadStatusById(String id) {
			if (mAllTask.containsKey(id)) {
				Thread task = mAllTask.get(id).get();
				//如果当前任务线程存活，并且没有被设置中断变量，则表示正在下载
				if (task != null && task.isAlive() && !task.isInterrupted()) return true;
			}
			return false;
		}
		
		/**
		 * 终止指定id的书籍的下载线程，如果该线程仍在运行的话
		 * */
		public void interruptDownloadThreadById(String id) {
			if (mAllTask.containsKey(id)) {
				Thread task = mAllTask.get(id).get();
				if (task != null && task.isAlive()) task.interrupt();
				else mAllTask.remove(id);
			}
		}
	}
}
