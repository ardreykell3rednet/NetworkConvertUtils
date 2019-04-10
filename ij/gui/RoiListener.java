package ij.gui;
import ij.ImagePlus;
	
	/** Plugins that implement this interface are notified when
		an ROI is created, modified or deleted. The 
		Plugins/Utilities/Monitor Events command uses this interface.
	*/
	public interface RoiListener {
		int CREATED = 1;
		int MOVED = 2;
		int MODIFIED = 3;
		int EXTENDED = 4;
		int COMPLETED = 5;
		int DELETED = 6;

	void roiModified(ImagePlus imp, int id);

}
