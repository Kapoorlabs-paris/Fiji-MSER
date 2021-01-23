package mser3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import ij.IJ;
import ij.gui.Roi;
import mserGUI.CovistoMserPanel;
import mserMethods.FinderUtils;
import net.imglib2.Cursor;
import net.imglib2.KDTree;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealPointSampleList;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labeling.DefaultROIStrategyFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingROIStrategy;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import timeGUI.CovistoTimeselectPanel;
import zGUI.CovistoZselectPanel;

public class ComputeCompTree<T extends RealType<T> & NativeType<T>> {

	final InteractiveMethods parent;
	final JProgressBar jpb;
	public final RandomAccessibleInterval<T> source;

	public RandomAccessibleInterval<BitType> bitimg;
	public boolean apply3D;
	public int z;
	public int t;

	public ComputeCompTree(final InteractiveMethods parent, final RandomAccessibleInterval<T> source,
			final JProgressBar jpb, boolean apply3D, int z, int t) {

		this.parent = parent;
		this.source = source;
		this.jpb = jpb;
		this.apply3D = apply3D;
		this.z = z;
		this.t = t;
		
		bitimg = new ArrayImgFactory<BitType>().create(source, new BitType());
	}

	public void execute() {

		MserTree<T> newtree;
		// Compute the component tree
		if (CovistoMserPanel.darktobright)

			newtree = MserTree.buildMserTree(source, CovistoMserPanel.delta, CovistoMserPanel.minSize, CovistoMserPanel.maxSize,
					CovistoMserPanel.Unstability_Score, CovistoMserPanel.minDiversity, true);

		else

			newtree = MserTree.buildMserTree(source, CovistoMserPanel.delta, CovistoMserPanel.minSize, CovistoMserPanel.maxSize,
					CovistoMserPanel.Unstability_Score, CovistoMserPanel.minDiversity, false);

		parent.Rois = FinderUtils.getcurrentRois(newtree);
		parent.CurrentPreRoiobject = new ArrayList<PreRoiobject>();

		for (Roi currentroi : parent.Rois) {

			final double[] geocenter = currentroi.getContourCentroid();
			final Pair<Double, Integer> Intensityandpixels = PreRoiobject.getIntensity(currentroi, source);
			final double intensity = Intensityandpixels.getA();
			final double numberofpixels = Intensityandpixels.getB();
			final double averageintensity = intensity / numberofpixels;
			PreRoiobject currentobject = new PreRoiobject(currentroi,
					new double[] { geocenter[0], geocenter[1], CovistoZselectPanel.thirdDimension }, numberofpixels, intensity,
					averageintensity, CovistoZselectPanel.thirdDimension, CovistoTimeselectPanel.fourthDimension);
			parent.CurrentPreRoiobject.add(currentobject);
		}

		String uniqueID = Integer.toString(z) + Integer.toString(t);
		parent.ZTRois.put(uniqueID, parent.CurrentPreRoiobject);
		common3D.BinaryCreation.CreateBinaryRoi(parent, source, bitimg,parent.Rois, z, t);
	}



	public RandomAccessibleInterval<BitType> getBinaryimg() {

		return bitimg;
	}




}
