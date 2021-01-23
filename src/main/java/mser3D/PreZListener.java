package mser3D;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import ij.IJ;
import mser3D.InteractiveMethods.ValueChange;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import watershedGUI.CovistoWatershedPanel;
import zGUI.CovistoZselectPanel;


public class PreZListener implements AdjustmentListener {
		final Label label;
		final String string;
		final InteractiveMethods parent;
		final float min, max;
		final int scrollbarSize;
		final JScrollBar deltaScrollbar;

		public PreZListener(final InteractiveMethods parent, final Label label, final String string, final float min, final float max,
				final int scrollbarSize, final JScrollBar deltaScrollbar) {
			this.label = label;
			this.parent = parent;
			this.string = string;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.deltaScrollbar = deltaScrollbar;
				deltaScrollbar.addMouseListener(new CovistoStandardMouseListener(parent, ValueChange.THIRDDIMmouse));
			
				
				deltaScrollbar.setBlockIncrement(CovistoSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
				deltaScrollbar.setUnitIncrement(CovistoSlicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
		}



		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			
			CovistoZselectPanel.thirdDimension = (int) Math.round(CovistoSlicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize));
			
			deltaScrollbar
			.setValue(CovistoSlicer.computeScrollbarPositionFromValue(CovistoZselectPanel.thirdDimension, min, max, scrollbarSize));



			label.setText(string +  " = "  + CovistoZselectPanel.thirdDimension);
			
			
			
			
			CovistoZselectPanel.inputFieldZ.setText(Integer.toString((int)CovistoZselectPanel.thirdDimension));
			parent.panelFirst.validate();
			parent.panelFirst.repaint();
			
			
			ShowView show = new ShowView(parent);
			show.shownewZ();
			
		}
		
	
	
}