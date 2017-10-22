import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Card {
  
	private static final String SIDE_SEPARATOR = ";";
	private String path;
	private String fileName;
	private ArrayList<String> frontText = new ArrayList<String>();
	private ArrayList<String> backText = new ArrayList<String>();
	private File file;
	private File pathFile;
	private BufferedImage frontImage;
	private BufferedImage backImage;
	
	public Card (String path, String name) {
		this.path = path;
		this.fileName = name;
		pathFile = new File(path);
		pathFile.mkdirs();
		file = new File(path + "\\" + name);
	}
	
	public Card (String path, String name, 
			BufferedImage frontImage, 
			BufferedImage backImage) {
		this.path = path;
		this.fileName = name;
		pathFile = new File(path);
		pathFile.mkdirs();
		file = new File(path + "\\" + name + ".txt");
		this.frontImage = frontImage;
		this.backImage = backImage;
	}
	
	public void writeCard(ArrayList<String> frontWrite, ArrayList<String> backWrite,
			BufferedImage fImage, BufferedImage bImage) {
		if(fImage != null) {
			try {
				ImageIO.write((RenderedImage) fImage, "png", 
						new File(path + "\\" + fileName + "FrontImage"));
			} catch (IOException e) {
				System.out.println("Failed to find image file");
			}
			frontImage = fImage;
		}
		if(bImage != null) {
			try {
				ImageIO.write((RenderedImage) bImage, "png", 
						new File(path + "\\" + fileName + "BackImage"));
			} catch (IOException e) {
				System.out.println("Failed to find image file");
			}
			backImage = bImage;
		}
		writeCard(frontWrite, backWrite);
	}
		
	public void writeCard(ArrayList<String> frontWrite, ArrayList<String> backWrite) {
		
		if(!file.exists()) {
			try {
				file.createNewFile();
				System.out.println("Created new file");
			} catch (IOException e) {
				System.out.println("Failed to create file");
			}
		}
		file.setWritable(true);
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path + "\\" + fileName + ".txt");
		} catch (IOException e) {
			System.out.println("Failed to find file");
		}
		
		frontWrite.add(SIDE_SEPARATOR);
		for(String line: frontWrite) {
			System.out.println("front:" + line);
			writer.println(line);
		}
		for(String line: backWrite) {
			System.out.println("back:" + line);
			writer.println(line);
		}
		writer.close();
		updateCard();
		makeAnkiCard();
	}
	
	public void updateCard() {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(
					new FileReader(path + "\\" + fileName + ".txt"));
		} catch (FileNotFoundException e1) {
			System.out.println("Failed to find file");
		}
		
		String line = null;
		ArrayList<String> card = new ArrayList<String>();
		
        try {
            while((line = bufferedReader.readLine()) != null) {
                card.add(line);
            }   
            bufferedReader.close();         
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
		int index = card.indexOf(SIDE_SEPARATOR);
		System.out.println(index);
		for (int i = 0; i < index; i++) {
			frontText.add(card.remove(0));
		}
		if(index > -1) {
			card.remove(0);
		}
		for(String s: card) {
			backText.add(s);
		}
		
		try {
			frontImage = ImageIO.read(new File(path + "\\" + fileName + "FrontImage.png"));
		}
		catch(IOException e) {
			System.out.println("Failed to find file.");
		}
		try {
			backImage = ImageIO.read(new File(path + "\\" + fileName + "backImage.png"));
		}
		catch(IOException e) {
			System.out.println("Failed to find file.");
		}
	}
	
	private void makeAnkiCard() {
		File ankiFile = new File(path + "\\" + fileName + "Anki.txt");
		try {
			ankiFile.createNewFile();
			System.out.println("Created new file");
		} catch (IOException e) {
			System.out.println("Failed to create file");
		}
		
		ankiFile.setWritable(true);
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path + "\\" + fileName + "Anki.txt");
		} catch (IOException e) {
			System.out.println("Failed to find file");
		}
		
		for(String line: frontText) {
			writer.print(line);
		}
		writer.println("");
		for(String line: backText) {
			writer.print(line);
		}
		
		writer.close();
	}
	
	public ArrayList<String> getCardFront() {
		return frontText;
	}
	
	public ArrayList<String> getCardBack() {
		return backText;
	}
	
	public BufferedImage getFrontImage() {
		return frontImage;
	}
	
	public BufferedImage getBackImage() {
		return backImage;
	}
	
}
