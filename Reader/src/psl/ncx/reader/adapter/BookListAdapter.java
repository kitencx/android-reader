package psl.ncx.reader.adapter;

import java.util.ArrayList;

import psl.ncx.reader.R;
import psl.ncx.reader.model.Book;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {
	/*Context*/
	private Context context;
	/*显示布局*/
	private int layout;
	/*显示数据*/
	private ArrayList<Book> bookList;
	
	public BookListAdapter(Context context, int layout, ArrayList<Book> bookList){
		this.context = context;
		this.layout = layout;
		this.bookList = bookList;
	}

	@Override
	public int getCount() {
		return bookList.size();
	}

	@Override
	public Book getItem(int position) {
		return bookList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(layout, null);
		}
		TextView bookName = (TextView)convertView.findViewById(R.id.text_bookname);
		TextView author = (TextView)convertView.findViewById(R.id.text_author);
		TextView date = (TextView)convertView.findViewById(R.id.text_date);
		
		Book book = getItem(position);
		bookName.setText("书名：" + book.bookname);
		author.setText("作者：" + book.author);
		date.setText("最新章节：" + book.latestChapter);
		
		return convertView;
	}

}
