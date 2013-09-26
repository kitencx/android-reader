package psl.ncx.reader.async;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class LoadChapter{
	
	public ArrayList<String[]> doInBackground(String url){
		ArrayList<String[]> chapters = new ArrayList<String[]>();
		
		URL indexUrl = null;
		try {
			indexUrl = new URL(url);
		} catch (MalformedURLException e) {
			Log.e("URL", "目录页网址错误，请检查！" + url);
		}
		
		Document doc = null;
		try {
			doc = Jsoup.parse(indexUrl, 15000);
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
	
}
