package psl.ncx.reader.util;

public class URLValidator {
	/**url type，匹配简介页URL*/
	public static final int URL_SUMMARY = 0x0;
	/**url type，匹配目录页URL*/
	public static final int URL_INDEX = 0x1;
	/**url type，匹配内容页URL*/
	public static final int URL_CONTENT = 0x10;
	
	/**
	 * 验证指定的URL是否是指定类型的URL
	 * @param URL 验证的URL
	 * @param type 指定的类型
	 * */
	public static boolean validate(String URL, int type){
		switch(type){
		case URL_SUMMARY:
			return URL.matches("http://.+/jieshaoinfo/\\d+/\\d+\\.htm");
		case URL_INDEX:
			return URL.matches("http://.+/xiaoshuo/\\d+/\\d+/index\\.html");
		case URL_CONTENT:
			return URL.matches("http://.+/xiaoshuo/\\d+/\\d+/\\d+\\.html");
		}
		return false;
	}
}
