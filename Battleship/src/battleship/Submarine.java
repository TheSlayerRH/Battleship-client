package battleship;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Submarine {
	
	private SubmarineType type;
	private int size;
	private int[] topLeftLocation, bottomRightLocation;
	private boolean[] hits; 
	private int[][] locations;
	private int state; //0 horizontal and 1 vertical;
	private boolean attached, onBoard;
	private BufferedImage[] image;
	
	private static BufferedImage[][] submarinesImages;
	private static BufferedImage fireImage;
	static{
		try{
			final File file = new File("../../images/ship5.png");
			if(file.exists())
				System.out.println(file.getPath() + ": exists");
			else
				System.out.println(file.getPath() + ": doesn't exists");
			
			submarinesImages = new BufferedImage[5][2];
			for(int i=submarinesImages.length-1; i>=0; i--){
				submarinesImages[i] = new BufferedImage[]{ImageIO.read(Main.class.getClassLoader().getResourceAsStream("images/ship" + (i+1) + ".png")),//new File("src\\images\\ship" + (i+1) + ".png")),
						ImageIO.read(Main.class.getClassLoader().getResourceAsStream("images/ship" + (i+1) + "_vertical.png"))};//new File("src\\images\\ship" + (i+1) + "_vertical.png"))};
			}
			
			fireImage = ImageIO.read(Main.class.getClassLoader().getResourceAsStream("images/fire3.png"));//new File("src\\images\\fire3.png")); -this format doesn't work in the jar file.
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	};
	
	public Submarine(SubmarineType type) {
		this.type = type;
		size = type.getSize();
		image = submarinesImages[size-1];
		locations = new int[size][2];
		hits = new boolean[size];
		
		state = 0;
		attached = false;
		onBoard = false;
	}
	
	public SubmarineType getType() {
		return type;
	}
	
	public int getSize(){
		return size;
	}
	
	public int[][] getLocations(){
		return locations;
	}
	
	public int[] getTopLeftLocation(){
		return topLeftLocation;
	}
	
	public int[] getBottomRightLocation(){
		return bottomRightLocation;
	}
	
	public boolean isAttached(){
		return attached;
	}
	
	public boolean isOnBoard(){
		return onBoard;
	}
	
	public BufferedImage getImage(){
		return image[state];
	}
	
	public void setState(boolean vertical){
		if(vertical)
			this.state = 1;
		else
			this.state = 0;
	}
	
	public boolean isVertical(){
		if(state == 1)
			return true;
		
		return false;
	}
	
	public void setAttached(boolean attached){
		this.attached = attached;
	}
	
	public void setOnBoard(boolean onBoard){
		this.onBoard = onBoard;
	}
	
	public void setLocations(int[] leftBottomLocation){
		locations[0] = leftBottomLocation;
		for(int i=1; i<locations.length; i++){
			if(state == 1){
				locations[i] = new int[]{leftBottomLocation[0], leftBottomLocation[1] + i};
			}else{
				locations[i] = new int[]{leftBottomLocation[0] + i, leftBottomLocation[1]};
			}
		}
	}
	
	public void setHit(int shipPartIndex){
		hits[shipPartIndex] = true;
	}
	
	/**
	 * @param x - the x coordinate on the board;
	 * @param y - the y coordinate on the board;
	 * @return the index of the part of the ship that is in this x and y; -1 if non of the ship parts is on that location;
	 */
	public int locationOnShip(int x, int y){ //If returns -1 the location is not on the ship;
		for(int i=0; i<locations.length; i++){
			int currentLocationX = locations[i][0];
			int currentLocationY = locations[i][1];
			
			if(currentLocationX == x && currentLocationY == y){
				return i;
			}
		}
		
		return -1;
	}
	
	public void drawSubmarine(Graphics g, int x1, int y1, int x2, int y2, int cropX1, int cropY1, int cropX2, int cropY2){
		if(image!=null){
			Graphics2D gr = (Graphics2D)g;
			topLeftLocation = new int[]{x1, y1};
			bottomRightLocation = new int[]{x2, y2};
			
			gr.drawImage(image[state], x1, y1, x2, y2, cropX1, cropY1, cropX2, cropY2, null);
			
			for(int i=0; i<hits.length; i++){
				if(hits[i]){
					if(state == 1){
						int squareSize = (y2-y1)/size;
						int drawY1 = y1 + i*squareSize, drawY2 = y1 + (i+1)*squareSize;
						gr.drawImage(fireImage, x1, drawY1, x2, drawY2, 0, 0, fireImage.getWidth(), fireImage.getHeight(), null);
					}else{
						int squareSize = (x2-x1)/size;
						int drawX1 = x1 + i*squareSize, drawX2 = x1 + (i+1)*squareSize;
						gr.drawImage(fireImage, drawX1, y1, drawX2, y2, 0, 0, fireImage.getWidth(), fireImage.getHeight(), null);
					}
				}
			}
		}
	}
}
