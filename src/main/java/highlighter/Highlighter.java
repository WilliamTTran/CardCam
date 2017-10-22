package highlighter;

import java.awt.Rectangle;

import com.google.cloud.vision.v1.BoundingPoly;

import ij.process.ImageProcessor;

public class Highlighter {
	public static final int[] BLUE = new int[] {99,165,161};
	public static final int[] PINK = new int[] {218,111,137};
	public static final int RGB_RED = 0, RGB_GREEN = 1, RGB_BLUE = 2;
	protected static final double COLOR_DISTANCE_THRESHOLD = 60; //3d (r,g,b) distance to be considered dissimilar colors
	protected static final int TOP_LEFT = 0, TOP_RIGHT = 1, BOTTOM_RIGHT = 2, BOTTOM_LEFT = 3;
	
	public static final String TEST_IMAGE_FILEPATH = "C:/Users/Kevin/Desktop/docusign3.jpg";
	
	  protected static boolean isPixelSimilarColor(ImageProcessor ip, int x, int y, int[] colorToCompare) { 
		  int[] rgb = new int[3];
		  ip.getPixel(x,y,rgb);
		  
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
	
	  protected static double highlightPercentage(ImageProcessor ip, Rectangle r, int[] colorToCompare) {
		  int similar = 0, different = 0;

		  for(int y=r.y; y<r.getMaxY(); y++) {
			  for(int x=r.x; x<r.getMaxX(); x++) {
				  if(Highlighter.isPixelSimilarColor(ip,x,y, colorToCompare)) {
					  similar++;
				  }else {
					  different++;
				  }
			  }
		  }
		  return ((double)similar)/different;
	  }

	  protected static Rectangle polyToRect(BoundingPoly bp) {
		  return new Rectangle(bp.getVertices(TOP_LEFT).getX(),
				  bp.getVertices(TOP_LEFT).getY(),
				  bp.getVertices(BOTTOM_RIGHT).getX() - bp.getVertices(TOP_LEFT).getX(),
				  bp.getVertices(BOTTOM_RIGHT).getY() - bp.getVertices(TOP_LEFT).getY());
	  }
}
