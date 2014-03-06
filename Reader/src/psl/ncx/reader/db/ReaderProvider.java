package psl.ncx.reader.db;

import psl.ncx.reader.constant.TableContract;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ReaderProvider extends ContentProvider {
	private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int TABLE_BOOKENTRY = 1;
	private static final int TABLE_CHAPTERENTRY = 2;
	private DBOpenHelper mOpenHelper;
	
	static {
		mMatcher.addURI(TableContract.AUTHORITY, TableContract.BookEntry.TABLE_NAME, TABLE_BOOKENTRY);
		mMatcher.addURI(TableContract.AUTHORITY, TableContract.ChapterEntry.TABLE_NAME, TABLE_CHAPTERENTRY);
	}
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DBOpenHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db;
		SQLiteQueryBuilder queryBuilder;
		
		switch (mMatcher.match(uri)) {
		case TABLE_BOOKENTRY:
			db = mOpenHelper.getReadableDatabase();
			queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(TableContract.BookEntry.TABLE_NAME);
			Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		case TABLE_CHAPTERENTRY:
			db = mOpenHelper.getReadableDatabase();
			queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(TableContract.ChapterEntry.TABLE_NAME);
			c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		switch (mMatcher.match(uri)) {
		case TABLE_BOOKENTRY:
			return "vnd.android.cursor.dir/vnd.psl.ncx.provider." + TableContract.BookEntry.TABLE_NAME;
		case TABLE_CHAPTERENTRY:
			return "vnd.android.cursor.dir/vnd.psl.ncx.provider." + TableContract.ChapterEntry.TABLE_NAME;
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
