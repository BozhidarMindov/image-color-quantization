package test.java.models.som;


import main.java.models.som.SOMNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SOMNodeTest {

    SOMNode node;

    @BeforeEach
    void setUp() {
        node = new SOMNode(3, 0, 0);
    }

    @Test
    public void testSetWeights_ValidInput() {
        double[] newWeights = {0.1, 0.2, 0.3};
        node.setWeights(newWeights);
        assertArrayEquals(newWeights, node.getWeights(), "The weights should be updated correctly");
    }

    @Test
    public void testSetWeights_InvalidInput() {
        // Null weights
        assertThrows(IllegalArgumentException.class, () -> node.setWeights(null),
                "Setting null weights should throw an exception");

        // Mismatched dimensions
        double[] invalidWeights = {0.1, 0.2};
        assertThrows(IllegalArgumentException.class, () -> node.setWeights(invalidWeights),
                "Setting weights with mismatched dimensions should throw an exception");
    }

    @Test
    public void testUpdateWeights() {
        double[] input = {0.5, 0.5, 0.5};
        double learningRate = 0.1;
        double influence = 0.8;

        double[] initialWeights = node.getWeights().clone();
        node.updateWeights(input, learningRate, influence);

        double[] updatedWeights = node.getWeights();
        for (int i = 0; i < updatedWeights.length; i++) {
            double expected = initialWeights[i] + learningRate * influence * (input[i] - initialWeights[i]);
            assertEquals(expected, updatedWeights[i], "The weights should be updated correctly");
        }
    }
}