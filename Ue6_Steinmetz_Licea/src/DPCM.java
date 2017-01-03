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
	
	private ImageView startView;
	private ImageView predictionView;
	private ImageView reconstructedView;
	
	private JLabel statusLine;
	private JComboBox<String> method;
	
	private double entropy = 0;
	
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
		
		predictionView = new ImageView(startView.getImgWidth(), startView.getImgWidth());
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
		String [] methodNames = {"A (horizontal)", "B (vertikal)", "C (diagonal)", "A + B - C", "(A + B) / 2", "adaptiv"};
		
		method = new JComboBox<String>(methodNames);
		method.setSelectedIndex(0);
		method.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculate();
			}
		});
		
		//drop down label
		JLabel predictorLabel = new JLabel("Prädikator: ");
		
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
		
		//entropy displaying
		JLabel entropyLabel = new JLabel("Entropie: " + entropy);
		JLabel entropyPredictionLabel = new JLabel("Entropie: " + 1);
		JLabel entropyReconstructedLabel = new JLabel("Entropie: " + 2);
		JPanel entropyDisplay = new JPanel(new GridLayout(1, 3));
		entropyDisplay.add(entropyLabel);
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
			System.out.println("a cool method will be here");
			break;
		case 1:
			System.out.println("and here");
			break;
		default:
			Arrays.fill(predictionView.getPixels(), 0xffffffff);
			System.out.println("FU");
			break;
		}
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
}