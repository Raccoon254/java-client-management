package com.management.controller.technician;

import com.management.model.Technician;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for the technician form view (add/edit)
 */
public class TechnicianFormController {

    @FXML private Label titleLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField credentialsField;
    @FXML private ComboBox<String> credentialLevelCombo;
    @FXML private TextField zipCodeField;
    @FXML private TextField coverageAreaField;
    @FXML private ComboBox<String> payTypeCombo;
    @FXML private TextField accountInfoField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipField;
    @FXML private TextField legalNameField;
    @FXML private TextArea notesArea;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label zipError;
    @FXML private Label stateError;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private TechnicianService technicianService;
    private Technician technician;
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
        ValidationUtils.setupZipCodeTextField(zipCodeField);
        ValidationUtils.setupZipCodeTextField(zipField);

        // Initialize combo box values
        credentialLevelCombo.getItems().addAll("Junior", "Mid", "Senior", "Expert");
        payTypeCombo.getItems().addAll("Hourly", "Salary", "Contract", "Commission");

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

        // Set up button actions
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
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
        firstNameField.setText(technician.getFirstName());
        lastNameField.setText(technician.getLastName());
        emailField.setText(technician.getEmail());
        credentialsField.setText(technician.getCredentials());

        if (technician.getCredentialLevel() != null) {
            credentialLevelCombo.setValue(technician.getCredentialLevel());
        }

        zipCodeField.setText(technician.getZipCode());
        coverageAreaField.setText(technician.getCoverageArea());

        if (technician.getPayType() != null) {
            payTypeCombo.setValue(technician.getPayType());
        }

        accountInfoField.setText(technician.getAccountInfo());
        addressField.setText(technician.getAddress());
        cityField.setText(technician.getCity());
        stateField.setText(technician.getState());
        zipField.setText(technician.getZip());
        legalNameField.setText(technician.getLegalName());
        notesArea.setText(technician.getNotes());
    }

    /**
     * Update the form title based on the mode
     */
    private void updateFormTitle() {
        if (mode == Mode.ADD) {
            titleLabel.setText("Add New Technician");
            saveButton.setText("Create Technician");
        } else {
            titleLabel.setText("Edit Technician");
            saveButton.setText("Save Changes");
        }
    }

    /**
     * Handle saving the technician
     */
    private void handleSave() {
        // Validate all required fields
        boolean isValid = validateForm();

        if (!isValid) {
            // Show error message with specific validation errors
            StringBuilder errorMsg = new StringBuilder("Please correct the following errors:\n");

            if (firstNameError.isVisible()) {
                errorMsg.append("- ").append(firstNameError.getText()).append("\n");
            }
            if (lastNameError.isVisible()) {
                errorMsg.append("- ").append(lastNameError.getText()).append("\n");
            }
            if (emailError.isVisible()) {
                errorMsg.append("- ").append(emailError.getText()).append("\n");
            }
            if (zipError.isVisible()) {
                errorMsg.append("- ").append(zipError.getText()).append("\n");
            }
            if (stateError.isVisible()) {
                errorMsg.append("- ").append(stateError.getText()).append("\n");
            }

            AlertUtils.showWarningAlert("Validation Error", errorMsg.toString());
            return;
        }

        try {
            if (mode == Mode.ADD) {
                // Create new technician
                Technician newTechnician = createTechnicianFromForm();
                int technicianId = technicianService.createTechnician(newTechnician);

                if (technicianId > 0) {
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
            // Show the specific error message from the service layer
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

        newTechnician.setFirstName(firstNameField.getText().trim());
        newTechnician.setLastName(lastNameField.getText().trim());
        newTechnician.setEmail(emailField.getText().trim());
        newTechnician.setCredentials(credentialsField.getText().trim());
        newTechnician.setCredentialLevel(credentialLevelCombo.getValue());
        newTechnician.setZipCode(zipCodeField.getText().trim());
        newTechnician.setCoverageArea(coverageAreaField.getText().trim());
        newTechnician.setPayType(payTypeCombo.getValue());
        newTechnician.setAccountInfo(accountInfoField.getText().trim());
        newTechnician.setAddress(addressField.getText().trim());
        newTechnician.setCity(cityField.getText().trim());
        newTechnician.setState(stateField.getText().trim());
        newTechnician.setZip(zipField.getText().trim());
        newTechnician.setLegalName(legalNameField.getText().trim());
        newTechnician.setNotes(notesArea.getText().trim());

        return newTechnician;
    }

    /**
     * Update the existing technician with form data
     */
    private void updateTechnicianFromForm() {
        technician.setFirstName(firstNameField.getText().trim());
        technician.setLastName(lastNameField.getText().trim());
        technician.setEmail(emailField.getText().trim());
        technician.setCredentials(credentialsField.getText().trim());
        technician.setCredentialLevel(credentialLevelCombo.getValue());
        technician.setZipCode(zipCodeField.getText().trim());
        technician.setCoverageArea(coverageAreaField.getText().trim());
        technician.setPayType(payTypeCombo.getValue());
        technician.setAccountInfo(accountInfoField.getText().trim());
        technician.setAddress(addressField.getText().trim());
        technician.setCity(cityField.getText().trim());
        technician.setState(stateField.getText().trim());
        technician.setZip(zipField.getText().trim());
        technician.setLegalName(legalNameField.getText().trim());
        technician.setNotes(notesArea.getText().trim());
    }

    /**
     * Validate the form
     * @return true if the form is valid
     */
    private boolean validateForm() {
        // Reset all error labels first
        firstNameError.setVisible(false);
        lastNameError.setVisible(false);
        emailError.setVisible(false);
        zipError.setVisible(false);
        stateError.setVisible(false);

        boolean isFirstNameValid = ValidationUtils.validateField(
                firstNameField, firstNameError, text -> ValidationUtils.validateRequired("First name", text));

        boolean isLastNameValid = ValidationUtils.validateField(
                lastNameField, lastNameError, text -> ValidationUtils.validateRequired("Last name", text));

        boolean isEmailValid = ValidationUtils.validateField(
                emailField, emailError, text -> ValidationUtils.validateEmail("Email", text));

        boolean isZipValid = ValidationUtils.validateField(
                zipCodeField, zipError, text -> ValidationUtils.validateZipCode("Zip code", text, false));

        boolean isStateValid = true;
        if (stateField.getText() != null && !stateField.getText().trim().isEmpty()) {
            isStateValid = ValidationUtils.validateField(
                    stateField, stateError, text -> text.length() > 2 ? "State should be 2 letters" : "");
        }

        return isFirstNameValid && isLastNameValid && isEmailValid && isZipValid && isStateValid;
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