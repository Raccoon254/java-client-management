package com.management.controller.service;

import com.management.model.Customer;
import com.management.model.ServiceRequest;
import com.management.service.CustomerService;
import com.management.service.ServiceRequestService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the service request form view (add/edit)
 */
public class ServiceRequestFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private ComboBox<Customer> customerComboBox;

    @FXML
    private TextField jobIdField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private DatePicker serviceDatePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private TextField refNoField;

    @FXML
    private TextField buildingNameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField stateField;

    @FXML
    private TextField zipField;

    @FXML
    private TextField pocNameField;

    @FXML
    private TextField pocPhoneField;

    @FXML
    private TextField participantNameField;

    @FXML
    private TextArea notesField;

    @FXML
    private TextField serviceCostField;

    @FXML
    private TextField addedCostField;

    @FXML
    private TextField parkingFeesField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label descriptionError;

    @FXML
    private Label customerError;

    @FXML
    private Label dateError;

    @FXML
    private Label timeError;

    @FXML
    private Label stateError;

    @FXML
    private Label zipError;

    @FXML
    private Label serviceCostError;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private ServiceRequestService serviceRequestService;
    private CustomerService customerService;
    private ServiceRequest serviceRequest;
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
        // Set up date picker with default value
        serviceDatePicker.setValue(LocalDate.now());

        // Set up customer combo box converter
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                if (customer == null) {
                    return null;
                }
                return customer.getCustomerNumber() + " - " + customer.getFirstName() + " " + customer.getLastName();
            }

            @Override
            public Customer fromString(String string) {
                // Not used for combo box
                return null;
            }
        });

        // Set up status options
        statusComboBox.getItems().addAll("Pending", "Scheduled", "In Progress", "Completed", "Cancelled");
        statusComboBox.setValue("Pending");

        // Set up text formatters
        ValidationUtils.setupZipCodeTextField(zipField);
        ValidationUtils.setupPhoneTextField(pocPhoneField);
        ValidationUtils.setupDoubleTextField(serviceCostField);
        ValidationUtils.setupDoubleTextField(addedCostField);
        ValidationUtils.setupDoubleTextField(parkingFeesField);

        // Set up validation listeners
        descriptionField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateTextArea(descriptionField, descriptionError,
                        text -> ValidationUtils.validateRequired("Description", text)));

        customerComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null;
            ValidationUtils.showValidationError(customerError, isValid ? "" : "Customer is required");
        });

        serviceDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null;
            ValidationUtils.showValidationError(dateError, isValid ? "" : "Service date is required");
        });

        // Time validation
        startTimeField.textProperty().addListener((obs, oldVal, newVal) ->
                validateTimes());

        endTimeField.textProperty().addListener((obs, oldVal, newVal) ->
                validateTimes());

        stateField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(stateField, stateError,
                        text -> text.length() > 2 ? "State should be 2 letters" : ""));

        zipField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(zipField, zipError,
                        text -> ValidationUtils.validateZipCode("Zip code", text, false)));

        serviceCostField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(serviceCostField, serviceCostError,
                        text -> ValidationUtils.validateDouble("Service cost", text, false)));

        // Set up button actions
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    /**
     * Validate start and end times
     */
    private void validateTimes() {
        String startTime = startTimeField.getText();
        String endTime = endTimeField.getText();

        if (startTime.isEmpty() && endTime.isEmpty()) {
            ValidationUtils.showValidationError(timeError, "");
            return;
        }

        try {
            if (!startTime.isEmpty() && !endTime.isEmpty()) {
                LocalTime start = LocalTime.parse(startTime);
                LocalTime end = LocalTime.parse(endTime);

                if (start.isAfter(end)) {
                    ValidationUtils.showValidationError(timeError, "Start time cannot be after end time");
                } else {
                    ValidationUtils.showValidationError(timeError, "");
                }
            } else {
                ValidationUtils.showValidationError(timeError, "");
            }
        } catch (Exception e) {
            ValidationUtils.showValidationError(timeError, "Invalid time format (use HH:MM)");
        }
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Set the customer service
     * @param customerService The customer service to use
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
        loadCustomers();
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
     * Set the customer for a new service request
     * @param customer The customer to set
     */
    public void setCustomer(Customer customer) {
        if (customer != null) {
            customerComboBox.setValue(customer);
            // Disable customer selection if a specific customer is set
            customerComboBox.setDisable(true);
        }
    }

    /**
     * Load a service request for editing
     * @param serviceRequest The service request to edit
     */
    public void loadServiceRequest(ServiceRequest serviceRequest) {
        this.serviceRequest = serviceRequest;

        // Populate form fields
        jobIdField.setText(String.valueOf(serviceRequest.getJobId()));
        jobIdField.setDisable(true);

        descriptionField.setText(serviceRequest.getDescription());
        serviceDatePicker.setValue(serviceRequest.getServiceDate());

        if (serviceRequest.getStartTime() != null) {
            startTimeField.setText(serviceRequest.getStartTime().toString());
        }

        if (serviceRequest.getEndTime() != null) {
            endTimeField.setText(serviceRequest.getEndTime().toString());
        }

        refNoField.setText(serviceRequest.getRefNo());
        buildingNameField.setText(serviceRequest.getBuildingName());
        addressField.setText(serviceRequest.getServiceAddress());
        cityField.setText(serviceRequest.getServiceCity());
        stateField.setText(serviceRequest.getServiceState());
        zipField.setText(serviceRequest.getServiceZip());
        pocNameField.setText(serviceRequest.getPocName());
        pocPhoneField.setText(serviceRequest.getPocPhone());
        participantNameField.setText(serviceRequest.getServiceParticipantName());
        notesField.setText(serviceRequest.getServiceNotes());

        serviceCostField.setText(String.valueOf(serviceRequest.getServiceCost()));
        addedCostField.setText(String.valueOf(serviceRequest.getAddedCost()));
        parkingFeesField.setText(String.valueOf(serviceRequest.getParkingFees()));

        statusComboBox.setValue(serviceRequest.getStatus());

        // Set customer
        if (serviceRequest.getCustomerId() > 0 && serviceRequest.getCustomer() != null) {
            // Find matching customer in the combo box
            for (Customer customer : customerComboBox.getItems()) {
                if (customer.getCustomerId() == serviceRequest.getCustomerId()) {
                    customerComboBox.setValue(customer);
                    break;
                }
            }
        }
    }

    /**
     * Load customers for the combo box
     */
    private void loadCustomers() {
        try {
            if (customerService != null) {
                List<Customer> customers = customerService.getAllCustomers();
                customerComboBox.getItems().clear();
                customerComboBox.getItems().addAll(customers);
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the form title based on the mode
     */
    private void updateFormTitle() {
        if (mode == Mode.ADD) {
            titleLabel.setText("Create New Service Request");
            saveButton.setText("Create Service Request");
        } else {
            titleLabel.setText("Edit Service Request");
            saveButton.setText("Save Changes");
        }
    }

    /**
     * Handle saving the service request
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
                // Create new service request
                ServiceRequest newServiceRequest = createServiceRequestFromForm();
                int jobId = serviceRequestService.createServiceRequest(newServiceRequest);

                if (jobId > 0) {
                    AlertUtils.showInformationAlert("Success", "Service request created successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to create service request.");
                }
            } else {
                // Update existing service request
                updateServiceRequestFromForm();
                boolean success = serviceRequestService.updateServiceRequest(serviceRequest);

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Service request updated successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to update service request.");
                }
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new service request from the form data
     * @return The new service request
     */
    private ServiceRequest createServiceRequestFromForm() {
        ServiceRequest newServiceRequest = new ServiceRequest();

        Customer selectedCustomer = customerComboBox.getValue();
        if (selectedCustomer != null) {
            newServiceRequest.setCustomerId(selectedCustomer.getCustomerId());
            newServiceRequest.setCustomer(selectedCustomer);
        }

        newServiceRequest.setDescription(descriptionField.getText());
        newServiceRequest.setServiceDate(serviceDatePicker.getValue());

        // Parse times if provided
        if (!startTimeField.getText().isEmpty()) {
            newServiceRequest.setStartTime(LocalTime.parse(startTimeField.getText()));
        }

        if (!endTimeField.getText().isEmpty()) {
            newServiceRequest.setEndTime(LocalTime.parse(endTimeField.getText()));
        }

        newServiceRequest.setRefNo(refNoField.getText());
        newServiceRequest.setBuildingName(buildingNameField.getText());
        newServiceRequest.setServiceAddress(addressField.getText());
        newServiceRequest.setServiceCity(cityField.getText());
        newServiceRequest.setServiceState(stateField.getText());
        newServiceRequest.setServiceZip(zipField.getText());
        newServiceRequest.setPocName(pocNameField.getText());
        newServiceRequest.setPocPhone(pocPhoneField.getText());
        newServiceRequest.setServiceParticipantName(participantNameField.getText());
        newServiceRequest.setServiceNotes(notesField.getText());

        // Parse costs if provided
        if (!serviceCostField.getText().isEmpty()) {
            newServiceRequest.setServiceCost(Double.parseDouble(serviceCostField.getText()));
        }

        if (!addedCostField.getText().isEmpty()) {
            newServiceRequest.setAddedCost(Double.parseDouble(addedCostField.getText()));
        }

        if (!parkingFeesField.getText().isEmpty()) {
            newServiceRequest.setParkingFees(Double.parseDouble(parkingFeesField.getText()));
        }

        newServiceRequest.setStatus(statusComboBox.getValue());

        return newServiceRequest;
    }

    /**
     * Update the existing service request with form data
     */
    private void updateServiceRequestFromForm() {
        Customer selectedCustomer = customerComboBox.getValue();
        if (selectedCustomer != null) {
            serviceRequest.setCustomerId(selectedCustomer.getCustomerId());
            serviceRequest.setCustomer(selectedCustomer);
        }

        serviceRequest.setDescription(descriptionField.getText());
        serviceRequest.setServiceDate(serviceDatePicker.getValue());

        // Parse times if provided
        if (!startTimeField.getText().isEmpty()) {
            serviceRequest.setStartTime(LocalTime.parse(startTimeField.getText()));
        } else {
            serviceRequest.setStartTime(null);
        }

        if (!endTimeField.getText().isEmpty()) {
            serviceRequest.setEndTime(LocalTime.parse(endTimeField.getText()));
        } else {
            serviceRequest.setEndTime(null);
        }

        serviceRequest.setRefNo(refNoField.getText());
        serviceRequest.setBuildingName(buildingNameField.getText());
        serviceRequest.setServiceAddress(addressField.getText());
        serviceRequest.setServiceCity(cityField.getText());
        serviceRequest.setServiceState(stateField.getText());
        serviceRequest.setServiceZip(zipField.getText());
        serviceRequest.setPocName(pocNameField.getText());
        serviceRequest.setPocPhone(pocPhoneField.getText());
        serviceRequest.setServiceParticipantName(participantNameField.getText());
        serviceRequest.setServiceNotes(notesField.getText());

        // Parse costs if provided
        if (!serviceCostField.getText().isEmpty()) {
            serviceRequest.setServiceCost(Double.parseDouble(serviceCostField.getText()));
        } else {
            serviceRequest.setServiceCost(0.0);
        }

        if (!addedCostField.getText().isEmpty()) {
            serviceRequest.setAddedCost(Double.parseDouble(addedCostField.getText()));
        } else {
            serviceRequest.setAddedCost(0.0);
        }

        if (!parkingFeesField.getText().isEmpty()) {
            serviceRequest.setParkingFees(Double.parseDouble(parkingFeesField.getText()));
        } else {
            serviceRequest.setParkingFees(0.0);
        }

        serviceRequest.setStatus(statusComboBox.getValue());
    }

    /**
     * Validate the form
     * @return true if the form is valid
     */
    private boolean validateForm() {
        boolean isDescriptionValid = ValidationUtils.validateTextArea(
                descriptionField, descriptionError,
                text -> ValidationUtils.validateRequired("Description", text));

        boolean isCustomerValid = customerComboBox.getValue() != null;
        ValidationUtils.showValidationError(customerError, isCustomerValid ? "" : "Customer is required");

        boolean isDateValid = serviceDatePicker.getValue() != null;
        ValidationUtils.showValidationError(dateError, isDateValid ? "" : "Service date is required");

        // Validate times if both are provided
        boolean isTimeValid = true;
        if (!startTimeField.getText().isEmpty() && !endTimeField.getText().isEmpty()) {
            try {
                LocalTime start = LocalTime.parse(startTimeField.getText());
                LocalTime end = LocalTime.parse(endTimeField.getText());

                if (start.isAfter(end)) {
                    ValidationUtils.showValidationError(timeError, "Start time cannot be after end time");
                    isTimeValid = false;
                } else {
                    ValidationUtils.showValidationError(timeError, "");
                }
            } catch (Exception e) {
                ValidationUtils.showValidationError(timeError, "Invalid time format (use HH:MM)");
                isTimeValid = false;
            }
        }

        boolean isStateValid = ValidationUtils.validateField(
                stateField, stateError,
                text -> text.isEmpty() || text.length() > 2 ? "State should be 2 letters" : "");

        boolean isZipValid = ValidationUtils.validateField(
                zipField, zipError,
                text -> ValidationUtils.validateZipCode("Zip code", text, false));

        boolean isServiceCostValid = ValidationUtils.validateField(
                serviceCostField, serviceCostError,
                text -> ValidationUtils.validateDouble("Service cost", text, false));

        return isDescriptionValid && isCustomerValid && isDateValid && isTimeValid &&
                isStateValid && isZipValid && isServiceCostValid;
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