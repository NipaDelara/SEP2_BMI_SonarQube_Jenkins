package org.example.demo1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BMICalculatorTest {

    // Test BMI calculation using sample values
    @Test
    void testBMICalculation() {

        // Arrange: weight in kg and height in meters
        double weight = 70.0;
        double height = 1.75;
        double expected = 22.86;

        // Act: calculate BMI
        double actual = weight / (height * height);

        // Assert: compare expected and actual results
        assertEquals(expected, actual, 0.01);
    }
}
