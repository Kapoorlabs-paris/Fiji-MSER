package mser3D;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import dogGUI.CovistoDogPanel;
import ij.IJ;
import mser3D.InteractiveMethods.ValueChange;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import timeGUI.CovistoTimeselectPanel;



public class PreTimeListener implements AdjustmentListener {
	final Label label;
	final String string;
	final InteractiveMethods parent;
	final float min, max;
	final int scrollbarSize;
	final JScrollBar deltaScrollbar;

	public PreTimeListener(final InteractiveMethods parent, final Label label, final String string, final float min, final float max,
			final int scrollbarSize, final JScrollBar deltaScrollbar) {
		this.label = label;
		this.parent = parent;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;

		this.deltaScrollbar = deltaScrollbar;
			deltaScrollbar.addMouseListener(new CovistoStandardMouseListener(parent, ValueChange.FOURTHDIMmouse));
	
			deltaScrollbar.setBlockIncrement(CovistoSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
			deltaScrollbar.setUnitIncrement(CovistoSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
	}

	

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		
	
		CovistoTimeselectPanel.fourthDimension = (int) Math.round(CovistoSlicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize));


		deltaScrollbar
		.setValue(CovistoSlicer.computeScrollbarPositionFromValue(CovistoTimeselectPanel.fourthDimension, min, max, scrollbarSize));
		
		label.setText(string +  " = "  + CovistoTimeselectPanel.fourthDimension);

	
		parent.panelFirst.validate();
		parent.panelFirst.repaint();
		ShowView show = new ShowView(parent);
		show.shownewT();

	}
	


}