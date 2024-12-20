package test.java.models.minibatchkmeans;

import main.java.models.decays.LinearDecay;
import main.java.models.distances.EuclideanDistance;
import main.java.models.minibatchkmeans.Centroid;
import main.java.models.minibatchkmeans.MiniBatchKMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MiniBatchKMeansTest {
    MiniBatchKMeans miniBatchKMeans;

    @BeforeEach
    void setUp() {
        miniBatchKMeans = new MiniBatchKMeans(3, new EuclideanDistance(), new LinearDecay());
    }

    @Test
    public void testTrain_ValidInput() {
        double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
        };

        assertDoesNotThrow(() -> miniBatchKMeans.train(data, 10), "Training should not throw exceptions with valid input");
    }

    @Test
    public void testTrain_InvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> miniBatchKMeans.train(null, 10),
                "Training with null data should throw an exception");
        assertThrows(IllegalArgumentException.class, () -> miniBatchKMeans.train(new double[][]{}, 10),
                "Training with empty data should throw an exception");
        assertThrows(IllegalArgumentException.class, () -> miniBatchKMeans.train(new double[][]{{0.1,0.1, 0.1}}, 10),
                "Training with smaller data than the amount of clusters desired should throw an exception");
    }

    @Test
    public void testFindClosestUnit() {
        double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
        };
        miniBatchKMeans.train(data, 10);
        Object closestUnit = miniBatchKMeans.findClosestUnit(new double[]{0.3, 0.3, 0.3});

        assertNotNull(closestUnit, "The closest centroid should not be null");
        assertInstanceOf(Centroid.class, closestUnit, "The closest unit should be a Centroid");
    }

    @Test
    public void testGetUnits() {
        double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
        };
        miniBatchKMeans.train(data, 10);
        List<Object> units = miniBatchKMeans.getUnits();

        assertEquals(3, units.size(), "The number of units should match the total number of clusters");
    }

    @Test
    public void testGetUnitsDeepCopy() {
        double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
        };
        miniBatchKMeans.train(data, 10);
        List<Object> deepCopy = miniBatchKMeans.getUnitsDeepCopy();
        assertEquals(3, deepCopy.size(), "Deep copy should contain all centroids");
        Centroid original = (Centroid) miniBatchKMeans.getUnits().getFirst();
        Centroid copy = (Centroid) deepCopy.getFirst();

        assertNotSame(original, copy, "Deep copy centroids should not be the same instances as the original centroids");
        assertArrayEquals(original.getCoordinates(), copy.getCoordinates(), "The coordinates of deep copy centroids should match the original centroids' coordinates");
    }

    @Test
    public void testSetUnits_ValidInput() {
         double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
        };
        miniBatchKMeans.train(data, 10);
        List<Object> newUnits = miniBatchKMeans.getUnitsDeepCopy();

        assertDoesNotThrow(() -> miniBatchKMeans.setUnits(newUnits),
                "Setting valid centroids should not throw exceptions");
    }

    @Test
    public void testSetUnits_InvalidInput() {
        List<Object> invalidUnits = List.of(new Object(), new Object(), new Object());

        assertThrows(IllegalArgumentException.class, () -> miniBatchKMeans.setUnits(invalidUnits), "Setting invalid centroids should throw an exception");
    }

    @Test
    public void testUpdateUnitCoordinates_ValidUnit() {
        double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
        };
        miniBatchKMeans.train(data, 10);
        Centroid unit = (Centroid) miniBatchKMeans.getUnits().getFirst();
        double[] newCoordinates = {0.5, 0.5, 0.5};
        assertDoesNotThrow(() -> miniBatchKMeans.updateUnitCoordinates(unit, newCoordinates), "Updating a valid centroid should not throw an exception");
        assertArrayEquals(newCoordinates, unit.getCoordinates(), "Coordinates should be updated correctly");
    }

    @Test
    public void testUpdateUnitCoordinates_InvalidUnit() {
        Centroid invalidUnit = new Centroid(new double[]{1.0, 1.0, 1.0});
        double[] newCoordinates = {0.5, 0.5, 0.5};

        assertThrows(IllegalArgumentException.class, () -> miniBatchKMeans.updateUnitCoordinates(invalidUnit, newCoordinates),
                "Updating an invalid centroid should throw an exception");
    }
}