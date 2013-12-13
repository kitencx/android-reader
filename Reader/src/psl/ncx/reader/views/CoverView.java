package psl.ncx.reader.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
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
	/**
	 * 下载完成百分比
	 * */
	private int percent;
	/**
	 * 完成度背景大小
	 * */
	private Rect bg_rect;
	
	public CoverView(Context context) {
		super(context);
		
		this.paint = new TextPaint();
		paint.setTextSize(30.0f);
		paint.setAntiAlias(true);
	}
	
	public CoverView(Context context, AttributeSet attr) {
		super(context, attr);
		
		this.paint = new TextPaint();
		paint.setTextSize(30.0f);
		paint.setAntiAlias(true);
	}

	/**
	 * 设置书名
	 * */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * 设置下载完成百分比，如果该值被设置，则会根据该值进行背景颜色的绘制
	 * */
	public void setPercent(int percent) {
		this.percent = percent;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (title != null) {
			if(rect == null){
				rect = new Rect(getPaddingLeft(), (int)(getHeight() * 0.6f),
						getWidth() - getPaddingRight(), (int)(getHeight() * 0.6f) + 40);
			}
			paint.setColor(0xa0000000);
			canvas.drawRect(rect, paint);
			paint.setColor(0xffffffff);
			int length = paint.breakText(title, true, rect.width(), null);
			title = title.substring(0, length);
			float x = (getWidth() - paint.measureText(title)) / 2;
			canvas.drawText(title, x, getHeight() * 0.6f + paint.getTextSize(), paint);
		}
		
		if (percent > 0 && percent < 100) {
			int dh = (getHeight() - getPaddingTop() - getPaddingBottom());
			if (bg_rect == null) {
				bg_rect = new Rect(getPaddingLeft(), dh - dh * percent / 100,
						getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
			} else {
				bg_rect.top = dh - dh * percent / 100;
			}
			paint.setColor(0x803366ff);
			canvas.drawRect(bg_rect, paint);
		}
	}
}
