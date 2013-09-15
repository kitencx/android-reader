package psl.ncx.reader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	private EditText mSearchBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//获取各个组件
		mSearchBox = (EditText) findViewById(R.id.text_search);
		mSearchBox.setText("首席御医");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void search(View view){
		Intent intent = new Intent(this, SearchResultActivity.class);
		String keyword = mSearchBox.getText().toString();
		intent.putExtra("keyword", keyword);
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
}
