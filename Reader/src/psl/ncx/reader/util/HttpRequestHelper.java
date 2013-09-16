package psl.ncx.reader.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class HttpRequestHelper {
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
	 * @return 不会为null，只要获取失败，就会抛出IOException;
	 * */
	public static Bitmap loadURLImage(String url, String filename) throws IOException{
		/*尝试从本地存储上加载该章节，如果有则直接返回*/
		DataStorage store = new DataStorage();
		Bitmap cache = store.loadImageFromFile(filename);
		if(cache != null) return cache;
		
		/*本地存储没有该图片，从网络获取*/
		URL imageURL = new URL(url);
		HttpURLConnection conn = null;
		InputStream is = null;
		try{
			conn = (HttpURLConnection) imageURL.openConnection();
			is = conn.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			if(bitmap == null) throw new IOException("图片解码失败！");
			//图片下载成功，缓存至本地目录
			if(store.storeImage(bitmap, filename)){
				//释放掉原来的图片资源
				bitmap.recycle();
				bitmap = null;
				//重新载入本地图片
				bitmap = store.loadImageFromFile(filename);
			}
			
			return bitmap;
		}finally{
			if(is != null){
				try{
					is.close();
				}finally{
					if(conn != null) conn.disconnect();
				}
			}
		}
	}
}
