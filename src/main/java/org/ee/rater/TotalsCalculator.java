package org.ee.rater;

import java.util.List;

public class TotalsCalculator {
	private Rater rater;

	public TotalsCalculator(Rater rater) {
		this.rater = rater;
	}

	public void calculateTotals() throws CloneNotSupportedException {
		if(rater.getAbsolute() == null) {
			rater.setAbsolute(new Category());
		}
		List<Rateable> rateables = rater.getAbsolute().getRateables();
		for(Rateable r : rater.getRateables(false)) {
			int index = rateables.indexOf(r);
			if(index < 0) {
				index = rateables.size();
				rateables.add(r.clone());
			}
			rateables.get(index).setRating(computeRating(r));
		}
	}

	private double computeRating(Rateable r) {
		double rating = 0;
		for(Category cat : rater.getCategories()) {
			int index = cat.getRateables().indexOf(r);
			if(index >= 0) {
				rating += cat.getWeight() * cat.getRateables().get(index).getRating();
			}
		}
		return rating;
	}
}
