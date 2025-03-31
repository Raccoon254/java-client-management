package com.management.controller.customer;

import com.management.model.Customer;
import com.management.service.CustomerService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

/**
 * Controller for the customer form view (add/edit)
 */
public class CustomerFormController {

    @FXML private Label titleLabel;
    @FXML private TabPane formTabPane;

    // Form fields
    @FXML private TextField customerNumberField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField mobileField;
    @FXML private TextField companyField;
    @FXML private TextField positionField;
    @FXML private TextField businessNameField;
    @FXML private TextField streetAddressField;
    @FXML private TextField stateField;
    @FXML private TextField zipCodeField;
    @FXML private TextField websiteField;

    // Error labels
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label stateError;
    @FXML private Label zipError;

    // Navigation buttons
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private CustomerService customerService;
    private Customer customer;
    private Mode mode = Mode.ADD;

    /**
     * Mode enum for the form (add or edit)
     */
    public enum Mode {
        ADD, EDIT
    }

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up text formatters and validators
        ValidationUtils.setupPhoneTextField(phoneField);
        ValidationUtils.setupPhoneTextField(mobileField);
        ValidationUtils.setupZipCodeTextField(zipCodeField);

        // Setup state field (restrict to 2 characters)
        stateField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 2) {
                stateField.setText(oldVal);
            }
        });

        // Set up validation listeners
        firstNameField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(firstNameField, firstNameError,
                        text -> ValidationUtils.validateRequired("First name", text)));

        lastNameField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(lastNameField, lastNameError,
                        text -> ValidationUtils.validateRequired("Last name", text)));

        emailField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(emailField, emailError,
                        text -> ValidationUtils.validateEmail("Email", text)));

        phoneField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(phoneField, phoneError,
                        text -> ValidationUtils.validatePhone("Phone number", text, false)));

        stateField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(stateField, stateError,
                        text -> text.length() > 2 ? "State should be 2 letters" : ""));

        zipCodeField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(zipCodeField, zipError,
                        text -> ValidationUtils.validateZipCode("Zip code", text, false)));

        // Set up tab navigation buttons
        prevButton.setOnAction(e -> navigateToPreviousTab());
        nextButton.setOnAction(e -> navigateToNextTab());

        // Update button states based on current tab
        formTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateNavigationButtons();
        });

        // Initial button state
        updateNavigationButtons();

        // Set up action buttons
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    /**
     * Update navigation button states based on current tab
     */
    private void updateNavigationButtons() {
        int currentIndex = formTabPane.getSelectionModel().getSelectedIndex();
        int lastIndex = formTabPane.getTabs().size() - 1;

        prevButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == lastIndex);

        // Only show save button on the last tab
        saveButton.setVisible(currentIndex == lastIndex);
    }

    /**
     * Navigate to the previous tab
     */
    private void navigateToPreviousTab() {
        int currentIndex = formTabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            formTabPane.getSelectionModel().select(currentIndex - 1);
        }
    }

    /**
     * Navigate to the next tab
     */
    private void navigateToNextTab() {
        int currentIndex = formTabPane.getSelectionModel().getSelectedIndex();
        int tabCount = formTabPane.getTabs().size();

        // Validate the current tab before proceeding
        if (validateCurrentTab(currentIndex)) {
            if (currentIndex < tabCount - 1) {
                formTabPane.getSelectionModel().select(currentIndex + 1);
            }
        } else {
            AlertUtils.showWarningAlert("Validation Error", "Please correct the errors before proceeding.");
        }
    }

    /**
     * Validate the current tab
     * @param tabIndex The index of the tab to validate
     * @return true if the tab is valid
     */
    private boolean validateCurrentTab(int tabIndex) {
        switch (tabIndex) {
            case 0: // Basic Information
                return validateBasicInfo();
            case 1: // Business Details
                return true; // No required fields
            case 2: // Address
                return validateAddressInfo();
            default:
                return true;
        }
    }

    /**
     * Validate basic information fields
     * @return true if all required fields are valid
     */
    private boolean validateBasicInfo() {
        boolean isFirstNameValid = ValidationUtils.validateField(
                firstNameField, firstNameError, text -> ValidationUtils.validateRequired("First name", text));

        boolean isLastNameValid = ValidationUtils.validateField(
                lastNameField, lastNameError, text -> ValidationUtils.validateRequired("Last name", text));

        boolean isEmailValid = ValidationUtils.validateField(
                emailField, emailError, text -> ValidationUtils.validateEmail("Email", text));

        boolean isPhoneValid = ValidationUtils.validateField(
                phoneField, phoneError, text -> ValidationUtils.validatePhone("Phone number", text, false));

        return isFirstNameValid && isLastNameValid && isEmailValid && isPhoneValid;
    }

    /**
     * Validate address information fields
     * @return true if all required fields are valid
     */
    private boolean validateAddressInfo() {
        boolean isStateValid = ValidationUtils.validateField(
                stateField, stateError, text -> text.length() > 2 ? "State should be 2 letters" : "");

        boolean isZipValid = ValidationUtils.validateField(
                zipCodeField, zipError, text -> ValidationUtils.validateZipCode("Zip code", text, false));

        return isStateValid && isZipValid;
    }

    /**
     * Set the customer service
     * @param customerService The customer service to use
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Set the mode (add or edit)
     * @param mode The mode to set
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        updateFormTitle();
    }

    /**
     * Load a customer for editing
     * @param customer The customer to edit
     */
    public void loadCustomer(Customer customer) {
        this.customer = customer;

        // Populate the form fields
        customerNumberField.setText(customer.getCustomerNumber());
        customerNumberField.setDisable(true); // Customer number should not be editable

        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhoneNumber());
        mobileField.setText(customer.getMobileNumber());
        companyField.setText(customer.getCompanyName());
        positionField.setText(customer.getPosition());
        businessNameField.setText(customer.getBusinessName());
        streetAddressField.setText(customer.getStreetAddress());
        stateField.setText(customer.getState());
        zipCodeField.setText(customer.getZipCode());
        websiteField.setText(customer.getWebsite());
    }

    /**
     * Update the form title based on the mode
     */
    private void updateFormTitle() {
        if (mode == Mode.ADD) {
            titleLabel.setText("Add New Customer");
            saveButton.setText("Create Customer");
        } else {
            titleLabel.setText("Edit Customer");
            saveButton.setText("Save Changes");
        }
    }

    /**
     * Handle saving the customer
     */
    private void handleSave() {
        // Validate all tabs
        boolean isValid = validateAllTabs();

        if (!isValid) {
            AlertUtils.showWarningAlert("Validation Error", "Please correct the errors in the form.");
            return;
        }

        try {
            if (mode == Mode.ADD) {
                // Create new customer
                Customer newCustomer = createCustomerFromForm();

                // Generate the custom customer number
                String customerId = generateCustomerNumber(
                        zipCodeField.getText(),
                        phoneField.getText(),
                        LocalDate.now().toString()
                );
                newCustomer.setCustomerNumber(customerId);

                int id = customerService.createCustomer(newCustomer);

                if (id > 0) {
                    AlertUtils.showInformationAlert("Success", "Customer created successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to create customer.");
                }
            } else {
                // Update existing customer
                updateCustomerFromForm();
                boolean success = customerService.updateCustomer(customer);

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Customer updated successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to update customer.");
                }
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a custom customer number based on the specified format
     * @param zip ZIP code
     * @param phone Phone number
     * @param dateStr Date string
     * @return Formatted customer number
     */
    private String generateCustomerNumber(String zip, String phone, String dateStr) {
        try {
            // Extract last 3 digits of ZIP
            String zipLast3 = zip.length() >= 3 ? zip.substring(zip.length() - 3) : zip;
            zipLast3 = padLeft(zipLast3, '0', 3);

            // Extract last 2 digits of phone (digits only)
            String digitsOnly = phone.replaceAll("\\D", "");
            String phoneLast2 = digitsOnly.length() >= 2 ?
                    digitsOnly.substring(digitsOnly.length() - 2) :
                    padLeft(digitsOnly, '0', 2);

            // Parse date
            LocalDate date = LocalDate.parse(dateStr);

            // Last 2 digits of year
            String yearLast2 = String.valueOf(date.getYear()).substring(2);

            // Get day-of-year (1-366) as 3 digits
            int dayOfYear = date.get(ChronoField.DAY_OF_YEAR);
            String day3Digit = padLeft(String.valueOf(dayOfYear), '0', 3);

            return zipLast3 + phoneLast2 + day3Digit + yearLast2;
        } catch (Exception e) {
            // Fallback to a default format if there's any error
            return "CID" + System.currentTimeMillis() % 10000;
        }
    }

    /**
     * Pad a string with a character to a certain length
     * @param input Input string
     * @param padChar Character to pad with
     * @param length Target length
     * @return Padded string
     */
    private String padLeft(String input, char padChar, int length) {
        if (input == null) input = "";
        StringBuilder sb = new StringBuilder();
        for (int i = input.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(input);
        return sb.substring(Math.max(0, sb.length() - length));
    }

    /**
     * Create a new customer from the form data
     * @return The new customer
     */
    private Customer createCustomerFromForm() {
        Customer newCustomer = new Customer();

        newCustomer.setFirstName(firstNameField.getText());
        newCustomer.setLastName(lastNameField.getText());
        newCustomer.setEmail(emailField.getText());
        newCustomer.setPhoneNumber(phoneField.getText());
        newCustomer.setMobileNumber(mobileField.getText());
        newCustomer.setCompanyName(companyField.getText());
        newCustomer.setPosition(positionField.getText());
        newCustomer.setBusinessName(businessNameField.getText());
        newCustomer.setStreetAddress(streetAddressField.getText());
        newCustomer.setState(stateField.getText());
        newCustomer.setZipCode(zipCodeField.getText());
        newCustomer.setWebsite(websiteField.getText());

        return newCustomer;
    }

    /**
     * Update the existing customer with form data
     */
    private void updateCustomerFromForm() {
        customer.setFirstName(firstNameField.getText());
        customer.setLastName(lastNameField.getText());
        customer.setEmail(emailField.getText());
        customer.setPhoneNumber(phoneField.getText());
        customer.setMobileNumber(mobileField.getText());
        customer.setCompanyName(companyField.getText());
        customer.setPosition(positionField.getText());
        customer.setBusinessName(businessNameField.getText());
        customer.setStreetAddress(streetAddressField.getText());
        customer.setState(stateField.getText());
        customer.setZipCode(zipCodeField.getText());
        customer.setWebsite(websiteField.getText());
    }

    /**
     * Validate all tabs in the form
     * @return true if all tabs are valid
     */
    private boolean validateAllTabs() {
        boolean isBasicInfoValid = validateBasicInfo();
        boolean isAddressValid = validateAddressInfo();

        // If any tab is invalid, navigate to that tab
        if (!isBasicInfoValid) {
            formTabPane.getSelectionModel().select(0);
            return false;
        }

        if (!isAddressValid) {
            formTabPane.getSelectionModel().select(2);
            return false;
        }

        return true;
    }

    /**
     * Handle canceling the form
     */
    private void handleCancel() {
        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Cancel",
                "Are you sure you want to cancel?",
                "All changes will be lost."
        );

        if (confirmed) {
            closeForm();
        }
    }

    /**
     * Close the form
     */
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}