package main.java.models.decays;

import main.java.models.interfaces.Decay;

/**
 * Implements a linear decay functionality for reducing values.
 */
public class LinearDecay implements Decay {
    /**
     * Applies a linear decay function to reduce the initial value based on the
     * current epoch and total number of epochs.
     *
     * @param initialValue the initial value to be decayed
     * @param epoch        the current epoch
     * @param totalEpochs  the total number of epochs
     * @return the decayed value
     * @throws IllegalArgumentException if any input value is negative or the total epochs is 0
     */
    @Override
    public double compute(double initialValue, int epoch, int totalEpochs) {
        if (initialValue < 0 || epoch < 0 || totalEpochs <= 0) {
            throw new IllegalArgumentException("All input values must be positive, and totalEpochs must be greater than zero.");
        }
        double coefficient = 1.0 - ((double) epoch / totalEpochs);
        return coefficient * initialValue;
    }
}
