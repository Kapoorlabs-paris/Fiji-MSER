package mser3D;

import java.awt.CardLayout;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timeGUI.CovistoTimeselectPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ColorProcessor;
import ij3d.Image3DUniverse;
import mserGUI.CovistoMserPanel;
import mserMethods.MSERSeg;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import zGUI.CovistoZselectPanel;

public class InteractiveMethods {

	public NumberFormat nf;
	public RandomAccessibleInterval<FloatType> originalimg;
	public int ndims;

	public int Maxlabel;
	public int thresholdsliderInit = 125;
 
	public ImagePlus imp;
	public HashMap<String, Integer> Accountedframes;
	public HashMap<String, Integer> AccountedZ;
	public RandomAccessibleInterval<FloatType> CurrentView;
	
	
	
	public final int scrollbarSize = 1000;
	public JProgressBar jpb;
	public File inputfile;
	public boolean autothreshwater = false;
	public boolean snakeongoing = false;
	public int Progressmin = 0;
	public int Progressmax = 100;
	public int max = Progressmax;
	public MserTree<UnsignedByteType> newtree;
	public Roi nearestRoiCurr;
	public int rowchoice;
	public static int standardSensitivity = 4;
	public int sensitivity = standardSensitivity;
	public Color colorChange = Color.RED;
	
	public MouseMotionListener ml;
	public MouseListener mvl;
	public int timeMin = 1;
	

	
	public int tablesize;
	

	public ArrayList<Roi> Rois;
	public ArrayList<Roi> AfterRemovedRois;
	public ArrayList<PreRoiobject> CurrentPreRoiobject;
	public ArrayList<Roi> NearestNeighbourRois;
	public ArrayList<Roi> BiggerRois;
	public JTable table;
	public int row;
	
	public Color colorDrawMser = Color.green;
	public Color colorDrawDog = Color.red;
	public Color colorConfirm = Color.blue;
	public Color colorSnake = Color.YELLOW;
	public Color colorTrack = Color.GREEN;
	public Overlay overlay;
	public FinalInterval interval;
	
	public RandomAccessibleInterval<BitType> bitimg;
	public RandomAccessibleInterval<UnsignedByteType> newimg;
	
	public RandomAccessibleInterval<BitType> Segbitimg;
	public RandomAccessibleInterval<BitType> Segafterremovebitimg;
	
	
	public RandomAccessibleInterval<FloatType> bitimgFloat;
	public RandomAccessibleInterval<IntType> intimg;
	public HashMap<Integer, ArrayList<ThreeDRoiobject>> Timetracks;
	public ArrayList<PreRoiobject> ZTPreRoiobject;
	public ConcurrentHashMap<String, ArrayList<PreRoiobject>> ZTRois;
	public HashMap<Integer, ArrayList<ThreeDRoiobject>> threeDTRois;
    public ConcurrentHashMap<Integer, ArrayList<double[]>> AllEvents;
	public ArrayList<RefinedPeak<Point>> peaks;

	public String uniqueID, ZID, TID;

	public Image3DUniverse universe;
	public boolean apply3D = false;

	public ImageStack prestack;
	public boolean SegMode;

	public ColorProcessor cp = null;
    public final String userfile;
	public boolean AutoSnake = true;
	public boolean advancedSnake = false;

	public static enum ValueChange {

		ALL, MSER,  FOURTHDIMmouse, THIRDDIMmouse,
		THIRDDIM, MINDIVERSITY, DELTA, MINSIZE, MAXSIZE, MAXVAR, DARKTOBRIGHT, PREROI, NearestN, Kalman, ALPHA, BETA, ThreeDTrackDisplay, ThreeDTrackDisplayALL;

	}

	

	public InteractiveMethods(final RandomAccessibleInterval<FloatType> originalimg, File file, String userfile) {

		this.originalimg = originalimg;
		this.inputfile = file;
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(3);
		nf.setGroupingUsed(false);
		this.userfile = userfile;
		this.ndims = originalimg.numDimensions();
	}



	public void run(String arg0) {
		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(255);
		Normalize.normalize(Views.iterable(originalimg), minval, maxval);
		prestack = new ImageStack((int) originalimg.dimension(0), (int) originalimg.dimension(1),
				java.awt.image.ColorModel.getRGBdefault());
		Accountedframes = new HashMap<String, Integer>();
		AccountedZ = new HashMap<String, Integer>();
		AllEvents = new ConcurrentHashMap<Integer, ArrayList<double[]>>();
		AfterRemovedRois = new ArrayList<Roi>(); 
		//universe = new Image3DUniverse((int) originalimg.dimension(0), (int) originalimg.dimension(1));
		jpb = new JProgressBar();
		overlay = new Overlay();
		interval = new FinalInterval(originalimg.dimension(0), originalimg.dimension(1));
		peaks = new ArrayList<RefinedPeak<Point>>();
		ZTRois = new ConcurrentHashMap<String, ArrayList<PreRoiobject>>();
		threeDTRois = new HashMap<Integer, ArrayList<ThreeDRoiobject>>();
		CurrentPreRoiobject = new ArrayList<PreRoiobject>();
		ZTPreRoiobject = new ArrayList<PreRoiobject>();
		Timetracks = new HashMap<Integer, ArrayList<ThreeDRoiobject>>();
		
		Segbitimg = new ArrayImgFactory<BitType>().create(originalimg,
				new BitType());
		
		Segafterremovebitimg =  new ArrayImgFactory<BitType>().create(originalimg,
				new BitType());
		

		if (ndims < 3) {

			CovistoZselectPanel.thirdDimensionSize = 0;
			CovistoTimeselectPanel.fourthDimensionSize = 0;
		}

		if (ndims == 3) {

			CovistoTimeselectPanel.fourthDimension = 0;
			CovistoTimeselectPanel.fourthDimensionsliderInit = 0;
			CovistoZselectPanel.thirdDimension = 1;
			CovistoTimeselectPanel.fourthDimensionSize = 0;
			CovistoZselectPanel.thirdDimensionSize = (int) originalimg.dimension(2);

		}

		if (ndims == 4) {

			CovistoTimeselectPanel.fourthDimension = 1;
			CovistoZselectPanel.thirdDimension = 1;
			CovistoZselectPanel.thirdDimensionSize = (int) originalimg.dimension(2);
			CovistoTimeselectPanel.fourthDimensionSize = (int) originalimg.dimension(3);

			prestack = new ImageStack((int) originalimg.dimension(0), (int) originalimg.dimension(1),
					java.awt.image.ColorModel.getRGBdefault());
		}

		CurrentView = CovistoSlicer.getCurrentView(originalimg, CovistoTimeselectPanel.fourthDimension,
				CovistoZselectPanel.thirdDimensionSize, CovistoZselectPanel.thirdDimension,
				CovistoTimeselectPanel.fourthDimensionSize);

		imp = ImageJFunctions.show(CurrentView);
	
		imp.setTitle("Active image" + " " + "time point : " + CovistoTimeselectPanel.fourthDimension + " " + " Z: "
				+ CovistoZselectPanel.thirdDimension);

		updatePreview(ValueChange.ALL);

		Cardframe.repaint();
		Cardframe.validate();
		panelFirst.repaint();
		panelFirst.validate();

		Card();

	}

	public void updatePreview(final ValueChange change) {

		overlay = imp.getOverlay();
		int localthirddim = CovistoZselectPanel.thirdDimension, localfourthdim = CovistoTimeselectPanel.fourthDimension;
		uniqueID = Integer.toString(CovistoZselectPanel.thirdDimension)
				+ Integer.toString(CovistoTimeselectPanel.fourthDimension);
		ZID = Integer.toString(CovistoZselectPanel.thirdDimension);
		TID = Integer.toString(CovistoTimeselectPanel.fourthDimension);
		if (overlay == null) {

			overlay = new Overlay();
			imp.setOverlay(overlay);
		}


	



		if (change == ValueChange.PREROI) {

			ZTPreRoiobject.clear();
			for (Roi currentroi : Rois) {

				final double[] geocenter = currentroi.getContourCentroid();
				final Pair<Double, Integer> Intensityandpixels = PreRoiobject.getIntensity(currentroi, CurrentView);
				final double intensity = Intensityandpixels.getA();
				final double numberofpixels = Intensityandpixels.getB();
				final double averageintensity = intensity / numberofpixels;
				PreRoiobject currentobject = new PreRoiobject(currentroi,
						new double[] { geocenter[0], geocenter[1], CovistoZselectPanel.thirdDimension }, numberofpixels,
						intensity, averageintensity, CovistoZselectPanel.thirdDimension,
						CovistoTimeselectPanel.fourthDimension);
				ZTPreRoiobject.add(currentobject);
			}
			Accountedframes.put(TID, CovistoTimeselectPanel.fourthDimension);

			AccountedZ.put(ZID, CovistoZselectPanel.thirdDimension);
			ZTRois.put(uniqueID, ZTPreRoiobject);

			if (overlay != null)
				overlay.clear();

			for (Map.Entry<String, ArrayList<PreRoiobject>> entry : ZTRois.entrySet()) {

				ArrayList<PreRoiobject> current = entry.getValue();
				for (PreRoiobject currentroi : current) {

					if (currentroi.fourthDimension == CovistoTimeselectPanel.fourthDimension
							&& currentroi.thirdDimension == CovistoZselectPanel.thirdDimension) {

						currentroi.rois.setStrokeColor(colorConfirm);
						overlay.add(currentroi.rois);
					}

				}
			}
			imp.setOverlay(overlay);
			imp.updateAndDraw();

		}

		if (change == ValueChange.THIRDDIM) {

			if (imp == null) {
				imp = ImageJFunctions.show(CurrentView);

			}

			else {

				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				imp.updateAndDraw();

			}

			imp.setTitle("Active image" + " " + "time point : " + CovistoTimeselectPanel.fourthDimension + " " + " Z: "
					+ CovistoZselectPanel.thirdDimension);

			newimg = CovistoSlicer.PREcopytoByteImage(CurrentView);

		}

		if (change == ValueChange.FOURTHDIMmouse || change == ValueChange.THIRDDIMmouse) {

			if(!apply3D) {
			
			if (imp == null) {
				imp = ImageJFunctions.show(CurrentView);

			}

			else {

				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				imp.updateAndDraw();

			}

			imp.setTitle("Active image" + " " + "time point : " + CovistoTimeselectPanel.fourthDimension + " " + " Z: "
					+ CovistoZselectPanel.thirdDimension);

			newimg = CovistoSlicer.PREcopytoByteImage(CurrentView);


				MSERSeg computeMSER = new MSERSeg(this, jpb);
				computeMSER.execute();


		
			}
			CovistoZselectPanel.zText.setText("Current Z = " + localthirddim);
			CovistoZselectPanel.zgenText.setText("Current Z / T = " + localthirddim);
			CovistoZselectPanel.zslider.setValue(CovistoSlicer.computeScrollbarPositionFromValue(localthirddim,
					CovistoZselectPanel.thirdDimensionsliderInit, CovistoZselectPanel.thirdDimensionSize,
					scrollbarSize));
			CovistoZselectPanel.zslider.repaint();
			CovistoZselectPanel.zslider.validate();

			CovistoTimeselectPanel.timeText.setText("Current T = " + localfourthdim);
			CovistoTimeselectPanel.timeslider.setValue(CovistoSlicer.computeScrollbarPositionFromValue(
					localfourthdim, CovistoTimeselectPanel.fourthDimensionsliderInit,
					CovistoTimeselectPanel.fourthDimensionSize, CovistoTimeselectPanel.scrollbarSize));
			CovistoTimeselectPanel.timeslider.repaint();
			CovistoTimeselectPanel.timeslider.validate();
		}

		if (change == ValueChange.MSER) {
			if (imp == null) {
				imp = ImageJFunctions.show(CurrentView);

			}

			else {

				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				imp.updateAndDraw();

			}

			imp.setTitle("Active image" + " " + "time point : " + CovistoTimeselectPanel.fourthDimension + " " + " Z: "
					+ CovistoZselectPanel.thirdDimension);

			newimg = CovistoSlicer.PREcopytoByteImage(CurrentView);

			MSERSeg computeMSER = new MSERSeg(this, jpb);
			computeMSER.execute();

		}




	}

	public JFrame Cardframe = new JFrame("MSER Segmentation for spot detection");
	public JPanel panelCont = new JPanel();
	public JPanel panelFirst = new JPanel();
	public JPanel Timeselect = new JPanel();
	public JPanel Zselect = new JPanel();
	public JPanel MserPanel = new JPanel();

	public CheckboxGroup detection = new CheckboxGroup();

	public final Insets insets = new Insets(10, 0, 0, 0);
	public final GridBagLayout layout = new GridBagLayout();
	public final GridBagConstraints c = new GridBagConstraints();

	public TextField inputField = new TextField();
	public TextField inputtrackField;

	public JScrollPane scrollPane;
	public JPanel PanelSelectFile = new JPanel();
	public Border selectfile = new CompoundBorder(new TitledBorder("Select Track"), new EmptyBorder(c.insets));
	public JPanel controlnextthird = new JPanel();
	public JPanel controlprevthird = new JPanel();

	public void Card() {

		CardLayout cl = new CardLayout();

		c.insets = new Insets(5, 5, 5, 5);
		panelCont.setLayout(cl);

		panelCont.add(panelFirst, "1");

		panelFirst.setLayout(layout);
		inputField.setColumns(10);

		
		

		c.anchor = GridBagConstraints.BOTH;
		c.ipadx = 35;

		c.gridwidth = 10;
		c.gridheight = 10;
		c.gridy = 1;
		c.gridx = 0;

		// Put time slider
		Timeselect = CovistoTimeselectPanel.TimeselectPanel(ndims);

		panelFirst.add(Timeselect, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		// Put z slider

		Zselect = CovistoZselectPanel.ZselectPanel(ndims);

		panelFirst.add(Zselect, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));






		// Mser detection panel
		MserPanel = CovistoMserPanel.MserPanel();
		panelFirst.add(MserPanel, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.RELATIVE, new Insets(10, 10, 0, 10), 0, 0));


		
		
		CovistoMserPanel.AllMser.addActionListener(new PREZMserListener(this));
		CovistoMserPanel.deltaS.addAdjustmentListener(new PREDeltaListener(this, CovistoMserPanel.deltaText,
				CovistoMserPanel.deltastring, CovistoMserPanel.deltaMin, CovistoMserPanel.deltaMax,
				CovistoMserPanel.scrollbarSize, CovistoMserPanel.deltaS));

		CovistoMserPanel.Unstability_ScoreS.addAdjustmentListener(new PREUnstability_ScoreListener(this,
				CovistoMserPanel.Unstability_ScoreText, CovistoMserPanel.Unstability_Scorestring,
				CovistoMserPanel.Unstability_ScoreMin, CovistoMserPanel.Unstability_ScoreMax,
				CovistoMserPanel.scrollbarSize, CovistoMserPanel.Unstability_ScoreS));

		CovistoMserPanel.minDiversityS.addAdjustmentListener(new PREMinDiversityListener(this,
				CovistoMserPanel.minDivText, CovistoMserPanel.minDivstring, CovistoMserPanel.minDiversityMin,
				CovistoMserPanel.minDiversityMax, CovistoMserPanel.scrollbarSize, CovistoMserPanel.minDiversityS));

		CovistoMserPanel.minSizeS.addAdjustmentListener(new PREMinSizeListener(this, CovistoMserPanel.minSizeText,
				CovistoMserPanel.minSizestring, CovistoMserPanel.minSizemin, CovistoMserPanel.minSizemax,
				CovistoMserPanel.scrollbarSize, CovistoMserPanel.minSizeS));

		CovistoMserPanel.maxSizeS.addAdjustmentListener(new PREMaxSizeListener(this, CovistoMserPanel.maxSizeText,
				CovistoMserPanel.maxSizestring, CovistoMserPanel.minSizemin, CovistoMserPanel.minSizemax,
				CovistoMserPanel.scrollbarSize, CovistoMserPanel.maxSizeS));
        
		CovistoMserPanel.findmaximaMser.addItemListener(new FindMaximaMserListener(this));
		CovistoMserPanel.findminimaMser.addItemListener(new FindMinimaMserListener(this));

		CovistoTimeselectPanel.timeslider.addAdjustmentListener(
				new PreTimeListener(this, CovistoTimeselectPanel.timeText, CovistoTimeselectPanel.timestring,
						CovistoTimeselectPanel.fourthDimensionsliderInit, CovistoTimeselectPanel.fourthDimensionSize,
						CovistoTimeselectPanel.scrollbarSize, CovistoTimeselectPanel.timeslider));

		if (ndims > 3)
			CovistoZselectPanel.zslider.addAdjustmentListener(new PreZListener(this, CovistoZselectPanel.zText,
					CovistoZselectPanel.zstring, CovistoZselectPanel.thirdDimensionsliderInit,
					CovistoZselectPanel.thirdDimensionSize, scrollbarSize, CovistoZselectPanel.zslider));
		else
			CovistoZselectPanel.zslider.addAdjustmentListener(new PreZListener(this, CovistoZselectPanel.zgenText,
					CovistoZselectPanel.zgenstring, CovistoZselectPanel.thirdDimensionsliderInit,
					CovistoZselectPanel.thirdDimensionSize, scrollbarSize, CovistoZselectPanel.zslider));

		CovistoZselectPanel.inputFieldZ.addTextListener(new PreZlocListener(this, false));
		CovistoTimeselectPanel.inputFieldT.addTextListener(new PreTlocListener(this, false));

		
		Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		cl.show(panelCont, "1");

		Cardframe.add(panelCont, "Center");
		Cardframe.add(jpb, "Last");
		panelFirst.setVisible(true);
		Cardframe.pack();
		Cardframe.setVisible(true);
	}

	public static void main(String[] args) {

		new ImageJ();
		JFrame frame = new JFrame("");
		MSERFileChooser panel = new MSERFileChooser();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
	}

}
