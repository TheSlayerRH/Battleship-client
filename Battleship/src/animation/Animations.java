package animation;

/*
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
*/
public class Animations {
	/*
	private static BufferedImage[] fireAnimation;
	private static BufferedImage[] explosionAnimation;
	static{
		try {
			BufferedImage fire = ImageIO.read(new File("src\\images\\fireAnimation.png"));
			BufferedImage explosion = ImageIO.read(new File("src\\images\\explosionAnimation.png"));
			
			fireAnimation = toAnimationImages(fire, 4, 4);
			explosionAnimation = toAnimationImages(explosion, 4, 4);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static BufferedImage[] toAnimationImages(BufferedImage spriteSheet, int imagesPerRow, int imagesPerColumn){
		int imageWidth = spriteSheet.getWidth()/imagesPerRow;
		int imageHeight = spriteSheet.getHeight()/imagesPerColumn;
		
		BufferedImage[] images = new BufferedImage[imagesPerRow*imagesPerColumn];
		
		for(int row=0; row<imagesPerColumn; row++){
			for(int column=0; column<imagesPerRow; column++){
				BufferedImage currentImage = spriteSheet.getSubimage(column*imageWidth, row*imageHeight, imageWidth, imageHeight);
				images[imagesPerRow*row + column] = currentImage;
			}
		}
		
		return images;
	}
	*/
}
