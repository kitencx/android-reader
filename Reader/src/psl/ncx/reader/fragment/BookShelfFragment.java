package psl.ncx.reader.fragment;

import java.util.ArrayList;

import psl.ncx.reader.ContentActivity;
import psl.ncx.reader.R;
import psl.ncx.reader.adapter.BookShelfAdapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.service.DownloadService;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

public class BookShelfFragment extends Fragment {
	/**
	 * ActionBar
	 * */
	private ActionBar mActionBar;
	/**
	 * 书架视图
	 * */
	private GridView mGrid;
	private BookShelfAdapter mAdapter;
	private ArrayList<Book> mData;
	/**
	 * 工具栏Fragment
	 * */
	private ToolBarFragment mToolBar;
	/**
	 * 处理界面更新
	 * */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//接收到广播，更新UI
			int percent = intent.getIntExtra(IntentConstant.DOWNLOAD_PERCENT, 0);
			String bookid = intent.getStringExtra("BOOKID");
			int position = getPostionByBookId(bookid);
			Book book = (Book) mGrid.getItemAtPosition(position);
			if (book != null) {
				//因为Adapter是在异步任务中建立的，所以不能保证广播接收器注册的时候已经可以获取到Book
				//此处加入null判断，当可以获取到的时候，才进行更新
				book.percent = percent;
				mGrid.invalidateViews();
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragment = inflater.inflate(R.layout.fragment_bookshelf, container, false);
		
		mActionBar = getActivity().getActionBar();

		mData = new ArrayList<Book>();
		mAdapter = new BookShelfAdapter(getActivity(), mData);
		mGrid = (GridView) fragment.findViewById(R.id.gridview);
		mGrid.setAdapter(mAdapter);
		//TouchListener，触摸隐藏搜索栏
		mGrid.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()){
				case MotionEvent.ACTION_DOWN:
					if (mActionBar.isShowing()) mActionBar.hide();
					break;
				}
				return false;
			}
		});
		//ItemClickListener，点击看书
		mGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//防止用户连续点击
				mGrid.setEnabled(false);
				Book book = (Book) parent.getItemAtPosition(position);
				Intent intent = new Intent(getActivity(), ContentActivity.class);
				intent.putExtra(IntentConstant.BOOK_INFO, book);
				startActivity(intent);
				//当前点击处理完毕，解除点击限制
				mGrid.setEnabled(true);
			}
		});
		//ItemLongClickListener，长按删除
		mGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Book book = (Book) parent.getItemAtPosition(position);
				AlertDialog.Builder builder = new Builder(getActivity(), R.style.SimpleDialogTheme);
				builder.setMessage("删除《" + book.bookname + "》？")
				.setNegativeButton("删除", new OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, int which) {
						dialog.dismiss();
						mAdapter.remove(position);
					}
				})
				.setPositiveButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
				return true;
			}
		});
		
		return fragment;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		getActivity().registerReceiver(receiver, new IntentFilter(DownloadService.class.getName()));
		new AsyncTaskLoadBooks().execute();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		//解除注册的广播接收器
		getActivity().unregisterReceiver(receiver);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//解除服务绑定
		mAdapter.unBindService();
	}
	
	/**
	 * 查找指定Book在GridView中的索引位置
	 * @param bookid 指定Book的id
	 * @return 指定Book所处的索引位置，没有找到则返回-1
	 * */
	private int getPostionByBookId(String bookid) {
		for (int i = 0; i < mGrid.getCount(); i++) {
			Book book = (Book) mGrid.getItemAtPosition(i);
			if (book.bookid.equals(bookid)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 异步加载类，加载所有Book信息
	 * */
	class AsyncTaskLoadBooks extends AsyncTask<Void, Void, ArrayList<Book>> {
		@Override
		protected ArrayList<Book> doInBackground(Void... params) {
			//查询数据库中所有的Book信息
			ArrayList<Book> books = DBAccessHelper.queryAllBooks(getActivity());
			return books;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Book> books) {
			//清除原先内容
			mData.clear();
			//显示新载入的内容
			mData.addAll(books);
			mAdapter.notifyDataSetChanged();
			//允许更新，使更新按钮可用
			mToolBar = (ToolBarFragment)getFragmentManager().findFragmentByTag("toolbar");
			mToolBar.setUpdateEnabled(true);
			mToolBar.setBooks(mData);
		}
	}
	
}
