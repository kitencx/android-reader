package psl.ncx.reader.async;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import psl.ncx.reader.business.ContentResolver;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.model.ChapterLink;
import psl.ncx.reader.service.DownloadService;
import psl.ncx.reader.util.DataAccessUtil;
import android.content.Context;
import android.content.Intent;

/**
 * 下载线程，待定，线程无法独立运行，跨Activity访问，考虑更换Service
 * */
public class DownloadThread extends Thread {
	private Context context;
	/**
	 * 下载的Book
	 * */
	private Book mBook;
	/**
	 * handler，用于更新UI
	 * */
	public DownloadThread(Context context, Book book){
		this.context = context;
		this.mBook = book;
	}
	
	/**
	 * 取得当前线程正在下载的Book
	 * */
	public Book getBook() {
		return mBook;
	}
	
	@Override
	public void run() {
		ArrayList<ChapterLink> chapters = mBook.catalog;

		if (chapters == null) return;
		
		int size = chapters.size();
		int percent = 0;
		for (int i = 0 ; i < size; i++) {
			if (Thread.currentThread().isInterrupted()) {
				//每个循环都检查是否中断，如果中断，那么结束本任务
				break;
			}
			
			ChapterLink chapter = chapters.get(i);
			String title = chapter.title;
			String link = chapter.link;
			try {
				String filename = mBook.bookid + "-" + title.replaceAll("[/\\s\\.\\\\]", "") + ".txt";
				if (!DataAccessUtil.exists(context, filename)) {
					Document doc = Jsoup.connect(link).timeout(10000).get();
					String content = ContentResolver.resolveContent(doc, mBook.from);
					DataAccessUtil.storeTextContent(context, content, filename);
				}
				percent = (i + 1) * 100 / size;
				Intent intent = new Intent(DownloadService.class.getName());
				intent.putExtra(IntentConstant.DOWNLOAD_PERCENT, percent);
				intent.putExtra("BOOKID", mBook.bookid);
				context.sendBroadcast(intent);
			} catch (IOException e) {
				//忽略当前的，继续下一个任务
			}
		}
		//任务结束，无论下载至何处，都发送结束信号
		Intent intent = new Intent(DownloadService.class.getName());
		intent.putExtra(IntentConstant.DOWNLOAD_PERCENT, 100);
		intent.putExtra("BOOKID", mBook.bookid);
		context.sendBroadcast(intent);
	}
}
