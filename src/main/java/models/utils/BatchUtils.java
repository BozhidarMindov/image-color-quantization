package main.java.models.utils;

import java.util.Random;

/**
 * Provides utility methods for handling operations related to data batching.
 */
public class BatchUtils {

    /**
     * Selects and returns a mini-batch of random data points from the input data.
     *
     * @param data      the input data
     * @param batchSize the size of the mini-batch to select
     * @param random    a Random instance for generating random indexes
     * @return a 2D array representing the mini-batch
     */
    public static double[][] getMiniBatch(double[][] data, int batchSize, Random random) {
        double[][] miniBatch = new double[batchSize][data[0].length];
        for (int i = 0; i < batchSize; i++) {
            miniBatch[i] = data[random.nextInt(data.length)];
        }
        return miniBatch;
    }
}
