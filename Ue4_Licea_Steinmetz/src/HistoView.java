// BV Ue04 WS2016/17 Vorgabe Hilfsklasse HistoView
//
// Copyright (C) 2014 by Klaus Jung

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class HistoView extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int graySteps = 256;
	private static final int height = 200;
	private static final int width = graySteps;
	
	private int[] histogram = null;
	
	public HistoView() {
		super();
		TitledBorder titBorder = BorderFactory.createTitledBorder("Histogram");
		titBorder.setTitleColor(Color.GRAY);
		setBorder(titBorder);
		add(new HistoScreen());
	}
	
	public boolean setHistogram(int[] histogram) {
		if(histogram == null || histogram.length != graySteps) {
			return false;
		}
		this.histogram = histogram;
		update();
		return true;
	}
	
	public boolean update() {
		if(histogram == null) {
			return false;
		}
		invalidate();
		repaint();
		return true;
	}
	
	class HistoScreen extends JComponent {

		private static final long serialVersionUID = 1L;
		
		public void paintComponent(Graphics g) {
			g.clearRect(0, 0, width, height);

			// TODO: draw histogram instead of diagonal lines
			//debug
//	    	for(int j = 0; j < 256; j++) {
//				System.out.println("pixel nr.: " + j + " = " + histogramPixels[j]);
//			}
			g.setColor(Color.black);
			
			//TODO get logic of the factor
			
			for(int i = 0; i < histogram.length; i++) {
				g.drawLine(i, 0, i, factor * histogram[i]);
			}
		}
		
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}
	}
}
