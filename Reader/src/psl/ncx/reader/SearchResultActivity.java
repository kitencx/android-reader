package psl.ncx.reader;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

public class SearchResultActivity extends Activity {
	/**搜索结果显示容器*/
	private ListView mLvResult;
	/**重试按钮*/
	private Button mBtnRetry;
	/**加载指示*/
	private ProgressBar mLoadIndicator;
	/**结果数据Adapter*/
	private BookListAdapter mAdapter;
	private List<Book> mData;
	/**当前搜索的关键字*/
	private String keyword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_searchresult);
		
		getActionBar().setHomeButtonEnabled(true);
		
		Intent intent = getIntent();
		keyword = intent.getStringExtra(IntentConstant.SEARCH_KEYWORD);
		
		mLvResult = (ListView) findViewById(R.id.listview_result);
		mBtnRetry = (Button) findViewById(R.id.btn_retry);
		mLoadIndicator = (ProgressBar) findViewById(R.id.pb_loadindicator);
		
		mData = new ArrayList<Book>();
		mAdapter = new BookListAdapter(this, R.layout.listitem_booklist, mData);
		mLvResult.setAdapter(mAdapter);
		//添加空显示
		mLvResult.setEmptyView(findViewById(R.id.tv_empty));
		mLvResult.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Book book = (Book) mLvResult.getItemAtPosition(position);
				Intent intent = new Intent(SearchResultActivity.this, SummaryActivity.class);
				intent.putExtra(IntentConstant.BOOK_INFO, book);
				intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
		
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
			mAdapter.setLoading(true);
			mLvResult.setVisibility(View.GONE);
			mBtnRetry.setVisibility(View.GONE);
			mLoadIndicator.setVisibility(View.VISIBLE);
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
			mLoadIndicator.setVisibility(View.GONE);
			if(result == null){
				/*搜索发生意外*/
				mLvResult.setVisibility(View.GONE);
				mBtnRetry.setVisibility(View.VISIBLE);
				mBtnRetry.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new SearchBook().execute(keyword);
						//移除监听
						v.setOnClickListener(null);
					}
				});
			} else {
				/*搜索成功*/
				mAdapter.setLoading(false);
				mBtnRetry.setVisibility(View.GONE);
				mLvResult.setVisibility(View.VISIBLE);
				//清楚原有内容
				mData.clear();
				mData.addAll(result);
				mAdapter.notifyDataSetChanged();
			}
		}
	}
}
