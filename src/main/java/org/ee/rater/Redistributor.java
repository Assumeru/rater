package org.ee.rater;

import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class Redistributor {
	private Rater rater;
	private List<Rateable> rateables;
	private double delta;

	public Redistributor(Rater rater) {
		this.rater = rater;
		rateables = rater.getAbsolute().getRateables();
	}

	public void redistribute() {
		rateables.sort(null);
		double[] values = getVector();
		double mu;
		double sigma;
		if(rater.getMu() == null) {
			mu = new Mean().evaluate(values);
		} else {
			mu = rater.getMu();
		}
		if(rater.getSigma() == null) {
			sigma = new StandardDeviation().evaluate(values, mu);
		} else {
			sigma = rater.getSigma();
		}
		rerate(new NormalDistribution(mu, sigma));
	}

	private double[] getVector() {
		double[] vector = new double[rateables.size()];
		for(int i = 0; i < vector.length; i++) {
			vector[i] = rateables.get(i).getRating();
		}
		return vector;
	}

	private void rerate(NormalDistribution distribution) {
		final int size = rateables.size();
		delta = rater.getMaxRating() - rater.getMinRating();
		for(int i = 0; i < size; i++) {
			rateables.get(i).setRating(getRating(distribution.cumulativeProbability(getRating((double) i / size))));
		}
	}

	private double getRating(double percentage) {
		return percentage * delta + rater.getMinRating();
	}
}
