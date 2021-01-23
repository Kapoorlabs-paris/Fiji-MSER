package mser3D;

import java.awt.TextComponent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import ij.IJ;
import mser3D.InteractiveMethods.ValueChange;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import zGUI.CovistoZselectPanel;


public class PreZlocListener implements TextListener {

	final InteractiveMethods parent;

	boolean pressed;
	public PreZlocListener(final InteractiveMethods parent, boolean pressed) {

		this.parent = parent;

		this.pressed = pressed;
		
	}


	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent) e.getSource();

		
		tc.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				
				if (arg0.getKeyChar() == KeyEvent.VK_ENTER ) {
					
					
					pressed = false;
					
				}
				
				
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				String s = tc.getText();
				if (arg0.getKeyChar() == KeyEvent.VK_ENTER && !pressed) {
					pressed = true;
					if (CovistoZselectPanel.thirdDimension > CovistoZselectPanel.thirdDimensionSize) {
						IJ.log("Max frame number exceeded, moving to last frame instead");
						CovistoZselectPanel.thirdDimension = CovistoZselectPanel.thirdDimensionSize;
					} else
						CovistoZselectPanel.thirdDimension = Integer.parseInt(s);
					ShowView show = new ShowView(parent);
					show.shownewZ();
					
					CovistoZselectPanel.zText.setText("Current Z = " + CovistoZselectPanel.thirdDimension);
					CovistoZselectPanel.zgenText.setText("Current Z / T = " + CovistoZselectPanel.thirdDimension);
					parent.updatePreview(ValueChange.THIRDDIMmouse);
					
					
				}
				CovistoZselectPanel.zslider.setValue(CovistoSlicer.computeScrollbarPositionFromValue(CovistoZselectPanel.thirdDimension, CovistoZselectPanel.thirdDimensionsliderInit, 
						CovistoZselectPanel.thirdDimensionSize, CovistoZselectPanel.scrollbarSize));
				CovistoZselectPanel.zslider.repaint();
				CovistoZselectPanel.zslider.validate();

			}
		});
		
		

	}
}