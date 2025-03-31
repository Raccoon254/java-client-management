package com.management.controller;

import com.management.model.*;
import com.management.service.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Reports view
 */
public class ReportsController {

    // Services
    private CustomerService customerService;
    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private QuoteService quoteService;
    private PaymentService paymentService;

    // FXML Components - Date Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button applyDateFilterButton;
    @FXML private Button resetFilterButton;

    // FXML Components - Summary Tab
    @FXML private Label totalServiceRequestsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label outstandingPaymentsLabel;
    @FXML private Label activeCustomersLabel;
    @FXML private Label activeTechniciansLabel;
    @FXML private Label completionRateLabel;
    @FXML private PieChart serviceStatusChart;
    @FXML private BarChart<String, Number> revenueChart;

    // FXML Components - Service Requests Tab
    @FXML private ComboBox<String> serviceStatusComboBox;
    @FXML private TableView<ServiceRequest> serviceRequestsTable;
    @FXML private TableColumn<ServiceRequest, Integer> serviceIdColumn;
    @FXML private TableColumn<ServiceRequest, LocalDate> serviceDateColumn;
    @FXML private TableColumn<ServiceRequest, String> serviceCustomerColumn;
    @FXML private TableColumn<ServiceRequest, String> serviceDescriptionColumn;
    @FXML private TableColumn<ServiceRequest, String> serviceStatusColumn;
    @FXML private TableColumn<ServiceRequest, Double> serviceCostColumn;

    // FXML Components - Financial Tab
    @FXML private ComboBox<String> financialReportTypeComboBox;
    @FXML private ComboBox<String> financialGroupByComboBox;
    @FXML private BarChart<String, Number> financialChart;
    @FXML private TableView<Map<String, Object>> financialTable;
    @FXML private TableColumn<Map<String, Object>, String> financialPeriodColumn;
    @FXML private TableColumn<Map<String, Object>, Double> financialRevenueColumn;
    @FXML private TableColumn<Map<String, Object>, Double> financialCostsColumn;
    @FXML private TableColumn<Map<String, Object>, Double> financialProfitColumn;
    @FXML private TableColumn<Map<String, Object>, Double> financialMarginColumn;

    // FXML Components - Customer Reports Tab
    @FXML private ComboBox<String> customerReportTypeComboBox;
    @FXML private TableView<Map<String, Object>> customerReportTable;
    @FXML private TableColumn<Map<String, Object>, String> customerNumberColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerCompanyColumn;
    @FXML private TableColumn<Map<String, Object>, Integer> customerServiceCountColumn;
    @FXML private TableColumn<Map<String, Object>, Double> customerTotalSpendingColumn;
    @FXML private TableColumn<Map<String, Object>, LocalDate> customerLastServiceColumn;

    // FXML Components - Technician Reports Tab
    @FXML private ComboBox<String> technicianReportTypeComboBox;
    @FXML private TableView<Map<String, Object>> technicianReportTable;
    @FXML private TableColumn<Map<String, Object>, Integer> technicianIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> technicianNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> technicianCredentialsColumn;
    @FXML private TableColumn<Map<String, Object>, Integer> technicianServiceCountColumn;
    @FXML private TableColumn<Map<String, Object>, Double> technicianCompletionRateColumn;
    @FXML private TableColumn<Map<String, Object>, Double> technicianWorkloadColumn;

    // FXML Components - Bottom Bar
    @FXML private Label reportStatusLabel;
    @FXML private Button printReportButton;
    @FXML private Button refreshReportButton;

    // Date ranges for filtering
    private LocalDate startDate;
    private LocalDate endDate;

    // Formatters
    private final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private final DecimalFormat percentFormat = new DecimalFormat("0.0%");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Initialize the controller
     */
    public void initialize() {
        // Set default date range (current month)
        startDate = LocalDate.now().withDayOfMonth(1);
        endDate = LocalDate.now();
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);

        // Initialize ComboBoxes
        initializeComboBoxes();

        // Initialize tables
        initializeServiceRequestTable();
        initializeFinancialTable();
        initializeCustomerReportTable();
        initializeTechnicianReportTable();

        // Load initial data
        loadReportData();
    }

    /**
     * Initialize ComboBoxes with options
     */
    private void initializeComboBoxes() {
        // Service status options
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "All", "Pending", "Scheduled", "In Progress", "Completed", "Cancelled");
        serviceStatusComboBox.setItems(statusOptions);
        serviceStatusComboBox.setValue("All");
        serviceStatusComboBox.setOnAction(e -> updateServiceRequestsTable());

        // Financial report types
        ObservableList<String> financialReportTypes = FXCollections.observableArrayList(
                "Revenue", "Profit & Loss", "Service Type Analysis");
        financialReportTypeComboBox.setItems(financialReportTypes);
        financialReportTypeComboBox.setValue("Revenue");
        financialReportTypeComboBox.setOnAction(e -> updateFinancialReport());

        // Financial grouping options
        ObservableList<String> financialGroupings = FXCollections.observableArrayList(
                "Monthly", "Quarterly", "Yearly");
        financialGroupByComboBox.setItems(financialGroupings);
        financialGroupByComboBox.setValue("Monthly");
        financialGroupByComboBox.setOnAction(e -> updateFinancialReport());

        // Customer report types
        ObservableList<String> customerReportTypes = FXCollections.observableArrayList(
                "Top Customers by Revenue", "Customer Activity", "Customer Retention");
        customerReportTypeComboBox.setItems(customerReportTypes);
        customerReportTypeComboBox.setValue("Top Customers by Revenue");
        customerReportTypeComboBox.setOnAction(e -> updateCustomerReport());

        // Technician report types
        ObservableList<String> technicianReportTypes = FXCollections.observableArrayList(
                "Technician Performance", "Technician Workload", "Technician Utilization");
        technicianReportTypeComboBox.setItems(technicianReportTypes);
        technicianReportTypeComboBox.setValue("Technician Performance");
        technicianReportTypeComboBox.setOnAction(e -> updateTechnicianReport());
    }

    /**
     * Initialize the service request table
     */
    private void initializeServiceRequestTable() {
        serviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("jobId"));
        serviceDateColumn.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        serviceCustomerColumn.setCellValueFactory(data -> {
            Customer customer = data.getValue().getCustomer();
            if (customer != null) {
                return new SimpleStringProperty(customer.getFullName());
            }
            return new SimpleStringProperty("N/A");
        });
        serviceDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        serviceStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        serviceCostColumn.setCellValueFactory(data -> {
            double totalCost = data.getValue().getTotalCost();
            return new SimpleObjectProperty<>(totalCost);
        });

        // Format currency
        serviceCostColumn.setCellFactory(col -> new TableCell<ServiceRequest, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });
    }

    /**
     * Initialize the financial table
     */
    private void initializeFinancialTable() {
        financialPeriodColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("period")));
        financialRevenueColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("revenue")));
        financialCostsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("costs")));
        financialProfitColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("profit")));
        financialMarginColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("margin")));

        // Format currency columns
        financialRevenueColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });

        financialCostsColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });

        financialProfitColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });

        financialMarginColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(percentFormat.format(amount));
                }
            }
        });
    }

    /**
     * Initialize the customer report table
     */
    private void initializeCustomerReportTable() {
        customerNumberColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("customerNumber")));
        customerNameColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("name")));
        customerCompanyColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("company")));
        customerServiceCountColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("serviceCount")));
        customerTotalSpendingColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("totalSpending")));
        customerLastServiceColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((LocalDate) data.getValue().get("lastService")));

        // Format currency columns
        customerTotalSpendingColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });

        // Format date columns
        customerLastServiceColumn.setCellFactory(col -> new TableCell<Map<String, Object>, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
    }

    /**
     * Initialize the technician report table
     */
    private void initializeTechnicianReportTable() {
        technicianIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("technicianId")));
        technicianNameColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("name")));
        technicianCredentialsColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("credentials")));
        technicianServiceCountColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Integer) data.getValue().get("serviceCount")));
        technicianCompletionRateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("completionRate")));
        technicianWorkloadColumn.setCellValueFactory(data -> new SimpleObjectProperty<>((Double) data.getValue().get("workload")));

        // Format percentage columns
        technicianCompletionRateColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(percentFormat.format(value));
                }
            }
        });
    }

    /**
     * Load all report data
     */
    private void loadReportData() {
        try {
            updateSummaryData();
            updateServiceRequestsTable();
            updateFinancialReport();
            updateCustomerReport();
            updateTechnicianReport();
            reportStatusLabel.setText("Report data loaded successfully.");
        } catch (Exception e) {
            reportStatusLabel.setText("Error loading report data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update summary data
     */
    private void updateSummaryData() {
        // Get filtered service requests
        List<ServiceRequest> serviceRequests = getFilteredServiceRequests();

        // Count service requests
        int totalRequests = serviceRequests.size();
        totalServiceRequestsLabel.setText(String.valueOf(totalRequests));

        // Calculate total revenue
        double totalRevenue = serviceRequests.stream()
                .mapToDouble(ServiceRequest::getTotalCost)
                .sum();
        totalRevenueLabel.setText(currencyFormat.format(totalRevenue));

        // Calculate outstanding payments
        double outstandingPayments = 0;
        for (ServiceRequest request : serviceRequests) {
            double remaining = paymentService.getRemainingBalance(request.getJobId());
            outstandingPayments += remaining;
        }
        outstandingPaymentsLabel.setText(currencyFormat.format(outstandingPayments));

        // Count active customers
        Set<Integer> activeCustomers = serviceRequests.stream()
                .map(ServiceRequest::getCustomerId)
                .collect(Collectors.toSet());
        activeCustomersLabel.setText(String.valueOf(activeCustomers.size()));

        // Count active technicians
        Set<Integer> activeTechnicians = new HashSet<>();
        for (ServiceRequest request : serviceRequests) {
            for (Technician tech : request.getTechnicians()) {
                activeTechnicians.add(tech.getTechnicianId());
            }
        }
        activeTechniciansLabel.setText(String.valueOf(activeTechnicians.size()));

        // Calculate completion rate
        long completedRequests = serviceRequests.stream()
                .filter(r -> "Completed".equals(r.getStatus()))
                .count();
        double completionRate = totalRequests > 0 ? (double) completedRequests / totalRequests : 0;
        completionRateLabel.setText(percentFormat.format(completionRate));

        // Update service status chart
        updateServiceStatusChart(serviceRequests);

        // Update revenue chart
        updateRevenueChart(serviceRequests);
    }

    /**
     * Update service status chart
     * @param serviceRequests List of service requests
     */
    private void updateServiceStatusChart(List<ServiceRequest> serviceRequests) {
        // Count service requests by status
        Map<String, Long> statusCounts = serviceRequests.stream()
                .collect(Collectors.groupingBy(
                        sr -> sr.getStatus() != null ? sr.getStatus() : "Unknown",
                        Collectors.counting()
                ));

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        serviceStatusChart.setData(pieChartData);
        serviceStatusChart.setTitle("Service Requests by Status");
    }

    /**
     * Update revenue chart
     * @param serviceRequests List of service requests
     */
    private void updateRevenueChart(List<ServiceRequest> serviceRequests) {
        // Group service requests by month
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();

        for (ServiceRequest request : serviceRequests) {
            LocalDate date = request.getServiceDate();
            if (date != null) {
                String monthYear = date.getMonth().toString() + " " + date.getYear();
                monthlyRevenue.merge(monthYear, request.getTotalCost(), Double::sum);
            }
        }

        // Create bar chart data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Revenue");

        for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        revenueChart.getData().clear();
        revenueChart.getData().add(series);
        revenueChart.setTitle("Monthly Revenue");
    }

    /**
     * Update service requests table
     */
    private void updateServiceRequestsTable() {
        List<ServiceRequest> filteredRequests = getFilteredServiceRequests();

        // Apply status filter if needed
        String statusFilter = serviceStatusComboBox.getValue();
        if (!"All".equals(statusFilter)) {
            filteredRequests = filteredRequests.stream()
                    .filter(r -> statusFilter.equals(r.getStatus()))
                    .collect(Collectors.toList());
        }

        serviceRequestsTable.setItems(FXCollections.observableArrayList(filteredRequests));
    }

    /**
     * Update financial report
     */
    private void updateFinancialReport() {
        String reportType = financialReportTypeComboBox.getValue();
        String groupBy = financialGroupByComboBox.getValue();

        List<ServiceRequest> serviceRequests = getFilteredServiceRequests();
        List<Map<String, Object>> reportData = new ArrayList<>();

        switch (groupBy) {
            case "Monthly":
                reportData = generateMonthlyFinancialReport(serviceRequests, reportType);
                break;
            case "Quarterly":
                reportData = generateQuarterlyFinancialReport(serviceRequests, reportType);
                break;
            case "Yearly":
                reportData = generateYearlyFinancialReport(serviceRequests, reportType);
                break;
        }

        financialTable.setItems(FXCollections.observableArrayList(reportData));
        updateFinancialChart(reportData);
    }

    /**
     * Generate monthly financial report
     * @param serviceRequests List of service requests
     * @param reportType Type of report
     * @return List of report data
     */
    private List<Map<String, Object>> generateMonthlyFinancialReport(List<ServiceRequest> serviceRequests, String reportType) {
        // Group service requests by month
        Map<String, List<ServiceRequest>> groupedRequests = new LinkedHashMap<>();

        for (ServiceRequest request : serviceRequests) {
            LocalDate date = request.getServiceDate();
            if (date != null) {
                String monthYear = date.getMonth().toString() + " " + date.getYear();
                groupedRequests.computeIfAbsent(monthYear, k -> new ArrayList<>()).add(request);
            }
        }

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (Map.Entry<String, List<ServiceRequest>> entry : groupedRequests.entrySet()) {
            String period = entry.getKey();
            List<ServiceRequest> requests = entry.getValue();
            double revenue = requests.stream().mapToDouble(ServiceRequest::getTotalCost).sum();

            // For simplicity, assume costs are 60% of revenue
            double costs = revenue * 0.6;
            double profit = revenue - costs;
            double margin = revenue > 0 ? profit / revenue : 0;

            Map<String, Object> row = new HashMap<>();
            row.put("period", period);
            row.put("revenue", revenue);
            row.put("costs", costs);
            row.put("profit", profit);
            row.put("margin", margin);

            reportData.add(row);
        }

        return reportData;
    }

    /**
     * Generate quarterly financial report
     * @param serviceRequests List of service requests
     * @param reportType Type of report
     * @return List of report data
     */
    private List<Map<String, Object>> generateQuarterlyFinancialReport(List<ServiceRequest> serviceRequests, String reportType) {
        // Group service requests by quarter
        Map<String, List<ServiceRequest>> groupedRequests = new LinkedHashMap<>();

        for (ServiceRequest request : serviceRequests) {
            LocalDate date = request.getServiceDate();
            if (date != null) {
                int quarter = (date.getMonthValue() - 1) / 3 + 1;
                String quarterYear = "Q" + quarter + " " + date.getYear();
                groupedRequests.computeIfAbsent(quarterYear, k -> new ArrayList<>()).add(request);
            }
        }

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (Map.Entry<String, List<ServiceRequest>> entry : groupedRequests.entrySet()) {
            String period = entry.getKey();
            List<ServiceRequest> requests = entry.getValue();
            double revenue = requests.stream().mapToDouble(ServiceRequest::getTotalCost).sum();

            // For simplicity, assume costs are 60% of revenue
            double costs = revenue * 0.6;
            double profit = revenue - costs;
            double margin = revenue > 0 ? profit / revenue : 0;

            Map<String, Object> row = new HashMap<>();
            row.put("period", period);
            row.put("revenue", revenue);
            row.put("costs", costs);
            row.put("profit", profit);
            row.put("margin", margin);

            reportData.add(row);
        }

        return reportData;
    }

    /**
     * Generate yearly financial report
     * @param serviceRequests List of service requests
     * @param reportType Type of report
     * @return List of report data
     */
    private List<Map<String, Object>> generateYearlyFinancialReport(List<ServiceRequest> serviceRequests, String reportType) {
        // Group service requests by year
        Map<Integer, List<ServiceRequest>> groupedRequests = new TreeMap<>();

        for (ServiceRequest request : serviceRequests) {
            LocalDate date = request.getServiceDate();
            if (date != null) {
                groupedRequests.computeIfAbsent(date.getYear(), k -> new ArrayList<>()).add(request);
            }
        }

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (Map.Entry<Integer, List<ServiceRequest>> entry : groupedRequests.entrySet()) {
            String period = String.valueOf(entry.getKey());
            List<ServiceRequest> requests = entry.getValue();
            double revenue = requests.stream().mapToDouble(ServiceRequest::getTotalCost).sum();

            // For simplicity, assume costs are 60% of revenue
            double costs = revenue * 0.6;
            double profit = revenue - costs;
            double margin = revenue > 0 ? profit / revenue : 0;

            Map<String, Object> row = new HashMap<>();
            row.put("period", period);
            row.put("revenue", revenue);
            row.put("costs", costs);
            row.put("profit", profit);
            row.put("margin", margin);

            reportData.add(row);
        }

        return reportData;
    }

    /**
     * Update financial chart
     * @param reportData List of report data
     */
    private void updateFinancialChart(List<Map<String, Object>> reportData) {
        String reportType = financialReportTypeComboBox.getValue();

        // Create series for the chart
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        XYChart.Series<String, Number> costsSeries = new XYChart.Series<>();
        costsSeries.setName("Costs");

        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        profitSeries.setName("Profit");

        for (Map<String, Object> row : reportData) {
            String period = (String) row.get("period");
            double revenue = (Double) row.get("revenue");
            double costs = (Double) row.get("costs");
            double profit = (Double) row.get("profit");

            revenueSeries.getData().add(new XYChart.Data<>(period, revenue));
            costsSeries.getData().add(new XYChart.Data<>(period, costs));
            profitSeries.getData().add(new XYChart.Data<>(period, profit));
        }

        financialChart.getData().clear();

        if ("Revenue".equals(reportType)) {
            financialChart.getData().add(revenueSeries);
        } else if ("Profit & Loss".equals(reportType)) {
            financialChart.getData().add(revenueSeries);
            financialChart.getData().add(costsSeries);
            financialChart.getData().add(profitSeries);
        }

        financialChart.setTitle(reportType + " Report");
    }

    /**
     * Update customer report
     */
    private void updateCustomerReport() {
        String reportType = customerReportTypeComboBox.getValue();
        List<ServiceRequest> serviceRequests = getFilteredServiceRequests();
        List<Map<String, Object>> reportData = new ArrayList<>();

        // Get all customers
        List<Customer> allCustomers = customerService.getAllCustomers();

        // Build customer data
        Map<Integer, Map<String, Object>> customerData = new HashMap<>();

        for (Customer customer : allCustomers) {
            Map<String, Object> data = new HashMap<>();
            data.put("customerId", customer.getCustomerId());
            data.put("customerNumber", customer.getCustomerNumber());
            data.put("name", customer.getFullName());
            data.put("company", customer.getCompanyName() != null ? customer.getCompanyName() : "N/A");
            data.put("serviceCount", 0);
            data.put("totalSpending", 0.0);
            data.put("lastService", null);
            customerData.put(customer.getCustomerId(), data);
        }

        // Process service requests
        for (ServiceRequest request : serviceRequests) {
            int customerId = request.getCustomerId();
            Map<String, Object> data = customerData.get(customerId);

            if (data != null) {
                // Update service count
                data.put("serviceCount", (Integer) data.get("serviceCount") + 1);

                // Update total spending
                data.put("totalSpending", (Double) data.get("totalSpending") + request.getTotalCost());

                // Update last service date
                LocalDate currentLastService = (LocalDate) data.get("lastService");
                if (currentLastService == null || request.getServiceDate().isAfter(currentLastService)) {
                    data.put("lastService", request.getServiceDate());
                }
            }
        }

        // Create report data based on report type
        if ("Top Customers by Revenue".equals(reportType)) {
            // Sort by total spending
            reportData = customerData.values().stream()
                    .sorted(Comparator.comparing(m -> ((Double) m.get("totalSpending")), Comparator.reverseOrder()))
                    .limit(20)
                    .collect(Collectors.toList());
        } else if ("Customer Activity".equals(reportType)) {
            // Sort by service count
            reportData = customerData.values().stream()
                    .sorted(Comparator.comparing(m -> ((Integer) m.get("serviceCount")), Comparator.reverseOrder()))
                    .limit(20)
                    .collect(Collectors.toList());
        } else if ("Customer Retention".equals(reportType)) {
            // Filter to customers with at least one service
            reportData = customerData.values().stream()
                    .filter(m -> (Integer) m.get("serviceCount") > 0)
                    .sorted(Comparator.comparing(m -> ((LocalDate) m.get("lastService")), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }

        customerReportTable.setItems(FXCollections.observableArrayList(reportData));
    }

    /**
     * Update technician report
     */
    private void updateTechnicianReport() {
        String reportType = technicianReportTypeComboBox.getValue();
        List<ServiceRequest> serviceRequests = getFilteredServiceRequests();
        List<Map<String, Object>> reportData = new ArrayList<>();

        // Get all technicians
        List<Technician> allTechnicians = technicianService.getAllTechnicians();

        // Build technician data
        Map<Integer, Map<String, Object>> technicianData = new HashMap<>();

        for (Technician technician : allTechnicians) {
            Map<String, Object> data = new HashMap<>();
            data.put("technicianId", technician.getTechnicianId());
            data.put("name", technician.getFullName());
            data.put("credentials", technician.getCredentials() != null ? technician.getCredentials() : "N/A");
            data.put("serviceCount", 0);
            data.put("completedCount", 0);
            data.put("completionRate", 0.0);
            data.put("workload", 0.0);
            technicianData.put(technician.getTechnicianId(), data);
        }

        // Process service requests
        for (ServiceRequest request : serviceRequests) {
            for (Technician technician : request.getTechnicians()) {
                int technicianId = technician.getTechnicianId();
                Map<String, Object> data = technicianData.get(technicianId);

                if (data != null) {
                    // Update service count
                    data.put("serviceCount", (Integer) data.get("serviceCount") + 1);

                    // Update completed count
                    if ("Completed".equals(request.getStatus())) {
                        data.put("completedCount", (Integer) data.get("completedCount") + 1);
                    }
                }
            }
        }

        // Calculate completion rates and workload
        double totalServices = serviceRequests.size();

        for (Map<String, Object> data : technicianData.values()) {
            int serviceCount = (Integer) data.get("serviceCount");
            int completedCount = (Integer) data.get("completedCount");

            // Calculate completion rate
            double completionRate = serviceCount > 0 ? (double) completedCount / serviceCount : 0;
            data.put("completionRate", completionRate);

            // Calculate workload (percentage of all service requests)
            double workload = totalServices > 0 ? (double) serviceCount / totalServices : 0;
            data.put("workload", workload);
        }

        // Create report data based on report type
        if ("Technician Performance".equals(reportType)) {
            // Sort by completion rate
            reportData = technicianData.values().stream()
                    .filter(m -> (Integer) m.get("serviceCount") > 0)
                    .sorted(Comparator.comparing(m -> ((Double) m.get("completionRate")), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        } else if ("Technician Workload".equals(reportType)) {
            // Sort by service count
            reportData = technicianData.values().stream()
                    .sorted(Comparator.comparing(m -> ((Integer) m.get("serviceCount")), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        } else if ("Technician Utilization".equals(reportType)) {
            // Sort by workload
            reportData = technicianData.values().stream()
                    .sorted(Comparator.comparing(m -> ((Double) m.get("workload")), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }

        technicianReportTable.setItems(FXCollections.observableArrayList(reportData));
    }

    /**
     * Get filtered service requests based on date range
     * @return List of filtered service requests
     */
    private List<ServiceRequest> getFilteredServiceRequests() {
        // Get all service requests
        List<ServiceRequest> allRequests = serviceRequestService.getAllServiceRequests();

        // Filter by date range
        return allRequests.stream()
                .filter(r -> r.getServiceDate() != null &&
                        (!r.getServiceDate().isBefore(startDate) &&
                                !r.getServiceDate().isAfter(endDate)))
                .collect(Collectors.toList());
    }

    /**
     * Handle applying date filter
     * @param event The action event
     */
    @FXML
    private void handleApplyDateFilter(ActionEvent event) {
        LocalDate newStartDate = startDatePicker.getValue();
        LocalDate newEndDate = endDatePicker.getValue();

        if (newStartDate != null && newEndDate != null) {
            if (newStartDate.isAfter(newEndDate)) {
                reportStatusLabel.setText("Error: Start date cannot be after end date.");
                return;
            }

            startDate = newStartDate;
            endDate = newEndDate;
            loadReportData();
        } else {
            reportStatusLabel.setText("Error: Please select both start and end dates.");
        }
    }

    /**
     * Handle resetting filters
     * @param event The action event
     */
    @FXML
    private void handleResetFilters(ActionEvent event) {
        // Reset date pickers to current month
        LocalDate now = LocalDate.now();
        startDate = now.withDayOfMonth(1);
        endDate = now;
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);

        // Reset combo boxes
        serviceStatusComboBox.setValue("All");

        loadReportData();
    }

    /**
     * Handle exporting service report
     * @param event The action event
     */
    @FXML
    private void handleExportServiceReport(ActionEvent event) {
        exportReport("Service Requests Report", serviceRequestsTable.getItems());
    }

    /**
     * Handle exporting financial report
     * @param event The action event
     */
    @FXML
    private void handleExportFinancialReport(ActionEvent event) {
        exportReport("Financial Report", financialTable.getItems());
    }

    /**
     * Handle exporting customer report
     * @param event The action event
     */
    @FXML
    private void handleExportCustomerReport(ActionEvent event) {
        exportReport("Customer Report", customerReportTable.getItems());
    }

    /**
     * Handle exporting technician report
     * @param event The action event
     */
    @FXML
    private void handleExportTechnicianReport(ActionEvent event) {
        exportReport("Technician Report", technicianReportTable.getItems());
    }

    /**
     * Export a report to CSV
     * @param reportName Name of the report
     * @param data Data to export
     */
    private <T> void exportReport(String reportName, List<T> data) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(reportName.replaceAll("\\s+", "_") + ".csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                if (data.isEmpty()) {
                    writer.println("No data to export");
                    reportStatusLabel.setText("Report exported successfully (no data).");
                    return;
                }

                if (data.get(0) instanceof ServiceRequest) {
                    exportServiceRequestsReport(writer, (List<ServiceRequest>) data);
                } else if (data.get(0) instanceof Map) {
                    exportMapBasedReport(writer, (List<Map<String, Object>>) data);
                }

                reportStatusLabel.setText("Report exported successfully.");
            } catch (IOException e) {
                reportStatusLabel.setText("Error exporting report: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Export service requests report
     * @param writer PrintWriter
     * @param data List of service requests
     */
    private void exportServiceRequestsReport(PrintWriter writer, List<ServiceRequest> data) {
        // Write header
        writer.println("ID,Date,Customer,Description,Status,Cost");

        // Write data
        for (ServiceRequest request : data) {
            String customerName = request.getCustomer() != null ?
                    request.getCustomer().getFullName().replaceAll(",", " ") : "N/A";

            String description = request.getDescription() != null ?
                    "\"" + request.getDescription().replaceAll("\"", "\"\"") + "\"" : "";

            writer.println(
                    request.getJobId() + "," +
                            request.getServiceDate() + "," +
                            customerName + "," +
                            description + "," +
                            request.getStatus() + "," +
                            request.getTotalCost()
            );
        }
    }

    /**
     * Export map-based report
     * @param writer PrintWriter
     * @param data List of maps
     */
    private void exportMapBasedReport(PrintWriter writer, List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            writer.println("No data to export");
            return;
        }

        // Get column names from the first row
        Map<String, Object> firstRow = data.get(0);
        String header = String.join(",", firstRow.keySet());
        writer.println(header);

        // Write data
        for (Map<String, Object> row : data) {
            StringBuilder line = new StringBuilder();
            boolean first = true;

            for (Object value : row.values()) {
                if (!first) {
                    line.append(",");
                }

                if (value != null) {
                    String stringValue = value.toString();
                    if (stringValue.contains(",") || stringValue.contains("\"") || stringValue.contains("\n")) {
                        stringValue = "\"" + stringValue.replaceAll("\"", "\"\"") + "\"";
                    }
                    line.append(stringValue);
                }

                first = false;
            }

            writer.println(line);
        }
    }

    /**
     * Handle printing report
     * @param event The action event
     */
    @FXML
    private void handlePrintReport(ActionEvent event) {
        reportStatusLabel.setText("Print functionality not implemented in this version.");
    }

    /**
     * Handle refreshing report
     * @param event The action event
     */
    @FXML
    private void handleRefreshReport(ActionEvent event) {
        loadReportData();
    }

    /**
     * Set customer service
     * @param customerService The customer service
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Set technician service
     * @param technicianService The technician service
     */
    public void setTechnicianService(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    /**
     * Set service request service
     * @param serviceRequestService The service request service
     */
    public void setServiceRequestService(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    /**
     * Set quote service
     * @param quoteService The quote service
     */
    public void setQuoteService(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /**
     * Set payment service
     * @param paymentService The payment service
     */
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}