package battleship;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;

import javax.swing.JPanel;

public class ShipsPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1386024251967933265L;
	private final int size, padding; //size = size to draw each square of the ships;
	private Color backgroundColor;
	private int shipAttached;
	private Submarine[] submarines;
	private boolean attachedOnBoard;
	
	public ShipsPanel(int variation, int size, int padding, Submarine[] submarines, Color backgroundColor){
		this.size = size;
		this.padding = padding;
		resetGame();
		
		this.backgroundColor = backgroundColor;
		setBackground(backgroundColor);
		
		this.submarines = submarines;
	}
	
	public void resetGame(){
		this.shipAttached = -1;
		this.attachedOnBoard = false;
	}

	public void setShipAttached(int shipAttached){
		this.shipAttached = shipAttached;
	}
	
	public void setAttachedOnBoard(boolean attachedOnBoard){
		this.attachedOnBoard = attachedOnBoard;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D gr = (Graphics2D)g;
		gr.setColor(backgroundColor);
		gr.fillRect(0, 0, getWidth(), getHeight());
		gr.setColor(Color.BLACK);
		//gr.clearRect(0, 0, getWidth(), getHeight());
		
		Font headlinesFont = new Font(Font.SERIF, Font.BOLD, 46);		
		gr.setFont(headlinesFont);
		boolean allOnBoard = true;
		for(int i=0; i<submarines.length; i++){
			if(!submarines[i].isOnBoard()){
				allOnBoard = false;
				break;
			}
		}
		
		if(shipAttached != -1 || !allOnBoard)
			gr.drawString("Your ships", padding, padding + headlinesFont.getSize());
		
		final int distanceFromTop = 100;
		for(int i=0; i<submarines.length; i++){
			Submarine currentSubmarine = submarines[i];
			if(shipAttached != i && !currentSubmarine.isOnBoard()){
				currentSubmarine.drawSubmarine(gr, padding, distanceFromTop + (padding + size)*i, size*currentSubmarine.getSize(),
						distanceFromTop + (padding + size)*i + size - padding, 0, 0, currentSubmarine.getImage().getWidth(), currentSubmarine.getImage().getHeight());
			}
		}
		if(shipAttached != -1 && !attachedOnBoard){
			drawAttachedShip(gr);
		}
		
	}

	private void drawAttachedShip(Graphics2D gr) {
		//Get mouse coordinates on the ShipsPanel:
		int mouseX = (int) (MouseInfo.getPointerInfo().getLocation().getX() - this.getLocationOnScreen().getX());
		int mouseY = (int) (MouseInfo.getPointerInfo().getLocation().getY() - this.getLocationOnScreen().getY());
		
		Submarine currentSubmarine = submarines[shipAttached];
		
		//Set the x and y for the drawn ship:
		int x = mouseX - (size / 2) + padding;
		int y = mouseY - (size / 2) + padding;

		if(currentSubmarine.isVertical()){
			currentSubmarine.drawSubmarine(gr, x, y, x + size - padding,
					y + size*currentSubmarine.getSize() - padding, 0, 0, currentSubmarine.getImage().getWidth(), currentSubmarine.getImage().getHeight());
		}else{
			if(mouseX >= -size * (currentSubmarine.getSize()-1) && y > -currentSubmarine.getImage().getHeight() || mouseY < size + padding){
				int drawnPartX1=0, drawnPartY1=0, drawnPartX2=currentSubmarine.getImage().getWidth(), drawnPartY2=currentSubmarine.getImage().getHeight();
				
				if(x < 0){
					drawnPartX1 = -x;
				}
				if(y < 0){
					drawnPartY1 = -y;
				}
				
				currentSubmarine.drawSubmarine(gr, x, y, x + size*currentSubmarine.getSize() - padding,
						y + size - padding, drawnPartX1, drawnPartY1, drawnPartX2, drawnPartY2);
			}
		}
	}
	
}
