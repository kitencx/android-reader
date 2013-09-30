package psl.ncx.reader.business;

import org.jsoup.nodes.Document;

import psl.ncx.reader.constant.SupportSite;

public class ContentResolver {
	private int site;
	
	public ContentResolver(int site){
		this.site = site;
	}
	
	public String resolveContent(Document doc){
		switch(site){
		case SupportSite.WJZW:
			return doc.select("#content").html();
		}
		return null;
	}
}
