package psl.ncx.reader.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 数据读写辅助类，提供了各种静态方法用于文件的CRUD操作
 * */
public class DataAccessUtil {
	/**
	 * 载入指定图书的封面
	 * @param context
	 * @param book 指定图书
	 * @return 有则返回指定的封面，没有则返回null
	 * */
	public static Bitmap loadCoverImage(Context context, String filename){
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(filename);
			Bitmap src = BitmapFactory.decodeStream(fis);
			if(src != null){
				if(src.getWidth() == 200 && src.getHeight() == 250){
					return src;
				}else{
					Bitmap cover = Bitmap.createScaledBitmap(src, 200, 250, true);
					src.recycle();
					src = null;
					return cover;
				}
			}
		} catch (FileNotFoundException e) {
		} finally {
			if(fis != null){
				try {fis.close();}
				catch (IOException e){}
			}
		}
		return null;
	}
}
