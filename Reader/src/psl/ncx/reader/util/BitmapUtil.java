package psl.ncx.reader.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BitmapUtil {
	public static Bitmap combineBitmaps(Bitmap...bitmaps){
		if (bitmaps.length == 0) return null;
		
		int height = 0;
		for(int i = 0; i < bitmaps.length; i++){
			if(bitmaps[i] != null){
				height += bitmaps[i].getHeight();
			}
		}
		if(height == 0) return null;
		int width = bitmaps[0].getWidth();
		
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		int top = 0;
		for(int i = 0; i < bitmaps.length; i ++){
			if(bitmaps[i] != null){
				canvas.drawBitmap(bitmaps[i], 0, top, paint);
				top += bitmaps[i].getHeight();
				bitmaps[i].recycle();
				bitmaps[i] = null;
			}
		}
		
		return result;
	}
}
