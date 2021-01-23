package mser3D;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import mser3D.MserAll;

public class PREZMserListener implements ActionListener {
	
	final InteractiveMethods parent;
	
	public PREZMserListener(final InteractiveMethods parent) {
		
		this.parent = parent;
	}
	


	@Override
	public void actionPerformed(final ActionEvent arg0) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				go();

			}

		});

	}

	public void go() {

	

		MserAll dosnake = new MserAll(parent);
		dosnake.execute();

	}

}
