package GUIElements;

import ij.ImagePlus;
import loci.formats.FormatException;
import src.ImgToCSV;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class OpenImage extends JPanel {
	ImagePlus imagePlus;
	int image;

	public OpenImage(ImagePlus ip, int img) {
		imagePlus = ip;
		image = img;
	}

	public OpenImage(String file, int series, int img) throws IOException, FormatException {
		imagePlus = ImgToCSV.imgFromFile(series, file);
		image = img;
	}

	public BufferedImage getImg(int n) {
		imagePlus.setPosition(n);
		return (BufferedImage) imagePlus.getImage();
	}

	public static void main(String[] args) throws IOException, FormatException {
		OpenImage img = new OpenImage("C:\\Neural Net Data\\P5 Syk\\ND2s\\SET 1.nd2", 0, 50);
		JFrame jf = new JFrame();
		JFrame hi = new JFrame();
		jf.add(img);
		JButton b = new JButton("press");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				img.drawPoint(50, 50);
			}
		});
		hi.add(b);
		jf.setVisible(true);
		hi.setVisible(true);
		//ImageIO.write(image,"tiff",new File("trial2.tiff"));
	}

	public void setImage(int image) {
		this.image = image;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		BufferedImage i = getImg(image);
		g.drawImage(i, 0, 0, this);
	}

	public void drawPoint(int x, int y) {
		Graphics g = getGraphics();
		g.setColor(Color.yellow);
		g.fillOval(x, y, 5, 5);
	}

}
