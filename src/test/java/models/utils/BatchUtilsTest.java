package test.java.models.utils;

import main.java.models.utils.BatchUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BatchUtilsTest {

    Random random = new Random();

    @Test
    public void testGetMiniBatch() {
         double[][] data = {
                {1.0, 1.0, 1.0},
                {0.5, 0.5, 0.5},
                {0.8, 0.8, 0.8},
                {0.3, 0.3, 0.3},
        };
        int batchSize = 2;
        double[][] miniBatch = BatchUtils.getMiniBatch(data, batchSize, random);

        assertNotNull(miniBatch, "The mini-batch should not be null");
        assertEquals(batchSize, miniBatch.length, "The mini-batch size should match the requested batch size");
        assertEquals(data[0].length, miniBatch[0].length, "Each row in the mini-batch should match the data row size");
    }
}