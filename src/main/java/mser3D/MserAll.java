package mser3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import mser3D.InteractiveMethods.ValueChange;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import timeGUI.CovistoTimeselectPanel;
import zGUI.CovistoZselectPanel;

public class MserAll extends SwingWorker<Void, Void> {

	final InteractiveMethods parent;

	public MserAll(final InteractiveMethods parent) {

		this.parent = parent;

	}
public class ParallelCalls implements Callable<Void> {

		
		public final InteractiveMethods parent;
		public final int z;
		public final int t;
		public RandomAccessibleInterval<BitType> currentbitimg;

		
		public ParallelCalls(InteractiveMethods parent,RandomAccessibleInterval<BitType> currentbitimg, int z, int t) {
			
			
			this.parent = parent;
			this.currentbitimg = currentbitimg;

			this.z = z;
			this.t = t;
			
		}
		
		
		@Override
		public Void call() throws Exception {
		
			double percent = t +z;
			RandomAccessibleInterval<FloatType> CurrentView = CovistoSlicer.getCurrentView(parent.originalimg, z,
					CovistoZselectPanel.thirdDimensionSize, t, CovistoTimeselectPanel.fourthDimensionSize);
			MSERProgressBar.MSERSetProgressBar(parent.jpb, 100 *(percent / (CovistoTimeselectPanel.fourthDimensionSize + CovistoZselectPanel.thirdDimensionSize + 1 )) ,"Computing");
			// UnsignedByteType image created here
			parent.updatePreview(ValueChange.THIRDDIMmouse);
			RandomAccessibleInterval<UnsignedByteType> newimg = CovistoSlicer.PREcopytoByteImage(CurrentView);
		
	
			
			processParallelSlice(newimg, currentbitimg,  z, t);
			
			return null;
		}
		
		
		
	}
	@Override
	protected Void doInBackground() throws Exception {


		
		parent.apply3D = true;
		
		
		RandomAccessibleInterval<BitType> bitimg = new ArrayImgFactory<BitType>().create(parent.originalimg, new BitType());
		
		List<Future<Void>> list = new ArrayList<Future<Void>>();
		int nThreads = Runtime.getRuntime().availableProcessors();
		final ExecutorService taskExecutor = Executors.newFixedThreadPool(nThreads);
		for (int t = CovistoTimeselectPanel.fourthDimensionsliderInit; t <= CovistoTimeselectPanel.fourthDimensionSize; ++t) {


			for (int z = CovistoZselectPanel.thirdDimensionsliderInit; z <= CovistoZselectPanel.thirdDimensionSize; ++z) {
				
				CovistoZselectPanel.thirdDimension = z;
				CovistoTimeselectPanel.fourthDimension = t;
				
				RandomAccessibleInterval<BitType> currentbitimg = CovistoSlicer.getCurrentView(bitimg, z, CovistoZselectPanel.thirdDimensionSize, t,
						CovistoTimeselectPanel.fourthDimensionSize);
				
			
				ParallelCalls call = new ParallelCalls(parent, currentbitimg, z, t);
				
				Future<Void> Futureresult = taskExecutor.submit(call);
				list.add(Futureresult);
			
			}
			
		
		}
		
	for (Future<Void> fut : list) {
			
			
			fut.get();
			
		}
	
	
	        RandomAccessibleInterval<IntType> intimg = LabelSegmentationImage(bitimg);
			ImageJFunctions.show(intimg).setTitle("Label Image");
		
		
		
		
		
		
		
		
		
		
		return null;
	}
	public RandomAccessibleInterval<IntType> LabelSegmentationImage(RandomAccessibleInterval<BitType> SegmentationImage) {
		
		   long[] dims = new long[SegmentationImage.numDimensions()];
		   SegmentationImage.dimensions(dims);
	       RandomAccessibleInterval<IntType> indexImg = ArrayImgs.ints(dims);
	       RandomAccessibleInterval<IntType> LabelImage =ArrayImgs.ints(dims);	
	       ImgLabeling<Integer, IntType> labeling = new ImgLabeling<>(indexImg);
	       Iterator<Integer> labels = new Iterator<Integer>()
	      {
	          private int i = 1;

	          @Override
	          public boolean hasNext()
	          {
	              return true;
	          }

	          @Override
	          public Integer next()
	          {
	              return i++;
	          }

	          @Override
	          public void remove()
	          {}
	      };
	      
	      ConnectedComponents.labelAllConnectedComponents(SegmentationImage, labeling, labels, StructuringElement.FOUR_CONNECTED);
	      
		  LabelImage = labeling.getIndexImg();
			
	      return LabelImage;		
			
		}

	
	protected void processParallelSlice(RandomAccessibleInterval< UnsignedByteType > slice, RandomAccessibleInterval< BitType > bitoutputslice, int z, int t) {
		
		
		
		
		
		ComputeCompTree<UnsignedByteType> ComputeMSER = new ComputeCompTree<UnsignedByteType>(parent, slice, parent.jpb, parent.apply3D, z, t);
		ComputeMSER.execute();
		
		RandomAccessibleInterval<BitType> bitimg =  ComputeMSER.getBinaryimg();
		Cursor< BitType > bitcursor = Views.iterable(bitoutputslice).localizingCursor();
		
		RandomAccess<BitType> ranac = bitimg.randomAccess();
		
		while(bitcursor.hasNext()) {
			
			bitcursor.fwd();
			
			ranac.setPosition(bitcursor);
			
			bitcursor.get().set(ranac.get());
			
			
		}
		
	
	
		
	}
	
	
	protected void processSlice(RandomAccessibleInterval< UnsignedByteType > slice, RandomAccessibleInterval< BitType > bitoutputslice, int z, int t) {
		
		
		
		parent.CurrentPreRoiobject = new ArrayList<PreRoiobject>();
	
		ComputeCompTree<UnsignedByteType> ComputeMSER = new ComputeCompTree<UnsignedByteType>(parent, slice, parent.jpb, parent.apply3D, z, t);
		ComputeMSER.execute();
		
		RandomAccessibleInterval<BitType> bitimg =  ComputeMSER.getBinaryimg();
		Cursor< BitType > bitcursor = Views.iterable(bitoutputslice).localizingCursor();
		
		RandomAccess<BitType> ranac = bitimg.randomAccess();
		
		while(bitcursor.hasNext()) {
			
			bitcursor.fwd();
			
			ranac.setPosition(bitcursor);
			
			bitcursor.get().set(ranac.get());
			
			
		}
		
	
	
		
	}

	@Override
	protected void done() {
		try {
		
			parent.apply3D = false;
			MSERProgressBar.MSERSetProgressBar(parent.jpb, "Done");

			get();
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}

	}

}
