package psl.ncx.reader.business;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import psl.ncx.reader.util.HttpRequestHelper;

public class BookCollection {
	
	/**
	 * 搜索小说信息
	 * @param keyword 搜索的关键字
	 * @param charset POST请求过程中参数的编码类型
	 * @return 符合查询条件的小说信息集合，每个String[]代表一本小说，包含有5个信息：
	 * <ol>
	 * 	<li>小说封面</li>
	 * 	<li>书名</li>
	 * 	<li>作者</li>
	 * 	<li>最新章节</li>
	 * 	<li>目录页URL</li>
	 * </ol>
	 * */
	public ArrayList<String[]> searchBookByKeyword(String keyword, String charset){
		URL url = null;
		HttpURLConnection conn = null;
		try {
			url = new URL("http://www.69zw.com/modules/article/search.php");
		}catch (MalformedURLException e){
			Log.e("URL", "URL格式无法解析，请检查！");
			System.err.println("URL格式无法解析，请检查！");
			e.printStackTrace();
		}
		
		try{
			conn = (HttpURLConnection) url.openConnection();
			//配置POST默认参数
			HttpRequestHelper.configDefaultConnection(conn, "POST");

			Map<String, String> values = new TreeMap<String, String>();
			values.put("searchtype", "articlename");
			values.put("searchkey", keyword);
			values.put("Submit", " 搜 索 ");
			//设置请求正文
			HttpRequestHelper.setPayLoad(conn, values, charset);

			InputStream is = null;
			try{
				is = conn.getInputStream();
				
				//读取流
				Document doc = Jsoup.parse(is, charset, "http://www.69zw.com");
				
				return resolvingDocument(doc);
				
			} finally{
				if(is != null) is.close();
			}
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("编码类型错误，不支持编码类型：" + e.getMessage());
			e.printStackTrace();
		} catch (ProtocolException e) {
			System.out.println("协议错误，不支持请求方式：" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("连接错误，请检查网址！");
			e.printStackTrace();
		} finally{
			if(conn != null) conn.disconnect();
		}

		return new ArrayList<String[]>();
	}
	
	public ArrayList<String[]> resolvingDocument(Document doc){
		ArrayList<String[]> bookList = new ArrayList<String[]>();
		
		Elements rows = doc.select("table.grid tr");
		if(rows.size() > 1){
			for(int i = 1; i < rows.size(); i++){
				Element row = rows.eq(i).first();
				String bookName = row.child(0).text();
				String author = row.child(2).text();
				String latestChapter = row.child(1).text();
				String indexUrl = row.child(1).child(0).absUrl("href");
				bookList.add(new String[]{String.valueOf(i), bookName, author, latestChapter, indexUrl});
			}
		}else{
			Element startRead = doc.select(".btopt").first();
			if(startRead != null){
				String[] contents = doc.title().split(" - ");
				String bookName = contents[0].trim();
				String author = contents[1].trim();
				String latestChapter = doc.select(".newupdate ul li a").first().text();
				String indexUrl = startRead.child(0).absUrl("href");
				bookList.add(new String[]{"1", bookName, author, latestChapter, indexUrl});
			}
		}
		
		
		return bookList;
	}
	
	public static void main(String args[]){
		new BookCollection().searchBookByKeyword("首席御医", "GBK");
	}
}
