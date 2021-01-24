package mser3D;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mser3D.InteractiveMethods.ValueChange;
import mserGUI.CovistoMserPanel;

public class FindMinimaMserListener implements ItemListener {
	
	final InteractiveMethods  parent;
	public FindMinimaMserListener( InteractiveMethods parent) {
		
		this.parent = parent;
	}
	



	@Override
	public void itemStateChanged(final ItemEvent arg0) {
		
		
		
		
		if (arg0.getStateChange() == ItemEvent.DESELECTED) {
			CovistoMserPanel.darktobright = false;
			CovistoMserPanel.brighttodark = false;
		} else if (arg0.getStateChange() == ItemEvent.SELECTED) {
			CovistoMserPanel.darktobright = false;
			CovistoMserPanel.brighttodark = true;
			parent.updatePreview(ValueChange.MSER);
		}

	}
	

}

