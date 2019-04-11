package GUIElements;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JSplitPane;

import loci.formats.FormatException;
import src.ImgToCSV;
import java.awt.Color;

public class Hello {
	public static void main(String[] args) {
		new Hello();
	}
	public Hello() {
		openGUI();
	}
	/**
	 * @wbp.parser.entryPoint
	 */
	public void openGUI() {
		JFrame frame=new JFrame();
		frame.getContentPane().setLayout(null);
		frame.setSize(512, 300);
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 434, 33);
		frame.getContentPane().add(panel);
		
		JLabel lblChooseImage = new JLabel("Choose Image:");
		panel.add(lblChooseImage);
		
		JButton imgChoose = new JButton("Select a File or Folder...");
		FileChoose img=new FileChoose(frame,"nd2","tiff");
		imgChoose.addActionListener(img);
		
		
		panel.add(imgChoose);
		
		
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(0, 44, 434, 33);
		frame.getContentPane().add(panel_1);
		
		JLabel lblChooseRoi = new JLabel("Choose ROI:");
		panel_1.add(lblChooseRoi);
		FileChoose roi=new FileChoose(frame,"roi");
		JButton roiChoose = new JButton("Select a File or Folder...");
		roiChoose.addActionListener(roi);
		panel_1.add(roiChoose);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(0, 88, 434, 33);
		frame.getContentPane().add(panel_2);
		
		JLabel lblChooseOutputLocation = new JLabel("Choose Output Location");
		panel_2.add(lblChooseOutputLocation);
		FileChoose outDir=new FileChoose(frame);
		JButton outChoose = new JButton("Select a File or Folder...");
		outChoose.addActionListener(outDir);
		panel_2.add(outChoose);
		
		JButton confirm = new JButton("Confirm");
		JLabel lblPleaseAssignA = new JLabel("Please assign a file to all the fields");
		lblPleaseAssignA.setForeground(Color.RED);
		lblPleaseAssignA.setBounds(145, 223, 171, 14);
		confirm.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if(img.file!=null&&roi.file!=null&&outDir.file!=null) {
        			try {
        				ImgToCSV itc=new ImgToCSV(img.file.getAbsolutePath(), roi.file.getAbsolutePath(), outDir.file.getAbsolutePath());
        				itc.setTESTING_MODE(true);
        				System.out.println(itc);
        				
        			} catch (IOException e1) {
        				// TODO Auto-generated catch block
        				e1.printStackTrace();
        			}
        			//nextStep
        		}
        		else {
        			
        			frame.getContentPane().add(lblPleaseAssignA);
        		}
     
        	}
 
        });
		confirm.setBounds(170, 166, 113, 46);
		frame.getContentPane().add(confirm);
		
		
		frame.setVisible(true);
		
	}
}

