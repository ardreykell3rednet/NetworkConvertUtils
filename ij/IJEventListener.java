package ij;

	/** Plugins that implement this interface are notified when the user
	     changes the foreground color, changes the background color,
	     closes the color picker, closes the Log window or switches to
	     another tool.
	*/
	public interface IJEventListener {
		int FOREGROUND_COLOR_CHANGED = 0;
		int BACKGROUND_COLOR_CHANGED = 1;
		int COLOR_PICKER_CLOSED= 2;
		int LOG_WINDOW_CLOSED= 3;
		int TOOL_CHANGED= 4;

	void eventOccurred(int eventID);

}
