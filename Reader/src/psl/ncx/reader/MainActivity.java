package psl.ncx.reader;

import java.util.ArrayList;

import psl.ncx.reader.adapter.BookShelfAdapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.service.DownloadService;
import psl.ncx.reader.service.DownloadService.DownloadServiceBinder;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity {
	/**操作栏，提供搜索*/
	private ActionBar mActionBar;
	/**搜索菜单*/
	private MenuItem mSearchItem;
	/**书架视图*/
	private GridView mGridView;
	/**工具栏按钮：搜索、更新*/
	private ImageButton mBtnSearch, mBtnUpdate;
	/**书架数据源*/
	private BookShelfAdapter mAdapter;
	private ArrayList<Book> mData;
	/**下载服务绑定*/
	private DownloadServiceBinder mBinder;
	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBinder = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			mBinder = (DownloadServiceBinder) binder;
		}
	};
	/**下载进度显示*/
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String id = intent.getStringExtra("BOOKID");
			int percent = intent.getIntExtra(IntentConstant.DOWNLOAD_PERCENT, 0);
			System.out.println("接收广播，percent=" + percent + "，bookid=" + id);
			for (int i = 0; i < mData.size(); i++) {
				Book book = mData.get(i);
				if (book.bookid.equals(id)) {
					book.percent = percent;
					mGridView.invalidateViews();
					break;
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//绑定下载服务
		bindService(new Intent(this, DownloadService.class), mServiceConn, BIND_AUTO_CREATE);
		
		setContentView(R.layout.activity_main);
		
		mActionBar = getActionBar();
		mActionBar.hide();

		//初始化Views
		mGridView = (GridView) findViewById(R.id.bookshelf);
		mBtnSearch = (ImageButton) findViewById(R.id.toolbar_search);
		mBtnUpdate = (ImageButton) findViewById(R.id.toolbar_update);
		
		//初始化书架数据源
		mData = new ArrayList<Book>();
		
		//绑定事件监听
		bindListeners();
		
		//载入书籍信息
		new AsyncTaskLoadBooks().execute();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(mReceiver, new IntentFilter(DownloadService.class.getName()));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBinder != null) unbindService(mServiceConn);
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
	
	/**为View绑定事件监听*/
	private void bindListeners() {
		/**搜索按钮*/
		mBtnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mActionBar.isShowing()) mActionBar.hide();
				else mActionBar.show();
			}
		});
		
		/**更新按钮*/
		mBtnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//更新书籍按钮，点击需要异步处理
				v.setEnabled(false);		//防止重复点击，在更新任务结束之后会重置状态
				new AsyncTaskUpdate().execute();
			}
		});
		
		/**书架*/
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Book book = (Book) parent.getItemAtPosition(position);
				Intent intent = new Intent(MainActivity.this, ContentActivity.class);
				intent.putExtra(IntentConstant.BOOK_INFO, book);
				startActivity(intent);
			}
		});
		mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Book book = (Book) parent.getItemAtPosition(position);
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("注意")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("确定删除：\n《" + book.bookname + "》?")
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
		mGridView.setOnTouchListener(new  OnTouchListener() {
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
	class AsyncTaskUpdate extends AsyncTask<Void, Integer, ArrayList<Book>> {
		private ProgressDialog progress;
		
		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(MainActivity.this);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setTitle("正在更新，请稍候...");
			progress.setCancelable(false);
			progress.setMax(mData.size());
			progress.show();
		}
		
		@Override
		protected ArrayList<Book> doInBackground(Void... params) {
			for (int i = 1; i <= mData.size(); i++) {
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
			mBtnUpdate.setEnabled(true);	//重置更新按钮状态
		}
	}
	
	/**
	 * 书籍信息异步载入任务类
	 * */
	class AsyncTaskLoadBooks extends AsyncTask<Void, Void, ArrayList<Book>> {
		private ProgressDialog progress;
		
		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(MainActivity.this, "请稍后...", "正在载入书籍。");
			progress.setCancelable(false);
		}
		
		@Override
		protected ArrayList<Book> doInBackground(Void... params) {
			while (mBinder == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
			return DBAccessHelper.queryAllBooks(MainActivity.this);
		}
		
		@Override
		protected void onPostExecute(ArrayList<Book> result) {
			mData.clear();
			mData.addAll(result);
			if (mAdapter == null) {
				mAdapter = new BookShelfAdapter(MainActivity.this, mData, mBinder);
				mGridView.setAdapter(mAdapter);
			} else {
				mAdapter.notifyDataSetChanged();
			}
			if (progress != null) progress.dismiss();
		}
		
	}
}
