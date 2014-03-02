package psl.ncx.reader.async;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import psl.ncx.reader.MainActivity;
import psl.ncx.reader.business.ContentResolver;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.model.ChapterLink;
import psl.ncx.reader.util.DataAccessUtil;
import android.content.Context;
import android.content.Intent;

/**
 * 下载线程，待定，线程无法独立运行，跨Activity访问，考虑更换Service
 * */
public class DownloadThread extends Thread {
	private Context mContext;
	/**下载的Bookid*/
	private String mId;

	public DownloadThread(Context context, String id){
		this.mContext = context;
		this.mId = id;
	}
	
	/**
	 * 获取当前线程下载的Bookid
	 */
	public String getDownloadBookId() {
		return this.mId;
	}
	
	@Override
	public void run() {
		Book book = MainActivity.getBookById(mId);
		ArrayList<ChapterLink> chapters = book.catalog;
		
		int size = chapters.size();
		for (int i = 0 ; i < size; i++) {
			if (Thread.currentThread().isInterrupted()) {
				//每个循环都检查是否中断，如果中断，那么结束本任务
				break;
			}
			
			ChapterLink chapter = chapters.get(i);
			String title = chapter.title;
			String link = chapter.link;
			try {
				String filename = book.bookid + "-" + title.replaceAll("[/\\s\\.\\\\]", "") + ".txt";
				if (!DataAccessUtil.exists(mContext, filename)) {
					Document doc = Jsoup.connect(link).timeout(10000).get();
					String content = ContentResolver.resolveContent(doc, book.from);
					DataAccessUtil.storeTextContent(mContext, content, filename);
				}
				int percent = (i + 1) * 100 / size;
				Intent intent = new Intent(MainActivity.class.getName());
				intent.putExtra(IntentConstant.BOOKID, mId);
				intent.putExtra(IntentConstant.DOWNLOAD_PERCENT, percent);
				mContext.sendBroadcast(intent);
			} catch (IOException e) {
				//忽略当前的，继续下一个任务
			}
		}
		Intent intent = new Intent(MainActivity.class.getName());
		intent.putExtra(IntentConstant.BOOKID, mId);
		intent.putExtra(IntentConstant.DOWNLOAD_PERCENT, 100);
		mContext.sendBroadcast(intent);
	}
}
