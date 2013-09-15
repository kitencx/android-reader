package psl.ncx.reader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import psl.ncx.reader.adapter.ChapterListAdapter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		url = intent.getStringExtra("IndexURL");
		
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
			ArrayList<String[]> chapters = new ArrayList<String[]>();
			
			URL indexUrl = null;
			try {
				indexUrl = new URL(params[0]);
			} catch (MalformedURLException e) {
				Log.e("URL", "目录页网址错误，请检查！" + params[0]);
				System.err.println("获取失败，检查目录页网址是否正确：" + params[0]);
			}
			
			Document doc = null;
			try {
				doc = Jsoup.parse(indexUrl, 30000);
			} catch (IOException e) {
				System.err.println("连接打开错误！" + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			/*目录页有两种结构，一种为div.mod_container dl dd li，每个dd包含2个li，另一种为div.mod_container ul li*/
			Elements contents = doc.select(".mod_container dl dd");
			if(!contents.isEmpty()){
				int size = contents.size();
				for(int i = 0; i < size; i += 3){
					Element dd1 = null, dd2 = null, dd3 = null;
					if(i < size) dd1 = contents.get(i);
					if((i + 1) < size) dd2 = contents.get(i + 1);
					if((i + 2) < size) dd3 = contents.get(i + 2);
					
					for(int j = 0; j < 2; j++){
						if(dd1 != null){
							Element li = dd1.child(j);
							Element a = li.select("a[href]").first();
							if(a != null){
								String chapterName = a.text();
								String chapterUrl = a.absUrl("href");
								chapters.add(new String[]{chapterName, chapterUrl});
							}
						}
						
						if(dd2 != null){
							Element li = dd2.child(j);
							Element a = li.select("a[href]").first();
							if(a != null){
								String chapterName = a.text();
								String chapterUrl = a.absUrl("href");
								chapters.add(new String[]{chapterName, chapterUrl});
							}
						}
						
						if(dd3 != null){
							Element li = dd3.child(j);
							Element a = li.select("a[href]").first();
							if(a != null){
								String chapterName = a.text();
								String chapterUrl = a.absUrl("href");
								chapters.add(new String[]{chapterName, chapterUrl});
							}
						}
					}
				}
			}else{
				contents = doc.select(".mod_container ul li");
				int size = contents.size();
				if(size != 0){
					for(int i = 0; i < size; i++){
						Element li = contents.get(i);
						Element a = li.select("a[href]").first();
						if(a != null){
							String chapterName = a.text();
							String chapterUrl = a.absUrl("href");
							chapters.add(new String[]{chapterName, chapterUrl});
						}
					}
				}
			}
			
			
			return chapters;
		}
		
		@Override
		protected void onPostExecute(final ArrayList<String[]> result) {
			if(result == null){
				//如果列表为空，载入失败，显示重试按钮
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
					intent.putExtra("Position", position);
					intent.putExtra("Chapters", result);
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			
			});
		}
		
	}
}
