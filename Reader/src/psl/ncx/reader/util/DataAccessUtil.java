package psl.ncx.reader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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

	/**
	 * 将指定的内容缓存至本地
	 * @param context
	 * @param content 进行缓存的内容
	 * @param filename 缓存文件名
	 * @return true成功，false失败
	 * @see #storeImageContent(Context, Bitmap, String)
	 * */
	public static boolean storeTextContent(Context context, String content, String filename){
		File cacheDir = context.getCacheDir();
		File cacheFile = new File(cacheDir, filename);
		FileWriter fw = null;
		try {
			fw = new FileWriter(cacheFile);
			fw.write(content);
			fw.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fw != null) 
				try {fw.close();}
				catch (IOException e) {}
		}
		return false;
	}
	
	/**
	 * 将指定的图片内容缓存至本地
	 * @param context
	 * @param content 进行缓存的图片内容
	 * @param filename 缓存文件名
	 * @return true成功，false失败
	 * @see #storeTextContent(Context, String, String)
	 * */
	public static boolean storeImageContent(Context context, Bitmap content, String filename){
		File cacheDir = context.getCacheDir();
		File cacheFile = new File(cacheDir, filename);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(cacheFile);
			return content.compress(Bitmap.CompressFormat.PNG, 0, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {fos.close();}
				catch (IOException e) {}
			}
		}
		return false;
	}

	/**
	 * 从缓存中载入指定的文本内容
	 * @param context
	 * @param filename 载入的文件名
	 * @return 文本内容，没有缓存或者读取失败则为null
	 * */
	public static String loadTextContentFromCache(Context context, String filename){
		//查看文件是否存在
		File cacheFile = new File(context.getCacheDir(), filename);
		if (!cacheFile.exists()) return null;
		
		FileReader reader = null;
		try {
			reader = new FileReader(cacheFile);
			StringBuilder sb = new StringBuilder();
			char[] buf = new char[8 * 1024];
			int length;
			while((length = reader.read(buf)) != -1){
				sb.append(buf, 0, length);
			}
			System.out.println("当前内容从缓存中载入!");
			return sb.toString();
		} catch (FileNotFoundException e) {
			//已经判断存在，不会发生
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {reader.close();}
				catch (IOException e) {}
		}
		
		return null;
	}

	/**
	 * 从缓存中载入指定的图片内容
	 * @param context
	 * @param filename 载入的文件名
	 * @return 图片，没有缓存或者读取失败则为null
	 * */
	public static Bitmap loadImageContentFromCache(Context context, String filename){
		File cacheFile = new File(context.getCacheDir(), filename);
		if (!cacheFile.exists()) return null;
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
		
		System.out.println("当前图片从缓存中载入!");
		return BitmapFactory.decodeFile(cacheFile.getPath(), opts);
	}
	
	/**
	 * 判断指定的缓存文件是否存在
	 * @return true：存在，false：不存在
	 * */
	public static boolean exists(Context context, String filename) {
		File contentFile = new File(context.getCacheDir(), filename);
		return contentFile.exists();
	}
}
