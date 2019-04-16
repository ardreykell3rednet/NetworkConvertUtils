package src;

import java.io.File;

public class JNIWrapper {
	static {
		System.loadLibrary("NeuralNet");
	}

	public JNIWrapper(String netName, String netPath, String dataDir, int startImg) {
		new File(netPath).mkdirs();
		String[] files = ImgToCSV.arrConvert(new File(dataDir).listFiles());
		runNetwork(netName, netPath, dataDir, files, startImg);
	}

	private native String runNetwork(String netName, String netPath, String dataDir, String[] dataFiles, int startImg);

}
