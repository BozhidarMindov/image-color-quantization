package main.java.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.controllers.utils.ColorExtractionResult;
import main.java.models.decays.LinearDecay;
import main.java.models.distances.EuclideanDistance;
import main.java.models.interfaces.Decay;
import main.java.models.interfaces.Distance;
import main.java.models.interfaces.Quantizer;
import main.java.models.minibatchkmeans.MiniBatchKMeans;
import main.java.models.som.SOM;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.*;

import static main.java.controllers.utils.GeneralUtils.*;
import static main.java.controllers.utils.ImageUtils.*;

/**
 * Represents the controller for the UI of the application. This class handles all user interaction, image loading,
 * processing, file saving and color quantization using SOM or Mini Batch K-means.
 */
public class QuantizerController implements Initializable {
    private static final int DISPLAY_IMAGE_MAX_HEIGHT = 350;
    private static final int DISPLAY_IMAGE_MAX_WIDTH = 350;
    private static final int MAX_TOTAL_PIXELS = 49_000_000;
    private static final int MAX_SIZE = 50;
    @FXML
    public TilePane learnedColorsPane;
    // A copy of the original units
    List<Object> originalUnits;
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView quantizedImageView;
    @FXML
    private ComboBox<String> algorithmComboBox;
    @FXML
    private TextField mapWidthField;
    @FXML
    private TextField mapHeightField;
    @FXML
    private TextField epochsField;
    @FXML
    private TextField kField;
    @FXML
    private Label originalSizeLabel;
    @FXML
    private Label convertedSizeLabel;
    @FXML
    private Label countColorsOriginalImageLabel;
    @FXML
    private Label countColorsQuantizedImageLabel;
    @FXML
    private Label meanSquaredErrorLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label originalLabel;
    @FXML
    private Label quantLabel;
    @FXML
    private HBox colorPickerBox;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private HBox imageBox;
    @FXML
    private Button resetColorButton;
    @FXML
    private Pane somPane;
    @FXML
    private Pane miniBatchKmeansPane;
    // Global objects needed for the program to function correctly
    private String originalFormat;
    private BufferedImage fullQuantizedImage;
    private BufferedImage originalQuantizedImage;
    private BufferedImage originalImage;
    private Object selectedUnit;
    private Quantizer quantizer;
    // Mapping for each BMU/centroid to corresponding pixels
    private Map<Object, List<Point>> pixelMap;
    // Mapping to store color squares for easy access when colors change
    private Map<Object, Rectangle> colorSquares;

    /**
     * Initializes the controller and sets up the UI components, event listeners, and initial state of the app.
     *
     * @param url            the location used to resolve relative paths for the root object, or null if not known
     * @param resourceBundle the resources used to localize the root object, or null if not available
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get UI elements ready
        algorithmComboBox.getItems().addAll("SOM", "Mini Batch K-means");

        // Add listener for combo box value change
        algorithmComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("SOM".equals(newValue)) {
                somPane.setVisible(true);
                miniBatchKmeansPane.setVisible(false);
            } else if ("Mini Batch K-means".equals(newValue)) {
                somPane.setVisible(false);
                miniBatchKmeansPane.setVisible(true);
            }
        });

        originalLabel.setVisible(false);
        quantLabel.setVisible(false);
        // Add SOM as the initial algorithm
        algorithmComboBox.setValue("SOM");

        imageBox.setSpacing(10);

        learnedColorsPane.setHgap(5);
        learnedColorsPane.setVgap(5);

        // Disable quantizedImageView initially since there is no image in it
        quantizedImageView.setDisable(true);

        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(200);

        resetColorButton.setVisible(false);

        filterIntegerTextField(epochsField, "500", 100, 10000);

        // Filter fields for SOM parameters
        filterIntegerTextField(mapWidthField, "4", 1, 16);
        filterIntegerTextField(mapHeightField, "4", 1, 16);

        //Filter fields for KMeans parameters
        filterIntegerTextField(kField, "16", 1, 256);

        // Add color-picker functionality (initially invisible)
        setUpColorPicker();
    }

    /**
     * Handles the upload of images (when the upload button is clicked), resizes the original image and
     * displays it in the originalImageView.
     */
    @FXML
    private void uploadImage() {
        clear();
        FileChooser fileChooser = createFileChooser(
                "Select Image",
                new FileChooser.ExtensionFilter("Image File", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) originalImageView.getScene().getWindow();

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            // Enforce max size
            if (selectedFile.length() > MAX_SIZE * 1024 * 1024) {
                showAlert("Image Size Limit Exceeded (" + MAX_SIZE + "MB)");
                return;
            }
            handleSelectedFile(selectedFile);
        } else {
            showAlert("File selection was canceled.");
        }
    }

    /**
     * Handles all actions related to the input file.
     *
     * @param selectedFile the input file
     */
    private void handleSelectedFile(File selectedFile) {
        try {
            // Determine original image format
            originalFormat = getImageFormat(selectedFile);
            if (originalFormat == null) {
                showAlert("Unrecognized image format!");
                originalFormat = null;
                return;
            }
            loadAndDisplayOriginalImage(selectedFile);
        } catch (IOException e) {
            showAlert("Error reading the image file: " + e.getMessage());
        }
    }

    /**
     * Loads the input image file, removes alpha channel if present, resizes and display the image.
     *
     * @param selectedFile the input image file
     * @throws IOException if an expectation occurs while reading the image
     */
    private void loadAndDisplayOriginalImage(File selectedFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(selectedFile);

        if (bufferedImage.getHeight() * bufferedImage.getWidth() > MAX_TOTAL_PIXELS) {
            showAlert("Image resolution is too large. Choose a smaller image!");
            originalSizeLabel.setText("");
            originalFormat = null;
            return;
        }

        if (bufferedImage.getColorModel().hasAlpha()) {
            // Replace the transparent background with white background
            bufferedImage = convertTransparentToColor(bufferedImage, Color.WHITE);
        }
        // Resize and convert the image to a JavaFX Image
        javafx.scene.image.Image image = resizeAndConvertToFxImage(
                bufferedImage, DISPLAY_IMAGE_MAX_WIDTH, DISPLAY_IMAGE_MAX_HEIGHT
        );
        originalImage = bufferedImage;
        originalImageView.setImage(image);
        originalLabel.setVisible(true);
        originalSizeLabel.setText(String.format(
                "Original Size: %.4f MB", (double) selectedFile.length() / (1024 * 1024))
        );
    }

    /**
     * Handles the color-quantization of the uploaded image (when the quantize button is clicked), using the selected
     * algorithm and parameters.
     */
    @FXML
    private void quantizeImage() {
        if (originalImageView.getImage() == null) {
            showAlert("Please upload an image.");
            return;
        }
        // Start timing the quantization process
        long startTime = System.nanoTime();

        // Reset all variable fields and UI fields required for quantization
        resetCommonComponents();

        Distance distance = new EuclideanDistance();
        Decay decay = new LinearDecay();
        String algorithm = algorithmComboBox.getValue();
        // Extract colors from the image
        ColorExtractionResult result = extractColors(originalImage);
        double[][] inputColors = result.getColors();
        int uniqueColorCount = result.getUniqueColorCount();
        countColorsOriginalImageLabel.setText("Count of Unique Colors in the Original Image: " + uniqueColorCount);
        int epochs = Integer.parseInt(epochsField.getText());

        if (algorithm.equals("SOM")) {
            int mapWidth = Integer.parseInt(mapWidthField.getText());
            int mapHeight = Integer.parseInt(mapHeightField.getText());

            if (uniqueColorCount <= mapWidth * mapHeight) {
                showAlert("The image has fewer unique colors than the number specified for quantization. Please choose a lower color count or a different image.");
                return;
            }

            fullQuantizedImage = performSOMQuantization(
                    originalImage,
                    inputColors,
                    mapWidth,
                    mapHeight,
                    epochs,
                    distance,
                    decay
            );

        } else if (algorithm.equals("Mini Batch K-means")) {
            int k = Integer.parseInt(kField.getText());

            if (uniqueColorCount <= k) {
                showAlert("The image has fewer unique colors than the number specified for quantization. Please choose a lower color count or a different image.");
                return;
            }
            fullQuantizedImage = performKMeansQuantization(originalImage, inputColors, k, epochs, distance, decay);
        }
        quantizedImageView.setDisable(false);
        displayQuantizedImage(fullQuantizedImage);
        // Save the original quantized image
        originalQuantizedImage = copyBufferedImage(fullQuantizedImage);
        long endTime = System.nanoTime();
        // Time in seconds
        long duration = (endTime - startTime) / 1_000_000_000;
        timeLabel.setText("Execution time: " + duration + " seconds");
        quantLabel.setVisible(true);
    }

    private void resetCommonComponents() {
        // Reset Labels
        convertedSizeLabel.setText("");
        meanSquaredErrorLabel.setText("");
        timeLabel.setText("");
        countColorsOriginalImageLabel.setText("");
        countColorsQuantizedImageLabel.setText("");
        quantLabel.setVisible(false);

        //Reset Image views
        quantizedImageView.setImage(null);
        quantizedImageView.setDisable(true);

        // Reset colors pane
        learnedColorsPane.getChildren().clear();

        // Reset the color picker container and restColors button visibility
        colorPickerBox.setVisible(false);
        resetColorButton.setVisible(false);

        // Clear maps and selections
        pixelMap = new HashMap<>();
        colorSquares = new HashMap<>();
        originalUnits = new ArrayList<>();
        selectedUnit = null;
        quantizer = null;

        // Reset Images
        fullQuantizedImage = null;
        originalQuantizedImage = null;
    }

    /**
     * Saves the color-quantized image to a file with a specific format (depending on what the user has selected).
     */
    @FXML
    private void saveQuantizedImage() {
        if (quantizedImageView.getImage() == null) {
            showAlert("No quantized image to save. Please generate an image first.");
            return;
        }

        FileChooser fileChooser = createFileChooser(
                "Save Quantized Image",
                new FileChooser.ExtensionFilter("8-bit PNG File", "*.png"),
                new FileChooser.ExtensionFilter("Standard PNG File", "*.png"),
                new FileChooser.ExtensionFilter("JPEG File", "*.jpg")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            String fileFormat = getImageFormat(file);
            if (fileFormat == null) {
                showAlert("Invalid image format.");
                return;
            }
            try {
                String description = fileChooser.getSelectedExtensionFilter().getDescription();
                switch (description) {
                    case "8-bit PNG File" -> saveAs8BitPng(fullQuantizedImage, pixelMap, file, quantizer);
                    case "Standard PNG File" -> ImageIO.write(fullQuantizedImage, "png", file);
                    case "JPEG File" -> ImageIO.write(fullQuantizedImage, "jpg", file);
                    default -> showAlert("Unsupported file format.");
                }
                showAlert("Image saved successfully as " + file.getName() + " (" + fileFormat.toUpperCase() + ")");
            } catch (IOException e) {
                showAlert("Error saving the image: " + e.getMessage());
            }
        }
    }

    /**
     * Clears the UI elements and resets needed variables.
     */
    @FXML
    private void clear() {
        // Reset Original Image related components
        originalImageView.setImage(null);
        originalSizeLabel.setText("");
        originalLabel.setVisible(false);
        originalImage = null;
        originalFormat = null;

        // Reset other UI components
        resetCommonComponents();
    }

    /**
     * Resets the quantizedImageView and restores the original color-quantized image.
     */
    @FXML
    private void resetQuantizedImage() {
        // Reset the quantized image view and display the original quantized image again
        fullQuantizedImage = copyBufferedImage(originalQuantizedImage);

        if (originalUnits != null) {
            // Reset the quantizer to its original units
            quantizer.setUnits(new ArrayList<>(originalUnits));
        }
        displayQuantizedImage(fullQuantizedImage);

        // Clear the learned colors palette and display it again
        learnedColorsPane.getChildren().clear();
        displayLearnedColors();

        // Reset the color squares map
        colorSquares.clear();
        // Reinitialize color picker
        setUpColorPicker();
    }

    /**
     * Performs color-quantization on the input image and returns a new color-quantized image.
     *
     * @param image       a BufferedImage to color-quantize
     * @param inputColors the colors of the buffered image
     * @param epochs      the number of epochs to train
     * @param distance    the distance metric
     * @return a color-quantized version of the image
     */
    private BufferedImage performQuantization(BufferedImage image, double[][] inputColors, int epochs, Distance distance) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Initialize and train the quantizer (either a Mini Batch K-means or a SOM)
        quantizer.train(inputColors, epochs);

        // Store original units
        originalUnits = new ArrayList<>(quantizer.getUnitsDeepCopy());

        // Cache to store previously processed colors and their closest units
        Map<Integer, Object> cache = new HashMap<>();

        BufferedImage quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int index = 0;
        double totalSquaredError = 0.0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] normalizedColor = inputColors[index];

                Color currentColor = getColorFromUnitCoordinates(normalizedColor);
                int colorKey = currentColor.getRGB();

                // Check the cache
                Object closestUnit;
                // If the cache contains the color key, fetch from the cache and avoid finding the closest unit
                if (cache.containsKey(colorKey)) {
                    closestUnit = cache.get(colorKey);
                } else {
                    // Find the nearest quantizer unit and store it in the cache
                    closestUnit = quantizer.findClosestUnit(normalizedColor);
                    cache.put(colorKey, closestUnit);
                }

                // Convert unit coordinates back to RGB
                double[] coordinates = quantizer.getUnitCoordinates(closestUnit);
                Color quantizedColor = getColorFromUnitCoordinates(coordinates);

                // Calculate and accumulate current error
                double error = distance.compute(normalizedColor, coordinates);
                totalSquaredError += Math.pow(error, 2);

                // Set the pixel value in the color-quantized image
                quantizedImage.setRGB(x, y, quantizedColor.getRGB());
                // Add the pixel to the centroid's list in the map
                pixelMap.computeIfAbsent(closestUnit, n -> new ArrayList<>()).add(new Point(x, y));
                index++;
            }
        }
        // Compute the MSQE
        double meanSquaredQuantizationError = totalSquaredError / (width * height) * Math.pow(255, 2);
        meanSquaredErrorLabel.setText(String.format("MSQE: %.4f", meanSquaredQuantizationError));
        countColorsQuantizedImageLabel.setText("Count of Colors in the Quantized Image: " + pixelMap.size());
        displayLearnedColors();
        resetColorButton.setVisible(true);
        return quantizedImage;
    }

    /**
     * Displays the palette of colors that the quantizer was able to learn.
     */
    private void displayLearnedColors() {
        for (Map.Entry<Object, List<Point>> entry : pixelMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                // Skip SOMNodes/Clusters that are not associated to any pixels
                continue;
            }
            Object unit = entry.getKey();
            double[] unitCoordinates = quantizer.getUnitCoordinates(unit);
            Color color = getColorFromUnitCoordinates(unitCoordinates);

            // Display each color as a square
            javafx.scene.shape.Rectangle colorSquare = new javafx.scene.shape.Rectangle(20, 20);
            colorSquare.setFill(toJavaFxColor(color));
            Tooltip colorTooltip = new Tooltip(String.format("RGB(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue()));
            Tooltip.install(colorSquare, colorTooltip);
            colorSquare.setOnMouseEntered(event -> colorTooltip.show(colorSquare, event.getScreenX() + 10, event.getScreenY() + 10));
            colorSquare.setOnMouseExited(event -> colorTooltip.hide());
            colorSquare.setOnMouseClicked(event -> {
                // Store selected unit and update color picker
                selectedUnit = unit;
                colorPicker.setValue((javafx.scene.paint.Color) colorSquare.getFill());
                colorPickerBox.setVisible(true);
                colorSquares.put(unit, colorSquare);
            });
            learnedColorsPane.getChildren().add(colorSquare);
        }
    }

    /**
     * Quantizes the colors in the input image using the Self-Organizing Map (SOM) algorithm.
     *
     * @param image       the input image
     * @param inputColors the RGB colors in the inout image
     * @param mapWidth    the width of the map
     * @param mapHeight   the height of the map
     * @param epochs      the amount of epochs for training
     * @param distance    the distance metric
     * @return the color-quantized image
     */
    private BufferedImage performSOMQuantization(
            BufferedImage image,
            double[][] inputColors,
            int mapWidth,
            int mapHeight,
            int epochs,
            Distance distance,
            Decay decay
    ) {
        quantizer = new SOM(3, mapWidth, mapHeight, distance, decay);
        return performQuantization(image, inputColors, epochs, distance);
    }

    /**
     * Quantizes the colors in the input image using the Mini Batch K-means algorithm.
     *
     * @param image       the input image
     * @param inputColors the RGB colors in the inout image
     * @param k           the amount of clusters
     * @param epochs      the amount of epochs for training
     * @param distance    the distance metric
     * @return the color-quantized image
     */
    private BufferedImage performKMeansQuantization(
            BufferedImage image,
            double[][] inputColors,
            int k,
            int epochs,
            Distance distance,
            Decay decay
    ) {
        quantizer = new MiniBatchKMeans(k, distance, decay);
        return performQuantization(image, inputColors, epochs, distance);
    }

    /**
     * Displays the color-quantized image in the quantizedImageView and calculate its size in memory.
     *
     * @param quantizedImage the quantized image to display
     */
    private void displayQuantizedImage(BufferedImage quantizedImage) {
        // Convert a BufferedImage to a JavaFX Image and set it to the quantizedImageView
        Image fxImage = resizeAndConvertToFxImage(quantizedImage, DISPLAY_IMAGE_MAX_WIDTH, DISPLAY_IMAGE_MAX_HEIGHT);
        quantizedImageView.setImage(fxImage);

        // Calculate converted image size in memory
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ImageIO.write(quantizedImage, originalFormat, b);
            b.flush();
            long convertedSize = b.size();
            b.close();
            convertedSizeLabel.setText(String.format("Converted Size (%s): %.4f MB", originalFormat, (double) convertedSize / (1024 * 1024)));
        } catch (IOException e) {
            showAlert("Error calculating converted image size: " + e.getMessage());
        }
    }

    /**
     * Initializes the functionality of the color picker.
     */
    private void setUpColorPicker() {
        colorPickerBox.setVisible(false);
        colorPicker.setOnAction(e -> {
            javafx.scene.paint.Color newColor = colorPicker.getValue();
            Color newAwtColor = new Color(
                    (int) (newColor.getRed() * 255),
                    (int) (newColor.getGreen() * 255),
                    (int) (newColor.getBlue() * 255)
            );
            List<Point> pixels = pixelMap.get(selectedUnit);
            if (pixels != null) {
                updateImageWithNewColor(fullQuantizedImage, pixels, newAwtColor);
                double[] newCoordinates = new double[]{
                        newAwtColor.getRed() / 255.0,
                        newAwtColor.getGreen() / 255.0,
                        newAwtColor.getBlue() / 255.0
                };
                Rectangle squareToUpdate = colorSquares.get(selectedUnit);
                // Update color square in the palette
                if (squareToUpdate != null) {
                    squareToUpdate.setFill(newColor);
                    quantizer.updateUnitCoordinates(selectedUnit, newCoordinates);
                }
                displayQuantizedImage(fullQuantizedImage);
            } else {
                showAlert("No pixels found for the selected unit.");
            }
            selectedUnit = null;

            // Hide ColorPicker after selection
            colorPickerBox.setVisible(false);
        });
    }
}
