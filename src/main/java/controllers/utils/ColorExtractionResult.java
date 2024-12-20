package main.java.controllers.utils;

/**
 * Represents the result of the color extraction process, containing the extracted colors
 * and the count of unique colors.
 */
public class ColorExtractionResult {
    private final double[][] colors; // A 2D array representing the extracted colors.
    private final int uniqueColorCount; // The count of unique colors in the extracted result

    /**
     * Constructs a new ColorExtractionResult with the specified extracted colors and unique color count.
     *
     * @param colors           A 2D array of extracted colors.
     * @param uniqueColorCount The number of unique colors in the extracted result.
     */
    public ColorExtractionResult(double[][] colors, int uniqueColorCount) {
        this.colors = colors;
        this.uniqueColorCount = uniqueColorCount;
    }

    /**
     * Returns the extracted colors.
     *
     * @return A 2D array of extracted colors.
     */
    public double[][] getColors() {
        return colors;
    }

    /**
     * Returns the count of unique colors in the extracted result.
     *
     * @return The count of unique colors.
     */
    public int getUniqueColorCount() {
        return uniqueColorCount;
    }
}
