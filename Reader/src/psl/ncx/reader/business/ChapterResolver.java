package psl.ncx.reader.business;

import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import psl.ncx.reader.constant.SupportSite;
import psl.ncx.reader.model.ChapterLink;

/**
 * 目录解析类
 * */
public class ChapterResolver {
	
	public static ArrayList<ChapterLink> resolveIndex(Document doc, String site){
		if (SupportSite.WJZW.equals(site)) {
			return new ChapterResolver().resolveIndexForWJZW(doc);
		} else if (SupportSite.LJZW.equals(site)) {
			return new ChapterResolver().resolveIndexForLJZW(doc);
		}
		return null;
	}
	
	/***
	 * 五九中文
	 * */
	private ArrayList<ChapterLink> resolveIndexForWJZW(Document doc){
		if (doc == null) return null;
		
		ArrayList<ChapterLink> chapters = new ArrayList<ChapterLink>();
		Elements links = doc.select("table.acss a");
		for(Element link : links){
			chapters.add(new ChapterLink(link.text(), link.absUrl("href")));
		}
		Collections.sort(chapters);
		return chapters;
	}
	
	private ArrayList<ChapterLink> resolveIndexForLJZW(Document doc){
		if (doc == null) return null;
		
		ArrayList<ChapterLink> chapters = new ArrayList<ChapterLink>();
		Elements links = doc.select(".mod_container a");
		for(Element link : links){
			chapters.add(new ChapterLink(link.text(), link.absUrl("href")));
		}
		Collections.sort(chapters);
		return chapters;
	}
}
