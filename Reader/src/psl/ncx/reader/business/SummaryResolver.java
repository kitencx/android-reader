package psl.ncx.reader.business;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import psl.ncx.reader.constant.SupportSite;
import psl.ncx.reader.model.Book;


/**
 * 简介和封面解析类
 * */
public class SummaryResolver {
	
	/**
	 * 尝试解析Document，如果有简介和封面信息，则写入output
	 * @param doc 解析的页面
	 * @param output 输出对象，解析的结果会被写入该对象
	 * @param SupportSite，根据指定的站点解析，每个站点的Document结构不同
	 * @return 返回图片的链接地址
	 * */
	public static String resolveSummary(Document doc, Book output, String site){
		if (SupportSite.WJZW.equals(site)) {
			return new SummaryResolver().resolveSummaryInWJZW(doc, output);
		} else if (SupportSite.LJZW.equals(site)) {
			return new SummaryResolver().resolveSummaryInLJZW(doc, output);
		} 
		return null;
	}
	
	/**
	 * 六九中文
	 * 封面信息:div.coverleft a img
	 * 简介信息:div.intro
	 * */
	private String resolveSummaryInLJZW(Document doc, Book output){
		String intro = doc.select(".intro").text();
		output.summary = intro;
		
		Elements img = doc.select(".coverleft img");
		if (!img.isEmpty()) return img.first().absUrl("src");
		
		return null;
	}
	
	/**
	 * 伍九中文
	 * 简介信息:span.hottext 内容简介：之后的同级元素
	 * */
	private String resolveSummaryInWJZW(Document doc, Book output){
		Elements e = doc.select(".hottext");
		e = e.parents();
		if (!e.isEmpty()) {
			Element td = e.first();
			
			if (output.summary == null) output.summary = td.ownText();
			
			Elements imgs = td.select("img");
			if (imgs.size() > 0) {
				Element img = imgs.first();
				return img.absUrl("src");
			}
		}
		
		return null;
	}
}
