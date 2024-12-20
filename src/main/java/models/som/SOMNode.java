package main.java.models.som;

/**
 * Represents a node in a Self-Organizing Map (SOM) with a set of weights and coordinates.
 */
public class SOMNode {
    private final int x; // The X-coordinate of the node in the grid
    private final int y; // The Y-coordinate of the node in the grid
    private double[] weights; // The weight vector for the node

    /**
     * Constructs a SOMNode instance with a specified dimensionality for weights and 2D coordinates in the grid.
     *
     * @param dimension the number of dimensions
     * @param x         the x-coordinate of the node in the grid
     * @param y         the y-coordinate of the node in the grid
     */
    public SOMNode(int dimension, int x, int y) {
        this.weights = new double[dimension];
        this.x = x;
        this.y = y;
        initializeWeights();
    }

    /**
     * Initializes the weights of the node to random values between 0 and 1.
     */
    private void initializeWeights() {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = Math.random();
        }
    }

    /**
     * Returns the weights of the node.
     *
     * @return an array of weights
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     * Sets the weights of the node to the specified values.
     *
     * @param weights an array of new weight values
     * @throws IllegalArgumentException if the input weights are null
     * or if the input length weights are of different length than the original ones
     */
    public void setWeights(double[] weights) {
        if (weights == null || weights.length != this.weights.length) {
            throw new IllegalArgumentException(
                    "Weights array must not be null and must have the same length as the existing weights."
            );
        }
        this.weights = weights;
    }

    /**
     * Updates the weights of the node based on the input vector, learning rate and neighbourhood influence.
     *
     * @param input        the input vector used for updating weights
     * @param learningRate the learning rate used during the update
     * @param influence    the neighbourhood influence factor based on the distance to the Best Matching Unit (BMU)
     */
    public void updateWeights(double[] input, double learningRate, double influence) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] += learningRate * influence * (input[i] - weights[i]);
        }
    }

    /**
     * Returns the x-coordinate of the node in the grid.
     *
     * @return the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the node in the grid.
     *
     * @return the y-coordinate
     */
    public int getY() {
        return y;
    }

}
