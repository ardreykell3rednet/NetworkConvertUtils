package GUIElements;

import javax.swing.*;

public class ChannelSelection {

	private JFrame frmChannelSelection;
	private JTextField textField;
	private boolean chan1;
	private boolean chan2;
	private boolean chan3;
	private boolean chan4;
	private char input;

	/**
	 * Create the application.
	 */
	public ChannelSelection(char input) {
		this.input = input;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmChannelSelection = new JFrame();
		frmChannelSelection.setVisible(true);
		frmChannelSelection.setTitle("Channel Selection");
		frmChannelSelection.setBounds(100, 100, 751, 708);
		frmChannelSelection.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmChannelSelection.getContentPane().setLayout(null);

		JLabel lblPleaseSelectWhich = new JLabel("Please select which channels you would like the network to examine.");
		lblPleaseSelectWhich.setBounds(49, 51, 533, 20);
		frmChannelSelection.getContentPane().add(lblPleaseSelectWhich);

		JCheckBox channel1 = new JCheckBox("Channel 1 (Hoechst)");
		channel1.setBounds(49, 83, 211, 29);
		frmChannelSelection.getContentPane().add(channel1);
		channel1.addActionListener(e -> chan1 = true);

		JCheckBox channel2 = new JCheckBox("Channel 2");
		channel2.setBounds(49, 120, 139, 29);
		frmChannelSelection.getContentPane().add(channel2);
		channel2.addActionListener(e -> chan2 = true);

		JCheckBox channel3 = new JCheckBox("Channel 3");
		channel3.setBounds(49, 157, 139, 29);
		frmChannelSelection.getContentPane().add(channel3);
		channel3.addActionListener(e -> chan3 = true);

		JCheckBox channel4 = new JCheckBox("Channel 4");
		channel4.setBounds(49, 194, 139, 29);
		frmChannelSelection.getContentPane().add(channel4);
		channel4.addActionListener(e -> chan4 = true);

		JButton btnConfirm = new JButton("CONFIRM");
		btnConfirm.addActionListener(arg0 -> {
			frmChannelSelection.setVisible(false);
			if (chan1 || chan2 || chan3 || chan4) {
				FileSelectorWindow fsw = new FileSelectorWindow(chan1, chan2, chan3, chan4);
			}
		});
		btnConfirm.setBounds(201, 162, 115, 29);
		frmChannelSelection.getContentPane().add(btnConfirm);
	}
}
