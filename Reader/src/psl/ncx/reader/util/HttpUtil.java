package psl.ncx.reader.util;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class HttpUtil {
	/**
	 *根据指定的请求方式，配置默认的连接参数
	 *@param connection 待配置的连接对象
	 *@param requestType 请求类型，POST/GET
	 *@exception ProtocolException 指定的请求方式不被连接对象所支持，则抛出
	 * */
	public static void configDefaultConnection(HttpURLConnection connection, 
			String requestType) throws ProtocolException{
		
		if("GET".equalsIgnoreCase(requestType)){
			try {
				connection.setRequestMethod("GET");
			} catch (ProtocolException e) {
				throw e;
			}
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept-Encoding", "UTF-8,GBK");
			connection.setDoInput(true);
			
		}else if("POST".equalsIgnoreCase(requestType)){
			try {
				connection.setRequestMethod("GET");
			} catch (ProtocolException e) {
				throw e;
			}
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept-Encoding", "UTF-8,GBK");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
		}else{
			throw new ProtocolException("configDefaultConnection()只支持POST/GET请求的默认参数配置，不支持设置类型：" + requestType);
		}
	}
	
	/**
	 *设置POST请求正文，将需要传递的参数按指定编码写入请求正文
	 *@param connection 连接对象
	 *@param values 请求参数，key-value形式
	 *@param charset 参数编码类型
	 *@exception UnsupportedEncodingException 如果指定的参数编码类型不被支持，则抛出
	 *@exception IOException 在写入请求正文，connection会建立一个到目标服务器的tcp连接，如果有错误发生，则抛出
	 * */
	public static void setPayLoad(HttpURLConnection connection, 
			Map<String, String> values, String charset) throws UnsupportedEncodingException, IOException{
		
		if(values == null || values.isEmpty()) throw new RuntimeException("传递的参数列表不能为空！");
		
		StringBuffer payload = new StringBuffer();
		Iterator<Map.Entry<String, String>> iter = values.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> entry = iter.next();
			String key = URLEncoder.encode(entry.getKey(), charset);
			String value = URLEncoder.encode(entry.getValue(), charset);
			payload.append(key);
			payload.append("=");
			payload.append(value);
			payload.append("&");
		}
		DataOutputStream dos = null;
		try{
			dos = new DataOutputStream(connection.getOutputStream());
			dos.writeBytes(payload.substring(0, payload.length() - 1));
		} finally{
			if(dos != null) dos.close();
		}
	}
	
	/**
	 * 根据URL获取图片
	 * @param context
	 * @param url 图片src属性
	 * @return 失败则为null
	 * */
	public static Bitmap loadImageFromURL(Context context, String url){
		URL imageURL = null;
		try {
			imageURL = new URL(url);
		} catch (MalformedURLException e) {
			//should not happen
			e.printStackTrace();
		}
		
		HttpURLConnection conn = null;
		InputStream is = null;
		try{
			conn = (HttpURLConnection) imageURL.openConnection();
			is = conn.getInputStream();
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(is != null)
				try {is.close();}
				catch (IOException e) {}
				finally {if(conn != null) conn.disconnect();}
		}
		return null;
	}
	
	/**
	 * 保存网络图片到本地，采用随机命名
	 * @param context
	 * @param url 图片URL
	 * @return 保存成功则返回图片的文件名，否则返回null
	 * */
	public static String storeImageFromURL(Context context, String url) {
		URL imageURL = null;
		try {
			imageURL = new URL(url);
		} catch (MalformedURLException e) {
			//should not happen.
			e.printStackTrace();
			return null;
		}
		HttpURLConnection conn = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try{
			conn = (HttpURLConnection) imageURL.openConnection();
			is = conn.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			if(bitmap == null) throw new IOException("图片解码失败！");
			//图片下载成功，缓存至本地目录
			String filename = System.currentTimeMillis() + ".png";
			fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)){
				return filename;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(is != null)
				try{is.close();} 
				catch (IOException e) {}
				finally{
					if(fos != null)
						try{fos.close();}
						catch(IOException e){}
						finally{if(conn != null) conn.disconnect();}
				}
		}
		return null;
	}

	public static  boolean hasAvaliableNetwork(Context context){
		ConnectivityManager connectivityManager = 
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null) System.out.println(info.getTypeName());
		return info != null;
	}
}
