package mser3D;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ij.gui.Roi;
import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.RealLocalizable;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

public class ThreeDRoiobject extends AbstractEuclideanSpace implements RealLocalizable, Comparable<ThreeDRoiobject> {

	
	
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
	
	public ArrayList<PreRoiobject> roiobject;
	public double[] geometriccenter;
	public double volume;
	public double totalintensity;
	public double averageintensity;
	public int fourthDimension;
	
	public ThreeDRoiobject(final ArrayList<PreRoiobject> roiobject, final double[] geometriccenter, final double volume, final double totalintensity, final double averageintensity, final int fourthDimension) {
		super(3);
		this.roiobject = roiobject;
		this.geometriccenter = geometriccenter;
		this.volume = volume;
		this.totalintensity = totalintensity;
		this.averageintensity = averageintensity;
		this.fourthDimension = fourthDimension;
		this.ID = IDcounter.incrementAndGet();
		putFeature( Time,Double.valueOf( fourthDimension ) );
		putFeature( XPOSITION, Double.valueOf( geometriccenter[0] ) );
		putFeature( YPOSITION, Double.valueOf( geometriccenter[1] ) );
		putFeature( ZPOSITION, Double.valueOf( geometriccenter[2] ) );
		putFeature( Size, Double.valueOf( volume ) );
	
			this.name = "ID" + ID;
	}
	
	
	/*
	 * STATIC KEYS
	 */
	/**
	 * Set the name of this Spot.
	 * 
	 * @param name
	 *            the name to use.
	 */
	public void setName( final String name )
	{
		this.name = name;
	}

	public int ID()
	{
		return ID;
	}

	@Override
	public String toString()
	{
		String str;
		if ( null == name || name.equals( "" ) )
			str = "ID" + ID;
		else
			str = name;
		return str;
	}

	/** The name of the blob X position feature. */
	public static final String XPOSITION = "XPOSITION";

	/** The name of the blob Y position feature. */
	public static final String YPOSITION = "YPOSITION";
	
	/** The name of the blob Y position feature. */
	public static final String ZPOSITION = "ZPOSITION";
	/** The name of the blob Y position feature. */
	public static final String Size = "Size";
	
	/** The label of the blob position feature. */
	public static final String LABEL = "LABEL";

	/** The name of the frame feature. */
	public static final String Time = "Time";
	
	
	
	/** The position features. */
	public final static String[] POSITION_FEATURES = new String[] { XPOSITION, YPOSITION, ZPOSITION };
	
	/** The name of the Z feature. */
	public static final String Z = "Z";
	
	public final Double getFeature( final String feature )
	{
		return features.get( feature );
	}
	/**
	 * The 7 privileged spot features that must be set by a spot detector:
	 * {@link #QUALITY}, {@link #POSITION_X}, {@link #POSITION_Y},
	 * {@link #POSITION_Z}, {@link #POSITION_Z}, {@link #RADIUS}, {@link #FRAME}
	 * .
	 */
	public final static Collection< String > FEATURES = new ArrayList< >( 4 );

	/** The 7 privileged spot feature names. */
	public final static Map< String, String > FEATURE_NAMES = new HashMap< >( 4 );

	/** The 7 privileged spot feature short names. */
	public final static Map< String, String > FEATURE_SHORT_NAMES = new HashMap< >( 4 );

	/** The 7 privileged spot feature dimensions. */
	public final static Map< String, Dimension > FEATURE_DIMENSIONS = new HashMap< >( 4 );

	/** The 7 privileged spot feature isInt flags. */
	public final static Map< String, Boolean > IS_INT = new HashMap< >( 4 );

	static
	{
		FEATURES.add( XPOSITION );
		FEATURES.add( YPOSITION );
		FEATURES.add( ZPOSITION );
		FEATURES.add( Size );
		FEATURES.add( Time );

		FEATURE_NAMES.put( XPOSITION, "X" );
		FEATURE_NAMES.put( YPOSITION, "Y" );
		FEATURE_NAMES.put( ZPOSITION, "Z" );
		FEATURE_NAMES.put( Size, "S" );
		FEATURE_NAMES.put( Time, "T" );
		

		FEATURE_SHORT_NAMES.put( XPOSITION, "X" );
		FEATURE_SHORT_NAMES.put( YPOSITION, "Y" );
		FEATURE_SHORT_NAMES.put( ZPOSITION, "Z" );
		FEATURE_SHORT_NAMES.put( Size, "S" );
		FEATURE_SHORT_NAMES.put( Time, "T" );
		

		FEATURE_DIMENSIONS.put( XPOSITION, Dimension.POSITION );
		FEATURE_DIMENSIONS.put( YPOSITION, Dimension.POSITION );
		FEATURE_DIMENSIONS.put( ZPOSITION, Dimension.POSITION );
		FEATURE_DIMENSIONS.put( Size, Dimension.LENGTH );
		FEATURE_DIMENSIONS.put( Time, Dimension.TIME );
		

		IS_INT.put( XPOSITION, Boolean.FALSE );
		IS_INT.put( YPOSITION, Boolean.FALSE );
		IS_INT.put( ZPOSITION, Boolean.FALSE );
		IS_INT.put( Size, Boolean.FALSE );
		IS_INT.put( Time, Boolean.FALSE );
		
	}

	/**
	 * Blank constructor meant to be used when loading a spot collection from a
	 * file. <b>Will</b> mess with the {@link #IDcounter} field, so this
	 * constructor <u>should not be used for normal spot creation</u>.
	 *
	 * @param ID
	 *            the spot ID to set
	 */
	public ThreeDRoiobject( final int ID )
	{
		super( 3 );
		this.ID = ID;
		synchronized ( IDcounter )
		{
			if ( IDcounter.get() < ID )
			{
				IDcounter.set( ID );
			}
		}
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
	
	public static double[] getCentroid3D(ArrayList<PreRoiobject> roiobject) {
		
		int ndims = roiobject.get(0).geometriccenter.length;
		double[] centroid = new double[ndims];
		
		for(PreRoiobject current: roiobject) {
			
			
			for (int i= 0; i < ndims; ++i) {
				centroid[i]+=current.geometriccenter[i] * current.area;
			    centroid[i]/=current.area;
			}
		}

		return centroid;
		
	}
	
	public static Pair<Double, Integer> getIntensity3D(ArrayList<PreRoiobject> roiobject){
		
		double Intensity = 0;
        int NumberofPixels = 0;
        
        for(PreRoiobject current: roiobject) {
        	
        	Intensity += current.totalintensity;
        	NumberofPixels += current.area;
        	
        	
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

	public double IntensityDistanceTo(ThreeDRoiobject target) {
		// Returns squared distance between the source Blob and the target Blob.

		

		double IntensityDistance = 1 -  Math.pow((totalintensity / target.totalintensity), 2);

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

	public double numberofPixelsRatioTo(ThreeDRoiobject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final int sourcePixels = (int) volume;
		final int targetPixels = (int) target.volume;

		
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

	public double NormalizedPixelratioandDistanceTo(ThreeDRoiobject target, final double alpha, final double beta) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = geometriccenter;
		final double[] targetLocation = target.geometriccenter;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}
		
		double cost = distance / (1 + distance);
		
		final int sourcePixels = (int) volume;
		final int targetPixels = (int) target.volume;

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
	public double diffTo(final ThreeDRoiobject target, int n) {

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

	public double squareDistanceTo(ThreeDRoiobject target) {
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
	public int compareTo(ThreeDRoiobject o) {

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
