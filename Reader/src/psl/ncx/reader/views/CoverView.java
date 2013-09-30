package psl.ncx.reader.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.widget.ImageView;

/**
 * 封面视图，显示封面图片和书名
 * */
public class CoverView extends ImageView {
	/**
	 * 文本画笔
	 * */
	private TextPaint paint;
	/**
	 * 书名
	 * */
	private String title;
	/**
	 * 书名背景大小
	 * */
	private Rect rect;
	
	public CoverView(Context context){
		super(context);
		
		this.paint = new TextPaint();
		paint.setTextSize(15.0f);
		paint.setAntiAlias(true);
	}

	/**
	 * 设置书名
	 * */
	public void setTitle(String title){
		this.title = title;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(title != null){
			if(rect == null){
				rect = new Rect(getPaddingLeft(), (int)(getHeight() * 0.6f),
						getWidth() - getPaddingRight(), (int)(getHeight() * 0.6f) + 20);
			}
			paint.setColor(0x80000000);
			canvas.drawRect(rect, paint);
			paint.setColor(0xffffffff);
			int length = paint.breakText(title, true, rect.width(), null);
			title = title.substring(0, length);
			float x = (getWidth() - paint.measureText(title)) / 2;
			canvas.drawText(title, x, getHeight() * 0.6f + paint.getTextSize(), paint);
		}
	}
}
