package highlighter;

import com.google.api.services.vision.v1.model.BoundingPoly;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Color;
import android.util.Log;

public class Highlighter {
	public static final int[] BLUE = new int[] {99,165,161};
	public static final int[] PINK = new int[] {218,111,137};
	public static final int RGB_RED = 0, RGB_GREEN = 1, RGB_BLUE = 2;
	protected static final double COLOR_DISTANCE_THRESHOLD = 60; //3d (r,g,b) distance to be considered dissimilar colors
	protected static final int TOP_LEFT = 0, TOP_RIGHT = 1, BOTTOM_RIGHT = 2, BOTTOM_LEFT = 3;
	
	public static final String TEST_IMAGE_FILEPATH = "C:/Users/Kevin/Desktop/docusign3.jpg";
	
	  protected static boolean isPixelSimilarColor(Bitmap ip, int x, int y, int[] colorToCompare) {
		  int color = ip.getPixel(x,y);
		  int r = Color.red(color);
		  int g = Color.green(color);
		  int b = Color.blue(color);
		  int[] rgb = { r, g, b};

          //Log.i("RGB LUL", x + " " + y + " " + rgb[0] + " " + rgb[1] + " " + rgb[2]);

		  return similarColor(colorToCompare,rgb);
	  }

	  protected static boolean similarColor(int[] c1, int[] c2) {
		  return colorDistance(c1,c2) < COLOR_DISTANCE_THRESHOLD;
	  }
	  
	  protected static double colorDistance(int[] c1, int[] c2) {
		  return Math.sqrt(Math.pow(c1[Highlighter.RGB_RED]-c2[Highlighter.RGB_RED],2)
				  + Math.pow(c1[Highlighter.RGB_GREEN]-c2[Highlighter.RGB_GREEN],2)
				  + Math.pow(c1[Highlighter.RGB_BLUE]-c2[Highlighter.RGB_BLUE],2));
	  }
	
	  protected static double highlightPercentage(Bitmap ip, Rect r, int[] colorToCompare) {
		  int similar = 0, different = 0;

		  for(int y=Math.max(0,r.top); y<r.bottom; y++) {
			  for(int x=Math.max(0,r.left); x<r.right; x++) {
				  if(Highlighter.isPixelSimilarColor(ip,x,y, colorToCompare)) {
					  similar++;
				  } else {
					  different++;
				  }
			  }
		  }

		  Log.i("PERCENTAGE", similar + " " + different);

		  return ((double)similar)/different;
	  }

	  protected static Rect polyToRect(BoundingPoly bp) {
          Log.i("asd", bp.getVertices().get(TOP_LEFT).getY() + " " + bp.getVertices().get(BOTTOM_RIGHT).getY());
          try {
              return new Rect(bp.getVertices().get(TOP_LEFT).getX(),
                      bp.getVertices().get(TOP_LEFT).getY(),
                      bp.getVertices().get(BOTTOM_RIGHT).getX(),
                      bp.getVertices().get(BOTTOM_RIGHT).getY());
          }catch(Exception e){
              return null;
          }
	  }
}
