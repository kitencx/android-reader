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
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.util.HttpUtil;
import psl.ncx.reader.views.PagedView;
import psl.ncx.reader.views.PagedView.OnPagingListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
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
	/**
	 * 当前阅读的Book对象
	 * */
	private Book book;
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
	
	private OnPagingListener pagingListener = new OnPagingListener() {

		@Override
		public void onPageUp(View v) {
		}

		@Override
		public void onPageDown(View v) {
		}

		@Override
		public void onPageOverForward(View v) {
			position++;
			new LoadContent().execute();
		}

		@Override
		public void onPageOverBack(View v) {
			position--;
			new LoadContent().execute();
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActionBar = getActionBar();
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.hide();

		screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		
		textGesture = new GestureDetector(this, new SimpleOnGestureListener(){
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				//点击显示ActionBar
				int cx = screenSize.x / 2, cy = screenSize.y / 2;
				float x = e.getX(), y = e.getY();
				if (x > cx - 100 && x < cx + 100 && y > cy -100 && y < cy + 100) {
					if(mActionBar.isShowing()) mActionBar.hide();
					else mActionBar.show();
					return true;
				}
				return false;
			}
		}); 
		listener = new PageDownListener();
		
		Intent intent = getIntent();
		book = (Book) intent.getSerializableExtra(IntentConstant.BOOK_INFO);
		if (book.catalog == null) book.catalog = DBAccessHelper.queryChaptersById(this, book.bookid);
		position = book.bookmark < 0 ? 0 : book.bookmark;
		
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
			mActionBar.hide();
			intent = new Intent(this, ChapterActivity.class);
			intent.putExtra(IntentConstant.OPEN_INDEX, position);
			intent.putExtra(IntentConstant.BOOK_INFO, book);
			startActivityForResult(intent, 0);
			overridePendingTransition(R.anim.in_from_top, R.anim.stay);
			break;
		case R.id.action_download:
			//下载任务开启
			item.setEnabled(false);
			item.setIcon(R.drawable.downloading);
			intent = new Intent(this, DownloadService.class);
			intent.putExtra(IntentConstant.BOOK_INFO, book);
			startService(intent);
			break;
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
		return true;
	}
	/**
	 * 方法被覆写，触发书签保存操作
	 * */
	@Override
	protected void onPause() {
		super.onPause();
		//保存书签
		DBAccessHelper.updateBookMark(this, book.bookid, position);
	}
	
	/**
	 * 异步任务，获取章节内容
	 * */
	class LoadContent extends AsyncTask<Void, Void, String>{
		@Override
		protected void onPreExecute() {
			if (contentView != null) contentView.abortAnimation();
			//显示载入动画
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.setOnTouchListener(listener);
			processing.startAnimation(AnimationUtils.loadAnimation(ContentActivity.this, R.drawable.processing));
		}
		
		@Override
		protected String doInBackground(Void... params) {
			String cname = book.catalog.get(position).title.replaceAll("[/\\s\\.\\\\]", ""); 
			String url = book.catalog.get(position).link;
			//先从缓存中读取内容
			String textContent = DataAccessUtil
					.loadTextContentFromCache(ContentActivity.this, book.bookid + "-" + cname + ".txt");
			if (textContent != null) {
				if (textContent.equals("")) {	//删除不包含内容的缓存文件
					File file = new File(getCacheDir(), book.bookid + "-" + cname + ".txt");
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
			
			//文本章节
			String content = psl.ncx.reader.business.ContentResolver.resolveContent(doc, book.from);
			if (useCache && !StringUtil.isBlank(content))
				DataAccessUtil.storeTextContent(ContentActivity.this, content, book.bookid + "-" + cname + ".txt");
			return content;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(result == null){
				AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this, R.style.SimpleDialogTheme);
				builder.setMessage("没有可用的网络！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
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
			setContentView(R.layout.activity_content);
			result = replacePunctuMarks(result);
			contentView = (PagedView) findViewById(R.id.view_content);
			contentView.setLongClickable(true);
			contentView.enableDragOver(true);
			contentView.setTitle(book.catalog.get(position).title);
			contentView.setText(result);
			contentView.setOnPagingListener(pagingListener);
			contentView.setOnTouchListener(listener);
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
