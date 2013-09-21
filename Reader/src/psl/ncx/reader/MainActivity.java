package psl.ncx.reader;

import java.util.ArrayList;

import psl.ncx.reader.adapter.BookShelfAdapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.util.DataAccessHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity {
	private SearchView mSearchView;
	private MenuItem mSearchMenu;
	private ActionBar mActionBar;
	private ImageButton mBookShelf;
	private ImageButton mSearch;
	private GridView mGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//隐藏ActionBar
		mActionBar = getActionBar();
		mActionBar.hide();
		
		mBookShelf = (ImageButton) findViewById(R.id.bookshelf);
		mBookShelf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mActionBar.isShowing()) mActionBar.hide();
			}
		});
		
		mSearch = (ImageButton) findViewById(R.id.search);
		mSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mActionBar.isShowing()) mActionBar.hide();
				else mActionBar.show();
			}
		});
		
		mGrid = (GridView) findViewById(R.id.gridview);
		mGrid.setNumColumns(3);
		/**点下书架隐藏搜索栏*/
		mGrid.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					if(mActionBar.isShowing()) mActionBar.hide();
					break;
				}
				return false;
			}
		});
		/**点击显示指定书籍目录*/
		mGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String bookname = ((String[]) parent.getItemAtPosition(position))[1];
				Intent intent = new Intent(MainActivity.this, ContentActivity.class);
				intent.putExtra(IntentConstant.OPEN_INDEX, 0);
				intent.putExtra(IntentConstant.OPEN_BOOKNAME, bookname);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
			}
		});
		/**长按删除指定书籍*/
		mGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				String bookname = ((String[]) parent.getItemAtPosition(position))[1];
				deleteFile(bookname);
				return true;
			}
		});
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//检索目录，查看是否有新增书籍
		String[] books = DataAccessHelper.listBooksName(this);
		ArrayList<String[]> data = new ArrayList<String[]>();
		for(int i = 0; i < books.length; i++){
			data.add(new String[]{String.valueOf(R.drawable.cover), books[i]});
		}
		mGrid.setAdapter(new BookShelfAdapter(this, data));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//创建搜索栏
		getMenuInflater().inflate(R.menu.main, menu);

		mSearchMenu = menu.findItem(R.id.searchitem);
		mSearchView = (SearchView) mSearchMenu.getActionView();
		//添加搜索响应
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				//隐藏搜索栏，并转跳到查询页
				mActionBar.hide();
				Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
				intent.putExtra(IntentConstant.SEARCH_KEYWORD, query);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				
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
