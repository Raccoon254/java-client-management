package com.management.controller.customer;

import com.management.model.Customer;
import com.management.service.CustomerService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for the customer form view (add/edit)
 */
public class CustomerFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField customerNumberField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField mobileField;

    @FXML
    private TextField companyField;

    @FXML
    private TextField positionField;

    @FXML
    private TextField businessNameField;

    @FXML
    private TextField streetAddressField;

    @FXML
    private TextField stateField;

    @FXML
    private TextField zipCodeField;

    @FXML
    private TextField websiteField;

    @FXML
    private Label firstNameError;

    @FXML
    private Label lastNameError;

    @FXML
    private Label emailError;

    @FXML
    private Label phoneError;

    @FXML
    private Label stateError;

    @FXML
    private Label zipError;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

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

        // Set up button actions
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
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
        // Validate all fields
        boolean isValid = validateForm();

        if (!isValid) {
            AlertUtils.showWarningAlert("Validation Error", "Please correct the errors in the form.");
            return;
        }

        try {
            if (mode == Mode.ADD) {
                // Create new customer
                Customer newCustomer = createCustomerFromForm();
                int customerId = customerService.createCustomer(newCustomer);

                if (customerId > 0) {
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
     * Create a new customer from the form data
     * @return The new customer
     */
    private Customer createCustomerFromForm() {
        Customer newCustomer = new Customer();

        // Only set customer number if it's provided, otherwise service will generate one
        if (customerNumberField.getText() != null && !customerNumberField.getText().isEmpty()) {
            newCustomer.setCustomerNumber(customerNumberField.getText());
        }

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
     * Validate the form
     * @return true if the form is valid
     */
    private boolean validateForm() {
        boolean isFirstNameValid = ValidationUtils.validateField(
                firstNameField, firstNameError, text -> ValidationUtils.validateRequired("First name", text));

        boolean isLastNameValid = ValidationUtils.validateField(
                lastNameField, lastNameError, text -> ValidationUtils.validateRequired("Last name", text));

        boolean isEmailValid = ValidationUtils.validateField(
                emailField, emailError, text -> ValidationUtils.validateEmail("Email", text));

        boolean isPhoneValid = ValidationUtils.validateField(
                phoneField, phoneError, text -> ValidationUtils.validatePhone("Phone number", text, false));

        boolean isStateValid = ValidationUtils.validateField(
                stateField, stateError, text -> text.length() > 2 ? "State should be 2 letters" : "");

        boolean isZipValid = ValidationUtils.validateField(
                zipCodeField, zipError, text -> ValidationUtils.validateZipCode("Zip code", text, false));

        return isFirstNameValid && isLastNameValid && isEmailValid &&
                isPhoneValid && isStateValid && isZipValid;
    }

    /**
     * Handle canceling the form
     */
    private void handleCancel() {
        closeForm();
    }

    /**
     * Close the form
     */
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}