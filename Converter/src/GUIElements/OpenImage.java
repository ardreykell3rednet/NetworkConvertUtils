package GUIElements;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import loci.formats.FormatException;
import src.HyperStackWrapper;
import src.ImgToCSV;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class OpenImage extends JPanel {
	private ImagePlus imagePlus;
	private int imageNum;

	public OpenImage(ImagePlus ip, int img) {
		imagePlus = ip;
		imageNum = img;
	}

	public OpenImage(ImagePlus ip, int series, int slice, int channel) {
		imagePlus = ip;
		HyperStackWrapper hsw = new HyperStackWrapper(imagePlus.getNChannels(), imagePlus.getNSlices(), imagePlus.getNFrames());
		imageNum = hsw.getStack(channel, slice, series);

	}

	public OpenImage(String file, int series, int image) throws IOException, FormatException {
		imagePlus = ImgToCSV.imgFromFile(series, file);
		imageNum = image;
	}

	public OpenImage(String file, int image) throws IOException, FormatException {
		this(file, 0, image);
	}

	public OpenImage(String file, int series, int slice, int channel) throws IOException, FormatException {
		imagePlus = ImgToCSV.imgFromFile(series, file);
		HyperStackWrapper hsw = new HyperStackWrapper(imagePlus.getNChannels(), imagePlus.getNSlices(), imagePlus.getNFrames());
		imageNum = hsw.getStack(channel, slice, 0);
	}

	public static void main(String[] args) throws IOException, FormatException {
		OpenImage img = new OpenImage("C:\\Neural Net Data\\P5 Syk\\ND2s\\SET 1.nd2", 0, 27, 2);
		IJ.open();
		WindowManager.setTempCurrentImage(img.getImagePlus());
		JFrame jf = new JFrame();
		JFrame hi = new JFrame();
		jf.add(img);
		JButton b = new JButton("press");
		b.addActionListener(e -> img.drawPoint(50, 50));
		hi.add(b);
		jf.setVisible(true);
		hi.setVisible(true);
		//ImageIO.write(image,"tiff",new File("trial2.tiff"));
	}

	public ImagePlus getImagePlus() {
		return imagePlus;
	}


	public int getImageNum() {
		return imageNum;
	}

	public void setImageNum(int imageNum) {
		if (imageNum < imagePlus.getImageStackSize())
			this.imageNum = imageNum;
		else
			throw new IllegalArgumentException("Please pass in an image number that is present in the file");
	}

	public void setImageNum(int series, int slice, int channel) {
		HyperStackWrapper hsw = new HyperStackWrapper(imagePlus.getNChannels(), imagePlus.getNSlices(), imagePlus.getNFrames());
		setImageNum(hsw.getStack(channel, slice, series));
	}

	public void setImageNum(int slice, int channel) {
		setImageNum(0, slice, channel);
	}

	public BufferedImage getImg() {
		imagePlus.setPosition(imageNum);
		return (BufferedImage) imagePlus.getImage();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		BufferedImage i = getImg();
		g.drawImage(i, 0, 0, this);
	}

	public void drawPoint(int x, int y) {
		Graphics g = getGraphics();
		g.setColor(Color.yellow);
		g.fillOval(x, y, 5, 5);
	}

}
