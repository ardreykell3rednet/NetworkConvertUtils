package GUIElements;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

public class FileChoose implements ActionListener {

	public File[] file;
	FileFilter filter;
	Component comp;

	public FileChoose(Component comp, boolean multi, String... extensions) {
		this.comp=comp;
		if(extensions!=null&&extensions.length>0)
			filter = new FileNameExtensionFilter(Arrays.toString(extensions), extensions);
		else if(extensions[0]=="dir")

			filter= new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return "Select a directory";
			}
			
		};
		else {
			filter=null;
		}
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		JFileChooser fc=new JFileChooser("C:/");
		if(filter!=null)
			fc.addChoosableFileFilter(filter);
		fc.setMultiSelectionEnabled(true);
    	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setCurrentDirectory(new File("C:/"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.showOpenDialog(comp);
		file = fc.getSelectedFiles();
        JButton c=(JButton) e.getSource();
		if (file != null) {
			c.setText(file.length > 1 ? file.length + " files selected" : file[0].getName());
		}

	}


}
