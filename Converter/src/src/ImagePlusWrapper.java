package src;

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

	public byte[] openBytes1(int index) {
		return (byte[]) image.getImageStack().getProcessor(index + 1).convertToByteProcessor().getPixelsCopy();
	}
	//the problem is toByteProcessor, somehow messes w numbers

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
		return image.getNFrames();
	}

	@Override
	public int getImageCount() {
		return getSizeC() * getSizeZ() * getSizeT();
	}

	@Override
	public int[] getZCTCoords(int index) {
		int i = index + 1;
		image.setPosition(i);
		int[] arr = new int[]{image.getSlice() - 1, image.getChannel() - 1, image.getFrame() - 1};
		return arr;
	}

	public byte[] openBytes2(int no) throws IOException, FormatException {
		return openBytes(no, 0, 0, image.getWidth(), image.getHeight());
	}
	@Override
	public byte[] openBytes(int no, int x, int y, int w, int h) throws FormatException, IOException {
		image.setPosition(no + 1);
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
