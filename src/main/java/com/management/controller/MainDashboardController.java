package com.management.controller;

import com.management.controller.customer.CustomerFormController;
import com.management.controller.service.ServiceRequestFormController;
import com.management.controller.technician.TechnicianFormController;
import com.management.dao.implementations.*;
import com.management.dao.interfaces.*;
import com.management.model.User;
import com.management.service.*;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;
import com.management.util.GenericPageLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the main dashboard
 */
public class MainDashboardController implements DashboardController.DashboardNavigationCallback {
    // Main content areas
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private StackPane contentArea;

    // Header elements
    @FXML
    private Button createButton;

    @FXML
    private Button requestsButton;

    @FXML
    private ImageView logo_icon;

    @FXML
    private Button messagesButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button backButton;

    @FXML
    private Label breadcrumbLabel;

    @FXML
    private ScrollPane contentScrollPane;

    // Services
    private CustomerService customerService;
    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private QuoteService quoteService;
    private PaymentService paymentService;
    private UserService userService;

    // Page loader
    private GenericPageLoader pageLoader;

    // User and state
    private User currentUser;
    private String currentSection = "";
    private ScheduledExecutorService scheduler;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize services
        DatabaseService databaseService = new DatabaseService();

        CustomerDAO customerDAO = new CustomerDAOImpl(databaseService);
        TechnicianDAO technicianDAO = new TechnicianDAOImpl(databaseService);
        ServiceRequestDAO serviceRequestDAO = new ServiceRequestDAOImpl(databaseService);
        QuoteDAO quoteDAO = new QuoteDAOImpl(databaseService);
        PaymentDAO paymentDAO = new PaymentDAOImpl(databaseService);
        UserDAO userDAO = new UserDAOImpl(databaseService);

        customerService = new CustomerService(customerDAO, serviceRequestDAO);
        technicianService = new TechnicianService(technicianDAO, serviceRequestDAO);
        serviceRequestService = new ServiceRequestService(serviceRequestDAO, customerDAO, technicianDAO);
        quoteService = new QuoteService(quoteDAO, serviceRequestDAO);
        paymentService = new PaymentService(paymentDAO, serviceRequestDAO, quoteDAO);
        userService = new UserService(userDAO);

        // Initialize the page loader
        pageLoader = new GenericPageLoader(
                customerService,
                technicianService,
                serviceRequestService,
                quoteService,
                paymentService,
                userService
        );

        // Set this controller as the navigation callback for dashboard
        pageLoader.setNavigationCallback(this);

        // Configure scrolling behavior for dashboard content
        if (contentScrollPane != null) {
            contentScrollPane.setFitToWidth(true);
            contentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Add style to make the scrollbar less obtrusive
            contentScrollPane.getStyleClass().add("dashboard-scroll");
        }

        // Set up navigation handlers
        if (createButton != null) {
            createButton.setOnAction(e -> showCreateMenu());
        }

        if (requestsButton != null) {
            requestsButton.setOnAction(e -> showServiceRequests());
        }

        if (messagesButton != null) {
            messagesButton.setOnAction(e -> showMessages());
        }

        if (profileButton != null) {
            profileButton.setOnAction(e -> openUserProfile());
        }

        if (backButton != null) {
            backButton.setOnAction(e -> navigateBack());
        }

        loadLogoIcon();

        // Default to dashboard view
        showDashboard();
    }

    //grasshopper-svgrepo-com.png
    /**
     * Loads the company logo icon into the logo_icon ImageView.
     */
    private void loadLogoIcon() {
        // Check if the ImageView is initialized
        if (logo_icon == null) {
            System.out.println("logo_icon ImageView is null");
            return;
        }

        try {
            // Define the path to the company logo image in resources
            String path = "/images/icons/grasshopper-svgrepo-com.png"; // Adjust this path based on your actual file location
            var stream = getClass().getResourceAsStream(path);

            if (stream == null) {
                System.err.println("Resource stream is null for logo icon at path: " + path);
                setFallbackLogo();
            } else {
                Image image = new Image(stream);
                if (image.isError()) {
                    System.err.println("Error loading logo image from " + path);
                    setFallbackLogo();
                } else {
                    logo_icon.setImage(image);
                    // Configure ImageView properties for optimal display
                    logo_icon.setPreserveRatio(true);
                    logo_icon.setSmooth(true);
                    logo_icon.setCache(true);
                    logo_icon.setVisible(true);
                    logo_icon.setOpacity(1.0);
                    System.out.println("Company logo loaded successfully: width=" + image.getWidth() +
                            ", height=" + image.getHeight());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading logo icon: " + e.getMessage());
            e.printStackTrace();
            setFallbackLogo();
        }
    }

    /**
     * Sets a fallback logo if the primary logo fails to load.
     */
    private void setFallbackLogo() {
        try {
            // Attempt to load a default logo image
            String defaultPath = "/images/icons/grasshopper-svgrepo-com.png"; // Adjust this path as needed
            var defaultStream = getClass().getResourceAsStream(defaultPath);
            if (defaultStream != null) {
                Image defaultImage = new Image(defaultStream);
                logo_icon.setImage(defaultImage);
                System.out.println("Fallback logo loaded from " + defaultPath);
            } else {
                System.err.println("Default logo not found at " + defaultPath);
                // Use an SVG path as a last resort
                if (logo_icon.getParent() instanceof javafx.scene.layout.Pane) {
                    javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) logo_icon.getParent();
                    parent.getChildren().remove(logo_icon);

                    javafx.scene.shape.SVGPath svgPath = new javafx.scene.shape.SVGPath();
                    svgPath.setContent("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"); // Simple house icon SVG
                    svgPath.setFill(javafx.scene.paint.Color.valueOf("#3498db")); // Blue color
                    parent.getChildren().add(svgPath);
                    System.out.println("Fallback SVG logo set");
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting fallback logo: " + e.getMessage());
        }
    }
    /**
     * Initialize the user
     * @param user The logged-in user
     */
    public void initUser(User user) {
        this.currentUser = user;
        if (profileButton != null) {
            String initial = user.getUsername().substring(0, 1).toUpperCase();
            profileButton.setText(initial);
        }
    }

    /**
     * Show the dashboard
     */
    private void showDashboard() {
        if (currentSection.equals("dashboard")) return;
        currentSection = "dashboard";

        if (pageLoader.loadDashboardPage(contentArea)) {
            updateBreadcrumb("Home > Dashboard");
        }
    }

    /**
     * Show the customers section
     */
    private void showCustomers() {
        if (currentSection.equals("customers")) return;
        currentSection = "customers";

        if (pageLoader.loadCustomersPage(contentArea)) {
            updateBreadcrumb("Home > Customers");
        }
    }

    /**
     * Show the technicians section
     */
    private void showTechnicians() {
        if (currentSection.equals("technicians")) return;
        currentSection = "technicians";

        if (pageLoader.loadTechniciansPage(contentArea)) {
            updateBreadcrumb("Home > Technicians");
        }
    }

    /**
     * Show the service requests section
     */
    private void showServiceRequests() {
        if (currentSection.equals("serviceRequests")) return;
        currentSection = "serviceRequests";

        if (pageLoader.loadServiceRequestsPage(contentArea)) {
            updateBreadcrumb("Home > Service Requests");
        }
    }

    /**
     * Show the quotes section
     */
    private void showQuotes() {
        if (currentSection.equals("quotes")) return;
        currentSection = "quotes";

        if (pageLoader.loadQuotesPage(contentArea)) {
            updateBreadcrumb("Home > Quotes");
        }
    }

    /**
     * Show the payments section
     */
    private void showPayments() {
        if (currentSection.equals("payments")) return;
        currentSection = "payments";

        if (pageLoader.loadPaymentsPage(contentArea)) {
            updateBreadcrumb("Home > Payments");
        }
    }

    /**
     * Show the reports section
     */
    private void showReports() {
        if (currentSection.equals("reports")) return;
        currentSection = "reports";

        if (pageLoader.loadReportsPage(contentArea)) {
            updateBreadcrumb("Home > Reports");
        }
    }

    /**
     * Show the settings section
     */
    private void showSettings() {
        if (currentSection.equals("settings")) return;
        currentSection = "settings";

        if (pageLoader.loadSettingsPage(contentArea)) {
            updateBreadcrumb("Home > Settings");
        }
    }

    // Dashboard navigation callback implementations
    @Override
    public void navigateToCustomers() {
        showCustomers();
    }

    @Override
    public void navigateToTechnicians() {
        showTechnicians();
    }

    @Override
    public void navigateToServiceRequests() {
        showServiceRequests();
    }

    @Override
    public void navigateToPayments() {
        showPayments();
    }

    @Override
    public void navigateToReports() {
        showReports();
    }

    @Override
    public void navigateToSettings() {
        showSettings();
    }

    /**
     * Show messages
     */
    private void showMessages() {
        AlertUtils.showInformationAlert(
                "Messages",
                "Messages feature will be implemented in a future version."
        );
    }

    /**
     * Show create menu
     */
    private void showCreateMenu() {
        Object[] options = {
                "New Customer",
                "New Technician",
                "New Service Request",
                "New Quote",
                "New Payment"
        };

        int choice = AlertUtils.showChoiceDialog(
                "Create New",
                "Select an item to create:",
                "Choose an option below:",
                options
        );

        if (choice >= 0) {
            switch (choice) {
                case 0:
                    handleNewCustomer();
                    break;
                case 1:
                    handleNewTechnician();
                    break;
                case 2:
                    handleNewServiceRequest();
                    break;
                case 3:
                    handleNewQuote();
                    break;
                case 4:
                    handleNewPayment();
                    break;
            }
        }
    }

    /**
     * Navigate back
     */
    private void navigateBack() {
        // Simply go to dashboard for now
        showDashboard();
    }

    /**
     * Update breadcrumb text
     */
    private void updateBreadcrumb(String text) {
        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText(text);
        }
    }

    /**
     * Open the user profile modal
     */
    @FXML
    private void openUserProfile() {
        FXMLLoaderUtil.openDialog(
                "/fxml/settings/user_profile.fxml",
                "User Profile",
                mainBorderPane.getScene().getWindow(),
                (UserProfileController controller) -> {
                    controller.setUserService(userService);
                    controller.setCurrentUser(currentUser);
                    controller.initialize();
                }
        );
    }

    /**
     * Open the about dialog
     */
    @FXML
    private void openAbout() {
        AlertUtils.showInformationAlert(
                "About Client Management System",
                "Client Management System v1.0.0\n\n" +
                        "A comprehensive system for managing clients, technicians, service requests, quotes, and payments."
        );
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        boolean confirmed = AlertUtils.showConfirmationAlert(
                "Logout",
                "Are you sure you want to logout?",
                "You will be returned to the login screen."
        );

        if (confirmed) {
            // Clean up resources
            shutdown();

            // Use FXMLLoaderUtil to change scene to login
            boolean success = FXMLLoaderUtil.changeScene(
                    (Stage) mainBorderPane.getScene().getWindow(),
                    "/fxml/login.fxml",
                    "Client Management System - Login"
            );

            if (success) {
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                stage.setMaximized(false);
                stage.setResizable(false);
                stage.centerOnScreen();
            }
        }
    }

    /**
     * Stop the clock when the window is closed
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Handle new customer button
     */
    private void handleNewCustomer() {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
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
    private void handleNewTechnician() {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
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
    private void handleNewServiceRequest() {
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
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
     * Handle new quote
     */
    private void handleNewQuote() {
        AlertUtils.showInformationAlert(
                "New Quote",
                "The New Quote feature will be implemented in a future version."
        );
    }

    /**
     * Handle new payment
     */
    private void handleNewPayment() {
        AlertUtils.showInformationAlert(
                "New Payment",
                "The New Payment feature will be implemented in a future version."
        );
    }
}