package psl.ncx.reader.fragment;

import psl.ncx.reader.R;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class ToolBarFragment extends Fragment {
	private ImageButton mbtnBookShelf;
	private ImageButton mbtnSearch;
	private ActionBar mActionBar;

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
		return fragment;
	}
}
