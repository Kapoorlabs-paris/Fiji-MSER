package mser3D;

import java.awt.Color;

import javax.swing.JProgressBar;

public class MSERProgressBar {

	
	public static void MSERSetProgressBar(JProgressBar jpb, double percent, String message) {

		jpb.setValue((int) Math.round(percent));
		jpb.setOpaque(true);
		jpb.setStringPainted(true);
		jpb.setString(message);

	}
	
	public static void MSERSetProgressBar(JProgressBar jpb, String message) {

		
		
		jpb.setOpaque(true);
		jpb.setStringPainted(true);
		jpb.setString(message);
		
	}
	
	
	
}
