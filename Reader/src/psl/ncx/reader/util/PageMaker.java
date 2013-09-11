package psl.ncx.reader.util;

import java.util.ArrayList;

import android.graphics.Paint;

public class PageMaker {
	private String content;
	private int width;
	private int height;
	private int end;
	private int begin;
	private int lineCount;
	private Paint paint;
	private int drawableWidth;
	private int drawableHeight;
	private int paddingTop;
	private int paddingLeft;
	private int paddingRight;
	private int paddingBottom;
	private int marginTop;
	private int marginLeft;
	private int marginRight;
	private int marginBottom;
	
	/**
	 * @param content 页面内容
	 * @param w 页面宽度
	 * @param h 页面高度
	 * @param paint 内容显示视图的paint对象，如要将内容显示在TextView上，则该参数必须为TextView.getPaint()
	 * */
	public PageMaker(String content, int w, int h, Paint paint){
		this.content = content;
		this.width = w;
		this.height = h;
		this.paint = paint;
		
		resizeDrawableArea();
	}
	
	/**
	 * 设置视图Margin，l/t/r/b为左/上/右/下的Margin值
	 * */
	public void setMargin(int l, int t, int r, int b){
		this.marginLeft = l;
		this.marginTop = t;
		this.marginRight = r;
		this.marginBottom = b;
		
		//Margin变化，必须重新计算绘制区域及行数
		resizeDrawableArea();
	}
	
	/**
	 * 设置视图Padding，l/t/r/b为左/上/右/下的Padding值
	 * */
	public void setPadding(int l, int t, int r, int b){
		this.paddingLeft = l;
		this.paddingTop = t;
		this.paddingRight = r;
		this.paddingBottom = b;
		
		//Padding变化，必须重新计算绘制区域及行数
		resizeDrawableArea();
	}
	
	
	/**
	 * 设置文本
	 * @param text 需要分页显示的所有内容
	 * */
	public void setText(String text){
		this.content = text;
	}
	
	/**
	 * 判断是否有上一页，如果有上一页，将读取指针指向上一页页首
	 * @return true 有上一页
	 * */
	public boolean prePage(){
		int i = 0;
		if(begin > 0){
			while(i < lineCount){
				String preParagraph = readParagraphBack(begin);
				if(preParagraph != null){
					begin -= preParagraph.length();

					while(i < lineCount){
						int length = paint.breakText(preParagraph, true, drawableWidth, null);
						i++;
						preParagraph = preParagraph.substring(length);
						if(preParagraph.length() <= 0){
							break;
						}
					}
					begin += preParagraph.length();
				}else{
					break;
				}
			}
			end = begin;
			return true;
		}else{
			return false;
		}
	}
	
	public ArrayList<String> nextPage(){
		ArrayList<String> lines = new ArrayList<String>();
		if(end < content.length()){
			this.begin = this.end;
			while(lines.size() < lineCount){
				String nextParagraph = readParagraphForward(end);
				if(nextParagraph != null){
					end += nextParagraph.length();
					
					while(lines.size() < lineCount){
						int length = paint.breakText(nextParagraph, true, drawableWidth, null);
						lines.add(nextParagraph.substring(0, length));
						nextParagraph = nextParagraph.substring(length);
						if(nextParagraph.length() <= 0){
							break;
						}
					}
					end -= nextParagraph.length();
				}else{
					break;
				}
			}
			return lines;
		}
		return null;
	}
	
	/**
	 * 读取一个段落
	 * @param 起始位置
	 * @return 读取的段落内容
	 * */
	public String readParagraphForward(int sPosition){
		if(sPosition < content.length()){
			int index = content.indexOf("\n", sPosition);
			if(index == -1){
				return content.substring(sPosition, content.length());
			}else{
				return content.substring(sPosition, index + 1);
			}
		}
		return null;
	}
	
	public String readParagraphBack(int ePosition){
		if(ePosition > 0){
			int i = ePosition - 1;
			while(i >= 0){
				if(content.charAt(i) == '\n' && i != ePosition - 1){
					break;
				}
				i--;
			}
			return content.substring(++i, ePosition);
		}
		return null;
	}
	
	/**
	 * 指针复位，指向起始位置
	 * */
	public void reset(){
		this.end = 0;
	}
	
	/**
	 * 重新计算文本绘制区域大小
	 * */
	private void resizeDrawableArea(){
		this.drawableWidth = width - marginLeft - marginRight - paddingLeft - paddingRight;
		this.drawableHeight = height - marginTop - marginBottom - paddingTop - paddingBottom;
		float textsize = paint.getTextSize();
		this.lineCount = (int)(drawableHeight/textsize);
	}
}
