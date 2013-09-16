package psl.ncx.reader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class DataStorage {
	private String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "Reader";
	
	public DataStorage(){
		File directory = new File(path);
		directory.mkdirs();
	}
	/**
	 * 存储图片，存储过程中会将图片格式强制转换成png
	 * @param bitmap 需要存储的图片
	 * @param imageName 存储的文件名
	 * @return true 存储成功，false存储过程中发生错误
	 * */
	public boolean storeImage(Bitmap bitmap, String imageName){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(path + File.separator + imageName + ".png");
				bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if(fos != null)
					try {
						fos.close();
					} catch (IOException e) {}
			}
		}
		
		return false;
	}
	
	/**
	 * 本地存储上加载指定的图片
	 * @return 如果加载失败，返回null;
	 * */
	public Bitmap loadImageFromFile(String fileName){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path + File.separator + fileName + ".png");
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.ALPHA_8;
			opts.inScaled = false;
			Bitmap bitmap = BitmapFactory.decodeStream(fis, null, opts);
			return bitmap;
		} catch (FileNotFoundException e) {
			//忽略，返回null，让当前线程从网络载入
		} finally {
			if(fis != null){
				try{
					fis.close();
				} catch (IOException e){}
			}
		}
		
		return null;
	}
}
