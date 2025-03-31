package com.management.util;

import javafx.scene.control.*;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Utility class for form validation
 */
public class ValidationUtils {

    // Common validation patterns
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$");

    private static final Pattern ZIP_CODE_PATTERN =
            Pattern.compile("^\\d{5}(-\\d{4})?$");

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("\\d*");

    private static final Pattern DECIMAL_PATTERN =
            Pattern.compile("\\d*\\.?\\d*");

    /**
     * Check if a string is null or empty
     * @param str The string to check
     * @return true if the string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validate an email address
     * @param email The email address to validate
     * @return true if the email is valid
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate a phone number
     * @param phone The phone number to validate
     * @return true if the phone number is valid
     */
    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }

        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate a zip code
     * @param zipCode The zip code to validate
     * @return true if the zip code is valid
     */
    public static boolean isValidZipCode(String zipCode) {
        if (isNullOrEmpty(zipCode)) {
            return false;
        }

        return ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    /**
     * Validate a decimal number
     * @param number The number to validate
     * @return true if the number is a valid decimal
     */
    public static boolean isValidDecimal(String number) {
        if (isNullOrEmpty(number)) {
            return false;
        }

        return DECIMAL_PATTERN.matcher(number).matches();
    }

    /**
     * Validate required fields in a form
     * @param fieldName The name of the field (for error message)
     * @param value The field value
     * @return Empty string if valid, error message if invalid
     */
    public static String validateRequired(String fieldName, String value) {
        return isNullOrEmpty(value) ? fieldName + " is required" : "";
    }

    /**
     * Validate an email field
     * @param fieldName The name of the field (for error message)
     * @param email The email to validate
     * @return Empty string if valid, error message if invalid
     */
    public static String validateEmail(String fieldName, String email) {
        if (isNullOrEmpty(email)) {
            return fieldName + " is required";
        }

        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }

        return "";
    }

    /**
     * Validate a phone field
     * @param fieldName The name of the field (for error message)
     * @param phone The phone to validate
     * @param required Whether the field is required
     * @return Empty string if valid, error message if invalid
     */
    public static String validatePhone(String fieldName, String phone, boolean required) {
        if (isNullOrEmpty(phone)) {
            return required ? fieldName + " is required" : "";
        }

        if (!isValidPhone(phone)) {
            return "Please enter a valid phone number (XXX-XXX-XXXX)";
        }

        return "";
    }

    /**
     * Validate a zip code field
     * @param fieldName The name of the field (for error message)
     * @param zipCode The zip code to validate
     * @param required Whether the field is required
     * @return Empty string if valid, error message if invalid
     */
    public static String validateZipCode(String fieldName, String zipCode, boolean required) {
        if (isNullOrEmpty(zipCode)) {
            return required ? fieldName + " is required" : "";
        }

        if (!isValidZipCode(zipCode)) {
            return "Please enter a valid zip code (XXXXX or XXXXX-XXXX)";
        }

        return "";
    }

    /**
     * Validate a decimal field
     * @param fieldName The name of the field (for error message)
     * @param value The value to validate
     * @param required Whether the field is required
     * @return Empty string if valid, error message if invalid
     */
    public static String validateDecimal(String fieldName, String value, boolean required) {
        if (isNullOrEmpty(value)) {
            return required ? fieldName + " is required" : "";
        }

        if (!isValidDecimal(value)) {
            return "Please enter a valid number";
        }

        return "";
    }

    /**
     * Set up a text field to accept only numbers
     * @param textField The text field to set up
     */
    public static void setupNumericTextField(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (NUMBER_PATTERN.matcher(newText).matches()) {
                return change;
            }
            return null;
        };

        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Set up a text field to accept only decimal numbers
     * @param textField The text field to set up
     */
    public static void setupDecimalTextField(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (DECIMAL_PATTERN.matcher(newText).matches()) {
                return change;
            }
            return null;
        };

        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Set up a text field to format phone numbers
     * @param textField The text field to set up
     */
    public static void setupPhoneTextField(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            // Remove all non-digit characters
            String digitsOnly = newValue.replaceAll("\\D", "");

            // Format the phone number
            String formattedPhone = formatPhoneNumber(digitsOnly);

            // Only update if the formatted value is different to avoid cursor jumps
            if (!newValue.equals(formattedPhone)) {
                textField.setText(formattedPhone);
            }
        });
    }

    /**
     * Format a phone number string
     * @param digitsOnly The phone number digits
     * @return The formatted phone number
     */
    private static String formatPhoneNumber(String digitsOnly) {
        if (digitsOnly.length() <= 3) {
            return digitsOnly;
        } else if (digitsOnly.length() <= 6) {
            return String.format("(%s) %s",
                    digitsOnly.substring(0, 3),
                    digitsOnly.substring(3));
        } else {
            return String.format("(%s) %s-%s",
                    digitsOnly.substring(0, 3),
                    digitsOnly.substring(3, 6),
                    digitsOnly.substring(6, Math.min(10, digitsOnly.length())));
        }
    }

    /**
     * Set up a text field to format zip codes
     * @param textField The text field to set up
     */
    public static void setupZipCodeTextField(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            // Remove all non-digit characters
            String digitsOnly = newValue.replaceAll("\\D", "");

            // Format the zip code
            String formattedZip = formatZipCode(digitsOnly);

            // Only update if the formatted value is different to avoid cursor jumps
            if (!newValue.equals(formattedZip)) {
                textField.setText(formattedZip);
            }
        });
    }

    /**
     * Format a zip code string
     * @param digitsOnly The zip code digits
     * @return The formatted zip code
     */
    private static String formatZipCode(String digitsOnly) {
        if (digitsOnly.length() <= 5) {
            return digitsOnly;
        } else {
            return String.format("%s-%s",
                    digitsOnly.substring(0, 5),
                    digitsOnly.substring(5, Math.min(9, digitsOnly.length())));
        }
    }

    /**
     * Validate a form field and show/hide error message
     * @param textField The text field to validate
     * @param errorLabel The label to show error message
     * @param validator The validation function
     * @return true if valid
     */
    public static boolean validateField(TextField textField, javafx.scene.control.Label errorLabel,
                                        java.util.function.Function<String, String> validator) {
        String error = validator.apply(textField.getText());
        errorLabel.setText(error);
        errorLabel.setVisible(!error.isEmpty());

        // Add/remove error style class
        if (error.isEmpty()) {
            textField.getStyleClass().remove("error");
        } else if (!textField.getStyleClass().contains("error")) {
            textField.getStyleClass().add("error");
        }

        return error.isEmpty();
    }

    /**
     * Validate a date picker and show/hide error message
     * @param datePicker The date picker to validate
     * @param errorLabel The label to show error message
     * @param fieldName The field name for error message
     * @param required Whether the field is required
     * @return true if valid
     */
    public static boolean validateDatePicker(DatePicker datePicker, javafx.scene.control.Label errorLabel,
                                             String fieldName, boolean required) {
        String error = "";

        if (required && datePicker.getValue() == null) {
            error = fieldName + " is required";
        }

        errorLabel.setText(error);
        errorLabel.setVisible(!error.isEmpty());

        // Add/remove error style class
        if (error.isEmpty()) {
            datePicker.getStyleClass().remove("error");
        } else if (!datePicker.getStyleClass().contains("error")) {
            datePicker.getStyleClass().add("error");
        }

        return error.isEmpty();
    }

    /**
     * Validate a combo box and show/hide error message
     * @param comboBox The combo box to validate
     * @param errorLabel The label to show error message
     * @param fieldName The field name for error message
     * @return true if valid
     */
    public static boolean validateComboBox(ComboBox<?> comboBox, javafx.scene.control.Label errorLabel,
                                           String fieldName) {
        String error = "";

        if (comboBox.getValue() == null) {
            error = fieldName + " is required";
        }

        errorLabel.setText(error);
        errorLabel.setVisible(!error.isEmpty());

        // Add/remove error style class
        if (error.isEmpty()) {
            comboBox.getStyleClass().remove("error");
        } else if (!comboBox.getStyleClass().contains("error")) {
            comboBox.getStyleClass().add("error");
        }

        return error.isEmpty();
    }

    /**
     * Validate a text area and show/hide error message
     * @param textArea The text area to validate
     * @param errorLabel The label to show error message
     * @param fieldName The field name for error message
     * @param required Whether the field is required
     * @return true if valid
     */
    public static boolean validateTextArea(TextArea textArea, javafx.scene.control.Label errorLabel,
                                           String fieldName, boolean required) {
        String error = "";

        if (required && isNullOrEmpty(textArea.getText())) {
            error = fieldName + " is required";
        }

        errorLabel.setText(error);
        errorLabel.setVisible(!error.isEmpty());

        // Add/remove error style class
        if (error.isEmpty()) {
            textArea.getStyleClass().remove("error");
        } else if (!textArea.getStyleClass().contains("error")) {
            textArea.getStyleClass().add("error");
        }

        return error.isEmpty();
    }

    public static boolean validateTextArea(TextArea textArea, javafx.scene.control.Label errorLabel,
                                           java.util.function.Function<String, String> validator) {
        String error = validator.apply(textArea.getText());
        errorLabel.setText(error);
        errorLabel.setVisible(!error.isEmpty());

        // Add/remove error style class
        if (error.isEmpty()) {
            textArea.getStyleClass().remove("error");
        } else if (!textArea.getStyleClass().contains("error")) {
            textArea.getStyleClass().add("error");
        }

        return error.isEmpty();
    }

    public static void showValidationError(Label customerError, String s) {
        customerError.setText(s);
        customerError.setVisible(true);
    }

    /**
     * Set up a text field to accept only decimal numbers
     * @param textField The text field to set up
     */
    public static void setupDoubleTextField(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            // Allow empty string, single decimal point, or proper decimal number
            if (newText.isEmpty() || newText.equals(".") || newText.matches("^\\d*\\.?\\d*$")) {
                return change;
            }
            return null;
        };

        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Validate a double value
     * @param fieldName The name of the field (for error message)
     * @param text The value to validate
     * @param required Whether the field is required
     * @return Empty string if valid, error message if invalid
     */
    public static String validateDouble(String fieldName, String text, boolean required) {
        if (isNullOrEmpty(text)) {
            return required ? fieldName + " is required" : "";
        }

        // Special case for just a decimal point with no digits
        if (text.equals(".")) {
            return "Please enter a valid number";
        }

        try {
            Double.parseDouble(text);
            return "";
        } catch (NumberFormatException e) {
            return "Please enter a valid number";
        }
    }
}