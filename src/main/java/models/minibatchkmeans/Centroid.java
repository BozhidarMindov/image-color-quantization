package main.java.models.minibatchkmeans;

/**
 * Represents a centroid with coordinates in a K-Means cluster.
 */
public class Centroid {
    private double[] coordinates; // The coordinates of the centroid

    /**
     * Constructs a Centroid instance with specified coordinates.
     *
     * @param coordinates an array with the initial coordinates of the centroid
     */
    public Centroid(double[] coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Returns the coordinates of the centroid.
     *
     * @return an array with the coordinates of the centroid
     */
    public double[] getCoordinates() {
        return coordinates;
    }

    /**
     * Sets new coordinates for the centroid.
     *
     * @param coordinates an array with the new coordinates
     * @throws IllegalArgumentException if the input coordinates are null
     *                                  or if the input length coordinates are of different length than the original ones
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null || coordinates.length != this.coordinates.length) {
            throw new IllegalArgumentException(
                    "Coordinates array must not be null and must have the same length as the existing coordinates."
            );
        }
        this.coordinates = coordinates;
    }
}
