package mser3D;

import ij.IJ;
import timeGUI.CovistoTimeselectPanel;
import zGUI.CovistoZselectPanel;

public class ShowView {

	
	final InteractiveMethods parent;
	
	
	public ShowView(final InteractiveMethods parent) {
		
		this.parent = parent;
		
	}
	
	
	public void shownewZ() {

		if (CovistoZselectPanel.thirdDimension > CovistoZselectPanel.thirdDimensionSize) {
			IJ.log("Max Z stack exceeded, moving to last Z instead");
			CovistoZselectPanel.thirdDimension = CovistoZselectPanel.thirdDimensionSize;
			
			
			parent.CurrentView = CovistoSlicer.getCurrentView(parent.originalimg, (int)CovistoZselectPanel.thirdDimension,
					(int)CovistoZselectPanel.thirdDimensionSize, (int)CovistoTimeselectPanel.fourthDimension, (int)CovistoTimeselectPanel.fourthDimensionSize);
			
		} else {

			parent.CurrentView = CovistoSlicer.getCurrentView(parent.originalimg, (int)CovistoZselectPanel.thirdDimension,
					(int)CovistoZselectPanel.thirdDimensionSize, (int)CovistoTimeselectPanel.fourthDimension, (int)CovistoTimeselectPanel.fourthDimensionSize);
			
		}

		
	}

	
	
	public void shownewT() {

		if (CovistoTimeselectPanel.fourthDimension > CovistoTimeselectPanel.fourthDimensionSize) {
			IJ.log("Max time point exceeded, moving to last time point instead");
			CovistoTimeselectPanel.fourthDimension = CovistoTimeselectPanel.fourthDimensionSize;
			
			
			parent.CurrentView = CovistoSlicer.getCurrentView(parent.originalimg,(int) CovistoZselectPanel.thirdDimension,
					(int)CovistoZselectPanel.thirdDimensionSize,(int) CovistoTimeselectPanel.fourthDimension, (int)CovistoTimeselectPanel.fourthDimensionSize);
			
		} else {

			parent.CurrentView = CovistoSlicer.getCurrentView(parent.originalimg,(int) CovistoZselectPanel.thirdDimension,
					(int)CovistoZselectPanel.thirdDimensionSize, (int)CovistoTimeselectPanel.fourthDimension, (int)CovistoTimeselectPanel.fourthDimensionSize);
			
		}

		
		
	

		
	}
	
}
