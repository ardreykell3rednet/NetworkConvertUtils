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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

// TODO correlate ND2s and ROIs
public class ImgToCSV {

	private final String[] IMG_LOCATION;
	private final String[] ROI_LOCATION;
	private final int NUM_SECTIONS;
	private final int MAX_ROI;
	private final boolean ARCHIVE_TIFFS;

	private final int FIELD_SIZE;
	private final int INCREMENT;
	private final double SCALING_FACTOR;
	private boolean TESTING_MODE = false;
	private int seriesCount;
	private String tiff;
	private String csv;

	public ImgToCSV(String[] imgLoc, String[] roiLoc, String outLoc, boolean archive, int maxRoi, double scalingFactor, int fieldSize, int incr) throws FileNotFoundException {
		if (imgLoc == null || roiLoc == null)
			throw new IllegalArgumentException("Please pass in image and roi files");

		File img = new File(imgLoc[0]);
		File roi = new File(roiLoc[0]);

		if (img.isDirectory()) {
			IMG_LOCATION =
					arrConvert(img.listFiles());
		} else
			IMG_LOCATION = imgLoc;

		if (roi.isDirectory()) {
			ROI_LOCATION =
					arrConvert(roi.listFiles());
		} else
			ROI_LOCATION = roiLoc;


		ARCHIVE_TIFFS = archive;
		MAX_ROI = maxRoi;
		FIELD_SIZE = fieldSize;
		INCREMENT = incr;


		for (String s : IMG_LOCATION)
			if (!new File(s).exists())
				throw new FileNotFoundException("The file at " + s + " was not found");

		for (String s : ROI_LOCATION)
			if (!new File(s).exists())
				throw new FileNotFoundException("The file at " + s + " was not found");

		int i = 1;
		File runDir;
		if (outLoc != null) {
			do {
				runDir = new File(outLoc, "Run " + i);
				i++;
			} while (runDir.exists());
			runDir.mkdirs();
			makePaths(runDir);
		}

		int numFields = 0;
		for (int y = 0; y <= (512 - FIELD_SIZE); y += INCREMENT) {
			for (int x = 0; x <= (512 - FIELD_SIZE); x += INCREMENT) {
				numFields++;
			}
		}
		NUM_SECTIONS = numFields;
		SCALING_FACTOR = scalingFactor;
	}

	public ImgToCSV(File[] imgLoc, File[] roiLoc, File outDir) throws FileNotFoundException {
		this(arrConvert(imgLoc), arrConvert(roiLoc), outDir.getAbsolutePath(), false, 10, .5, 64, 8);

	}
	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc, boolean archive, int maxRoi, double scalingFactor, int fieldSize, int incr) throws FileNotFoundException {
		this(new String[]{nd2Loc}, new String[]{roiLoc}, outLoc, archive, maxRoi, scalingFactor, fieldSize, incr);
	}
	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc, boolean archive, int maxRoi, int scalingFactor) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, archive, maxRoi, scalingFactor, 64, 8);
	}
	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc, boolean archive, int maxRoi) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, archive, maxRoi, .5, 64, 8);
	}

	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc, boolean archive) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, archive, 10);
	}


	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc, int maxRoi) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, false, maxRoi);
	}

	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc) throws FileNotFoundException {
		this(nd2Loc, roiLoc, outLoc, false);
	}

	ImgToCSV(String outLoc, boolean archive) throws FileNotFoundException {
		this("C:\\Neural Net Data\\P5 Syk\\ND2s", "C:\\Neural Net Data\\P5 Syk\\VectorROIs", outLoc, archive);
	}

	ImgToCSV(String outLoc) throws FileNotFoundException {
		this(outLoc, false);
	}

	ImgToCSV(boolean archive) throws FileNotFoundException {
		this("C:\\Neural Net Data\\FINAL", archive);
	}

	public ImgToCSV() throws FileNotFoundException {
		this(false);
	}

	public static String[] arrConvert(File[] files) {
		return Arrays.stream(files)
				.map(File::getAbsolutePath)
				.distinct()
				.toArray(String[]::new);
	}

	public static void main(String[] args) throws IOException, FormatException {
		ImgToCSV run = new ImgToCSV(null);
		run.setCsv("C:\\ImgToCSV v4-16-2019\\Run 1\\CSVs");
		boolean[] channels = {false, true, true, false};

		run.writeCSVs(5, 2, 0, channels);

	}

	public void run() throws IOException, FormatException {
		writeCSVs();
	}
	public String toString() {
		return "Image: " + Arrays.toString(IMG_LOCATION) + "ROI: " + Arrays.toString(ROI_LOCATION) + "Out Directory: " + csv;
	}

	public void writeCSVs() throws IOException, FormatException {
		writeCSVs(0, 0, 0, null);
	}

	public void writeCSV() throws IOException, FormatException {
		writeCSV(0, 0, 0, null);
	}

	/*public void run(int startFile, int startSeries, int startSlice, boolean... channels) throws IOException, FormatException {

		File nd2 = new File(IMG_LOCATION);
		//File[] nd2List = nd2Folder.listFiles();
		File roi = new File(ROI_LOCATION);
		//File[] roiList = roiFolder.listFiles();

		if (nd2.isDirectory() && roi.isDirectory() && nd2.listFiles().length == roi.listFiles().length) {
			writeCSVs(startFile, startSeries, startSlice, channels);
		} else if (nd2.isFile() && roi.isFile()) {
			writeCSV(startFile, startSeries, startSlice, channels);
		}
	}

	public void run(boolean... channels) throws IOException, FormatException {
		run(0, 0, 0, channels);
	}*/

	// TODO CORRELATE ND2s with ROIs to prevent MIXUP, fix mixup
	public void writeCSVs(int startFile, int startSeries, int startSlice, boolean[] channels) throws IOException, FormatException {

		if (IMG_LOCATION.length != ROI_LOCATION.length)
			throw new IllegalArgumentException("The number of Image files must be equal to the number of ROI files");
		for (int fileIndex = startFile; fileIndex < IMG_LOCATION.length; fileIndex++) {
			File nd2 = new File(IMG_LOCATION[fileIndex]);
			File roi = new File(ROI_LOCATION[fileIndex]);

			writeCSV(fileIndex, startSeries, startSlice, channels, nd2, roi);
			startSeries = 0;

			if (TESTING_MODE)
				return;
		}
	}

	public void writeCSV(int fileNum, int startSeries, int startSlice, boolean[] channels) throws IOException, FormatException {
		writeCSV(fileNum, startSeries, startSlice, channels, new File(IMG_LOCATION[0]), new File(ROI_LOCATION[0]));
	}

	public void writeCSV(int fileNum, int startSeries, int startSlice, boolean[] channels, File nd2, File roi) throws IOException, FormatException {
		File csvLoc = new File(csv);
		csvLoc.mkdirs();
		//csv + "\\ND2 " + (fileNum + 1) + ".csv"
		Path csvPath = Path.of(csv, "ND2 " + (fileNum + 1) + ".csv");
		CSVPrintStream csvStream = new CSVPrintStream(Files.newOutputStream(csvPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
		int series = startSeries;
		do {
			ImageReader byteRead;
			ImagePlus raw = getImage(series, nd2.getAbsolutePath());
			ImagePlus img = process(raw);

			if (channels != null && channels.length != img.getNChannels())
				throw new IllegalArgumentException("Please pass in a boolean array containing the appropriate number of channels");
			if (ARCHIVE_TIFFS) {
				File tiffLoc = new File(tiff, "ND2 " + (fileNum + 1));
				tiffLoc.mkdirs();
				String tiffPath = tiffLoc.getPath() + "\\Tiff " + (series + 1) + ".tiff";
				IJ.saveAsTiff(img, tiffPath);
				byteRead = new ImageReader();
				byteRead.setId(tiffPath);
			} else {
				byteRead = new ImagePlusWrapper(img);
			}

			int sizeX = byteRead.getSizeX();
			int sizeY = byteRead.getSizeY();
			csvStream.print(byteRead.getSizeC());
			csvStream.print(byteRead.getSizeZ());
			csvStream.println(seriesCount);
			HyperStackWrapper hsw = new HyperStackWrapper(byteRead.getSizeC(), byteRead.getSizeZ(), seriesCount);
			for (int slice = startSlice; slice < byteRead.getImageCount(); slice++) {
				long timeIn = System.currentTimeMillis();
				startSlice = 0;
				System.out.println("ND2: " + fileNum + " Series: " + (series) + " Image: " + (slice));
				int[] zct = byteRead.getZCTCoords(slice);
				if (channels == null || channels[zct[1]]) {
					HSD standard = new HSD(zct[1], zct[0], series);
					csvStream.print(NUM_SECTIONS);
					csvStream.print(zct[1]);
					csvStream.print(zct[0]);
					csvStream.println(series);
					for (int y = 0; y <= (sizeY - FIELD_SIZE); y += INCREMENT) {
						for (int x = 0; x <= (sizeX - FIELD_SIZE); x += INCREMENT) {
							PointRoi noScale = (PointRoi) RoiDecoder.open(roi.getAbsolutePath());
							short[] pos = noScale.positions;
							PointRoi curRoi = (PointRoi) RoiScaler.scale(
									noScale,
									SCALING_FACTOR, SCALING_FACTOR, false
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
					System.out.println(System.currentTimeMillis() - timeIn);
				}
			}
			if (TESTING_MODE) return;
			csvStream.println("SERIES_END");

			series++;
		} while (series < seriesCount);
	}

	//TODO recognize Nd2s in mixed folder
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

	private ImagePlus getImage(int series, String file) throws IOException, FormatException {
		ImporterOptions options = new ImporterOptions();
		options.setWindowless(true);
		options.setId(file);
		options.setAutoscale(true);
		options.setSeriesOn(series, true);
		ImportProcess process = new ImportProcess(options);
		process.execute();
		seriesCount = process.getSeriesCount();
		System.out.println(seriesCount);
		ImagePlusReader reader = new ImagePlusReader(process);
		ImagePlus[] imagePluses = reader.openImagePlus();
		System.out.println(imagePluses[0].isHyperStack());
		System.out.println(imagePluses[0].getNChannels());
		return imagePluses[0];

	}

	public ImagePlus process(ImagePlus img) {
		int channel = img.getNChannels();
		int zslice = img.getNSlices();
		int tseries = img.getNFrames();
		WindowManager.setTempCurrentImage(img);
		Converter conv = new Converter();
		conv.run("8-bit");


		StackProcessor sp = new StackProcessor(img.getStack(), img.getProcessor());
		ImageStack iStack = sp.resize(512, 512, true);
		ImagePlus update = new ImagePlus("Updated Image", iStack);

		ContrastAdjuster adj = new ContrastAdjuster();
		adj.reset(update, update.getProcessor());


		return HyperStackConverter.toHyperStack(update, channel, zslice, tseries, "grayscale");
	}


	/**
	 * @return the rOI_LOCATION
	 */
	public String[] getROI_LOCATION() {
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
	public String[] getIMG_LOCATION() {
		return IMG_LOCATION;
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

	public void setTESTING_MODE(boolean TESTING_MODE) {
		this.TESTING_MODE = TESTING_MODE;
	}

	/**
	 * @return the seriesCount
	 */
	public int getSeriesCount() {
		return seriesCount;
	}

	public void setSeriesCount(int seriesCount) {
		this.seriesCount = seriesCount;
	}

	/**
	 * @return the tiff
	 */
	public String getTiff() {
		return tiff;
	}

	public void setTiff(String tiff) {
		this.tiff = tiff;
	}

	/**
	 * @return the csv
	 */
	public String getCsv() {
		return csv;
	}

	public void setCsv(String csv) {
		this.csv = csv;
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
				for (int IMG_LOCATION = 0; IMG_LOCATION < tiffReader.getImageCount(); IMG_LOCATION++) {
					for(int y = 0; y<=(sizeY- FIELD_SIZE); y+=INCREMENT) {
						for(int x = 0; x<=(sizeX- FIELD_SIZE); x+=INCREMENT) {
							byte[] nd2Bytes=tiffReader.openBytes(IMG_LOCATION,x,y, FIELD_SIZE, FIELD_SIZE);

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
public class ImgToCSV {
	final String IMG_LOCATION;
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

	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc, int fieldSize, int incr, int maxRoi) {
		IMG_LOCATION = nd2Loc;
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

	public ImgToCSV(String nd2Loc, String roiLoc, String outLoc) {
		this(nd2Loc, roiLoc, outLoc, 64, 8, 10);
	}

	private ImgToCSV() {
		this("C:\\Neural Net Data\\P5 Syk\\ND2s", "C:\\Neural Net Data\\P5 Syk\\VectorROIs", "C:\\Neural Net Data\\FINAL", 64, 8, 10);
	}

	public static void main(String[] args) {
		ImgToCSV run = new ImgToCSV();
		try {
			run.writeCSVs();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// TODO CORRELATE ND2s with ROIs to prevent MIXUP, fix mixup
	public void writeCSVs() throws Exception {
		File nd2Folder = new File(IMG_LOCATION);
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
				ImagePlus IMG_LOCATION = process(raw);
				ImagePlus hyperstack = HyperStackConverter.toHyperStack(IMG_LOCATION, 4, 31, 1, "grayscale");
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

	public ImagePlus process(ImagePlus IMG_LOCATION) {
		WindowManager.setTempCurrentImage(IMG_LOCATION);
		Converter conv = new Converter();
		conv.run("8-bit");

		StackProcessor sp = new StackProcessor(IMG_LOCATION.getStack(), IMG_LOCATION.getProcessor());
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
				for (int IMG_LOCATION = 0; IMG_LOCATION < tiffReader.getImageCount(); IMG_LOCATION++) {
					for(int y = 0; y<=(sizeY- FIELD_SIZE); y+=INCREMENT) {
						for(int x = 0; x<=(sizeX- FIELD_SIZE); x+=INCREMENT) {
							byte[] nd2Bytes=tiffReader.openBytes(IMG_LOCATION,x,y, FIELD_SIZE, FIELD_SIZE);

							String csvString = getCSVString(nd2Bytes);
							csvStream.print(csvString+"\n");
							System.out.println(new Point(x,y));

						}
					}
				}
			}
		}


*/

