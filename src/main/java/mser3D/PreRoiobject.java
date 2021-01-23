package mser3D;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ij.gui.Roi;
import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

public class PreRoiobject extends AbstractEuclideanSpace implements RealLocalizable, Comparable<PreRoiobject> {

	/*
	 * FIELDS
	 */

	public static AtomicInteger IDcounter = new AtomicInteger( -1 );

	/** Store the individual features, and their values. */
	private final ConcurrentHashMap< String, Double > features = new ConcurrentHashMap< String, Double >();

	/** A user-supplied name for this spot. */
	private String name;

	/** This spot ID. */
	private final int ID;
	
	public Roi rois;
	public double[] geometriccenter;
	public double area;
	public double totalintensity;
	public double averageintensity;
	public int thirdDimension;
	public int fourthDimension;
	
	
	public PreRoiobject(final Roi rois, final double[] geometriccenter, final double area, final double totalintensity, final double averageintensity, final int thirdDimension, final int fourthDimension) {
		super(3);
		this.rois = rois;
		this.geometriccenter = geometriccenter;
		this.area = area;
		this.totalintensity = totalintensity;
		this.averageintensity = averageintensity;
		this.thirdDimension = thirdDimension;
		this.fourthDimension = fourthDimension;
		this.ID = IDcounter.incrementAndGet();
		putFeature( Time,Double.valueOf( fourthDimension ) );
		putFeature( Z,Double.valueOf( thirdDimension ) );
		putFeature( XPOSITION, Double.valueOf( geometriccenter[0] ) );
		putFeature( YPOSITION, Double.valueOf( geometriccenter[1] ) );
		
		putFeature( ZPOSITION, Double.valueOf( geometriccenter[2] ) );
	}
	/*
	 * STATIC KEYS
	 */

	



	/** The name of the blob X position feature. */
	public static final String XPOSITION = "XPOSITION";

	/** The name of the blob Y position feature. */
	public static final String YPOSITION = "YPOSITION";
	
	/** The name of the blob Y position feature. */
	public static final String ZPOSITION = "ZPOSITION";
	
	
	/** The label of the blob position feature. */
	public static final String LABEL = "LABEL";

	/** The name of the frame feature. */
	public static final String Time = "Time";
	
	/** The name of the Z feature. */
	public static final String Z = "Z";
	
	public final Double getFeature( final String feature )
	{
		return features.get( feature );
	}

	/**
	 * Stores the specified feature value for this spot.
	 *
	 * @param feature
	 *            the name of the feature to store, as a {@link String}.
	 * @param value
	 *            the value to store, as a {@link Double}. Using
	 *            <code>null</code> will have unpredicted outcomes.
	 */
	public final void putFeature( final String feature, final Double value )
	{
		features.put( feature, value );
	}
	public static < T extends RealType< T > & NativeType< T >> Pair<Double, Integer> getIntensity(Roi roi, RandomAccessibleInterval<T> source) {

		double Intensity = 0;
        int NumberofPixels = 0;
		Cursor<T> currentcursor = Views.iterable(source).localizingCursor();

		final double[] position = new double[source.numDimensions()];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				Intensity += currentcursor.get().getRealDouble();

				NumberofPixels++;
			}

		}

		
		
		return new ValuePair<Double, Integer>(Intensity, NumberofPixels);

	}

	/**
	 * Returns the Intnesity weighted squared distance between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the Intensity weighted distance to the current blob to target
	 *         blob specified.
	 */

	public double IntensityDistanceTo(PreRoiobject target) {
		// Returns squared distance between the source Blob and the target Blob.

		

		double IntensityDistance =  Math.pow((totalintensity - target.totalintensity), 2);

		return IntensityDistance;
	}
	/**
	 * Returns the Noramlized cost function based on ratio of pixels between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the ratio of pixels of the current blob to target blob specified.
	 */

	public double numberofPixelsRatioTo(PreRoiobject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final int sourcePixels = (int) area;
		final int targetPixels = (int) target.area;

		
		if (targetPixels > 0){
		double ratio = sourcePixels/ targetPixels;

		double sigma = 10; 
		double cost = 0;
		double coeff = 1 - Math.exp(-1/(4 * sigma));
		double a = -4*Math.log(coeff);
		
		
		if (ratio > 0  && ratio <= 0.5)
			cost = Math.exp(-a * ratio *ratio);
		if (ratio > 0.5 && ratio <= 1.5)
			cost = 1 - Math.exp(- (ratio - 1) *(ratio - 1)/ sigma);
		if (ratio > 1.5 && ratio <= 2)
			cost = Math.exp(-a * (ratio - 2)* (ratio - 2));
		else
			cost = 1;
		
		

		return cost;
		}
		
		else
			return 0;
	}
	
	/**
	 * Returns the Normalized combo cost function based on ratio of pixels b/w blobs and the Normalized square distances between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the Normalized distance to the current blob to target blob specified.
	 */

	public double NormalizedPixelratioandDistanceTo(PreRoiobject target, final double alpha, final double beta) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = geometriccenter;
		final double[] targetLocation = target.geometriccenter;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}
		
		double cost = distance / (1 + distance);
		
		final int sourcePixels = (int) area;
		final int targetPixels = (int) target.area;

		if (targetPixels > 0){
		double ratio = sourcePixels/ targetPixels;

		double sigma = 10; 
		double ratiocost = 0;
		double coeff = 1 - Math.exp(-(0.2 * 0.2)/(sigma));
		double a = -(1.0/(0.8 * 0.8))*Math.log(coeff);
		
		
		if (ratio > 0  && ratio <= 0.5)
			ratiocost = Math.exp(-a * ratio * ratio);
		if (ratio > 0.5 && ratio <= 1.5)
			ratiocost = 1 - Math.exp(- (ratio - 1) * (ratio - 1)/ sigma);
		if (ratio > 1.5 && ratio <= 2)
			ratiocost = Math.exp(-a * (ratio - 2) * (ratio - 2));
		else
			ratiocost = 1;
		
		double combinedcost = (alpha * cost + beta * ratiocost) / (alpha + beta);
		

		return combinedcost;
		}
		
		else
			
			return 0;
	}
	/**
	 * Returns the difference between the location of two blobs, this operation
	 * returns ( <code>A.diffTo(B) = - B.diffTo(A)</code>)
	 *
	 * @param target
	 *            the Blob to compare to.
	 * @param int
	 *            n n = 0 for X- coordinate, n = 1 for Y- coordinate
	 * @return the difference in co-ordinate specified.
	 */
	public double diffTo(final PreRoiobject target, int n) {

		final double thisBloblocation = geometriccenter[n];
		final double targetBloblocation = target.geometriccenter[n];
		return thisBloblocation - targetBloblocation;
	}
	/**
	 * Returns the squared distance between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the distance to the current blob to target blob specified.
	 */

	public double squareDistanceTo(PreRoiobject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = geometriccenter;
		final double[] targetLocation = target.geometriccenter;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}

		return distance;
	}

	@Override
	public int compareTo(PreRoiobject o) {

		return hashCode() - o.hashCode();
	}

	@Override
	public double getDoublePosition(int d) {
		
		return (float) getDoublePosition(d);
	}

	@Override
	public float getFloatPosition(int d) {
		
		return (float) getDoublePosition(d);
	}

	@Override
	public void localize(float[] position) {
		int n = position.length;
		for (int d = 0; d < n; ++d)
			position[d] = getFloatPosition(d);
		
	}

	@Override
	public void localize(double[] position) {
		int n = position.length;
		for (int d = 0; d < n; ++d)
			position[d] = getFloatPosition(d);
		
	}
	

}
