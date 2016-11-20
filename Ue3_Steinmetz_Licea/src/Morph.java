import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

//Uebung 3 Vorlage WS2016/17
//Copyright (C) 2014 by Klaus Jung
//Re-used by Prof. Dr. Zhang
//All rights reserved.
//
//Date: 2014-10-31
//new Date: 2016-11-7
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Morph extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int borderWidth = 5;
	private static final int maxWidth = 460;
	private static final int maxHeight = 320;
	private static final File openPath = new File(".");
	private static final int sliderGranularity = 100;

	private static final double scalingX = 1.0 / 0.76;
	private static final double scalingY = 1.0 / 0.66;

	private static JFrame frame;
	
	
	private ImageView startView;		// image view for start picture
	private ImageView morphView;		// image view for intermediate picture
	private ImageView endView;			// image view for end picture

	private double morphPos;			// 0.0 is "start", 1.0 is "end"
	
	private JSlider morphSlider;		// slider for current morphing position
	private JComboBox<String> method;	// the selected transmission method
	private JLabel statusLine;			// to print some status text
	
	 
	public Morph() {
        super(new BorderLayout(borderWidth, borderWidth));

        setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
 
        // load the default images
        File input1 = new File("RedApple.jpg");
        // file not found, choose another image
        if(!input1.canRead()) input1 = openFile("Open Image 1"); 
        
        File input2 = new File("GreenApple.jpg");
        //random playing with child's picture.
        //File input1 = new File("lisko1e.gif");
        // file not found, choose another image
        //if(!input1.canRead()) input1 = openFile("Open Image 1"); 
        
        //File input2 = new File("lisko2d.gif");
        // file not found, choose another image
        if(!input2.canRead()) input2 = openFile("Open Image 2"); 
        startView = new ImageView(input1);
        startView.setMaxSize(new Dimension(maxWidth, maxHeight));
       
        endView = new ImageView(input2);
        endView.setMaxSize(new Dimension(maxWidth, maxHeight));
        
		// create empty image for morphing
		morphView = new ImageView(startView.getImgWidth(), startView.getImgHeight());
		morphView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
        // control panel
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,borderWidth,0,0);
        
        // transmission methods
        String[] methodNames = {"Cross-fading", "Scale left image", "Scale right image", "Scale & move left image", "Scale & move right image", "Morphing"};
        
        method = new JComboBox<String>(methodNames);
        method.setSelectedIndex(0);		// set initial method
        method.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		calculate();
        	}
        });
        // morphing position
        JLabel morphLabel = new JLabel("Morphing Position:");
        morphPos = 0;
        morphSlider = new JSlider(JSlider.HORIZONTAL, 0, sliderGranularity, (int)(morphPos * sliderGranularity));
        morphSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		morphPos = morphSlider.getValue() / (double)sliderGranularity;
        		calculate();
        	}
        });
        
        controls.add(method, c);
        controls.add(morphLabel, c);
        controls.add(morphSlider, c);
        
        // images
        JPanel images = new JPanel(new GridLayout(1,3));
        images.add(startView);
        images.add(morphView);
        images.add(endView);
        
        // status panel
        JPanel status = new JPanel(new GridLayout(1,3));
        
        // some status text
        statusLine = new JLabel(" ");
        status.add(statusLine, c);
        
         
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        
        calculate();
                       
	}
	
	private File openFile(String title) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(openPath);
        int ret = chooser.showDialog(this, title);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
	
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Morph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new Morph();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	private synchronized void calculate() {
		long startTime = System.currentTimeMillis();
		
		/*
		 * implemented methods: scaleRight(), scaleAndMoveLeft(), scaleAndMoveRight();
		 * and morph(). For our own organizational purposes:
		 * 
		 * scaleRight() -> d) Vergrößern des grünen Apfels auf die Größe des roten Apfels.
		 * 
		 * scaleAndMoveLeft() -> e) Zusätzliches kontinuierliches Verschieden des grünen Apfels, 
		 * so dass er an die Position des roten Apfels wandert.
		 * 
		 * scaleAndMoveRight() -> c) Zusätzliches kontinuierliches Verschieden des roten Apfels, 
		 * so dass er an die Position des grünen Apfels wandert.
		 * 
		 * morph() -> f) Kombination von a), c) und e) zur Umsetzung eines einfachen Morphing.
		 * 
		 */
		switch(method.getSelectedIndex()) {
		case 0:
			crossfade();
			break;
		case 1:
			scaleLeft();
			break;
		case 2:
			scaleRight();
			break;
		case 3:
			scaleAndMoveLeft();
			break;
		case 4:
			scaleAndMoveRight();
			break;
		case 5:
			morph();
			break;
		default:
			Arrays.fill(morphView.getPixels(), 0xffffffff); // white image
			break;
		}
		
		morphView.applyChanges();
		
		
		long time = System.currentTimeMillis() - startTime;
    	statusLine.setText("Processing time: " + time + " ms");
	}


	
	void crossfade() {
		int[] pixA = startView.getPixels();
		int[] pixM = morphView.getPixels();
		int[] pixB = endView.getPixels();
		
		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int posM = y * width + x;
				
				int argb = pixA[posM];
				int rA = (argb >> 16) & 0xff;
				int gA = (argb >>  8) & 0xff;
				int bA = (argb)       & 0xff;
				
				argb = pixB[posM];
				
				int rB = (argb >> 16) & 0xff;
				int gB = (argb >>  8) & 0xff;
				int bB = (argb)       & 0xff;
				
				int rM = (int) ((1 - a) * rA + a * rB);
				int gM = (int) ((1 - a) * gA + a * gB);
				int bM = (int) ((1 - a) * bA + a * bB);
				
				pixM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}

	void scaleLeft() {
		
		
		// This implements a simple nearest neighbor scaling.
		// You may replace it by a bilinear scaling for better visual results
		
		//loads the original image, sets max width and max height.
		int[] sourcePixelsImgA = startView.getPixels();
		//creates empty image for morphing
		int[] morphedPixels = morphView.getPixels();
		
		int width = startView.getImgWidth();
		int height = startView.getImgHeight();
		
		//sliderMorphPosition, used by the program slider, to access the 
		//current position of index (or x).
		double sliderMorphPosition = morphPos;
		
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				//what is happening here - posM? poorly named. Have
				//to read code to figure out what it means. Renaming shortly.
				// is basically, our current working pixel.
				int currentlyIndexedPixel = y * width + x;
				
				// current scaling - reading the code, above, the scaling is different,
				// but not sure why it's necessarily done. In any event, above, scaling
				// is set to float values 1.0/.76 and 1.0/.66
				double xScaled = scalingX * sliderMorphPosition + 1.0 * (1 - sliderMorphPosition);
				double yScaled = scalingY * sliderMorphPosition + 1.0 * (1 - sliderMorphPosition);
				
				// scaled coordinates in image A
				int xScaledSource = (int)(x * xScaled);
				//added (-100 * a) to variable yA so that the red apple appears more aligned with
				//the green apple.
				int yScaledSource = (int)((y * yScaled) + (-100 * sliderMorphPosition));
				
				// sets everything to white
				int argb = 0xffffffff; 
				
				/*
				 * notes to myself: scaled x, y values.
				 * if scaled x greater than or equal to 0 AND
				 * x less than width AND
				 * and scaled y greater than or equal to 0, AND
				 * y is less than height.
				 */
				 
				 
				if(xScaledSource >= 0 && xScaledSource < width && yScaledSource >= 0 && yScaledSource < height) {
					// we are inside image A
					argb = sourcePixelsImgA[yScaledSource * width + xScaledSource];
				}
				//extract rgb values from our current working pixel.
				int redAttribute = (argb >> 16) & 0xff;
				int greenAttribute = (argb >>  8) & 0xff;
				int blueAttribute = (argb)       & 0xff;
				
				
				morphedPixels[currentlyIndexedPixel] = 0xFF000000 | (redAttribute << 16) | (greenAttribute << 8) | blueAttribute;
			}
		}
	}
	
	
	
void scaleRight() {
		/*
		 * Mostly just used the given method, scaleLeft(), and adjusted to scale
		 * the image on the right (the green apple).
		 */
		int[] sourcePixelsImgB = endView.getPixels();
		int[] morphedPixels = morphView.getPixels();
		
		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double morphSliderPosition = morphPos;
	
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int posM = y * width + x;
				
				// current scaling
				double xScaled = 1/(scalingX * morphSliderPosition + 1.0 * (1 - morphSliderPosition));
				double yScaled = 1/(scalingY * morphSliderPosition + 1.0 * (1 - morphSliderPosition));
				
				// scaled coordinates for image B, arrived to the 125, 75 values
				//by trial-and-error, until it looked semi-aligned.
				int xA = (int)((x * xScaled) + (125 * morphSliderPosition));
				int yA = (int)((y * yScaled) + (75 * morphSliderPosition));
				
				int argb = 0xffffffff; // white pixel
				
				if(xA >= 0 && xA < width && yA >= 0 && yA < height) {
					// we are inside image A
					argb = sourcePixelsImgB[yA * width + xA];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;
				
				
				morphedPixels[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}


	void scaleAndMoveLeft() {
		
		// This implements a simple nearest neighbor scaling.
		// You may replace it by a bilinear scaling for better visual results

		int[] pixelA = startView.getPixels();
		int[] pixelM = morphView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * a + 1.0 * (1 - a);
				double yS = scalingY * a + 1.0 * (1 - a);

				// scaled coordinates in image A
				int xA = (int)((x * xS) - (350 * a));
				int yA = (int)((y * yS) - (100 * a));

				int argb = 0xffffffff; // white pixel

				if(xA >= 0 && xA < width && yA >= 0 && yA < height) {
					// we are inside image A
					argb = pixelA[yA * width + xA];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;


				pixelM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}
	
	void scaleAndMoveRight() {
		
		int[] pixelB = endView.getPixels();
		int[] pixelM = morphView.getPixels();
		
		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;
	
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int posM = y * width + x;
				
				// current scaling
				double xS = 1/(scalingX * a + 1.0 * (1 - a));
				double yS = 1/(scalingY * a + 1.0 * (1 - a));
				
				// scaled coordinates in image B
				int xA = (int)((x * xS) + (250 * a));
				int yA = (int)((y * yS) + (70 * a));
				
				int argb = 0xffffffff; // white pixel
				
				if(xA >= 0 && xA < width && yA >= 0 && yA < height) {
					// we are inside image A
					argb = pixelB[yA * width + xA];
				}

				int rM = (argb >> 16) & 0xff;
				int gM = (argb >>  8) & 0xff;
				int bM = (argb)       & 0xff;
				
				
				pixelM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}
	
	void morph() {
		int[] pixelA = startView.getPixels();
		int[] pixelM = morphView.getPixels();
		int[] pixelB = endView.getPixels();

		int width = morphView.getImgWidth();
		int height = morphView.getImgHeight();
		double a = morphPos;
		
		int rA = 0;
		int gA = 0;
		int bA = 0;
		
		int rB = 0;
		int gB = 0;
		int bB = 0;

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int posM = y * width + x;

				// current scaling
				double xS = scalingX * a + 1.0 * (1 - a);
				double yS = scalingY * a + 1.0 * (1 - a);
				
				// current scaling
				double xSb = 1/(scalingX * (1 - a) + 1.0 * a);
				double ySb = 1/(scalingY * (1 - a) + 1.0 * a);

				// scaled coordinates in image A
				int xA = (int)((x * xS) - (350 * a));
				int yA = (int)((y * yS) - (100 * a));

				int argb = 0xffffffff; // white pixel

				if(xA >= 0 && xA < width && yA >= 0 && yA < height) {
					// we are inside image A
					argb = pixelA[yA * width + xA];
					rA = (argb >> 16) & 0xff;
					gA = (argb >>  8) & 0xff;
					bA = (argb)       & 0xff;
				}else{
					rA = 255;
					gA = 255;
					bA = 255;
				}

				// scaled coordinates in image B
				int xB = (int)((x * xSb) + (250 * (1 - a)));
				int yB = (int)((y * ySb) + (80 * (1 - a)));


				if(xB >= 0 && xB < width && yB >= 0 && yB < height) {
					// we are inside image A
					argb = pixelB[yB * width + xB];
					rB = (argb >> 16) & 0xff;
					gB = (argb >>  8) & 0xff;
					bB = (argb)       & 0xff;
				}
				

				int rM = (int) ((1 - a) * rA + a * rB);
				int gM = (int) ((1 - a) * gA + a * gB);
				int bM = (int) ((1 - a) * bA + a * bB);
				
				pixelM[posM] = 0xFF000000 | (rM << 16) | (gM << 8) | bM;
			}
		}
	}
}