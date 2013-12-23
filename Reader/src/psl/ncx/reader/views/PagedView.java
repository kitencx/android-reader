package psl.ncx.reader.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Scroller;

public class PagedView extends View {
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
	 * 标识当前一页或者后一页没有内容时，是否可将当前页内容进行拖动
	 * */
	private boolean canDragOver;
	/**
	 * 标识此次触摸操作是否经过ACTION_MOVE
	 * */
	private boolean isMoved;
	/**
	 * 标识翻页之后显示第一页还是最后一页
	 * */
	private boolean showLast;
	/**
	 * 标识是否需要重新计算页数，是否保持在当前页
	 * */
	private boolean reCalc;
	private boolean isStay;
	
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
	
	/**
	 * 当前一页或者后一页没有内容时，是否可将当前页内容进行拖动
	 * */
	public boolean canDragOver() {
		return canDragOver;
	}
	
	/**
	 * 当前一页或者后一页没有内容时，是否可将当前页内容进行拖动
	 * @param flag true 可以拖动
	 * */
	public void enableDragOver(boolean flag) {
		canDragOver = flag;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			abortAnimation();
			
			isMoved = false;
			
			mDownPointer.x = event.getX();
			mMovedPointer.x = event.getX();
			//当前页
			if (mPages.size() > 0) mCurPage = mPages.get(mCurPointer);
			//前一页
			if (mCurPointer - 1 >= 0) mPrePage = mPages.get(mCurPointer - 1);
			else mPrePage = null;
			//后一页
			if (mCurPointer + 1 < mPages.size()) mNextPage = mPages.get(mCurPointer + 1);
			else mNextPage = null;
			
			break;
		case MotionEvent.ACTION_MOVE:
			isMoved = true;
			float dx = event.getX() - mDownPointer.x;
			if (dx > 0) {
				//往右拖动，如果没有上一页，则拖动距离
				if (mPrePage != null) mMovedPointer.x = mDownPointer.x + dx;
				else if (canDragOver) mMovedPointer.x = mDownPointer.x + dx;
			} else {
				//往左拖动，如果没有下一页，则拖动距离
				if (mNextPage != null) mMovedPointer.x = mDownPointer.x + dx;
				else if (canDragOver) mMovedPointer.x = mDownPointer.x + dx;
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			//抬起时，判断后续动作
			//如果移动距离大于100px，并且移动的方向有内容，则翻页
			//如果移动的距离大于100px，但是该方向没有内容，则载入下移章节
			//如果移动的距离小于100px，则不翻页
			dx = mMovedPointer.x - mDownPointer.x;
			if (Math.abs(dx) > 100) {
				//横向移动距离大于100px，翻页
				if (dx > 0) {
					if (mPrePage == null) {
						if (mListener != null) 	mListener.onPageOverBack(this);
						mScroller.startScroll((int)mMovedPointer.x, 0, -(int)dx, 0);
					} else {
						if (mListener != null) mListener.onPageUp(this);
						mCurPointer--;
						mScroller.startScroll((int)mMovedPointer.x, 0, getWidth() - (int)dx, 0, 800);
					}
				} else {
					if (mNextPage == null) {
						if (mListener != null) 	mListener.onPageOverForward(this);
						mScroller.startScroll((int)mMovedPointer.x, 0, -(int)dx, 0);
					} else {
						if (mListener != null) mListener.onPageDown(this);
						mCurPointer++;
						mScroller.startScroll((int)mMovedPointer.x, 0, -(int)dx - getWidth(), 0, 800);
					}
				} 
			} else if (dx == 0){
				//快速点击，则翻页，并且此次触摸事件没有经过移动
				if (mDownPointer.x > mScreenSize.x/2 + 100 && !isMoved) {
					if (mNextPage == null) {
						if (mListener != null) 	mListener.onPageOverForward(this);
						mScroller.startScroll((int)mMovedPointer.x, 0, -(int)dx, 0);
					} else {
						if (mListener != null) mListener.onPageDown(this);
						mMovedPointer.x = mDownPointer.x - getWidth();
						mCurPointer++;
					}
				} else if (mDownPointer.x < mScreenSize.x/2 - 100 && !isMoved) {
					if (mPrePage == null) {
						if (mListener != null) 	mListener.onPageOverBack(this);
						mScroller.startScroll((int)mMovedPointer.x, 0, -(int)dx, 0);
					} else {
						if (mListener != null) mListener.onPageUp(this);
						mMovedPointer.x = mDownPointer.x + getWidth();
						mCurPointer--;
					}
				}
			} else {
				//移动距离不足，滚动回原位
				mScroller.startScroll((int)mMovedPointer.x, 0, -(int)dx, 0);
			}
			invalidate();
			break;
		}
		return true;
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mMovedPointer.x = mScroller.getCurrX();
			invalidate();
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
		float dx = mMovedPointer.x - mDownPointer.x;
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
