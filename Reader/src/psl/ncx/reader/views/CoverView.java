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
	private TextPaint mPaint;
	/**
	 * 书名
	 * */
	private String title;
	/**
	 * 书名背景大小
	 * */
	private Rect mTitleRect;
	/**
	 * 下载完成百分比
	 * */
	private int percent;
	/**
	 * 完成度背景大小
	 * */
	private Rect bg_rect;
	
	public CoverView(Context context, AttributeSet attr) {
		super(context, attr);
		
		this.mPaint = new TextPaint();
		mPaint.setTextSize(30.0f);
		mPaint.setAntiAlias(true);
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
			if(mTitleRect == null){
				mTitleRect = new Rect(getPaddingLeft(), (int)(getHeight() * 0.6f),
						getWidth() - getPaddingRight(), (int)(getHeight() * 0.6f) + 40);
			}
			mPaint.setColor(0xa0000000);
			canvas.drawRect(mTitleRect, mPaint);
			mPaint.setColor(0xffffffff);
			int length = mPaint.breakText(title, true, mTitleRect.width(), null);
			title = title.substring(0, length);
			float x = (getWidth() - mPaint.measureText(title)) / 2;
			canvas.drawText(title, x, getHeight() * 0.6f + mPaint.getTextSize(), mPaint);
		}
		
		if (percent > 0 && percent < 100) {
			int dh = (getHeight() - getPaddingTop() - getPaddingBottom());
			if (bg_rect == null) {
				bg_rect = new Rect(getPaddingLeft(), dh - dh * percent / 100,
						getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
			} else {
				bg_rect.top = dh - dh * percent / 100;
			}
			mPaint.setColor(0x803366ff);
			canvas.drawRect(bg_rect, mPaint);
			mPaint.setColor(0xffaaff00);
			String percentstr = percent + "%";
			float x = (getWidth() - mPaint.measureText(percentstr)) / 2;
			canvas.drawText(percentstr, x, getHeight() / 2, mPaint);
		}
	}
}
