package GUIElements;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChoose implements ActionListener {
	
	public File file;
	FileNameExtensionFilter filter;
	Component comp;
	public FileChoose(Component comp,String...extensions) {
		this.comp=comp;
		if(extensions!=null&&extensions.length>0)
			filter=new FileNameExtensionFilter("Image Filter", extensions);
		else
			filter=null;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		JFileChooser fc=new JFileChooser("C:/");
		if(filter!=null)
			fc.addChoosableFileFilter(filter);
    	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setCurrentDirectory(new File("C:/"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.showOpenDialog(comp);
        file = fc.getSelectedFile();
        JButton c=(JButton) e.getSource();
        c.setText(file.getName());
	}

}
