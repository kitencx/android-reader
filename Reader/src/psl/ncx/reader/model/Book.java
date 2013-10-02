package psl.ncx.reader.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Book implements Serializable{
	/**序列化版本号*/
	private static final long serialVersionUID = 2446669851043724797L;
	/**unique id*/
	public String bookid;
	/**书名*/
	public String bookname;
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
	public ArrayList<ChapterLink> catalog;
	/**书签*/
	public int bookmark;
	/**来源*/
	public String from;
	/**封面图片文件名*/
	public String cover;
	/**简介页链接*/
	public String summaryURL;
	/**下载百分比*/
	public int percent;
}
