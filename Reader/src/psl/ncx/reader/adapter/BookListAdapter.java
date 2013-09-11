package psl.ncx.reader.adapter;

import java.util.ArrayList;

import psl.ncx.reader.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {
	/*Context*/
	private Context context;
	/*显示布局*/
	private int layout;
	/*显示数据*/
	private ArrayList<String[]> bookList;
	
	public BookListAdapter(Context context, int layout, ArrayList<String[]> bookList){
		this.context = context;
		this.layout = layout;
		this.bookList = bookList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bookList.size();
	}

	@Override
	public String[] getItem(int position) {
		// TODO Auto-generated method stub
		return bookList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(layout, null);
		}
		ImageView cover = (ImageView) convertView.findViewById(R.id.imageview_cover);
		TextView bookName = (TextView)convertView.findViewById(R.id.text_bookname);
		TextView author = (TextView)convertView.findViewById(R.id.text_author);
		TextView date = (TextView)convertView.findViewById(R.id.text_date);
		
		cover.setImageResource(R.drawable.ic_launcher);
		bookName.setText("书名：" + getItem(position)[1]);
		author.setText("作者：" + getItem(position)[2]);
		date.setText("最新章节：" + getItem(position)[3]);
		
		return convertView;
	}

}
