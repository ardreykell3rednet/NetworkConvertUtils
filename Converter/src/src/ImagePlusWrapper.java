package src;

import GUIElements.OutWindow;
import ij.ImagePlus;
import loci.formats.FormatException;
import loci.formats.ImageReader;

import java.io.IOException;

public class ImagePlusWrapper extends ImageReader {
	ImagePlus image;

	public ImagePlusWrapper(ImagePlus ip) {
		image = ip;
		//he
	}

	@Override
	public byte[] openBytes(int index) {
		return (byte[]) image.getImageStack().getProcessor(index + 1).convertToByteProcessor().getPixelsCopy();
	}

	@Override
	public int getSizeX() {
		return image.getWidth();
	}

	@Override
	public int getSizeY() {
		return image.getHeight();
	}

	@Override
	public int getSizeC() {
		return image.getNChannels();
	}

	@Override
	public int getSizeZ() {
		return image.getNSlices();
	}

	@Override
	public int getSizeT() {
		return image.getFrame();
	}

	@Override
	public int getImageCount() {
		return getSizeC() * getSizeZ() * getSizeT();
	}

	@Override
	public int[] getZCTCoords(int index) {
		image.setPosition(index + 1);
		int[] arr = new int[]{image.getSlice() - 1, image.getChannel() - 1, image.getFrame() - 1};
		new OutWindow().println(arr);
		return arr;
	}

	@Override
	public byte[] openBytes(int no, int x, int y, int w, int h) throws FormatException, IOException {
		image.setCurrentSlice(no);
		if (w > image.getWidth())
			throw new IndexOutOfBoundsException("Width of the section cannot be longer than the width of the image");
		if (h > image.getHeight())
			throw new IndexOutOfBoundsException("Height of the section cannot be longer than the height of the image");
		if (image.getBitDepth() == 24) throw new FormatException("Not equipped to deal with RGB images");
		byte[] pixelVals = new byte[w * h * image.getBytesPerPixel()];
		int iter = 0;
		for (int yPix = y; yPix < h; yPix++) {
			for (int xPix = x; xPix < w; xPix++) {
				int pix = image.getPixel(xPix, yPix)[0];
				switch (image.getBitDepth()) {
					case 8: {
						pixelVals[iter++] = (byte) pix;

						break;
					}
					case 16: {
						pixelVals[iter++] = (byte) pix;
						pixelVals[iter++] = (byte) (pix >>> 8);
						break;
					}
					case 32: {
						pixelVals[iter++] = (byte) pix;
						pixelVals[iter++] = (byte) (pix >>> 8);
						pixelVals[iter++] = (byte) (pix >>> 16);
						break;
					}
					default:
						throw new FormatException("Could not identify bit depth of image");
				}
			}
		}
		return pixelVals;

	}
}
