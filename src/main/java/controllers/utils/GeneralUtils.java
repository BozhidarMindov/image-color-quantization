package main.java.controllers.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;

import java.awt.*;
import java.util.function.UnaryOperator;

/**
 * Provides utility methods for general-purpose operations related to JavaFX.
 */
public class GeneralUtils {
    /**
     * Configures a text field to accept only integer values within a specified range.
     * The text field will go back to its default value and alert the user if the input in the field is out of bounds,
     * invalid or if focus is lost on the field.
     *
     * @param textField    the text field to be configured
     * @param defaultValue the default value of the text field
     * @param minValue     the minimum value of the text field
     * @param maxValue     the maximum value of the text field
     */
    public static void filterIntegerTextField(TextField textField, String defaultValue, int minValue, int maxValue) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            // Allow empty or digits only
            if (newText.isEmpty() || newText.matches("\\d*")) {
                return change;
            }
            // Reject non-digit changes
            return null;
        };

        // Formatter with filter, sets to default if the value in the filter is out of bounds after losing focus
        TextFormatter<Integer> textFormatter = new TextFormatter<>(new IntegerStringConverter(), Integer.parseInt(defaultValue), filter);
        textField.setTextFormatter(textFormatter);

        // Validate on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus lost
                try {
                    int value = Integer.parseInt(textField.getText());
                    // Reset to default if the input is out of bounds
                    if (value < minValue || value > maxValue) {
                        showAlert("Invalid input. The value must be between " + minValue + " and " + maxValue + ".");
                        textField.setText(defaultValue);
                    }
                } catch (NumberFormatException e) {
                    // Reset if invalid
                    showAlert("Invalid input. Please enter a valid integer.");
                    textField.setText(defaultValue);
                }
            }
        });
    }

    /**
     * Converts a java.awt.Color to a javafx.scene.paint.Color
     *
     * @param color the color in java.awt.Color format to be converted
     * @return a javafx.scene.paint.Color which represents the same color
     */
    public static javafx.scene.paint.Color toJavaFxColor(Color color) {
        return javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Displays information alert with a specified message (used for alerts or errors).
     *
     * @param message the message to be displayed
     */
    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }

    /**
     * Creates a FileChooser with a specific title, description and extensions.
     *
     * @param title   the title of the file chooser
     * @param filters the extension filters to be used
     * @return a file chooser with the specified parameters
     */
    public static FileChooser createFileChooser(String title, FileChooser.ExtensionFilter... filters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(filters);
        return fileChooser;
    }
}
