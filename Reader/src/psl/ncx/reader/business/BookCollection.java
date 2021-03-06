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

import psl.ncx.reader.model.Book;
import psl.ncx.reader.util.HttpRequestHelper;

public class BookCollection {
	private String error;
	
	/**
	 * 搜索小说信息
	 * @param keyword 搜索的关键字
	 * @param charset POST请求过程中参数的编码类型
	 * @return 符合查询条件的小说信息集合
	 * */
	public ArrayList<Book> searchBookByKeyword(String keyword, String charset){
		URL url = null;
		HttpURLConnection conn = null;
		try {
			url = new URL("http://www.69zw.com/modules/article/search.php");
		}catch (MalformedURLException e){
			Log.e("URL", "URL格式无法解析，请检查！");
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
				if(conn.getResponseCode() == 200){
					//如果请求正常返回，则解析返回
					Document doc = Jsoup.parse(is, charset, "http://www.69zw.com");
					return resolvingDocument(doc);
				}
			} finally{
				if(is != null) is.close();
			}
		} catch (UnsupportedEncodingException e) {
			System.err.println("编码类型错误，不支持编码类型：" + e.getMessage());
		} catch (ProtocolException e) {
			System.err.println("协议错误，不支持请求方式：" + e.getMessage());
		} catch (IOException e) {
			System.out.println("连接错误，请检查网址！");
			error = e.getMessage();
		} finally{
			if(conn != null) conn.disconnect();
		}

		return null;
	}
	/**
	 * 解析当前网页，提取出书籍信息
	 * <p>搜索结果网页有两种情况：</p>
	 * <ul>
	 * 	<li>搜索结果有0本或者超过1本书籍符合条件，则会转到search.php页面，页面包含一个表格，列出匹配的书籍内容</li>
	 * 	<li>搜索结果有且只有1本书籍符合条件，则会转跳到该书籍的简介页面</li>
	 * </ul>
	 * @param doc 当前解析的网页
	 * @return 搜索结果，书籍的列表
	 * */
	public ArrayList<Book> resolvingDocument(Document doc){
		ArrayList<Book> bookList = new ArrayList<Book>();
		
		//查看是否存在class=grid的table，有就取得所有行，没有则表示当前书籍简介页面
		Elements rows = doc.select("table.grid tr");
		if(rows.size() > 1){
			for(int i = 1; i < rows.size(); i++){
				Element row = rows.eq(i).first();
				String bookName = row.child(0).text();
				String author = row.child(2).text();
				String latestChapter = row.child(1).text();
				String indexUrl = row.child(1).child(0).absUrl("href");
				Book book = new Book();
				book.bookName = bookName;
				book.author = author;
				book.latestChapter = latestChapter;
				book.indexURL = indexUrl;
				bookList.add(book);
			}
		}else{
			Element startRead = doc.select(".btopt").first();
			if(startRead != null){
				String[] contents = doc.title().split(" - ");
				String bookName = contents[0].trim();
				String author = contents[1].trim();
				String latestChapter = doc.select(".newupdate ul li a").first().text();
				String indexUrl = startRead.child(0).absUrl("href");
				Book book = new Book();
				book.bookName = bookName;
				book.author = author;
				book.latestChapter = latestChapter;
				book.indexURL = indexUrl;
				bookList.add(book);
			}
		}
		
		return bookList;
	}
	
	/**
	 * 如果searchBookByKeyword返回null，则可调用此方法查看失败原因
	 * @return 失败原因
	 * */
	public String checkError(){
		return error;
	}
}
