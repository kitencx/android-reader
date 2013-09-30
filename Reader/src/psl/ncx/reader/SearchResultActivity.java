package psl.ncx.reader;

import java.util.ArrayList;

import psl.ncx.reader.adapter.BookListAdapter;
import psl.ncx.reader.business.BookSearch;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.constant.SupportSite;
import psl.ncx.reader.model.Book;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchResultActivity extends Activity {
	/**搜索结果显示容器*/
	private ListView listView;
	/**当前搜索的关键字*/
	private String keyword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setHomeButtonEnabled(true);
		
		Intent intent = getIntent();
		keyword = intent.getStringExtra(IntentConstant.SEARCH_KEYWORD);
		//载入数据
		new SearchBook().execute(keyword);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_result, menu);
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
	 * 查找书籍异步任务类
	 * */
	private class SearchBook extends AsyncTask<String, Integer, ArrayList<Book>>{
		/**
		 * 任务开始前显示处理动画
		 * */
		@Override
		protected void onPreExecute() {
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.startAnimation(AnimationUtils.loadAnimation(SearchResultActivity.this, R.drawable.processing));
		}
		
		/**
		 * 查询包含指定关键字的小说，书名/作者
		 * @param params 包含查询条件的数组，0:关键字	1:查询所使用编码
		 * */
		@Override
		protected ArrayList<Book> doInBackground(String... params) {
			ArrayList<Book> all = null;
			
			ArrayList<Book> from1 = BookSearch.searchByKeyword(keyword, SupportSite.WJZW);
			if(from1 != null){
				if(all == null) all = new ArrayList<Book>();
				all.addAll(from1);
			}
			
			ArrayList<Book> from2 = BookSearch.searchByKeyword(keyword, SupportSite.LJZW);
			if(from2 != null){
				if(all == null) all = new ArrayList<Book>();
				all.addAll(from2);
			}
			
			return all; 
		}
		
		@Override
		protected void onPostExecute(ArrayList<Book> result) {
			if(result == null){
				/*搜索发生意外*/
				setContentView(R.layout.button_center);
				Button retry = (Button) findViewById(R.id.button_center);
				retry.setText("连接错误，重试？");
				retry.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new SearchBook().execute(keyword);
						//移除监听
						v.setOnClickListener(null);
					}
				});
				return;
			}
			/*搜索成功*/
			setContentView(R.layout.activity_search_result);
			listView = (ListView) findViewById(R.id.listview_result);
			if(result.isEmpty()){
				TextView header = new TextView(SearchResultActivity.this);
				header.setText("很不幸，没有匹配的结果！");
				listView.addHeaderView(header, null, false);
			}
			
			BookListAdapter adapter = new BookListAdapter(SearchResultActivity.this, R.layout.listitem_booklist, result);
			
			listView.setAdapter(adapter);
			/*为每个Item添加点击响应，点击打开简介页*/
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Book book = (Book) listView.getItemAtPosition(position);
					Intent intent = new Intent(SearchResultActivity.this, SummaryActivity.class);
					intent.putExtra(IntentConstant.BOOK_INFO, book);
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			});
		}
	}
}
