package psl.ncx.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import psl.ncx.reader.adapter.ChapterListAdapter;
import psl.ncx.reader.constant.IntentConstant;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ChapterActivity extends Activity {
	private ListView listView;
	private String url;
	private String bookName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		bookName = intent.getStringExtra(IntentConstant.OPEN_BOOKNAME);
		
		new ListChapters().execute(url);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chapter, menu);
		return true;
	}

	private class ListChapters extends AsyncTask<String, Integer, ArrayList<String[]>>{

		@Override
		protected void onPreExecute() {
			setContentView(R.layout.loading);
			ImageView processing = (ImageView) findViewById(R.id.imageview_loading);
			processing.startAnimation(AnimationUtils.loadAnimation(ChapterActivity.this, R.drawable.processing));
		}
		
		@Override
		protected ArrayList<String[]> doInBackground(String... params) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(
						new FileReader(ChapterActivity.this.getFilesDir() + File.separator + bookName));
				ArrayList<String[]> chapters = new ArrayList<String[]>();
				String data;
				while((data = br.readLine()) != null){
					String[] entry = data.split("=");
					if(entry[0].matches("\\d+")){
						int p = entry[1].lastIndexOf(",");
						String title = entry[1].substring(0, p);
						String curl = entry[1].substring(p + 1);
						chapters.add(new String[]{title, curl});
					}
				}
				return chapters;
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(br != null)
					try {br.close();} 
					catch (IOException e) {}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(final ArrayList<String[]> result) {
			if(result == null || result.isEmpty()){
				//如果列表为空，尝试网络载入
				setContentView(R.layout.button_center);
				Button retry = (Button) findViewById(R.id.button_center);
				retry.setText("载入目录失败，重试？");
				retry.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						new ListChapters().execute(url);
					}
				});
				return;
			}
			for(String[] str:result){
				for(int i=0; i<str.length;i++){
					System.out.print(str[i] + ",");
				}
				System.out.println();
			}
			
			setContentView(R.layout.activity_chapter);
			listView = (ListView) findViewById(R.id.listview_chapter);
			
			ChapterListAdapter adapter = new ChapterListAdapter(ChapterActivity.this, 
					android.R.layout.simple_list_item_1, result);
			listView.setAdapter(adapter);
			
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(ChapterActivity.this, ContentActivity.class);
					intent.putExtra(IntentConstant.OPEN_INDEX, position);
					intent.putExtra(IntentConstant.CHAPTERS, result);
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			
			});
		}
		
	}
}
