package psl.ncx.reader.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 显示文本，必须满屏
 * */
public class backPagedView extends View {
	private final float DEFAULT_TEXTSIZE = 21.0f;
	/*画笔*/
	private TextPaint mPaint;
	/*章节名画笔*/
	private TextPaint titlePaint;
	/*章节名*/
	private String title;
	/*显示内容*/
	private ArrayList<String> mContent;
	
	public backPagedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public backPagedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public backPagedView(Context context) {
		super(context);
		init();
	}

	/**
	 * 设置显示的内容
	 * @param content 显示的内容，每一个String元素代表一行显示的文本
	 * */
	public void setContent(ArrayList<String> content){
		this.mContent = content;
		invalidate();
	}
	
	public void setPaint(TextPaint paint){
		this.mPaint = paint;
	}
	
	public TextPaint getPaint(){
		return mPaint;
	}
	
	public void setTitle(String title){
		this.title = title;
		if(titlePaint == null){
			titlePaint = new TextPaint();
			titlePaint.setTextSize(mPaint.getTextSize() * 0.8f);
			titlePaint.setAntiAlias(true);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//绘制章节名
		if(title != null){
			float titleTextSize = titlePaint.getTextSize();
			float titleLength = titlePaint.measureText(title);
			canvas.drawText(title, (getWidth() - titleLength)/2, titleTextSize, titlePaint);
		}
		//绘制正文内容
		if(mContent != null){
			int yoffset = (int) mPaint.getTextSize() + getPaddingTop();
			for(int i = 0; i < mContent.size(); i++){
				String line = mContent.get(i);
				if (line.equals("\n")) {
					//空行，进行特殊绘制
					canvas.drawText(line, getPaddingLeft(), yoffset, mPaint);
				} else {
					canvas.drawText(line, getPaddingLeft(), yoffset, mPaint);
				}
				yoffset += mPaint.getFontSpacing();
			}
		}
		
	}
	
	private void init(){
		mPaint = new TextPaint();
		mPaint.setTextSize(DEFAULT_TEXTSIZE);
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
	}
}
