package psl.ncx.reader;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import psl.ncx.reader.util.PageMaker;
import psl.ncx.reader.views.PagedView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class ContentActivity extends Activity {
	private ArrayList<String[]> chapters;
	private int position;
	private PagedView contentView;
	private PageMaker maker;
	private PageDownListener listener;
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//隐藏标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//满屏显示
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		contentView = new PagedView(this);
		contentView.setPadding(15, 20, 15, 20);
		
		setContentView(contentView);
		
		Point screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		maker = new PageMaker(null, screenSize.x, screenSize.y, contentView.getPaint());
		maker.setPadding(15, 20, 15, 20);
		
		listener = new PageDownListener();
		
		Intent intent = getIntent();
		position = intent.getIntExtra("Position", -1);
		chapters = (ArrayList<String[]>)intent.getSerializableExtra("Chapters");
		String url = chapters.get(position)[1];
		
		new LoadContent().execute(url);
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
		protected String doInBackground(String... params) {
			String url = params[0];
			Document doc = null;
			try {
				doc = Jsoup.connect(url).get();
			} catch (IOException e) {
				Log.e("Content", "载入章节内容失败：" + e.getMessage());
				return "连接失败！";
			}
			
			Elements content = doc.select(".novel_content");
			if(content.isEmpty()){
				return "无法解析内容！";
			}else{
				return content.first().html();
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			String content = Html.fromHtml(result).toString();
			//新章节载入，重置分页工具类状态
			maker.reset();
			//设置需要分页的内容
			maker.setText(content);
			//获取第一页，并显示
			contentView.setContent(maker.nextPage());
			//添加翻页事件监听
			contentView.setOnTouchListener(listener);
		}
	}
	
	private class PageDownListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				if(event.getX() > 280){
					//点击点位于屏幕右侧，往后翻页
					ArrayList<String> page = maker.nextPage();
					if(page != null)
						//后一页有内容，显示
						contentView.setContent(page);
					else
						//后一页无内容，判断是否有下一章节，有，则载入下一章节，否则不响应
						if(position + 1 < chapters.size()){
							new LoadContent().execute(chapters.get(++position)[1]);
							//载入新章节任务启动后，移除点击事件，防止连点造成的连续翻页
							contentView.setOnTouchListener(null);
						}
				}else if(event.getX() < 200){
					//点击点位于屏幕左侧，往前翻页
					if(maker.prePage()){
						//如果有前一页，获取并显示
						ArrayList<String> page = maker.nextPage();
						contentView.setContent(page);
					}else{
						//没有前一页，判断是否有前一个章节，有，则载入，否则不响应
						if(position - 1 >= 0){
							new LoadContent().execute(chapters.get(--position)[1]);
							//载入新章节任务启动后，移除点击事件，防止连点造成的连续翻页
							contentView.setOnTouchListener(null);
						}
					}
				}
			}
			return false;
		}
	}
}
