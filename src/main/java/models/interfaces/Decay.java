package main.java.models.interfaces;

/**
 * Defines a metric that returns a decayed value based on an input one
 */
public interface Decay {
    /**
     * Computes the decayed value based on the current epoch and the total epochs.
     *
     * @param initialValue the initial value to be decayed
     * @param epoch        the current epoch
     * @param totalEpochs  the total number of epochs
     * @return the decayed value
     */
    double compute(double initialValue, int epoch, int totalEpochs);
}
