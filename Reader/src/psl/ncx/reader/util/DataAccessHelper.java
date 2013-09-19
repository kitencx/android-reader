package psl.ncx.reader.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import psl.ncx.reader.model.Book;

import android.content.Context;

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
		try {
			fos = context.openFileOutput(book.bookName + ".txt", Context.MODE_PRIVATE);
			writer = new PrintWriter(fos, true);
			writer.write("INDEX_URL=" + book.indexURL + "\n");
			if(book.catalog != null){
				ArrayList<String[]> catalog = book.catalog;
				int size = book.catalog.size();
				for(int i = 0; i < size; i++){
					writer.write(i + "=" + catalog.get(i)[0] + "," + catalog.get(i)[1] + "\n");
					if(writer.checkError()) System.out.println("写入文件发生错误！");
				}
				return true;
			}
		} catch (FileNotFoundException e) {
			// 忽略，因为openFileOutput()会创建文件，不会抛出此异常
		} finally {
			if(fos != null)
				try{fos.close();}
				catch (IOException e){}
			if(writer != null) writer.close();
		}
		return false;
	}
	
	/**
	 * 列出该app下所有.txt文件
	 * @return 所有文件的文件名数组
	 * */
	public static String[] listBooksName(Context context){
		File dir = context.getFilesDir();
		return dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if(filename.endsWith(".txt")) return true;
				return false;
			}
		});
	}
}
