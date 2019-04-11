package src;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.PointRoi;
import ij.io.RoiDecoder;
import ij.plugin.Converter;
import ij.plugin.HyperStackConverter;
import ij.plugin.RoiScaler;
import ij.plugin.frame.ContrastAdjuster;
import ij.process.StackProcessor;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// TODO correlate ND2s and ROIs
// TODO start from middle of series
// TODO Use previous TIFFs
public class Runner {

	public final String ROI_LOCATION;
	public final int NUM_SECTIONS;
	public final int MAX_ROI;
	public final boolean ARCHIVE_TIFFS;
	final String ND2_LOCATION;
	final int FIELD_SIZE;
	final int INCREMENT;
	private final boolean TESTING_MODE = true;
	int seriesCount;
	String tiff;
	String csv;


	public Runner(String nd2Loc, String roiLoc, String outLoc, boolean archive, int maxRoi, int fieldSize, int incr) throws FileNotFoundException {
		ND2_LOCATION = nd2Loc;
		ROI_LOCATION = roiLoc;
		ARCHIVE_TIFFS = archive;
		MAX_ROI = maxRoi;
		FIELD_SIZE = fieldSize;
		INCREMENT = incr;
		if (!new File(ND2_LOCATION).exists())
			throw new FileNotFoundException("The file at " + ND2_LOCATION + " was not found");
		if (!new File(ROI_LOCATION).exists())
			throw new FileNotFoundException("The file at " + ROI_LOCATION + " was not found");
		int i = 1;
		File runDir;
		do {
			runDir = new File(outLoc, "Run " + i);
			i++;
		} while (runDir.exists());
		runDir.mkdirs();
		makePaths(runDir);
		int numFields = 0;
		for (int y = 0; y <= (512 - FIELD_SIZE); y += INCREMENT) {
			for (int x = 0; x <= (512 - FIELD_SIZE); x += INCREMENT) {
				numFields++;
			}
		}
		NUM_SECTIONS = numFields;
	}


	public Runner(String nd2Loc, String roiLoc, String outLoc, boolean archive, int maxRoi) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, archive, maxRoi, 64, 8);
	}

	public Runner(String nd2Loc, String roiLoc, String outLoc, boolean archive) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, archive, 10);
	}

	public Runner(String nd2Loc, String roiLoc, String outLoc, int maxRoi) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, false, maxRoi);
	}

	public Runner(String nd2Loc, String roiLoc, String outLoc) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, false);
	}

	private Runner(String outLoc, boolean archive) throws FileNotFoundException {
		this("C:\\Neural Net Data\\P5 Syk\\ND2s", "C:\\Neural Net Data\\P5 Syk\\VectorROIs", outLoc, archive);
	}

	private Runner(String outLoc) throws FileNotFoundException {
		this(outLoc, false);
	}

	private Runner(boolean archive) throws FileNotFoundException {
		this("C:\\Neural Net Data\\FINAL", archive);
	}

	private Runner() throws FileNotFoundException {
		this(false);
	}

	public void writeCSVs() throws IOException, FormatException {
		writeCSVs(0, 0, 0);
	}
	// TODO CORRELATE ND2s with ROIs to prevent MIXUP, fix mixup
	public void writeCSVs(int startFile, int startSeries, int startSlice) throws IOException, FormatException {

		File nd2Folder = new File(ND2_LOCATION);
		File[] nd2List = nd2Folder.listFiles();
		File roiFolder = new File(ROI_LOCATION);
		File[] roiList = roiFolder.listFiles();
		if (nd2List.length == 0 || roiList.length == 0)
			throw new FileNotFoundException();
		for (int fileIndex = startFile; fileIndex < nd2List.length; fileIndex++) {
			startFile = 0;
			File nd2 = nd2List[fileIndex];
			File roi = roiList[fileIndex];

			File csvLoc = new File(csv);
			csvLoc.mkdirs();
			CSVPrintStream csvStream = new CSVPrintStream(new FileOutputStream(csv + "\\ND2 " + (fileIndex + 1) + ".csv"));
			int series;
			do {
				series = startSeries;
				startSeries = 0;
				ImageReader byteRead;
				ImagePlus raw = getImage(series, nd2);
				ImagePlus img = process(raw);
				ImagePlus hyperstack = HyperStackConverter.toHyperStack(img, 4, 31, 1, "grayscale");
				if (ARCHIVE_TIFFS) {
					File tiffLoc = new File(tiff, "ND2 " + (fileIndex + 1));
					tiffLoc.mkdirs();
					String tiffPath = tiffLoc.getPath() + "\\Tiff " + (series + 1) + ".tiff";
					IJ.saveAsTiff(hyperstack, tiffPath);
					byteRead = new ImageReader();
					byteRead.setId(tiffPath);
				} else {
					byteRead = new ImagePlusWrapper(hyperstack);
				}

				int sizeX = byteRead.getSizeX();
				int sizeY = byteRead.getSizeY();
				csvStream.print(byteRead.getSizeC());
				csvStream.print(byteRead.getSizeZ());
				csvStream.println(seriesCount);
				HyperStackWrapper hsw = new HyperStackWrapper(byteRead.getSizeC(), byteRead.getSizeZ(), seriesCount);
				for (int slice = startSlice; slice < byteRead.getImageCount(); slice++) {
					System.out.println("ND2: " + fileIndex + " Series: " + (series) + " Image: " + (slice));
					int[] zct = byteRead.getZCTCoords(slice);
					if (zct[1] == 0 || zct[1] == 3) continue;
					HSD standard = new HSD(zct[1], zct[0], series);
					csvStream.print(NUM_SECTIONS);
					csvStream.print(zct[1]);
					csvStream.print(zct[0]);
					csvStream.println(series);
					for (int y = 0; y <= (sizeY - FIELD_SIZE); y += INCREMENT) {
						for (int x = 0; x <= (sizeX - FIELD_SIZE); x += INCREMENT) {
							PointRoi noScale = (PointRoi) RoiDecoder.open(roi.getPath());
							short[] pos = noScale.positions;
							PointRoi curRoi = (PointRoi) RoiScaler.scale(
									noScale,
									0.5, 0.5, false
							);
							short[] newPos = new short[pos.length];
							for (int i = 0; i < pos.length; i++)
								newPos[i] = (short) (pos[i] - 1);
							curRoi.positions = newPos;

							int[] roiIndexes = new int[MAX_ROI];
							Point[] roiPoints = curRoi.getContainedPoints();
							csvStream.print(x);
							csvStream.print(y);
							Rectangle field = new Rectangle(x, y, FIELD_SIZE, FIELD_SIZE);
							int numRois = 0;
							for (int roiPos = 0; roiPos < curRoi.getNCoordinates(); roiPos++) {
								boolean layer = hsw.getHyperStack(curRoi.getPointPosition(roiPos)).equals(standard);
								boolean loc = field.contains(roiPoints[roiPos]);

								if (layer && loc) {
									roiIndexes[numRois] = roiPos;
									numRois++;
								}
							}
							csvStream.print(numRois);
							for (int currentPoints = 0; currentPoints < numRois; currentPoints++) {
								Point p = roiPoints[roiIndexes[currentPoints]];
								csvStream.print(p.x);
								csvStream.print(p.y);
							}
							int pad = MAX_ROI - (numRois * 2);
							for (int i = 0; i < pad; i++)
								csvStream.pad();
							byte[] nd2Bytes = byteRead.openBytes(slice, x, y, FIELD_SIZE, FIELD_SIZE);

							csvStream.println(nd2Bytes);
						}
					}
					csvStream.println("IMAGE_END");
				
					if (TESTING_MODE) return;
				}
				csvStream.println("SERIES_END");
				if (TESTING_MODE) break;

				series++;
			} while (series < seriesCount);
		}
	}

	private void makePaths(File baseFolder) {
		if (ARCHIVE_TIFFS) {
			File tempTiff = new File(baseFolder, "Tiffs");
			tempTiff.mkdirs();
			tiff = tempTiff.getAbsolutePath();
		}

		File tempCSVs = new File(baseFolder, "CSVs");
		tempCSVs.mkdirs();
		csv = tempCSVs.getAbsolutePath();
	}

	private ImagePlus getImage(int series, File file) throws IOException, FormatException {
		ImporterOptions options = new ImporterOptions();
		options.setWindowless(true);
		options.setId(file.getPath());
		options.setAutoscale(true);
		options.setSeriesOn(series, true);
		ImportProcess process = new ImportProcess(options);
		process.execute();
		seriesCount = process.getSeriesCount();
		System.out.println(seriesCount);
		ImagePlusReader reader = new ImagePlusReader(process);
		ImagePlus[] imagePluses = reader.openImagePlus();
		return imagePluses[0];
	}

	public ImagePlus process(ImagePlus img) {
		WindowManager.setTempCurrentImage(img);
		Converter conv = new Converter();
		conv.run("8-bit");

		StackProcessor sp = new StackProcessor(img.getStack(), img.getProcessor());
		ImageStack iStack = sp.resize(512, 512, true);
		ImagePlus update = new ImagePlus("Updated Image", iStack);

		ContrastAdjuster adj = new ContrastAdjuster();
		adj.reset(update, update.getProcessor());
		return update;
	}


	/**
	 * @return the rOI_LOCATION
	 */
	public String getROI_LOCATION() {
		return ROI_LOCATION;
	}


	/**
	 * @return the nUM_SECTIONS
	 */
	public int getNUM_SECTIONS() {
		return NUM_SECTIONS;
	}


	/**
	 * @return the mAX_ROI
	 */
	public int getMAX_ROI() {
		return MAX_ROI;
	}


	/**
	 * @return the aRCHIVE_TIFFS
	 */
	public boolean isARCHIVE_TIFFS() {
		return ARCHIVE_TIFFS;
	}


	/**
	 * @return the nD2_LOCATION
	 */
	public String getND2_LOCATION() {
		return ND2_LOCATION;
	}


	/**
	 * @return the fIELD_SIZE
	 */
	public int getFIELD_SIZE() {
		return FIELD_SIZE;
	}


	/**
	 * @return the iNCREMENT
	 */
	public int getINCREMENT() {
		return INCREMENT;
	}


	/**
	 * @return the tESTING_MODE
	 */
	public boolean isTESTING_MODE() {
		return TESTING_MODE;
	}


	/**
	 * @return the seriesCount
	 */
	public int getSeriesCount() {
		return seriesCount;
	}


	/**
	 * @return the tiff
	 */
	public String getTiff() {
		return tiff;
	}


	/**
	 * @return the csv
	 */
	public String getCsv() {
		return csv;
	}
}
/*

public static void asdf(String[] args) throws Exception {

		for(int series = 0; series< seriesCount; series++) {
			ImporterOptions options = new ImporterOptions();
			options.setWindowless(true);
			options.setId(PATH_ND2);
			options.setAutoscale(true);
			options.setSeriesOn(series, true);
			ImportProcess process = new ImportProcess(options);
			process.execute();
			ImagePlusReader reader = new ImagePlusReader(process);
			ImagePlus[] imagePluses = reader.openImagePlus();
			System.out.println(imagePluses.length);
			ImagePlus stack = imagePluses[0];
			System.out.println(stack.getStackSize());

			WindowManager.setTempCurrentImage(stack);
			Converter conv = new Converter();
			conv.run("8-bit");
			System.out.println(imagePluses.length);
			System.out.println(stack.getBytesPerPixel());
			System.out.println(stack.getStackSize());

			StackProcessor sp = new StackProcessor(stack.getStack(), stack.getProcessor());
			ImageStack iStack = sp.resize(512, 512, false);
			ImagePlus update = new ImagePlus("Updated Image", iStack);

			ContrastAdjuster adj = new ContrastAdjuster();
			adj.reset(update, update.getProcessor());
			String tiffPath="temp"+series+".tiff";
			IJ.saveAsTiff(update, tiffPath);

			ImageReader tiffReader = new ImageReader();
			tiffReader.setId(tiffPath);
			tiffReader.setSeries(0);

			PrintStream csvStream = new PrintStream(new FileOutputStream("C:\\Neural Net Data\\out_Test"+series+".csv"));
			int sizeX = tiffReader.getSizeX();
			int sizeY = tiffReader.getSizeY();
			for (int ser = 0; ser < 1; ser++) {
				for (int img = 0; img < tiffReader.getImageCount(); img++) {
					for(int y = 0; y<=(sizeY- FIELD_SIZE); y+=INCREMENT) {
						for(int x = 0; x<=(sizeX- FIELD_SIZE); x+=INCREMENT) {
							byte[] nd2Bytes=tiffReader.openBytes(img,x,y, FIELD_SIZE, FIELD_SIZE);

							String csvString = getCSVString(nd2Bytes);
							csvStream.print(csvString+"\n");
							System.out.println(new Point(x,y));

						}
					}
				}
			}
		}


*/


/*
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.PointRoi;
import ij.io.RoiDecoder;
import ij.plugin.Converter;
import ij.plugin.HyperStackConverter;
import ij.plugin.RoiScaler;
import ij.plugin.frame.ContrastAdjuster;
import ij.process.StackProcessor;
import loci.formats.ImageReader;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

// TODO correlate ND2s and ROIs
// TODO start from middle of series
// TODO Use previous TIFFs
public class Runner {
	final String ND2_LOCATION;
	final int FIELD_SIZE;
	final int INCREMENT;
	int seriesCount;
	public final String ROI_LOCATION;
	public final int NUM_SECTIONS;
	public final int MAX_ROI;
	String tiff;
	String csv;
	private final boolean TEST = true;
	public final boolean ARCHIVE_TIFFS = false;

	public Runner(String nd2Loc, String roiLoc, String outLoc, int fieldSize, int incr, int maxRoi) {
		ND2_LOCATION = nd2Loc;
		ROI_LOCATION = roiLoc;
		FIELD_SIZE = fieldSize;
		INCREMENT = incr;
		MAX_ROI = maxRoi;
		int i = 1;
		File runDir;
		do {
			runDir = new File(outLoc, "Run " + i);
			i++;
		} while (runDir.exists());
		runDir.mkdirs();
		makePaths(runDir);
		int numFields = 0;
		for (int y = 0; y <= (512 - FIELD_SIZE); y += INCREMENT) {
			for (int x = 0; x <= (512 - FIELD_SIZE); x += INCREMENT) {
				numFields++;
			}
		}
		NUM_SECTIONS = numFields;
	}

	public Runner(String nd2Loc, String roiLoc, String outLoc) {
		this(nd2Loc, roiLoc, outLoc, 64, 8, 10);
	}

	private Runner() {
		this("C:\\Neural Net Data\\P5 Syk\\ND2s", "C:\\Neural Net Data\\P5 Syk\\VectorROIs", "C:\\Neural Net Data\\FINAL", 64, 8, 10);
	}

	public static void main(String[] args) {
		Runner run = new Runner();
		try {
			run.writeCSVs();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// TODO CORRELATE ND2s with ROIs to prevent MIXUP, fix mixup
	public void writeCSVs() throws Exception {
		File nd2Folder = new File(ND2_LOCATION);
		File[] nd2List = nd2Folder.listFiles();
		File roiFolder = new File(ROI_LOCATION);
		File[] roiList = roiFolder.listFiles();
		if (nd2List.length == 0 || roiList.length == 0)
			throw new FileNotFoundException();
		for (int fileIndex = 0; fileIndex < nd2List.length; fileIndex++) {
			File nd2 = nd2List[fileIndex];
			File roi = roiList[fileIndex];
			File tiffLoc = new File(tiff, "ND2 " + (fileIndex + 1));
			tiffLoc.mkdirs();
			File csvLoc = new File(csv);
			csvLoc.mkdirs();
			CSVPrintStream csvStream = new CSVPrintStream(new FileOutputStream(csv + "\\ND2 " + (fileIndex + 1) + ".csv"));
			int series;
			do {
				series = 0;
				String tiffPath = tiffLoc.getPath() + "\\Tiff " + (series + 1) + ".tiff";
				ImageReader byteRead;
				ImagePlus raw = getImage(series, nd2);
				ImagePlus img = process(raw);
				ImagePlus hyperstack = HyperStackConverter.toHyperStack(img, 4, 31, 1, "grayscale");
				if(ARCHIVE_TIFFS) {
						IJ.saveAsTiff(hyperstack, tiffPath);
						byteRead= new ImageReader();
				}
				else {
					byteRead=new ImagePlusWrapper(hyperstack);
				}

				byteRead.setId(tiffPath);
				int sizeX = byteRead.getSizeX();
				int sizeY = byteRead.getSizeY();
				csvStream.print(byteRead.getSizeC());
				csvStream.print(byteRead.getSizeZ());
				csvStream.println(seriesCount);
				HyperStackWrapper hsw = new HyperStackWrapper(byteRead.getSizeC(), byteRead.getSizeZ(), seriesCount);
				for (int slice = 0; slice < byteRead.getImageCount(); slice++) {
					System.out.println("ND2: " + fileIndex + " Series: " + (series) + " Image: " + (slice));
					int[] zct = byteRead.getZCTCoords(slice);
					if (zct[1] == 0 || zct[1] == 3) continue;
					HSD standard = new HSD(zct[1], zct[0], series);
					csvStream.print(NUM_SECTIONS);
					csvStream.print(zct[1]);
					csvStream.print(zct[0]);
					csvStream.println(series);
					for (int y = 0; y <= (sizeY - FIELD_SIZE); y += INCREMENT) {
						for (int x = 0; x <= (sizeX - FIELD_SIZE); x += INCREMENT) {
							PointRoi noScale = (PointRoi) RoiDecoder.open(roi.getPath());
							short[] pos = noScale.positions;
							PointRoi curRoi = (PointRoi) RoiScaler.scale(
									noScale,
									0.5, 0.5, false
							);
							short[] newPos = new short[pos.length];
							for (int i = 0; i < pos.length; i++)
								newPos[i] = (short) (pos[i] - 1);
							curRoi.positions = newPos;

							int[] roiIndexes = new int[MAX_ROI];
							Point[] roiPoints = curRoi.getContainedPoints();
							csvStream.print(x);
							csvStream.print(y);
							Rectangle field = new Rectangle(x, y, FIELD_SIZE, FIELD_SIZE);
							int numRois = 0;
							for (int roiPos = 0; roiPos < curRoi.getNCoordinates(); roiPos++) {
								boolean layer = hsw.getHyperStack(curRoi.getPointPosition(roiPos)).equals(standard);
								boolean loc = field.contains(roiPoints[roiPos]);

								if (layer && loc) {
									roiIndexes[numRois] = roiPos;
									numRois++;
								}
							}
							csvStream.print(numRois);
							for (int currentPoints = 0; currentPoints < numRois; currentPoints++) {
								Point p = roiPoints[roiIndexes[currentPoints]];
								csvStream.print(p.x);
								csvStream.print(p.y);
							}
							int pad = MAX_ROI - (numRois * 2);
							for (int i = 0; i < pad; i++)
								csvStream.pad();
							byte[] nd2Bytes = byteRead.openBytes(slice, x, y, FIELD_SIZE, FIELD_SIZE);

							csvStream.println(nd2Bytes);
						}
					}
					csvStream.println("IMAGE_END");
					if (TEST) break;
				}
				csvStream.println("SERIES_END");
				if (TEST) break;

				series++;
			} while (series < seriesCount);
		}
		Desktop.getDesktop().open(new File(csv));
	}

	private void makePaths(File baseFolder) {
		File tempTiff = new File(baseFolder, "Tiffs");
		File tempCSVs = new File(baseFolder, "CSVs");
		tempTiff.mkdirs();
		tempCSVs.mkdirs();
		tiff = tempTiff.getAbsolutePath();
		csv = tempCSVs.getAbsolutePath();
	}

	public ImagePlus getImage(int series, File file) throws Exception {
		ImporterOptions options = new ImporterOptions();
		options.setWindowless(true);
		options.setId(file.getPath());
		options.setAutoscale(true);
		options.setSeriesOn(series, true);
		ImportProcess process = new ImportProcess(options);
		process.execute();
		seriesCount = process.getSeriesCount();
		System.out.println(seriesCount);
		ImagePlusReader reader = new ImagePlusReader(process);
		ImagePlus[] imagePluses = reader.openImagePlus();
		return imagePluses[0];
	}

	public ImagePlus process(ImagePlus img) {
		WindowManager.setTempCurrentImage(img);
		Converter conv = new Converter();
		conv.run("8-bit");

		StackProcessor sp = new StackProcessor(img.getStack(), img.getProcessor());
		ImageStack iStack = sp.resize(512, 512, true);
		ImagePlus update = new ImagePlus("Updated Image", iStack);

		ContrastAdjuster adj = new ContrastAdjuster();
		adj.reset(update, update.getProcessor());
		return update;
	}
}
*/
/*

public static void asdf(String[] args) throws Exception {

		for(int series = 0; series< seriesCount; series++) {
			ImporterOptions options = new ImporterOptions();
			options.setWindowless(true);
			options.setId(PATH_ND2);
			options.setAutoscale(true);
			options.setSeriesOn(series, true);
			ImportProcess process = new ImportProcess(options);
			process.execute();
			ImagePlusReader reader = new ImagePlusReader(process);
			ImagePlus[] imagePluses = reader.openImagePlus();
			System.out.println(imagePluses.length);
			ImagePlus stack = imagePluses[0];
			System.out.println(stack.getStackSize());

			WindowManager.setTempCurrentImage(stack);
			Converter conv = new Converter();
			conv.run("8-bit");
			System.out.println(imagePluses.length);
			System.out.println(stack.getBytesPerPixel());
			System.out.println(stack.getStackSize());

			StackProcessor sp = new StackProcessor(stack.getStack(), stack.getProcessor());
			ImageStack iStack = sp.resize(512, 512, false);
			ImagePlus update = new ImagePlus("Updated Image", iStack);

			ContrastAdjuster adj = new ContrastAdjuster();
			adj.reset(update, update.getProcessor());
			String tiffPath="temp"+series+".tiff";
			IJ.saveAsTiff(update, tiffPath);

			ImageReader tiffReader = new ImageReader();
			tiffReader.setId(tiffPath);
			tiffReader.setSeries(0);

			PrintStream csvStream = new PrintStream(new FileOutputStream("C:\\Neural Net Data\\out_Test"+series+".csv"));
			int sizeX = tiffReader.getSizeX();
			int sizeY = tiffReader.getSizeY();
			for (int ser = 0; ser < 1; ser++) {
				for (int img = 0; img < tiffReader.getImageCount(); img++) {
					for(int y = 0; y<=(sizeY- FIELD_SIZE); y+=INCREMENT) {
						for(int x = 0; x<=(sizeX- FIELD_SIZE); x+=INCREMENT) {
							byte[] nd2Bytes=tiffReader.openBytes(img,x,y, FIELD_SIZE, FIELD_SIZE);

							String csvString = getCSVString(nd2Bytes);
							csvStream.print(csvString+"\n");
							System.out.println(new Point(x,y));

						}
					}
				}
			}
		}


*/

