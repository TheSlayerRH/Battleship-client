package battleship;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Board extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1686242487331475184L;
	private final int squareSize;
	private Submarine[] submarines;
	private int boardWidth, boardHeight;
	private int[][] board; //Enemy board: 0 = empty square, 1 = hit, -1 = miss;
	private boolean enemyBoard;
	private int shipAttached;
	private int shipAttachedSize;
	private int padding;
	private boolean attachedOnBoard;
	private boolean myTurn;
	private List<int[]> hits, misses;
	private static BufferedImage missImage, hitImage;
	static{
		try{
			missImage = ImageIO.read(Main.class.getClassLoader().getResourceAsStream("images/miss.png"));//new File("src\\images\\miss.png"));
			hitImage = ImageIO.read(Main.class.getClassLoader().getResourceAsStream("images/hit.png"));//new File("src\\images\\hit.png"));
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	public Board(Point size, int squareSize, int variation, Submarine[] submarines, boolean enemyBoard, int padding, int shipAttachedSize){
		this.squareSize = squareSize;
		this.submarines = submarines;
		this.boardWidth = size.x;
		this.boardHeight = size.y;
		this.enemyBoard = enemyBoard;
		this.padding = padding;
		this.shipAttachedSize = shipAttachedSize;
		resetGame();
	}
	
	public void resetGame(){
		//this.submarines = submarines;
		this.attachedOnBoard = false;
		this.myTurn = false;
		hits = new ArrayList<int[]>();
		misses = new ArrayList<int[]>();
		
		board = new int[boardHeight][boardWidth];
		this.shipAttached = -1;
	}
	
	public void addHitOrMiss(int x, int y, boolean isHit){
		int[] shoot = {x, y};
		if(isHit){
			hits.add(shoot);
			board[y][x] = 1;
		}else{
			misses.add(shoot);
			board[y][x] = -1;
		}
	}
	
	public boolean isEnemyBoard(){
		return enemyBoard;
	}
	
	public void setShipAttached(int shipAttached){
		this.shipAttached = shipAttached;
	}
	
	public int[][] getBoard(){
		return board;
	}
	
	public void setBoard(int x, int y, int number){
		board[y][x] = number;
	}
	
	public void setAttachedOnBoard(boolean attachedOnBoard){
		this.attachedOnBoard = attachedOnBoard;
	}
	
	public void setMyTurn(boolean myTurn){
		this.myTurn = myTurn;
	}
	
	public Submarine[] getSubmarines(){
		return submarines;
	}
	
	public List<int[]> getHits() {
		return hits;
	}

	public List<int[]> getMisses() {
		return misses;
	}

	public boolean isLegalArrangement(){
		for(int a=0; a<submarines.length; a++){
			int[][] locationsA = submarines[a].getLocations();
			for(int b=0; b<submarines.length; b++){
				if(b == a)
					continue;
				
				int[][] locationsB = submarines[b].getLocations();
				
				if(isNear(locationsA, locationsB))
					return false;
			}
		}
		
		return true;
	}
	
	private boolean isNear(int[][] locations1, int[][] locations2){
		for(int a=0; a<locations1.length; a++){
			int[] location1 = locations1[a];
			for(int b=0; b<locations2.length; b++){
				int[] location2 = locations2[b];
				
				if(Math.abs(location1[0] - location2[0]) <= 1 && Math.abs(location1[1] - location2[1]) <= 1){
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gr = (Graphics2D)g;
		
		
		//Draw letters and numbers in a thiner stroke:
		//Letters:
		gr.setFont(new Font("Arial", gr.getFont().getStyle(), /*gr.getFont().getSize() + 3*/squareSize/2));
		gr.drawLine(0, squareSize, 0, (boardHeight + 1)*squareSize);
		for(int y=1; y<=boardHeight+1; y++){
			int currentY = y*squareSize;
			gr.drawLine(0, currentY, squareSize, currentY);
			gr.drawString("" + (char)(y + 64), squareSize/3, currentY + 3*squareSize/4);
		}
		//Numbers:
		gr.drawLine(squareSize, 0, (boardWidth + 1)*squareSize, 0);
		for(int x=1; x<=boardWidth+1; x++){
			int currentX = x*squareSize;
			gr.drawLine(currentX, 0, currentX, squareSize);
			gr.drawString(Integer.toString(x), currentX + squareSize/3, 3*squareSize/4);
		}
		
		gr.setStroke(new BasicStroke(2)); //Thicken the stroke;
		//Make the board other lines:
		for(int y=1; y<=boardHeight+1; y++){
			int currentY = y*squareSize;
			gr.drawLine(squareSize, currentY, (boardWidth + 1)*squareSize, currentY);
		}
		for(int x=1; x<=boardWidth+1; x++){
			int currentX = x*squareSize;
			gr.drawLine(currentX, squareSize, currentX, (boardHeight + 1)*squareSize);
		}
		//---------------------------------------------------------------------------------------------
		//Draw ships stuff here:
		if(!enemyBoard){
			for(int i=0; i<submarines.length; i++){
				Submarine currentSubmarine = submarines[i];
				if(shipAttached != i && currentSubmarine.isOnBoard()){
					int[] firstLocation = currentSubmarine.getLocations()[0];
					
					int drawX = firstLocation[0]*squareSize + padding/2;
					int drawY = firstLocation[1]*squareSize + padding/2;
					
					if(currentSubmarine.isVertical()){
						currentSubmarine.drawSubmarine(gr, drawX, drawY, drawX + squareSize - padding,
								drawY + squareSize*currentSubmarine.getSize() - padding, 0, 0, currentSubmarine.getImage().getWidth(), currentSubmarine.getImage().getHeight());
					}else{
						currentSubmarine.drawSubmarine(gr, drawX, drawY, drawX + shipAttachedSize*currentSubmarine.getSize() - padding,
								drawY + shipAttachedSize - padding, 0, 0, currentSubmarine.getImage().getWidth(), currentSubmarine.getImage().getHeight());
					}
				}
			}
		}else if(myTurn){
			int mouseX = (int) (MouseInfo.getPointerInfo().getLocation().getX() - this.getLocationOnScreen().getX());
			int mouseY = (int) (MouseInfo.getPointerInfo().getLocation().getY() - this.getLocationOnScreen().getY());
			
			if(mouseX > squareSize && mouseY > squareSize){
				int x = mouseX/squareSize;
				if((double)mouseX/squareSize%1 == 0) x = mouseX/squareSize - 1; //x coordinate on the board;
				int y = mouseY/squareSize; //y coordinate on the board;
				if((double)mouseY/squareSize%1 == 0) y = mouseY/squareSize - 1;
				
				if(x< boardWidth+1 && y < boardHeight+1 && board[y-1][x-1] == 0){
					gr.setColor(Color.YELLOW);
					gr.fillRect(x * squareSize + 1, y * squareSize + 1, squareSize - 2, squareSize - 2);
				}
			}
		}
		//---------------------------------------------------------------------------------------------
		if(shipAttached != -1 && !attachedOnBoard){
			drawAttachedShip(gr);
		}
		//---------------------------------------------------------------------------------------------
		drawHitsAndMisses(gr);
	}
	
	private void drawHitsAndMisses(Graphics2D gr) {
		if(hits.size() > 0){
			for(int[] shoot: hits){
				int x = shoot[0] + 1, y = shoot[1] + 1;
				gr.drawImage(hitImage, x*squareSize+1, y*squareSize+1, (x+1)*squareSize-2,
						(y+1)*squareSize-2, 0, 0, hitImage.getWidth(), hitImage.getHeight(), null);
			}
		}
		if(misses.size() > 0){
			for(int[] shoot: misses){
				int x = shoot[0] + 1, y = shoot[1] + 1;
				gr.drawImage(missImage, x*squareSize+1, y*squareSize+1, (x+1)*squareSize-2,
						(y+1)*squareSize-2, 0, 0, missImage.getWidth(), missImage.getHeight(), null);
			}
		}
	}

	private void drawAttachedShip(Graphics2D gr) {
		//Get mouse coordinates on the ShipsPanel:
		int mouseX = (int) (MouseInfo.getPointerInfo().getLocation().getX() - this.getLocationOnScreen().getX());
		int mouseY = (int) (MouseInfo.getPointerInfo().getLocation().getY() - this.getLocationOnScreen().getY());
		
		Submarine currentSubmarine = submarines[shipAttached];
		
		if(currentSubmarine.isVertical()){
			if(enemyBoard || (mouseY >= getHeight() - shipAttachedSize * (currentSubmarine.getSize()-1)) || mouseX <= squareSize || mouseY <= squareSize){
				int x = mouseX - (shipAttachedSize / 2) + padding;
				int y = mouseY - (shipAttachedSize / 2) + padding;
				
				currentSubmarine.drawSubmarine(gr, x, y, x + shipAttachedSize - padding,
						y + shipAttachedSize*currentSubmarine.getSize() - padding, 0, 0, currentSubmarine.getImage().getWidth(), currentSubmarine.getImage().getHeight());
			}else{
				int x = mouseX/squareSize;
				if((double)mouseX/squareSize%1 == 0) x = mouseX/squareSize - 1; //x coordinate on the board;
				int y = mouseY/squareSize; //y coordinate on the board;
				if((double)mouseY/squareSize%1 == 0) y = mouseY/squareSize - 1;
				
				int drawX = x*squareSize + padding/2;
				int drawY = y*squareSize + padding/2;
				
				currentSubmarine.drawSubmarine(gr, drawX, drawY, drawX + shipAttachedSize - padding,
						drawY + shipAttachedSize*currentSubmarine.getSize() - padding, 0, 0, currentSubmarine.getImage().getWidth(), currentSubmarine.getImage().getHeight());
			}
		}else{
			if(enemyBoard || (mouseX >= getWidth() - shipAttachedSize * (currentSubmarine.getSize()-1)) || mouseX <= squareSize || mouseY <= squareSize || mouseY >= getY() + getHeight() - padding){
				//Set the x and y for the drawn ship:
				int x = mouseX - (shipAttachedSize / 2) + padding;
				int y = mouseY - (shipAttachedSize / 2) + padding;
				if(mouseX > -shipAttachedSize * (currentSubmarine.getSize()-1) /*+ squareSize*/ || (enemyBoard && mouseY > squareSize)){
					int drawnPartX1=0, drawnPartY1=0, drawnPartX2=currentSubmarine.getImage().getWidth(), drawnPartY2=currentSubmarine.getImage().getHeight();
					if(x < 0){
						drawnPartX1 = -x;
					}
					if(y < 0){
						drawnPartY1 = -y;
					}
					
					currentSubmarine.drawSubmarine(gr, x, y, x + shipAttachedSize*currentSubmarine.getSize() - padding,
							y + shipAttachedSize - padding, drawnPartX1, drawnPartY1, drawnPartX2, drawnPartY2);
				}
			}else{
				int x = mouseX/squareSize;
				if((double)mouseX/squareSize%1 == 0) x = mouseX/squareSize - 1; //x coordinate on the board;
				int y = mouseY/squareSize; //y coordinate on the board;
				if((double)mouseY/squareSize%1 == 0) y = mouseY/squareSize - 1;
				
				int drawX = x*squareSize + padding/2;
				int drawY = y*squareSize + padding/2;
				
				currentSubmarine.drawSubmarine(gr, drawX, drawY, drawX + shipAttachedSize*currentSubmarine.getSize() - padding,
						drawY + shipAttachedSize - padding, 0, 0, currentSubmarine.getImage().getWidth(), currentSubmarine.getImage().getHeight());
			}
		}
	}
	
}
