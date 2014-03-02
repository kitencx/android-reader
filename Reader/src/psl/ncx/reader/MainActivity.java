package psl.ncx.reader;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import psl.ncx.reader.adapter.BookShelfAdapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity {
	/**ViewHolder，包含所有显示的View*/
	class ViewHolder {
		public ProgressBar loadIndicator;
		public GridView bookShelf;
		public ImageButton search;
		public ImageButton update;
	}
	/**所有下载线程的引用*/
	public static final Map<String, Thread> DL_TASKS = new HashMap<String, Thread>();
	public static final List<Book> BOOKS = new ArrayList<Book>();
	/**操作栏，提供搜索*/
	private ActionBar mActionBar;
	/**ViewHolder*/
	private ViewHolder mViewHolder;
	/**搜索菜单*/
	private MenuItem mSearchItem;
	/**书架数据源*/
	private BookShelfAdapter mAdapter;
	/**广播接收，用于更新ui*/
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String id = intent.getStringExtra(IntentConstant.BOOKID);
			int percent = intent.getIntExtra(IntentConstant.DOWNLOAD_PERCENT, 0);
			Book book = getBookById(id);
			if (book != null) {
				if (book.percent != percent) {
					book.percent = percent;
					mViewHolder.bookShelf.invalidateViews();
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		mActionBar = getActionBar();
		mActionBar.hide();

		//初始化Views
		mViewHolder = new ViewHolder();
		mViewHolder.loadIndicator = (ProgressBar) findViewById(R.id.pb_loadindicator);
		mViewHolder.bookShelf = (GridView) findViewById(R.id.gv_bookshelf);
		mViewHolder.search = (ImageButton) findViewById(R.id.toolbar_search);
		mViewHolder.update = (ImageButton) findViewById(R.id.toolbar_update);
		
		//初始化书架数据源
		mAdapter = new BookShelfAdapter(this);
		mViewHolder.bookShelf.setAdapter(mAdapter);
		
		//绑定事件监听
		bindListeners();
		
		//载入书籍信息
		new AsyncTaskLoadBooks().execute();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			new AsyncTaskLoadBooks().execute();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		mSearchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) mSearchItem.getActionView();
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
				intent.putExtra(IntentConstant.SEARCH_KEYWORD, query.trim());
				startActivityForResult(intent, 0);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mReceiver, new IntentFilter(getClass().getName()));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	/**
	 * 获取指定Book当前的下载状态
	 * @param id
	 * @return true:正在下载 false:不在下载
	 */
	public static boolean getDownloadStatusById(String id) {
		Thread task = DL_TASKS.get(id);
		if (task != null) {
			State state = task.getState();
			if (state == State.RUNNABLE ) {
				//当前线程正在运行
				return true;
			} 
		} 
		return false;
	}
	
	/**
	 * 停止指定Book的下载线程
	 * @param id 需要停止下载的Book的id，如果没有该book的下载任务或者该book的下载任务已经完成/停止，则无操作
	 */
	public static void stopDownloadById(String id) {
		Thread task = DL_TASKS.get(id);
		if (task != null) {
			State state = task.getState();
			if (state == State.RUNNABLE ) {
				//当前线程正在运行
				task.interrupt();
			}
		} 
	}
	
	/**
	 * 获取指定Book对象
	 * @param id	需要获取的Bookid
	 * @return 
	 */
	public static Book getBookById(String id) {
		for (Book book : BOOKS) {
			if (id.equals(book.bookid)) {
				return book;
			}
		}
		return null;
	}
	
	/**为View绑定事件监听*/
	private void bindListeners() {
		/**搜索按钮*/
		mViewHolder.search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mActionBar.isShowing()) mActionBar.hide();
				else mActionBar.show();
			}
		});
		
		/**更新按钮*/
		mViewHolder.update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//更新书籍按钮，点击需要异步处理
				v.setEnabled(false);		//防止重复点击，在更新任务结束之后会重置状态
				new AsyncTaskUpdate().execute();
			}
		});
		/**书架，点击阅读*/
		mViewHolder.bookShelf.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Book book = (Book) parent.getItemAtPosition(position);
				Intent intent = new Intent(MainActivity.this, ContentActivity.class);
				intent.putExtra(IntentConstant.BOOKID, book.bookid);
				startActivity(intent);
			}
		});
		/**书架，长按删除*/
		mViewHolder.bookShelf.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Book book = (Book) parent.getItemAtPosition(position);
				new AlertDialog.Builder(MainActivity.this).setTitle("注意")
				.setMessage("确定删除：\n\t《" + book.bookname + "》?")
				.setPositiveButton("删除", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mAdapter.remove(position);
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
				return true;
			}
		});
		mViewHolder.bookShelf.setOnTouchListener(new  OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mActionBar.isShowing()) mActionBar.hide();
				return false;
			}
		});
	}
	
	/**
	 * 书籍章节更新异步任务类
	 * */
	private class AsyncTaskUpdate extends AsyncTask<Void, Integer, ArrayList<Book>> {
		private ProgressDialog progress;
		
		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(MainActivity.this);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setTitle("正在更新，请稍候...");
			progress.setCancelable(false);
			progress.setMax(BOOKS.size());
			progress.show();
		}
		
		@Override
		protected ArrayList<Book> doInBackground(Void... params) {
			for (int i = 1; i <= BOOKS.size(); i++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				publishProgress(i);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			progress.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(ArrayList<Book> result) {
			progress.dismiss();
			mViewHolder.update.setEnabled(true);	//重置更新按钮状态
		}
	}
	
	/**
	 * 书籍信息异步载入任务类
	 * */
	private class AsyncTaskLoadBooks extends AsyncTask<Void, Void, ArrayList<Book>> {
		@Override
		protected void onPreExecute() {
			mViewHolder.loadIndicator.setVisibility(View.VISIBLE);
			mViewHolder.bookShelf.setVisibility(View.GONE);
		}
		
		@Override
		protected ArrayList<Book> doInBackground(Void... params) {
			return DBAccessHelper.queryAllBooks(MainActivity.this);
		}
		
		@Override
		protected void onPostExecute(ArrayList<Book> result) {
			BOOKS.clear();
			BOOKS.addAll(result);
			mAdapter.notifyDataSetChanged();
			mViewHolder.loadIndicator.setVisibility(View.GONE);
			mViewHolder.bookShelf.setVisibility(View.VISIBLE);
		}
		
	}
}
