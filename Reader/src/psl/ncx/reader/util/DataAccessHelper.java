package psl.ncx.reader.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import psl.ncx.reader.model.Book;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 数据读写辅助类，提供了各种静态方法用于文件的CRUD操作
 * */
public class DataAccessHelper {
	
	/**
	 * 判断文件是否存在
	 * @param context
	 * @param booName 需要判断的书名，不包含扩展名
	 * @return true 已经存在
	 * */
	public static boolean isExisted(Context context, String bookName){
		File dir = context.getFilesDir();
		File book = new File(dir, bookName + ".txt");
		return book.exists();
	}
	
	/**
	 * 保存小说，保存格式:
	 * <ul>
	 * 	<li>以书名.txt为文件名</li>
	 * 	<li>文件内容为key=value形式，以"\n"分隔</li>
	 * 	<li>文件内容的第一行为：INDEX_URL=本书目录页URL</li>
	 * 	<li>之后的所有内容为：索引=章节名,章节内容URL	如：0=引子,http://test.com/testbook/test.htm</li>
	 * </ul>
	 * @param context
	 * @param book 要保存的书籍对象
	 * */
	public static boolean store(Context context, Book book){
		FileOutputStream fos = null;
		PrintWriter writer = null;
		if(book.catalog != null){
			try {
				fos = context.openFileOutput(book.bookName + ".txt", Context.MODE_PRIVATE);
				writer = new PrintWriter(fos, true);
				writer.write("INDEX_URL=" + book.indexURL + "\n");
				ArrayList<String[]> catalog = book.catalog;
				int size = book.catalog.size();
				for(int i = 0; i < size; i++){
					writer.write(i + "=" + catalog.get(i)[0] + "," + catalog.get(i)[1] + "\n");
					if(writer.checkError()){
						Log.e("Store", "保存目录时发生错误！");
						return false;
					}
				}
			} catch (FileNotFoundException e) {
			} finally {
				if(fos != null){
					try {fos.close();}
					catch (IOException e){}
					finally {if(writer != null) writer.close();}
				}
			}
		}
		
		if(book.cover != null){
			try {
				fos = context.openFileOutput(book.bookName + "@cover.png", Context.MODE_PRIVATE);
				book.cover.compress(Bitmap.CompressFormat.PNG, 0, fos);
			} catch (FileNotFoundException e) {
			} finally {
				if(fos != null)
					try {fos.close();} 
					catch (IOException e) {}
			}
		}
		
		return true;
	}
	
	/**
	 * 列出该app下所有.txt文件
	 * @return 所有文件的文件名数组
	 * */
	public static ArrayList<Object[]> loadBooks(Context context){
		ArrayList<Object[]> shelfdata = new ArrayList<Object[]>();
		File dir = context.getFilesDir();
		String[] books = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if(filename.endsWith(".txt")) return true;
				return false;
			}
		});
		for(int i = 0; i < books.length; i++){
			Object[] o = new Object[]{books[i], loadCoverImage(context, books[i])};
			shelfdata.add(o);
		}
		return shelfdata;
	}
	
	/**
	 * 从指定文件中载入书籍目录信息
	 * @param context
	 * @param filename 文件名
	 * @return 目录信息，每个String[]包含章节名和章节内容URL
	 * */
	public static ArrayList<String[]> loadCatalogFromFile(Context context, String filename){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(context.getFilesDir() + File.separator + filename));
			ArrayList<String[]> chapters = new ArrayList<String[]>();
			String line;
			while((line = br.readLine()) != null){
				if(line.matches("\\d+=.+,http://.+")){
					String chapter = line.substring(line.indexOf("=") + 1);
					int splitPoint = chapter.lastIndexOf(",");
					String title = chapter.substring(0, splitPoint);
					String url = chapter.substring(splitPoint + 1);
					chapters.add(new String[]{title, url});
				}
			}
			return chapters;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(br != null){
				try {br.close();}
				catch (IOException e){} 
			}
		}
		return null;
	}
	
	/**
	 * 缓存图片
	 * @param bitmap 缓存的图片
	 * @param filename 缓存的文件名
	 * @return 标识缓存结果，ture为成功
	 * */
	public static boolean storeImage(Context context, Bitmap bitmap, String filename){
		File cachePath = context.getCacheDir();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(cachePath.getPath() + File.separator + filename);
			return bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(fos != null){
				try {fos.close();}
				catch (IOException e){}
			}
		}
		return false;
	}
	
	/**
	 * 从系统Cache文件夹中取出指定的图片
	 * @param context
	 * @param filename 图片文件名，命名规则是：书名@章节名.png
	 * @return 失败则返回null
	 * */
	public static Bitmap loadImageFromCache(Context context, String filename){
		File cachePath = context.getCacheDir();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(cachePath.getPath() + File.separator + filename);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.ALPHA_8;
			Bitmap bitmap = BitmapFactory.decodeStream(fis, null, opts);
			return bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(fis != null){
				try {fis.close();}
				catch (IOException e){}
			}
		}
		
		return null;
	}
	
	/**
	 * 载入指定图书的封面
	 * @param context
	 * @param bookname 书名
	 * @return 如果失败则返回null
	 * */
	public static Bitmap loadCoverImage(Context context, String bookname){
		int extp = bookname.lastIndexOf(".");
		bookname = bookname.substring(0, extp==-1?bookname.length():extp);
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(bookname + "@cover.png");
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
			e.printStackTrace();
		} finally {
			if(fis != null){
				try {fis.close();}
				catch (IOException e){}
			}
		}
		return null;
	}
}
