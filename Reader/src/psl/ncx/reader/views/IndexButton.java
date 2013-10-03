package psl.ncx.reader.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * 用于AdpaterView中的Button，可以记录自身在Adpater中的索引位置
 * */
public class IndexButton extends ImageButton {
	/**
	 * 索引位置
	 * */
	private int position = -1;
	
	public IndexButton(Context context) {
		super(context);
	}

	public IndexButton(Context context, AttributeSet attr) {
		super(context, attr);
	}
	
	/**
	 * 获取该Button在AdapterView中所处的索引位置
	 * */
	public int getPosition() {
		return position;
	}
	
	/**
	 * 设置该Button在AdapterView中所处的索引位置
	 * */
	public void setPosition(int position) {
		this.position = position;
	}
}
