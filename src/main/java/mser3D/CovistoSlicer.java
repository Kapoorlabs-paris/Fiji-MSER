package mser3D;

import java.awt.Rectangle;
import java.util.ArrayList;

import ij.gui.Roi;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class CovistoSlicer  {
	
	public static  RandomAccessibleInterval<FloatType> getCurrentViewLarge(RandomAccessibleInterval<FloatType> originalimg, int thirdDimension) {
		
		
		
		final FloatType type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1), originalimg.dimension(2) };
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);

	
		
		totalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);
			
		
		return totalimg;

	}
	/**
	 * Generic, type-agnostic method to create an identical copy of an Img
	 *
	 * @param currentPreprocessedimg2
	 *            - the Img to copy
	 * @return - the copy of the Img
	 */
	public static Img<UnsignedByteType> PREcopytoByteImage(final RandomAccessibleInterval<FloatType> input) {
		// create a new Image with the same properties
		// note that the input provides the size for the new image as it
		// implements
		// the Interval interface
		RandomAccessibleInterval<FloatType> inputcopy = copyImage(input);
		Normalize.normalize(Views.iterable(inputcopy), new FloatType(0), new FloatType(255));
		final UnsignedByteType type = new UnsignedByteType();
		final ImgFactory<UnsignedByteType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(inputcopy, type);
		final Img<UnsignedByteType> output = factory.create(inputcopy, type);
		// create a cursor for both images
		RandomAccess<FloatType> ranac = inputcopy.randomAccess();
		Cursor<UnsignedByteType> cursorOutput = output.cursor();

		// iterate over the input
		while (cursorOutput.hasNext()) {
			// move both cursors forward by one pixel
			cursorOutput.fwd();

			ranac.setPosition(cursorOutput);

			// set the value of this pixel of the output image to the same as
			// the input,
			// every Type supports T.set( T type )
			cursorOutput.get().set((int) Math.round(ranac.get().get()));
		}

		// return the copy
		return output;
	}
	public static Img<FloatType> copyImage(final RandomAccessibleInterval<FloatType> input) {
		// create a new Image with the same dimensions but the other imgFactory
		// note that the input provides the size for the new image by
		// implementing the Interval interface
		Img<FloatType> output = new ArrayImgFactory<FloatType>().create(input, Views.iterable(input).firstElement());

		// create a cursor that automatically localizes itself on every move
		Cursor<FloatType> cursorInput = Views.iterable(input).localizingCursor();
		RandomAccess<FloatType> randomAccess = output.randomAccess();

		// iterate over the input cursor
		while (cursorInput.hasNext()) {
			// move input cursor forward
			cursorInput.fwd();

			// set the output cursor to the position of the input cursor
			randomAccess.setPosition(cursorInput);

			// set the value of this pixel of the output image, every Type
			// supports T.set( T type )
			randomAccess.get().set(cursorInput.get());
		}

		// return the copy
		return output;
	}
	public static < T extends RealType< T > & NativeType< T >>  RandomAccessibleInterval<T> getCurrentView(RandomAccessibleInterval<T> originalimg, int thirdDimension, int thirdDimensionSize, int fourthDimension, int fourthDimensionSize) {

		final T type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1) };
		final ImgFactory<T> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<T> totalimg = factory.create(dim, type);

		if (thirdDimensionSize == 0) {

			totalimg = originalimg;
		}

		if (thirdDimensionSize > 0 && fourthDimensionSize == 0) {

			totalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);

		}
		
		if (fourthDimensionSize > 0) {
			
			RandomAccessibleInterval<T> pretotalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);
			
			totalimg = Views.hyperSlice(pretotalimg, 2, fourthDimension - 1);
		}
		
		return totalimg;

	}
	
	public static  RandomAccessibleInterval<BitType> getCurrentViewBit(RandomAccessibleInterval<BitType> originalimg, int thirdDimension, int thirdDimensionSize, int fourthDimension, int fourthDimensionSize) {

		final BitType type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1) };
		final ImgFactory<BitType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<BitType> totalimg = factory.create(dim, type);

		if (thirdDimensionSize == 0) {

			totalimg = originalimg;
		}

		if (thirdDimensionSize > 0 && fourthDimensionSize == 0) {

			totalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);

		}
		
		if (fourthDimensionSize > 0) {
			
			RandomAccessibleInterval<BitType> pretotalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);
			
			totalimg = Views.hyperSlice(pretotalimg, 2, fourthDimension - 1);
		}
		return totalimg;

	}
	
	public static  RandomAccessibleInterval<IntType> getCurrentViewInt(RandomAccessibleInterval<IntType> originalimg, int thirdDimension, int thirdDimensionSize, int fourthDimension, int fourthDimensionSize) {

		final IntType type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1) };
		final ImgFactory<IntType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<IntType> totalimg = factory.create(dim, type);

		if (thirdDimensionSize == 0) {

			totalimg = originalimg;
		}

		if (thirdDimensionSize > 0 && fourthDimensionSize == 0) {

			totalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);

		}
		
		if (fourthDimensionSize > 0) {
			
			RandomAccessibleInterval<IntType> pretotalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);
			
			totalimg = Views.hyperSlice(pretotalimg, 2, fourthDimension - 1);
		}
		
		return totalimg;

	}
	
	
	public static  RandomAccessibleInterval<BitType> getCurrentViewBitRectangle(RandomAccessibleInterval<BitType> originalimg, int thirdDimension, int thirdDimensionSize, int fourthDimension, int fourthDimensionSize, Rectangle rect) {

		final BitType type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1) };
		final ImgFactory<BitType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<BitType> totalimg = factory.create(dim, type);

     long maxY = (long)rect.getMaxY();
	 long maxX = (long)rect.getMaxY();
	 long minY = (long)rect.getMinY();
	 long minX = (long)rect.getMinX();
	 
		if (thirdDimensionSize == 0) {

			totalimg = originalimg;
		}

		if (thirdDimensionSize > 0 && fourthDimensionSize == 0) {

			totalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);

		}
		
		if (fourthDimensionSize > 0) {
			
			RandomAccessibleInterval<BitType> pretotalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);
			
			totalimg = Views.hyperSlice(pretotalimg, 2, fourthDimension - 1);
		}
		
		  RandomAccessibleInterval< BitType > view =
	                Views.interval( totalimg, new long[] { minX, minY }, new long[]{ maxX, maxY } );
		
		
		return view;

	}
	
	public static  RandomAccessibleInterval<IntType> getCurrentViewIntRectangle(RandomAccessibleInterval<IntType> originalimg, int thirdDimension, int thirdDimensionSize, int fourthDimension, int fourthDimensionSize, Rectangle rect) {

		final IntType type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1) };
		final ImgFactory<IntType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<IntType> totalimg = factory.create(dim, type);
           
		     long maxY = (long)rect.getMaxY();
			 long maxX = (long)rect.getMaxY();
			 long minY = (long)rect.getMinY();
			 long minX = (long)rect.getMinX();
		
		if (thirdDimensionSize == 0) {

			totalimg = originalimg;
		}

		if (thirdDimensionSize > 0 && fourthDimensionSize == 0) {

			totalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);

		}
		
		if (fourthDimensionSize > 0) {
			
			RandomAccessibleInterval<IntType> pretotalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);
			
			totalimg = Views.hyperSlice(pretotalimg, 2, fourthDimension - 1);
		}
		
		 RandomAccessibleInterval< IntType > view =
	                Views.interval( totalimg, new long[] { minX, minY }, new long[]{ maxX, maxY } );
		
		
		return view;

	}
	
	
	

	
	
	
	public static float computeValueFromScrollbarPosition(final int scrollbarPosition, final float min, final float max,
			final int scrollbarSize) {
		return min + (scrollbarPosition / (float) scrollbarSize) * (max - min);
	}

	public static int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Math.round(((sigma - min) / (max - min)) * scrollbarSize);
	}

	
	
	
}
