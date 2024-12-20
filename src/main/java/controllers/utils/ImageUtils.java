package main.java.controllers.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import main.java.models.interfaces.Quantizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Provides utility methods for working with images.
 */
public class ImageUtils {
    /**
     * Determines the file format on a provided image file based on the file extension.
     *
     * @param file the image file
     * @return a string which represents the image format (if it is in 'jpeg', 'jpg' or 'png').
     * If the format is not one of the specified above, null will be returned
     */
    public static String getImageFormat(File file) {
        String fileFormat = null;
        // Determine format based on file extension
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            fileFormat = "jpg";
        } else if (fileName.endsWith(".png")) {
            fileFormat = "png";
        }
        return fileFormat;
    }

    /**
     * Extracts the colors from a BufferedImage and returns a result containing color data
     * and the count of unique colors in the image.
     *
     * @param image the input BufferedImage
     * @return a ColorExtractionResult containing extracted color data
     */
    public static ColorExtractionResult extractColors(BufferedImage image) {
        // A set to track unique colors
        Set<Integer> uniqueColors = new HashSet<>();
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] colors = new double[width * height][3];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgbColor = image.getRGB(x, y);
                Color color = new Color(rgbColor);
                colors[index] = new double[]{
                        color.getRed() / 255.0,
                        color.getGreen() / 255.0,
                        color.getBlue() / 255.0
                };
                uniqueColors.add(rgbColor);
                index++;
            }
        }
        return new ColorExtractionResult(colors, uniqueColors.size());
    }

    /**
     * Resizes a BufferedImage to the specified dimensions and converts it to a FX image.
     *
     * @param originalImage the original BufferedImage to resize
     * @param width         the target width
     * @param height        the target height
     * @return the resized FX image
     */
    public static Image resizeAndConvertToFxImage(BufferedImage originalImage, int width, int height) {
        // Create a new BufferedImage with the desired dimensions
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Draw the original image onto the resized image
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();

        // Convert the BufferedImage to a FX Image
        return SwingFXUtils.toFXImage(resizedImage, null);
    }

    /**
     * Updates a BufferedImage by changing the color of provided pixels to a new color.
     *
     * @param image    the BufferedImage to update
     * @param pixels   a list of pixel coordinates to update
     * @param newColor the new color to apply to the pixels
     */
    public static void updateImageWithNewColor(BufferedImage image, List<Point> pixels, Color newColor) {
        int rgb = newColor.getRGB();
        for (Point p : pixels) {
            image.setRGB(p.x, p.y, rgb);
        }
    }

    /**
     * Creates a copy of a provided BufferedImage.
     *
     * @param source the source BufferedImage to copy
     * @return a new BufferedImage that is a copy of the source one
     */
    public static BufferedImage copyBufferedImage(BufferedImage source) {
        BufferedImage newImage = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics graphics = newImage.getGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return newImage;
    }

    /**
     * Saves a BufferedImage as an 8-bit PNG file using a specified color-to-pixel mapping
     * and quantizer for color conversion.
     *
     * @param image            the source BufferedImage
     * @param colorToPixelsMap a map of color keys to pixel coordinates
     * @param outputFile       the file to save the image to
     * @param quantizer        the quantizer used to determine colors
     * @throws IOException if an error occurs during file writing
     */
    public static void saveAs8BitPng(BufferedImage image, Map<Object, List<Point>> colorToPixelsMap, File outputFile, Quantizer quantizer) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] reds = new byte[256];
        byte[] greens = new byte[256];
        byte[] blues = new byte[256];
        Map<Integer, Integer> colorToIndex = new HashMap<>();

        // Collect unique colors and map them to indices
        int colorIndex = 0;
        for (Object key : colorToPixelsMap.keySet()) {
            Color color = getColorFromUnitCoordinates(quantizer.getUnitCoordinates(key));
            int rgb = color.getRGB();

            // If this color hasn't been added to the map yet, add it
            if (!colorToIndex.containsKey(rgb)) {
                if (colorIndex >= 256) {
                    throw new IOException("The image contains more than 256 colors, not suitable for 8-bit PNG.");
                }
                reds[colorIndex] = (byte) color.getRed();
                greens[colorIndex] = (byte) color.getGreen();
                blues[colorIndex] = (byte) color.getBlue();
                colorToIndex.put(rgb, colorIndex);
                colorIndex++;
            }
        }

        // Make an indexed color model
        IndexColorModel colorModel = new IndexColorModel(8, colorIndex, reds, greens, blues);

        // Make a new BufferedImage with the indexed color model
        BufferedImage indexedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        // Iterate over the map to set the pixel values in the indexed image
        for (Map.Entry<Object, List<Point>> entry : colorToPixelsMap.entrySet()) {
            double[] unitCoordinates = quantizer.getUnitCoordinates(entry.getKey());
            int rgb = getColorFromUnitCoordinates(unitCoordinates).getRGB();
            int colorIndexForPixel = colorToIndex.get(rgb);

            for (Point p : entry.getValue()) {
                indexedImage.getRaster().setSample(p.x, p.y, 0, colorIndexForPixel);
            }
        }

        // Save the image
        ImageIO.write(indexedImage, "png", outputFile);
    }

    /**
     * Converts an array of unit coordinates to a Color object.
     *
     * @param unitCoordinates the unit coordinates
     * @return a Color object corresponding to the unit coordinates
     */
    public static Color getColorFromUnitCoordinates(double[] unitCoordinates) {
        return new Color(
                (int) (unitCoordinates[0] * 255),
                (int) (unitCoordinates[1] * 255),
                (int) (unitCoordinates[2] * 255)
        );
    }

    /**
     * Converts a BufferedImage with transparency (alpha channel) to a BufferedImage with a solid background color.
     * The method creates a new BufferedImage without an alpha channel, fills it with a specified background color,
     * and then draws the original image on top of it.
     *
     * @param imageWithAlpha  the input BufferedImage that contains transparency
     * @param backgroundColor the color to be used as the background for the transparent areas
     * @return a new BufferedImage with a specified background color
     */
    public static BufferedImage convertTransparentToColor(BufferedImage imageWithAlpha, Color backgroundColor) {
        // Create a new BufferedImage without an alpha channel (which only RGB channels)
        BufferedImage background = new BufferedImage(
                imageWithAlpha.getWidth(),
                imageWithAlpha.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        // Fill the new image with the specified background color
        Graphics2D g2d = background.createGraphics();
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, background.getWidth(), background.getHeight());

        // Draw the original image (with transparency) on top of the new background
        g2d.drawImage(imageWithAlpha, 0, 0, null);
        g2d.dispose();

        return background;
    }
}
