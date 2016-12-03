// BV Ue04 WS2016/17 Vorgabe Hilfsklasse StatsView
//
// Copyright (C) 2014 by Klaus Jung

import java.awt.Color;

import java.awt.GridLayout;
import java.lang.reflect.Array;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import java.util.ArrayList;
import java.util.Arrays;

public class StatsView extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String[] names = { "Name 1:", "Name 2:", "Name 3:", "Name 4:", "Name 5:", "Name 6:" }; // TODO: enter proper names
	private static final int rows = names.length;
	private static final int border = 2;
	private static final int columns = 2;
	private static final int graySteps = 256;
	
	private JLabel[] infoLabel = new JLabel[rows];
	private JLabel[] valueLabel = new JLabel[rows];
	
	private int[] histogram = null;
	
	private int[] originalPixels = null;
	
	private int allPixels = 0;
	
	public StatsView(int[] originalPixels) {
		super(new GridLayout(rows, columns, border, border));
		this.originalPixels = originalPixels;
		TitledBorder titBorder = BorderFactory.createTitledBorder("Statistics");
		titBorder.setTitleColor(Color.GRAY);
		setBorder(titBorder);
		for(int i = 0; i < rows; i++) {
			String name = "";
			switch(i) {
			case 0: name = "Minimum";
				break;
			case 1: name = "Maximum";
				break;
			case 2: name = "Mean";
				break;
			case 3: name = "Median";
				break;
			case 4: name = "Variance";
				break;
			case 5: name = "Entropy";
				break;
			default: name = "Name";
				break;
			}
			infoLabel[i] = new JLabel(name);
			valueLabel[i] = new JLabel("-----");
			add(infoLabel[i]);
			add(valueLabel[i]);
		}
	}
	
	private void setValue(int column, int value) {
		valueLabel[column].setText("" + value);
	}
	
	private void setValue(int column, double value) {
		valueLabel[column].setText(String.format(Locale.US, "%.2f", value));
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
		setValue(0, getMin());
		setValue(1, getMax());
		double mean = getMean();
		setValue(2, mean);
		setValue(3, getMedian());
		setValue(4, getVariance(mean));
		setValue(5, getEntropy());

		return true;
	}
	
	double getEntropy () {
		double entropy = 0.0;
		System.out.println("entropy all: " + allPixels);
		if(allPixels != 0) {
			for ( int i = 0; i < histogram.length; i++) {
				//possibility to occur
				double p = histogram[i] / allPixels;
			}
		}
		return entropy;
	}
	
	double getVariance (double mean) {
		double variance = 0;
		
		for (int i = 0; i < histogram.length; i++) {
			variance += Math.pow((histogram[i] - mean), 2);
		}
		variance = variance / histogram.length;

		return variance;
	}
	
	//TODO fix exceptions and logic for grey image
	int getMedian() {
		int median = 0;
		
		int [] sortedHistogram = Arrays.copyOf(histogram, histogram.length);
		Arrays.sort(sortedHistogram);

		//TODO doesn't correspond with result on slide
		median = histogram[(sortedHistogram[256 / 2 - 1] + sortedHistogram[256 / 2]) / 2];
		return median;
	}
	
	double getMean() {
		double mean = 0.0;
		
		for (int i = 0; i < histogram.length; i++) {
			
		}
		
		mean = mean / histogram.length;
		
		return mean;
	}
	
	int getMin() {
		for (int i = 0; i < histogram.length - 1; i++) {
			if(histogram[i] != 0)
				return i;
		}
		return -1;
	}
	
	int getMax() {
		for (int i = histogram.length - 1; i > 0; i--) {
			if(histogram[i] != 0)
				return i;
		}
		return -1;
	}
	
	void setAllPixels(int pixels) {
		allPixels = pixels;
		update();
		System.out.println("all: " + allPixels);
	}
	
	void setHistogramPixels(int[] pixels) {
		this.originalPixels = pixels;
	}
}
