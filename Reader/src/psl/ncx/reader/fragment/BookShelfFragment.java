package psl.ncx.reader.fragment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import psl.ncx.reader.ContentActivity;
import psl.ncx.reader.R;
import psl.ncx.reader.adapter.BookShelfAdapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class BookShelfFragment extends Fragment {
	private ActionBar mActionBar;
	private GridView mGrid;
	private ImageView anim;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragment = inflater.inflate(R.layout.fragment_bookshelf, container, false);
		
		mActionBar = getActivity().getActionBar();
		
		mGrid = (GridView) fragment.findViewById(R.id.gridview);
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
		mGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Book book = (Book) parent.getItemAtPosition(position);
				Intent intent = new Intent(getActivity(), ContentActivity.class);
				intent.putExtra(IntentConstant.BOOK_INFO, book);
				startActivity(intent);
			}
		});
		/**长按删除指定书籍*/
		mGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Book book = (Book) parent.getItemAtPosition(position);
				AlertDialog.Builder builder = new Builder(getActivity(), R.style.SimpleDialogTheme);
				builder.setMessage("删除《" + book.bookname + "》？")
				.setNegativeButton("删除", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//删除数据库中Book信息
						DBAccessHelper.removeBookById(getActivity(), book.bookid);
						//删除封面
						if(book.cover != null) getActivity().deleteFile(book.cover);
						//删除缓存章节
						File dir = getActivity().getCacheDir();
						String[] files = dir.list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String filename) {
								if (filename.startsWith(book.bookid + "-")){
									return true;
								}
								return false;
							}
						});
						for (String file : files){
							new File(dir, file).delete();
						}
						
						Toast.makeText(getActivity(), "《" + book.bookname + "》删除成功！", Toast.LENGTH_SHORT).show();
						new LoadBookShelf().execute();
						dialog.dismiss();
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
		
		anim = (ImageView) fragment.findViewById(R.id.anim_load);
		
		return fragment;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		new LoadBookShelf().execute();
	}
	
	/**
	 * 异步查询任务，查询所有的书籍的基本信息
	 * */
	private class LoadBookShelf extends AsyncTask<Void, Void, ArrayList<Book>>{
		@Override
		protected void onPreExecute() {
			mGrid.setVisibility(View.GONE);
			
			anim.setVisibility(View.VISIBLE);
			anim.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.drawable.processing));
		}
		
		@Override
		protected ArrayList<Book> doInBackground(Void... params) {
			return DBAccessHelper.queryAllBooks(getActivity());
		}
		
		@Override
		protected void onPostExecute(ArrayList<Book> result) {
			anim.clearAnimation();
			anim.setVisibility(View.GONE);
			
			mGrid.setVisibility(View.VISIBLE);
			BookShelfAdapter adapter = new BookShelfAdapter(getActivity(), result);
			mGrid.setAdapter(adapter);
		}
	}
}
