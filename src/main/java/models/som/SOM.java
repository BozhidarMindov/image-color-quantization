package main.java.models.som;

import main.java.models.interfaces.Decay;
import main.java.models.interfaces.Distance;
import main.java.models.interfaces.Quantizer;
import main.java.models.utils.BatchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implements a Self-Organizing Map (SOM). This is a neural network which uses a two-dimensional grid of nodes
 * to represent high-dimensional input data.
 */
public class SOM implements Quantizer {
    private final SOMNode[][] map; // A 2D grid representing the SOM
    private final int inputDimension; // The dimension of input data
    private final int mapWidth; // The width of the SOM grid
    private final int mapHeight; // The height of the SOM grid
    private final double initialLearningRate; // The initial learning rate for training (set to 0.5 by default)
    private final double initialRadius; // The initial neighborhood radius for training
    private final Distance distance; // The distance metric used to find the closest units
    private final Decay decay; // The decay metric used to decay the values of the learning rate and radius
    private final Random random; // A random generator for selecting input samples

    /**
     * Constructs a Self-Organizing Map (SOM) instance with the specified input dimensions, map width and height
     * and distance metric.
     *
     * @param inputDimension the dimensionality of input vectors
     * @param mapWidth       the width of the SOM grid
     * @param mapHeight      the height of the SOM grid
     * @param distance       the distance metric used to compute distance between nodes and input vectors
     * @param decay          the decay metric to use in the model
     */
    public SOM(int inputDimension, int mapWidth, int mapHeight, Distance distance, Decay decay) {
        this.inputDimension = inputDimension;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.initialLearningRate = 0.5;
        this.initialRadius = Math.max(mapWidth, mapHeight) / 2.0;
        this.distance = distance;
        this.decay = decay;
        this.map = new SOMNode[mapWidth][mapHeight];
        this.random = new Random();
        this.initializeMap();
    }

    /**
     * Initializes the nodes in the SOM grid by creating a new SOMNode for each position in the grid.
     */
    private void initializeMap() {
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                map[i][j] = new SOMNode(inputDimension, i, j);
            }
        }
    }

    /**
     * Returns the two-dimensional array representing the map.
     *
     * @return a 2D array of SOMNode objects
     */
    public SOMNode[][] getMap() {
        return map;
    }

    /**
     * Returns the width of the SOM grid.
     *
     * @return the width of the SOM grid
     */
    public int getMapWidth() {
        return mapWidth;
    }

    /**
     * Returns the height of the SOM grid.
     *
     * @return the height of the SOM grid
     */
    public int getMapHeight() {
        return mapHeight;
    }

    /**
     * Trains the SOM using the specified input data for a specific number of epochs.
     *
     * @param data   a 2D array representing the input data
     * @param epochs the number of epochs for training
     * @throws IllegalArgumentException if the input data is null, empty, or smaller than the selected number of nodes,
     * or if an input in the data has a different dimension than the one specified in the object instance
     */
    @Override
    public void train(double[][] data, int epochs) {
        if (data == null || data.length == 0 || data.length < getMapWidth() * getMapHeight()) {
            throw new IllegalArgumentException("Input data cannot be null or empty or smaller than the number of nodes.");
        }
        double learningRate = initialLearningRate;
        double radius = initialRadius;
        // Get batch size. Batch size is 2% of the data size, capped at 1024. At least 1 item in the batch.
        int batchSize = Math.max(1, Math.min((int) (data.length * 0.02), 1024));
        System.out.println("Batch size: " + batchSize);
        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Epoch " + (epoch + 1) + " | Learning rate: " + learningRate + " | Radius: " + radius);

            // Get a mini-batch of random points from data
            double[][] miniBatch = BatchUtils.getMiniBatch(data, batchSize, random);

            for (double[] input : miniBatch) {
                if (input.length != inputDimension) {
                    throw new IllegalArgumentException("Each input sample must have a dimensionality of " + inputDimension);
                }
                SOMNode bmu = findClosestUnit(input);

                // Update the weights of the SOM Nodes
                updateMapWeights(input, bmu, learningRate, radius);
            }

            // Apply decay and get the learning rate and neighborhood radius for next epoch
            learningRate = decay.compute(initialLearningRate, epoch, epochs);

            // Adjusting radius
            // For 80% of the epochs update the BMU and its nodes closest to it.
            // For 20% of epochs the radius will drop below 1, to allow for only updating the BMU.
            if (epoch < 0.8 * epochs) {
                radius = Math.max(1.0, decay.compute(initialRadius, epoch, epochs));
            } else {
                radius = decay.compute(initialRadius, epoch, epochs);
            }
        }
    }

    /**
     * Updates the weights in the nodes of the map. Nodes closer to the BMU will be updated more.
     *
     * @param input        the input vector being processed
     * @param bmu          the best-matching unit of input
     * @param learningRate the current learning rate
     * @param radius       the current radius
     */
    private void updateMapWeights(double[] input, SOMNode bmu, double learningRate, double radius) {
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                SOMNode node = map[i][j];
                double dist = distance.compute(new double[]{bmu.getX(), bmu.getY()}, new double[]{node.getX(), node.getY()});
                if (dist <= radius) {
                    double influence = calculateInfluence(dist, radius);
                    node.updateWeights(input, learningRate, influence);
                }
            }
        }
    }

    /**
     * Calculates the influence a node has to another one based on the distance between them and the current radius.
     *
     * @param dist   the distance between two nodes
     * @param radius the current radius
     * @return a value indicating the influence
     */
    private double calculateInfluence(double dist, double radius) {
        return Math.exp(-Math.pow(dist, 2) / (2 * Math.pow(radius, 2)));
    }

    /**
     * Returns the node in the map that is closest to the specified input vector.
     *
     * @param input the input vector to match
     * @return the Best Matching Unit (BMU) for the input
     */
    @Override
    public SOMNode findClosestUnit(double[] input) {
        // Find the node with the closest distance to the input vector
        SOMNode bmu = null;
        double minDist = Double.MAX_VALUE;

        for (SOMNode[] somNodes : map) {
            for (SOMNode somNode : somNodes) {
                double dist = distance.compute(input, somNode.getWeights());
                if (dist < minDist) {
                    minDist = dist;
                    bmu = somNode;
                }
            }
        }
        return bmu;
    }

    /**
     * Returns the weights of the specified unit if it is a SOMNode.
     *
     * @param unit the unit to retrieve weights from
     * @return the weights of the unit
     * @throws IllegalArgumentException if the unit is not a SOMNode
     */
    @Override
    public double[] getUnitCoordinates(Object unit) {
        if (unit instanceof SOMNode) {
            return ((SOMNode) unit).getWeights();
        }
        throw new IllegalArgumentException("Invalid unit type provided");
    }

    /**
     * Updates the  weights of the specified node with the provided values.
     *
     * @param unit the unit whose wights need to be updated. Must be an instance of SOMNode
     *             and should belong the current instance
     * @param arr  an array representing the new weights for the unit
     * @throws IllegalArgumentException if the provided unit is not of type SOMNode
     */
    @Override
    public void updateUnitCoordinates(Object unit, double[] arr) {
        if (!(unit instanceof SOMNode)) {
            throw new IllegalArgumentException("Invalid unit type provided. Must be an instance of SOMNode.");
        }
        // Check if the node belongs to the SOM
        if (getUnits().contains(unit)) {
            ((SOMNode) unit).setWeights(arr);
        } else {
            throw new IllegalArgumentException("The specified unit does not belong to this SOM instance.");
        }
    }

    /**
     * Returns a flattened map (in list form) of all nodes in the map.
     *
     * @return a list containing all SOMNode objects in the map
     */
    @Override
    public List<Object> getUnits() {
        ArrayList<Object> flattenedMap = new ArrayList<>();

        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                flattenedMap.add(map[i][j]);
            }
        }
        return flattenedMap;
    }

    /**
     * Returns a deep copy flattened map (in list form) of all nodes in the map.
     *
     * @return a deep copy list containing all SOMNode objects in the map
     */
    public List<Object> getUnitsDeepCopy() {
        ArrayList<Object> flattenedMapDeepCopy = new ArrayList<>();
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                SOMNode originalNode = map[i][j];
                SOMNode deepCopyNode = new SOMNode(
                        originalNode.getWeights().length,
                        originalNode.getX(),
                        originalNode.getY()
                );
                deepCopyNode.setWeights(originalNode.getWeights().clone());
                flattenedMapDeepCopy.add(deepCopyNode);
            }
        }
        return flattenedMapDeepCopy;
    }

    /**
     * Sets the nodes of the SOM to a predefined list of SOMNodes.
     *
     * @param units the list of nodes to set as the units
     * @throws IllegalArgumentException if the number of units does not match the number of nodes
     *                                  or if any unit is not a SOMNode
     */
    @Override
    public void setUnits(List<Object> units) {
        if (units.size() != mapWidth * mapHeight) {
            throw new IllegalArgumentException("Number of units does not match the size of the SOM map.");
        }

        int index = 0;
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                Object unit = units.get(index);
                if (!(unit instanceof SOMNode newNode)) {
                    throw new IllegalArgumentException("All units must be of type SOMNode.");
                }
                map[i][j].setWeights(newNode.getWeights().clone());
                index++;
            }
        }
    }
}
