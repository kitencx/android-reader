package psl.ncx.reader;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import psl.ncx.reader.async.DownloadThread;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.util.DataAccessUtil;
import psl.ncx.reader.util.HttpUtil;
import psl.ncx.reader.views.PagedView;
import psl.ncx.reader.views.PagedView.OnPagingListener;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ContentActivity extends Activity {
	/**
	 * ViewHolder，布局的所有组件集合
	 * @author kitencx
	 *
	 */
	class ViewHolder {
		public ProgressBar loadIndicator;
		public Button btnRetry;
		public PagedView contentView;
	}
	private final static float DEFAULT_TEXTSIZE = 36.0f;
	private final static float MIN_TEXTSIZE = 24.0f;
	private final static String CONNECT_FAILED = "connect_failed";
	/**
	 * 标识是否开启内容缓存
	 * */
	private boolean useCache = true;
	/**
	 * 页面组件
	 * */
	private ViewHolder mViewHolder;
	private ActionBar mActionBar;
	private MenuItem mDownloadItem;
	private PopupWindow mFontSizeSelector;
	/**
	 * 当前阅读的Book对象
	 * */
	private Book mBook;
	/**
	 * 当前阅读的章节
	 * */
	private int position;
	/**
	 * 翻页事件监听
	 * */
	private PageDownListener listener;
	/**
	 * 屏幕尺寸
	 * */
	private Point mScreenSize;
	/**
	 * 点击显示ActionBar的触摸点范围
	 */
	private Rect mRect;
	/**
	 * 手势检测
	 * */
	private GestureDetector textGesture;
	/**
	 * 用于标识翻页之后显示第一页还是最后一页
	 * */
	private boolean showLast;

	private OnPagingListener pagingListener = new OnPagingListener() {

		@Override
		public void onPageUp(View v) {
		}

		@Override
		public void onPageDown(View v) {
		}

		@Override
		public void onPageOverForward(View v) {
			if (position < mBook.catalog.size() - 1) {
				position++;
				showLast = false;
				new LoadContent().execute();
			} else {
				new AlertDialog.Builder(ContentActivity.this).setMessage(
						"已经是最后一章！").show();
			}
		}

		@Override
		public void onPageOverBack(View v) {
			if (position > 0) {
				position--;
				showLast = true;
				new LoadContent().execute();
			} else {
				new AlertDialog.Builder(ContentActivity.this).setMessage(
						"已经是第一章！").show();
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_content);

		mActionBar = getActionBar();
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.hide();

		mViewHolder = new ViewHolder();
		mViewHolder.loadIndicator = (ProgressBar) findViewById(R.id.pb_loadindicator);
		mViewHolder.btnRetry = (Button) findViewById(R.id.btn_retry);
		mViewHolder.contentView = (PagedView) findViewById(R.id.view_content);
		mViewHolder.contentView.setTextSize(DEFAULT_TEXTSIZE);
		mViewHolder.contentView.setLongClickable(true);

		mScreenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(mScreenSize);
		
		mRect = new Rect(mScreenSize.x / 4, mScreenSize.y / 3, mScreenSize.x * 3 / 4, mScreenSize.y * 2 / 3);

		textGesture = new GestureDetector(this, new SimpleOnGestureListener() {
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				if (mActionBar.isShowing()) {
					mActionBar.hide();
				}
				return false;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if (mActionBar.isShowing()) {
					mActionBar.hide();
					return false;
				} else {
					if (mRect.contains((int) e.getX(), (int) e.getY())) {
						if (MainActivity.getDownloadStatusById(mBook.bookid)) {
							mDownloadItem.setIcon(R.drawable.downloading);
							mDownloadItem.setEnabled(false);
						} else {
							mDownloadItem.setIcon(R.drawable.download);
							mDownloadItem.setEnabled(true);
						}
						mActionBar.show();
						return true;
					}
				}
				return false;
			}
		});
		
		listener = new PageDownListener();

		mViewHolder.contentView.setOnPagingListener(pagingListener);
		mViewHolder.contentView.setOnTouchListener(listener);

		Intent intent = getIntent();
		String bookid = intent.getStringExtra(IntentConstant.BOOKID);
		mBook = MainActivity.getBookById(bookid);
		//确保position的位置合法
		position = mBook.bookmark < 0 ? 0 : mBook.bookmark;
		position = position > mBook.catalog.size() - 1 ? mBook.catalog.size() - 1 : position; 

		new LoadContent().execute();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			this.finish();
			break;
		case R.id.action_catalog:
			if (mBook.catalog == null) {
				//如果目录为空，表示载入目录的异步任务没有完成
				new AlertDialog.Builder(this).setMessage("正在载入目录，请稍等！").show();
			} else {
				mActionBar.hide();
				Intent intent = new Intent(this, ChapterActivity.class);
				intent.putExtra(IntentConstant.OPEN_INDEX, position);
				intent.putExtra(IntentConstant.BOOKID, mBook.bookid);
				startActivityForResult(intent, 0);
				overridePendingTransition(R.anim.in_from_top, 0);
			}
			break;
		case R.id.action_download:
			//下载任务开启
			item.setEnabled(false);
			item.setIcon(R.drawable.downloading);
			if (!MainActivity.getDownloadStatusById(mBook.bookid)) {
				Thread task = new DownloadThread(this, mBook.bookid);
				MainActivity.DL_TASKS.put(mBook.bookid, task);
				task.start();
			}
			break;
		case R.id.action_fontsize:
			SeekBar seek = (SeekBar) LayoutInflater.from(this).inflate(R.layout.fontsize_selector, null);
			float textSize = mViewHolder.contentView.getTextSize();
			seek.setProgress((int) (textSize - MIN_TEXTSIZE) / 2);
			seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					mViewHolder.contentView.setTextSize(MIN_TEXTSIZE + progress * 2);
				}
			});
			mFontSizeSelector = new PopupWindow(seek, 
					400, LayoutParams.WRAP_CONTENT, true);
			mFontSizeSelector.setOutsideTouchable(true);
			mFontSizeSelector.setBackgroundDrawable(new ColorDrawable(0xffe0e0e0));
			mFontSizeSelector.showAtLocation(mViewHolder.contentView, Gravity.CENTER_HORIZONTAL|Gravity.TOP, 100, mActionBar.getHeight() + 5);
			mFontSizeSelector.setTouchInterceptor(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
						mFontSizeSelector.dismiss();
					}
					return false;
				}
			});
			break;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return textGesture.onTouchEvent(event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 0:
			switch (resultCode) {
			case Activity.RESULT_OK:
				position = data.getIntExtra(IntentConstant.OPEN_INDEX, 0);
				showLast = false; // 当用户选择某个章节时，永远显示第一页
				new LoadContent().execute();
				break;
			}
			break;
		}
	}

	/**
	 * 覆写该方法，使它每次返回的时候都回到书架
	 * */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		// 返回书架，必须设置Flag，否则只会新建一个MainActivity
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.content, menu);
		mDownloadItem = menu.findItem(R.id.action_download);
		return true;
	}
	
	/**
	 * 方法被覆写，触发书签保存操作
	 * */
	@Override
	protected void onPause() {
		super.onPause();
		// 保存书签
		DBAccessHelper.updateBookMark(this, mBook.bookid, position);
	}
	
	/**
	 * 异步任务，获取章节内容
	 * */
	class LoadContent extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			mViewHolder.contentView.setVisibility(View.GONE);
			mViewHolder.btnRetry.setVisibility(View.GONE);
			mViewHolder.loadIndicator.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(Void... params) {
			String cname = mBook.catalog.get(position).title.replaceAll(
					"[/\\s\\.\\\\]", "");
			String url = mBook.catalog.get(position).link;

			// 先从缓存中读取内容
			String textContent = DataAccessUtil.loadTextContentFromCache(
					ContentActivity.this, mBook.bookid + "-" + cname + ".txt");
			if (textContent != null) {
				if (textContent.equals("")) { // 删除不包含内容的缓存文件
					File file = new File(getCacheDir(), mBook.bookid + "-"
							+ cname + ".txt");
					file.delete();
				} else {
					return textContent;
				}
			}

			if (!HttpUtil.hasAvaliableNetwork(ContentActivity.this))
				return null;
			// 从网络获取内容
			Document doc = null;
			try {
				doc = Jsoup.connect(url).timeout(10000).get();
			} catch (IOException e) {
				return CONNECT_FAILED;
			}
			String content = psl.ncx.reader.business.ContentResolver
					.resolveContent(doc, mBook.from);
			if (useCache && !StringUtil.isBlank(content)) {
				DataAccessUtil.storeTextContent(ContentActivity.this, content,
						mBook.bookid + "-" + cname + ".txt");
			}
			return content;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ContentActivity.this, R.style.SimpleDialogTheme);
				builder.setMessage("没有可用的网络！")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										ContentActivity.this.finish();
									}
								}).show();
			} else if (CONNECT_FAILED.equals(result)) {
				showConnectFailed(result);
			} else {
				showTextContent(result);
			}
		}

		/**
		 * 替换所有半角标点为全角，美化文本显示
		 * */
		private String replacePunctuMarks(String src) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < src.length(); i++) {
				char c = src.charAt(i);
				switch (c) {
				case 160:
				case 32:
					// 替换空格
					builder.append((char) 160).append(((char) 160));
					break;
				case 8220:
					// 替换双引号
					builder.append((char) 12317);
					break;
				case 8221:
					builder.append((char) 12318);
					break;
				default:
					builder.append(c);
				}
			}
			return builder.toString();
		}

		/**
		 * 连接失败，则显示重试按钮
		 * */
		private void showConnectFailed(String result) {
			mViewHolder.loadIndicator.setVisibility(View.GONE);
			mViewHolder.contentView.setVisibility(View.GONE);
			
			mViewHolder.btnRetry.setVisibility(View.VISIBLE);
			mViewHolder.btnRetry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new LoadContent().execute();
				}
			});
		}

		/**
		 * 显示文本章节内容
		 * */
		private void showTextContent(String result) {
			mViewHolder.loadIndicator.setVisibility(View.GONE);
			mViewHolder.btnRetry.setVisibility(View.GONE);
			
			mViewHolder.contentView.setVisibility(View.VISIBLE);
			result = replacePunctuMarks(result);
			mViewHolder.contentView.setTitle(mBook.catalog.get(position).title);
			mViewHolder.contentView.setText(result, showLast);
		}
	}

	/**
	 * 翻页事件监听
	 * */
	private class PageDownListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (v instanceof ScrollView) {
			} else if (v instanceof PagedView) {
				return textGesture.onTouchEvent(event);
			}
			return false;
		}
	}
}
