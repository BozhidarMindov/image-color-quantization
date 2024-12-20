package main.java.models.minibatchkmeans;

import main.java.models.interfaces.Decay;
import main.java.models.interfaces.Distance;
import main.java.models.interfaces.Quantizer;
import main.java.models.utils.BatchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implements the Mini-Batch K-Means clustering algorithm. This is a version of the traditional K-Means algorithm which
 * picks random batches of data on each iteration and uses them for training.
 * This speeds up conversion and training times.
 */
public class MiniBatchKMeans implements Quantizer {
    private final List<Cluster> clusters; // A list of clusters in the model
    private final int k; // The number of clusters
    private final Distance distance; // The distance metric used to find the closest centroids
    private final double initialLearningRate; // The initial learning rate for training (set to 0.5 by default)
    private final Decay decay; // The decay metric used to decay the value of the learning rate
    private final double convergenceThreshold; // The convergence threshold (set to 0.0001 by default)
    private final Random random; // A random generator for selecting mini-batches

    /**
     * Constructs a MiniBatchKMeans instance with a specified number of clusters and distance metric.
     *
     * @param k        the number of clusters
     * @param distance the distance metric used to compute distances between points and centroids
     */
    public MiniBatchKMeans(int k, Distance distance, Decay decay) {
        this.k = k;
        this.distance = distance;
        this.decay = decay;
        this.convergenceThreshold = 0.0001;
        this.initialLearningRate = 0.5;
        this.random = new Random();
        this.clusters = new ArrayList<>();
    }

    /**
     * Initializes clusters with random centroids from the input data.
     *
     * @param data the input data used for initialization
     */
    private void initializeClusters(double[][] data) {
        // Initialize clusters with random centroids from the data
        for (int i = 0; i < k; i++) {
            Centroid centroid = new Centroid(data[random.nextInt(data.length)]);
            clusters.add(new Cluster(centroid));
        }
    }

    /**
     * Clears all points from the clusters.
     * This is used as a preparation step for the next assignment phase.
     */
    private void clearClusters() {
        for (Cluster cluster : clusters) {
            cluster.clearPoints();
        }
    }

    /**
     * Assigns each point in the input data to the closest cluster.
     *
     * @param data the input data points to be assigned
     */
    private void assignPointsToClusters(double[][] data) {
        for (double[] point : data) {
            Cluster closestCluster = findClosestCluster(point);
            closestCluster.addPoint(point);
        }
    }

    /**
     * Updates the centroids of the clusters based on the assigned points
     * and checks for convergence based on the set threshold.
     *
     * @return true if centroids have converged, false otherwise
     */
    private boolean updateCentroids(double learningRate) {
        boolean isConverged = true;

        for (Cluster cluster : clusters) {
            double[] newCoordinates = new double[cluster.getCentroid().getCoordinates().length];
            List<double[]> points = cluster.getPoints();

            if (points.isEmpty()) {
                continue;
            }

            // Calculate the mean for each dimension using points in the mini-batch

            // Sum up all point coordinates
            for (double[] point : points) {
                for (int i = 0; i < newCoordinates.length; i++) {
                    newCoordinates[i] += point[i];
                }
            }

            // Get the mean
            for (int i = 0; i < newCoordinates.length; i++) {
                newCoordinates[i] /= points.size();
            }

            double[] centroidCoords = cluster.getCentroid().getCoordinates();

            // Store the old centroid coordinates for the convergence check
            double[] oldCoordinates = cluster.getCentroid().getCoordinates().clone();

            // Apply the learning rate to update the centroid position (coordinates are updated in place)
            for (int i = 0; i < centroidCoords.length; i++) {
                centroidCoords[i] = (1 - learningRate) * centroidCoords[i] + learningRate * newCoordinates[i];
            }

            // Calculate the distance moved
            double movement = distance.compute(oldCoordinates, centroidCoords);

            // Check for convergence based on the movement
            if (movement > convergenceThreshold) {
                isConverged = false;
            }
        }

        return isConverged;
    }

    /**
     * Trains the Mini-Batch K-Means model using the provided data over a specified number of epochs.
     *
     * @param data   the input data for training
     * @param epochs the number of epochs
     * @throws IllegalArgumentException if the input data is null, empty, or smaller than the selected number of clusters
     */
    @Override
    public void train(double[][] data, int epochs) {
        if (data == null || data.length == 0 || data.length < k) {
            throw new IllegalArgumentException("Input data cannot be null or empty or smaller than k.");
        }
        initializeClusters(data);

        boolean converged = false;
        double learningRate = initialLearningRate;
        // Get batch size. Batch size is 2% of the data size, capped at 1024. At least 1 item in the batch.
        int batchSize = Math.max(1, Math.min((int) (data.length * 0.02), 1024));
        System.out.println("Batch size: " + batchSize);
        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Epoch " + (epoch + 1) + " | Learning rate: " + learningRate);
            clearClusters();

            // Get a mini-batch of random points from data
            double[][] miniBatch = BatchUtils.getMiniBatch(data, batchSize, random);

            // Assign the mini-batch points to clusters
            assignPointsToClusters(miniBatch);

            // Update centroids based on the mini-batch and check for convergence
            converged = updateCentroids(learningRate);
            if (converged) {
                System.out.println("Converged at epoch " + (epoch + 1));
                break;
            }
            //Apply decay and get the learning rate for next epoch
            learningRate = decay.compute(initialLearningRate, epoch, epochs);
        }

        if (!converged) {
            System.out.println("Reached maximum epochs without convergence.");
        }
    }

    /**
     * Returns the cluster whose centroid is the closest to the given input vector.
     *
     * @param input the input vector
     * @return the closest cluster
     */
    private Cluster findClosestCluster(double[] input) {
        Cluster closestCluster = null;
        double minDist = Double.MAX_VALUE;

        // Find the closest cluster for each point in the mini-batch
        for (Cluster cluster : clusters) {
            double dist = distance.compute(input, cluster.getCentroid().getCoordinates());
            if (dist < minDist) {
                minDist = dist;
                closestCluster = cluster;
            }
        }
        return closestCluster;
    }

    /**
     * Returns the centroid that is closest to the specified input vector.
     *
     * @param input the input vector
     * @return the closest unit (centroid)
     */
    @Override
    public Object findClosestUnit(double[] input) {
        return findClosestCluster(input).getCentroid();
    }

    /**
     * Returns the coordinates of the specified unit if it is a Centroid.
     *
     * @param unit the unit to coordinates from
     * @return the coordinates of the unit
     * @throws IllegalArgumentException if the unit is not a Centroid
     */
    @Override
    public double[] getUnitCoordinates(Object unit) {
        if (unit instanceof Centroid) {
            return ((Centroid) unit).getCoordinates();
        }
        throw new IllegalArgumentException("Invalid unit type provided");
    }

    /**
     * Updates the coordinates of the specified node with the provided values.
     *
     * @param unit the unit whose coordinates need to be updated
     *             Must be an instance of Centroid and should belong to the current instance
     * @param arr  an array representing the new coordinates for the unit
     * @throws IllegalArgumentException if the provided unit is not of type Centroid
     */
    @Override
    public void updateUnitCoordinates(Object unit, double[] arr) {
        if (!(unit instanceof Centroid)) {
            throw new IllegalArgumentException("Invalid unit type provided. Must be an instance of Centroid.");
        }
        // Check if the node belongs to the current instance
        if (getUnits().contains(unit)) {
            ((Centroid) unit).setCoordinates(arr);
        } else {
            throw new IllegalArgumentException("The specified unit does not belong to this Mini Batch K-Means instance.");
        }
    }

    /**
     * Returns a list of all centroids in the model.
     *
     * @return a list containing all centroids
     */
    @Override
    public List<Object> getUnits() {
        ArrayList<Object> centroids = new ArrayList<>();
        for (Cluster cluster : clusters) {
            centroids.add(cluster.getCentroid());
        }
        return centroids;
    }

    /**
     * Sets the centroids of the clusters to a predefined list of Centroids.
     *
     * @param units the list of centroids to set as the clusters
     * @throws IllegalArgumentException if the number of units does not match the number of clusters
     *                                  or if any unit is not a Centroid
     */
    @Override
    public void setUnits(List<Object> units) {
        if (units.size() != clusters.size()) {
            throw new IllegalArgumentException("The number of units does not match the number of clusters.");
        }

        for (int i = 0; i < k; i++) {
            Object unit = units.get(i);
            if (!(unit instanceof Centroid newCentroid)) {
                throw new IllegalArgumentException("All units must be of type Centroid.");
            }

            // Update the cluster's centroid
            clusters.get(i).getCentroid().setCoordinates(newCentroid.getCoordinates());
        }
    }

    /**
     * Returns a deep copy list of all centroids in the model.
     *
     * @return a deep copy list containing all centroids
     */
    public List<Object> getUnitsDeepCopy() {
        List<Object> centroidsDeepCopy = new ArrayList<>();
        for (Cluster cluster : clusters) {
            Centroid originalCentroid = cluster.getCentroid();
            // Create a deep copy of the centroid
            Centroid deepCopyCentroid = new Centroid(originalCentroid.getCoordinates().clone());
            centroidsDeepCopy.add(deepCopyCentroid);
        }
        return centroidsDeepCopy;
    }
}
