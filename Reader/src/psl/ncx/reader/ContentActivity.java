package psl.ncx.reader;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import psl.ncx.reader.util.HttpRequestHelper;
import psl.ncx.reader.util.PageMaker;
import psl.ncx.reader.views.PagedView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

public class ContentActivity extends Activity {
	private final String CONNECT_FAILED = "connect_failed";
	private final String RESOLVE_FAILED = "resolve_failed";
	private final String IMAGE_CONTENT = "image_content";
	private PagedView contentView;
	private ScrollView scrollView;
	private Bitmap bitmap;
	private ArrayList<String[]> chapters;
	private int position;
	private PageMaker maker;
	private PageDownListener listener;
	private Point screenSize;
	private GestureDetector textGesture;
	private GestureDetector imgGesture;
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//隐藏标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//满屏显示
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		
		textGesture = new GestureDetector(this, new FlipOverGestureForText());
		imgGesture = new GestureDetector(this, new FlipOverGestureForImage());
		listener = new PageDownListener();
		
		Intent intent = getIntent();
		position = intent.getIntExtra("Position", -1);
		chapters = (ArrayList<String[]>)intent.getSerializableExtra("Chapters");
		
		new LoadContent().execute(chapters.get(position)[1]);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.content, menu);
		return true;
	}
	
	/**
	 * 异步任务，获取章节内容
	 * */
	private class LoadContent extends AsyncTask<String, Integer, String>{

		@Override
		protected void onPreExecute() {
			//新任务启动，释放当前的图片内容
			if(bitmap != null){
				bitmap.recycle();
				bitmap = null;
			}
			//显示载入动画
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.startAnimation(AnimationUtils.loadAnimation(ContentActivity.this, R.drawable.processing));
		}
		
		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			Document doc = null;
			try {
				doc = Jsoup.connect(url).get();
			} catch (IOException e) {
				Log.e("Content", "载入章节内容失败：" + e.getMessage());
				return CONNECT_FAILED;
			}
			
			Elements content = doc.select(".novel_content");
			if(content.isEmpty()){
				return RESOLVE_FAILED;
			}else{
				//检查是否图片章节
				Elements image = content.select(".divimage");
				if(image.isEmpty()){
					//不是图片
					return content.first().html();
				}
				//返回图片链接地址
				try {
					Bitmap src = HttpRequestHelper.loadImageFromURL(image.first().child(0).absUrl("src"));
					//同比缩放图片
					Matrix matrix = new Matrix();
					float scale = ((float)screenSize.x) / src.getWidth();
					matrix.postScale(scale, scale);
					bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
					//释放原图片
					src.recycle();
					src = null;
					
					return IMAGE_CONTENT;
				} catch (IOException e) {
					e.printStackTrace();
					return CONNECT_FAILED;
				}
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(CONNECT_FAILED.equals(result)){
				showConnectFailed(result);
			}else if(RESOLVE_FAILED.equals(result)){
				//解析失败
			}else if(IMAGE_CONTENT.equals(result)){
				showImageContent(result);
			}else{
				showTextContent(result);
			}
		}
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
				new LoadContent().execute(chapters.get(position)[1]);
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
		contentView = (PagedView) findViewById(R.id.view_content);
		contentView.setLongClickable(true);
		
		maker = new PageMaker(Html.fromHtml(result).toString(), screenSize.x, screenSize.y, contentView.getPaint());
		maker.setPadding(contentView.getPaddingLeft(), contentView.getPaddingTop(), 
				contentView.getPaddingRight(), contentView.getPaddingBottom());
		
		//新章节载入，重置分页工具类状态
		maker.reset();
		//获取第一页，并显示
		contentView.setTitle(chapters.get(position)[0]);
		contentView.setContent(maker.nextPage());
		//添加翻页事件监听
		contentView.setOnTouchListener(listener);
	}
	
	/**
	 * 翻页事件监听
	 * */
	private class PageDownListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(v instanceof ScrollView){
				return imgGesture.onTouchEvent(event);
			}else if(v instanceof PagedView){
				return textGesture.onTouchEvent(event);
			}
			return false;
		}
	}
	
	/**
	 * 手势检测，用于文本章节
	 * */
	private class FlipOverGestureForText extends SimpleOnGestureListener{
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(e2.getX() - e1.getX() > 100){
				/*向右滑动，后退翻页*/
				if(maker.prePage()){
					ArrayList<String> page = maker.nextPage();
					contentView.setContent(page);
				}else{
					if(position - 1 >= 0){
						new LoadContent().execute(chapters.get(--position)[1]);
						//载入新章节任务启动后，移除点击事件，防止连点造成的连续翻页
						contentView.setOnTouchListener(null);
					}
				}
			}else if(e1.getX() - e2.getX() > 100){
				/*向左滑动，前进翻页*/
				ArrayList<String> page = maker.nextPage();
				if(page != null)	contentView.setContent(page);
				else{
					if(position + 1 < chapters.size()){
						new LoadContent().execute(chapters.get(++position)[1]);
						//载入新章节任务启动后，移除点击事件，防止连点造成的连续翻页
						contentView.setOnTouchListener(null);
					}
				}
			}
			
			//无论何种情况，都消耗该事件
			return true;
		}
	}
	
	/**
	 * 手势检测，用于图片章节
	 * */
	private class FlipOverGestureForImage extends SimpleOnGestureListener{
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(e2.getX() - e1.getX() > 100){
				if(position - 1 > 0){
					new LoadContent().execute(chapters.get(--position)[1]);
					scrollView.setOnTouchListener(null);
				}
			}else if(e1.getX() - e2.getX() > 100){
				if(position + 1 < chapters.size()){
					new LoadContent().execute(chapters.get(++position)[1]);
					scrollView.setOnTouchListener(null);
				}
			}
			
			//该事件监听会被注册到ScrollView上，为防止拦截滚动事件，永远返回false;
			return false;
		}
	}
}
