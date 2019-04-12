package GUIElements;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class OutWindow {
	static int loc = 10;
	private JTextArea field;
	private JScrollPane myJScrollPane;
	private JFrame frame;

	public OutWindow() {
		this("Window " + loc / 10);
	}

	public OutWindow(String s) {
		field = new JTextArea("");
		myJScrollPane = new JScrollPane(field,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame = new JFrame(s);
		loc += 50;
		field.setEditable(false);
		frame.setLocation(loc, loc);
		SwingUtilities.invokeLater(this::createGUI);

	}

	public void onTop(boolean top) {
		frame.setAlwaysOnTop(top);
	}

	private void createGUI() {

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(new Dimension(512, 512));

		frame.getContentPane().add(myJScrollPane);

		//Display the window.
		frame.setVisible(true);
	}

	public void println(String s) {
		field.append(s + "\n");
		field.setCaretPosition(field.getDocument().getLength());
	}

	public void println(int[] arr) {
		println(Arrays.toString(arr));
	}

	public void println(short[] arr) {
		println(Arrays.toString(arr));
	}

	public void println(byte[] arr) {
		println(Arrays.toString(arr));
	}

	public void println(char[] arr) {
		println(Arrays.toString(arr));
	}

	public void println(long[] arr) {
		println(Arrays.toString(arr));
	}

	public void println(int i) {
		println(String.valueOf(i));
	}

	public void println(double b) {
		println(String.valueOf(b));
	}
}
