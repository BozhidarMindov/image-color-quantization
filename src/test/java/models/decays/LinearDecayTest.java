package test.java.models.decays;

import main.java.models.decays.LinearDecay;
import main.java.models.interfaces.Decay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinearDecayTest {
    Decay decay;
    double initialValue;
    int totalEpochs;

    @BeforeEach
    void setUp() {
        decay = new LinearDecay();
        initialValue = 0.5;
        totalEpochs = 10;
    }

    @Test
    public void testCompute_ZeroEpoch() {
        int epoch = 0;
        double expected = 0.5;
        double result = decay.compute(initialValue, epoch, totalEpochs);

        assertEquals(expected, result, "Input value should remain the same at epoch 0");
    }

    @Test
    public void testCompute_HalfwayEpoch() {
        int epoch = 5;
        double expected = 0.25;
        double result = decay.compute(initialValue, epoch, totalEpochs);

        assertEquals(expected, result, "Input Value should be halved at halfway epoch");
    }

    @Test
    public void testCompute_LastEpoch() {
        int epoch = 10;
        double expected = 0.0;
        double result = decay.compute(initialValue, epoch, totalEpochs);

        assertEquals(expected, result, "Input Value should be 0 at the last epoch");
    }

    @Test
    public void testCompute_NegativeEpoch() {
        int epoch = -1;
        
        assertThrows(IllegalArgumentException.class, () -> {
            decay.compute(initialValue, epoch, totalEpochs);
        }, "Negative epoch should throw an IllegalArgumentException");
    }
}