package org.ee.rater;

import java.text.NumberFormat;
import java.util.Objects;

public class Rateable implements Comparable<Rateable>, Cloneable {
	private String id;
	private String name;
	private double rating;

	public int compareTo(Rateable o) {
		return Double.compare(rating, o.rating);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	@Override
	public String toString() {
		return id + " " + rating + " " + name;
	}

	public String toString(NumberFormat format) {
		return id + " " + format.format(rating) + " " + name;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Rateable) {
			Rateable other = (Rateable) obj;
			return id == other.id || (id != null && id.equals(other.id));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 4813 + Objects.hashCode(id);
	}

	@Override
	public Rateable clone() throws CloneNotSupportedException {
		return (Rateable) super.clone();
	}
}
