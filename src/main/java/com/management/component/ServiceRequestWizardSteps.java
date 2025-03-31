package com.management.component;

import com.management.component.WizardFramework.WizardStep;
import com.management.model.Customer;
import com.management.model.ServiceRequest;
import com.management.model.Technician;
import com.management.service.CustomerService;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Steps for the service request wizard
 */
public class ServiceRequestWizardSteps {

    /**
     * Customer information step
     */
    public static class CustomerStep implements WizardStep {
        private final VBox content;
        private final Label titleLabel;
        private final AutoCompleteTextField<Customer> customerField;
        private final Button createCustomerButton;
        private final Label customerHistory;
        private final Label customerInfoLabel;
        private final CustomerService customerService;
        private final List<Customer> customers;
        private final ServiceRequest serviceRequest;
        private Label errorLabel;

        /**
         * Create a new customer step
         * @param customerService The customer service
         * @param serviceRequest The service request to populate
         */
        public CustomerStep(CustomerService customerService, ServiceRequest serviceRequest) {
            this.customerService = customerService;
            this.customers = customerService.getAllCustomers();
            this.serviceRequest = serviceRequest;

            content = new VBox(15);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("wizard-step");

            titleLabel = new Label("Step 1: Customer Information");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

            // Customer search field
            HBox customerBox = new HBox(10);
            customerBox.setAlignment(Pos.CENTER_LEFT);
            Label customerLabel = new Label("Select Customer:");
            customerLabel.setPrefWidth(120);
            customerField = new AutoCompleteTextField<>(customers, c ->
                    c.getCustomerNumber() + " - " + c.getFirstName() + " " + c.getLastName());
            customerField.setPrefWidth(350);

            createCustomerButton = new Button("New Customer");
            createCustomerButton.setOnAction(e -> createNewCustomer());

            customerBox.getChildren().addAll(customerLabel, customerField, createCustomerButton);

            // Error message
            errorLabel = new Label();
            errorLabel.getStyleClass().add("error-label");
            errorLabel.setVisible(false);

            // Customer history
            customerHistory = new Label();
            customerHistory.setWrapText(true);
            customerHistory.setPrefWidth(Region.USE_COMPUTED_SIZE);

            // Customer information
            customerInfoLabel = new Label();
            customerInfoLabel.setWrapText(true);

            // Set up customer field listener
            customerField.setOnItemSelected(event -> {
                Customer customer = (Customer) event.getItem();
                if (customer != null) {
                    updateCustomerInfo(customer);
                    serviceRequest.setCustomerId(customer.getCustomerId());
                    serviceRequest.setCustomer(customer);
                    errorLabel.setVisible(false);
                }
            });

            Separator separator = new Separator();

            content.getChildren().addAll(
                    titleLabel,
                    new Label("Please select a customer for this service request."),
                    customerBox,
                    errorLabel,
                    separator,
                    new Label("Customer Information:"),
                    customerInfoLabel,
                    new Label("Customer History:"),
                    customerHistory
            );
        }

        @Override
        public Node getContent() {
            return content;
        }

        @Override
        public boolean validate() {
            if (serviceRequest.getCustomer() == null || serviceRequest.getCustomerId() <= 0) {
                errorLabel.setText("Please select a customer");
                errorLabel.setVisible(true);
                return false;
            }
            return true;
        }

        @Override
        public void onEnter() {
            // Nothing to do here
        }

        @Override
        public void reset() {
            customerField.reset();
            customerInfoLabel.setText("");
            customerHistory.setText("");
            errorLabel.setVisible(false);
            serviceRequest.setCustomer(null);
            serviceRequest.setCustomerId(0);
        }

        @Override
        public String getTitle() {
            return "Customer Information";
        }

        /**
         * Update customer information display
         * @param customer The customer to display
         */
        private void updateCustomerInfo(Customer customer) {
            if (customer != null) {
                StringBuilder info = new StringBuilder();
                info.append("Name: ").append(customer.getFirstName()).append(" ").append(customer.getLastName()).append("\n");
                info.append("Customer #: ").append(customer.getCustomerNumber()).append("\n");

                if (customer.getCompanyName() != null && !customer.getCompanyName().isEmpty()) {
                    info.append("Company: ").append(customer.getCompanyName()).append("\n");
                }

                if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().isEmpty()) {
                    info.append("Phone: ").append(customer.getPhoneNumber()).append("\n");
                }

                if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                    info.append("Email: ").append(customer.getEmail()).append("\n");
                }

                if (customer.getStreetAddress() != null && !customer.getStreetAddress().isEmpty()) {
                    info.append("Address: ").append(customer.getStreetAddress()).append("\n");

                    if (customer.getState() != null || customer.getZipCode() != null) {
                        String cityStateZip = "";
                        if (customer.getCity() != null && !customer.getCity().isEmpty()) {
                            cityStateZip += customer.getCity();
                        }
                        if (customer.getState() != null && !customer.getState().isEmpty()) {
                            if (!cityStateZip.isEmpty()) cityStateZip += ", ";
                            cityStateZip += customer.getState();
                        }
                        if (customer.getZipCode() != null && !customer.getZipCode().isEmpty()) {
                            if (!cityStateZip.isEmpty()) cityStateZip += " ";
                            cityStateZip += customer.getZipCode();
                        }
                        info.append(cityStateZip).append("\n");
                    }
                }

                customerInfoLabel.setText(info.toString());

                // In a real app, you would fetch customer history
                customerHistory.setText("No previous service requests found.");
            } else {
                customerInfoLabel.setText("");
                customerHistory.setText("");
            }
        }

        /**
         * Open dialog to create a new customer
         */
        private void createNewCustomer() {
            // This would open a dialog to create a new customer
            // After creation, you would add the customer to the list and select it
            AlertUtils.showInformationAlert("Create Customer",
                    "This would open a dialog to create a new customer.");
        }
    }

    /**
     * Service details step
     */
    public static class ServiceDetailsStep implements WizardStep {
        private final VBox content;
        private final Label titleLabel;
        private final TextArea descriptionField;
        private final ComboBox<String> serviceTypeComboBox;
        private final ComboBox<String> priorityComboBox;
        private final ServiceRequest serviceRequest;
        private Label descriptionError;

        /**
         * Create a new service details step
         * @param serviceRequest The service request to populate
         */
        public ServiceDetailsStep(ServiceRequest serviceRequest) {
            this.serviceRequest = serviceRequest;

            content = new VBox(15);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("wizard-step");

            titleLabel = new Label("Step 2: Service Details");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

            // Service type
            HBox serviceTypeBox = new HBox(10);
            serviceTypeBox.setAlignment(Pos.CENTER_LEFT);
            Label serviceTypeLabel = new Label("Service Type:");
            serviceTypeLabel.setPrefWidth(120);
            serviceTypeComboBox = new ComboBox<>();
            serviceTypeComboBox.setPrefWidth(350);
            serviceTypeComboBox.getItems().addAll(
                    "Installation", "Maintenance", "Repair", "Inspection", "Consultation", "Other");
            serviceTypeBox.getChildren().addAll(serviceTypeLabel, serviceTypeComboBox);

            // Priority
            HBox priorityBox = new HBox(10);
            priorityBox.setAlignment(Pos.CENTER_LEFT);
            Label priorityLabel = new Label("Priority:");
            priorityLabel.setPrefWidth(120);
            priorityComboBox = new ComboBox<>();
            priorityComboBox.setPrefWidth(350);
            priorityComboBox.getItems().addAll("Low", "Medium", "High", "Critical");
            priorityBox.getChildren().addAll(priorityLabel, priorityComboBox);

            // Description
            VBox descriptionBox = new VBox(5);
            Label descriptionLabel = new Label("Description: *");
            descriptionField = new TextArea();
            descriptionField.setPrefHeight(150);
            descriptionField.setWrapText(true);

            descriptionError = new Label("Description is required");
            descriptionError.getStyleClass().add("error-label");
            descriptionError.setVisible(false);

            descriptionBox.getChildren().addAll(descriptionLabel, descriptionField, descriptionError);

            // Validation
            descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    descriptionError.setVisible(true);
                } else {
                    descriptionError.setVisible(false);
                }
            });

            content.getChildren().addAll(
                    titleLabel,
                    new Label("Please provide details about the service needed."),
                    serviceTypeBox,
                    priorityBox,
                    descriptionBox
            );
        }

        @Override
        public Node getContent() {
            return content;
        }

        @Override
        public boolean validate() {
            String description = descriptionField.getText();
            if (description == null || description.trim().isEmpty()) {
                descriptionError.setVisible(true);
                return false;
            }

            // Update service request
            serviceRequest.setDescription(description);

            // Save selected service type and priority in notes field
            StringBuilder notes = new StringBuilder();

            if (serviceTypeComboBox.getValue() != null) {
                notes.append("Service Type: ").append(serviceTypeComboBox.getValue()).append("\n");
            }

            if (priorityComboBox.getValue() != null) {
                notes.append("Priority: ").append(priorityComboBox.getValue()).append("\n");
            }

            // Preserve existing notes if any
            if (serviceRequest.getServiceNotes() != null && !serviceRequest.getServiceNotes().isEmpty()) {
                notes.append("\n").append(serviceRequest.getServiceNotes());
            }

            serviceRequest.setServiceNotes(notes.toString());

            return true;
        }

        @Override
        public void onEnter() {
            // Pre-fill fields if editing
            if (serviceRequest.getDescription() != null) {
                descriptionField.setText(serviceRequest.getDescription());
            }

            // Parse service type and priority from notes if available
            if (serviceRequest.getServiceNotes() != null) {
                String notes = serviceRequest.getServiceNotes();

                if (notes.contains("Service Type:")) {
                    for (String type : serviceTypeComboBox.getItems()) {
                        if (notes.contains("Service Type: " + type)) {
                            serviceTypeComboBox.setValue(type);
                            break;
                        }
                    }
                }

                if (notes.contains("Priority:")) {
                    for (String priority : priorityComboBox.getItems()) {
                        if (notes.contains("Priority: " + priority)) {
                            priorityComboBox.setValue(priority);
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void reset() {
            descriptionField.clear();
            serviceTypeComboBox.setValue(null);
            priorityComboBox.setValue(null);
            descriptionError.setVisible(false);
        }

        @Override
        public String getTitle() {
            return "Service Details";
        }
    }

    /**
     * Scheduling step
     */
    public static class SchedulingStep implements WizardStep {
        private final VBox content;
        private final Label titleLabel;
        private final DatePicker serviceDatePicker;
        private final TextField startTimeField;
        private final TextField endTimeField;
        private final ServiceRequest serviceRequest;
        private Label dateError;
        private Label timeError;

        /**
         * Create a new scheduling step
         * @param serviceRequest The service request to populate
         */
        public SchedulingStep(ServiceRequest serviceRequest) {
            this.serviceRequest = serviceRequest;

            content = new VBox(15);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("wizard-step");

            titleLabel = new Label("Step 3: Scheduling");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

            // Service date
            HBox dateBox = new HBox(10);
            dateBox.setAlignment(Pos.CENTER_LEFT);
            Label dateLabel = new Label("Service Date: *");
            dateLabel.setPrefWidth(120);
            serviceDatePicker = new DatePicker();
            serviceDatePicker.setValue(LocalDate.now()); // Default to today
            serviceDatePicker.setPrefWidth(200);
            dateBox.getChildren().addAll(dateLabel, serviceDatePicker);

            dateError = new Label("Service date is required");
            dateError.getStyleClass().add("error-label");
            dateError.setVisible(false);

            // Start time
            HBox startTimeBox = new HBox(10);
            startTimeBox.setAlignment(Pos.CENTER_LEFT);
            Label startTimeLabel = new Label("Start Time:");
            startTimeLabel.setPrefWidth(120);
            startTimeField = new TextField();
            startTimeField.setPromptText("HH:MM");
            startTimeField.setPrefWidth(200);
            startTimeBox.getChildren().addAll(startTimeLabel, startTimeField);

            // End time
            HBox endTimeBox = new HBox(10);
            endTimeBox.setAlignment(Pos.CENTER_LEFT);
            Label endTimeLabel = new Label("End Time:");
            endTimeLabel.setPrefWidth(120);
            endTimeField = new TextField();
            endTimeField.setPromptText("HH:MM");
            endTimeField.setPrefWidth(200);
            endTimeBox.getChildren().addAll(endTimeLabel, endTimeField);

            timeError = new Label("Invalid time format or start time after end time");
            timeError.getStyleClass().add("error-label");
            timeError.setVisible(false);

            // Validation
            serviceDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    dateError.setVisible(true);
                } else {
                    dateError.setVisible(false);
                }
            });

            // Time validation
            startTimeField.textProperty().addListener((obs, oldVal, newVal) -> validateTimes());
            endTimeField.textProperty().addListener((obs, oldVal, newVal) -> validateTimes());

            // Calendar view (placeholder)
            Label calendarLabel = new Label("Calendar View:");
            calendarLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

            // Placeholder for calendar
            Pane calendarPlaceholder = new Pane();
            calendarPlaceholder.setMinHeight(200);
            calendarPlaceholder.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd;");

            content.getChildren().addAll(
                    titleLabel,
                    new Label("Please select the service date and time."),
                    dateBox,
                    dateError,
                    startTimeBox,
                    endTimeBox,
                    timeError,
                    calendarLabel,
                    calendarPlaceholder
            );
        }

        private void validateTimes() {
            String startTimeText = startTimeField.getText();
            String endTimeText = endTimeField.getText();

            if (startTimeText.isEmpty() && endTimeText.isEmpty()) {
                timeError.setVisible(false);
                return;
            }

            try {
                if (!startTimeText.isEmpty() && !endTimeText.isEmpty()) {
                    LocalTime startTime = LocalTime.parse(startTimeText);
                    LocalTime endTime = LocalTime.parse(endTimeText);

                    if (startTime.isAfter(endTime)) {
                        timeError.setText("Start time cannot be after end time");
                        timeError.setVisible(true);
                    } else {
                        timeError.setVisible(false);
                    }
                } else {
                    timeError.setVisible(false);
                }
            } catch (DateTimeParseException e) {
                timeError.setText("Invalid time format (use HH:MM)");
                timeError.setVisible(true);
            }
        }

        @Override
        public Node getContent() {
            return content;
        }

        @Override
        public boolean validate() {
            boolean valid = true;

            // Validate date
            if (serviceDatePicker.getValue() == null) {
                dateError.setVisible(true);
                valid = false;
            }

            // Validate times
            String startTimeText = startTimeField.getText();
            String endTimeText = endTimeField.getText();

            LocalTime startTime = null;
            LocalTime endTime = null;

            if (!startTimeText.isEmpty() || !endTimeText.isEmpty()) {
                try {
                    if (!startTimeText.isEmpty()) {
                        startTime = LocalTime.parse(startTimeText);
                    }

                    if (!endTimeText.isEmpty()) {
                        endTime = LocalTime.parse(endTimeText);
                    }

                    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
                        timeError.setText("Start time cannot be after end time");
                        timeError.setVisible(true);
                        valid = false;
                    }
                } catch (DateTimeParseException e) {
                    timeError.setText("Invalid time format (use HH:MM)");
                    timeError.setVisible(true);
                    valid = false;
                }
            }

            if (valid) {
                // Update service request
                serviceRequest.setServiceDate(serviceDatePicker.getValue());
                serviceRequest.setStartTime(startTime);
                serviceRequest.setEndTime(endTime);
            }

            return valid;
        }

        @Override
        public void onEnter() {
            // Pre-fill fields if editing
            if (serviceRequest.getServiceDate() != null) {
                serviceDatePicker.setValue(serviceRequest.getServiceDate());
            }

            if (serviceRequest.getStartTime() != null) {
                startTimeField.setText(serviceRequest.getStartTime().toString());
            }

            if (serviceRequest.getEndTime() != null) {
                endTimeField.setText(serviceRequest.getEndTime().toString());
            }
        }

        @Override
        public void reset() {
            serviceDatePicker.setValue(LocalDate.now());
            startTimeField.clear();
            endTimeField.clear();
            dateError.setVisible(false);
            timeError.setVisible(false);
        }

        @Override
        public String getTitle() {
            return "Scheduling";
        }
    }

    /**
     * Location and access step
     */
    public static class LocationStep implements WizardStep {
        private final VBox content;
        private final Label titleLabel;
        private final TextField buildingNameField;
        private final TextField addressField;
        private final TextField cityField;
        private final TextField stateField;
        private final TextField zipField;
        private final TextField pocNameField;
        private final TextField pocPhoneField;
        private final TextArea accessInfoField;
        private final ServiceRequest serviceRequest;
        private Label stateError;
        private Label zipError;

        /**
         * Create a new location step
         * @param serviceRequest The service request to populate
         */
        public LocationStep(ServiceRequest serviceRequest) {
            this.serviceRequest = serviceRequest;

            content = new VBox(15);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("wizard-step");

            titleLabel = new Label("Step 4: Location & Access");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

            // Create a two-column grid for location fields
            GridPane locationGrid = new GridPane();
            locationGrid.setHgap(10);
            locationGrid.setVgap(10);

            // Column constraints
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(120);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPrefWidth(200);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPrefWidth(120);
            ColumnConstraints col4 = new ColumnConstraints();
            col4.setPrefWidth(200);
            locationGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

            // Building name
            Label buildingNameLabel = new Label("Building Name:");
            buildingNameField = new TextField();
            locationGrid.add(buildingNameLabel, 0, 0);
            locationGrid.add(buildingNameField, 1, 0);

            // Address
            Label addressLabel = new Label("Address:");
            addressField = new TextField();
            locationGrid.add(addressLabel, 0, 1);
            locationGrid.add(addressField, 1, 1);

            // City
            Label cityLabel = new Label("City:");
            cityField = new TextField();
            locationGrid.add(cityLabel, 0, 2);
            locationGrid.add(cityField, 1, 2);

            // State
            Label stateLabel = new Label("State:");
            stateField = new TextField();
            stateField.setPromptText("2-letter code");
            locationGrid.add(stateLabel, 0, 3);
            locationGrid.add(stateField, 1, 3);

            stateError = new Label("State should be 2 letters");
            stateError.getStyleClass().add("error-label");
            stateError.setVisible(false);
            locationGrid.add(stateError, 1, 4);

            // ZIP
            Label zipLabel = new Label("ZIP Code:");
            zipField = new TextField();
            zipField.setPromptText("XXXXX or XXXXX-XXXX");
            locationGrid.add(zipLabel, 2, 3);
            locationGrid.add(zipField, 3, 3);

            zipError = new Label("Invalid ZIP code format");
            zipError.getStyleClass().add("error-label");
            zipError.setVisible(false);
            locationGrid.add(zipError, 3, 4);

            // POC Name
            Label pocNameLabel = new Label("POC Name:");
            pocNameField = new TextField();
            locationGrid.add(pocNameLabel, 2, 0);
            locationGrid.add(pocNameField, 3, 0);

            // POC Phone
            Label pocPhoneLabel = new Label("POC Phone:");
            pocPhoneField = new TextField();
            pocPhoneField.setPromptText("XXX-XXX-XXXX");
            locationGrid.add(pocPhoneLabel, 2, 1);
            locationGrid.add(pocPhoneField, 3, 1);

            // Access information
            Label accessInfoLabel = new Label("Access Information:");
            accessInfoField = new TextArea();
            accessInfoField.setPrefHeight(100);
            accessInfoField.setWrapText(true);
            accessInfoField.setPromptText("Enter any access instructions, gate codes, parking information, etc.");

            // Setup validation
            ValidationUtils.setupZipCodeTextField(zipField);
            ValidationUtils.setupPhoneTextField(pocPhoneField);

            stateField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty() && (newVal.length() != 2 || !newVal.matches("[A-Za-z]{2}"))) {
                    stateError.setVisible(true);
                } else {
                    stateError.setVisible(false);
                }
            });

            zipField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty() && !ValidationUtils.isValidZipCode(newVal)) {
                    zipError.setVisible(true);
                } else {
                    zipError.setVisible(false);
                }
            });

            // Fill location from customer if available
            Button fillFromCustomerButton = new Button("Fill from Customer");
            fillFromCustomerButton.setOnAction(e -> fillFromCustomer());

            content.getChildren().addAll(
                    titleLabel,
                    new Label("Please provide the service location and access information."),
                    fillFromCustomerButton,
                    locationGrid,
                    accessInfoLabel,
                    accessInfoField
            );
        }

        private void fillFromCustomer() {
            if (serviceRequest.getCustomer() != null) {
                Customer customer = serviceRequest.getCustomer();

                if (customer.getStreetAddress() != null) {
                    addressField.setText(customer.getStreetAddress());
                }

                if (customer.getCity() != null) {
                    cityField.setText(customer.getCity());
                }

                if (customer.getState() != null) {
                    stateField.setText(customer.getState());
                }

                if (customer.getZipCode() != null) {
                    zipField.setText(customer.getZipCode());
                }

                // POC info
                pocNameField.setText(customer.getFirstName() + " " + customer.getLastName());

                if (customer.getPhoneNumber() != null) {
                    pocPhoneField.setText(customer.getPhoneNumber());
                }
            }
        }

        @Override
        public Node getContent() {
            return content;
        }

        @Override
        public boolean validate() {
            boolean valid = true;

            // Validate state if entered
            if (stateField.getText() != null && !stateField.getText().isEmpty()) {
                if (stateField.getText().length() != 2 || !stateField.getText().matches("[A-Za-z]{2}")) {
                    stateError.setVisible(true);
                    valid = false;
                }
            }

            // Validate ZIP if entered
            if (zipField.getText() != null && !zipField.getText().isEmpty()) {
                if (!ValidationUtils.isValidZipCode(zipField.getText())) {
                    zipError.setVisible(true);
                    valid = false;
                }
            }

            if (valid) {
                // Update service request
                serviceRequest.setBuildingName(buildingNameField.getText());
                serviceRequest.setServiceAddress(addressField.getText());
                serviceRequest.setServiceCity(cityField.getText());
                serviceRequest.setServiceState(stateField.getText());
                serviceRequest.setServiceZip(zipField.getText());
                serviceRequest.setPocName(pocNameField.getText());
                serviceRequest.setPocPhone(pocPhoneField.getText());

                // Add access info to notes
                if (accessInfoField.getText() != null && !accessInfoField.getText().isEmpty()) {
                    String currentNotes = serviceRequest.getServiceNotes();
                    if (currentNotes == null) {
                        currentNotes = "";
                    }

                    if (!currentNotes.isEmpty()) {
                        currentNotes += "\n\n";
                    }

                    currentNotes += "Access Information:\n" + accessInfoField.getText();
                    serviceRequest.setServiceNotes(currentNotes);
                }
            }

            return valid;
        }

        @Override
        public void onEnter() {
            // Pre-fill fields if editing
            if (serviceRequest.getBuildingName() != null) {
                buildingNameField.setText(serviceRequest.getBuildingName());
            }

            if (serviceRequest.getServiceAddress() != null) {
                addressField.setText(serviceRequest.getServiceAddress());
            }

            if (serviceRequest.getServiceCity() != null) {
                cityField.setText(serviceRequest.getServiceCity());
            }

            if (serviceRequest.getServiceState() != null) {
                stateField.setText(serviceRequest.getServiceState());
            }

            if (serviceRequest.getServiceZip() != null) {
                zipField.setText(serviceRequest.getServiceZip());
            }

            if (serviceRequest.getPocName() != null) {
                pocNameField.setText(serviceRequest.getPocName());
            }

            if (serviceRequest.getPocPhone() != null) {
                pocPhoneField.setText(serviceRequest.getPocPhone());
            }

            // Extract access info from notes if available
            if (serviceRequest.getServiceNotes() != null && serviceRequest.getServiceNotes().contains("Access Information:")) {
                String notes = serviceRequest.getServiceNotes();
                int startIndex = notes.indexOf("Access Information:") + "Access Information:".length();
                int endIndex = notes.indexOf("\n\n", startIndex);

                if (endIndex == -1) {
                    endIndex = notes.length();
                }

                String accessInfo = notes.substring(startIndex, endIndex).trim();
                accessInfoField.setText(accessInfo);
            }
        }

        @Override
        public void reset() {
            buildingNameField.clear();
            addressField.clear();
            cityField.clear();
            stateField.clear();
            zipField.clear();
            pocNameField.clear();
            pocPhoneField.clear();
            accessInfoField.clear();
            stateError.setVisible(false);
            zipError.setVisible(false);
        }

        @Override
        public String getTitle() {
            return "Location & Access";
        }
    }

    /**
     * Technician assignment step
     */
    public static class TechnicianStep implements WizardStep {
        private final VBox content;
        private final Label titleLabel;
        private final AutoCompleteTextField<Technician> technicianField;
        private final ListView<Technician> assignedTechniciansList;
        private final TechnicianService technicianService;
        private final List<Technician> technicians;
        private final ServiceRequest serviceRequest;
        private final Button assignButton;
        private final Button removeButton;

        /**
         * Create a new technician step
         * @param technicianService The technician service
         * @param serviceRequest The service request to populate
         */
        public TechnicianStep(TechnicianService technicianService, ServiceRequest serviceRequest) {
            this.technicianService = technicianService;
            this.technicians = technicianService.getAllTechnicians();
            this.serviceRequest = serviceRequest;

            content = new VBox(15);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("wizard-step");

            titleLabel = new Label("Step 5: Technician Assignment");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

            // Technician search field
            HBox technicianBox = new HBox(10);
            technicianBox.setAlignment(Pos.CENTER_LEFT);
            Label technicianLabel = new Label("Select Technician:");
            technicianLabel.setPrefWidth(120);
            technicianField = new AutoCompleteTextField<>(technicians, t ->
                    t.getFirstName() + " " + t.getLastName() +
                            (t.getCredentialLevel() != null ? " (" + t.getCredentialLevel() + ")" : ""));
            technicianField.setPrefWidth(350);

            assignButton = new Button("Assign");
            assignButton.setOnAction(e -> assignTechnician());

            technicianBox.getChildren().addAll(technicianLabel, technicianField, assignButton);

            // Assigned technicians list
            VBox assignedTechniciansBox = new VBox(5);
            Label assignedTechniciansLabel = new Label("Assigned Technicians:");
            assignedTechniciansList = new ListView<>();
            assignedTechniciansList.setPrefHeight(200);
            assignedTechniciansList.setCellFactory(listView -> new ListCell<Technician>() {
                @Override
                protected void updateItem(Technician technician, boolean empty) {
                    super.updateItem(technician, empty);

                    if (technician == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(technician.getFirstName() + " " + technician.getLastName() +
                                (technician.getCredentialLevel() != null ?
                                        " (" + technician.getCredentialLevel() + ")" : ""));
                    }
                }
            });

            removeButton = new Button("Remove Selected");
            removeButton.setOnAction(e -> removeTechnician());
            removeButton.setDisable(true);

            // Handle selection
            assignedTechniciansList.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> removeButton.setDisable(newVal == null));

            assignedTechniciansBox.getChildren().addAll(assignedTechniciansLabel, assignedTechniciansList, removeButton);

            // Recommended technicians (placeholder)
            VBox recommendedBox = new VBox(5);
            Label recommendedLabel = new Label("Recommended Technicians:");
            recommendedLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

            // Placeholder for recommended technicians
            Label recommendedPlaceholder = new Label("Recommendations will be based on availability, skills, and location.");
            recommendedPlaceholder.setStyle("-fx-font-style: italic;");

            recommendedBox.getChildren().addAll(recommendedLabel, recommendedPlaceholder);

            content.getChildren().addAll(
                    titleLabel,
                    new Label("Please assign technicians to this service request."),
                    recommendedBox,
                    technicianBox,
                    assignedTechniciansBox
            );
        }

        private void assignTechnician() {
            Technician selectedTechnician = technicianField.getSelectedItem();
            if (selectedTechnician != null) {
                // Check if technician is already assigned
                boolean alreadyAssigned = false;
                for (Technician technician : assignedTechniciansList.getItems()) {
                    if (technician.getTechnicianId() == selectedTechnician.getTechnicianId()) {
                        alreadyAssigned = true;
                        break;
                    }
                }

                if (!alreadyAssigned) {
                    assignedTechniciansList.getItems().add(selectedTechnician);
                    technicianField.reset();
                }
            }
        }

        private void removeTechnician() {
            Technician selectedTechnician = assignedTechniciansList.getSelectionModel().getSelectedItem();
            if (selectedTechnician != null) {
                assignedTechniciansList.getItems().remove(selectedTechnician);
            }
        }

        @Override
        public Node getContent() {
            return content;
        }

        @Override
        public boolean validate() {
            // Update service request
            List<Technician> assignedTechnicians = new ArrayList<>(assignedTechniciansList.getItems());
            serviceRequest.setTechnicians(assignedTechnicians);

            // Assignment is optional
            return true;
        }

        @Override
        public void onEnter() {
            // Pre-fill assigned technicians if editing
            if (serviceRequest.getTechnicians() != null && !serviceRequest.getTechnicians().isEmpty()) {
                assignedTechniciansList.getItems().setAll(serviceRequest.getTechnicians());
            }
        }

        @Override
        public void reset() {
            technicianField.reset();
            assignedTechniciansList.getItems().clear();
        }

        @Override
        public String getTitle() {
            return "Technician Assignment";
        }
    }

    /**
     * Cost estimation step
     */
    public static class CostEstimationStep implements WizardStep {
        private final VBox content;
        private final Label titleLabel;
        private final TextField serviceCostField;
        private final TextField addedCostField;
        private final TextField parkingFeesField;
        private final Label totalCostLabel;
        private final TextArea costNotesField;
        private final ServiceRequest serviceRequest;
        private Label serviceCostError;
        private Label addedCostError;
        private Label parkingFeesError;

        /**
         * Create a new cost estimation step
         * @param serviceRequest The service request to populate
         */
        public CostEstimationStep(ServiceRequest serviceRequest) {
            this.serviceRequest = serviceRequest;

            content = new VBox(15);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("wizard-step");

            titleLabel = new Label("Step 6: Cost Estimation");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

            // Create grid for cost fields
            GridPane costGrid = new GridPane();
            costGrid.setHgap(10);
            costGrid.setVgap(10);

            // Column constraints
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(120);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPrefWidth(150);
            costGrid.getColumnConstraints().addAll(col1, col2);

            // Service cost
            Label serviceCostLabel = new Label("Service Cost:");
            serviceCostField = new TextField();
            serviceCostField.setPromptText("0.00");
            costGrid.add(serviceCostLabel, 0, 0);
            costGrid.add(serviceCostField, 1, 0);

            serviceCostError = new Label("Invalid number format");
            serviceCostError.getStyleClass().add("error-label");
            serviceCostError.setVisible(false);
            costGrid.add(serviceCostError, 1, 1);

            // Added cost
            Label addedCostLabel = new Label("Added Cost:");
            addedCostField = new TextField();
            addedCostField.setPromptText("0.00");
            costGrid.add(addedCostLabel, 0, 2);
            costGrid.add(addedCostField, 1, 2);

            addedCostError = new Label("Invalid number format");
            addedCostError.getStyleClass().add("error-label");
            addedCostError.setVisible(false);
            costGrid.add(addedCostError, 1, 3);

            // Parking fees
            Label parkingFeesLabel = new Label("Parking Fees:");
            parkingFeesField = new TextField();
            parkingFeesField.setPromptText("0.00");
            costGrid.add(parkingFeesLabel, 0, 4);
            costGrid.add(parkingFeesField, 1, 4);

            parkingFeesError = new Label("Invalid number format");
            parkingFeesError.getStyleClass().add("error-label");
            parkingFeesError.setVisible(false);
            costGrid.add(parkingFeesError, 1, 5);

            // Total cost
            Label totalLabel = new Label("Total Cost:");
            totalLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
            totalCostLabel = new Label("$0.00");
            totalCostLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
            costGrid.add(totalLabel, 0, 6);
            costGrid.add(totalCostLabel, 1, 6);

            // Cost notes
            Label costNotesLabel = new Label("Cost Notes:");
            costNotesField = new TextArea();
            costNotesField.setPrefHeight(100);
            costNotesField.setWrapText(true);
            costNotesField.setPromptText("Enter any notes about the cost estimate.");

            // Setup validation
            ValidationUtils.setupDoubleTextField(serviceCostField);
            ValidationUtils.setupDoubleTextField(addedCostField);
            ValidationUtils.setupDoubleTextField(parkingFeesField);

            // Update total when costs change
            serviceCostField.textProperty().addListener((obs, oldVal, newVal) -> updateTotal());
            addedCostField.textProperty().addListener((obs, oldVal, newVal) -> updateTotal());
            parkingFeesField.textProperty().addListener((obs, oldVal, newVal) -> updateTotal());

            content.getChildren().addAll(
                    titleLabel,
                    new Label("Please provide cost estimates for this service request."),
                    costGrid,
                    costNotesLabel,
                    costNotesField
            );
        }

        private void updateTotal() {
            double serviceCost = parseDouble(serviceCostField.getText());
            double addedCost = parseDouble(addedCostField.getText());
            double parkingFees = parseDouble(parkingFeesField.getText());

            double total = serviceCost + addedCost + parkingFees;
            totalCostLabel.setText(String.format("$%.2f", total));
        }

        private double parseDouble(String text) {
            if (text == null || text.isEmpty()) {
                return 0.0;
            }

            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        @Override
        public Node getContent() {
            return content;
        }

        @Override
        public boolean validate() {
            boolean valid = true;

            // Validate service cost
            try {
                double serviceCost = Double.parseDouble(serviceCostField.getText());
                serviceCostError.setVisible(false);
            } catch (NumberFormatException e) {
                if (serviceCostField.getText() != null && !serviceCostField.getText().isEmpty()) {
                    serviceCostError.setVisible(true);
                    valid = false;
                }
            }

            // Validate added cost
            try {
                double addedCost = Double.parseDouble(addedCostField.getText());
                addedCostError.setVisible(false);
            } catch (NumberFormatException e) {
                if (addedCostField.getText() != null && !addedCostField.getText().isEmpty()) {
                    addedCostError.setVisible(true);
                    valid = false;
                }
            }

            // Validate parking fees
            try {
                double parkingFees = Double.parseDouble(parkingFeesField.getText());
                parkingFeesError.setVisible(false);
            } catch (NumberFormatException e) {
                if (parkingFeesField.getText() != null && !parkingFeesField.getText().isEmpty()) {
                    parkingFeesError.setVisible(true);
                    valid = false;
                }
            }

            if (valid) {
                // Update service request
                serviceRequest.setServiceCost(parseDouble(serviceCostField.getText()));
                serviceRequest.setAddedCost(parseDouble(addedCostField.getText()));
                serviceRequest.setParkingFees(parseDouble(parkingFeesField.getText()));

                // Add cost notes to notes
                if (costNotesField.getText() != null && !costNotesField.getText().isEmpty()) {
                    String currentNotes = serviceRequest.getServiceNotes();
                    if (currentNotes == null) {
                        currentNotes = "";
                    }

                    if (!currentNotes.isEmpty()) {
                        currentNotes += "\n\n";
                    }

                    currentNotes += "Cost Notes:\n" + costNotesField.getText();
                    serviceRequest.setServiceNotes(currentNotes);
                }
            }

            return valid;
        }

        @Override
        public void onEnter() {
            // Pre-fill fields if editing
            serviceCostField.setText(String.format("%.2f", serviceRequest.getServiceCost()));
            addedCostField.setText(String.format("%.2f", serviceRequest.getAddedCost()));
            parkingFeesField.setText(String.format("%.2f", serviceRequest.getParkingFees()));

            updateTotal();

            // Extract cost notes from notes if available
            if (serviceRequest.getServiceNotes() != null && serviceRequest.getServiceNotes().contains("Cost Notes:")) {
                String notes = serviceRequest.getServiceNotes();
                int startIndex = notes.indexOf("Cost Notes:") + "Cost Notes:".length();
                int endIndex = notes.indexOf("\n\n", startIndex);

                if (endIndex == -1) {
                    endIndex = notes.length();
                }

                String costNotes = notes.substring(startIndex, endIndex).trim();
                costNotesField.setText(costNotes);
            }
        }

        @Override
        public void reset() {
            serviceCostField.setText("0.00");
            addedCostField.setText("0.00");
            parkingFeesField.setText("0.00");
            totalCostLabel.setText("$0.00");
            costNotesField.clear();
            serviceCostError.setVisible(false);
            addedCostError.setVisible(false);
            parkingFeesError.setVisible(false);
        }

        @Override
        public String getTitle() {
            return "Cost Estimation";
        }
    }

    /**
     * Review and confirmation step
     */
    public static class ReviewStep implements WizardStep {
        private final VBox content;
        private final Label titleLabel;
        private final VBox summaryContainer;
        private final CheckBox sendConfirmationCheck;
        private final ServiceRequest serviceRequest;
        private final Consumer<ServiceRequest> onSaveCallback;

        /**
         * Create a new review step
         * @param serviceRequest The service request to review
         * @param onSaveCallback Callback when saving
         */
        public ReviewStep(ServiceRequest serviceRequest, Consumer<ServiceRequest> onSaveCallback) {
            this.serviceRequest = serviceRequest;
            this.onSaveCallback = onSaveCallback;

            content = new VBox(15);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("wizard-step");

            titleLabel = new Label("Step 7: Review & Confirmation");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 18));

            // Summary container
            summaryContainer = new VBox(10);
            summaryContainer.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 10px; -fx-border-color: #ddd;");

            // Options
            VBox optionsBox = new VBox(10);
            sendConfirmationCheck = new CheckBox("Send confirmation email to customer");
            sendConfirmationCheck.setSelected(true);

            optionsBox.getChildren().add(sendConfirmationCheck);

            content.getChildren().addAll(
                    titleLabel,
                    new Label("Please review the service request details before confirming."),
                    summaryContainer,
                    optionsBox
            );
        }

        @Override
        public Node getContent() {
            return content;
        }

        @Override
        public boolean validate() {
            // Call the save callback
            onSaveCallback.accept(serviceRequest);
            return true;
        }

        @Override
        public void onEnter() {
            // Generate summary
            updateSummary();
        }

        private void updateSummary() {
            summaryContainer.getChildren().clear();

            // Customer
            if (serviceRequest.getCustomer() != null) {
                addSummarySection("Customer",
                        serviceRequest.getCustomer().getFirstName() + " " + serviceRequest.getCustomer().getLastName() +
                                " (" + serviceRequest.getCustomer().getCustomerNumber() + ")");
            }

            // Description
            addSummarySection("Description", serviceRequest.getDescription());

            // Date and time
            StringBuilder dateTime = new StringBuilder();
            dateTime.append(formatDate(serviceRequest.getServiceDate()));

            if (serviceRequest.getStartTime() != null) {
                dateTime.append(" at ").append(formatTime(serviceRequest.getStartTime()));

                if (serviceRequest.getEndTime() != null) {
                    dateTime.append(" - ").append(formatTime(serviceRequest.getEndTime()));
                }
            }

            addSummarySection("Date & Time", dateTime.toString());

            // Location
            StringBuilder location = new StringBuilder();
            if (serviceRequest.getBuildingName() != null && !serviceRequest.getBuildingName().isEmpty()) {
                location.append(serviceRequest.getBuildingName()).append("\n");
            }

            if (serviceRequest.getServiceAddress() != null && !serviceRequest.getServiceAddress().isEmpty()) {
                location.append(serviceRequest.getServiceAddress()).append("\n");

                StringBuilder cityStateZip = new StringBuilder();
                if (serviceRequest.getServiceCity() != null && !serviceRequest.getServiceCity().isEmpty()) {
                    cityStateZip.append(serviceRequest.getServiceCity());
                }

                if (serviceRequest.getServiceState() != null && !serviceRequest.getServiceState().isEmpty()) {
                    if (cityStateZip.length() > 0) {
                        cityStateZip.append(", ");
                    }
                    cityStateZip.append(serviceRequest.getServiceState());
                }

                if (serviceRequest.getServiceZip() != null && !serviceRequest.getServiceZip().isEmpty()) {
                    if (cityStateZip.length() > 0) {
                        cityStateZip.append(" ");
                    }
                    cityStateZip.append(serviceRequest.getServiceZip());
                }

                if (cityStateZip.length() > 0) {
                    location.append(cityStateZip);
                }
            }

            if (location.length() > 0) {
                addSummarySection("Location", location.toString());
            }

            // Point of contact
            if (serviceRequest.getPocName() != null && !serviceRequest.getPocName().isEmpty()) {
                StringBuilder poc = new StringBuilder();
                poc.append(serviceRequest.getPocName());

                if (serviceRequest.getPocPhone() != null && !serviceRequest.getPocPhone().isEmpty()) {
                    poc.append(" (").append(serviceRequest.getPocPhone()).append(")");
                }

                addSummarySection("Point of Contact", poc.toString());
            }

            // Technicians
            if (serviceRequest.getTechnicians() != null && !serviceRequest.getTechnicians().isEmpty()) {
                StringBuilder technicians = new StringBuilder();
                for (Technician technician : serviceRequest.getTechnicians()) {
                    if (technicians.length() > 0) {
                        technicians.append("\n");
                    }
                    technicians.append(technician.getFirstName()).append(" ").append(technician.getLastName());

                    if (technician.getCredentialLevel() != null && !technician.getCredentialLevel().isEmpty()) {
                        technicians.append(" (").append(technician.getCredentialLevel()).append(")");
                    }
                }

                addSummarySection("Assigned Technicians", technicians.toString());
            }

            // Costs
            StringBuilder costs = new StringBuilder();
            costs.append("Service Cost: $").append(String.format("%.2f", serviceRequest.getServiceCost())).append("\n");
            costs.append("Added Cost: $").append(String.format("%.2f", serviceRequest.getAddedCost())).append("\n");
            costs.append("Parking Fees: $").append(String.format("%.2f", serviceRequest.getParkingFees())).append("\n");
            costs.append("Total: $").append(String.format("%.2f", serviceRequest.getTotalCost()));

            addSummarySection("Cost Estimate", costs.toString());

            // Status
            addSummarySection("Status", serviceRequest.getStatus());

            // Notes
            if (serviceRequest.getServiceNotes() != null && !serviceRequest.getServiceNotes().isEmpty()) {
                addSummarySection("Notes", serviceRequest.getServiceNotes());
            }
        }

        private void addSummarySection(String title, String content) {
            VBox section = new VBox(5);

            Label titleLabel = new Label(title + ":");
            titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

            Label contentLabel = new Label(content);
            contentLabel.setWrapText(true);

            section.getChildren().addAll(titleLabel, contentLabel);
            summaryContainer.getChildren().add(section);

            if (summaryContainer.getChildren().size() > 1) {
                Separator separator = new Separator();
                separator.setPadding(new Insets(5, 0, 5, 0));
                summaryContainer.getChildren().add(separator);
            }
        }

        private String formatDate(LocalDate date) {
            if (date == null) {
                return "";
            }
            return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        }

        private String formatTime(LocalTime time) {
            if (time == null) {
                return "";
            }
            return time.format(DateTimeFormatter.ofPattern("h:mm a"));
        }

        @Override
        public void reset() {
            summaryContainer.getChildren().clear();
            sendConfirmationCheck.setSelected(true);
        }

        @Override
        public String getTitle() {
            return "Review & Confirmation";
        }

        /**
         * Get whether to send confirmation email
         * @return true if confirmation should be sent
         */
        public boolean isSendConfirmation() {
            return sendConfirmationCheck.isSelected();
        }
    }
}