package psl.ncx.reader.business;

import org.jsoup.nodes.Document;

import android.text.Html;

import psl.ncx.reader.constant.SupportSite;

public class ContentResolver {
	
	public static String resolveContent(Document doc, String site){
		if (SupportSite.WJZW.equals(site)) {
			return Html.fromHtml(doc.select("#content").html()).toString();
		} else if (SupportSite.LJZW.equals(site)) {
			return Html.fromHtml(doc.select(".novel_content").html()).toString();
		}
		return null;
	}
}
