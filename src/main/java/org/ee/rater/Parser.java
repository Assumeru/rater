package org.ee.rater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Parser {
	private File file;

	public Parser(File file) {
		this.file = file;
	}

	private static interface Scan {
		public void scan(Scanner in, int line);
	}

	private void scanFile(Scan scan) throws FileNotFoundException {
		try(Scanner in = new Scanner(new BufferedInputStream(new FileInputStream(file)))) {
			in.useLocale(Locale.US);
			for(int i = 1; in.hasNextLine(); i++) {
				if(!in.hasNext()) {
					in.nextLine();
					continue;
				}
				try {
					scan.scan(in, i);
				} catch(NoSuchElementException e) {
					Rater.error("Error parsing line " + i + " in file " + file, e);
					break;
				}
			}
		}
	}

	public Category parseCategory() throws IOException {
		final Category category = new Category();
		category.setName(file.getName());
		scanFile(new Scan() {
			@Override
			public void scan(Scanner in, int line) {
				category.getRateables().add(parseRateable(in, line));
			}
		});
		return category;
	}

	private Rateable parseRateable(Scanner in, int line) {
		Rateable rateable = new Rateable();
		rateable.setId(in.next());
		rateable.setRating(in.nextDouble());
		in.skip(" ");
		rateable.setName(in.nextLine());
		return rateable;
	}

	public Map<String, Double> parseWeights() throws IOException {
		final Map<String, Double> weights = new HashMap<>();
		scanFile(new Scan() {
			@Override
			public void scan(Scanner in, int line) {
				double weight = in.nextDouble();
				in.skip(" ");
				String name = in.nextLine();
				weights.put(name, weight);
			}
		});
		return weights;
	}
}
