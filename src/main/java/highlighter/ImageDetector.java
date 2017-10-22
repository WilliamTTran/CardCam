package highlighter;

import java.awt.Color;
import java.awt.Point;
<<<<<<< HEAD
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
=======
>>>>>>> 791e964ea7e8fde063b48418b153d67f99fd1ac7
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

import com.google.cloud.vision.v1.Vertex;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.Opener;
import ij.process.ImageProcessor;

public class ImageDetector {
<<<<<<< HEAD
	private static final double HIGHLIGHT_THRESHOLD = 0.30; //proportion of box which needs to be colored to stop being considered an image
	private static final int AREA_THRESHOLD = 5000; //min image area
	
	private ArrayList<Point> checkedPoints = null;
	private ArrayList<Rectangle> detectedImages = null;
	private Point bottomRight = null;
	private ImageProcessor ip;
	
	static int count = 0;
	public ArrayList<BufferedImage> detectImages(String filePath, int[] colorToCompare) throws IOException {
		checkedPoints = new ArrayList<Point>();
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
					  Rectangle boxedImage = getBoxedImage(x,y, colorToCompare); //will be null unless it finds a new object
					  	if(boxedImage != null){
					  		if(Highlighter.highlightPercentage(ip, boxedImage, colorToCompare) < HIGHLIGHT_THRESHOLD){
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
		ImageIO.write(images.get(1), "png", new File("C:/Users/Kevin/Desktop/test.png"));
		return images;
	}
	
	private Rectangle getBoxedImage(int x, int y, int[] colorToCompare) {
		bottomRight = new Point(x,y);
		if(Highlighter.isPixelSimilarColor(ip, x, y, colorToCompare)) {
			checkAdjacentPixels(x,y,colorToCompare);
		}
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
			if(Highlighter.isPixelSimilarColor(ip, p0.x+1, p0.y, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				checkedPoints.add(p); 
			}
			p = new Point(p0.x,p0.y+1);
			if(Highlighter.isPixelSimilarColor(ip, p0.x, p0.y+1, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				checkedPoints.add(p); 
			}
			p = new Point(p0.x,p0.y-1);
			if(Highlighter.isPixelSimilarColor(ip, p0.x, p0.y-1, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				checkedPoints.add(p); 
			}
			p = new Point(p0.x-1,p0.y);
			if(Highlighter.isPixelSimilarColor(ip, p0.x-1, p0.y, colorToCompare) && !checkedPoints.contains(p)) {
				q.add(p);
				//System.out.println(x + " " + y + " " + Highlighter.isPixelSimilarColor(ip, x-1, y, colorToCompare));
				checkedPoints.add(p); 
			}
		}
		
		
	}
	
	private void recursivelyCheckAdjacentPixels(int x, int y, int[] colorToCompare) {
		Point p = new Point(x+1,y);
		if(Highlighter.isPixelSimilarColor(ip, x+1, y, colorToCompare) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x+1, y, colorToCompare);
		}
		p = new Point(x,y+1);
		if(Highlighter.isPixelSimilarColor(ip, x, y+1, colorToCompare) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x, y+1, colorToCompare);
		}
		p = new Point(x,y-1);
		if(Highlighter.isPixelSimilarColor(ip, x, y-1, colorToCompare) && !checkedPoints.contains(p)) {
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x, y-1, colorToCompare);
		}
		p = new Point(x-1,y);
		if(Highlighter.isPixelSimilarColor(ip, x-1, y, colorToCompare) && !checkedPoints.contains(p)) {
			//System.out.println(x + " " + y + " " + Highlighter.isPixelSimilarColor(ip, x-1, y, colorToCompare));
			checkedPoints.add(p); 
			recursivelyCheckAdjacentPixels(x-1, y, colorToCompare);
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
	
=======
    private static final double HIGHLIGHT_THRESHOLD = 0.30; //proportion of box which needs to be colored to stop being considered an image
    private static final int AREA_THRESHOLD = 5000; //min image area

    private HashSet<Point> checkedPoints = null;
    private ArrayList<Roi> detectedImages = new ArrayList<Roi>();
    private Point bottomRight = null;
    private ImageProcessor ip;

    public void detectImages(String filePath, Color colorToCompare) {
        checkedPoints = new HashSet<Point>();
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
                for(Roi roi : detectedImages) {
                    if(roi.contains(x, y)) {
                        contains = true;
                        break;
                    }
                }
                if(!contains && !checkedPoints.contains(new Point(x,y))) {
                    Roi boxedImage = getBoxedImage(x,y, colorToCompare); //will be null unless it finds a new object
                    if(boxedImage != null){
                        System.out.println(Highlighter.highlightPercentage(ip, boxedImage, colorToCompare));
                        System.out.println(boxedImage);
                        if(Highlighter.highlightPercentage(ip, boxedImage, colorToCompare) < HIGHLIGHT_THRESHOLD){
                            detectedImages.add(boxedImage);
                        }
                    }
                }
            }
        }
        System.out.println(detectedImages.size());
    }

    private Roi getBoxedImage(int x, int y, Color colorToCompare) {
        bottomRight = new Point(x,y);
        recursivelyCheckAdjacentPixels(x,y,colorToCompare);
        double width = bottomRight.getX() - x;
        double height = bottomRight.getY() - y;
        if(width * height >= AREA_THRESHOLD) {
            return new Roi(x,y,
                    width,
                    height);
        }else {
            return null;
        }
    }

    private void recursivelyCheckAdjacentPixels(int x, int y, Color colorToCompare) {
        Point p = new Point(x-1,y);
        if(Highlighter.isPixelSimilarColor(ip, x-1, y, colorToCompare) && !checkedPoints.contains(p)) {
            checkedPoints.add(p);
            recursivelyCheckAdjacentPixels(x-1, y, colorToCompare);
        }
        p = new Point(x+1,y);
        if(Highlighter.isPixelSimilarColor(ip, x+1, y, colorToCompare) && !checkedPoints.contains(p)) {
            checkedPoints.add(p);
            recursivelyCheckAdjacentPixels(x+1, y, colorToCompare);
        }
        p = new Point(x,y+1);
        if(Highlighter.isPixelSimilarColor(ip, x, y+1, colorToCompare) && !checkedPoints.contains(p)) {
            checkedPoints.add(p);
            recursivelyCheckAdjacentPixels(x, y+1, colorToCompare);
        }
        p = new Point(x,y-1);
        if(Highlighter.isPixelSimilarColor(ip, x, y-1, colorToCompare) && !checkedPoints.contains(p)) {
            checkedPoints.add(p);
            recursivelyCheckAdjacentPixels(x, y-1, colorToCompare);
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

>>>>>>> 791e964ea7e8fde063b48418b153d67f99fd1ac7
}
