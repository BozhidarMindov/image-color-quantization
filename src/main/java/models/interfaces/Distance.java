package main.java.models.interfaces;

/**
 * Defines a distance metric that can compute the distance between two vectors.
 */
public interface Distance {
    /**
     * Computes the distance between two vectors.
     *
     * @param a the first input vector
     * @param b the second input vector
     * @return the distance between the two vectors
     */
    double compute(double[] a, double[] b);
}
