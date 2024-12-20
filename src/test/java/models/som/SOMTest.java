package test.java.models.som;

import main.java.models.decays.LinearDecay;
import main.java.models.distances.EuclideanDistance;
import main.java.models.interfaces.Quantizer;
import main.java.models.som.SOM;
import main.java.models.som.SOMNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SOMTest {
    Quantizer som;

    @BeforeEach
    void setUp() {
        som = new SOM(3, 3, 3, new EuclideanDistance(), new LinearDecay());
    }

    @Test
    public void testTrain_ValidInput() {
        double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8}
        };
        assertDoesNotThrow(() -> som.train(data, 10), "Training should not throw exceptions with valid input");
    }

    @Test
    public void testTrain_InvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> som.train(null, 10), "Training with null data should throw an exception");
        assertThrows(IllegalArgumentException.class, () -> som.train(new double[][]{}, 10), "Training with empty data should throw an exception");
        assertThrows(IllegalArgumentException.class, () -> som.train(new double[][]{{0.1,0.1, 0.1}}, 10),
                "Training with smaller data than the amount of nodes desired should throw an exception");

        double[][] data = {
                {1.0, 1.0},
                {0.5, 0.5}
        };
        assertThrows(IllegalArgumentException.class, () -> som.train(data, 10), "Training with different input dimensions should throw an exception");
    }

    @Test
    public void testFindClosestUnit() {
          double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8}
        };
        som.train(data, 10);
        Object closestUnit = som.findClosestUnit(new double[]{0.3, 0.3, 0.3});
        assertNotNull(closestUnit, "The closest unit should not be null");
        assertInstanceOf(SOMNode.class, closestUnit, "The closest unit should be a SOMNode");
    }


    @Test
    public void testGetUnits() {
        List<Object> units = som.getUnits();
        assertEquals(9, units.size(), "The number of units should match the total nodes in the SOM");
    }

    @Test
    public void testGetUnitsDeepCopy() {
        List<Object> deepCopy = som.getUnitsDeepCopy();
        assertEquals(9, deepCopy.size(), "Deep copy should contain all nodes");

        SOMNode original = (SOMNode) som.getUnits().getFirst();
        SOMNode copy = (SOMNode) deepCopy.getFirst();

        assertNotSame(original, copy, "Deep copy nodes should not be the same instances as the original nodes");
        assertArrayEquals(original.getWeights(), copy.getWeights(), "The weights of deep copy nodes should match the original nodes' weights");
    }

    @Test
    public void testSetUnits_ValidInput() {
        List<Object> newUnits = som.getUnitsDeepCopy();
        assertDoesNotThrow(() -> som.setUnits(newUnits), "Setting valid nodes should not throw an exception ");
    }

    @Test
    public void testSetUnits_InvalidInput() {
        List<Object> invalidUnits = List.of(new Object(), new Object(), new Object());
        assertThrows(IllegalArgumentException.class, () -> som.setUnits(invalidUnits), "Setting invalid nodes should throw an exception");
    }

    @Test
    public void testUpdateUnitCoordinates_ValidUnit() {
        SOMNode unit = (SOMNode) som.getUnits().getFirst();
        double[] newWeights = {0.5, 0.5, 0.5};
        assertDoesNotThrow(() -> som.updateUnitCoordinates(unit, newWeights), "Updating a valid node should not throw an exception");
        assertArrayEquals(newWeights, unit.getWeights(), "The weights should be updated correctly");
    }

    @Test
    public void testUpdateUnitCoordinates_InvalidUnit() {
        SOMNode invalidUnit = new SOMNode(3, 1, 1);
        double[] newWeights = {0.5, 0.5, 0.5};
        assertThrows(IllegalArgumentException.class, () -> som.updateUnitCoordinates(invalidUnit, newWeights),
                "Updating an invalid node should throw an exception");
    }
}