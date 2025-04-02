package com.management.controller.customer;

import com.management.model.Customer;
import com.management.service.CustomerService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

/**
 * Controller for the step-based customer form view (add/edit)
 */
public class CustomerFormController {

    @FXML private Label titleLabel;

    // Navigation buttons
    @FXML private Button basicInfoButton;
    @FXML private Button businessButton;
    @FXML private Button addressButton;
    @FXML private Button additionalButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Content sections
    @FXML private StackPane contentStack;
    @FXML private VBox basicInfoSection;
    @FXML private HBox idContainer;
    @FXML private VBox businessSection;
    @FXML private VBox addressSection;
    @FXML private VBox additionalSection;

    // Form fields - Basic Info
    @FXML private TextField customerNumberField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    // Form fields - Business
    @FXML private TextField companyField;
    @FXML private TextField positionField;
    @FXML private TextField businessNameField;
    @FXML private TextField mobileField;

    // Form fields - Address
    @FXML private TextField streetAddressField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipCodeField;
    @FXML private TextField countryField;

    // Form fields - Additional Info
    @FXML private TextField websiteField;
    @FXML private TextField referredByField;
    @FXML private TextField tagsField;

    // Error labels
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label stateError;
    @FXML private Label zipError;

    private CustomerService customerService;
    private Customer customer;
    private Mode mode = Mode.ADD;

    // Step management
    private int currentStep = 0;
    private final int TOTAL_STEPS = 4;

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
        idContainer.setManaged(false);
        idContainer.setVisible(false);

        // Set up text formatters and validators
        setupValidators();

        // Set up step-based navigation
        updateButtonStyles();
        updateNavigationButtons();

        // Set up actions for navigation buttons
        setupNavigationButtonActions();
    }

    /**
     * Set up validators for form fields
     */
    private void setupValidators() {
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
    }

    /**
     * Set up action handlers for navigation buttons
     */
    private void setupNavigationButtonActions() {
        prevButton.setOnAction(e -> handlePrevious());
        nextButton.setOnAction(e -> handleNext());
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());

        // Set navigation button actions
        basicInfoButton.setOnAction(e -> navigateToStep(0));
        businessButton.setOnAction(e -> navigateToStep(1));
        addressButton.setOnAction(e -> navigateToStep(2));
        additionalButton.setOnAction(e -> navigateToStep(3));
    }

    /**
     * Show a specific step in the form
     */
    @FXML
    public void showBasicInfo() {
        navigateToStep(0);
    }

    @FXML
    public void showBusiness() {
        if (validateCurrentStep()) {
            navigateToStep(1);
        }
    }

    @FXML
    public void showAddress() {
        if (validateCurrentStep() && (currentStep == 0 ? validateBasicInfo() : true)) {
            navigateToStep(2);
        }
    }

    @FXML
    public void showAdditional() {
        if (validateCurrentStep() &&
                (currentStep == 0 ? validateBasicInfo() : true) &&
                (currentStep == 2 ? validateAddressInfo() : true)) {
            navigateToStep(3);
        }
    }

    /**
     * Navigate to a specific step
     * @param stepIndex The index of the step to navigate to
     */
    private void navigateToStep(int stepIndex) {
        // Ensure index is within bounds
        if (stepIndex < 0 || stepIndex >= TOTAL_STEPS) {
            return;
        }

        // If trying to navigate forward, validate current step first
        if (stepIndex > currentStep && !validateCurrentStep()) {
            return;
        }

        // Hide all sections
        basicInfoSection.setVisible(false);
        businessSection.setVisible(false);
        addressSection.setVisible(false);
        additionalSection.setVisible(false);

        // Show the selected section
        switch (stepIndex) {
            case 0:
                basicInfoSection.setVisible(true);
                break;
            case 1:
                businessSection.setVisible(true);
                break;
            case 2:
                addressSection.setVisible(true);
                break;
            case 3:
                additionalSection.setVisible(true);
                break;
        }

        // Update current step
        currentStep = stepIndex;

        // Update UI
        updateButtonStyles();
        updateNavigationButtons();
    }

    /**
     * Update the styles of step buttons to show the active step
     */
    private void updateButtonStyles() {
        // Remove active class from all buttons
        basicInfoButton.getStyleClass().remove("active-nav-button");
        businessButton.getStyleClass().remove("active-nav-button");
        addressButton.getStyleClass().remove("active-nav-button");
        additionalButton.getStyleClass().remove("active-nav-button");

        // Add active class to current step button
        switch (currentStep) {
            case 0:
                if (!basicInfoButton.getStyleClass().contains("active-nav-button")) {
                    basicInfoButton.getStyleClass().add("active-nav-button");
                }
                break;
            case 1:
                if (!businessButton.getStyleClass().contains("active-nav-button")) {
                    businessButton.getStyleClass().add("active-nav-button");
                }
                break;
            case 2:
                if (!addressButton.getStyleClass().contains("active-nav-button")) {
                    addressButton.getStyleClass().add("active-nav-button");
                }
                break;
            case 3:
                if (!additionalButton.getStyleClass().contains("active-nav-button")) {
                    additionalButton.getStyleClass().add("active-nav-button");
                }
                break;
        }
    }

    /**
     * Update the state of navigation buttons based on current step
     */
    private void updateNavigationButtons() {
        prevButton.setDisable(currentStep == 0);
        nextButton.setVisible(currentStep < TOTAL_STEPS - 1);
        saveButton.setVisible(currentStep == TOTAL_STEPS - 1);
    }

    /**
     * Handle the Previous button click
     */
    @FXML
    public void handlePrevious() {
        if (currentStep > 0) {
            navigateToStep(currentStep - 1);
        }
    }

    /**
     * Handle the Next button click
     */
    @FXML
    public void handleNext() {
        if (validateCurrentStep() && currentStep < TOTAL_STEPS - 1) {
            navigateToStep(currentStep + 1);
        }
    }

    /**
     * Validate the current step before proceeding
     * @return true if the step is valid
     */
    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 0:
                return validateBasicInfo();
            case 1:
                return true; // Business info has no required fields
            case 2:
                return validateAddressInfo();
            case 3:
                return true; // Additional info has no required fields
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

        // Basic Info
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhoneNumber());

        // Business Info
        mobileField.setText(customer.getMobileNumber());
        companyField.setText(customer.getCompanyName());
        positionField.setText(customer.getPosition());
        businessNameField.setText(customer.getBusinessName());

        // Address Info
        streetAddressField.setText(customer.getStreetAddress());
        cityField.setText(customer.getCity());
        stateField.setText(customer.getState());
        zipCodeField.setText(customer.getZipCode());

        // Additional Info
        websiteField.setText(customer.getWebsite());
        // Add handling for new fields if they're in the Customer model
    }

    /**
     * Update the form title based on the mode
     */
    private void updateFormTitle() {
        if (mode == Mode.ADD) {
            titleLabel.setText("New Customer");
            saveButton.setText("Create Customer");
        } else {
            titleLabel.setText("Edit Customer");
            saveButton.setText("Save Changes");
        }
    }

    /**
     * Handle saving the customer
     */
    @FXML
    public void handleSave() {
        // Validate all sections
        boolean isValid = validateAllSections();

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

        // Basic Info
        newCustomer.setFirstName(firstNameField.getText());
        newCustomer.setLastName(lastNameField.getText());
        newCustomer.setEmail(emailField.getText());
        newCustomer.setPhoneNumber(phoneField.getText());

        // Business Info
        newCustomer.setMobileNumber(mobileField.getText());
        newCustomer.setCompanyName(companyField.getText());
        newCustomer.setPosition(positionField.getText());
        newCustomer.setBusinessName(businessNameField.getText());

        // Address Info
        newCustomer.setStreetAddress(streetAddressField.getText());
        newCustomer.setState(stateField.getText());
        newCustomer.setZipCode(zipCodeField.getText());

        // Additional Info
        newCustomer.setWebsite(websiteField.getText());

        return newCustomer;
    }

    /**
     * Update the existing customer with form data
     */
    private void updateCustomerFromForm() {
        // Basic Info
        customer.setFirstName(firstNameField.getText());
        customer.setLastName(lastNameField.getText());
        customer.setEmail(emailField.getText());
        customer.setPhoneNumber(phoneField.getText());

        // Business Info
        customer.setMobileNumber(mobileField.getText());
        customer.setCompanyName(companyField.getText());
        customer.setPosition(positionField.getText());
        customer.setBusinessName(businessNameField.getText());

        // Address Info
        customer.setStreetAddress(streetAddressField.getText());
        customer.setState(stateField.getText());
        customer.setZipCode(zipCodeField.getText());

        // Additional Info
        customer.setWebsite(websiteField.getText());
    }

    /**
     * Validate all sections in the form
     * @return true if all sections are valid
     */
    private boolean validateAllSections() {
        boolean isBasicInfoValid = validateBasicInfo();
        boolean isAddressValid = validateAddressInfo();

        // If any section is invalid, navigate to that section
        if (!isBasicInfoValid) {
            navigateToStep(0);
            return false;
        }

        if (!isAddressValid) {
            navigateToStep(2);
            return false;
        }

        return true;
    }

    /**
     * Handle canceling the form
     */
    @FXML
    public void handleCancel() {
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