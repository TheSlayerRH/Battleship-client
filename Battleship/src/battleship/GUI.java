package battleship;

import java.awt.Color;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUI extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 919841868011523000L;
	private final int padding;
	private final int variation;
	private final int squareSize1, squareSize2;
	
	private final Point boardsSize;
	
	private Board board1, board2;
	private ShipsPanel shipsPanel;
	private BackgroundPanel panel, colorPanel;
	private JButton buttonReady;
	
	private Submarine[] submarines;
	
	private int shipAttached;
	private boolean myTurn;
	private boolean gameStarted;
	
	private Socket server;
	private BufferedReader input;
	private PrintWriter output;
	private int playerNumber;
	
	Thread exitListener;
	
	public GUI(Socket server, String title, int padding, int variation, int squareSize1, int squareSize2, Point boardsSize){
		super(title);
		this.padding = padding;
		this.variation = variation;
		this.squareSize1 = squareSize1;
		this.squareSize2 = squareSize2;
		this.boardsSize = boardsSize;
		shipAttached = -1;
		myTurn = false;
		gameStarted = false;
		
		this.server = server;
		try{
			this.input = new BufferedReader(new InputStreamReader(server.getInputStream()));
			this.output = new PrintWriter(server.getOutputStream(), true);
			
			String message = input.readLine();
			playerNumber = Integer.parseInt(message.substring(message.length()-1));
			while(!message.equals("Both players are connected, ready to start. Please order your fleet and click ready.")){
				System.out.println(message);
				message = input.readLine();
			}
			System.out.println(message);
		}catch(IOException e){
			e.printStackTrace();
		}
		
		if(variation == 1){
			submarines = new Submarine[5];
			submarines[0] = new Submarine(SubmarineType.Carrier);
			submarines[1] = new Submarine(SubmarineType.Battleship);
			submarines[2] = new Submarine(SubmarineType.Cruiser);
			submarines[3] = new Submarine(SubmarineType.Submarine);
			submarines[4] = new Submarine(SubmarineType.Destroyer);
		}
		
		setLayout(null);
		//Set the size to fit the boards in:
		setSize((int) (17 + 2*padding + (boardsSize.x+1)*squareSize1 + boardsSize.x*squareSize2), (int) (39 + 2*padding + (boardsSize.y+1)*squareSize1 + boardsSize.y*squareSize2));
		//Set the frame to not be resizeable:
		setResizable(false);
		
		/*
		exitListener = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(myTurn){
					
					try {
						String message = input.readLine();
						if(message.equals("Game Over, the other player probably had a rage quit.")){
							server.close();
							System.exit(0);
						}
						
						exitListener.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		*/
		
		initializeComponents();
	}
	
	private void initializeComponents(){
		//panel:
		panel = new BackgroundPanel(squareSize1, padding, submarines, boardsSize);
		panel.setBounds(0, 0, this.getWidth(), this.getHeight());
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setLayout(null);
		add(panel);
		
		//board1:
		board1 = new Board(boardsSize, squareSize1, variation, submarines, false, padding, squareSize1);
		board1.setBounds(padding, padding, squareSize1*(boardsSize.x + 1)+1, squareSize1*(boardsSize.y + 1)+1); //plus 1 squareSize for letters and numberse space;
		board1.setLayout(null);
		panel.add(board1);
		
		//colorPanel - that square1 size panel at the left top corner of board1, used to give that square of the board the background color:
		colorPanel = new BackgroundPanel(squareSize1, padding, submarines, boardsSize);
		colorPanel.setBounds(0, 0, squareSize1, squareSize1);
		colorPanel.setBackground(panel.getBackground());
		board1.add(colorPanel);
		
		//board2:
		board2 = new Board(boardsSize, squareSize2, variation, submarines, true, padding, squareSize1);
		board2.setBounds(padding + squareSize1*(boardsSize.x+1) - squareSize2, padding+squareSize1*(boardsSize.y+1) - squareSize2, squareSize2*(boardsSize.x + 1)+1, squareSize2*(boardsSize.y + 1)+1);
		panel.add(board2);
		
		//shipsPanel:
		shipsPanel = new ShipsPanel(1, squareSize1, padding, submarines, panel.getBackground());
		shipsPanel.setBounds(board1.getX() + board1.getWidth(), board1.getY(), board2.getWidth() - squareSize2, board1.getHeight() - squareSize2 - 1);
		shipsPanel.setLayout(null);
		panel.add(shipsPanel);
		
		//buttonReady:
		buttonReady = new JButton();
		buttonReady.setText("Ready");
		buttonReady.setFont(new Font(Font.SERIF, Font.BOLD, 32));
		buttonReady.setBounds(padding, shipsPanel.getHeight() - padding - 2*squareSize1, 4*squareSize1, 2*squareSize1);
		buttonReady.setFocusable(false);
		buttonReady.setVisible(false);
		shipsPanel.add(buttonReady);
		buttonReady.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!board1.isLegalArrangement()){
					JOptionPane.showMessageDialog(null, "Please arrange your fleet by the rules:\n"
							+ "\t- The ships cannot overlap (i.e., only one ship can occupy any given square in the grid).\n"
							+ "\t- The squares around a ship must not be occupied.");
					return;
				}
				
				for(Submarine sub: submarines){
					int[][] locations = sub.getLocations();
					for(int i=0; i<locations.length; i++){
						board1.setBoard(locations[i][0] - 1, locations[i][1] - 1, playerNumber);
					}
				}
				
				int[][] board = board1.getBoard();
				String boardStr = "";
				
				for(int y=0; y<board.length; y++){
					for(int x=0; x<board[y].length; x++){
						boardStr += board[y][x] + ", ";
					}
				}
				
				boardStr = boardStr.substring(0, boardStr.length()-2) + ". " + board[0].length + ", " + board.length;
				try {
					output.println(boardStr);
					System.out.println(input.readLine());
					buttonReady.setVisible(false);
					gameStarted = true;
					
					//Start a thread that will listen to the messages from the server:
					Thread listener = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								while(true){
									if(!myTurn){
										String message = input.readLine();
										System.out.println(message);
										
										if(message.equals("Your turn")){
											myTurn = true;
											board2.setMyTurn(myTurn);
											board2.repaint();
											panel.repaint();
										}else if(message.startsWith("Opponent MISS - ")){
											int commaI = message.length() - 1;
											for(; commaI>=0; commaI--){
												if(message.charAt(commaI) == ',')
													break;
											}
											int x = Integer.parseInt(message.substring(16, commaI));
											int y = Integer.parseInt(message.substring(commaI + 2));
											
											board1.addHitOrMiss(x, y, false);
											
											board1.repaint();
										}else if(message.startsWith("Opponent HIT - ")){
											int commaI = message.length() - 1;
											for(; commaI>=0; commaI--){
												if(message.charAt(commaI) == ',')
													break;
											}
											int x = Integer.parseInt(message.substring(15, commaI));
											int y = Integer.parseInt(message.substring(commaI + 2));
											
											for(int i=0; i<submarines.length; i++){
												int locationOnShip = submarines[i].locationOnShip(x + 1, y + 1);
												if(locationOnShip != -1){
													submarines[i].setHit(locationOnShip);
													break;
												}
											}
											
											board1.repaint();
										}else if(message.startsWith("Winner: ")){
											String wonOrLost = "";
											if(Integer.parseInt(message.substring(message.length()-1)) == playerNumber)
												wonOrLost = "won";
											else
												wonOrLost = "lost";
											
											int answer = JOptionPane.showConfirmDialog(null, "You " + wonOrLost + "!\nWould you like to play again? ", "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
											if(answer == JOptionPane.YES_OPTION){
												output.println("YES");
												System.out.println("Waiting for the other player to answer...");
												String returnMessage = input.readLine();
												System.out.println(returnMessage);
												if(returnMessage.equals("RESTARTING")){
													resetGame();
												}else if(returnMessage.equals("The other player doesn't want to play again.")){
													server.close();
													JOptionPane.showMessageDialog(null, returnMessage);
												}
											}else{
												output.println("NO");
												server.close();
											}
											break;
										}
									}
									Thread.sleep(1000);
								}
							} catch (IOException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
						}
					});
					listener.start();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		//Add mouse listeners:
		MouseListenerClass mouseListener = new MouseListenerClass();
		shipsPanel.addMouseListener(mouseListener);
		shipsPanel.addMouseMotionListener(mouseListener);
		board1.addMouseListener(mouseListener);
		board1.addMouseMotionListener(mouseListener);
		board2.addMouseListener(mouseListener);
		board2.addMouseMotionListener(mouseListener);
		panel.addMouseListener(mouseListener);
		panel.addMouseMotionListener(mouseListener);
		colorPanel.addMouseListener(mouseListener);
		colorPanel.addMouseMotionListener(mouseListener);
		
		//Add key listeners:
		KeyListenerClass keyListener = new KeyListenerClass();
		addKeyListener(keyListener);
	}
	
	private void resetGame(){
		gameStarted = false;
		if(variation == 1){
			submarines[0] = new Submarine(SubmarineType.Carrier);
			submarines[1] = new Submarine(SubmarineType.Battleship);
			submarines[2] = new Submarine(SubmarineType.Cruiser);
			submarines[3] = new Submarine(SubmarineType.Submarine);
			submarines[4] = new Submarine(SubmarineType.Destroyer);
		}
		shipsPanel.resetGame();
		board1.resetGame();
		board2.resetGame();
		colorPanel.resetGame();
		panel.resetGame();
		shipsPanel.repaint();
		board1.repaint();
		board2.repaint();
		colorPanel.repaint();
		panel.repaint();
	}
	
	public class MouseListenerClass implements MouseListener, MouseMotionListener {

		//MouseListener methods:
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getComponent() == shipsPanel){
				if(shipAttached != -1){
					submarines[shipAttached].setState(false);
					shipAttached = -1;
				}
				for(int i=0; i<submarines.length; i++){
					Submarine currentSubmarine = submarines[i];
					if(!currentSubmarine.isOnBoard() && e.getX() > currentSubmarine.getTopLeftLocation()[0] && e.getX() < currentSubmarine.getBottomRightLocation()[0]
							&& e.getY() > currentSubmarine.getTopLeftLocation()[1] && e.getY() < currentSubmarine.getBottomRightLocation()[1]){
						shipAttached = i;
						break;
					}
				}
				
				updateShipAttached();
				if(shipAttached != -1)
					setOnBoardIfNeeded(e);
				shipsPanel.repaint();
				panel.repaint();
			}else if(e.getComponent() == board1){
				if(shipAttached != -1 && e.getX() > squareSize1 && e.getY() > squareSize1 &&
						((submarines[shipAttached].isVertical() &&
						e.getY() < board1.getHeight() - (submarines[shipAttached].getSize()-1)*squareSize1 && e.getX() < board1.getWidth()) ||
						(!submarines[shipAttached].isVertical() &&
						e.getX() < board1.getWidth() - (submarines[shipAttached].getSize()-1)*squareSize1 && e.getY() < board1.getHeight()))){
					//The user is trying to locate the attached ship on the board;
					int x = e.getX()/squareSize1;
					if((double)e.getX()/squareSize1%1 == 0) x = e.getX()/squareSize1 - 1; //x coordinate on the board;
					int y = e.getY()/squareSize1; //y coordinate on the board;
					if((double)e.getY()/squareSize1%1 == 0) y = e.getY()/squareSize1 - 1;
					submarines[shipAttached].setLocations(new int[]{x, y});
					shipAttached = -1;
					updateShipAttached();
					if(isReadyToStart()){
						buttonReady.setVisible(true);
						shipsPanel.repaint();
					}
				}else if(shipAttached != -1){
					if(buttonReady.isVisible()){
						buttonReady.setVisible(false);
					}
					submarines[shipAttached].setState(false);
					shipAttached = -1;
					updateShipAttached();
					shipsPanel.repaint();
					board1.repaint();
					board2.repaint();
					panel.repaint();
					colorPanel.repaint();
				}else if(shipAttached == -1 && !gameStarted){
					for(int i=0; i<submarines.length; i++){
						Submarine currentSubmarine = submarines[i];
						if(currentSubmarine.isOnBoard() && e.getX() > currentSubmarine.getTopLeftLocation()[0] && e.getX() < currentSubmarine.getBottomRightLocation()[0]
								&& e.getY() > currentSubmarine.getTopLeftLocation()[1] && e.getY() < currentSubmarine.getBottomRightLocation()[1]){
							shipAttached = i;
							updateShipAttached();
							if(buttonReady.isVisible())
								buttonReady.setVisible(false);
							
							break;
						}
					}
				}
			}else if(myTurn && e.getComponent() == board2){
				//The user is trying to shoot at the enemy fleet:
				int x = e.getX()/squareSize2 - 1;
				if((double)e.getX()/squareSize2%1 == 0) x--; //x coordinate on the board;
				int y = e.getY()/squareSize2 - 1; //y coordinate on the board;
				if((double)e.getY()/squareSize2%1 == 0) y--;
				
				if(isLegalMove(x, y, board2)){
					try {
						output.println("Shoot " + x + ", " + y);
						String message = input.readLine();
						System.out.println(message);
						System.out.println("~~~~~~~~~~~~");
						if(message.startsWith("HIT - ")){
							board2.addHitOrMiss(x, y, true);
						}else if(message.startsWith("MISS - ")){
							board2.addHitOrMiss(x, y, false);
						}
						
						board2.repaint();
						panel.repaint();
						
						myTurn = false;
						board2.setMyTurn(false);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}else{
				if(shipAttached != -1){
					submarines[shipAttached].setState(false);
					shipAttached = -1;
					updateShipAttached();
					shipsPanel.repaint();
					board1.repaint();
					board2.repaint();
					panel.repaint();
					colorPanel.repaint();
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if(myTurn && e.getComponent() == board2){
				board2.repaint();
				panel.repaint();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		//MouseMotionListener methods:
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseMoved(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if(shipAttached != -1){
				setOnBoardIfNeeded(e);
				shipsPanel.repaint();
				board1.repaint();
				board2.repaint();
				panel.repaint();
				colorPanel.repaint();
			}else{
				if(myTurn && e.getComponent() == board2){
					board2.repaint();
					panel.repaint();
				}
			}
		}
		
		//Helper methods I created:
		private void setOnBoardIfNeeded(MouseEvent e){
			if(e.getComponent() == board1 && e.getX() > squareSize1 && e.getY() > squareSize1 &&
					((submarines[shipAttached].isVertical() &&
					e.getY() < board1.getHeight() - (submarines[shipAttached].getSize()-1)*squareSize1 && e.getX() < board1.getWidth()) ||
					(!submarines[shipAttached].isVertical() &&
					e.getX() < board1.getWidth() - (submarines[shipAttached].getSize()-1)*squareSize1 && e.getY() < board1.getHeight()))){
				submarines[shipAttached].setOnBoard(true);
				shipsPanel.setAttachedOnBoard(true);
				board2.setAttachedOnBoard(true);
				panel.setAttachedOnBoard(true);
				colorPanel.setAttachedOnBoard(true);
			}else{
				submarines[shipAttached].setOnBoard(false);
				shipsPanel.setAttachedOnBoard(false);
				board2.setAttachedOnBoard(false);
				panel.setAttachedOnBoard(false);
				colorPanel.setAttachedOnBoard(false);
			}
		}
		
		private void updateShipAttached(){
			shipsPanel.setShipAttached(shipAttached);
			board1.setShipAttached(shipAttached);
			board2.setShipAttached(shipAttached);
			panel.setShipAttached(shipAttached);
			colorPanel.setShipAttached(shipAttached);
		}
		
		private boolean isReadyToStart(){
			if(shipAttached == -1){
				for(int i=0; i<submarines.length; i++){
					if(!submarines[i].isOnBoard()){
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		private boolean isLegalMove(int x, int y, Board board){
			if(y >= board.getBoard().length || y < 0 || x >= board.getBoard()[y].length || x < 0)
				return false;
			
			List<int[]> hits = board.getHits();
			List<int[]> misses = board.getMisses();
			
			for(int i=0; i<hits.size(); i++){
				int[] currentHit = hits.get(i);
				if(x == currentHit[0] && y == currentHit[1])
					return false;
			}
			for(int i=0; i<misses.size(); i++){
				int[] currentMiss = misses.get(i);
				if(x == currentMiss[0] && y == currentMiss[1])
					return false;
			}
			
			return true;
		}
	}
	
	
	public class KeyListenerClass implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			
		}

		@Override
		public void keyTyped(KeyEvent e) {
			if(shipAttached != -1){
				
				char keyChar = e.getKeyChar();
				if(!submarines[shipAttached].isVertical() && keyChar == 'v'){
					submarines[shipAttached].setState(true);
					setOnBoardIfNeeded();
					shipsPanel.repaint();
					board1.repaint();
					board2.repaint();
					panel.repaint();
					colorPanel.repaint();
				}else if(submarines[shipAttached].isVertical() && keyChar == 'h'){
					submarines[shipAttached].setState(false);
					setOnBoardIfNeeded();
					shipsPanel.repaint();
					board1.repaint();
					board2.repaint();
					panel.repaint();
					colorPanel.repaint();
				}
				
			}
			e.consume();
		}
		
		//Helper methods I created:
		private void setOnBoardIfNeeded(){
			int xOnBoard1 = (int) (MouseInfo.getPointerInfo().getLocation().getX() - board1.getLocationOnScreen().getX());
			int yOnBoard1 = (int) (MouseInfo.getPointerInfo().getLocation().getY() - board1.getLocationOnScreen().getY());
			
			if(xOnBoard1 > squareSize1 && yOnBoard1 > squareSize1 &&
					((submarines[shipAttached].isVertical() &&
					yOnBoard1 < board1.getHeight() - (submarines[shipAttached].getSize()-1)*squareSize1 && xOnBoard1 < board1.getWidth()) ||
					(!submarines[shipAttached].isVertical() &&
					xOnBoard1 < board1.getWidth() - (submarines[shipAttached].getSize()-1)*squareSize1 && yOnBoard1 < board1.getHeight()))){
				shipsPanel.setAttachedOnBoard(true);
				board2.setAttachedOnBoard(true);
				panel.setAttachedOnBoard(true);
				colorPanel.setAttachedOnBoard(true);
			}else{
				shipsPanel.setAttachedOnBoard(false);
				board2.setAttachedOnBoard(false);
				panel.setAttachedOnBoard(false);
				colorPanel.setAttachedOnBoard(false);
			}
		}
	}
	
}
