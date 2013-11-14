package psl.ncx.reader.fragment;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import psl.ncx.reader.R;
import psl.ncx.reader.business.ChapterResolver;
import psl.ncx.reader.db.DBAccessHelper;
import psl.ncx.reader.model.Book;
import psl.ncx.reader.model.ChapterLink;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class ToolBarFragment extends Fragment {
	private ImageButton mbtnBookShelf;
	private ImageButton mbtnSearch;
	private ImageButton mbtnUpdate;
	private ActionBar mActionBar;
	private List<Book> mBooks;
	private ProgressDialog mDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragment = inflater.inflate(R.layout.fragment_toolbar, container, false);
		
		mActionBar = getActivity().getActionBar();
		
		mbtnSearch = (ImageButton) fragment.findViewById(R.id.search);
		mbtnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mActionBar.isShowing()) mActionBar.hide();
				else mActionBar.show();
			}
		});
		
		mbtnBookShelf = (ImageButton) fragment.findViewById(R.id.bookshelf);
		mbtnBookShelf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mActionBar.isShowing()) mActionBar.hide();
			}
		});
		
		mbtnUpdate = (ImageButton) fragment.findViewById(R.id.update);
		mbtnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mbtnUpdate.setEnabled(false);
				new UpdateAsyncTask().execute();
			}
		});
		
		return fragment;
	}
	
	public void setBooks(List<Book> books) {
		this.mBooks = books;
	}
	
	class UpdateAsyncTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			if (mBooks == null || mBooks.isEmpty()) {
				//没有需要更新的书籍
				cancel(false);
				new AlertDialog.Builder(getActivity()).setMessage("没有需要更新的书籍！")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface source, int which) {
								source.dismiss();
								mbtnUpdate.setEnabled(true);
							}
						}).show();
			} else {
				if (mDialog == null) {
					mDialog = new ProgressDialog(getActivity());
					mDialog.setCancelable(false);
					mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				}
				mDialog.setMax(mBooks.size());
				mDialog.setProgress(0);
				mDialog.show();
			}
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			for (int i = 0; i < mBooks.size(); i++) {
				Book book = mBooks.get(i);
				String url = book.indexURL;
				String from = book.from;
				try {
					Document doc = Jsoup.connect(url).timeout(5000).get();
					List<ChapterLink> chapters = ChapterResolver.resolveIndex(doc, from);
					List<ChapterLink> old = book.catalog;
					if (old == null) old = DBAccessHelper.queryChaptersById(getActivity(), book.bookid);
					if (chapters.size() > old.size()) {
						//有新章节
						chapters.removeAll(old);
						if (chapters.size() > 0) {
							DBAccessHelper.addNewChapter(getActivity(), chapters, book);
							//更新当前book的目录信息
							book.catalog = DBAccessHelper.queryChaptersById(getActivity(), book.bookid);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				publishProgress(i + 1);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			mDialog.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mbtnUpdate.setEnabled(true);
			if (mDialog.isShowing()) mDialog.dismiss();
		}
	}
}
