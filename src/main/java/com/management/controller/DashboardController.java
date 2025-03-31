package com.management.controller;

import com.management.controller.customer.CustomerFormController;
import com.management.controller.service.ServiceRequestFormController;
import com.management.controller.technician.TechnicianFormController;
import com.management.model.Customer;
import com.management.model.ServiceRequest;
import com.management.service.*;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the dashboard view
 */
public class DashboardController {

    // Dashboard elements
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalTechniciansLabel;
    @FXML private Label activeServiceRequestsLabel;
    @FXML private Label pendingPaymentsLabel;
    @FXML private PieChart statusChart;

    // Icons
    @FXML private ImageView clientsIcon;
    @FXML private ImageView techniciansIcon;
    @FXML private ImageView projectsIcon;
    @FXML private ImageView invoicesIcon;
    @FXML private ImageView tasksIcon;
    @FXML private ImageView hoursIcon;
    @FXML private ImageView reportsIcon;
    @FXML private ImageView settingsIcon;

    // Table elements - these may not exist in the new dashboard
    @FXML private TableView<ServiceRequest> upcomingServiceTable;
    @FXML private TableColumn<ServiceRequest, String> dateColumn;
    @FXML private TableColumn<ServiceRequest, String> customerColumn;
    @FXML private TableColumn<ServiceRequest, String> descriptionColumn;
    @FXML private TableColumn<ServiceRequest, String> locationColumn;

    // Services
    private CustomerService customerService;
    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private QuoteService quoteService;
    private PaymentService paymentService;
    private UserService userService;
    private boolean servicesInitialized = false;

    /**
     * Set the customer service
     * @param customerService The customer service
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
        checkServiceInitialization();
    }

    /**
     * Set the technician service
     * @param technicianService The technician service
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
        checkServiceInitialization();
    }

    /**
     * Set the service request service
     * @param serviceRequestService The service request service
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
        checkServiceInitialization();
    }

    /**
     * Set the quote service
     * @param quoteService The quote service
     */
    public void setQuoteService(QuoteService quoteService) {
        this.quoteService = quoteService;
        checkServiceInitialization();
    }

    /**
     * Set the payment service
     * @param paymentService The payment service
     */
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
        checkServiceInitialization();
    }

    /**
     * Set the user service
     * @param userService The user service
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
        checkServiceInitialization();
    }

    /**
     * Check if all required services are initialized, then load data
     */
    private void checkServiceInitialization() {
        if (customerService != null &&
                technicianService != null &&
                serviceRequestService != null &&
                quoteService != null &&
                paymentService != null) {
            servicesInitialized = true;

            // Only set up table columns if the table exists
            if (upcomingServiceTable != null) {
                setupTableColumns();
            }

            loadDashboardData();
        }
    }

    /**
     * Initialize the controller - this is automatically called by FXML loader
     */
    @FXML
    public void initialize() {
        // Try a direct approach with test image first
        try {
            if (clientsIcon != null) {
                // Hardcode a simple test image directly in FXML-accessible code
                String directPath = "/images/icons/user-circle-svgrepo-com.png";
                System.out.println("Trying direct icon loading from: " + directPath);

                Image testImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(directPath)));
                clientsIcon.setImage(testImage);

                System.out.println("Test image set directly. Image details: width=" +
                        testImage.getWidth() + ", height=" + testImage.getHeight());
            }
        } catch (Exception e) {
            System.err.println("Direct image loading test failed: " + e.getMessage());
            e.printStackTrace();
        }
        listAllResources();

        // Then continue with normal icon loading
        loadIcons();
    }

    private void listAllResources() {
        try {
            // Print the classpath for debugging
            System.out.println("Java Classpath:");
            String classpath = System.getProperty("java.class.path");
            String[] classpathEntries = classpath.split(System.getProperty("path.separator"));
            for (String entry : classpathEntries) {
                System.out.println("  " + entry);
            }

            // Try to list resources in the images directory
            System.out.println("\nTrying to list resources in images directory:");
            try {
                var url = getClass().getClassLoader().getResource("images");
                if (url != null) {
                    try {
                        var file = new java.io.File(url.toURI());
                        if (file.isDirectory()) {
                            for (File f : file.listFiles()) {
                                System.out.println("  " + f.getName());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("  Error listing directory: " + e.getMessage());
                    }
                } else {
                    System.out.println("  images directory not found");
                }
            } catch (Exception e) {
                System.out.println("  Error accessing images directory: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error listing resources: " + e.getMessage());
        }
    }

    /**
     * Load all the icons
     */
    private void loadIcons() {
        try {
            // Direct approach with extensive debugging
            if (clientsIcon != null) {
                try {
                    var stream = getClass().getResourceAsStream("/images/icons/user-circle-svgrepo-com.png");
                    if (stream == null) {
                        System.err.println("Resource stream is null for clients icon!");
                    } else {
                        System.out.println("Found resource for clients icon");

                        // Create image and check its properties
                        Image image = new Image(stream);
                        System.out.println("Image loaded: width=" + image.getWidth() +
                                ", height=" + image.getHeight() +
                                ", error=" + image.isError());

                        // Set the image and verify ImageView properties
                        clientsIcon.setImage(image);
                        System.out.println("ImageView properties: fitWidth=" + clientsIcon.getFitWidth() +
                                ", fitHeight=" + clientsIcon.getFitHeight() +
                                ", visible=" + clientsIcon.isVisible() +
                                ", managed=" + clientsIcon.isManaged() +
                                ", opacity=" + clientsIcon.getOpacity());

                        // Force ImageView properties if needed
                        clientsIcon.setPreserveRatio(true);
                        clientsIcon.setSmooth(true);
                        clientsIcon.setCache(true);
                        clientsIcon.setVisible(true);
                        clientsIcon.setOpacity(1.0);

                        // Get parent information
                        if (clientsIcon.getParent() != null) {
                            System.out.println("Parent: " + clientsIcon.getParent().getClass().getName() +
                                    ", visible=" + clientsIcon.getParent().isVisible());
                        } else {
                            System.out.println("No parent found for clientsIcon");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error with clients icon: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Similar approach for other icons...
            loadIconWithDebug(techniciansIcon, "/images/icons/bolt-svgrepo-com.png", "technicians");
            loadIconWithDebug(projectsIcon, "/images/icons/routing-2-svgrepo-com.png", "projects");
            loadIconWithDebug(invoicesIcon, "/images/icons/transfer-vertical-svgrepo-com.png", "invoices");
            loadIconWithDebug(tasksIcon, "/images/icons/clock-circle-svgrepo-com.png", "tasks");
            loadIconWithDebug(hoursIcon, "/images/icons/alarm-add-svgrepo-com.png", "hours");
            loadIconWithDebug(reportsIcon, "/images/icons/graph-new-up-svgrepo-com.png", "reports");
            loadIconWithDebug(settingsIcon, "/images/icons/settings-minimalistic-svgrepo-com.png", "settings");

        } catch (Exception e) {
            System.err.println("Error loading icons: " + e.getMessage());
            e.printStackTrace();

            // If loading icons fails, we'll use SVG paths as fallback
            setFallbackSVGIcons();
        }
    }

    /**
     * Helper method to load an icon with detailed debugging
     */
    private void loadIconWithDebug(ImageView imageView, String path, String iconName) {
        if (imageView == null) {
            System.out.println(iconName + " ImageView is null");
            return;
        }

        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Resource stream is null for " + iconName + " icon!");
            } else {
                System.out.println("Found resource for " + iconName + " icon");

                // Create and set the image
                Image image = new Image(stream);
                System.out.println(iconName + " image loaded: width=" + image.getWidth() +
                        ", height=" + image.getHeight() +
                        ", error=" + image.isError());

                imageView.setImage(image);

                // Force ImageView properties
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
                imageView.setVisible(true);
                imageView.setOpacity(1.0);
            }
        } catch (Exception e) {
            System.err.println("Error with " + iconName + " icon: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Load an image from the classpath
     * @param path Path to the image within the classpath
     * @return The Image object
     */
    private Image loadImage(String path) {
        try {

            // Example with debug output for one icon
            if (clientsIcon != null) {
                var stream = getClass().getResourceAsStream("/images/icons/user-circle-svgrepo-com.png");
                if (stream == null) {
                    System.err.println("Resource stream is null for clients icon!");
                } else {
                    System.out.println("Found resource for clients icon");
                    clientsIcon.setImage(new Image(stream));
                }
            }

            // Remove leading slash to use class-relative path
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // Try different class loader approaches
            var url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                return new Image(url.toExternalForm());
            } else {
                System.err.println("Could not find resource: " + path);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Could not load image from path: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set fallback SVG icons if image loading fails
     */
    private void setFallbackSVGIcons() {
        try {
            System.out.println("Using fallback SVG icons...");

            // Replace ImageViews with SVG paths
            if (clientsIcon != null && clientsIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) clientsIcon.getParent();
                parent.getChildren().remove(clientsIcon);

                // Create SVG path
                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
                path.setFill(javafx.scene.paint.Color.valueOf("#3498db"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("clients-icon");
                parent.getChildren().add(0, path);
            }

            if (techniciansIcon != null && techniciansIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) techniciansIcon.getParent();
                parent.getChildren().remove(techniciansIcon);

                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M13 7h-2v4H7v2h4v4h2v-4h4v-2h-4V7zm-1-5C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z");
                path.setFill(javafx.scene.paint.Color.valueOf("#2ecc71"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("technicians-icon");
                parent.getChildren().add(0, path);
            }

            if (projectsIcon != null && projectsIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) projectsIcon.getParent();
                parent.getChildren().remove(projectsIcon);

                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm0 4c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm6 12H6v-1.4c0-2 4-3.1 6-3.1s6 1.1 6 3.1V19z");
                path.setFill(javafx.scene.paint.Color.valueOf("#f39c12"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("projects-icon");
                parent.getChildren().add(0, path);
            }

            // Add similar code for other icons
            if (invoicesIcon != null && invoicesIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) invoicesIcon.getParent();
                parent.getChildren().remove(invoicesIcon);

                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M15 15H3v2h12v-2zm0-8H3v2h12V7zM3 13h18v-2H3v2zm0 8h18v-2H3v2zM3 3v2h18V3H3z");
                path.setFill(javafx.scene.paint.Color.valueOf("#e74c3c"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("invoices-icon");
                parent.getChildren().add(0, path);
            }

            if (tasksIcon != null && tasksIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) tasksIcon.getParent();
                parent.getChildren().remove(tasksIcon);

                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zM7 10h2v7H7zm4-3h2v10h-2zm4 6h2v4h-2z");
                path.setFill(javafx.scene.paint.Color.valueOf("#9b59b6"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("tasks-icon");
                parent.getChildren().add(0, path);
            }

            if (hoursIcon != null && hoursIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) hoursIcon.getParent();
                parent.getChildren().remove(hoursIcon);

                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z");
                path.setFill(javafx.scene.paint.Color.valueOf("#34495e"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("hours-icon");
                parent.getChildren().add(0, path);
            }

            if (reportsIcon != null && reportsIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) reportsIcon.getParent();
                parent.getChildren().remove(reportsIcon);

                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zM7 10h2v7H7zm4-3h2v10h-2zm4 6h2v4h-2z");
                path.setFill(javafx.scene.paint.Color.valueOf("#16a085"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("reports-icon");
                parent.getChildren().add(0, path);
            }

            if (settingsIcon != null && settingsIcon.getParent() != null) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) settingsIcon.getParent();
                parent.getChildren().remove(settingsIcon);

                javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
                path.setContent("M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z");
                path.setFill(javafx.scene.paint.Color.valueOf("#7f8c8d"));
                path.getStyleClass().add("metric-icon");
                path.getStyleClass().add("settings-icon");
                parent.getChildren().add(0, path);
            }

        } catch (Exception e) {
            System.err.println("Error setting fallback SVG icons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set up table columns
     */
    private void setupTableColumns() {
        // Make sure all necessary elements exist before configuring
        if (dateColumn == null || customerColumn == null ||
                descriptionColumn == null || locationColumn == null) {
            return;
        }

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

        customerColumn.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue().getCustomer();
            return new SimpleStringProperty(customer != null ? customer.getFullName() : "Unknown");
        });

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getServiceLocation()));
    }

    /**
     * Load dashboard data
     */
    public void loadDashboardData() {
        if (!servicesInitialized) {
            return; // Don't try to load data if services aren't initialized
        }

        try {
            // Load counts
            int customerCount = customerService.countCustomers();
            int technicianCount = technicianService.countTechnicians();
            int activeServiceCount = serviceRequestService.getServiceRequestsByStatus("In Progress").size();
            int pendingPaymentCount = paymentService.getPaymentsByStatus("Pending").size();

            // Update dashboard labels - check for null before setting text
            if (totalCustomersLabel != null) {
                totalCustomersLabel.setText(String.valueOf(customerCount));
            }
            if (totalTechniciansLabel != null) {
                totalTechniciansLabel.setText(String.valueOf(technicianCount));
            }
            if (activeServiceRequestsLabel != null) {
                activeServiceRequestsLabel.setText(String.valueOf(activeServiceCount));
            }
            if (pendingPaymentsLabel != null) {
                pendingPaymentsLabel.setText(String.valueOf(pendingPaymentCount));
            }

            // Load status chart data if chart exists
            if (statusChart != null) {
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

                // Get service request counts by status
                List<ServiceRequest> pendingRequests = serviceRequestService.getServiceRequestsByStatus("Pending");
                List<ServiceRequest> inProgressRequests = serviceRequestService.getServiceRequestsByStatus("In Progress");
                List<ServiceRequest> completedRequests = serviceRequestService.getServiceRequestsByStatus("Completed");
                List<ServiceRequest> cancelledRequests = serviceRequestService.getServiceRequestsByStatus("Cancelled");

                // Add data to chart
                pieChartData.add(new PieChart.Data("Pending", pendingRequests.size()));
                pieChartData.add(new PieChart.Data("In Progress", inProgressRequests.size()));
                pieChartData.add(new PieChart.Data("Completed", completedRequests.size()));
                pieChartData.add(new PieChart.Data("Cancelled", cancelledRequests.size()));

                statusChart.setData(pieChartData);
            }

            // Update table if it exists
            if (upcomingServiceTable != null) {
                // Get upcoming service requests (next 7 days)
                LocalDate today = LocalDate.now();
                LocalDate nextWeek = today.plusDays(7);
                List<ServiceRequest> upcomingServices = serviceRequestService.getServiceRequestsByDateRange(today, nextWeek);

                // Sort by date
                upcomingServices.sort((sr1, sr2) -> sr1.getServiceDate().compareTo(sr2.getServiceDate()));

                // Add to table
                upcomingServiceTable.setItems(FXCollections.observableArrayList(upcomingServices));
            }

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Failed to load dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle new customer button
     */
    @FXML
    private void handleNewCustomer() {
        Stage stage = (Stage) totalCustomersLabel.getScene().getWindow();
        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_form.fxml",
                "New Customer",
                stage,
                (CustomerFormController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.initialize();
                    controller.setMode(CustomerFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle new technician button
     */
    @FXML
    private void handleNewTechnician() {
        Stage stage = (Stage) totalCustomersLabel.getScene().getWindow();
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "New Technician",
                stage,
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.initialize();
                    controller.setMode(TechnicianFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle new service request button
     */
    @FXML
    private void handleNewServiceRequest() {
        Stage stage = (Stage) totalCustomersLabel.getScene().getWindow();
        FXMLLoaderUtil.openDialog(
                "/fxml/service/service_request_form.fxml",
                "New Service Request",
                stage,
                (ServiceRequestFormController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setCustomerService(customerService);
                    controller.initialize();
                    controller.setMode(ServiceRequestFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle generate report button
     */
    @FXML
    private void handleGenerateReport() {
        AlertUtils.showInformationAlert(
                "Generate Report",
                "This feature will be implemented in a future version."
        );
    }
}