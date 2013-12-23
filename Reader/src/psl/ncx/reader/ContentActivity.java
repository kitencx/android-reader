package psl.ncx.reader;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.service.DownloadService;
import psl.ncx.reader.service.DownloadService.DownloadServiceBinder;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.util.HttpUtil;
import psl.ncx.reader.views.PagedView;
import psl.ncx.reader.views.PagedView.OnPagingListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ScrollView;

public class ContentActivity extends Activity {
	private final String CONNECT_FAILED = "connect_failed";
	/**
	 * 标识是否开启内容缓存
	 * */
	private boolean useCache = true;
	/**
	 * 页面组件
	 * */
	private PagedView contentView;
	private ActionBar mActionBar;
	private MenuItem mDownloadItem;
	/**
	 * 当前阅读的Book对象
	 * */
	private Book mBook;
	/**
	 * 当前阅读的章节
	 * */
	private int position;
	/**
	 * 翻页事件监听
	 * */
	private PageDownListener listener;
	/**
	 * 屏幕尺寸
	 * */
	private Point screenSize;
	/**
	 * 手势检测
	 * */
	private GestureDetector textGesture;
	/**
	 * 用于标识翻页之后显示第一页还是最后一页
	 * */
	private boolean showLast;
	/**
	 * Binder，用于DownloadService的访问操作
	 * */
	private DownloadServiceBinder mBinder;
	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBinder = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (DownloadServiceBinder) service;
		}
	};
	
	private OnPagingListener pagingListener = new OnPagingListener() {

		@Override
		public void onPageUp(View v) {
		}

		@Override
		public void onPageDown(View v) {
		}

		@Override
		public void onPageOverForward(View v) {
			if (position < mBook.catalog.size() - 1) {
				position++;
				showLast = false;
				new LoadContent().execute();
			} else {
				new AlertDialog.Builder(ContentActivity.this).setMessage("已经是最后一章！").show();
			}
		}

		@Override
		public void onPageOverBack(View v) {
			if (position > 0) {
				position--;
				showLast = true;
				new LoadContent().execute();
			} else {
				new AlertDialog.Builder(ContentActivity.this).setMessage("已经是第一章！").show();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_content);
		
		mActionBar = getActionBar();
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.hide();
		
		bindService(new Intent(this, DownloadService.class), mServiceConn, Service.BIND_AUTO_CREATE);
		
		contentView = (PagedView) findViewById(R.id.view_content);
		contentView.setTextSize(getSharedPreferences("ReaderPreference", 
				MODE_PRIVATE).getFloat("DEFAULT_TEXTSIZE", 36.0f));
		contentView.setLongClickable(true);
		contentView.enableDragOver(true);
		
		screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		
		textGesture = new GestureDetector(this, new SimpleOnGestureListener(){
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				if (mActionBar.isShowing()) mActionBar.hide();
				return false;
			}
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				//点击显示ActionBar
				int cx = screenSize.x / 2, cy = screenSize.y / 2;
				float x = e.getX(), y = e.getY();
				if (x > cx - 100 && x < cx + 100 && y > cy -100 && y < cy + 100) {
					if(mActionBar.isShowing()) mActionBar.hide();
					else {
						if (mBinder != null) {
							if (mBinder.getDownloadStatusById(mBook.bookid)) {
								if (mDownloadItem != null) {
									mDownloadItem.setIcon(R.drawable.downloading);
									mDownloadItem.setEnabled(false);
								}
							} else {
								mDownloadItem.setIcon(R.drawable.download);
								mDownloadItem.setEnabled(true);
							}
						}
						mActionBar.show();
					}
					return true;
				}
				return false;
			}
		}); 
		listener = new PageDownListener();
		
		contentView.setOnPagingListener(pagingListener);
		contentView.setOnTouchListener(listener);
		
		Intent intent = getIntent();
		mBook = (Book) intent.getSerializableExtra(IntentConstant.BOOK_INFO);
		position = mBook.bookmark < 0 ? 0 : mBook.bookmark;
		
		new LoadContent().execute();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			//返回书架，必须设置Flag，否则只会新建一个MainActivity
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.action_catalog:
			if (mBook.catalog == null) {
				//如果目录为空，表示载入目录的异步任务没有完成
				new AlertDialog.Builder(this).setMessage("正在载入目录，请稍等！").show();
			} else {
				mActionBar.hide();
				intent = new Intent(this, ChapterActivity.class);
				intent.putExtra(IntentConstant.OPEN_INDEX, position);
				intent.putExtra(IntentConstant.BOOK_INFO, mBook);
				startActivityForResult(intent, 0);
				overridePendingTransition(R.anim.in_from_top, R.anim.stay);
			}
			break;
		case R.id.action_download:
			//下载任务开启
			item.setEnabled(false);
			item.setIcon(R.drawable.downloading);
			intent = new Intent(this, DownloadService.class);
			intent.putExtra(IntentConstant.BOOK_INFO, mBook);
			startService(intent);
			break;
		case R.id.action_font_increment:
			if (contentView != null) {
				float textSize = contentView.getTextSize();
				if (textSize < 80.0f) {
					contentView.setTextSize(textSize + 2);
					contentView.invalidate();
				}
			}
			break;
		case R.id.action_font_decrement:
			if (contentView != null) {
				float textSize = contentView.getTextSize();
				if (textSize > 16.0f) {
					contentView.setTextSize(textSize - 2);
					contentView.invalidate();
				}
			}
		}
		return true;
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		return textGesture.onTouchEvent(event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case 0:
			switch(resultCode){
			case Activity.RESULT_OK:
				position = data.getIntExtra(IntentConstant.OPEN_INDEX, 0);
				showLast = false;	//当用户选择某个章节时，永远显示第一页
				new LoadContent().execute();
				break;
			}
			break;
		}
	}
	
	/**
	 * 覆写该方法，使它每次返回的时候都回到书架
	 * */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		//返回书架，必须设置Flag，否则只会新建一个MainActivity
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.content, menu);
		mDownloadItem = menu.findItem(R.id.action_download);
		return true;
	}
	/**
	 * 方法被覆写，触发书签保存操作
	 * */
	@Override
	protected void onPause() {
		super.onPause();
		//保存书签
		DBAccessHelper.updateBookMark(this, mBook.bookid, position);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBinder != null) unbindService(mServiceConn);
	}
	
	/**
	 * 异步任务，获取章节内容
	 * */
	class LoadContent extends AsyncTask<Void, Void, String>{
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(ContentActivity.this, "请稍候！", "正在载入内容。", true, false);
		}
		
		@Override
		protected String doInBackground(Void... params) {
			if (mBook.catalog == null) {	//如果没有载入目录，则先载入，并校验载入章节索引的有效性
				mBook.catalog = DBAccessHelper.queryChaptersById(ContentActivity.this, mBook.bookid);
				if (position < 0) position = 0;
				else if (position >= mBook.catalog.size()) position = mBook.catalog.size() - 1;
			}
			
			String cname = mBook.catalog.get(position).title.replaceAll("[/\\s\\.\\\\]", ""); 
			String url = mBook.catalog.get(position).link;

			//先从缓存中读取内容
			String textContent = DataAccessUtil
					.loadTextContentFromCache(ContentActivity.this, mBook.bookid + "-" + cname + ".txt");
			if (textContent != null) {
				if (textContent.equals("")) {	//删除不包含内容的缓存文件
					File file = new File(getCacheDir(), mBook.bookid + "-" + cname + ".txt");
					file.delete();
				} else {
					return textContent;
				}
			}
			
			if (!HttpUtil.hasAvaliableNetwork(ContentActivity.this)) return null;
			//从网络获取内容
			Document doc = null;
			try {
				doc = Jsoup.connect(url).timeout(10000).get();
			} catch (IOException e) {
				return CONNECT_FAILED;
			}
			String content = psl.ncx.reader.business.ContentResolver.resolveContent(doc, mBook.from);
			if (useCache && !StringUtil.isBlank(content)) {
				DataAccessUtil.storeTextContent(ContentActivity.this, content, mBook.bookid + "-" + cname + ".txt");
			}
			
			//等待下载服务绑定完成
			while(mBinder == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
			
			return content;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (dialog != null) dialog.dismiss();
			
			if(result == null){
				AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this, R.style.SimpleDialogTheme);
				builder.setMessage("没有可用的网络！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			}else if(CONNECT_FAILED.equals(result)){
				showConnectFailed(result);
			}else{
				showTextContent(result);
			}
		}
		
		/**
		 * 替换所有半角标点为全角，美化文本显示
		 * */
		private String replacePunctuMarks(String src){
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < src.length(); i++){
				char c = src.charAt(i);
				switch(c){
				case 160:
				case 32:
					//替换空格
					builder.append((char)160).append(((char)160));
					break;
				case 8220:
					//替换双引号
					builder.append((char)12317);
					break;
				case 8221:
					builder.append((char)12318);
					break;
				default:
					builder.append(c);
				}
			}
			return builder.toString();
		}
		
		/**
		 * 连接失败，则显示重试按钮
		 * */
		private void showConnectFailed(String result){
			setContentView(R.layout.button_center);
			Button retry = (Button) findViewById(R.id.button_center);
			retry.setText("连接失败，重试？");
			retry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new LoadContent().execute();
				}
			});
		}
		
		/**
		 * 显示文本章节内容
		 * */
		private void showTextContent(String result){
			result = replacePunctuMarks(result);
			contentView.setTitle(mBook.catalog.get(position).title);
			contentView.setText(result, showLast);
		}
	}
	
	/**
	 * 翻页事件监听
	 * */
	private class PageDownListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(v instanceof ScrollView){
			} else if (v instanceof PagedView) {
				return textGesture.onTouchEvent(event);
			}
			return false;
		}
	}
}
