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
	private ImageButton mbtnShelf;
	private ImageButton mbtnSearch;
	private ActionBar mActionBar;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar = getActivity().getActionBar();
		
		View fragment = inflater.inflate(R.layout.fragment_toolbar, container, false);
		
		mbtnShelf = (ImageButton) fragment.findViewById(R.id.bookshelf);
		mbtnShelf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mActionBar.isShowing()) mActionBar.hide();
			}
		});
		
		mbtnSearch = (ImageButton) fragment.findViewById(R.id.search);
		mbtnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mActionBar.isShowing()) mActionBar.hide();
				else mActionBar.show();
			}
		});
		
		return fragment;
	}
}
