package psl.ncx.reader;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.service.DownloadService;
import psl.ncx.reader.util.BitmapUtil;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.util.HttpUtil;
import psl.ncx.reader.views.PagedView;
import psl.ncx.reader.views.PagedView.OnPagingListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
	private final String IMAGE_CONTENT = "image_content";
	/**
	 * 标识是否开启内容缓存
	 * */
	private boolean useCache = true;
	/**
	 * 页面组件
	 * */
	private PagedView contentView;
	private ScrollView scrollView;
	private ActionBar mActionBar;
	/**
	 * 图片章节的内容
	 * */
	private Bitmap bitmap;
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
	 * 手势检测，图片章节和文本章节的手势检测是不同的
	 * */
	private GestureDetector imgGesture;
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
		
		imgGesture = new GestureDetector(this, new FlipOverGestureForImage());
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
			//新任务启动，释放当前的图片内容
			if(bitmap != null){
				bitmap.recycle();
				bitmap = null;
			}
			if (contentView != null) contentView.abortAnimation();
			//显示载入动画
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.startAnimation(AnimationUtils.loadAnimation(ContentActivity.this, R.drawable.processing));
		}
		
		@Override
		protected String doInBackground(Void... params) {
			if(book.catalog == null){
				position = book.bookmark;
				book.catalog = DBAccessHelper.queryChaptersById(ContentActivity.this, book.bookid);
			}
			String cname = book.catalog.get(position).title.replaceAll("[/\\s\\.\\\\]", ""); 
			String url = book.catalog.get(position).link;
			
			//先从缓存中读取内容
			String textContent = DataAccessUtil
					.loadTextContentFromCache(ContentActivity.this, book.bookid + "-" + cname + ".txt");
			if (textContent != null) {
				System.out.println("缓存载入");
				return textContent;
			}
			bitmap = DataAccessUtil
					.loadImageContentFromCache(ContentActivity.this, book.bookid + "-" + cname + ".png");
			if (bitmap != null) {
				return IMAGE_CONTENT;
			}
			
			if (!HttpUtil.hasAvaliableNetwork(ContentActivity.this)) return null;
			//从网络获取内容
			Document doc = null;
			try {
				doc = Jsoup.connect(url).timeout(10000).get();
			} catch (IOException e) {
				return CONNECT_FAILED;
			}
			
			//先检查是否是图片章节
			Elements contentNode = doc.select(".imagecontent");
			if(!contentNode.isEmpty()){
				Bitmap[] bitmaps = new Bitmap[contentNode.size()];
				for(int i = 0; i < bitmaps.length; i++){
					bitmaps[i] = HttpUtil.loadImageFromURL(ContentActivity.this, contentNode.get(i).absUrl("src"));
				}
				if(bitmaps.length > 1){
					//合并一个章节的所有图片
					try{
						bitmap = BitmapUtil.combineBitmaps(bitmaps);
					} catch (OutOfMemoryError e){
						e.printStackTrace();
						return null;
					}
				}else{
					bitmap = bitmaps[0];
				}
				if(bitmap == null) return CONNECT_FAILED;
				
				if(useCache) DataAccessUtil.storeImageContent(ContentActivity.this, 
						bitmap, book.bookid + "-" + cname + ".png");
				return IMAGE_CONTENT;
			}
			//文本章节
			String content = psl.ncx.reader.business.ContentResolver.resolveContent(doc, book.from);
			if (useCache)
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
			}else if(IMAGE_CONTENT.equals(result)){
				showImageContent(result);
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
		 * 显示图片章节内容
		 * */
		private void showImageContent(String result){
			setContentView(R.layout.activity_content_image);
			ImageView imageView = (ImageView) findViewById(R.id.imageview_content);
			scrollView = (ScrollView)imageView.getParent();

			imageView.setImageBitmap(bitmap);
			//如果给ImageView添加事件响应的话，因为ACTION_MOVE事件会被拦截，导致无法检测手势
			scrollView.setOnTouchListener(listener);
		}
		
		/**
		 * 显示文本章节内容
		 * */
		private void showTextContent(String result){
			setContentView(R.layout.activity_content);
			result = replacePunctuMarks(result);
			System.out.println(result.length());
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
				return imgGesture.onTouchEvent(event);
			} else if (v instanceof PagedView) {
				return textGesture.onTouchEvent(event);
			}
			return false;
		}
	}
	
	/**
	 * 手势检测，用于图片章节
	 * */
	private class FlipOverGestureForImage extends SimpleOnGestureListener{
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			//点击显示ActionBar
			if(mActionBar.isShowing()) mActionBar.hide();
			else mActionBar.show();
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(e2.getX() - e1.getX() > 100){
				//先判断是否有当前章节没有显示完毕
				if(position - 1 > 0){
					position--;
					new LoadContent().execute();
					scrollView.setOnTouchListener(null);
				}
			}else if(e1.getX() - e2.getX() > 100){
				if(position + 1 < book.catalog.size()){
					position++;
					new LoadContent().execute();
					scrollView.setOnTouchListener(null);
				}
			}
			
			//该事件监听会被注册到ScrollView上，为防止拦截滚动事件，永远返回false;
			return false;
		}
	}
}
