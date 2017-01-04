import java.awt.BorderLayout;
import java.awt.Color;
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

import javax.print.attribute.standard.PresentationDirection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DPCM extends JPanel 
{
	private static final int borderWidth = 5;
	private static final int maxWidth = 460;
	private static final int maxHeight = 320;
	private static File openPath = new File(".");
	
	private static JFrame frame;
	
	private static ImageView startView;
	private static ImageView predictionView;
	private static ImageView reconstructedView;
	
	private JLabel statusLine;
	private JComboBox<String> method;
	JLabel entropyStartLabel;
	static JLabel entropyPredictionLabel;
	JLabel entropyReconstructedLabel;
	
	private static double entropyStart;
	private static double entropyPredictor;
	private static double entropyReconstructed;
	
	public DPCM() 
	{
		super(new BorderLayout(borderWidth, borderWidth));
		
		setBorder(BorderFactory.createEmptyBorder(borderWidth, borderWidth, borderWidth, borderWidth));
		
		//autoload image
		File input1 = new File("src/test1.jpg");
		if(!input1.canRead()) {
			input1 = openFile("Open Image");
		}
		
		//initialize the image sockets
		startView = new ImageView(input1);
		startView.setMaxSize(new Dimension(maxWidth, maxHeight));
		TitledBorder startViewBorder = BorderFactory.createTitledBorder("Eingabebild");
		startViewBorder.setTitleColor(Color.BLACK);
		startView.setBorder(startViewBorder);
		//convert to gray scale
		startView.setPixels(convertToGrayscale(startView));
		
		predictionView = new ImageView(input1);
		predictionView.setMaxSize(new Dimension(maxWidth, maxHeight));
		TitledBorder predictionViewBorder = BorderFactory.createTitledBorder("Prädiktionsfehlerbild");
		predictionViewBorder.setTitleColor(Color.GRAY);
		predictionView.setBorder(predictionViewBorder);
		
		reconstructedView = new ImageView(startView.getImgWidth(), startView.getImgWidth());
		reconstructedView.setMaxSize(new Dimension(maxWidth, maxHeight));
		TitledBorder reconstructedViewBorder = BorderFactory.createTitledBorder("Rekonstruiertes Bild");
		reconstructedViewBorder.setTitleColor(Color.BLACK);
		reconstructedView.setBorder(reconstructedViewBorder);
		
		//control panel
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, borderWidth, 0, 0);
		
		//predictor methods
		//TODO add empty method to change from
		String [] methodNames = {"Methode wählen", "A (horizontal)", "B (vertikal)", "C (diagonal)", "A + B - C", "(A + B) / 2", "adaptiv"};
		
		method = new JComboBox<String>(methodNames);
		method.setSelectedIndex(0);
		method.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculate();
			}
		});
		
		//drop down label
		JLabel predictorLabel = new JLabel("Prädiktor: ");
		
		//create open image btn
		JButton load = new JButton("Bild öffnen");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if(input != null) {
					startView.loadImage(input);
					startView.setMaxSize(new Dimension(maxWidth, maxHeight));
					calculate();
				}
			}
		});
		
		//show created elements
		controls.add(load, c);
		controls.add(predictorLabel, c);
		controls.add(method, c);
		
		//show image
		JPanel images = new JPanel(new GridLayout(1, 3));
		images.add(startView);
		images.add(predictionView);
		images.add(reconstructedView);
		
		setAllEntropies();
		
		//entropy displaying
		entropyStartLabel = new JLabel("Entropie: " + getEntropy(startView));
		entropyPredictionLabel = new JLabel("Entropie: " + getEntropy(predictionView));
		entropyReconstructedLabel = new JLabel("Entropie: " + getEntropy(reconstructedView) + ", MSE = ");
		JPanel entropyDisplay = new JPanel(new GridLayout(1, 3));
		entropyDisplay.add(entropyStartLabel);
		entropyDisplay.add(entropyPredictionLabel);
		entropyDisplay.add(entropyReconstructedLabel);
		
		
		//status panel
		JPanel status = new JPanel(new GridLayout(1, 3));
		statusLine = new JLabel("Qua Qua");
		status.add(statusLine, c);
		
		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(entropyDisplay, BorderLayout.SOUTH);
//		add(status, BorderLayout.EAST);
		
		calculate();
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

	protected void calculate() {
		switch(method.getSelectedIndex()) {
		case 0:
			System.out.println("nothin case 0");
			break;
		case 1:
			System.out.println("and here");
			predictA();
			break;
		default:
			System.out.println("FU");
			break;
		}
	}
	
	private static void predictA() 
	{
		//!?!?!?!??!?!? gives crazy values but works on second click
		int imgHeight = startView.getHeight();
		int imgWidth = startView.getWidth();
		int[] pixelsOld = startView.getPixels();	
		int[] pixelsNew = new int[pixelsOld.length];
		
		for(int y = 0; y < imgWidth; y++) {
			for(int x = 0; x < imgHeight; x++) {
				
				//posX is the X of a C-B-A-X square kernel
				int posX = y * imgWidth + x;
				int posA = y * imgWidth + (x - 1);
				
				//upper edge handling
				if(posX >= pixelsOld.length) {
					continue;
				}
				
				//value of the pixel X of a C-B-A-X square kernel
				int valX = pixelsOld[posX] & 0xff;
				
				int valA;
				//set value for A
				if(posA < 0) {
					valA = 128;
				}
				else {
					valA = pixelsOld[posA] & 0xff;
				}
				
				//calculate e
				int e = valX - valA;
				
				//tune 
				e += 128;
				if(e > 255) {
					e = 255;
				}
				if(e < 0) {
					e = 0;
				}
				pixelsNew[posX] = (0xff << 24) | (e << 16) | (e << 8) | e;
				
			}
		}
		predictionView.setPixels(pixelsNew);
		entropyPredictionLabel.setText("Entropie: " + getEntropy(predictionView));;
	}
	
	/**
	 * 
	 * @param img convert this img to gray scale
	 * @return the new array with the gray values
	 */
	private static int[] convertToGrayscale(ImageView img) {
		int [] pixels = img.getPixels();
		for(int i = 0; i < pixels.length; i++) {
			int argb = pixels[i];
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = (argb) & 0xff;
			int greyScale = (r + g + b) / 3;
			
			pixels[i] = (0xff << 24) | (greyScale << 16) | (greyScale << 8) | greyScale;
		}
		return pixels;
	}
	
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("DPCM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new DPCM();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
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
	
	static double getEntropy (ImageView img) 
	{
		int [] histogram = getHistogram(img);
		int pixelSum = getPixelSum(histogram);
		double entropy = 0.0;
		double log2 =  Math.log(2.0);
		
		for ( int i = 0; i < histogram.length; i++) {
			//possibility to occur
			if(histogram[i] != 0){
				double p = (double)histogram[i] / pixelSum;
				entropy += p * Math.log(1 / p) / log2;
			}
		}
		//reduce fraction
		entropy = Math.round(entropy * 1000.0) / 1000.0;
		System.out.println("entropy: " + entropy);
		return entropy;
	}
	
	static void setAllEntropies() {
		entropyStart = getEntropy(startView);
		entropyPredictor = getEntropy(predictionView);
		entropyReconstructed = getEntropy(reconstructedView);
	}
	
	static int[] getHistogram(ImageView img) 
	{
		int [] histogram = new int[256];
		int [] pixels = img.getPixels();
		for(int i = 0; i < pixels.length; i++) {
			int pix = pixels[i] & 0xff;
    		//contribute to histogram statistics
    		histogram[pix]++;
    	}
		return histogram;
	}
	
	static int getPixelSum(int [] histogram) 
	{
		int pixelSum = 0;
		for (int i = 0; i < histogram.length; i++) {
			if (histogram[i] != 0) {
				pixelSum += histogram[i];
			}
		}
		return pixelSum;
	}
}