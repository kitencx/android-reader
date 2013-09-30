package psl.ncx.reader.async;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import psl.ncx.reader.business.ContentResolver;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.model.ChapterLink;
import psl.ncx.reader.util.DataAccessUtil;
import android.content.Context;
import android.os.Handler;

public class DownloadThread implements Runnable {
	private Context context;
	private Handler mHandler;
	private Book mBook;
	
	public DownloadThread(Context context, Handler handler, Book book){
		this.context = context;
		this.mHandler = handler;
		this.mBook = book;
	}
	
	@Override
	public void run() {
		ArrayList<ChapterLink> chapters = mBook.catalog;
		
		if (chapters == null) return;
		
		for (int i = 0 ; i < chapters.size(); i++) {
			ChapterLink chapter = chapters.get(i);
			String title = chapter.title;
			String link = chapter.link;
			
			try {
				Document doc = Jsoup.connect(link).timeout(10000).get();
				String content = ContentResolver.resolveContent(doc, mBook.from);
				System.out.println(mBook.bookname + "-" + title + ":"  +
						DataAccessUtil.storeTextContent(context, content, mBook.bookid + "-" + title + ".txt"));
				mHandler.sendEmptyMessage(1);
			} catch (IOException e) {
				//忽略当前的，继续下一个任务
			}
		}
	}

}
