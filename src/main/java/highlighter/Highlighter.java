package highlighter;

import java.awt.Color;
import java.awt.Rectangle;

import com.google.cloud.vision.v1.BoundingPoly;

import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Highlighter {
	public static final Color BLUE = new Color(99,165,161);
	public static final Color PINK = new Color(218,111,137);
	protected static final double COLOR_DISTANCE_THRESHOLD = 60; //3d (r,g,b) distance to be considered dissimilar colors
	protected static final int TOP_LEFT = 0, TOP_RIGHT = 1, BOTTOM_RIGHT = 2, BOTTOM_LEFT = 3;
	
	public static final String TEST_IMAGE_FILEPATH = "C:/Users/Kevin/Desktop/docusign3.jpg";
	
	  protected static boolean isPixelSimilarColor(ImageProcessor ip, int x, int y, Color colorToCompare) { 
		  int[] rgb = new int[3];
		  ip.getPixel(x,y,rgb);
		  
		  Color c = new Color(rgb[0],rgb[1],rgb[2]);
		  
		  return similarColor(colorToCompare,c);
	  }

	  protected static boolean similarColor(Color c1, Color c2) {
		  return colorDistance(c1,c2) < COLOR_DISTANCE_THRESHOLD;
	  }
	  
	  protected static double colorDistance(Color c1, Color c2) {
		  return Math.sqrt(Math.pow(c1.getRed()-c2.getRed(),2)
				  + Math.pow(c1.getGreen()-c2.getGreen(),2)
				  + Math.pow(c1.getBlue()-c2.getBlue(),2));
	  }
	
	  protected static double highlightPercentage(ImageProcessor ip, Roi roi, Color colorToCompare) {
		  int similar = 0, different = 0;
		  
		  Rectangle r = roi.getBounds();
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

	  protected static Roi polyToRoi(BoundingPoly bp) {
		  return new Roi(bp.getVertices(TOP_LEFT).getX(),
				  bp.getVertices(TOP_LEFT).getY(),
				  bp.getVertices(BOTTOM_RIGHT).getX() - bp.getVertices(TOP_LEFT).getX(),
				  bp.getVertices(BOTTOM_RIGHT).getY() - bp.getVertices(TOP_LEFT).getY());
	  }
}
