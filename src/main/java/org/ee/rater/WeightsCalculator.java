package org.ee.rater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class WeightsCalculator {
	private final Rater rater;
	private RealMatrix matrix;
	private RealVector vector;

	public WeightsCalculator(Rater rater) {
		this.rater = rater;
	}

	public RealVector calculateWeights() {
		List<Rateable> rateables = new ArrayList<>(rater.getRateables(true));
		double[][] ratings = createMatrix(rateables);
		double[] totals = createVector(rateables);
		if(ratings.length < totals.length) {
			throw new CalculationException("ratings < totals");
		}
		removeDoubles(ratings, totals);
		if(matrix.getColumnDimension() > matrix.getRowDimension()) {
			throw new CalculationException("More categories than unique ratings, cannot compute weights");
		}
		return new QRDecomposition(matrix).getSolver().solve(vector);
	}

	private void removeDoubles(double[][] ratings, double[] totals) {
		Set<MatrixRow> set = new HashSet<>(totals.length);
		for(int r = 0; r < totals.length; r++) {
			set.add(new MatrixRow(ratings[r], totals[r]));
		}
		if(set.size() < totals.length) {
			ratings = new double[set.size()][];
			totals = new double[set.size()];
			int i = 0;
			for(MatrixRow row : set) {
				ratings[i] = row.row;
				totals[i] = row.solution;
				i++;
			}
		}
		matrix = MatrixUtils.createRealMatrix(ratings);
		vector = MatrixUtils.createRealVector(totals);
	}

	private double[][] createMatrix(final List<Rateable> rateables) {
		final List<Category> categories = rater.getCategories();
		final double[][] matrix = new double[rateables.size()][categories.size()];
		final int rows = matrix.length;
		if(rows < 1) {
			throw new CalculationException("No rateables found");
		}
		final int cols = matrix[0].length;
		for(int r = 0; r < rows; r++) {
			final Rateable row = rateables.get(r);
			for(int c = 0; c < cols; c++) {
				final List<Rateable> column = categories.get(c).getRateables();
				final int index = column.indexOf(row);
				if(index >= 0) {
					matrix[r][c] = column.get(index).getRating();
				}
			}
		}
		return matrix;
	}

	private double[] createVector(List<Rateable> rateables) {
		final List<Rateable> ratings = rater.getAbsolute().getRateables();
		final double[] vector = new double[rateables.size()];
		for(int i = 0; i < vector.length; i++) {
			final int index = ratings.indexOf(rateables.get(i));
			if(index >= 0) {
				vector[i] = ratings.get(index).getRating();
			}
		}
		return vector;
	}

	private static class MatrixRow {
		private Integer hash;
		final double[] row;
		final double solution;

		MatrixRow(double[] row, double solution) {
			this.row = row;
			this.solution = solution;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof MatrixRow) {
				MatrixRow other = (MatrixRow) obj;
				return solution == other.solution && Arrays.equals(row, other.row);
			}
			return false;
		}

		@Override
		public int hashCode() {
			if(hash == null) {
				hash = 43 + Double.hashCode(solution);
				for(int i = 0; i < row.length; i++) {
					hash = 43 * hash + Double.hashCode(row[i]);
				}
			}
			return hash;
		}
	}
}
