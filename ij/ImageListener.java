package ij;

	/** Plugins that implement this interface are notified when
		an image is opened, closed or updated. The 
		Plugins/Utilities/Monitor Events command uses this interface.
	*/
	public interface ImageListener {

	void imageOpened(ImagePlus imp);

	void imageClosed(ImagePlus imp);

	void imageUpdated(ImagePlus imp);

}
