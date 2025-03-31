package com.management.controller.technician;

import com.management.model.Technician;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.function.Predicate;

/**
 * Controller for the technician list view
 */
public class TechnicianListController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private TableView<Technician> technicianTable;

    @FXML
    private TableColumn<Technician, Integer> idColumn;

    @FXML
    private TableColumn<Technician, String> firstNameColumn;

    @FXML
    private TableColumn<Technician, String> lastNameColumn;

    @FXML
    private TableColumn<Technician, String> emailColumn;

    @FXML
    private TableColumn<Technician, String> credentialsColumn;

    @FXML
    private TableColumn<Technician, String> credentialLevelColumn;

    @FXML
    private TableColumn<Technician, String> coverageAreaColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterCredentialBox;

    @FXML
    private ComboBox<String> filterCoverageBox;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button scheduleButton;

    @FXML
    private Label statusLabel;

    private TechnicianService technicianService;
    private ObservableList<Technician> technicianList = FXCollections.observableArrayList();
    private FilteredList<Technician> filteredTechnicians;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("technicianId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        credentialsColumn.setCellValueFactory(new PropertyValueFactory<>("credentials"));
        credentialLevelColumn.setCellValueFactory(new PropertyValueFactory<>("credentialLevel"));
        coverageAreaColumn.setCellValueFactory(new PropertyValueFactory<>("coverageArea"));

        // Set up the search and filter functionality
        filteredTechnicians = new FilteredList<>(technicianList, p -> true);

        // Configure search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredTechnicians.setPredicate(createPredicate(
                    newValue,
                    filterCredentialBox.getValue(),
                    filterCoverageBox.getValue()
            ));
            updateStatusLabel();
        });

        // Configure combobox for credential level filtering
        filterCredentialBox.getItems().add("All Credentials");
        filterCredentialBox.setValue("All Credentials");
        filterCredentialBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredTechnicians.setPredicate(createPredicate(
                    searchField.getText(),
                    newVal,
                    filterCoverageBox.getValue()
            ));
            updateStatusLabel();
        });

        // Configure combobox for coverage area filtering
        filterCoverageBox.getItems().add("All Areas");
        filterCoverageBox.setValue("All Areas");
        filterCoverageBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredTechnicians.setPredicate(createPredicate(
                    searchField.getText(),
                    filterCredentialBox.getValue(),
                    newVal
            ));
            updateStatusLabel();
        });

        // Connect filtered list to TableView
        SortedList<Technician> sortedTechnicians = new SortedList<>(filteredTechnicians);
        sortedTechnicians.comparatorProperty().bind(technicianTable.comparatorProperty());
        technicianTable.setItems(sortedTechnicians);

        // Enable/disable buttons based on selection
        technicianTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            scheduleButton.setDisable(!hasSelection);
        });

        // Double-click to edit
        technicianTable.setRowFactory(tv -> {
            TableRow<Technician> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditTechnician();
                }
            });
            return row;
        });

        // Set default button states
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        scheduleButton.setDisable(true);

        // Set action handlers
        addButton.setOnAction(e -> handleAddTechnician());
        editButton.setOnAction(e -> handleEditTechnician());
        deleteButton.setOnAction(e -> handleDeleteTechnician());
        refreshButton.setOnAction(e -> refreshTechnicianList());
        exportButton.setOnAction(e -> handleExportTechnicians());
        scheduleButton.setOnAction(e -> handleViewTechnicianSchedule());
    }

    /**
     * Set the technician service
     * @param technicianService The technician service to use
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
        loadTechnicians();
        loadFilterOptions();
    }

    /**
     * Create a predicate for filtering technicians based on search text and filters
     * @param searchText The search text
     * @param credentialLevel The selected credential level
     * @param coverageArea The selected coverage area
     * @return A predicate for filtering technicians
     */
    private Predicate<Technician> createPredicate(String searchText, String credentialLevel, String coverageArea) {
        return technician -> {
            // If all filters are empty/default, show all technicians
            if ((searchText == null || searchText.isEmpty()) &&
                    (credentialLevel == null || credentialLevel.equals("All Credentials")) &&
                    (coverageArea == null || coverageArea.equals("All Areas"))) {
                return true;
            }

            boolean matchesSearch = true;
            boolean matchesCredential = true;
            boolean matchesCoverage = true;

            // Apply search filter if searchText is not empty
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseSearch = searchText.toLowerCase();

                matchesSearch = (technician.getFirstName().toLowerCase().contains(lowerCaseSearch) ||
                        technician.getLastName().toLowerCase().contains(lowerCaseSearch) ||
                        (technician.getEmail() != null && technician.getEmail().toLowerCase().contains(lowerCaseSearch)) ||
                        (technician.getCredentials() != null && technician.getCredentials().toLowerCase().contains(lowerCaseSearch)) ||
                        (technician.getCoverageArea() != null && technician.getCoverageArea().toLowerCase().contains(lowerCaseSearch)));
            }

            // Apply credential level filter
            if (credentialLevel != null && !credentialLevel.equals("All Credentials")) {
                matchesCredential = (technician.getCredentialLevel() != null &&
                        technician.getCredentialLevel().equals(credentialLevel));
            }

            // Apply coverage area filter
            if (coverageArea != null && !coverageArea.equals("All Areas")) {
                matchesCoverage = (technician.getCoverageArea() != null &&
                        technician.getCoverageArea().contains(coverageArea));
            }

            return matchesSearch && matchesCredential && matchesCoverage;
        };
    }

    /**
     * Load technicians from the database
     */
    private void loadTechnicians() {
        try {
            statusLabel.setText("Loading technicians...");

            // Clear current list
            technicianList.clear();

            // Get the latest technician list
            List<Technician> technicians = technicianService.getAllTechnicians();

            // Update the observable list
            technicianList.addAll(technicians);

            updateStatusLabel();
        } catch (Exception e) {
            statusLabel.setText("Error loading technicians: " + e.getMessage());
            AlertUtils.showErrorAlert("Error", "Failed to load technicians: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load filter options for credential levels and coverage areas
     */
    private void loadFilterOptions() {
        try {
            List<Technician> allTechnicians = technicianService.getAllTechnicians();

            // Remember current selections
            String currentCredential = filterCredentialBox.getValue();
            String currentCoverage = filterCoverageBox.getValue();

            // Clear and add default options
            filterCredentialBox.getItems().clear();
            filterCredentialBox.getItems().add("All Credentials");

            filterCoverageBox.getItems().clear();
            filterCoverageBox.getItems().add("All Areas");

            // Add unique credential levels
            allTechnicians.stream()
                    .map(Technician::getCredentialLevel)
                    .filter(level -> level != null && !level.isEmpty())
                    .distinct()
                    .sorted()
                    .forEach(filterCredentialBox.getItems()::add);

            // Add unique coverage areas (zip codes)
            allTechnicians.stream()
                    .map(Technician::getZipCode)
                    .filter(zip -> zip != null && !zip.isEmpty())
                    .distinct()
                    .sorted()
                    .forEach(filterCoverageBox.getItems()::add);

            // Restore selections or set defaults
            if (currentCredential != null && filterCredentialBox.getItems().contains(currentCredential)) {
                filterCredentialBox.setValue(currentCredential);
            } else {
                filterCredentialBox.setValue("All Credentials");
            }

            if (currentCoverage != null && filterCoverageBox.getItems().contains(currentCoverage)) {
                filterCoverageBox.setValue(currentCoverage);
            } else {
                filterCoverageBox.setValue("All Areas");
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load filter options: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the status label with current filter results
     */
    private void updateStatusLabel() {
        int totalCount = technicianList.size();
        int shownCount = filteredTechnicians.size();

        if (totalCount == shownCount) {
            statusLabel.setText(String.format("%d technicians", totalCount));
        } else {
            statusLabel.setText(String.format("Showing %d of %d technicians", shownCount, totalCount));
        }
    }

    /**
     * Refresh the technician list
     */
    private void refreshTechnicianList() {
        loadTechnicians();
        loadFilterOptions();
    }

    /**
     * Handle adding a new technician
     */
    private void handleAddTechnician() {
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "Add New Technician",
                mainPane.getScene().getWindow(),
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.setMode(TechnicianFormController.Mode.ADD);
                }
        );

        // Refresh the list to show the new technician
        refreshTechnicianList();
    }

    /**
     * Handle editing a technician
     */
    private void handleEditTechnician() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "Edit Technician",
                mainPane.getScene().getWindow(),
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.setMode(TechnicianFormController.Mode.EDIT);
                    controller.loadTechnician(selectedTechnician);
                }
        );

        // Refresh the list to show the updated technician
        refreshTechnicianList();
    }

    /**
     * Handle deleting a technician
     */
    private void handleDeleteTechnician() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Delete Technician",
                "Are you sure you want to delete this technician?",
                "This will permanently delete " + selectedTechnician.getFirstName() + " " +
                        selectedTechnician.getLastName() + " (ID: " + selectedTechnician.getTechnicianId() + ").\n\n" +
                        "This action cannot be undone."
        );

        if (confirmed) {
            try {
                boolean success = technicianService.deleteTechnician(selectedTechnician.getTechnicianId());

                if (success) {
                    statusLabel.setText("Technician deleted successfully");
                    refreshTechnicianList();
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to delete technician");
                }
            } catch (IllegalStateException e) {
                // Specific exception for deleting technicians with existing service assignments
                AlertUtils.showErrorAlert(
                        "Cannot Delete Technician",
                        "This technician has existing service assignments and cannot be deleted. " +
                                "Please remove all service assignments for this technician first."
                );
            } catch (Exception e) {
                AlertUtils.showErrorAlert("Error", "Failed to delete technician: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle exporting technicians to CSV
     */
    private void handleExportTechnicians() {
        try {
            // Get the currently filtered technicians
            List<Technician> techniciansToExport = filteredTechnicians;

            // Use save dialog to get file path
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Technicians");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("technicians.csv");

            java.io.File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());

            if (file != null) {
                // Export the technicians
                com.management.util.CSVExporter.exportTechnicians(
                        techniciansToExport, file.getAbsolutePath()
                );

                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported " + techniciansToExport.size() + " technicians to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export technicians: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle viewing a technician's schedule
     */
    private void handleViewTechnicianSchedule() {
        Technician selectedTechnician = technicianTable.getSelectionModel().getSelectedItem();
        if (selectedTechnician == null) {
            return;
        }

        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_schedule.fxml",
                "Technician Schedule - " + selectedTechnician.getFirstName() + " " + selectedTechnician.getLastName(),
                mainPane.getScene().getWindow(),
                (TechnicianScheduleController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.loadTechnicianSchedule(selectedTechnician);
                }
        );
    }

    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        //TODO
    }
}