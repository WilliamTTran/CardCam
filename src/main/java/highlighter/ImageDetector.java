package highlighter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

public class ImageDetector {
	private static final double HIGHLIGHT_THRESHOLD = 0.30; //proportion of box which needs to be colored to stop being considered an image
	private static final int AREA_THRESHOLD = 5000; //min image area
	
	private ArrayList<Point> checkedPoints = null;
	private ArrayList<Rect> detectedImages = null;
	private Point bottomRight = null;
	private Bitmap image;

	static int count = 0;
	public ArrayList<Bitmap> detectImages(Bitmap passedImage, int[] colorToCompare) throws IOException {
		checkedPoints = new ArrayList<Point>();
		detectedImages = new ArrayList<Rect>();
		bottomRight = null;
		
		//process image
		image = passedImage;
		
		for(int y=0; y<image.getHeight(); y++) {
			  for(int x=0; x<image.getWidth(); x++) {
				  //skip checking if the area has already been highlighted
				  boolean contains = false;
				  for(Rect rect : detectedImages) {
					  if(rect.contains(x, y)) {
						  contains = true;
						  break;
					  }
				  }
				  if(!contains && !checkedPoints.contains(new Point(x,y))) {
					  Rect boxedImage = getBoxedImage(x,y, colorToCompare); //will be null unless it finds a new object
					  	if(boxedImage != null){
					  		if(Highlighter.highlightPercentage(image, boxedImage, colorToCompare) < HIGHLIGHT_THRESHOLD){
								  detectedImages.add(boxedImage);
					  		}
					  	}
				  }
			  }
		  }
		ArrayList<Bitmap> images = new ArrayList<Bitmap>();
		for(Rect r : detectedImages) {
            Bitmap resizedbitmap = Bitmap.createBitmap(image, 0,0, r.width(), r.height());
			
		    images.add(resizedbitmap);
		}
		//ImageIO.write(images.get(1), "png", new File("C:/Users/Kevin/Desktop/test.png"));
		return images;
	}
	
	private Rect getBoxedImage(int x, int y, int[] colorToCompare) {
		bottomRight = new Point(x,y);
		if(Highlighter.isPixelSimilarColor(image, x, y, colorToCompare)) {
			checkAdjacentPixels(x,y,colorToCompare);
		}
		int width = (int) (bottomRight.x - x);
		int height = (int) (bottomRight.y - y);

		if(width * height >= AREA_THRESHOLD) {
			return new Rect(x,y,
					width,
					height);
		}else {
			return null;
		}
	}
	
	private void checkAdjacentPixels(int x, int y, int[] colorToCompare) {
		Queue<Point> q = new LinkedList<Point>();
		q.add(new Point(x,y));
		checkedPoints.add(new Point(x,y));
		while(q.size() > 0) {
			if(q.size()%1000 ==0) {
				System.out.println(q.peek().x + " " + q.peek().y);
			}
			Point p0 = q.poll();
			setBottomRight(p0.x,p0.y);
			
			Point p = new Point(p0.x+1,p0.y);
			if(Highlighter.isPixelSimilarColor(image, p0.x+1, p0.y, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				checkedPoints.add(p); 
			}
			p = new Point(p0.x,p0.y+1);
			if(Highlighter.isPixelSimilarColor(image, p0.x, p0.y+1, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				checkedPoints.add(p); 
			}
			p = new Point(p0.x,p0.y-1);
			if(Highlighter.isPixelSimilarColor(image, p0.x, p0.y-1, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				checkedPoints.add(p); 
			}
			p = new Point(p0.x-1,p0.y);
			if(Highlighter.isPixelSimilarColor(image, p0.x-1, p0.y, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				//System.out.println(x + " " + y + " " + Highlighter.isPixelSimilarColor(ip, x-1, y, colorToCompare));
				checkedPoints.add(p); 
			}
		}
	}
	
	private void recursivelyCheckAdjacentPixels(int x, int y, int[] colorToCompare) {
		Point p = new Point(x+1,y);
		if(Highlighter.isPixelSimilarColor(image, x+1, y, colorToCompare) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x+1, y, colorToCompare);
		}
		p = new Point(x,y+1);
		if(Highlighter.isPixelSimilarColor(image, x, y+1, colorToCompare) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x, y+1, colorToCompare);
		}
		p = new Point(x,y-1);
		if(Highlighter.isPixelSimilarColor(image, x, y-1, colorToCompare) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x, y-1, colorToCompare);
		}
		p = new Point(x-1,y);
		if(Highlighter.isPixelSimilarColor(image, x-1, y, colorToCompare) && !checkedPoints.contains(p)) {
			//System.out.println(x + " " + y + " " + Highlighter.isPixelSimilarColor(ip, x-1, y, colorToCompare));
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x-1, y, colorToCompare);
		}
		setBottomRight(x,y);
	}
	
	private void setBottomRight(int x, int y) {
		if(x > bottomRight.x) {
			bottomRight.x = x;
		}
		if(y > bottomRight.y) {
			bottomRight.y = y;
		}
	}
	
}
