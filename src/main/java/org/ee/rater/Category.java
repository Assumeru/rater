package org.ee.rater;

import java.util.ArrayList;
import java.util.List;

public class Category {
	private String name;
	private List<Rateable> rateables;
	private double weight;

	public Category() {
		rateables = new ArrayList<>();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<Rateable> getRateables() {
		return rateables;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append('\n');
		for(Rateable r : rateables) {
			sb.append(r).append('\n');
		}
		return sb.toString();
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
