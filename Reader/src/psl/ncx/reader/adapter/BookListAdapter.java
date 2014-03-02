package psl.ncx.reader.adapter;

import java.util.List;

import psl.ncx.reader.R;
import psl.ncx.reader.model.Book;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {
	/**Context*/
	private Context mContext;
	/**显示布局*/
	private int mLayout;
	/**显示数据*/
	private List<Book> mData;
	/***/
	private boolean isLoading;
	
	public BookListAdapter(Context context, int layout, List<Book> mData){
		this.mContext = context;
		this.mLayout = layout;
		this.mData = mData;
		this.isLoading = true;
	}
	
	/**
	 * 获取Adapter当前状态，载入/非载入，防止在载入的时候被认为Adapter是空的
	 * @return
	 */
	public boolean isLoading() {
		return isLoading;
	}
	
	public void setLoading(boolean flag) {
		this.isLoading = flag;
	}
	
	/**
	 * override该方法，当Adapter处于loading状态时，该Adapter不为空
	 */
	@Override
	public boolean isEmpty() {
		if (isLoading) {
			return false;
		}
		return super.isEmpty();
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Book getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(mLayout, null);
		}
		TextView bookName = (TextView)convertView.findViewById(R.id.text_bookname);
		TextView from = (TextView)convertView.findViewById(R.id.text_from);
		TextView author = (TextView)convertView.findViewById(R.id.text_author);
		TextView date = (TextView)convertView.findViewById(R.id.text_date);
		
		Book book = getItem(position);
		bookName.setText("书名：" + book.bookname);
		from.setText("来源：" + book.from);
		author.setText("作者：" + book.author);
		date.setText("最新章节：" + book.latestChapter);
		
		return convertView;
	}

}
