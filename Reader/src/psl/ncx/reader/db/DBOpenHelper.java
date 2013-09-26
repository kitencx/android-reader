package psl.ncx.reader.db;

import psl.ncx.reader.constant.TableContract.BookEntry;
import psl.ncx.reader.constant.TableContract.ChapterEntry;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "psl_ncx_reader";
	private static final String TYPE_TEXT = " TEXT";
	/**Book表创建SQL语句*/
	private static final String SQL_CREATE_BOOKENTRY = 
			"CREATE TABLE " + BookEntry.TABLE_NAME + "(" +
			BookEntry._ID + " INTEGER PRIMARY KEY," +
			BookEntry.COLUMN_BOOKNAME + TYPE_TEXT + "," +
			BookEntry.COLUMN_AUTHOR + TYPE_TEXT + "," +
			BookEntry.COLUMN_FROM + TYPE_TEXT + "," +
			BookEntry.COLUMN_SUMMARY + TYPE_TEXT + "," +
			BookEntry.COLUMN_COVER + TYPE_TEXT + "," +
			BookEntry.COLUMN_INDEXURL + TYPE_TEXT + "," +
			BookEntry.COLUMN_SUMMARYURL + TYPE_TEXT + "," +
			BookEntry.COLUMN_LATESTCHAPTER + TYPE_TEXT  + "," + 
			BookEntry.COLUMN_UPDATETIME + TYPE_TEXT  + "," + 
			BookEntry.COLUMN_BOOKMARK + " INTEGER);";
	/**Chapter表创建SQL语句*/
	private static final String SQL_CREATE_CHAPTERENTRY = 
			"CREATE TABLE " + ChapterEntry.TABLE_NAME + "(" +
			ChapterEntry._ID + "INTEGER PRIMARY KEY," +
			ChapterEntry.COLUMN_CHAPTERNAME + TYPE_TEXT + "," +
			ChapterEntry.COLUMN_CHAPTERURL + TYPE_TEXT + "," +
			ChapterEntry.COLUMN_BOOKNAME + TYPE_TEXT + "," +
			ChapterEntry.COLUMN_BOOK_ID + TYPE_TEXT + ");";
 	
	public DBOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_BOOKENTRY);
		db.execSQL(SQL_CREATE_CHAPTERENTRY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//待实现
	}

}
