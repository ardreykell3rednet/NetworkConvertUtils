package src;
import GUIElements.OutWindow;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.io.Opener;
import ij.io.RoiDecoder;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

class Tester {


	public static void asd(String[] args) {

		ImagePlus img = new Opener().openImage("C:\\Neural Net Data\\FINAL\\Run 8\\Tiffs\\ND2 1\\Tiff 1.tiff");
		img.setCurrentSlice(5);
		new OutWindow().println(img.getFrame());

	}

	public static void gg(String[] args) throws FileNotFoundException {
		PointRoi noScale = (PointRoi) RoiDecoder.open(
				"C:\\Neural Net Data\\P5 Syk\\VectorROIs\\SET5XYZ.roi"
		);
		PrintStream p = new PrintStream("posFile");
		p.println(Arrays.toString(noScale.positions));
	}

	public static void ij(String[] args) {
		int FIELD_SIZE = 64;
		int INCREMENT = 8;
		int i = 0;
		for (int y = 0; y <= (512 - FIELD_SIZE); y += INCREMENT) {
			for (int x = 0; x <= (512 - FIELD_SIZE); x += INCREMENT) {
				i++;
			}
		}
		System.out.println(i);
	}
}

/*
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.PointRoi;
import ij.io.RoiDecoder;
import ij.plugin.Converter;
import ij.plugin.RoiScaler;
import ij.plugin.frame.ContrastAdjuster;
import ij.process.StackProcessor;
import loci.common.Location;
import loci.common.Region;
import loci.common.image.IImageScaler;
import loci.common.image.SimpleImageScaler;
import loci.common.services.ServiceFactory;
import loci.formats.*;
import loci.formats.in.DynamicMetadataOptions;
import loci.formats.in.ND2Reader;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.out.JPEGWriter;
import loci.formats.services.OMEXMLService;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import ome.xml.meta.OMEXMLMetadataRoot;
import ome.xml.model.Image;
import ome.xml.model.Pixels;
import ome.xml.model.primitives.PositiveInteger;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

*
 * TODO Currently working on implementing a scaling feature and seperating out color channels



public class Tester {
	final static String PATH_ND2 ="C:\\Neural Net Data\\P5 Syk\\ND2s\\P5 18_293_1 CX3;SykFF 1xTMXatP1 sox9 iba1 cd68 s1z1 60x.nd2";
	final static String SCALED_TIFF="C:\\Neural Net Data\\ScaledND2.tiff";
	final static String PATH_TIFF ="C:\\Neural Net Data\\holder.tiff";
	final static String pathOut = "C:\\Neural Net Data\\hammer.csv";

	static{
		Location loc=new Location(pathOut);
		if(loc.exists()) loc.delete();
		loc=new Location("temp.tiff");
		if(loc.exists()) loc.delete();
		loc=new Location("C:\\Neural Net Data\\out_Test.csv");
		if (loc.exists()) loc.delete();
	}


	public static void a(String[] args) throws Exception {
		ImageReader tiffReader = new ImageReader();
		tiffReader.setId(SCALED_TIFF);
		for(int i=0;i<tiffReader.getImageCount();i++)
			System.out.println(Arrays.toString(tiffReader.getZCTCoords(i)));
	}
	public static void main(String[] args) throws Exception {
		CSVPrintStream csvStream=new CSVPrintStream(new FileOutputStream(pathOut));
		ImageReader tiffReader = new ImageReader();
		tiffReader.setId("F:\\Neural Net Data\\Run 5\\Tiffs\\ND2 2\\Tiff 7.tiff");
		HyperStackWrapper hsw=new HyperStackWrapper(4,31,12);
		HSD standard = new HSD(1,11, 6);

		for (int y = 0; y <= (512 - FIELD_SIZE); y += INCREMENT) {
			for (int x = 0; x <= (512 - FIELD_SIZE); x += INCREMENT) {
				PointRoi noScale= (PointRoi) RoiDecoder.open("C:\\Neural Net Data\\P5 Syk\\VectorROIs\\SET2XYZ.roi");
				short[] pos=noScale.positions;
				PointRoi curRoi = (PointRoi) RoiScaler.scale(
						(PointRoi) noScale,
						0.5,0.5,false
				);
				short[] newPos=new short[pos.length];
				for(int i=0;i<pos.length;i++)
					newPos[i]=(short)(pos[i]-1);
				curRoi.positions=newPos;

				int[] roiIndexes = new int[ImgToCSV.MAX_ROI];
				Point[] roiPoints = curRoi.getContainedPoints();
				csvStream.print(x);
				csvStream.print(y);
				Rectangle field = new Rectangle(x, y, FIELD_SIZE, FIELD_SIZE);
				int numRois = 0;
				for (int roiPos = 0; roiPos < curRoi.getNCoordinates(); roiPos++) {
					boolean layer=hsw.getHyperStack(curRoi.getPointPosition(roiPos)).equals(standard);
					boolean loc=field.contains(roiPoints[roiPos]);

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
				int pad= ImgToCSV.MAX_ROI -(numRois*2);
				for (int i=0;i<pad;i++)
					csvStream.pad();
				byte[] nd2Bytes = tiffReader.openBytes(45, x, y, FIELD_SIZE, FIELD_SIZE);

				csvStream.println(nd2Bytes);
			}
		}
		String print=Arrays.toString(tiffReader.getZCTCoords(45));
		System.out.println(print);
	}
	public static void asd(String[] args) throws Exception {

		for(int series=0;series<12;series++) {
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
	}
	public static void asdfa(String[] args) throws Exception {

		ImageReader nd2Reader=new ImageReader();
		nd2Reader.setMetadataFiltered(true);
		nd2Reader.setOriginalMetadataPopulated(true);
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		nd2Reader.setMetadataStore(service.createOMEXMLMetadata());
		nd2Reader.setId(SCALED_TIFF);
		nd2Reader.setSeries(0);
		MetadataStore store=nd2Reader.getMetadataStore();
		MetadataTools.populatePixels(store, nd2Reader, false, false);

		IImageScaler scaler=new SimpleImageScaler();



byte[] tester=nd2Reader.get
		byte[] delZero=new byte[]


		ImageWriter writer=new ImageWriter();
		DynamicMetadataOptions options=new DynamicMetadataOptions();
		writer.setMetadataOptions(options);
		factory = new ServiceFactory();
		service = factory.getInstance(OMEXMLService.class);
		String format = service.getOMEXML(service.asRetrieve(store));
		OMEXMLMetadataRoot root = (OMEXMLMetadataRoot)store.getRoot();
		IMetadata meta = service.createOMEXMLMetadata(format);
		Image exportImage = new Image(root.getImage(0));
		Pixels exportPixels = new Pixels(root.getImage(0).getPixels());
		exportImage.setPixels(exportPixels);
		OMEXMLMetadataRoot newRoot = (OMEXMLMetadataRoot)meta.getRoot();

		while(newRoot.sizeOfImageList() > 0) {
			newRoot.removeImage(newRoot.getImage(0));
		}

		while(newRoot.sizeOfPlateList() > 0) {
			newRoot.removePlate(newRoot.getPlate(0));
		}

		newRoot.addImage(exportImage);
		meta.setRoot(newRoot);
		meta.setPixelsSizeX(new PositiveInteger(512), 0);
		meta.setPixelsSizeY(new PositiveInteger(512), 0);
		ImageReader noScale=new ImageReader();
		noScale.setId(PATH_ND2);
		noScale.setSeries(0);
		int scale = nd2Reader.getSizeX();
		int type = noScale.getPixelType();

		writer.setMetadataRetrieve(meta);
		Location loc=new Location(PATH_TIFF);
		if(loc.exists())
			loc.delete();
		writer.setId(PATH_TIFF);

		for(int i=0;i<nd2Reader.getImageCount();i++) {
			byte[] buf=noScale.openBytes(0);

			byte[] pixelVals=scaler.downsample(buf,1024,1024,2,FormatTools.getBytesPerPixel(type),noScale.isLittleEndian(), FormatTools.isFloatingPoint(type), 1, noScale.isInterleaved());

			writer.saveBytes(i,pixelVals);
		}

	}
	public static void equalityTest(String[] args) throws Exception {
		final String OG_LOC="C:\\Neural Net Data\\bftools\\idrkwhattocallit.tiff";
		final String SCALED_LOC="C:\\Neural Net Data\\bftools\\Series1.tiff";
		ImageReader ogReader=new ImageReader();
		ogReader.setId(OG_LOC);

		ImageReader scaledReader=new ImageReader();
		scaledReader.setId(SCALED_LOC);
		assert ogReader.getImageCount()==scaledReader.getImageCount();
		for(int i=0;i<ogReader.getImageCount();i++){
			System.out.println(Arrays.equals(ogReader.openBytes(i),scaledReader.openBytes(i)));
		}
	}
	public static void testConvert(String[] args) throws Exception {
		ImageReader nd2Reader=new ImageReader();
		nd2Reader.setId(PATH_ND2);
		nd2Reader.setSeries(0);
		IImageScaler scaler=new SimpleImageScaler();

		PrintStream csvStream = new PrintStream(new FileOutputStream("C:\\Neural Net Data\\trial1.csv"));
		int sizeX=nd2Reader.getSizeX();
		int sizeY=nd2Reader.getSizeY();
		int increment=FIELD_SIZE/8;
		System.out.println(sizeX);
		System.out.println(sizeY);

		for(int img=0;img<1;img++) {
			for(int y = 0; y<=(sizeY- FIELD_SIZE); y+=increment) {
				for(int x = 0; x<=(sizeX- FIELD_SIZE); x+=increment) {
							byte[] nd2Bytes=nd2Reader.openBytes(img,x,y, FIELD_SIZE, FIELD_SIZE);

							String csvString = getCSVString(nd2Bytes);
							csvStream.print(csvString+"\n");
							System.out.println(new Point(x,y));

				}
			}

		}


	}

	static String getCSVString(byte[] nd2Bytes) {
		String temp = Arrays.toString(nd2Bytes);
		return temp.substring(1, temp.length() - 2);
	}

	public static void tonsOfStuff(String[] args) throws Exception {
        ImageReader read=new ImageReader();
        ND2Reader reader= (ND2Reader) read.getReader(PATH_ND2);
        reader.setId(PATH_ND2);
        byte[] tester=reader.openBytes(0,0,0,reader.getSizeX(),reader.getSizeY());

		System.out.println(Arrays.toString(tester));

		ByteArrayToImage.convert(tester, PATH_TIFF,pathOut);

		OMEXMLMetadataRoot root = (OMEXMLMetadataRoot)reader.getMetadataStore().getRoot();
		Image image=root.getImage(0);
		image.getPixels();


		if(true) return;
		ImageWriter writer=new ImageWriter();
		IFormatWriter w = writer.getWriter(PATH_TIFF);
		MetadataRetrieve retrieve=writer.getMetadataRetrieve();
		System.out.println(retrieve.getImageCount());
		writer.setId(PATH_TIFF);
		String format=writer.getFormat();
		writer.setResolution(reader.getResolution());
		writer.setSeries(0);
		writer.setId(PATH_TIFF);
		System.out.println(reader.getSizeX());
		writer.saveBytes(0,tester);
		writer.close();

		if(true) return;
		JPEGWriter jpegWriter=new JPEGWriter();
		jpegWriter.setId(pathOut);
		System.out.println(reader.getSizeY());
		jpegWriter.saveBytes(0,tester);
		jpegWriter.close();
	}
	public static void imageReader(String[] args) throws Exception{
		ImageReader read=new ImageReader();
		String path="C:\\Neural Net Data\\P5 Syk\\P5 18_293_1 CX3;SykFF 1xTMXatP1 sox9 iba1 cd68 s1z1 60x.nd2";
		ND2Reader reader= (ND2Reader) read.getReader(path);
		reader.setId(path);
		int seriesCount=reader.getSeriesCount();
		System.out.println("Series Count: "+seriesCount);

		for (int i = 0; i < seriesCount; i++) {
			reader.setSeries(i);
			int imgCount=reader.getImageCount();
			System.out.println("Image Count: "+imgCount);
		}

		ArrayList<byte[]> arr=new ArrayList<>();
		//try (PrintStream fileStream = new PrintStream(new FileOutputStream("C:\\Neural Net Data\\haldane.csv"))) {
		//byte[][][] arr=new byte[count][][];
		for (int s = 0; s < 1; s++) {
			reader.setSeries(s);
			int imgCount = reader.getImageCount();
			for (int i = 0; i < 2; i++) {
				byte[] temporaryVar=reader.openBytes(i);
				ImageWriter imageWriter=new ImageWriter();
				String outPath=FormatTools.getFilename(s,i,reader,"C:\\Neural Net Data\\haldane.jpeg");
				imageWriter.setId(outPath);
				imageWriter.saveBytes(0,temporaryVar,new Region(reader.getSizeX(),reader.getSizeY(),reader.getOptimalTileWidth(),reader.getOptimalTileHeight()));
				imageWriter.close();
arr.add(temporaryVar);
                    String temp = Arrays.toString(temporaryVar);
                    String temp1 = temp.substring(1, temp.length() - 2);
                    fileStream.print(temp1+"\n");
                    System.out.println("Hello World");


			}
		}
		//}

		arr.forEach(bytes ->
				System.out.println(Arrays.toString(bytes))
		);
try (PrintStream fileStream = new PrintStream(new FileOutputStream("C:\\Neural Net Data\\tris.csv"))) {
            for (byte[] bytes : arr) {
                String temp = Arrays.toString(bytes);
                String temp1 = temp.substring(1, temp.length() - 2);
                fileStream.print(temp1+"\n");
                System.out.println("Hello World");
            }
        }



ImageWriter imageWriter=new ImageWriter();
        //FormatTools.getFilename();
        imageWriter.setId("C:\\Neural Net Data\\tris.tiff");
        //imageWriter.saveBytes(0,new byte[],new Region(reader.getSizeX(),reader.getSizeY(),reader.getOptimalTileWidth(),reader.getOptimalTileHeight()));
        imageWriter.close();


	}

}
*/
