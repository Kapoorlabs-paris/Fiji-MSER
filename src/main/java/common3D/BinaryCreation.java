package common3D;

import java.util.ArrayList;

import dogGUI.CovistoDogPanel;
import ij.gui.Roi;
import mser3D.InteractiveMethods;
import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.algorithm.region.Circles;
public class BinaryCreation  {

	
	public static <T extends RealType<T> & NativeType<T>> void  CreateBinary(final InteractiveMethods parent, RandomAccessibleInterval<T> inputimg, RandomAccessibleInterval<BitType> outimg, int z, int t) {

			
			ArrayList<double[]> pointlist = parent.AllEvents.get(z); 
			
			for (double[] currentpoint : pointlist) {

				Point point = new Point(2);
				point.setPosition(new long[] {(long)currentpoint[0], (long)currentpoint[1]});
				Circles.add(Views.expandZero(outimg, new long[] {(int)CovistoDogPanel.distthreshold + 1, (int)CovistoDogPanel.distthreshold + 1}), point, (int)CovistoDogPanel.distthreshold, new BitType(true));
		

		}
		
		
	}

	

	public static <T extends RealType<T> & NativeType<T>> void  CreateBinaryDots(final InteractiveMethods parent, RandomAccessibleInterval<T> inputimg, RandomAccessibleInterval<BitType> outimg, int z, int t) {

		Cursor<T> incursor = Views.iterable(inputimg).localizingCursor();
		RandomAccess<BitType> outran = outimg.randomAccess();


		
		
		while (incursor.hasNext()) {

			incursor.fwd();
			outran.setPosition(incursor);

			
			ArrayList<double[]> pointlist = parent.AllEvents.get(z); 
			
			for (double[] currentpoint : pointlist) {

				if ( incursor.getIntPosition(0) == (int)Math.round(currentpoint[0])  &&  incursor.getIntPosition(1) == (int)Math.round(currentpoint[1])  ) {

					
					  HyperSphere< BitType > hyperSphere = new HyperSphere<BitType>( outimg, incursor, 2 );
					  
		                // set every value inside the sphere to 1
		                for ( BitType value : hyperSphere )
		                    value.setOne();
		                

				}

			}

		}
		
		
	}
	
public static <T extends RealType<T> & NativeType<T>> void  CreateBinaryRoi(final InteractiveMethods parent, RandomAccessibleInterval<T> inputimg, RandomAccessibleInterval<BitType> outimg, final ArrayList<Roi> Rois, int z, int t) {

	Cursor<T> incursor = Views.iterable(inputimg).localizingCursor();
	RandomAccess<BitType> outran = outimg.randomAccess();

	while (incursor.hasNext()) {

		incursor.fwd();
		outran.setPosition(incursor);

		for (Roi currentroi : Rois) {

			if (currentroi.contains(incursor.getIntPosition(0), incursor.getIntPosition(1))) {

				outran.get().setOne();

			}

		}

	}
}
}