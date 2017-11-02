package battleship;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;

import javax.swing.JPanel;

public class BackgroundPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5330083577785094138L;
	private int size, padding;
	private int shipAttached;
	private Submarine[] submarines;
	private boolean attachedOnBoard;
	
	public BackgroundPanel(int size, int padding, Submarine[] submarines, Point boardsSize){
		this.size = size;
		this.padding = padding;
		this.submarines = submarines;
		this.attachedOnBoard = false;
		
		shipAttached = -1;
	}
	
	public void resetGame(){
		this.attachedOnBoard = false;
		
		shipAttached = -1;
	}
	
	public void setShipAttached(int shipAttached){
		this.shipAttached = shipAttached;
	}
	
	public void setAttachedOnBoard(boolean attachedOnBoard){
		this.attachedOnBoard = attachedOnBoard;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D gr = (Graphics2D) g;
		//clear the panel:
		gr.setColor(getBackground());
		gr.fillRect(0, 0, getWidth(), getHeight());
		
		//draw the attached ship:
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
