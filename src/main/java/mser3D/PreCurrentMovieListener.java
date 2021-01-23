package mser3D;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ij.IJ;


public class PreCurrentMovieListener implements ActionListener {

	final MSERFileChooser  parent;

	public PreCurrentMovieListener(MSERFileChooser  parent) {

		this.parent = parent;

	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {

		parent.impA = IJ.getImage();
		
		if(parent.impA!=null)
		parent.DoneCurr(parent.Cardframe);
	}

}
