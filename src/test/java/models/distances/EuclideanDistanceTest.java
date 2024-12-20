package test.java.models.distances;

import main.java.models.distances.EuclideanDistance;
import main.java.models.interfaces.Distance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EuclideanDistanceTest {
    Distance distance;

    @BeforeEach
    void setUp() {
        distance = new EuclideanDistance();
    }

    @Test
    public void testCompute_ValidVectors() {
        double[] a = {0.1, 0.2, 0.3};
        double[] b = {0.4, 0.5, 0.6};

        double expected = Math.sqrt(Math.pow(0.4 - 0.1, 2) + Math.pow(0.5 - 0.2, 2) + Math.pow(0.6 - 0.3, 2));
        double result = distance.compute(a, b);

        assertEquals(expected, result, "The Euclidean distance for valid vectors should match expected value");
    }

    @Test
    public void testCompute_VectorsWithDifferentLengths() {
        double[] a = {0.1, 0.2};
        double[] b = {0.1, 0.2, 0.3};

        assertThrows(IllegalArgumentException.class, () -> {
            distance.compute(a, b);
        }, "Vectors with different lengths should throw IllegalArgumentException");
    }
}