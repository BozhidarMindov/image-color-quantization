package main.java.models.interfaces;

import java.util.List;

/**
 * Defines a quantizer that can train on input data, find the closest matching unit,
 * and provide details about its units.
 */
public interface Quantizer {
    /**
     * Trains the quantizer using the provided input data over a specified number of epochs.
     *
     * @param data   a 2D array representing the input data
     * @param epochs the number of epochs
     */
    void train(double[][] data, int epochs);

    /**
     * Finds the unit that is the closest to the given input vector.
     *
     * @param input the input vector
     * @return the closest unit to the input
     */
    Object findClosestUnit(double[] input);

    /**
     * Returns the coordinates/weights of a specified unit.
     *
     * @param unit the unit to retrieve data from
     * @return an array representing the unit's coordinates/weights
     */
    double[] getUnitCoordinates(Object unit);


    /**
     * Updates the coordinates/weights of the specified unit with the provided values.
     *
     * @param unit the unit whose coordinates/weights need to be updated
     * @param arr  an array representing the new coordinates/weights for the unit
     * @throws IllegalArgumentException if the unit is invalid or the array dimensions do not match the expected size or
     *                                  the unit does not belong to the current instance
     */
    void updateUnitCoordinates(Object unit, double[] arr);

    /**
     * Returns a list of all units in the quantizer.
     *
     * @return a list of units
     */
    List<Object> getUnits();

    /**
     * Sets the units of a quantizer from a list of unit objects.
     *
     * @param units the list of units to use for the setting
     * @throws IllegalArgumentException if the number of units does not match the number
     *                                  of original units or if any unit type is the same as the original
     */
    void setUnits(List<Object> units);

    /**
     * Returns a deep copy list of all units in the quantizer.
     *
     * @return a deep copy of the list of units
     */
    List<Object> getUnitsDeepCopy();

}
