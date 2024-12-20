package test.java.models.minibatchkmeans;

import main.java.models.minibatchkmeans.Centroid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CentroidTest {
    double[] coordinates;

    @BeforeEach
    void setUp() {
        coordinates = new double[]{1.0, 1.0, 1.0};
    }

    @Test
    public void testSetCoordinates_ValidInput() {
        Centroid centroid = new Centroid(coordinates);
        double[] newCoordinates = {0.5, 0.5, 0.5};
        centroid.setCoordinates(newCoordinates);

        assertArrayEquals(newCoordinates, centroid.getCoordinates(),
                "The centroid coordinates should be updated correctly");
    }

    @Test
    public void testSetCoordinates_InvalidInput() {
        Centroid centroid = new Centroid(coordinates);

        assertThrows(IllegalArgumentException.class, () -> centroid.setCoordinates(null),
                "Setting null coordinates should throw an exception");
        assertThrows(IllegalArgumentException.class, () -> centroid.setCoordinates(new double[]{1.0}),
                "Setting coordinates with mismatched dimensions should throw an exception");
    }
}