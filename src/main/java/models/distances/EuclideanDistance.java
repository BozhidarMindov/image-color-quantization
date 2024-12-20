package main.java.models.distances;

import main.java.models.interfaces.Distance;

/**
 * Implements the Euclidean Distance metric for comparing vectors.
 */
public class EuclideanDistance implements Distance {
    /**
     * Computes the Euclidean distance between two vectors.
     *
     * @param a the first vector
     * @param b the second vector
     * @return the Euclidean distance between vectors a and b
     * @throws IllegalArgumentException if the input vectors do not have the same length
     */
    @Override
    public double compute(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            // Compute the sum of squared differences
            sum += Math.pow(a[i] - b[i], 2);
        }
        // Return the square root of the sum
        return Math.sqrt(sum);
    }
}
