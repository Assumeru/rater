package org.ee.rater;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.linear.RealVector;

public class Rater {
	private static final int ERROR_ARGUMENTS = 1 << 1;
	private static final int ERROR_NO_ABSOLUTE = 1 << 2;
	private static final int ERROR_NO_RATINGS = 1 << 3;
	private static final int ERROR_CALCULATION_ERROR = 1 << 4;
	private static final int ERROR_PARSING = 1 << 5;
	private static final String OPTION_HELP = "h";
	private static final String OPTION_CATEGORY = "f";
	private static final String OPTION_CALCULATE_WEIGHTS = "w";
	private static final String OPTION_ABSOLUTE = "a";
	private static final String OPTION_DEBUG = "v";
	private static final String OPTION_LIST = "l";
	private static final String OPTION_WEIGHTS = "i";
	private static final String OPTION_CALCULATE = "c";
	private static final String OPTION_TOTALS = "t";
	private static final String OPTION_STANDARD_DEVIATION = "s";
	private static final String OPTION_MEAN = "m";
	private static final String OPTION_REDISTRIBUTE = "d";
	private static final String OPTION_MIN_MAX = "r";
	private static boolean debug;

	public static void main(String[] args) {
		Options options = new Options().addOption(OPTION_HELP, "help", false, "displays this message")
				.addOption(OPTION_CATEGORY, "category", true, "adds a rating category file")
				.addOption(OPTION_CALCULATE_WEIGHTS, "calc-weights", false,
						"calculates the relative weight of each category")
				.addOption(OPTION_ABSOLUTE, "absolute", true, "sets the absolute ratings file")
				.addOption(OPTION_DEBUG, "verbose", false, "enables exception logging")
				.addOption(OPTION_LIST, "list", false, "lists categories")
				.addOption(OPTION_WEIGHTS, "load-weights", true, "sets weights from file")
				.addOption(OPTION_CALCULATE, "calculate", false, "calculates totals according to weight")
				.addOption(Option.builder(OPTION_TOTALS).longOpt("totals").numberOfArgs(1).desc("prints totals")
						.argName("decimal places").build())
				.addOption(OPTION_STANDARD_DEVIATION, "standard-deviation", true, "sets the standard deviation to use")
				.addOption(OPTION_MEAN, "mean", true, "sets the mean to use")
				.addOption(OPTION_REDISTRIBUTE, "redistribute", false,
						"redistributes the total based on either the calculated or provided standard deviation and mean")
				.addOption(Option.builder(OPTION_MIN_MAX).longOpt("range").numberOfArgs(2)
						.desc("sets the minimum and maximum rating (default: 1-10)").argName("min> <max").build());
		try {
			CommandLine line = new DefaultParser().parse(options, args);
			Rater rater = new Rater();
			debug = line.hasOption(OPTION_DEBUG);
			if(line.hasOption(OPTION_CATEGORY)) {
				rater.addFiles(line.getOptionValues(OPTION_CATEGORY));
			}
			if(line.hasOption(OPTION_ABSOLUTE)) {
				rater.addAbsoluteFile(line.getOptionValue(OPTION_ABSOLUTE));
			}
			if(line.hasOption(OPTION_WEIGHTS)) {
				rater.setWeightsFile(line.getOptionValue(OPTION_WEIGHTS));
			}
			if(line.hasOption(OPTION_MIN_MAX)) {
				rater.setMinMax(line.getOptionValues(OPTION_MIN_MAX));
			}
			if(line.hasOption(OPTION_CALCULATE)) {
				rater.recalculate();
			}
			if(line.hasOption(OPTION_STANDARD_DEVIATION)) {
				rater.setStandardDeviation(line.getOptionValue(OPTION_STANDARD_DEVIATION));
			}
			if(line.hasOption(OPTION_MEAN)) {
				rater.setMean(line.getOptionValue(OPTION_MEAN));
			}
			if(line.hasOption(OPTION_REDISTRIBUTE)) {
				rater.redistribute();
			}
			if(line.hasOption(OPTION_LIST)) {
				rater.listCategories();
			} else if(line.hasOption(OPTION_CALCULATE_WEIGHTS)) {
				rater.calculateWeights();
			} else if(line.hasOption(OPTION_TOTALS)) {
				rater.printAbsolutes(createFormat(line.getOptionValue(OPTION_TOTALS)));
			} else if(line.hasOption(OPTION_HELP) || line.getOptions().length == 0) {
				HelpFormatter help = new HelpFormatter();
				help.printHelp("rater", options);
			}
		} catch(ParseException e) {
			error(e.getMessage(), e);
			System.exit(ERROR_ARGUMENTS);
		} catch(Exception e) {
			error("Unkown error", e);
			System.exit(-1);
		}
	}

	private static NumberFormat createFormat(String decimalPlaces) {
		if(decimalPlaces == null || decimalPlaces.isEmpty()) {
			return null;
		}
		try {
			final int places = Math.max(0, Integer.parseInt(decimalPlaces));
			StringBuilder format = new StringBuilder('#');
			if(places > 0) {
				format.append('.');
			}
			for(int i = 0; i < places; i++) {
				format.append('0');
			}
			DecimalFormat out = new DecimalFormat(format.toString(), DecimalFormatSymbols.getInstance(Locale.US));
			out.setRoundingMode(RoundingMode.HALF_UP);
			out.setMaximumFractionDigits(places);
			return out;
		} catch(NumberFormatException e) {
			error("Invalid number of decimal places", e);
			return null;
		}
	}

	static void error(String message, Throwable throwable) {
		System.err.println(message);
		if(debug && throwable != null) {
			throwable.printStackTrace();
		}
	}

	private List<Category> categories;
	private Category absolute;
	private int status;
	private Double sigma;
	private Double mu;
	private double maxRating = 10;
	private double minRating = 1;

	public Rater() {
		categories = new ArrayList<>();
	}

	public List<Category> getCategories() {
		return categories;
	}

	public Category getAbsolute() {
		return absolute;
	}

	public void setAbsolute(Category absolute) {
		this.absolute = absolute;
	}

	public Double getMu() {
		return mu;
	}

	public Double getSigma() {
		return sigma;
	}

	public double getMaxRating() {
		return maxRating;
	}

	public double getMinRating() {
		return minRating;
	}

	public Set<Rateable> getRateables(boolean rated) {
		if(rated) {
			return new HashSet<>(absolute.getRateables());
		}
		Set<Rateable> set = new HashSet<>();
		for(Category cat : categories) {
			set.addAll(cat.getRateables());
		}
		return set;
	}

	private void addFiles(String[] fileNames) {
		for(String fileName : fileNames) {
			addFile(new File(fileName));
		}
	}

	private void addFile(File file) {
		try {
			categories.add(new Parser(file).parseCategory());
		} catch(IOException e) {
			error("Failed to parse file " + file, e);
			status |= ERROR_PARSING;
		}
	}

	private void addAbsoluteFile(String file) {
		try {
			absolute = new Parser(new File(file)).parseCategory();
		} catch(IOException e) {
			error("Failed to parse absolute ratings", e);
			status |= ERROR_PARSING;
		}
	}

	private void listCategories() {
		if(absolute != null) {
			System.out.println(absolute);
		}
		for(Category cat : categories) {
			System.out.println(cat);
		}
	}

	private void calculateWeights() {
		if(absolute == null) {
			System.err.println("Cannot calculate weights without absolute ratings");
			status |= ERROR_NO_ABSOLUTE;
		}
		if(categories.isEmpty()) {
			System.err.println("Cannot calculate weights without categories");
			status |= ERROR_NO_RATINGS;
		}
		if(status != 0) {
			System.exit(status);
		}
		try {
			RealVector weights = new WeightsCalculator(this).calculateWeights();
			for(int i = 0; i < weights.getDimension(); i++) {
				double weight = weights.getEntry(i);
				Category cat = categories.get(i);
				cat.setWeight(weight);
				System.out.println(weight + " " + cat.getName());
			}
		} catch(CalculationException e) {
			error("Could not calculate weights", e);
			System.exit(ERROR_CALCULATION_ERROR);
		}
	}

	private void setWeightsFile(String fileName) {
		try {
			Map<String, Double> weights = new Parser(new File(fileName)).parseWeights();
			for(Category cat : categories) {
				Double weight = weights.get(cat.getName());
				if(weight == null) {
					System.err.println("Unknown weight for category " + cat.getName());
				} else {
					cat.setWeight(weight);
				}
			}
		} catch(IOException e) {
			error("Failed to parse weights", e);
			status |= ERROR_PARSING;
		}
	}

	private void printAbsolutes(NumberFormat numberFormat) {
		if(absolute == null) {
			System.err.println("No absolutes to print");
		} else {
			absolute.getRateables().sort(null);
			for(Rateable r : absolute.getRateables()) {
				System.out.println(numberFormat == null ? r : r.toString(numberFormat));
			}
		}
	}

	private void recalculate() {
		try {
			new TotalsCalculator(this).calculateTotals();
		} catch(CloneNotSupportedException e) {
			System.err.println("This should never happen");
			System.exit(-1);
		}
	}

	private void setStandardDeviation(String stdDev) {
		if(stdDev != null && !stdDev.isEmpty()) {
			try {
				sigma = Double.parseDouble(stdDev);
			} catch(NumberFormatException e) {
				error("Invalid standard deviation", e);
			}
		}
	}

	private void setMean(String mean) {
		if(mean != null && !mean.isEmpty()) {
			try {
				mu = Double.parseDouble(mean);
			} catch(NumberFormatException e) {
				error("Invalid mean", e);
			}
		}
	}

	private void setMinMax(String[] values) {
		if(values.length == 2) {
			try {
				double min = Double.parseDouble(values[0]);
				double max = Double.parseDouble(values[1]);
				if(max < min) {
					System.err.println("max < min");
				} else if(max == min) {
					System.err.println("Minimum and maximum rating are the same");
				} else {
					maxRating = max;
					minRating = min;
				}
			} catch(NumberFormatException e) {
				error("Invalid minimum or maximum rating", e);
			}
		} else {
			System.err.println("Invalid number of arguments");
		}
	}

	private void redistribute() {
		if(absolute == null) {
			System.err.println("No absolutes to redistribute");
			System.exit(ERROR_NO_ABSOLUTE);
		}
		new Redistributor(this).redistribute();
	}
}
