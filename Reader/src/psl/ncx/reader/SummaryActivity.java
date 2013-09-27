package psl.ncx.reader;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import psl.ncx.reader.async.LoadChapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.util.HttpUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SummaryActivity extends Activity {
	private Button mCollect;
	private Book book;
	private String coverurl;
	private Bitmap cover;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setHomeButtonEnabled(true);
		
		Intent intent = getIntent();
		book = (Book)intent.getSerializableExtra(IntentConstant.BOOK_INFO);
		
		new LoadSummary().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.summary, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			this.finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
	
	private class LoadSummary extends AsyncTask<Void, Void, Book>{

		@Override
		protected void onPreExecute() {
			//显示载入动画
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.startAnimation(AnimationUtils.loadAnimation(SummaryActivity.this, R.drawable.processing));
		}
		
		@Override
		protected Book doInBackground(Void... params) {
			Document doc = null;
			try {
				doc = Jsoup.connect(book.summaryURL).get();
				resolveDocument(doc, book);
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
		 * @param doc 解析的页面
		 * @param outBook 解析结果保存进在对象
		 * */
		private void resolveDocument(Document doc, Book outBook){
			outBook.from = "六九中文";
			Elements infos = null;
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
						if("更新日期".equals(kv[0])) outBook.updateTime = kv[1];
					}
				}
			}
			/****************简介***********************************/
			infos = doc.select(".intro");
			outBook.summary = infos.text();
			/****************封面**********************************/
			infos = doc.select(".coverleft a img");
			if(!infos.isEmpty()){
				coverurl = infos.first().absUrl("src");
				cover = HttpUtil.loadImageFromURL(SummaryActivity.this, coverurl);
			}
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
					new LoadSummary().execute();
					v.setOnClickListener(null);
				}
			});
		}
		
		/**
		 * 显示简介
		 * */
		private void showSummary(Book result){
			setContentView(R.layout.activity_summary);
			ImageView imageCover = (ImageView)findViewById(R.id.imageview_cover);
			TextView textBookName = (TextView) findViewById(R.id.textview_bookname);
			TextView textAuthor = (TextView)findViewById(R.id.textview_author);
			mCollect = (Button)findViewById(R.id.button_collect);
			TextView textUpdateTime = (TextView)findViewById(R.id.textview_updatetime);
			TextView textLatestChapter = (TextView)findViewById(R.id.textview_latestchapter);
			TextView textSummary = (TextView)findViewById(R.id.textview_summary);
			
			imageCover.setImageBitmap(cover);
			textBookName.setText(result.bookname);
			textAuthor.setText(result.author);
			textUpdateTime.setText("更新日期：" + result.updateTime);
			textLatestChapter.setText("最新章节：" + result.latestChapter);
			textSummary.setText("简介：\n" + result.summary);
			
			mCollect.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new ListChapter().execute();
					mCollect.setOnClickListener(null);
					mCollect.setText("正在下载目录");
				}
			});
		}
		
		private class ListChapter extends AsyncTask<Void, Void, Long>{

			@Override
			protected Long doInBackground(Void... params) {
				ArrayList<String[]> chapters = new LoadChapter().doInBackground(book.indexURL);
				String filename = HttpUtil.storeImageFromURL(SummaryActivity.this, coverurl);
				//章节获取失败，则都不保存
				if(chapters == null) return -1L;
				
				book.catalog = chapters;
				book.cover = filename;
				
				return DBAccessHelper.insert(SummaryActivity.this, book);
			}
			
			@Override
			protected void onPostExecute(Long result) {
				if(result == -1){
					//保存失败！
					AlertDialog.Builder builder = new Builder(SummaryActivity.this, R.style.SimpleDialogTheme);
					builder.setMessage("下载目录失败！是否重试？")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setPositiveButton("重试", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new ListChapter().execute();
							dialog.dismiss();
						}
					}).show();
					return;
				}
				
				mCollect.setText("阅读");
				mCollect.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//点击开始阅读
						Intent intent = new Intent(SummaryActivity.this, ContentActivity.class);
						intent.putExtra(IntentConstant.BOOK_INFO, book);
						SummaryActivity.this.finish();
						startActivity(intent);
						overridePendingTransition(R.anim.in_from_top, R.anim.out_to_bottom);
					}
				});
			}
		}
	}
}
