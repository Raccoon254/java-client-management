package com.management.controller.technician;

import com.management.controller.service.ServiceRequestDetailsController;
import com.management.model.ServiceRequest;
import com.management.model.Technician;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.CSVExporter;
import com.management.util.DateTimeUtils;
import com.management.util.FXMLLoaderUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Controller for the technician schedule view
 */
public class TechnicianScheduleController {

    @FXML
    private Label technicianNameLabel;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<String> viewTypeComboBox;

    @FXML
    private TableView<ServiceRequest> scheduleTable;

    @FXML
    private TableColumn<ServiceRequest, String> dateColumn;

    @FXML
    private TableColumn<ServiceRequest, String> timeColumn;

    @FXML
    private TableColumn<ServiceRequest, String> customerColumn;

    @FXML
    private TableColumn<ServiceRequest, String> locationColumn;

    @FXML
    private TableColumn<ServiceRequest, String> descriptionColumn;

    @FXML
    private TableColumn<ServiceRequest, String> statusColumn;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button printButton;

    @FXML
    private Button closeButton;

    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private Technician technician;
    private ObservableList<ServiceRequest> serviceRequests = FXCollections.observableArrayList();

    // View types
    private enum ViewType {
        WEEK("Week View"),
        MONTH("Month View"),
        ALL("All Appointments");

        private final String displayName;

        ViewType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private ViewType currentViewType = ViewType.WEEK;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

        timeColumn.setCellValueFactory(cellData -> {
            ServiceRequest sr = cellData.getValue();
            StringBuilder time = new StringBuilder();
            if (sr.getStartTime() != null) {
                time.append(sr.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                if (sr.getEndTime() != null) {
                    time.append(" - ").append(sr.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            } else {
                time.append("All Day");
            }
            return new SimpleStringProperty(time.toString());
        });

        customerColumn.setCellValueFactory(cellData -> {
            ServiceRequest sr = cellData.getValue();
            if (sr.getCustomer() != null) {
                return new SimpleStringProperty(sr.getCustomer().getFirstName() + " " + sr.getCustomer().getLastName());
            }
            return new SimpleStringProperty("Unknown");
        });

        locationColumn.setCellValueFactory(cellData -> {
            ServiceRequest sr = cellData.getValue();
            String location = sr.getServiceLocation();
            return new SimpleStringProperty(location != null ? location : "N/A");
        });

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize view type combo box
        viewTypeComboBox.getItems().add(ViewType.WEEK.toString());
        viewTypeComboBox.getItems().add(ViewType.MONTH.toString());
        viewTypeComboBox.getItems().add(ViewType.ALL.toString());
        viewTypeComboBox.setValue(ViewType.WEEK.toString());

        // Set up date pickers
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        endDatePicker.setValue(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));

        // Set up event handlers
        viewTypeComboBox.setOnAction(e -> handleViewTypeChange());
        startDatePicker.setOnAction(e -> validateDateRange());
        endDatePicker.setOnAction(e -> validateDateRange());

        previousButton.setOnAction(e -> navigatePrevious());
        nextButton.setOnAction(e -> navigateNext());
        refreshButton.setOnAction(e -> refreshSchedule());
        exportButton.setOnAction(e -> handleExportSchedule());
        printButton.setOnAction(e -> handlePrintSchedule());
        closeButton.setOnAction(e -> handleClose());

        // Set double-click handler for service requests
        scheduleTable.setRowFactory(tv -> {
            TableRow<ServiceRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewServiceRequestDetails(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * Set the technician service
     * @param technicianService The technician service to use
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service to use
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Load technician schedule
     * @param technician The technician to load the schedule for
     */
    public void loadTechnicianSchedule(Technician technician) {
        this.technician = technician;

        // Set technician name in title
        technicianNameLabel.setText(technician.getFirstName() + " " + technician.getLastName());

        // Load the schedule
        refreshSchedule();
    }

    /**
     * Handle view type change
     */
    private void handleViewTypeChange() {
        String viewTypeStr = viewTypeComboBox.getValue();

        if (ViewType.WEEK.toString().equals(viewTypeStr)) {
            currentViewType = ViewType.WEEK;
            LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            startDatePicker.setValue(monday);
            endDatePicker.setValue(monday.plusDays(6));
        } else if (ViewType.MONTH.toString().equals(viewTypeStr)) {
            currentViewType = ViewType.MONTH;
            LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
            startDatePicker.setValue(firstOfMonth);
            endDatePicker.setValue(firstOfMonth.plusMonths(1).minusDays(1));
        } else {
            currentViewType = ViewType.ALL;
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
        }

        refreshSchedule();
    }

    /**
     * Validate date range
     */
    private void validateDateRange() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
                endDatePicker.setValue(startDatePicker.getValue());
            }
        }

        refreshSchedule();
    }

    /**
     * Navigate to previous period
     */
    private void navigatePrevious() {
        if (currentViewType == ViewType.WEEK) {
            LocalDate newStart = startDatePicker.getValue().minusWeeks(1);
            startDatePicker.setValue(newStart);
            endDatePicker.setValue(newStart.plusDays(6));
        } else if (currentViewType == ViewType.MONTH) {
            LocalDate newStart = startDatePicker.getValue().minusMonths(1);
            startDatePicker.setValue(newStart);
            endDatePicker.setValue(newStart.plusMonths(1).minusDays(1));
        }

        refreshSchedule();
    }

    /**
     * Navigate to next period
     */
    private void navigateNext() {
        if (currentViewType == ViewType.WEEK) {
            LocalDate newStart = startDatePicker.getValue().plusWeeks(1);
            startDatePicker.setValue(newStart);
            endDatePicker.setValue(newStart.plusDays(6));
        } else if (currentViewType == ViewType.MONTH) {
            LocalDate newStart = startDatePicker.getValue().plusMonths(1);
            startDatePicker.setValue(newStart);
            endDatePicker.setValue(newStart.plusMonths(1).minusDays(1));
        }

        refreshSchedule();
    }

    /**
     * Refresh the schedule
     */
    private void refreshSchedule() {
        try {
            serviceRequests.clear();

            if (technician == null) {
                return;
            }

            List<ServiceRequest> requests = technicianService.getTechnicianServiceRequests(technician.getTechnicianId());

            // Filter by date range if applicable
            if (currentViewType != ViewType.ALL) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();

                if (startDate != null && endDate != null) {
                    requests = requests.stream()
                            .filter(sr -> !sr.getServiceDate().isBefore(startDate) && !sr.getServiceDate().isAfter(endDate))
                            .collect(Collectors.toList());
                }
            }

            // Sort by date and time
            requests.sort((sr1, sr2) -> {
                int dateCompare = sr1.getServiceDate().compareTo(sr2.getServiceDate());
                if (dateCompare != 0) {
                    return dateCompare;
                }

                if (sr1.getStartTime() != null && sr2.getStartTime() != null) {
                    return sr1.getStartTime().compareTo(sr2.getStartTime());
                }

                return 0;
            });

            // Update table
            serviceRequests.addAll(requests);
            scheduleTable.setItems(serviceRequests);

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * View details of a service request
     * @param serviceRequest The service request to view
     */
    private void viewServiceRequestDetails(ServiceRequest serviceRequest) {
        if (serviceRequest == null) return;

        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_details.fxml",
                "Service Request Details",
                scheduleTable.getScene().getWindow(),
                (ServiceRequestDetailsController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.loadServiceRequestDetails(serviceRequest.getJobId());
                }
        );
    }

    /**
     * Handle exporting the schedule to CSV
     */
    private void handleExportSchedule() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Schedule");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            // Create file name with date range
            StringBuilder fileName = new StringBuilder("schedule_");
            fileName.append(technician.getLastName().toLowerCase()).append("_");

            if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                fileName.append(startDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                        .append("_to_")
                        .append(endDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            } else {
                fileName.append("all");
            }

            fileChooser.setInitialFileName(fileName.toString() + ".csv");

            File file = fileChooser.showSaveDialog(scheduleTable.getScene().getWindow());

            if (file != null) {
                CSVExporter.exportTechnicianSchedule(
                        technician,
                        serviceRequests,
                        file.getAbsolutePath()
                );

                AlertUtils.showInformationAlert(
                        "Export Successful",
                        "Successfully exported schedule to " + file.getName()
                );
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Export Error", "Failed to export schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle printing the schedule
     */
    private void handlePrintSchedule() {
        try {
            // TODO: Implement printing functionality
            AlertUtils.showInformationAlert(
                    "Print Schedule",
                    "This feature is not yet implemented"
            );
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to print schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle closing the dialog
     */
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}