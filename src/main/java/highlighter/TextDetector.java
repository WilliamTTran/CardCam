package highlighter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

public class TextDetector {
	private static final double HIGHLIGHT_THRESHOLD = 0.45; //proportion of box which needs to be colored to be considered 'highlighted text'
	private static final double NEW_LINE_THRESHOLD_MULTIPLIER = 0.5; //proportion of medianPixelHeight before a word is considered to be on a new line
	private static final double NEW_BLOCK_THRESHOLD_MULTIPLIER = 2.0; //proportion of medianPixelHeight before a line is considered to be a new block

  public static void main(String... args) throws Exception {
	  HashMap<String, String> blah = new HashMap<String, String>();
	  blah.put("GOOGLE_APPLICATION_CREDENTIALS", "C:/Users/Kevin/Desktop/gcred.json");
	  setEnv(blah);
	  //System.out.println(detectText(Highlighter.TEST_IMAGE_FILEPATH, Highlighter.BLUE));
	  //System.out.println(detectText(Highlighter.TEST_IMAGE_FILEPATH, Highlighter.PINK));
	  //ImageDetector id = new ImageDetector();
	  //System.out.println(id.detectImages(, Highlighter.PINK));
  }
  
  public static List<List<String>> getHighlightedText(Bitmap image, List<EntityAnnotation> annotations, int[] colorToCompare) {
	  int medianPixelHeight = getMedianPixelHeight(annotations);
	  ArrayList<List<String>> blocks = new ArrayList<List<String>>();
	  ArrayList<String> textBlock = new ArrayList<String>();
	  String line = "";
	  int tempY = -1;
	  //goes through left to right, top to bottom
	  for(EntityAnnotation ea : annotations) {
		  /*
		   * if y-distance is over new line threshold, make a new line;
		   * if y-distance between words is over new block threshold, make a new block;
		   * otherwise, concatenate;
		   */
		  if(Highlighter.polyToRect(ea.getBoundingPoly()) != null) {
              Log.i("EA SPORTS", ea.toString());
              if (Highlighter.highlightPercentage(image, Highlighter.polyToRect(ea.getBoundingPoly()), colorToCompare) > HIGHLIGHT_THRESHOLD) {
                  if (tempY == -1) {
                      tempY = (int) centerOfPoly(ea.getBoundingPoly()).y;
                  }
              }
              int yDistance = (int) Math.abs(tempY - centerOfPoly(ea.getBoundingPoly()).y);
              if (yDistance > medianPixelHeight * NEW_LINE_THRESHOLD_MULTIPLIER && line.length() > 0) {
                  textBlock.add(line.substring(0, line.length() - 1)); //remove trailing space
                  line = "";
                  tempY = -1;
              }
              if (yDistance > medianPixelHeight * NEW_BLOCK_THRESHOLD_MULTIPLIER && textBlock.size() > 0) {
                  //add to block list, make new block
                  blocks.add(textBlock);
                  textBlock = new ArrayList<String>();
              }

              if (Highlighter.highlightPercentage(image, Highlighter.polyToRect(ea.getBoundingPoly()), colorToCompare) > HIGHLIGHT_THRESHOLD) {
                  line += ea.getDescription() + " ";
              }
          }
	  }
	  return blocks;
  }
  
  private static int getMedianPixelHeight(List<EntityAnnotation> annotations) {
	  ArrayList<Integer> heights = new ArrayList<Integer>();
	  for(EntityAnnotation ea : annotations) {
          Log.i("BOUNDING POLY", ea.getBoundingPoly().toString());
          Log.i("VERTICES", ea.getBoundingPoly().getVertices().toString());
          Log.i("BOTTOM LEFT", "" + ea.getBoundingPoly().getVertices().get(Highlighter.BOTTOM_LEFT).getY());
          try {
              heights.add(ea.getBoundingPoly().getVertices().get(Highlighter.BOTTOM_LEFT).getY() - ea.getBoundingPoly().getVertices().get(Highlighter.TOP_LEFT).getY());

          }catch(Exception e){}
      }
	  Collections.sort(heights); //shortest to tallest
	  return heights.get(heights.size()/2);
	  
  }
  
  private static Point centerOfPoly(BoundingPoly bp) {
	  return new Point((bp.getVertices().get(Highlighter.TOP_LEFT).getX()+bp.getVertices().get(Highlighter.TOP_RIGHT).getX())/2,
			  (bp.getVertices().get(Highlighter.TOP_LEFT).getY()+bp.getVertices().get(Highlighter.BOTTOM_LEFT).getY())/2);
  }

  //https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
  public static void setEnv(Map<String, String> newenv) throws Exception {
	  try {
	    Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	    Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	    theEnvironmentField.setAccessible(true);
	    Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	    env.putAll(newenv);
	    Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	    theCaseInsensitiveEnvironmentField.setAccessible(true);
	    Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
	    cienv.putAll(newenv);
	  } catch (NoSuchFieldException e) {
	    Class[] classes = Collections.class.getDeclaredClasses();
	    Map<String, String> env = System.getenv();
	    for(Class cl : classes) {
	      if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	        Field field = cl.getDeclaredField("m");
	        field.setAccessible(true);
	        Object obj = field.get(env);
	        Map<String, String> map = (Map<String, String>) obj;
	        map.clear();
	        map.putAll(newenv);
	      }
	    }
	  }
	}

  /*public static List<List<String>> detectText(Bitmap image, int[] colorToCompare) throws Exception, IOException {
	  List<AnnotateImageRequest> requests = new ArrayList<>();

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
      byte[] bitmapdata = outputStream.toByteArray();
      InputStream is = new ByteArrayInputStream(bitmapdata);

	  ByteString imgBytes = ByteString.readFrom(is);

	  Image img = Image.newBuilder().setContent(imgBytes).build();
	  Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
	  AnnotateImageRequest request =
	      AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
	  requests.add(request);

	  try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
	    BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
	    List<AnnotateImageResponse> responses = response.getResponsesList();

	    for (AnnotateImageResponse res : responses) {
	      if (res.hasError()) {
	        System.out.printf("Error: %s\n", res.getError().getMessage());
	        return null;
	      }
		  
	      /*
	      // For full list of available annotations, see http://g.co/cloud/vision/docs
	      for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
	        out.printf("Text: %s\n", annotation.getDescription());
	        out.printf("%s\n", annotation.getBoundingPoly());
	      }

		  return getHighlightedText(image, res.getTextAnnotationsList(), colorToCompare);
	    }
	  }
	  return null;
	}*/
  
}