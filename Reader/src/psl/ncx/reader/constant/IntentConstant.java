package psl.ncx.reader.constant;

/**
 * 保存了Intent传递时所有传递值的名称
 * */
public interface IntentConstant {
	/**小说搜索关键字，传递的值为String*/
	public static final String SEARCH_KEYWORD = "psl.ncx.reader.search.keyword";
	/**当前打开小说的目录页URL，传递的值为String*/
	public static final String BOOK_INFO = "psl.ncx.reader.open.bookinfo";
	/**打开小说的章节索引，传递的值为int*/
	public static final String OPEN_INDEX = "psl.ncx.reader.open.index";
	/**下载完成度*/
	public static final String DOWNLOAD_PERCENT = "psl.ncx.reader.dlpercent";
	public static final String BOOKID = "psl.ncx.reader.bookid";
}
