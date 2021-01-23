package mser3D;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import mser3D.InteractiveMethods.ValueChange;
import mserGUI.CovistoMserPanel;

public class PREDeltaListener implements AdjustmentListener {
	
	final Label label;
	final String string;
	final InteractiveMethods parent;
	final float min, max;
	final int scrollbarSize;
	final JScrollBar scrollbar;
	
	
	public PREDeltaListener(final InteractiveMethods parent, final Label label, final String string, final float min, final float max, final int scrollbarSize, final JScrollBar scrollbar) {
		
		this.parent = parent;
		this.label = label;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;
		this.scrollbar = scrollbar;
		scrollbar.addMouseListener( new CovistoStandardMouseListener( parent, ValueChange.MSER ) );
		scrollbar.setBlockIncrement(CovistoSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
		scrollbar.setUnitIncrement(CovistoSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
	}
	
	
	




	@Override
	public void adjustmentValueChanged(final AdjustmentEvent event) {
		CovistoMserPanel.delta = ETrackScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			scrollbar.setValue(ETrackScrollbarUtils.computeScrollbarPositionFromValue(CovistoMserPanel.delta, min, max, scrollbarSize));

			label.setText(string +  " = "  + parent.nf.format(CovistoMserPanel.delta));

	
	
	}
	
	

}
