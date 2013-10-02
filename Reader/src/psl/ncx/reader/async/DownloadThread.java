package psl.ncx.reader.async;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import psl.ncx.reader.business.ContentResolver;
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
	 * 当前正在下载的章节
	 * */
	private int mCurrentPage;
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
	
	/**
	 * 取得当前正在下载的章节索引
	 * */
	public int currentPage() {
		return mCurrentPage;
	}
	
	public int maxPages() {
		return mBook.catalog.size();
	}
	
	@Override
	public void run() {
		ArrayList<ChapterLink> chapters = mBook.catalog;

		if (chapters == null) return;
		
		int size = chapters.size();
		for (int i = 0 ; i < size; i++) {
			if (Thread.currentThread().isInterrupted()) {
				//每个循环都检查是否中断，如果中断，那么结束本任务
				break;
			}
			
			ChapterLink chapter = chapters.get(i);
			String title = chapter.title;
			String link = chapter.link;
			mCurrentPage = i + 1;
			try {
				Document doc = Jsoup.connect(link).timeout(10000).get();
				String content = ContentResolver.resolveContent(doc, mBook.from);
				DataAccessUtil.storeTextContent(context, content, mBook.bookid + "-" + title + ".txt");
				Intent intent = new Intent(DownloadService.class.getName());
				intent.putExtra("FINISHED", mCurrentPage * 100 / size);
				intent.putExtra("BOOKID", mBook.bookid);
				context.sendBroadcast(intent);
			} catch (IOException e) {
				//忽略当前的，继续下一个任务
			}
			
		}
	}

}
