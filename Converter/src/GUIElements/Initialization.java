package GUIElements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Initialization {

	public boolean netTrain;
	public boolean cellIdent;
	private JFrame frmInitialization;

	/**
	 * Create the application.
	 */
	public Initialization() {
		initialize();
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Initialization window = new Initialization();
					window.frmInitialization.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmInitialization = new JFrame();
		frmInitialization.setTitle("Initialization");
		frmInitialization.setBounds(100, 100, 592, 435);
		frmInitialization.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmInitialization.getContentPane().setLayout(null);

		JLabel lblWelcomeToThe = new JLabel("Welcome to the Convolutional Neural Network!");
		lblWelcomeToThe.setBounds(15, 16, 398, 20);
		frmInitialization.getContentPane().add(lblWelcomeToThe);

		JLabel lblPleaseSelectWhich = new JLabel("Please select which Network function you would like to use today.");
		lblPleaseSelectWhich.setBounds(15, 52, 537, 20);
		frmInitialization.getContentPane().add(lblPleaseSelectWhich);

		JRadioButton rdbtnNetworkTraining = new JRadioButton("NETWORK TRAINING");
		rdbtnNetworkTraining.setBounds(15, 84, 222, 29);
		frmInitialization.getContentPane().add(rdbtnNetworkTraining);
		rdbtnNetworkTraining.addActionListener(e -> netTrain = true);
		JRadioButton rdbtnNetworkCellIdentification = new JRadioButton("NETWORK CELL IDENTIFICATION");
		rdbtnNetworkCellIdentification.setBounds(15, 121, 301, 29);
		frmInitialization.getContentPane().add(rdbtnNetworkCellIdentification);
		rdbtnNetworkCellIdentification.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cellIdent = true;
			}
		});
		JButton btnConfirm = new JButton("CONFIRM");
		btnConfirm.addActionListener(arg0 -> {
			frmInitialization.setVisible(false);
			if (cellIdent && !netTrain) {
				ChannelSelection channel = new ChannelSelection('I');
			} else if (netTrain && !cellIdent) {
				ChannelSelection channel = new ChannelSelection('T');
			} else {
				frmInitialization.setVisible(true);
				cellIdent = false;
				netTrain = false;
			}
		});
		btnConfirm.setBounds(201, 162, 115, 29);
		frmInitialization.getContentPane().add(btnConfirm);

	}
}
