package battleship;

import java.awt.Point;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {
	
	public static void main(String[] args) {
		
		//Connect to the server:
		String serverAddress = JOptionPane.showInputDialog("Enter IP Address of a machine that is\n" +
	            "running the date service on port 9090:");
		try {
			System.out.println("Trying to connect...");
			Socket server = new Socket(serverAddress, 9090);
			System.out.println("Connected to server: " + server.getRemoteSocketAddress());
			
			//GUI stuff:
			final Point boardsSize = new Point(10, 10);
			final int padding = 5, variation = 1, squareSize1 = 30, squareSize2 = 40;
			
			GUI gui = new GUI(server, "Battleship", padding, variation, squareSize1, squareSize2, boardsSize);
			gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			gui.setLocationRelativeTo(null);
			gui.setVisible(true);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
