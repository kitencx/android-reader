package psl.ncx.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import psl.ncx.reader.business.ChapterResolver;
import psl.ncx.reader.business.SummaryResolver;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.model.ChapterLink;
import psl.ncx.reader.util.HttpUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SummaryActivity extends Activity implements View.OnClickListener{
	private ViewHolder holder;
	/**
	 * 当前查看的Book对象
	 * */
	private Book book;
	/**
	 * 封面图片
	 * */
	private Bitmap cover;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_summary);
		//开启返回按钮
		getActionBar().setHomeButtonEnabled(true);
		//获取Book信息
		Intent intent = getIntent();
		book = (Book)intent.getSerializableExtra(IntentConstant.BOOK_INFO);
		//初始化各个组件
		initWidgets();
		
		new LoadCoverAndIntro().execute();
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
	
	/**
	 * View.OnClickListener，收藏按钮监听器
	 * */
	@Override
	public void onClick(View v) {
		v.setOnClickListener(null);
		((Button)v).setText("正在下载目录");
		new LoadChapters().execute();
	}
	
	private void initWidgets(){
		holder = new ViewHolder();
		holder.mCoverImage = (ImageView) findViewById(R.id.imageview_cover);
		holder.mTextName = (TextView) findViewById(R.id.textview_bookname);
		holder.mTextAuthor = (TextView) findViewById(R.id.textview_author);
		holder.mTextFrom = (TextView) findViewById(R.id.textview_from);
		holder.mTextTime = (TextView) findViewById(R.id.textview_updatetime);
		holder.mTextLatest = (TextView) findViewById(R.id.textview_latestchapter);
		holder.mTextSummary = (TextView) findViewById(R.id.textview_summary);
		holder.mButtonCollect = (Button) findViewById(R.id.button_collect);
		
		holder.mCoverImage.setImageResource(R.drawable.cover);
		holder.mTextName.setText(book.bookname);
		holder.mTextAuthor.setText("作者：" + book.author);
		holder.mTextFrom.setText("来源：" + book.from);
		holder.mTextTime.setText("更新日期：" + book.updateTime);
		holder.mTextLatest.setText("最新章节：" + book.latestChapter);
		holder.mTextSummary.setText("简介：\n" + book.summary);
		
		//为按钮添加事件
		holder.mButtonCollect.setOnClickListener(this);
	}
	
	/**
	 * ViewHolder，所有Widget的集合
	 * */
	class ViewHolder{
		public ImageView mCoverImage;
		public TextView mTextName;
		public TextView mTextAuthor;
		public TextView mTextFrom;
		public Button mButtonCollect;
		public TextView mTextTime;
		public TextView mTextLatest;
		public TextView mTextSummary;
	}
	
	/**
	 * 章节信息获取的异步任务，无参数，读取当前Book对象的目录页URL
	 * 如果获取失败，则当前书籍无法加入书架
	 * */
	class LoadChapters extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			FileOutputStream fos = null;
			try {
				Document doc = Jsoup.connect(book.indexURL).timeout(10000).get();
				ArrayList<ChapterLink> chapters = ChapterResolver.resolveIndex(doc, book.from);
				if (chapters != null && chapters.size() > 0){
					book.catalog = chapters;
					//保存封面
					if (cover != null) {
						String filename = System.currentTimeMillis() + ".png";
						fos = openFileOutput(filename, MODE_PRIVATE);
						if (cover.compress(Bitmap.CompressFormat.PNG, 0, fos)) book.cover = filename;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {fos.close();}
					catch (IOException e){}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (book.catalog != null) {
				long bookid = DBAccessHelper.insert(SummaryActivity.this, book);
				if (bookid == -1) {
					//目录获取成功，加入书架，如果失败，则删除封面，并提示重试
					deleteFile(book.cover);
					holder.mButtonCollect.setText(R.string.button_collect);
					holder.mButtonCollect.setOnClickListener(SummaryActivity.this);
					AlertDialog.Builder builder = new AlertDialog.Builder(SummaryActivity.this);
					builder.setMessage("添加失败！")
					.setNegativeButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).show();
				} else {
					book.bookid = String.valueOf(bookid);
					setResult(RESULT_OK);
					holder.mButtonCollect.setText("阅读");
					holder.mButtonCollect.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(SummaryActivity.this, ContentActivity.class);
							intent.putExtra(IntentConstant.BOOK_INFO, book);
							startActivity(intent);
							SummaryActivity.this.finish();
						}
					});
				}
			}
		}
		
	}
	
	/**
	 * 简介信息获取的异步任务类，无参数，读取当前Book对象的简介页URL
	 * 尝试获取封面和简介信息，如果有则更新
	 * */
	class LoadCoverAndIntro extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			Document doc = null;
			try {
				doc = Jsoup.connect(book.summaryURL).timeout(10000).get();
				/**尝试获取封面图片和简介*/
				String coverurl = SummaryResolver.resolveSummary(doc, book, book.from);
				if (coverurl != null) {
					cover = HttpUtil.loadImageFromURL(SummaryActivity.this, coverurl);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (cover != null) holder.mCoverImage.setImageBitmap(cover);
			if (book.summary != null) holder.mTextSummary.setText("简介：\n" + book.summary);
		}
	}
}
