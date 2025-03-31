package com.management.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Utility class for creating and displaying JavaFX alerts and dialogs
 */
public class AlertUtils {

    /**
     * Display an information alert
     * @param title The alert title
     * @param message The alert message
     */
    public static void showInformationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        setAlertIcon(alert);
        alert.showAndWait();
    }

    /**
     * Display a warning alert
     * @param title The alert title
     * @param message The alert message
     */
    public static void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        setAlertIcon(alert);
        alert.showAndWait();
    }

    /**
     * Display an error alert
     * @param title The alert title
     * @param message The alert message
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        setAlertIcon(alert);
        alert.showAndWait();
    }

    /**
     * Display a confirmation alert
     * @param title The alert title
     * @param header The alert header
     * @param message The alert message
     * @return true if OK was clicked, false otherwise
     */
    public static boolean showConfirmationAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        setAlertIcon(alert);
        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Display an exception details dialog
     * @param title The dialog title
     * @param header The dialog header
     * @param content The dialog content
     * @param ex The exception
     */
    public static void showExceptionDialog(String title, String header, String content, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable Exception
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        // Set expandable Exception into the dialog pane
        alert.getDialogPane().setExpandableContent(expContent);

        setAlertIcon(alert);
        alert.showAndWait();
    }

    /**
     * Display a custom input dialog
     * @param title The dialog title
     * @param header The dialog header
     * @param message The dialog message
     * @return Optional containing the entered value, or empty if canceled
     */
    public static Optional<String> showInputDialog(String title, String header, String message) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(message);

        setAlertIcon(dialog);
        return dialog.showAndWait();
    }

    /**
     * Set the application icon for an alert
     * @param alert The alert
     */
    private static void setAlertIcon(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        // Set your application icon here if available
        // stage.getIcons().add(new Image("/images/icon.png"));
    }

    /**
     * Set the application icon for a dialog
     * @param dialog The dialog
     */
    private static void setAlertIcon(javafx.scene.control.Dialog<?> dialog) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        // Set your application icon here if available
        // stage.getIcons().add(new Image("/images/icon.png"));
    }

    /**
     * Show a choice dialog with the given options
     * @param title The dialog title
     * @param header The dialog header
     * @param content The dialog content
     * @param choices The choices to display
     * @return The index of the selected choice, or -1 if cancelled
     */
    public static int showChoiceDialog(String title, String header, String content, Object[] choices) {
        ChoiceDialog<Object> dialog = new ChoiceDialog<>(choices[0], choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        Optional<Object> result = dialog.showAndWait();
        if (result.isPresent()) {
            for (int i = 0; i < choices.length; i++) {
                if (result.get().equals(choices[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
}