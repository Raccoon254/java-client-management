package com.management.util;

import com.management.controller.*;
import com.management.controller.customer.CustomerListController;
import com.management.controller.payment.PaymentListController;
import com.management.controller.quote.QuoteListController;
import com.management.controller.service.ServiceRequestListController;
import com.management.controller.technician.TechnicianListController;
import com.management.service.*;
import javafx.scene.layout.Pane;

/**
 * Utility class for loading different pages in the application
 */
public class GenericPageLoader {

    private final CustomerService customerService;
    private final TechnicianService technicianService;
    private final ServiceRequestService serviceRequestService;
    private final QuoteService quoteService;
    private final PaymentService paymentService;
    private final UserService userService;

    /**
     * Constructor with all services
     */
    public GenericPageLoader(
            CustomerService customerService,
            TechnicianService technicianService,
            ServiceRequestService serviceRequestService,
            QuoteService quoteService,
            PaymentService paymentService,
            UserService userService) {
        this.customerService = customerService;
        this.technicianService = technicianService;
        this.serviceRequestService = serviceRequestService;
        this.quoteService = quoteService;
        this.paymentService = paymentService;
        this.userService = userService;
    }

    /**
     * Load the dashboard page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadDashboardPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/dashboard.fxml",
                (DashboardController controller) -> {
                    // Set all services first
                    controller.setCustomerService(customerService);
                    controller.setTechnicianService(technicianService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setQuoteService(quoteService);
                    controller.setPaymentService(paymentService);
                    controller.setUserService(userService);
                },
                "Error Loading Dashboard"
        );
    }

    /**
     * Load the customers page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadCustomersPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/customer/customer_list.fxml",
                (CustomerListController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.initialize();
                },
                "Error Loading Customers"
        );
    }

    /**
     * Load the technicians page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadTechniciansPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/technician/technician_list.fxml",
                (TechnicianListController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.initialize();
                },
                "Error Loading Technicians"
        );
    }

    /**
     * Load the service requests page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadServiceRequestsPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/service/service_request_list.fxml",
                (ServiceRequestListController controller) -> {
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setCustomerService(customerService);
                    controller.initialize();
                },
                "Error Loading Service Requests"
        );
    }

    /**
     * Load the quotes page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadQuotesPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/quote/quote_list.fxml",
                (QuoteListController controller) -> {
                    controller.setQuoteService(quoteService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.initialize();
                },
                "Error Loading Quotes"
        );
    }

    /**
     * Load the payments page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadPaymentsPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/payment/payment_list.fxml",
                (PaymentListController controller) -> {
                    controller.setPaymentService(paymentService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.initialize();
                },
                "Error Loading Payments"
        );
    }

    /**
     * Load the reports page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadReportsPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/reports/reports.fxml",
                (ReportsController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.setTechnicianService(technicianService);
                    controller.setServiceRequestService(serviceRequestService);
                    controller.setQuoteService(quoteService);
                    controller.setPaymentService(paymentService);
                    controller.initialize();
                },
                "Error Loading Reports"
        );
    }

    /**
     * Load the settings page
     * @param container The container to load into
     * @return true if successful
     */
    public boolean loadSettingsPage(Pane container) {
        return FXMLLoaderUtil.loadIntoContainer(
                container,
                "/fxml/settings/settings.fxml",
                (SettingsController controller) -> {
                    controller.setUserService(userService);
                    controller.initialize();
                },
                "Error Loading Settings"
        );
    }
}