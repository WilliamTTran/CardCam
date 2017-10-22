package highlighter;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;

public class ImageDetector {
	private static final double HIGHLIGHT_THRESHOLD = 0.30; //proportion of box which needs to be colored to stop being considered an image
	private static final int AREA_THRESHOLD = 5000; //min image area
	
	private HashSet<Point> checkedPoints = null;
	private ArrayList<Rectangle> detectedImages = null;
	private Point bottomRight = null;
	private ImageProcessor ip;
	
	public ArrayList<BufferedImage> detectImages(String filePath, int[] pink) throws IOException {
		checkedPoints = new HashSet<Point>();
		detectedImages = new ArrayList<Rectangle>();
		bottomRight = null;
		
		//process image
		Opener opener = new Opener();  
		String imageFilePath = filePath;
		ImagePlus imp = opener.openImage(imageFilePath);
		this.ip = imp.getProcessor(); // ImageProcessor from ImagePlus 
		
		for(int y=0; y<ip.getHeight(); y++) {
			  for(int x=0; x<ip.getWidth(); x++) {
				  //skip checking if the area has already been highlighted
				  boolean contains = false;
				  for(Rectangle rect : detectedImages) {
					  if(rect.contains(x, y)) {
						  contains = true;
						  break;
					  }
				  }
				  if(!contains && !checkedPoints.contains(new Point(x,y))) {
					  Rectangle boxedImage = getBoxedImage(x,y, pink); //will be null unless it finds a new object
					  	if(boxedImage != null){
					  		if(Highlighter.highlightPercentage(ip, boxedImage, pink) < HIGHLIGHT_THRESHOLD){
								  detectedImages.add(boxedImage);
					  		}
					  	}
				  }
			  }
		  }
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		for(Rectangle r : detectedImages) {
			ImageProcessor cropped = ip.crop();
			cropped.setRoi(r);
			cropped = cropped.resize(r.getBounds().width, r.getBounds().height);
			
		    images.add(cropped.getBufferedImage());
		}
		return images;
	}
	
	private Rectangle getBoxedImage(int x, int y, int[] pink) {
		bottomRight = new Point(x,y);
		recursivelyCheckAdjacentPixels(x,y,pink);
		int width = (int) (bottomRight.getX() - x);
		int height = (int) (bottomRight.getY() - y);
		if(width * height >= AREA_THRESHOLD) {
			return new Rectangle(x,y,
					width,
					height);
		}else {
			return null;
		}
	}
	
	private void recursivelyCheckAdjacentPixels(int x, int y, int[] pink) {
		Point p = new Point(x-1,y);
		if(Highlighter.isPixelSimilarColor(ip, x-1, y, pink) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x-1, y, pink);
		}
		p = new Point(x+1,y);
		if(Highlighter.isPixelSimilarColor(ip, x+1, y, pink) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x+1, y, pink);
		}
		p = new Point(x,y+1);
		if(Highlighter.isPixelSimilarColor(ip, x, y+1, pink) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x, y+1, pink);
		}
		p = new Point(x,y-1);
		if(Highlighter.isPixelSimilarColor(ip, x, y-1, pink) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x, y-1, pink);
		}
		setBottomRight(x,y);
	}
	
	private void setBottomRight(int x, int y) {
		if(x > bottomRight.getX()) {
			bottomRight.x = x;
		}
		if(y > bottomRight.getY()) {
			bottomRight.y = y;
		}
	}
	
}
