package face_detection;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

public class BackGroundImage extends JPanel {
	private Image image;

	public BackGroundImage(Image input) {
		image = input;
	}

	@Override
	protected void paintComponent(Graphics grphcs) {
		super.paintComponent(grphcs);
		Graphics g = grphcs;
		g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	}
}
