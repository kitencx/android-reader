package psl.ncx.reader.constant;

import android.provider.BaseColumns;

public interface TableContract {
	public static final String AUTHORITY = "psl.ncx.reader";
	
	public static interface BookEntry extends BaseColumns{
		public static final String TABLE_NAME = "reader_book";
		public static final String COLUMN_BOOKNAME = "bookname";
		public static final String COLUMN_AUTHOR = "author";
		public static final String COLUMN_FROM = "fromweb";
		public static final String COLUMN_UPDATETIME = "updatetime";
		public static final String COLUMN_LATESTCHAPTER = "latestchapter";
		public static final String COLUMN_SUMMARY = "description";
		public static final String COLUMN_INDEXURL = "indexurl";
		public static final String COLUMN_SUMMARYURL = "summaryurl";
		public static final String COLUMN_COVER = "cover";
		public static final String COLUMN_BOOKMARK = "bookmark";
	}
	
	public static interface ChapterEntry extends BaseColumns{
		public static final String TABLE_NAME = "reader_chapter";
		public static final String COLUMN_BOOKNAME = "bookname";
		public static final String COLUMN_CHAPTERNAME = "chaptername";
		public static final String COLUMN_CHAPTERURL = "chapterurl";
		public static final String COLUMN_BOOK_ID = "bookid";
	}
}
