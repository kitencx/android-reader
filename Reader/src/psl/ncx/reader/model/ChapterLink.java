package psl.ncx.reader.model;

import java.io.Serializable;

/**
 * 章节的链接，包含章节名和链接地址信息，实现了Comparable接口，根据链接地址的文件序号进行排序
 * */
public class ChapterLink implements Comparable<ChapterLink>, Serializable {
	/**
	 * 序列化版本号
	 */
	private static final long serialVersionUID = 7362482689606010750L;
	
	public String title;
	public String link;
	
	public ChapterLink(){}
	
	public ChapterLink(String title, String link){
		this.title = title;
		this.link = link;
	}
	
	@Override
	public int compareTo(ChapterLink another) {
		//如果链接中有非内容章节的连接，则最小
		if (!another.link.matches("http://.+/\\d+\\.html")) return -1;
		
		String thisFileIndex = link.substring(link.lastIndexOf('/') + 1, link.lastIndexOf('.'));
		String anotherFileIndex = another.link.substring(another.link.lastIndexOf('/') + 1, another.link.lastIndexOf('.'));
		long thisIndex = Long.parseLong(thisFileIndex);
		long anotherIndex = Long.parseLong(anotherFileIndex);
		if(thisIndex > anotherIndex){
			return 1;
		}else if(thisIndex == anotherIndex){
			return 0;
		}else{
			return -1;
		}
	}

	@Override
	public int hashCode(){
		return link.hashCode() + title.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof ChapterLink)) return false;
		
		ChapterLink cl = (ChapterLink)o;
		return link.equals(cl.link) && title.equals(cl.title);
	}
}
