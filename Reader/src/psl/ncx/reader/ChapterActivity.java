package psl.ncx.reader;

import java.util.ArrayList;
import psl.ncx.reader.adapter.ChapterListAdapter;
import psl.ncx.reader.constant.IntentConstant;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ChapterActivity extends Activity {
	private ListView listView;
	private ArrayList<String[]> catalog;
	private int position;
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		position = intent.getIntExtra(IntentConstant.OPEN_INDEX, 0);
		catalog = (ArrayList<String[]>) intent.getSerializableExtra(IntentConstant.CHAPTERS);
		
		setContentView(R.layout.activity_chapter);
		listView = (ListView) findViewById(R.id.listview_chapter);
		
		ChapterListAdapter adapter = new ChapterListAdapter(ChapterActivity.this, 
				R.layout.listitem_chapter, catalog);
		//设置被选中的条目
//		adapter.setSelectedPosition(position);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra(IntentConstant.OPEN_INDEX, position);
				setResult(Activity.RESULT_OK, intent);
				ChapterActivity.this.finish();
				overridePendingTransition(0, R.anim.out_to_top);
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//让当前的章节居中
		listView.setSelection(position - 7);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_close:
			setResult(Activity.RESULT_CANCELED);
			this.finish();
			overridePendingTransition(0, R.anim.out_to_top);
			break;
		}
		
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chapter, menu);
		return true;
	}
}
