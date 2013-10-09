package psl.ncx.reader.business;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import psl.ncx.reader.constant.SupportSite;
import psl.ncx.reader.model.Book;

public class BookSearch {
	/**
	 * @param 搜索关键字
	 * @param 搜索类型，书名/作者，可以为null
	 * @return 匹配的书籍集合
	 * */
	public static ArrayList<Book> searchByKeyword(String keyword, String site){
		if (SupportSite.WJZW.equals(site)) {
			return new BookSearch().searchInWJZW(keyword);
		} else if (SupportSite.LJZW.equals(site)) {
			return new BookSearch().searchInLJZW(keyword);
		}
		return null;
	}
	/**
	 * 六九中文:http://www.69zw.com/modules/article/search.php?
	 * searchtype=articlename|author&searchkey=关键字&Submit=+%CB%D1+%CB%F7+
	 * <p>参数编码：GBK</p>
	 * @param keyword 搜索关键字
	 * */
	private ArrayList<Book> searchInLJZW(String keyword) {
		try {
			Document doc = Jsoup.connect("http://www.69zw.com/modules/article/search.php?" +
					"searchtype=articlename&searchkey="+ URLEncoder.encode(keyword, "GBK") + 
					"&Submit=+%CB%D1+%CB%F7+").timeout(10000).get();
			ArrayList<Book> books = new ArrayList<Book>();
			Elements results = doc.select("table.grid tr");
			if( results.size() > 1){
				for(int i = 1; i < results.size(); i++){
					Element result = results.get(i);
					if(result.childNodeSize() > 4){
						Book book = new Book();
						book.bookname = result.child(0).text();
						book.latestChapter = result.child(1).text();
						book.author = result.child(2).text();
						book.updateTime = "20" + result.child(4).text();
						book.indexURL = result.child(1).child(0).absUrl("href");
						book.summaryURL = result.child(0).child(0).absUrl("href");
						//验证是否是一本有效的Book,bookname,indexurl,必须不为空，添加有效Book
						if(book.bookname != null && book.indexURL != null && book.summaryURL != null){
							if(book.indexURL.startsWith("http://") && book.summaryURL.startsWith("http://"))
								book.from = SupportSite.LJZW;
								books.add(book);
						}
					}
				}
			}
			return books;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 五九中文:http://www.59to.com/modules/article/search.php?searchkey=关键字&searchtype=articlename|author
	 * <p>参数编码：gb2312</p>
	 * @param keyword 搜索关键字
	 * @return 所有匹配的Book，失败为null
	 * */
	private ArrayList<Book> searchInWJZW(String keyword){
		try {
			Connection conn = Jsoup.connect("http://www.59to.com/modules/article/search.php?searchkey="
					+ URLEncoder.encode(keyword, "gb2312") + "&searchtype=articlename")
					.timeout(10000).followRedirects(false);
			Document doc = conn.get();
			if (conn.response().statusCode() == 200) {
				
				//当前处于搜索结果列表页，有多个匹配或者没有匹配结果
				Elements results = doc.select("table.grid tr");
				ArrayList<Book> books = new ArrayList<Book>();
				if(results.size() > 1){
					//去除标题栏后仍有内容，则表示有匹配结果
					for(int i = 1; i < results.size(); i++){
						Element result = results.get(i);
						if(result.childNodeSize() > 4){
							Book book = new Book();
							book.bookname = result.child(0).text();
							book.latestChapter = result.child(1).text();
							book.author = result.child(2).text();
							book.updateTime = "20" + result.child(4).text();
							book.indexURL = result.child(1).child(0).absUrl("href");
							book.summaryURL = result.child(0).child(0).absUrl("href");
							//验证是否是一本有效的Book,bookname,indexurl,必须不为空，添加有效Book
							if(book.bookname != null && book.indexURL != null){
								if(book.indexURL.startsWith("http://"))
									book.from = SupportSite.WJZW;
									books.add(book);
							}
						}
					}
				}
				return books;
			} else if (conn.response().statusCode() == 302){
				//页面发生302转跳，搜索有且只有1个匹配的结果，先获取转跳地址
				String location = conn.response().header("location");
				doc = conn.url(location).timeout(10000).get();
				ArrayList<Book> books = new ArrayList<Book>();
				String[] baseinfo = doc.title().split("-");
				Book book = new Book();
				//简介链接
				book.summaryURL = location;
				if (baseinfo.length > 1) {
					//书名、作者
					book.bookname = baseinfo[0];
					book.author = baseinfo[1];
				}
				Elements urls = doc.select(".btnlink");
				if (!urls.isEmpty()) {
					//目录页链接
					book.indexURL = urls.first().absUrl("href");
				}
				
				
				//更新日期
				Elements e = doc.getElementsMatchingOwnText("\\d{4}-\\d{2}-\\d{2}");
				if (!e.isEmpty()) {
					String s = e.text();
					book.updateTime = s.substring(s.length() - 10);
				}
				//简介、最新章节
				e = doc.select(".hottext");
				e = e.parents();
				if (!e.isEmpty()) {
					Element td = e.first();
					if (td.childNodeSize() > 2) {
						book.latestChapter = td.child(2).text();
					}
					book.summary = td.ownText();
				}
				
				//验证是否是一本有效的Book,bookname,indexurl,必须不为空，添加有效Book
				if(book.bookname != null && book.indexURL != null){
					if(book.indexURL.startsWith("http://"))
						book.from = SupportSite.WJZW;
						books.add(book);
				}
				return books;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
