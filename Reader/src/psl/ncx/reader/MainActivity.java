package psl.ncx.reader;

import psl.ncx.reader.constant.IntentConstant;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity {
	private SearchView mSearchView;
	private MenuItem mSearchMenu;
	private ActionBar mActionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//隐藏ActionBar
		mActionBar = getActionBar();
		mActionBar.hide();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mActionBar.hide();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//创建搜索栏
		getMenuInflater().inflate(R.menu.main, menu);

		mSearchMenu = menu.findItem(R.id.searchitem);
		mSearchView = (SearchView) mSearchMenu.getActionView();
		mSearchView.setQueryHint("关键字：书名/作者");
		//添加搜索响应
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				//隐藏搜索栏，并转跳到查询页
				Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
				intent.putExtra(IntentConstant.SEARCH_KEYWORD, query);
				startActivity(intent);
				
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		
		return true;
	}
}
