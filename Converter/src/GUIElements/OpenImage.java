package GUIElements;

import ij.ImagePlus;
import loci.formats.FormatException;
import src.ImgToCSV;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class OpenImage {
	ImagePlus imagePlus;
	JLabel picture;

	public OpenImage(ImagePlus ip) {
		imagePlus = ip;
	}

	public OpenImage(String file, int series) throws IOException, FormatException {
		imagePlus = ImgToCSV.imgFromFile(series, file);
	}

	public BufferedImage getImg(int n) {
		imagePlus.setPosition(n);
		return (BufferedImage) imagePlus.getImage();
	}

	public void main() throws IOException, FormatException {
		OpenImage img = new OpenImage("C:\\Neural Net Data\\P5 Syk\\ND2s\\SET 1.nd2", 0);
		BufferedImage image = img.getImg(50);
		JFrame jf = new JFrame();
		JLabel picLabel = new JLabel(new ImageIcon(image));
		jf.add(picLabel);
		jf.setVisible(true);
		//ImageIO.write(image,"tiff",new File("trial2.tiff"));
	}

	public void drawPic(int n) throws IOException, FormatException {
		BufferedImage image = getImg(n);
		picture = new JLabel(new ImageIcon(image));
	}

	public void draw() {

	}

	public JLabel getPicture() {
		return picture;
	}
}
