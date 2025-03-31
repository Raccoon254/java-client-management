package com.management.controller;

import com.management.controller.customer.CustomerFormController;
import com.management.controller.service.ServiceRequestFormController;
import com.management.controller.technician.TechnicianFormController;
import com.management.model.Customer;
import com.management.model.Payment;
import com.management.model.ServiceRequest;
import com.management.model.Technician;
import com.management.service.*;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the dashboard view
 */
public class DashboardController {

    // Dashboard elements
    @FXML private Label welcomeLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalTechniciansLabel;
    @FXML private Label activeServiceRequestsLabel;
    @FXML private Label pendingPaymentsLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label hoursLoggedLabel;

    // Charts
    @FXML private PieChart statusChart;

    @FXML private AreaChart<String, Number> earningsChart;
    @FXML private CategoryAxis earningsXAxis;
    @FXML private NumberAxis earningsYAxis;

    @FXML private BarChart<String, Number> technicianChart;
    @FXML private CategoryAxis technicianXAxis;
    @FXML private NumberAxis technicianYAxis;

    @FXML private BarChart<String, Number> monthlyTrendsChart;
    @FXML private CategoryAxis monthlyTrendsXAxis;
    @FXML private NumberAxis monthlyTrendsYAxis;

    // Icons
    @FXML private ImageView clientsIcon;
    @FXML private ImageView techniciansIcon;
    @FXML private ImageView projectsIcon;
    @FXML private ImageView invoicesIcon;
    @FXML private ImageView tasksIcon;
    @FXML private ImageView hoursIcon;
    @FXML private ImageView reportsIcon;
    @FXML private ImageView settingsIcon;

    // Services
    private CustomerService customerService;
    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private QuoteService quoteService;
    private PaymentService paymentService;
    private UserService userService;
    private boolean servicesInitialized = false;

    private DashboardNavigationCallback navigationCallback;

    public interface DashboardNavigationCallback {
        void navigateToCustomers();
        void navigateToTechnicians();
        void navigateToServiceRequests();
        void navigateToPayments();
        void navigateToReports();
        void navigateToSettings();
    }

    public void setNavigationCallback(DashboardNavigationCallback callback) {
        this.navigationCallback = callback;
    }

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
        checkServiceInitialization();
    }

    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
        checkServiceInitialization();
    }

    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
        checkServiceInitialization();
    }

    public void setQuoteService(QuoteService quoteService) {
        this.quoteService = quoteService;
        checkServiceInitialization();
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
        checkServiceInitialization();
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
        checkServiceInitialization();
    }

    private void checkServiceInitialization() {
        if (customerService != null &&
                technicianService != null &&
                serviceRequestService != null &&
                quoteService != null &&
                paymentService != null) {
            servicesInitialized = true;
            loadDashboardData();
        }
    }

    @FXML
    public void initialize() {
        setWelcomeGreeting();

        initializeCharts();
        loadIcons();
    }

    private void setWelcomeGreeting() {
        if (welcomeLabel == null) return;

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        String greeting;

        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        welcomeLabel.setText(greeting + ", Admin!");
    }

    private void initializeCharts() {
        if (earningsChart != null) {
            earningsChart.setTitle("");
            earningsChart.setAnimated(false);
            earningsYAxis.setForceZeroInRange(true);
            earningsYAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(earningsYAxis, "$", null));
        }

        if (technicianChart != null) {
            technicianChart.setTitle("");
            technicianChart.setAnimated(false);
            technicianYAxis.setForceZeroInRange(true);
        }

        if (monthlyTrendsChart != null) {
            monthlyTrendsChart.setTitle("");
            monthlyTrendsChart.setAnimated(false);
            monthlyTrendsYAxis.setForceZeroInRange(true);
        }

        if (statusChart != null) {
            statusChart.setTitle("");
            statusChart.setAnimated(false);
            statusChart.setLabelsVisible(true);
        }
    }

    /**
     * Load all the icons
     */
    private void loadIcons() {
        loadIconWithDebug(clientsIcon, "/images/icons/user-circle-svgrepo-com.png", "clients");
        loadIconWithDebug(techniciansIcon, "/images/icons/bolt-svgrepo-com.png", "technicians");
        loadIconWithDebug(projectsIcon, "/images/icons/routing-2-svgrepo-com.png", "projects");
        loadIconWithDebug(invoicesIcon, "/images/icons/transfer-vertical-svgrepo-com.png", "invoices");
        loadIconWithDebug(tasksIcon, "/images/icons/clock-circle-svgrepo-com.png", "tasks");
        loadIconWithDebug(hoursIcon, "/images/icons/alarm-add-svgrepo-com.png", "hours");
        loadIconWithDebug(reportsIcon, "/images/icons/graph-new-up-svgrepo-com.png", "reports");
        loadIconWithDebug(settingsIcon, "/images/icons/settings-minimalistic-svgrepo-com.png", "settings");
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
        }
    }

    public void loadDashboardData() {
        if (!servicesInitialized) {
            System.out.println("Services not initialized yet, skipping data load");
            return;
        }

        try {
            // Use Platform.runLater to ensure UI updates happen on JavaFX thread
            Platform.runLater(() -> {
                System.out.println("Loading dashboard data...");

                // Load counts and metrics
                loadMetricsData();

                // Load charts
                loadStatusPieChart();
                loadEarningsChart();
                loadTechnicianPerformanceChart();
                loadMonthlyTrendsChart();

                System.out.println("Dashboard data loaded successfully");
            });
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showErrorAlert("Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void loadMetricsData() {
        try {
            // Main metrics
            int customerCount = customerService.countCustomers();
            int technicianCount = technicianService.countTechnicians();
            int activeServiceCount = serviceRequestService.getServiceRequestsByStatus("In Progress").size();
            int pendingServiceCount = serviceRequestService.getServiceRequestsByStatus("Pending").size();
            int pendingPaymentCount = paymentService.getPaymentsByStatus("Pending").size();

            // Calculate total pending tasks (pending service requests + pending tasks for technicians)
            int pendingTasks = pendingServiceCount;

            // Calculate total hours logged (estimate based on completed service requests)
            List<ServiceRequest> completedRequests = serviceRequestService.getServiceRequestsByStatus("Completed");
            double totalHours = 0.0;
            for (ServiceRequest request : completedRequests) {
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    totalHours += request.getStartTime().until(request.getEndTime(), java.time.temporal.ChronoUnit.MINUTES) / 60.0;
                } else {
                    // If no specific times, assume 2 hours per completed request
                    totalHours += 2.0;
                }
            }

            // Format the hours
            int hours = (int) totalHours;
            int minutes = (int) ((totalHours - hours) * 60);
            String hoursFormatted = hours + " hrs " + minutes + " mins";

            // Update dashboard labels
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
            if (pendingTasksLabel != null) {
                pendingTasksLabel.setText(String.valueOf(pendingTasks));
            }
            if (hoursLoggedLabel != null) {
                hoursLoggedLabel.setText(hoursFormatted);
            }
        } catch (Exception e) {
            System.err.println("Error loading metrics data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStatusPieChart() {
        if (statusChart == null) return;

        try {
            // Get service request counts by status
            List<ServiceRequest> pendingRequests = serviceRequestService.getServiceRequestsByStatus("Pending");
            List<ServiceRequest> inProgressRequests = serviceRequestService.getServiceRequestsByStatus("In Progress");
            List<ServiceRequest> completedRequests = serviceRequestService.getServiceRequestsByStatus("Completed");
            List<ServiceRequest> cancelledRequests = serviceRequestService.getServiceRequestsByStatus("Cancelled");

            // Create data for chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            if (pendingRequests.size() > 0) {
                pieChartData.add(new PieChart.Data("Pending", pendingRequests.size()));
            }

            if (inProgressRequests.size() > 0) {
                pieChartData.add(new PieChart.Data("In Progress", inProgressRequests.size()));
            }

            if (completedRequests.size() > 0) {
                pieChartData.add(new PieChart.Data("Completed", completedRequests.size()));
            }

            if (cancelledRequests.size() > 0) {
                pieChartData.add(new PieChart.Data("Cancelled", cancelledRequests.size()));
            }

            statusChart.setData(pieChartData);

            // Add percentage labels
            int total = pendingRequests.size() + inProgressRequests.size() +
                    completedRequests.size() + cancelledRequests.size();

            for (final PieChart.Data data : statusChart.getData()) {
                double percentage = data.getPieValue() / total * 100;
                String text = String.format("%.1f%%", percentage);

                javafx.scene.Node node = data.getNode();
                if (node != null) {
                    node.setOnMouseEntered(e -> {
                        node.setStyle("-fx-border-color: white; -fx-border-width: 2; -fx-border-style: solid;");
                    });

                    node.setOnMouseExited(e -> {
                        node.setStyle("");
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading status pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEarningsChart() {
        if (earningsChart == null) return;

        try {
            // Clear existing data
            earningsChart.getData().clear();

            // Get completed payments for the last 30 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            List<Payment> recentPayments = paymentService.getPaymentsByDateRange(startDate, endDate);

            // Group payments by date
            Map<LocalDate, Double> dailyPayments = new TreeMap<>();

            // Initialize all dates in the range
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dailyPayments.put(current, 0.0);
                current = current.plusDays(1);
            }

            // Sum payments by date
            for (Payment payment : recentPayments) {
                if ("Completed".equals(payment.getStatus()) && payment.getPaymentDate() != null) {
                    LocalDate date = payment.getPaymentDate();
                    dailyPayments.put(date, dailyPayments.getOrDefault(date, 0.0) + payment.getAmount());
                }
            }

            // Create chart series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Revenue");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
            dailyPayments.forEach((date, amount) -> {
                series.getData().add(new XYChart.Data<>(date.format(formatter), amount));
            });

            // Add series to chart
            earningsChart.getData().add(series);

            // Apply CSS to make the area look nicer
            series.getNode().setStyle("-fx-stroke: #2980b9; -fx-stroke-width: 2px;");

            // Set fill for area
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-background-color: #3498db, white;");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading earnings chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTechnicianPerformanceChart() {
        if (technicianChart == null) return;

        try {
            // Clear existing data
            technicianChart.getData().clear();

            // Get all completed service requests
            List<ServiceRequest> completedRequests = serviceRequestService.getServiceRequestsByStatus("Completed");

            // Count service requests by technician
            Map<Integer, Integer> technicianCounts = new HashMap<>();
            Map<Integer, String> technicianNames = new HashMap<>();

            for (ServiceRequest request : completedRequests) {
                List<Technician> technicians = request.getTechnicians();
                for (Technician technician : technicians) {
                    int techId = technician.getTechnicianId();
                    technicianCounts.put(techId, technicianCounts.getOrDefault(techId, 0) + 1);
                    technicianNames.put(techId, technician.getLastName() + ", " + technician.getFirstName());
                }
            }

            // Create chart series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Completed Tasks");

            // Sort technicians by number of completed tasks (descending) and limit to top 5
            technicianCounts.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        String techName = technicianNames.get(entry.getKey());
                        series.getData().add(new XYChart.Data<>(techName, entry.getValue()));
                    });

            // Add series to chart
            technicianChart.getData().add(series);

            // Set bar colors
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #2ecc71;");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading technician performance chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMonthlyTrendsChart() {
        if (monthlyTrendsChart == null) return;

        try {
            // Clear existing data
            monthlyTrendsChart.getData().clear();

            // Get current year and all service requests
            int currentYear = LocalDate.now().getYear();
            List<ServiceRequest> allRequests = serviceRequestService.getAllServiceRequests();

            // Group by month and status
            Map<Month, Map<String, Integer>> monthlyData = new TreeMap<>();

            // Initialize all months
            for (Month month : Month.values()) {
                Map<String, Integer> statusCounts = new HashMap<>();
                statusCounts.put("Pending", 0);
                statusCounts.put("In Progress", 0);
                statusCounts.put("Completed", 0);
                statusCounts.put("Cancelled", 0);
                monthlyData.put(month, statusCounts);
            }

            // Count requests by month and status
            for (ServiceRequest request : allRequests) {
                if (request.getServiceDate() != null && request.getServiceDate().getYear() == currentYear) {
                    Month month = request.getServiceDate().getMonth();
                    String status = request.getStatus() != null ? request.getStatus() : "Pending";

                    Map<String, Integer> statusCounts = monthlyData.get(month);
                    statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
                }
            }

            // Create series for each status
            XYChart.Series<String, Number> pendingSeries = new XYChart.Series<>();
            pendingSeries.setName("Pending");

            XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
            inProgressSeries.setName("In Progress");

            XYChart.Series<String, Number> completedSeries = new XYChart.Series<>();
            completedSeries.setName("Completed");

            // Add last 6 months of data
            Month currentMonth = LocalDate.now().getMonth();
            for (int i = 5; i >= 0; i--) {
                Month month = currentMonth.minus(i);
                Map<String, Integer> statusCounts = monthlyData.get(month);

                String monthName = month.getDisplayName(TextStyle.SHORT, Locale.getDefault());

                pendingSeries.getData().add(new XYChart.Data<>(monthName, statusCounts.get("Pending")));
                inProgressSeries.getData().add(new XYChart.Data<>(monthName, statusCounts.get("In Progress")));
                completedSeries.getData().add(new XYChart.Data<>(monthName, statusCounts.get("Completed")));
            }

            // Add series to chart
            monthlyTrendsChart.getData().addAll(pendingSeries, inProgressSeries, completedSeries);

            // Apply CSS to each series
            pendingSeries.getNode().setStyle("-fx-stroke: #f39c12;");
            for (XYChart.Data<String, Number> data : pendingSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #f39c12;");
                }
            }

            inProgressSeries.getNode().setStyle("-fx-stroke: #3498db;");
            for (XYChart.Data<String, Number> data : inProgressSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #3498db;");
                }
            }

            completedSeries.getNode().setStyle("-fx-stroke: #2ecc71;");
            for (XYChart.Data<String, Number> data : completedSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #2ecc71;");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading monthly trends chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void openCustomers(MouseEvent event) {
        System.out.println("Customers card clicked");
        if (navigationCallback != null) {
            navigationCallback.navigateToCustomers();
        } else {
            System.err.println("Navigation callback not set for customers");
        }
    }

    @FXML
    public void openTechnicians(MouseEvent event) {
        System.out.println("Technicians card clicked");
        if (navigationCallback != null) {
            navigationCallback.navigateToTechnicians();
        } else {
            System.err.println("Navigation callback not set for technicians");
        }
    }

    @FXML
    public void openServiceRequests(MouseEvent event) {
        System.out.println("Projects/Service Requests card clicked");
        if (navigationCallback != null) {
            navigationCallback.navigateToServiceRequests();
        } else {
            System.err.println("Navigation callback not set for service requests");
        }
    }

    @FXML
    public void openPayments(MouseEvent event) {
        System.out.println("Invoices/Payments card clicked");
        if (navigationCallback != null) {
            navigationCallback.navigateToPayments();
        } else {
            System.err.println("Navigation callback not set for payments");
        }
    }

    @FXML
    public void openReports(MouseEvent event) {
        System.out.println("Reports card clicked");
        if (navigationCallback != null) {
            navigationCallback.navigateToReports();
        } else {
            System.err.println("Navigation callback not set for reports");
        }
    }

    @FXML
    public void openSettings(MouseEvent event) {
        System.out.println("Settings card clicked");
        if (navigationCallback != null) {
            navigationCallback.navigateToSettings();
        } else {
            System.err.println("Navigation callback not set for settings");
        }
    }

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

    @FXML
    private void handleGenerateReport() {
        AlertUtils.showInformationAlert(
                "Generate Report",
                "This feature will be implemented in a future version."
        );
    }

    public void refreshData() {
        if (servicesInitialized) {
            loadDashboardData();
        }
    }
}