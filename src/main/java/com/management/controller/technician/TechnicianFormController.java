package com.management.controller.technician;

import com.management.model.Technician;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the step-based technician form view (add/edit)
 */
public class TechnicianFormController {

    @FXML private Label titleLabel;

    // Navigation buttons
    @FXML private Button basicInfoButton;
    @FXML private Button credentialsButton;
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
    @FXML private VBox professionalSection;
    @FXML private VBox addressSection;
    @FXML private VBox additionalSection;

    // Form fields - Basic Info
    @FXML private TextField technicianIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField legalNameField;

    // Form fields - Credentials
    @FXML private TextField credentialsField;
    @FXML private ComboBox<String> credentialLevelCombo;
    @FXML private TextField zipCodeField;
    @FXML private TextField coverageAreaField;

    // Form fields - Address
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipField;

    // Form fields - Additional Info
    @FXML private ComboBox<String> payTypeCombo;
    @FXML private TextField accountInfoField;
    @FXML private TextArea notesArea;

    // Error labels
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label zipError;
    @FXML private Label stateError;

    private TechnicianService technicianService;
    private Technician technician;
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

        // Initialize combo boxes
        initializeComboBoxes();
    }

    /**
     * Initialize combo box values
     */
    private void initializeComboBoxes() {
        // Add items to credential level combo box
        credentialLevelCombo.getItems().addAll("Junior", "Mid", "Senior", "Expert");

        // Add items to pay type combo box
        payTypeCombo.getItems().addAll("Hourly", "Salary", "Contract", "Commission");

        // Add selection listeners
        // This fixes the issue with dropdown selections not being applied
        credentialLevelCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Credential level selected: " + newVal);
            }
        });

        payTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Pay type selected: " + newVal);
            }
        });

        // Make sure the ComboBox uses the full width of its parent container
        credentialLevelCombo.setMaxWidth(Double.MAX_VALUE);
        payTypeCombo.setMaxWidth(Double.MAX_VALUE);

        // Ensure the dropdown list shows up properly
        credentialLevelCombo.setVisibleRowCount(4);
        payTypeCombo.setVisibleRowCount(4);
    }

    /**
     * Set up validators for form fields
     */
    private void setupValidators() {
        // Set up text formatters and validators
        ValidationUtils.setupZipCodeTextField(zipCodeField);
        ValidationUtils.setupZipCodeTextField(zipField);

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

        zipCodeField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(zipCodeField, zipError,
                        text -> ValidationUtils.validateZipCode("Zip code", text, false)));

        stateField.textProperty().addListener((obs, oldVal, newVal) ->
                ValidationUtils.validateField(stateField, stateError,
                        text -> text.length() > 2 ? "State should be 2 letters" : ""));
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
        credentialsButton.setOnAction(e -> navigateToStep(1));
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
    public void showCredentials() {
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
                (currentStep == 1 ? validateCredentials() : true)) {
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
        professionalSection.setVisible(false);
        addressSection.setVisible(false);
        additionalSection.setVisible(false);

        // Show the selected section
        switch (stepIndex) {
            case 0:
                basicInfoSection.setVisible(true);
                break;
            case 1:
                professionalSection.setVisible(true);
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
        credentialsButton.getStyleClass().remove("active-nav-button");
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
                if (!credentialsButton.getStyleClass().contains("active-nav-button")) {
                    credentialsButton.getStyleClass().add("active-nav-button");
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
                return validateCredentials();
            case 2:
                return validateAddress();
            case 3:
                return validateAdditional();
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

        return isFirstNameValid && isLastNameValid && isEmailValid;
    }

    /**
     * Validate credentials information fields
     * @return true if all required fields are valid
     */
    private boolean validateCredentials() {
        boolean isZipValid = ValidationUtils.validateField(
                zipCodeField, zipError, text -> ValidationUtils.validateZipCode("Zip code", text, false));

        return isZipValid;
    }

    /**
     * Validate address information fields
     * @return true if all required fields are valid
     */
    private boolean validateAddress() {
        boolean isStateValid = ValidationUtils.validateField(
                stateField, stateError, text -> text.length() > 2 ? "State should be 2 letters" : "");

        return isStateValid;
    }

    /**
     * Validate additional information fields
     * @return true if all required fields are valid
     */
    private boolean validateAdditional() {
        return true; // No required fields in additional section
    }

    /**
     * Set the technician service
     * @param technicianService The technician service to use
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
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
     * Load a technician for editing
     * @param technician The technician to edit
     */
    public void loadTechnician(Technician technician) {
        this.technician = technician;

        // Populate the form fields
        technicianIdField.setText(String.valueOf(technician.getTechnicianId()));
        technicianIdField.setDisable(true); // Technician ID should not be editable

        // Basic Info
        firstNameField.setText(technician.getFirstName());
        lastNameField.setText(technician.getLastName());
        emailField.setText(technician.getEmail());
        legalNameField.setText(technician.getLegalName());

        // Credentials
        credentialsField.setText(technician.getCredentials());

        // Make sure to set ComboBox values properly
        if (technician.getCredentialLevel() != null && !technician.getCredentialLevel().isEmpty()) {
            credentialLevelCombo.setValue(technician.getCredentialLevel());
        }

        zipCodeField.setText(technician.getZipCode());
        coverageAreaField.setText(technician.getCoverageArea());

        // Address
        addressField.setText(technician.getAddress());
        cityField.setText(technician.getCity());
        stateField.setText(technician.getState());
        zipField.setText(technician.getZip());

        // Additional Info
        if (technician.getPayType() != null && !technician.getPayType().isEmpty()) {
            payTypeCombo.setValue(technician.getPayType());
        }

        accountInfoField.setText(technician.getAccountInfo());
        notesArea.setText(technician.getNotes());
    }

    /**
     * Update the form title based on the mode
     */
    private void updateFormTitle() {
        if (mode == Mode.ADD) {
            titleLabel.setText("New Technician");
            saveButton.setText("Create Technician");
        } else {
            titleLabel.setText("Edit Technician");
            saveButton.setText("Save Changes");
        }
    }

    /**
     * Handle saving the technician
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
                // Create new technician
                Technician newTechnician = createTechnicianFromForm();
                int id = technicianService.createTechnician(newTechnician);

                if (id > 0) {
                    AlertUtils.showInformationAlert("Success", "Technician created successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to create technician.");
                }
            } else {
                // Update existing technician
                updateTechnicianFromForm();
                boolean success = technicianService.updateTechnician(technician);

                if (success) {
                    AlertUtils.showInformationAlert("Success", "Technician updated successfully.");
                    closeForm();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to update technician.");
                }
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new technician from the form data
     * @return The new technician
     */
    private Technician createTechnicianFromForm() {
        Technician newTechnician = new Technician();

        // Basic Info
        newTechnician.setFirstName(firstNameField.getText().trim());
        newTechnician.setLastName(lastNameField.getText().trim());
        newTechnician.setEmail(emailField.getText().trim());
        newTechnician.setLegalName(legalNameField.getText().trim());

        // Credentials
        newTechnician.setCredentials(credentialsField.getText().trim());

        // Get values directly from ComboBox value property
        newTechnician.setCredentialLevel(credentialLevelCombo.getValue());

        newTechnician.setZipCode(zipCodeField.getText().trim());
        newTechnician.setCoverageArea(coverageAreaField.getText().trim());

        // Address
        newTechnician.setAddress(addressField.getText().trim());
        newTechnician.setCity(cityField.getText().trim());
        newTechnician.setState(stateField.getText().trim());
        newTechnician.setZip(zipField.getText().trim());

        // Additional Info
        newTechnician.setPayType(payTypeCombo.getValue());
        newTechnician.setAccountInfo(accountInfoField.getText().trim());
        newTechnician.setNotes(notesArea.getText().trim());

        return newTechnician;
    }

    /**
     * Update an existing technician with form data
     */
    private void updateTechnicianFromForm() {
        // Basic Info
        technician.setFirstName(firstNameField.getText().trim());
        technician.setLastName(lastNameField.getText().trim());
        technician.setEmail(emailField.getText().trim());
        technician.setLegalName(legalNameField.getText().trim());

        // Credentials
        technician.setCredentials(credentialsField.getText().trim());

        // Get values directly from ComboBox value property
        technician.setCredentialLevel(credentialLevelCombo.getValue());

        technician.setZipCode(zipCodeField.getText().trim());
        technician.setCoverageArea(coverageAreaField.getText().trim());

        // Address
        technician.setAddress(addressField.getText().trim());
        technician.setCity(cityField.getText().trim());
        technician.setState(stateField.getText().trim());
        technician.setZip(zipField.getText().trim());

        // Additional Info
        technician.setPayType(payTypeCombo.getValue());
        technician.setAccountInfo(accountInfoField.getText().trim());
        technician.setNotes(notesArea.getText().trim());
    }

    /**
     * Validate all sections in the form
     * @return true if all sections are valid
     */
    private boolean validateAllSections() {
        boolean isBasicInfoValid = validateBasicInfo();
        boolean isCredentialsValid = validateCredentials();
        boolean isAddressValid = validateAddress();
        boolean isAdditionalValid = validateAdditional();

        // If any section is invalid, navigate to that section
        if (!isBasicInfoValid) {
            navigateToStep(0);
            return false;
        }

        if (!isCredentialsValid) {
            navigateToStep(1);
            return false;
        }

        if (!isAddressValid) {
            navigateToStep(2);
            return false;
        }

        if (!isAdditionalValid) {
            navigateToStep(3);
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