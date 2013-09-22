package psl.ncx.reader.model;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Book {
	/**书名*/
	public String bookName;
	/**作者*/
	public String author;
	/**更新时间*/
	public String updateTime;
	/**最新章节*/
	public String latestChapter;
	/**简介*/
	public String summary;
	/**目录页链接*/
	public String indexURL;
	/**目录*/
	public ArrayList<String[]> catalog;
	/**封面图片*/
	public Bitmap cover;
}
