package main.java.models.minibatchkmeans;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a K-Means cluster, which contains a centroid and list of points assigned to the cluster.
 */
public class Cluster {
    private final Centroid centroid; // The centroid of the cluster
    private final List<double[]> points; // The points assigned to the cluster

    /**
     * Constructs a Cluster instance with a specified centroid.
     *
     * @param centroid the centroid of the cluster
     */
    public Cluster(Centroid centroid) {
        this.centroid = centroid;
        this.points = new ArrayList<>();
    }

    /**
     * Returns the centroid of the cluster.
     *
     * @return the centroid of the cluster
     */
    public Centroid getCentroid() {
        return centroid;
    }

    /**
     * Returns a list of points assigned to the cluster.
     *
     * @return a list of points
     */
    public List<double[]> getPoints() {
        return points;
    }

    /**
     * Clears all points of the cluster.
     */
    public void clearPoints() {
        points.clear();
    }

    /**
     * Adds a point to the cluster
     *
     * @param point the point to be added
     */
    public void addPoint(double[] point) {
        points.add(point);
    }
}
