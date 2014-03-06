package psl.ncx.reader.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Scroller;

public class PagedView extends View {
	/**
	 * 翻页动画持续事件
	 */
	private static final int DEFAULT_DURATION = 500;
	/**
	 * 翻页监听
	 * */
	private OnPagingListener mListener;
	/**
	 * 绘制正文的文本画笔
	 * */
	private float mTextSize;
	private TextPaint mTextPaint;
	/**
	 * 按下、移动、抬起的触摸点坐标
	 * */
	private PointF mDownPointer;
	private PointF mMovedPointer;
	/**
	 * Scroller
	 * */
	private Scroller mScroller;
	/**
	 * 当前章节的标题、内容
	 * */
	private String mTitle;
	private String mContent;
	/**
	 * 分页指针
	 * */
	private int mEndPos;
	/**
	 * 页面内容
	 * */
	private ArrayList<String> mCurPage;
	private ArrayList<String> mNextPage;
	private ArrayList<String> mPrePage;
	/**
	 * 当前页指针
	 * */
	private int mCurPointer;
	/**
	 * 所有分页的内容
	 * */
	private ArrayList<ArrayList<String>> mPages;
	/**
	 * 屏幕大小
	 * */
	private Point mScreenSize;
	/**
	 * 标识翻页之后显示第一页还是最后一页
	 * */
	private boolean showLast;
	/**
	 * 标识是否需要重新计算页数，是否保持在当前页
	 * */
	private boolean reCalc;
	private boolean isStay;
	private Rect mPreArea1, mPreArea2, mNextArea1, mNextArea2;
	/**
	 * 页面偏移距离
	 */
	private float dx;
	
	/**
	 * 必须提供该构造，否则无法在xml中使用该View
	 * */
	public PagedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PagedView(Context context) {
		super(context);
		init();
	}
	
	/**
	 * 设置翻页监听
	 * */
	public void setOnPagingListener(OnPagingListener l) {
		this.mListener = l;
	}
	
	/**
	 * 设置当前章节的内容
	 * */
	public void setText(String content) {
		this.setText(content, false);
	}
	
	public void setText(String content, boolean showLast) {
		this.mContent = content;
		this.showLast = showLast;
		this.isStay = false;
		this.reCalc = true;
		invalidate();
	}
	
	/**
	 * 设置当前文本的字体大小
	 * */
	public void setTextSize(float textSize) {
		this.mTextSize = textSize;
		this.isStay = true;
		this.reCalc = true;
		invalidate();
	}
	
	public float getTextSize() {
		return this.mTextSize;
	}
	
	/**
	 * 获取当前章节的内容
	 * */
	public String getText() {
		return this.mContent;
	}
	
	public void setTitle(String title) {
		this.mTitle = title;
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			abortAnimation();
			
			mDownPointer.x = mMovedPointer.x = event.getX();
			mDownPointer.y = mMovedPointer.y = event.getY();
			dx = mMovedPointer.x - mDownPointer.x;
			//确定前一页、当前页、后一页内容
			if (mCurPointer >= 0 && mCurPointer < mPages.size()) mCurPage = mPages.get(mCurPointer);
			if (mCurPointer - 1 >= 0 && mCurPointer - 1 < mPages.size()) mPrePage = mPages.get(mCurPointer - 1);
			else mPrePage = null;
			if (mCurPointer + 1 >= 0 && mCurPointer + 1 < mPages.size()) mNextPage = mPages.get(mCurPointer + 1);
			else mNextPage = null;
			
			break;
		case MotionEvent.ACTION_MOVE:
			float x = event.getX();
			float y = event.getY();
			//防止小距离移动造成无法响应用户点击
			if (Math.abs(x - mDownPointer.x) > 10) {
				moveTo(x, y);
			}
			break;
		case MotionEvent.ACTION_UP:
			//抬起时，判断后续动作（前/后翻页，滚动回原位(不翻页)）
			if (Math.abs(dx) > 100) {
				moveOver(dx < 0);
			} else if (Math.abs(dx) < 10) {
				if (mPreArea1.contains((int) mDownPointer.x, (int) mDownPointer.y) 
						|| mPreArea2.contains((int) mDownPointer.x, (int) mDownPointer.y)) {
					moveOver(false);
				} else if (mNextArea1.contains((int) mDownPointer.x, (int) mDownPointer.y)
						|| mNextArea2.contains((int) mDownPointer.x, (int) mDownPointer.y)) {
					moveOver(true);
				}
			} else {
				//移动距离不足，滚动回原位
				moveBack();
			}
			break;
		}
		return true;
	}
	
	/**
	 * 移动到x坐标
	 * @param x	移动到的x点坐标
	 */
	public void moveTo(float x, float y) {
		mMovedPointer.x = x;
		mMovedPointer.y = y;
		dx = mMovedPointer.x - mDownPointer.x;
		invalidate();
	}
	
	/**
	 * 将页面恢复到移动前的状态，适用于当手指滑动时，页面跟随移动，松开后不翻页，需要将页面恢复回滑动前的状态
	 */
	public void moveBack() {
		mScroller.startScroll((int) mMovedPointer.x, 0, -(int) dx, 0);
		invalidate();
	}
	
	/**
	 * 翻页
	 * @param forward true:后一页，false:前一页
	 */
	public void moveOver(boolean forward) {
		if (forward) {
			if (mNextPage == null) {
				if (mListener != null) mListener.onPageOverForward(this);
				moveBack();
			} else {
				if (mListener != null) mListener.onPageDown(this);
				mCurPointer++;
				mScroller.startScroll((int) mMovedPointer.x, 0, -(int) dx - getWidth(), 0, DEFAULT_DURATION);
			}
		} else {
			if (mPrePage == null) {
				if (mListener != null) mListener.onPageOverBack(this);
				moveBack();
			} else {
				if (mListener != null) mListener.onPageUp(this);
				mCurPointer--;
				mScroller.startScroll((int) mMovedPointer.x, 0, getWidth() - (int) dx, 0, DEFAULT_DURATION);
			}
		}
		invalidate();
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			moveTo(mScroller.getCurrX(), mScroller.getCurrY());
		} 
	}
	
	/**
	 * 停止动画，并将当前View的状态直接置为动画结果时的状态
	 * */
	public void abortAnimation() {
		if (!mScroller.isFinished()) mScroller.abortAnimation();
	}
	
	/**
	 * 返回下一页内容，如果没有下一页，则返回null
	 * */
	ArrayList<String> nextPage() {
		mTextPaint.setTextSize(mTextSize);
		int lineCount = (int) ((getHeight() - getPaddingBottom() - getPaddingTop()) / mTextPaint.getFontSpacing());
		int drawWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		
		if (mEndPos >= mContent.length()) return null;

		ArrayList<String> content = new ArrayList<String>();
		while (content.size() < lineCount && mEndPos < mContent.length()) {
			String para = readParagraphForward(mEndPos);
			if (!para.equals("")) {
				mEndPos += para.length();
				
				if (para.equals("\n") && 				//如果第一行或者最后一行为空行，则跳过
						(content.size() == 0 || content.size() == lineCount - 1)) continue;		
				
				while (para.length() > 0) {
					int length = mTextPaint.breakText(para, true, drawWidth, null);
					content.add(para.substring(0, length));
					para = para.substring(length);
					if (content.size() >= lineCount) {
						break;
					}
				}
				mEndPos -= para.length();
			}
		}
		return content;
	}
	
	/**
	 * 从指定位置开始往后读取一个段落，包含换行符
	 * @param startPos 开始读取的位置
	 * @return 读取的段落
	 * */
	String readParagraphForward(int startPos) {
		int i = mContent.indexOf('\n', startPos);
		if (i != -1) {
			//找到换行符，读取段落
			return mContent.substring(startPos, i + 1);
		} else {
			//没有换行符，返回之后的所有内容
			return mContent.substring(startPos);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (reCalc) calcPages();
		//绘制标题
		if (mTitle != null) {
			mTextPaint.setTextSize(28.0f);
			float length = mTextPaint.measureText(mTitle);
			canvas.drawText(mTitle, (getWidth() - length)/2, mTextPaint.getFontSpacing(), mTextPaint);
		}
		//绘制内容
		mTextPaint.setTextSize(mTextSize);
		float space = mTextPaint.getFontSpacing();
		float x = dx + getPaddingLeft();
		float y = space + getPaddingTop();
		
		if (mPrePage == null && mCurPointer > 0) mPrePage = mPages.get(mCurPointer - 1); 
		if (mPrePage != null) {
			for (String str : mPrePage) {
				canvas.drawText(str, x - getWidth(), y, mTextPaint);
				y += space;
			}
		} 
		
		if (mCurPage == null && mPages.size() > 0) mCurPage = mPages.get(mCurPointer);
		if (mCurPage != null) {
			y = space + getPaddingTop();
			for (String str : mCurPage) {
				canvas.drawText(str, x, y, mTextPaint);
				y += space;
			}
		}
		
		if (mNextPage == null && mCurPointer < mPages.size() - 2) mNextPage = mPages.get(mCurPointer + 1);
		if (mNextPage != null) {
			y = space + getPaddingTop();
			for (String str : mNextPage) {
				canvas.drawText(str, getWidth() + x, y, mTextPaint);
				y += space;
			}
		}
		//绘制页码
		drawPageFooter(canvas);
	}
	
	/**
	 * 
	 * */
	private void drawPageFooter(Canvas canvas) {
		if (mPages.size() > 0) {
			mTextPaint.setTextSize(28.0f);
			String footer = "第" + (mCurPointer + 1) + "/" + mPages.size() + "页";
			float length = mTextPaint.measureText(footer);
			float x = (getWidth() - length)/2;
			float y = getHeight() - (mTextPaint.getFontSpacing() - mTextPaint.getTextSize());
			canvas.drawText(footer, x, y, mTextPaint);
		} 
	}
	
	/**
	 * 计算页数
	 * */
	private void calcPages() {
		mPages.clear();
		mEndPos = 0;
		ArrayList<String> page = null;
		while ((page = nextPage()) != null) {
			mPages.add(page);
		}
		//判断翻页动作，决定是显示第一页还是最后一页
		if (!isStay) mCurPointer = showLast ? mPages.size() - 1 : 0;
		else if (mCurPointer > mPages.size() - 1) mCurPointer = mPages.size() - 1;
		mPrePage = null; mCurPage = null; mNextPage = null;
		reCalc = false;
	}
	
	/**
	 * 初始化各成员变量
	 * */
	private void init() {
		mScreenSize = new Point();
		((WindowManager)getContext()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(mScreenSize);
		mPreArea1 = new Rect(0, 0, mScreenSize.x, mScreenSize.y / 3);
		mPreArea2 = new Rect(mPreArea1.left, mPreArea1.bottom, mScreenSize.x / 4, mScreenSize.y * 2 / 3);
		mNextArea1 = new Rect(mPreArea2.left, mPreArea2.bottom, mScreenSize.x, mScreenSize.y);
		mNextArea2 = new Rect(mScreenSize.x * 3 / 4, mPreArea2.top, mNextArea1.right, mNextArea1.top);
		
		mDownPointer = new PointF();
		mMovedPointer = new PointF();
		
		mTextPaint = new TextPaint();
		mTextSize = 40.0f;
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(mTextSize);
		
		mContent = "";
		mPages = new ArrayList<ArrayList<String>>();
		
		mScroller = new Scroller(getContext());
	}
	
	public static interface OnPagingListener {
		/**
		 * 往回翻页时触发，当没有前一页时，不会触发
		 * @see #onPageOverBack()
		 * */
		public void onPageUp(View v);
		/**
		 * 往后翻页时触发，当没有下一页时，不会触发
		 * @see #onPageOverForward()
		 * */
		public void onPageDown(View v);
		/**
		 * 当没有下一页，继续翻页的时候则触发该方法
		 * */
		public void onPageOverForward(View v);
		/**
		 * 当没有前一页，继续翻页则触发该方法
		 * */
		public void onPageOverBack(View v);
	}
}
