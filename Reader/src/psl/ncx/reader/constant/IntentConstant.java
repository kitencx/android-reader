package psl.ncx.reader.constant;

/**
 * 保存了Intent传递时所有传递值的名称
 * */
public interface IntentConstant {
	/**小说搜索关键字，传递的值为String*/
	public static final String SEARCH_KEYWORD = "psl.ncx.reader.search.keyword";
	/**当前打开的书名，传递的值为String*/
	public static final String OPEN_BOOKNAME = "psl.ncx.reader.open.bookname";
	/**当前打开小说的目录页URL，传递的值为String*/
	public static final String INDEX_URL = "psl.ncx.reader.open.indexurl";
	/**当前打开小说的简介页URL，传递的值为String*/
	public static final String SUMMARY_URL = "psl.ncx.reader.open.summaryurl";
	/**打开小说的目录信息，传递的值为ArrayList<String[]>*/
	public static final String CHAPTERS = "pls.ncx.reader.open.chapters";
	/**打开小说的章节索引，传递的值为int*/
	public static final String OPEN_INDEX = "psl.ncx.reader.open.index";
}
