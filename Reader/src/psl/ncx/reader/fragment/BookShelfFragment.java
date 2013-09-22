package psl.ncx.reader.fragment;

import java.util.ArrayList;

import psl.ncx.reader.ContentActivity;
import psl.ncx.reader.R;
import psl.ncx.reader.adapter.BookShelfAdapter;
import psl.ncx.reader.constant.IntentConstant;
import psl.ncx.reader.util.DataAccessHelper;
import android.app.ActionBar;
import android.app.Fragment;
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

public class BookShelfFragment extends Fragment {
	private GridView mGrid;
	private ImageView anim;
	private ActionBar mActionBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragment = inflater.inflate(R.layout.fragment_bookshelf, container, false);
		
		mActionBar = getActivity().getActionBar();
		
		mGrid = (GridView) fragment.findViewById(R.id.gridview);
		mGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object[] o = (Object[]) parent.getItemAtPosition(position);
				String bookname = (String) o[0];
				Intent intent = new Intent(getActivity(), ContentActivity.class);
				intent.putExtra(IntentConstant.OPEN_INDEX, 0);
				intent.putExtra(IntentConstant.OPEN_BOOKNAME, bookname);
				startActivity(intent);
			}
		});
		/**长按删除指定书籍*/
		mGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object[] o = (Object[]) parent.getItemAtPosition(position);
				String bookname = (String) o[0];
				getActivity().deleteFile(bookname);
				getActivity().deleteFile(bookname.substring(0, bookname.lastIndexOf('.')) + "@cover.png");
				return true;
			}
		});
		/**点下书架隐藏搜索栏*/
		mGrid.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					if(mActionBar.isShowing()) mActionBar.hide();
					break;
				}
				return false;
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
	
	private class LoadBookShelf extends AsyncTask<Void, Void, ArrayList<Object[]>>{
		@Override
		protected void onPreExecute() {
			mGrid.setVisibility(View.GONE);
			
			anim.setVisibility(View.VISIBLE);
			anim.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.drawable.processing));
		}
		
		@Override
		protected ArrayList<Object[]> doInBackground(Void... params) {
			return DataAccessHelper.loadBooks(getActivity());
		}
		
		@Override
		protected void onPostExecute(ArrayList<Object[]> result) {
			anim.clearAnimation();
			anim.setVisibility(View.GONE);
			
			mGrid.setVisibility(View.VISIBLE);
			BookShelfAdapter adapter = new BookShelfAdapter(getActivity(), result);
			mGrid.setAdapter(adapter);
		}
	}
}
