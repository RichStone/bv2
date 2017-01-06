// BV Ue04 WS2016/17 Vorgabe
//
// Copyright (C) 2015 by Klaus Jung

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.stream.IntStream;

public class ImageAnalysis extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final String author = "Steinmetz and Licea";	
	private static final String initialFilename = "mountains.png";
	private static final File openPath = new File(".");
	private static final int border = 10;
	private static final int maxWidth = 910; 
	private static final int maxHeight = 910; 
	private static final int graySteps = 256;
	
	private static JFrame frame;
	
	private ImageView imgView;						// image view
	private HistoView histoView = new HistoView();	// histogram view
	private StatsView statsView = new StatsView();	// statistics values view
	private JSlider brightnessSlider;				// brightness Slider
	private JSlider quantizeSlider;	
	
	int [] histogramPixels;
	int[] origPix;
	
	private JSlider contrastSlider;
	
	private JLabel statusLine;				// to print some status text
	
	/**
	 * Constructor. Constructs the layout of the GUI components and loads the initial image.
	 */
	public ImageAnalysis() {
        super(new BorderLayout(border, border));
        
        // load the default image
        File input = new File(initialFilename);
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        imgView = new ImageView(input);
        imgView.setMaxSize(new Dimension(maxWidth, maxHeight));
        
        origPix = imgView.getPixels().clone();
       
		// load image button
        JButton load = new JButton("Open Image");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		File input = openFile();
        		if(input != null) {
        			imgView.loadImage(input);
        			imgView.setMaxSize(new Dimension(maxWidth, maxHeight));
        			
        			origPix = imgView.getPixels();

        			frame.pack();
	                processImage();
        		}
        	}        	
        });
         
        JButton reset = new JButton("Reset Slider");
        reset.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		brightnessSlider.setValue(0);
        		
        		contrastSlider.setValue(10);
        		
        		processImage();
	    	}        	
	    });
        
        JButton autoContrast = new JButton("Auto Contrast");
        autoContrast.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		autocontrast();
        	}      	
	    });
        
        JButton autoContrast2 = new JButton("Auto Contrast 2");
        autoContrast2.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		int [] newImage = imgView.getPixels();
        		
        		/*
        		 * basic formula for auto contrasting. Linear operation.
        		 * f_autoContrast(a) = a_min + (a - a_low)*((a_max - a_min)/(a_hi-a_low))
        		 */
        		
        		int image_low, image_high;
        		
      
        		image_low = statsView.getMin();
        		image_high = statsView.getMax();
        		
        		for(int i = 0; i < origPix.length; i++) {
            		//get pixel
            		int oldPx = origPix[i] & 0xff;
            		
            		int newPx = (int) ((255 * (oldPx - image_low) / (image_high - image_low)));
            		//over-/underflow handling
        			if(newPx < 0) {
        				newPx = 0;
        			}
        			if(newPx > 255) {
        				newPx = 255; 
        			}
        			System.out.println("new: " + newPx);
            		newImage[i] = (0xFF << 24) | (newPx << 16) | (newPx << 8) | newPx;
            		
            		//contribute to histogram statistics
            		histogramPixels[newPx]++;
            	}
        	
        		imgView.setPixels(newImage);
        		histoView.setHistogram(histogramPixels);
            	statsView.setHistogram(histogramPixels);
        		
        		histoView.update();
        		statsView.update();
        		imgView.applyChanges();
        	}      	
	    });
        
        // some status text
        statusLine = new JLabel(" ");
        
        // top view controls
        JPanel topControls = new JPanel(new GridBagLayout());
        topControls.add(load);
        topControls.add(reset);
        topControls.add(autoContrast);
        topControls.add(autoContrast2);
        
        // center view
        JPanel centerControls = new JPanel();
        JPanel rightControls = new JPanel();
        rightControls.setLayout(new BoxLayout(rightControls, BoxLayout.Y_AXIS));
        centerControls.add(imgView);
        rightControls.add(histoView);
        rightControls.add(statsView);
        centerControls.add(rightControls);
        
        // bottom view controls
        JPanel botControls = new JPanel();
        botControls.setLayout(new BoxLayout(botControls, BoxLayout.Y_AXIS));
        
        // brightness slider
        brightnessSlider = new JSlider(-graySteps, graySteps, 0);
		TitledBorder titBorder = BorderFactory.createTitledBorder("Brightness: 0");
		titBorder.setTitleColor(Color.GRAY);
        brightnessSlider.setBorder(titBorder);
        brightnessSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		processImage();
        		int adjustedBrightnessValue = brightnessSlider.getValue();
        		titBorder.setTitle("Brightness: " + adjustedBrightnessValue);
        	}        	
        });
        
        // brightness slider
        contrastSlider = new JSlider(0, 100, 10);
        TitledBorder titBorderContrast = BorderFactory.createTitledBorder("Contrast: 10");
        titBorderContrast.setTitleColor(Color.GRAY);
        contrastSlider.setBorder(titBorderContrast);
        contrastSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		
        		processImage();
        		int adjustedContrastValue = contrastSlider.getValue();
        		titBorderContrast.setTitle("Contrast: " + adjustedContrastValue);
        	}
        });
        
        quantizeSlider = new JSlider(10, 200, 10);
        TitledBorder quantizeLabel = BorderFactory.createTitledBorder("Quantize");
        quantizeLabel.setTitleColor(Color.GRAY);
        quantizeSlider.setBorder(quantizeLabel);
        quantizeSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		processImage();
        	}
        });
        
        botControls.add(brightnessSlider);
        botControls.add(contrastSlider);
        botControls.add(quantizeSlider);
        statusLine.setAlignmentX(Component.CENTER_ALIGNMENT);
        botControls.add(statusLine);

        // add to main panel
        add(topControls, BorderLayout.NORTH);
        add(centerControls, BorderLayout.CENTER);
        add(botControls, BorderLayout.SOUTH);
               
        // add border to main panel
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial rotation
        processImage();
	}
	
	private void autocontrast() {
		int image_low, image_high, h, c;
		 
		
		image_low = (int) Math.round (statsView.getMinimumMinusOnePercent());
		//System.out.println("Value just above 1% is " + image_low);
		
		image_high = (int) Math.round (statsView.getMaximumMinusOnePercent());
		//System.out.println("Value just below 99% is " + image_high);

		h = (int) Math.round( (double) (128 - ( (image_low + image_high) / 2) ));
		c = (int) Math.round(10.0 * (255.0 / (image_high - image_low)));

		brightnessSlider.setValue(h);
		contrastSlider.setValue(c);
		
		processImage();
	}
	

	/**
	 * Set up and show the main frame.
	 */
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Image Analysis - " + author);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent contentPane = new ImageAnalysis();
        contentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(contentPane);

        // display the window
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	/**
	 * Main method. 
	 * @param args - ignored. No arguments are used by this application.
	 */
	public static void main(String[] args) {
        // schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	
	/**
	 * Open file dialog used to select a new image.
	 * @return The selected file object or null on cancel.
	 */
	private File openFile() {
		// file open dialog
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(openPath);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
	
    
	
    /**
     * Update image with new brightness and contrast values.
     * Update histogram, histogram view and statistics view.
     */
    protected void processImage() {
    	
		long startTime = System.currentTimeMillis();
		
		histogramPixels = new int[graySteps];
		
		int [] newImage = imgView.getPixels();

		int brightnessValue = brightnessSlider.getValue();
		double contrastValue = contrastSlider.getValue() / 10;
		if(contrastSlider.getValue()!=10) 
			contrastValue = contrastSlider.getValue() / 10.0;

		double delta = quantizeSlider.getValue() / 10.0;

    	for(int i = 0; i < origPix.length; i++) {
    		//get pixel
    		int oldPx = origPix[i] & 0xff;
    		
    		//calculate brightness and contrast
    		int newPx = (int) (((oldPx + brightnessValue) - 128) * contrastValue + 128);
    		
    		//quantize
    		int q = (int) Math.round((newPx / delta));
    		newPx = (int) Math.round(q * delta);
    				
    		//over-/underflow handling
			if(newPx < 0) {
				newPx = 0;
			}
			if(newPx > 255) {
				newPx = 255; 
			}
    		newImage[i] = (0xFF << 24) | (newPx << 16) | (newPx << 8) | newPx;
    		
    		//contribute to histogram statistics
    		histogramPixels[newPx]++;
    	}
    	System.out.println("brightness: " + brightnessValue);
    	System.out.println("contrast: " + contrastValue);
    	System.out.println("delta: " + (delta));
    	imgView.setPixels(newImage);
    	histoView.setHistogram(histogramPixels);
    	statsView.setHistogram(histogramPixels);
		
		imgView.applyChanges();
		histoView.update();
		statsView.update();
		
		// show processing time
		long time = System.currentTimeMillis() - startTime;
		statusLine.setText("Processing time = " + time + " ms.");
    }
}