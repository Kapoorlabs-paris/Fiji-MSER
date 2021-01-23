package mserMethods;

import java.awt.Font;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;

import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.PointSampleList;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.DefaultROIStrategyFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingROIStrategy;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.RealSum;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

public class FinderUtils{

	public static ArrayList<Roi> getcurrentRois(ArrayList<RefinedPeak<Point>> peaks, double sigma, double sigma2) {

		ArrayList<Roi> Allrois = new ArrayList<Roi>();

		for (final RefinedPeak<Point> peak : peaks) {
			float x = (float) (peak.getFloatPosition(0));
			float y = (float) (peak.getFloatPosition(1));

			final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma), Util.round(sigma + sigma2),
					Util.round(sigma + sigma2));

			Allrois.add(or);

		}

		return Allrois;

	}

	public static double getNumberofPixels(RandomAccessibleInterval<FloatType> source, Roi roi) {

		double NumberofPixels = 0;

		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		final double[] position = new double[source.numDimensions()];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				NumberofPixels++;

			}

		}

		return NumberofPixels;

	}

	
	public static long[] GetMaxcorners(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] maxVal = { inputimg.min(0), inputimg.min(1) };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				for (int d = 0; d < n; ++d) {

					final long p = intCursor.getLongPosition(d);
					if (p > maxVal[d])
						maxVal[d] = p;

				}

			}
		}

		return maxVal;

	}
	
	public static Roi CreateBigRoi (final Roi currentroi, final RandomAccessibleInterval<FloatType> currentimg, final double radius){
		
		
		
		double width = currentroi.getFloatWidth();
		double height = currentroi.getFloatHeight();
		
		double[] center = getCenter(currentroi, currentimg);
		
	
			Roi Bigroi = new OvalRoi(Util.round(center[0] -(width + radius)/2), Util.round(center[1] - (height + radius)/2 ), Util.round(width + radius),
					Util.round(height + radius));
			
		      if(radius == 0 )
		    	  Bigroi = currentroi;
			
		        
		        return Bigroi;
		
		
	}
	
	public static double[] getCenter(Roi roi, final RandomAccessibleInterval<FloatType> source) {

		double Intensity = 0;
		double[] center = new double[3];
		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();
		double SumX = 0;
		double SumY = 0;
		final double[] position = new double[source.numDimensions()];
		int count = 0;
		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {
				SumX += currentcursor.getDoublePosition(0) * currentcursor.get().getRealDouble();
				SumY += currentcursor.getDoublePosition(1) * currentcursor.get().getRealDouble();
				Intensity += currentcursor.get().getRealDouble();
                 count++;
			}

		}
		center[0] = SumX / Intensity;
		center[1] = SumY / Intensity;

		center[2] = 0;

		return center;

	}
	public static long[] GetMincorners(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] minVal = { inputimg.max(0), inputimg.max(1) };
		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				for (int d = 0; d < n; ++d) {

					final long p = intCursor.getLongPosition(d);
					if (p < minVal[d])
						minVal[d] = p;
				}

			}
		}

		return minVal;

	}

	public static double GetBoundingbox(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] position = new long[n];
		long[] minVal = { inputimg.max(0), inputimg.max(1) };
		long[] maxVal = { inputimg.min(0), inputimg.min(1) };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}

			}
		}

		double boxsize = Distance(minVal, maxVal);

		Pair<long[], long[]> boundingBox = new ValuePair<long[], long[]>(minVal, maxVal);
		return boxsize;
	}

	public static int GetMaxlabelsseeded(RandomAccessibleInterval<IntType> intimg) {

		// To get maximum Labels on the image
		Cursor<IntType> intCursor = Views.iterable(intimg).cursor();
		int currentLabel = 1;
		boolean anythingFound = true;
		while (anythingFound) {
			anythingFound = false;
			intCursor.reset();
			while (intCursor.hasNext()) {
				intCursor.fwd();
				int i = intCursor.get().get();
				if (i == currentLabel) {

					anythingFound = true;

				}
			}
			currentLabel++;
		}

		return currentLabel;

	}

	public static NativeImgLabeling<Integer, IntType> GetlabeledImage(RandomAccessibleInterval<FloatType> inputimg,
			NativeImgLabeling<Integer, IntType> seedLabeling) {

		int n = inputimg.numDimensions();
		long[] dimensions = new long[n];

		for (int d = 0; d < n; ++d)
			dimensions[d] = inputimg.dimension(d);
		final NativeImgLabeling<Integer, IntType> outputLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		final Watershed<FloatType, Integer> watershed = new Watershed<FloatType, Integer>();

		watershed.setSeeds(seedLabeling);
		watershed.setIntensityImage(inputimg);
		watershed.setStructuringElement(AllConnectedComponents.getStructuringElement(2));
		watershed.setOutputLabeling(outputLabeling);
		watershed.process();
		DefaultROIStrategyFactory<Integer> deffactory = new DefaultROIStrategyFactory<Integer>();
		LabelingROIStrategy<Integer, Labeling<Integer>> factory = deffactory
				.createLabelingROIStrategy(watershed.getResult());
		outputLabeling.setLabelingCursorStrategy(factory);

		return outputLabeling;

	}

	public static Pair<RandomAccessibleInterval<FloatType>, FinalInterval>  CurrentLabelImage(RandomAccessibleInterval<FloatType> img, EllipseRoi roi){
		
		int n = img.numDimensions();
		long[] position = new long[n];
		long[] minVal = { img.max(0), img.max(1) };
		long[] maxVal = { img.min(0), img.min(1) };
		
		Cursor<FloatType> localcursor = Views.iterable(img).localizingCursor();
		
		while (localcursor.hasNext()) {
			localcursor.fwd();
			int x = localcursor.getIntPosition(0);
			int y = localcursor.getIntPosition(1);
			if (roi.contains(x, y)){

				localcursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				
			}
		}
		FinalInterval interval = new FinalInterval(minVal, maxVal);
		RandomAccessibleInterval<FloatType> currentimgsmall = Views.interval(img, interval);
		
		Pair<RandomAccessibleInterval<FloatType>, FinalInterval> pair = new ValuePair<RandomAccessibleInterval<FloatType>, FinalInterval>(currentimgsmall, interval);
		
		return pair;
	}
	
	
	
	
/*	
public static RandomAccessibleInterval<FloatType> CurrentLabeloffsetImage(ArrayList<CommonOutput> imgs, final EllipseRoi roi,int label) {
		
		RandomAccessibleInterval<FloatType> currentimg = imgs.get(label).Actualroi;
		int n = currentimg.numDimensions();
		long[] position = new long[n];
		long[] minVal = { currentimg.max(0), currentimg.max(1) };
		long[] maxVal = { currentimg.min(0), currentimg.min(1) };
		
		Cursor<FloatType> localcursor = Views.iterable(currentimg).localizingCursor();

		while (localcursor.hasNext()) {
			localcursor.fwd();
			int x = localcursor.getIntPosition(0);
			int y = localcursor.getIntPosition(1);
			if (roi.contains(x, y)){

				localcursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				
			}
		}
		
		FinalInterval interval = new FinalInterval(minVal, maxVal) ;
		RandomAccessibleInterval<FloatType> currentimgsmall = Views.offsetInterval(currentimg, interval);
		return currentimgsmall;
		
	}
*/

public static FinalInterval  CurrentroiInterval(RandomAccessibleInterval<FloatType> currentimg, final  EllipseRoi roi){
	
	int n = currentimg.numDimensions();
	long[] position = new long[n];
	long[] minVal = { currentimg.max(0), currentimg.max(1) };
	long[] maxVal = { currentimg.min(0), currentimg.min(1) };
	
	Cursor<FloatType> localcursor = Views.iterable(currentimg).localizingCursor();

	while (localcursor.hasNext()) {
		localcursor.fwd();
		int x = localcursor.getIntPosition(0);
		int y = localcursor.getIntPosition(1);
		if (roi.contains(x, y)){

			localcursor.localize(position);
			for (int d = 0; d < n; ++d) {
				if (position[d] < minVal[d]) {
					minVal[d] = position[d];
				}
				if (position[d] > maxVal[d]) {
					maxVal[d] = position[d];
				}

			}
			
		}
	}
	
	FinalInterval interval = new FinalInterval(minVal, maxVal) ;
	
	return interval;
	
}

public static FinalInterval CurrentroiInterval(RandomAccessibleInterval<FloatType> currentimg, Roi roi) {
	int n = currentimg.numDimensions();
	long[] position = new long[n];
	long[] minVal = { currentimg.max(0), currentimg.max(1) };
	long[] maxVal = { currentimg.min(0), currentimg.min(1) };
	
	Cursor<FloatType> localcursor = Views.iterable(currentimg).localizingCursor();

	while (localcursor.hasNext()) {
		localcursor.fwd();
		int x = localcursor.getIntPosition(0);
		int y = localcursor.getIntPosition(1);
		if (roi.contains(x, y)){

			localcursor.localize(position);
			for (int d = 0; d < n; ++d) {
				if (position[d] < minVal[d]) {
					minVal[d] = position[d];
				}
				if (position[d] > maxVal[d]) {
					maxVal[d] = position[d];
				}

			}
			
		}
	}
	
	FinalInterval interval = new FinalInterval(minVal, maxVal) ;
	
	return interval;
}
	public static Pair<RandomAccessibleInterval<FloatType>, FinalInterval> CurrentLabeloffsetImagepair(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();
		long[] minVal = { originalimg.max(0), originalimg.max(1) };
		long[] maxVal = { originalimg.min(0), originalimg.min(1) };
		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label

		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				imageRA.get().set(inputRA.get());

			}

		}
		FinalInterval intervalsmall = new FinalInterval(minVal, maxVal) ;

		RandomAccessibleInterval<FloatType> outimgsmall = Views.offsetInterval(outimg, intervalsmall);

		Pair<RandomAccessibleInterval<FloatType>, FinalInterval> pair = new ValuePair<RandomAccessibleInterval<FloatType>, FinalInterval>(outimgsmall, intervalsmall);
		return pair;

	}

	
	public static Pair<RandomAccessibleInterval<FloatType>, FinalInterval> CurrentLabelImagepair(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();
		long[] minVal = { originalimg.max(0), originalimg.max(1) };
		long[] maxVal = { originalimg.min(0), originalimg.min(1) };
		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label

		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				imageRA.get().set(inputRA.get());

			}

		}
		FinalInterval intervalsmall = new FinalInterval(minVal, maxVal) ;
		RandomAccessibleInterval<FloatType> outimgsmall = Views.interval(outimg, intervalsmall);

		Pair<RandomAccessibleInterval<FloatType>, FinalInterval> pair = new ValuePair<RandomAccessibleInterval<FloatType>, FinalInterval>(outimgsmall, intervalsmall);
		return pair;

	}
	
	public static RandomAccessibleInterval<FloatType> CurrentLabelImage(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();

		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label
		
		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				intCursor.localize(position);
				
				imageRA.get().set(inputRA.get());

			}

		}
	

		return outimg;

	}
	
	public static RandomAccessibleInterval<FloatType> CurrentLabeloffsetImage(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();
	
		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label

		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				
				imageRA.get().set(inputRA.get());

			}

		}
		

		return outimg;

	}
	
	
	public static double Distance(final long[] minCorner, final long[] maxCorner) {

		double distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return Math.sqrt(distance);
	}
	
	
	
	public static int CummulativeDistance (final double[] pointT, final double[] pointTp1, final double[] pointTp2,   final double oldlength){
		
		int grow = 0;

		
		
		if ((pointTp2[0] < pointTp1[0]) && (pointTp2[0] > pointT[0]) && (pointTp2[0] < pointTp1[0]) && (pointTp2[0] > pointT[0])  )
			grow = -1;
		
		
		
			
			
		return grow;
		
		
	}
	
	public static float Distancesq(final double[] minCorner, final double[] maxCorner) {

		float distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return distance;
	}
	
	public static double Distance(final double[] minCorner, final double[] maxCorner) {

		double distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return Math.sqrt(distance);
	}
	
	public static double VelocityX(final double[] oldpoint, final double[] newpoint) {

		double Velocity = 0;

		int d = 0;

			Velocity = (-oldpoint[d] + newpoint[d]);

		
		return Velocity;
	}
	
	public static double VelocityY(final double[] oldpoint, final double[] newpoint) {

		double Velocity = 0;

		int d = oldpoint.length - 1;

			Velocity = (-oldpoint[d] + newpoint[d]);

		
		return Velocity;
	}

	public static < T extends RealType< T > & NativeType< T >>  Pair<ArrayList<Roi>, ArrayList<Double>> getKNearestRois(final RandomAccessibleInterval<T> currentimg,
			ArrayList<Roi> Allrois, Roi kdtreeroi, int k) {

		ArrayList<Roi> KnearestRoi = new ArrayList<Roi>();
		ArrayList<Double> Knearestdist = new ArrayList<Double>();
		Roi Knear = null;

		ArrayList<Pair<Double, Roi>> distRoi = new ArrayList<Pair<Double, Roi>>();
		double[] kdcenter = getCenter(currentimg, kdtreeroi);

		

			for (int index = 0; index < Allrois.size(); ++index) {

				double[] roicenter = getCenter(currentimg, Allrois.get(index));

				Pair<Double, Roi> distpair = new ValuePair<Double, Roi>(Distance(kdcenter, roicenter), Allrois.get(index));
				
				distRoi.add(distpair);

			}
			
			Comparator<Pair<Double, Roi>> distcomparison = new Comparator<Pair<Double, Roi>>() {

				@Override
				public int compare(final Pair<Double, Roi> A, final Pair<Double, Roi> B) {

					double diff= A.getA() - B.getA();
					
					if ( diff < 0 )
						return -1;
					else if ( diff > 0 )
						return 1;
					else
						return 0;
				}

			};
			
			Collections.sort(distRoi, distcomparison);
			
			for (int i = 0; i < k ; ++i){
				
				KnearestRoi.add(distRoi.get(i).getB());
				Knearestdist.add(distRoi.get(i).getA());
			}
			
		

		return new ValuePair<ArrayList<Roi>, ArrayList<Double>>(KnearestRoi, Knearestdist);

	}

	public static < T extends RealType< T > & NativeType< T >>  ArrayList<Roi> getKNearestRoisaboveCut(final RandomAccessibleInterval<T> currentimg,
			ArrayList<Roi> Allrois, Roi kdtreeroi, double distthresh, int k) {

		ArrayList<Roi> KnearestRoi = new ArrayList<Roi>();
		ArrayList<Roi> KnearestclearRoi = new ArrayList<Roi>();
		ArrayList<Double> Knearestdist = new ArrayList<Double>();
		Roi Knear = null;

		ArrayList<Pair<Double, Roi>> distRoi = new ArrayList<Pair<Double, Roi>>();
		double[] kdcenter = getCenter(currentimg, kdtreeroi);

		

			for (int index = 0; index < Allrois.size(); ++index) {

				double[] roicenter = getCenter(currentimg, Allrois.get(index));

				Pair<Double, Roi> distpair = new ValuePair<Double, Roi>(Distance(kdcenter, roicenter), Allrois.get(index));
				
				distRoi.add(distpair);

			}
			
			Comparator<Pair<Double, Roi>> distcomparison = new Comparator<Pair<Double, Roi>>() {

				@Override
				public int compare(final Pair<Double, Roi> A, final Pair<Double, Roi> B) {

					double diff= A.getA() - B.getA();
					
					if ( diff < 0 )
						return -1;
					else if ( diff > 0 )
						return 1;
					else
						return 0;
				}

			};
			
			Collections.sort(distRoi, distcomparison);
			
			for (int i = 0; i < k ; ++i){
				
				KnearestRoi.add(distRoi.get(i).getB());
				Knearestdist.add(distRoi.get(i).getA());
			}
			
			for (int i = 0; i < Knearestdist.size(); ++i) {
				
				if(Knearestdist.get(i) > distthresh)
					KnearestclearRoi.add(KnearestRoi.get(i));
				
			}
			
			
			
		

		return KnearestclearRoi;

	}

	
	public static < T extends RealType< T > & NativeType< T >>   Pair<double[], Boolean> mergeNearestRois(final RandomAccessibleInterval<T> currentimg, ArrayList<double[]> Allrois, double[] Clickedpoint, double distthresh) {


		boolean merged = false;
		double[] KDtreeroi = new double[Clickedpoint.length];

		
		final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Allrois.size());
		final List<FlagNode<double[]>> targetNodes = new ArrayList<FlagNode<double[]>>(Allrois.size());
		for (int index = 0; index < Allrois.size(); ++index) {

			double[] r = Allrois.get(index);
			 
			 
			 targetCoords.add( new RealPoint(r[0], r[1] ) );
			 

			targetNodes.add(new FlagNode<double[]>(Allrois.get(index)));

		}


		if (targetNodes.size() > 0 && targetCoords.size() > 0) {

			final KDTree<FlagNode<double[]>> Tree = new KDTree<FlagNode<double[]>>(targetNodes, targetCoords);

			final NNFlagsearchKDtree<double[]> Search = new NNFlagsearchKDtree<double[]>(Tree);


				final double[] source = Clickedpoint;
				final RealPoint sourceCoords = new RealPoint(source);
				Search.search(sourceCoords);
				
				final FlagNode<double[]> targetNode = Search.getSampler().get();

				KDtreeroi = targetNode.getValue();
		}
		

		
		
		if(KDtreeroi!=null) {
			
		double[] roicenter = KDtreeroi;
		
		double[] mergepoint = new double[roicenter.length];
		
		
		double distance = Distance(Clickedpoint, roicenter);
		if (distance < distthresh) {
		
			
			merged = true;
		}
		else
			merged = false;
			
			mergepoint = roicenter;
		

		return new ValuePair<double[], Boolean>(mergepoint, merged);
		}
		
		else return null;
		
	}
	
	
	public static Roi getNearestRois(ArrayList<Roi> Allrois, double[] Clickedpoint) {

		Roi KDtreeroi = null;

		final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Allrois.size());
		final List<FlagNode<Roi>> targetNodes = new ArrayList<FlagNode<Roi>>(Allrois.size());
		for (int index = 0; index < Allrois.size(); ++index) {

			 Roi r = Allrois.get(index);
			 Rectangle rect = r.getBounds();
			 
			 targetCoords.add( new RealPoint(rect.x + rect.width/2.0, rect.y + rect.height/2.0 ) );
			 

			targetNodes.add(new FlagNode<Roi>(Allrois.get(index)));

		}

		if (targetNodes.size() > 0 && targetCoords.size() > 0) {

			final KDTree<FlagNode<Roi>> Tree = new KDTree<FlagNode<Roi>>(targetNodes, targetCoords);

			final NNFlagsearchKDtree<Roi> Search = new NNFlagsearchKDtree<Roi>(Tree);


				final double[] source = Clickedpoint;
				final RealPoint sourceCoords = new RealPoint(source);
				Search.search(sourceCoords);
				final FlagNode<Roi> targetNode = Search.getSampler().get();

				KDtreeroi = targetNode.getValue();

		}

		return KDtreeroi;
	}

	public static double getIntensity(RandomAccessibleInterval<FloatType> source, Roi roi) {

		double Intensity = 0;

		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		final double[] position = new double[source.numDimensions()];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				Intensity += currentcursor.get().getRealDouble();

			}

		}

		return Intensity;

	}

	public static< T extends RealType< T > & NativeType< T >>  double[] getCenter(RandomAccessibleInterval<T> source, Roi roi) {

		double Intensity = 0;
		double[] center = new double[source.numDimensions()];
		Cursor<T> currentcursor = Views.iterable(source).localizingCursor();
		double SumX = 0;
		double SumY = 0;
		final double[] position = new double[source.numDimensions()];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {
				SumX += currentcursor.getDoublePosition(0) * currentcursor.get().getRealDouble();
				SumY += currentcursor.getDoublePosition(1) * currentcursor.get().getRealDouble();
				Intensity += currentcursor.get().getRealDouble();

			}

		}
		center[0] = SumX / Intensity;
		center[1] = SumY / Intensity;

		

		return center;

	}
	public static < T extends RealType< T > & NativeType< T >> ArrayList<Roi> getcurrentRois(MserTree<T> newtree) {

		ArrayList<double[]> meanandcovchildlist = new ArrayList<double[]>();
		ArrayList<double[]> meanandcovlist = new ArrayList<double[]>();
		ArrayList<double[]> redmeanandcovlist = new ArrayList<double[]>();
		final HashSet<Mser<T>> rootset = newtree.roots();
		
		
		final Iterator<Mser<T>> rootsetiterator = rootset.iterator();
		
		
		final ArrayList<double[]> AllmeanCovar = new ArrayList<double[]>();
		
		while (rootsetiterator.hasNext()) {

			Mser<T> rootmser = rootsetiterator.next();

			if (rootmser.size() > 0) {

				final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1], rootmser.cov()[0],
						rootmser.cov()[1], rootmser.cov()[2] };
				meanandcovlist.add(meanandcov);
			}
		}
		
		// We do this so the ROI remains attached the the same label and is not changed if the program is run again
	   
	       final Iterator<Mser<T>> treeiterator = newtree.iterator();
	       
	       while (treeiterator.hasNext()) {

				Mser<T> mser = treeiterator.next();
				//System.out.println(mser.getChildren().size());
				if (mser.getChildren().size()  > 1) {

					for (int index = 0; index < mser.getChildren().size(); ++index) {

						final double[] meanandcovchild = { mser.getChildren().get(index).mean()[0],
								mser.getChildren().get(index).mean()[1], mser.getChildren().get(index).cov()[0],
								mser.getChildren().get(index).cov()[1], mser.getChildren().get(index).cov()[2] };

						meanandcovchildlist.add(meanandcovchild);
						AllmeanCovar.add(meanandcovchild);
						
					}

				}

			}
	       redmeanandcovlist = meanandcovlist;
	       
	       /*
	        * Remove parent, not always a good idea
	        * 
			for (int childindex = 0; childindex < meanandcovchildlist.size(); ++childindex) {

				final double[] meanchild = new double[] { meanandcovchildlist.get(childindex)[0],
						meanandcovchildlist.get(childindex)[1] };

				for (int index = 0; index < meanandcovlist.size(); ++index) {

					final double[] mean = new double[] { meanandcovlist.get(index)[0], meanandcovlist.get(index)[1] };
					final double[] covar = new double[] { meanandcovlist.get(index)[2], meanandcovlist.get(index)[3],
							meanandcovlist.get(index)[4] };
					final EllipseRoi ellipse = createEllipse(mean, covar, 3);

					if (ellipse.contains((int) meanchild[0], (int) meanchild[1]))
						redmeanandcovlist.remove(index);

				}

			}
			*/

			for (int index = 0; index < redmeanandcovlist.size(); ++index) {

				final double[] meanandcov = new double[] { redmeanandcovlist.get(index)[0], redmeanandcovlist.get(index)[1],
						redmeanandcovlist.get(index)[2], redmeanandcovlist.get(index)[3], redmeanandcovlist.get(index)[4] };
				AllmeanCovar.add(meanandcov);

			}

		ArrayList<Roi> Allrois = new ArrayList<Roi>();

		
		for (int index = 0; index < AllmeanCovar.size(); ++index) {

			final double[] mean = { AllmeanCovar.get(index)[0], AllmeanCovar.get(index)[1] };
			final double[] covar = { AllmeanCovar.get(index)[2], AllmeanCovar.get(index)[3],
					AllmeanCovar.get(index)[4] };

		     Roi roi = createEllipse(mean, covar, 3);

			Allrois.add(roi);

		}

		return Allrois;
	}


	public static ArrayList<Roi> getcurrentRoiswoChild(MserTree<UnsignedByteType> newtree) {

		final HashSet<Mser<UnsignedByteType>> rootset = newtree.roots();

		ArrayList<Roi> Allrois = new ArrayList<Roi>();
		final Iterator<Mser<UnsignedByteType>> rootsetiterator = rootset.iterator();

		ArrayList<double[]> AllmeanCovar = new ArrayList<double[]>();

		while (rootsetiterator.hasNext()) {

			Mser<UnsignedByteType> rootmser = rootsetiterator.next();

			if (rootmser.size() > 0) {

				final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1], rootmser.cov()[0],
						rootmser.cov()[1], rootmser.cov()[2] };
				AllmeanCovar.add(meanandcov);

			}
		}

		// We do this so the ROI remains attached the the same label and is not
		// changed if the program is run again
		SortListbyproperty.sortpointList(AllmeanCovar);
		for (int index = 0; index < AllmeanCovar.size(); ++index) {

			final double[] mean = { AllmeanCovar.get(index)[0], AllmeanCovar.get(index)[1] };
			final double[] covar = { AllmeanCovar.get(index)[2], AllmeanCovar.get(index)[3],
					AllmeanCovar.get(index)[4] };

			EllipseRoi roi = createEllipse(mean, covar, 3);
			Allrois.add(roi);

		}

		return Allrois;

	}

	public static RandomAccessibleInterval<FloatType> getCurrentView(RandomAccessibleInterval<FloatType> originalimgA,
			int thirdDimension) {

		final FloatType type = originalimgA.randomAccess().get().createVariable();
		long[] dim = { originalimgA.dimension(0), originalimgA.dimension(1) };
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimgA, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);

		totalimg = Views.hyperSlice(originalimgA, 2, thirdDimension - 1);

		return totalimg;

	}
	public static <T extends RealType<T>> double[] Transformback(double[] location, double[] size, double[] min,
			double[] max) {

		int n = location.length;

		double[] delta = new double[n];

		final double[] realpos = new double[n];

		for (int d = 0; d < n; ++d){
			
			delta[d] = (max[d] - min[d]) / size[d];
		    
			realpos[d] = (location[d] - min[d]) / delta[d];
		}
		return realpos;

	}
	/**
	 * Extract the current 2d region of interest from the souce image
	 * 
	 * @param CurrentView
	 *            - the CurrentView image, a {@link Image} which is a copy of
	 *            the {@link ImagePlus}
	 * 
	 * @return
	 */

	public static RandomAccessibleInterval<FloatType> extractImage(
			final RandomAccessibleInterval<FloatType> intervalView, FinalInterval interval) {
/*
		final FloatType type = intervalView.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(intervalView, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(intervalView, type);

		final RandomAccessibleInterval<FloatType> img = Views.interval(intervalView, interval);
		double[] newmin = Transformback(new double[]{img.min(0), img.min(1)}, 
				new double[]{totalimg.dimension(0), totalimg.dimension(1)},
				new double[]{img.min(0), img.min(1)},
				new double[]{img.max(0), img.max(1)});
		
		double[] newmax = Transformback(new double[]{img.max(0), img.max(1)}, 
				new double[]{totalimg.dimension(0), totalimg.dimension(1)},
				new double[]{totalimg.min(0), totalimg.min(1)},
				new double[]{totalimg.max(0), totalimg.max(1)});
		long[] newminlong = new long[]{Math.round(newmin[0]), Math.round(newmin[1])};
		long[] newmaxlong = new long[]{Math.round(newmax[0]), Math.round(newmax[1])};
		
		RandomAccessibleInterval<FloatType> outimg = factory.create(new FinalInterval(newminlong, newmaxlong), type);
		RandomAccess<FloatType> ranac = outimg.randomAccess();
		final Cursor<FloatType> cursor = Views.iterable(img).localizingCursor();
		
		while(cursor.hasNext()){
			
			cursor.fwd();
			
			double[] newlocation = Transformback(new double[]{cursor.getDoublePosition(0), cursor.getDoublePosition(1)}, 
					new double[]{totalimg.dimension(0), totalimg.dimension(1)},
					new double[]{totalimg.min(0), totalimg.min(1)},
					new double[]{totalimg.max(0), totalimg.max(1)});
			long[] newlocationlong = new long[]{Math.round(newlocation[0]), Math.round(newlocation[1])};
			ranac.setPosition(newlocationlong);
			ranac.get().set(cursor.get());
			
		}
		
		
		
		//totalimg = Views.interval(Views.extendBorder(img), intervalView);
*/
		return intervalView;
	}

	public static RandomAccessibleInterval<FloatType> oldextractImage(
			final RandomAccessibleInterval<FloatType> intervalView, FinalInterval interval) {

		final FloatType type = intervalView.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(intervalView, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(intervalView, type);

		final RandomAccessibleInterval<FloatType> img = Views.interval(intervalView, interval);
		double[] newmin = Transformback(new double[]{img.min(0), img.min(1)}, 
				new double[]{totalimg.dimension(0), totalimg.dimension(1)},
				new double[]{img.min(0), img.min(1)},
				new double[]{img.max(0), img.max(1)});
		
		double[] newmax = Transformback(new double[]{img.max(0), img.max(1)}, 
				new double[]{totalimg.dimension(0), totalimg.dimension(1)},
				new double[]{totalimg.min(0), totalimg.min(1)},
				new double[]{totalimg.max(0), totalimg.max(1)});
		long[] newminlong = new long[]{Math.round(newmin[0]), Math.round(newmin[1])};
		long[] newmaxlong = new long[]{Math.round(newmax[0]), Math.round(newmax[1])};
		
		RandomAccessibleInterval<FloatType> outimg = factory.create(new FinalInterval(newminlong, newmaxlong), type);
		RandomAccess<FloatType> ranac = outimg.randomAccess();
		final Cursor<FloatType> cursor = Views.iterable(img).localizingCursor();
		
		while(cursor.hasNext()){
			
			cursor.fwd();
			
			double[] newlocation = Transformback(new double[]{cursor.getDoublePosition(0), cursor.getDoublePosition(1)}, 
					new double[]{totalimg.dimension(0), totalimg.dimension(1)},
					new double[]{totalimg.min(0), totalimg.min(1)},
					new double[]{totalimg.max(0), totalimg.max(1)});
			long[] newlocationlong = new long[]{Math.round(newlocation[0]), Math.round(newlocation[1])};
			ranac.setPosition(newlocationlong);
			ranac.get().set(cursor.get());
			
		}
		
		
		
		totalimg = Views.interval(Views.extendBorder(img), intervalView);

		return intervalView;
	}
	
	/**
	 * Generic, type-agnostic method to create an identical copy of an Img
	 *
	 * @param currentPreprocessedimg2
	 *            - the Img to copy
	 * @return - the copy of the Img
	 */
	public static Img<UnsignedByteType> copytoByteImage(final RandomAccessibleInterval<FloatType> input) {
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

	public static ArrayList<double[]> getRoiMean(MserTree<UnsignedByteType> newtree) {

		final HashSet<Mser<UnsignedByteType>> rootset = newtree.roots();

		final Iterator<Mser<UnsignedByteType>> rootsetiterator = rootset.iterator();

		ArrayList<double[]> AllmeanCovar = new ArrayList<double[]>();

		while (rootsetiterator.hasNext()) {

			Mser<UnsignedByteType> rootmser = rootsetiterator.next();

			if (rootmser.size() > 0) {

				final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1] };
				AllmeanCovar.add(meanandcov);

			}
		}

		// We do this so the ROI remains attached the the same label and is not
		// changed if the program is run again
		SortListbyproperty.sortpointList(AllmeanCovar);

		return AllmeanCovar;

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

	public static Float AutomaticThresholding(RandomAccessibleInterval<FloatType> inputimg) {

		FloatType max = new FloatType();
		FloatType min = new FloatType();
		RandomAccessibleInterval<FloatType> inputimgcopy = copy(inputimg);
		Float ThresholdNew, Thresholdupdate;

		max = computeMaxIntensity(inputimgcopy);
		min = computeMinIntensity(inputimgcopy);

		ThresholdNew = (max.get() - min.get()) / 2;

		// Get the new threshold value after segmenting the inputimage with
		// thresholdnew
		Thresholdupdate = SegmentbyThresholding(Views.iterable(inputimgcopy), ThresholdNew);

		while (true) {

			ThresholdNew = SegmentbyThresholding(Views.iterable(inputimgcopy), Thresholdupdate);

			// Check if the new threshold value is close to the previous value
			if (Math.abs(Thresholdupdate - ThresholdNew) < 1.0E-2)
				break;
			Thresholdupdate = ThresholdNew;
		}

		return ThresholdNew;

	}

	public static Img<FloatType> copy(final RandomAccessibleInterval<FloatType> input) {
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

	public static FloatType computeMaxIntensity(final RandomAccessibleInterval<FloatType> inputimg) {
		// create a cursor for the image (the order does not matter)
		final Cursor<FloatType> cursor = Views.iterable(inputimg).cursor();

		// initialize min and max with the first image value
		FloatType type = cursor.next();
		FloatType max = type.copy();

		// loop over the rest of the data and determine min and max value
		while (cursor.hasNext()) {
			// we need this type more than once
			type = cursor.next();

			if (type.compareTo(max) > 0) {
				max.set(type);

			}
		}

		return max;
	}

	public static FloatType computeMinIntensity(final RandomAccessibleInterval<FloatType> inputimg) {
		// create a cursor for the image (the order does not matter)
		final Cursor<FloatType> cursor = Views.iterable(inputimg).cursor();

		// initialize min and max with the first image value
		FloatType type = cursor.next();
		FloatType min = type.copy();

		// loop over the rest of the data and determine min and max value
		while (cursor.hasNext()) {
			// we need this type more than once
			type = cursor.next();

			if (type.compareTo(min) < 0) {
				min.set(type);

			}
		}

		return min;
	}

	// Segment image by thresholding, used to determine automatic thresholding
	// level
	public static Float SegmentbyThresholding(IterableInterval<FloatType> inputimg, Float Threshold) {

		int n = inputimg.numDimensions();
		Float ThresholdNew;
		PointSampleList<FloatType> listA = new PointSampleList<FloatType>(n);
		PointSampleList<FloatType> listB = new PointSampleList<FloatType>(n);
		Cursor<FloatType> cursor = inputimg.localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();

			if (cursor.get().get() < Threshold) {
				Point newpointA = new Point(n);
				newpointA.setPosition(cursor);
				listA.add(newpointA, cursor.get().copy());
			} else {
				Point newpointB = new Point(n);
				newpointB.setPosition(cursor);
				listB.add(newpointB, cursor.get().copy());
			}
		}
		final RealSum realSumA = new RealSum();
		long countA = 0;

		for (final FloatType type : listA) {
			realSumA.add(type.getRealDouble());
			++countA;
		}

		final double sumA = realSumA.getSum() / countA;

		final RealSum realSumB = new RealSum();
		long countB = 0;

		for (final FloatType type : listB) {
			realSumB.add(type.getRealDouble());
			++countB;
		}

		final double sumB = realSumB.getSum() / countB;

		ThresholdNew = (float) (sumA + sumB) / 2;

		return ThresholdNew;

	}

	/**
	 * 2D correlated Gaussian
	 * 
	 * @param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return ImageJ roi
	 */
	public static EllipseRoi createEllipse(final double[] mean, final double[] cov, final double nsigmas) {

		final double a = cov[0];
		final double b = cov[1];
		final double c = cov[2];
		final double d = Math.sqrt(a * a + 4 * b * b - 2 * a * c + c * c);
		final double scale1 = Math.sqrt(0.5 * (a + c + d)) * nsigmas;
		final double scale2 = Math.sqrt(0.5 * (a + c - d)) * nsigmas;
		final double theta = 0.5 * Math.atan2((2 * b), (a - c));
		final double x = mean[0];
		final double y = mean[1];
		final double dx = scale1 * Math.cos(theta);
		final double dy = scale1 * Math.sin(theta);
		final EllipseRoi ellipse = new EllipseRoi(x - dx, y - dy, x + dx, y + dy, scale2 / scale1);
		return ellipse;
	}
	

	

	
}
