package highlighter;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import com.google.cloud.vision.v1.Vertex;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.Opener;
import ij.process.ImageProcessor;

public class ImageDetector {
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

}
