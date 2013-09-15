package psl.ncx.reader;

import java.util.ArrayList;

import psl.ncx.reader.adapter.BookListAdapter;
import psl.ncx.reader.business.BookCollection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchResultActivity extends Activity {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		String keyword = intent.getStringExtra("keyword");
		//载入数据
		new SearchBook().execute(keyword);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_result, menu);
		return true;
	}

	private class SearchBook extends AsyncTask<String, Integer, ArrayList<String[]>>{
		@Override
		protected void onPreExecute() {
			//显示载入动画
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.startAnimation(AnimationUtils.loadAnimation(SearchResultActivity.this, R.drawable.processing));
		}
		
		/**
		 * 查询包含指定关键字的小说，书名/作者
		 * @param params 包含查询条件的数组，0:关键字	1:查询所使用编码
		 * */
		@Override
		protected ArrayList<String[]> doInBackground(String... params) {
			return new BookCollection().searchBookByKeyword(params[0], "GBK");
		}
		
		@Override
		protected void onPostExecute(ArrayList<String[]> result) {
			setContentView(R.layout.activity_search_result);
			listView = (ListView) findViewById(R.id.listview_result);
			TextView header = new TextView(SearchResultActivity.this);
			header.setText("搜索结果");
			
			BookListAdapter adapter = new BookListAdapter(SearchResultActivity.this, R.layout.listitem_booklist, result);
			
			listView.addHeaderView(header);
			listView.setAdapter(adapter);
			
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String[] book = (String[])listView.getItemAtPosition(position);
					String indexUrl = book[4];
					Intent intent = new Intent(SearchResultActivity.this, ChapterActivity.class);
					intent.putExtra("IndexURL", indexUrl);
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			});
		}
	}
}
