package psl.ncx.reader;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import psl.ncx.reader.async.LoadChapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.util.DataAccessHelper;
import psl.ncx.reader.util.URLValidator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SummaryActivity extends Activity {
	private String indexURL;
	private String summaryURL;
	private Button mCollect;
	private Book book;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		indexURL = intent.getStringExtra(IntentConstant.INDEX_URL);
		summaryURL = concatSummaryURL(indexURL);
		book = new Book();
		book.indexURL = indexURL;
		
		new LoadSummary().execute(summaryURL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.summary, menu);
		return true;
	}
	
	/**
	 * 根据提供的目录页URL，拼接一个简介页的URL
	 * */
	private String concatSummaryURL(String indexURL){
		String noprotocol = indexURL.substring(7);		//去除网址中的协议字符串
		String[] s = noprotocol.split("/");						//按"/"分割，取得网址目录结构
		String summaryURL = "http://" + s[0] + "/jieshaoinfo/" + s[2] + "/" + s[3] + ".htm";
		return summaryURL;
	}

	private class LoadSummary extends AsyncTask<String, Integer, Book>{

		@Override
		protected void onPreExecute() {
			//显示载入动画
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.startAnimation(AnimationUtils.loadAnimation(SummaryActivity.this, R.drawable.processing));
		}
		
		@Override
		protected Book doInBackground(String... params) {
			String url = params[0];
			if(!URLValidator.validate(url, URLValidator.URL_SUMMARY)) return null;
			
			Document doc = null;
			try {
				doc = Jsoup.connect(url).get();
				//解析
				resolveSummaryPage(doc, book);
				return book;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Book result) {
			if(result == null){
				showConnectFailed(result);
				return;
			}
			//成功返回数据
			showSummary(result);
		}
		
		/**
		 * 解析简介页，将解析到的数据保存如outBook
		 * */
		private void resolveSummaryPage(Document doc, Book outBook){
			/*****************书名***************************************/
			Elements infos = doc.select(".biaoti");
			outBook.bookName = infos.text();
			/*****************作者、更新日期********************/
			infos = doc.select(".info");
			for(int i = 0; i < infos.size(); i++){
				Element info = infos.get(i);
				String str = info.text();
				//替换掉所有全角冒号和空格
				str = str.replaceAll("：", ":");
				str = str.replaceAll(" ", "");
				//以"|"分割字符串，Android比较特别，"|"也需要转义
				String[] arrs = str.split("\\|");
				//查找具体信息
				for(String s : arrs){
					String[] kv = s.split(":");
					if(kv.length == 2){
						if("作者".equals(kv[0])) outBook.author = kv[1];
						else if("更新日期".equals(kv[0])) outBook.updateTime = kv[1];
					}
				}
			}
			/****************简介***********************************/
			infos = doc.select(".intro");
			outBook.summary = infos.text();
			/****************最新章节******************************/
			infos = doc.select(".newupdate ul li a");
			outBook.latestChapter = infos.text();
		}
		
		/**
		 * 连接失败，显示重试
		 * */
		private void showConnectFailed(Book result){
			setContentView(R.layout.button_center);
			Button retry = (Button) findViewById(R.id.button_center);
			retry.setText("连接失败，重试？");
			retry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new LoadSummary().execute(summaryURL);
					v.setOnClickListener(null);
				}
			});
		}
		
		/**
		 * 显示简介
		 * */
		private void showSummary(Book result){
			setContentView(R.layout.activity_summary);
			TextView textBookName = (TextView) findViewById(R.id.textview_bookname);
			TextView textAuthor = (TextView)findViewById(R.id.textview_author);
			mCollect = (Button)findViewById(R.id.button_collect);
			TextView textUpdateTime = (TextView)findViewById(R.id.textview_updatetime);
			TextView textLatestChapter = (TextView)findViewById(R.id.textview_latestchapter);
			TextView textSummary = (TextView)findViewById(R.id.textview_summary);
			
			textBookName.setText(result.bookName);
			textAuthor.setText(result.author);
			textUpdateTime.setText(result.updateTime);
			textLatestChapter.setText(result.latestChapter);
			textSummary.setText(result.summary);
			
			mCollect.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(URLValidator.validate(indexURL, URLValidator.URL_INDEX)){
						new ListChapter().execute(indexURL);
						mCollect.setOnClickListener(null);
						mCollect.setText("正在下载目录");
					}
				}
			});
		}
		
		private class ListChapter extends AsyncTask<String, Void, ArrayList<String[]>>{

			@Override
			protected ArrayList<String[]> doInBackground(String... params) {
				ArrayList<String[]> chapters = new LoadChapter().doInBackground(params[0]);
				if(chapters == null) return null;
				//保存书籍
				book.catalog = chapters;
				if(!DataAccessHelper.isExisted(SummaryActivity.this, book.bookName)){
					DataAccessHelper.store(SummaryActivity.this, book);
				}
				return chapters;
			}
			
			@Override
			protected void onPostExecute(final ArrayList<String[]> result) {
				mCollect.setText("阅读");
				mCollect.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//点击开始阅读
						Intent intent = new Intent(SummaryActivity.this, ContentActivity.class);
						intent.putExtra(IntentConstant.OPEN_INDEX, 0);
						intent.putExtra(IntentConstant.CHAPTERS, result);
						SummaryActivity.this.finish();
						startActivity(intent);
					}
				});
			}
		}
	}
}
