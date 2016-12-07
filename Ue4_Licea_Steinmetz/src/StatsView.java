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
	
	public StatsView() {
		super(new GridLayout(rows, columns, border, border));
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
		
		int pixelSum = getPixelSum();
		
		setValue(3, getMedian(pixelSum));
		setValue(4, getVariance(mean, pixelSum));
		setValue(5, getEntropy(pixelSum));

		return true;
	}
	
	double getEntropy (int pixelSum) 
	{
		double entropy = 0.0;
		double log2 =  Math.log(2.0);
		
			for ( int i = 0; i < histogram.length; i++) {
				//possibility to occur
				if(histogram[i] != 0){
					double p = (double)histogram[i] / pixelSum;
					entropy += p * Math.log(1 / p) / log2;
				}
			}
		return entropy;
	}
	
	double getVariance (double mean, int pixelSum) 
	{
		double variance = 0;
		
		for (int i = 0; i < histogram.length; i++) {
			variance += ( Math.pow((i - mean), 2) ) * ( (double)histogram[i] / pixelSum);
		}

		return variance;
	}
	
	//TODO fix exceptions and logic for grey image
	int getMedian(int pixelSum) 
	{
		double curr = 0;
		
		for (int i = 0; i < histogram.length; i++ ) {
			if ( histogram [i] != 0 ) {
				curr += histogram[i];
			}
			if(curr > (pixelSum / 2)) {
				return i;
			}
		}
		return -1;
	}
	
	double getMean() 
	{
		double mean = 0.0;
		int allPixels = 0;
		
		for (int i = 0; i < histogram.length; i++) {
			//probability density distribution
			allPixels += histogram[i];
			mean += histogram[i] * i;
		}
		
		mean = mean / allPixels;
		
		return mean;
	}
	
	int getMin() 
	{
		int min = 0;
		for (int i = histogram.length - 1; i >= 0; i--) {
			if(histogram[i] > 0)
				min = i;
		}

		return min;
	}
	
	int getMax() 
	{
		int max = 0;
		
		for (int i = 0; i < histogram.length; i++) {
			if(histogram[i] > 0)
				max = i;
		}
		return max;
	}
	
	int getPixelSum() 
	{
		int pixelSum = 0;
		for (int i = 0; i < histogram.length; i++) {
			if (histogram[i] != 0) {
				pixelSum += histogram[i];
			}
		}
		return pixelSum;
	}
	
	void setAllPixels(int pixels) 
	{
		allPixels = pixels;
		update();
	}
	
	void setHistogramPixels(int[] pixels) 
	{
		this.originalPixels = pixels;
	}
}
