package psl.ncx.reader.db;

import java.util.ArrayList;

import psl.ncx.reader.constant.TableContract.BookEntry;
import psl.ncx.reader.constant.TableContract.ChapterEntry;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.model.ChapterLink;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBAccessHelper {
	/**
	 * 增加一本书进数据库，包括章节信息，如果章节信息无法添加，那么整本书都无法添加
	 * @param context
	 * @param book 插入的书信息
	 * @return 成功则返回插入行id，否则返回-1
	 * */
	public static long insert(Context context, Book book){
		if(book == null || book.bookname == null) return -1;
		
		SQLiteDatabase db = new DBOpenHelper(context).getWritableDatabase();
		//添加数据
		ContentValues values = new ContentValues();
		values.put(BookEntry.COLUMN_BOOKNAME, book.bookname);
		values.put(BookEntry.COLUMN_AUTHOR, book.author);
		values.put(BookEntry.COLUMN_COVER, book.cover);
		values.put(BookEntry.COLUMN_FROM, book.from);
		values.put(BookEntry.COLUMN_INDEXURL, book.indexURL);
		values.put(BookEntry.COLUMN_LATESTCHAPTER, book.latestChapter);
		values.put(BookEntry.COLUMN_SUMMARY, book.summary);
		values.put(BookEntry.COLUMN_SUMMARYURL, book.summaryURL);
		values.put(BookEntry.COLUMN_UPDATETIME, book.updateTime);
		values.put(BookEntry.COLUMN_BOOKMARK, book.bookmark);
		
		//开启事务
		db.beginTransaction();
		
		long result = db.insert(BookEntry.TABLE_NAME, null, values);
		//如果书本信息插入成功，则开始插入章节信息
		int size = book.catalog.size();
		ArrayList<ChapterLink> catalog = book.catalog;
		for(int i = 0; i < size; i++){
			values.clear();
			values.put(ChapterEntry.COLUMN_BOOK_ID, result);
			values.put(ChapterEntry.COLUMN_BOOKNAME, book.bookname);
			values.put(ChapterEntry.COLUMN_CHAPTERNAME, catalog.get(i).title);
			values.put(ChapterEntry.COLUMN_CHAPTERURL, catalog.get(i).link);
			if(db.insert(ChapterEntry.TABLE_NAME, null, values) == -1){
				//插入失败，直接结束事务，因为没有db.setTransactionSuccessful()，所以事务会回滚
				db.endTransaction();
				db.close();
				return -1;
			}
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		return result;
	}
	
	/**
	 * 根据bookid查询所有该书的章节信息
	 * @param context
	 * @param bookid 查询id
	 * @return 指定id的所有章节信息，Sting[]包含章节名、章节链接，不会为null
	 * */
	public static ArrayList<ChapterLink> queryChaptersById(Context context, String bookid){
		SQLiteDatabase db = new DBOpenHelper(context).getReadableDatabase();
		Cursor result = db.query(ChapterEntry.TABLE_NAME,
				new String[]{ChapterEntry.COLUMN_CHAPTERNAME, ChapterEntry.COLUMN_CHAPTERURL}, 
				ChapterEntry.COLUMN_BOOK_ID + "=?",	new String[]{bookid}, null, null, null);
		ArrayList<ChapterLink> chapters = new ArrayList<ChapterLink>();
		while(result.moveToNext()){
			String chaptername = result.getString(result.getColumnIndex(ChapterEntry.COLUMN_CHAPTERNAME));
			String chapterurl = result.getString(result.getColumnIndex(ChapterEntry.COLUMN_CHAPTERURL));
			chapters.add(new ChapterLink(chaptername, chapterurl));
		}
		result.close();
		db.close();
		return chapters;
	}
	
	/**
	 * 查询所有Book基本信息
	 * @param context
	 * @return 所有Book的基本信息，不包括章节信息，不会为null
	 * @see #queryChaptersById(Context, String)
	 * */
	public static ArrayList<Book> queryAllBooks(Context context){
		SQLiteDatabase db = new DBOpenHelper(context).getReadableDatabase();
		Cursor result = db.query(BookEntry.TABLE_NAME, null, null, null, null, null, null);
		ArrayList<Book> books = new ArrayList<Book>();
		while(result.moveToNext()){
			Book book = new Book();
			book.bookid = result.getString(result.getColumnIndex(BookEntry._ID));
			book.bookname = result.getString(result.getColumnIndex(BookEntry.COLUMN_BOOKNAME));
			book.author = result.getString(result.getColumnIndex(BookEntry.COLUMN_AUTHOR));
			book.indexURL = result.getString(result.getColumnIndex(BookEntry.COLUMN_INDEXURL));
			book.summaryURL = result.getString(result.getColumnIndex(BookEntry.COLUMN_SUMMARYURL));
			book.summary = result.getString(result.getColumnIndex(BookEntry.COLUMN_SUMMARY));
			book.updateTime = result.getString(result.getColumnIndex(BookEntry.COLUMN_UPDATETIME));
			book.latestChapter = result.getString(result.getColumnIndex(BookEntry.COLUMN_LATESTCHAPTER));
			book.bookmark = result.getInt(result.getColumnIndex(BookEntry.COLUMN_BOOKMARK));
			book.cover = result.getString(result.getColumnIndex(BookEntry.COLUMN_COVER));
			book.from = result.getString(result.getColumnIndex(BookEntry.COLUMN_FROM));
			books.add(book);
		}
		result.close();
		db.close();
		return books;
	}

	/**
	 * 更新书签
	 * @param context
	 * @param bookid 需要更新的书的id
	 * @param bookmark 新书签索引
	 * @return 更新操作影响的记录数
	 * */
	public static int updateBookMark(Context context, String bookid, int bookmark){
		SQLiteDatabase db = new DBOpenHelper(context).getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BookEntry.COLUMN_BOOKMARK, bookmark);
		int result = db.update(BookEntry.TABLE_NAME, values, BookEntry._ID + "=?", new String[]{bookid});
		db.close();
		return result;
	}
	
	/**
	 * 删除数据库中指定bookid的所有条目，包括章节信息，注意删除封面图片，此次删除操作不包括该图片
	 * @return 删除成功true，否则false
	 * */
	public static boolean removeBookById(Context context, String bookid){
		boolean result = false;
		SQLiteDatabase db = new DBOpenHelper(context).getWritableDatabase();
		db.beginTransaction();
		int result1 = db.delete(BookEntry.TABLE_NAME, BookEntry._ID + "=?", new String[]{bookid});
		int result2 = db.delete(ChapterEntry.TABLE_NAME, ChapterEntry.COLUMN_BOOK_ID  + "=?", new String[]{bookid});
		if (result1 != 0 && result2 != 0) {
			//都成功，则事件成功
			db.setTransactionSuccessful();
			result = true;
		}
		db.endTransaction();
		db.close();
		return result;
	}
}
