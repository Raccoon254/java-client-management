package com.management.controller.dialogs;

import com.management.controller.customer.CustomerFormController;
import com.management.controller.service.ServiceRequestViewController;
import com.management.controller.technician.TechnicianFormController;
import com.management.service.CustomerService;
import com.management.service.PaymentService;
import com.management.service.QuoteService;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;
import com.management.util.FXMLLoaderUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Controller for the Create Menu Dialog
 */
public class CreateMenuDialogController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Button customerButton;

    @FXML
    private Button technicianButton;

    @FXML
    private Button serviceRequestButton;

    @FXML
    private Button quoteButton;

    @FXML
    private Button paymentButton;

    @FXML
    private Button closeButton;

    // Services
    private CustomerService customerService;
    private TechnicianService technicianService;
    private ServiceRequestService serviceRequestService;
    private QuoteService quoteService;
    private PaymentService paymentService;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialization code if needed
    }

    /**
     * Set the services needed by this controller
     * @param customerService The customer service
     * @param technicianService The technician service
     * @param serviceRequestService The service request service
     * @param quoteService The quote service
     * @param paymentService The payment service
     */
    public void setServices(
            CustomerService customerService,
            TechnicianService technicianService,
            ServiceRequestService serviceRequestService,
            QuoteService quoteService,
            PaymentService paymentService) {

        this.customerService = customerService;
        this.technicianService = technicianService;
        this.serviceRequestService = serviceRequestService;
        this.quoteService = quoteService;
        this.paymentService = paymentService;
    }

    /**
     * Handle new customer button click
     */
    @FXML
    private void handleNewCustomer() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();

        // Close this dialog first
        stage.close();

        // Open the customer form dialog
        FXMLLoaderUtil.openDialog(
                "/fxml/customer/customer_form.fxml",
                "New Customer",
                stage.getOwner(),
                (CustomerFormController controller) -> {
                    controller.setCustomerService(customerService);
                    controller.initialize();
                    controller.setMode(CustomerFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle new technician button click
     */
    @FXML
    private void handleNewTechnician() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();

        // Close this dialog first
        stage.close();

        // Open the technician form dialog
        FXMLLoaderUtil.openDialog(
                "/fxml/technician/technician_form.fxml",
                "New Technician",
                stage.getOwner(),
                (TechnicianFormController controller) -> {
                    controller.setTechnicianService(technicianService);
                    controller.initialize();
                    controller.setMode(TechnicianFormController.Mode.ADD);
                }
        );
    }

    /**
     * Handle new service request button click
     */
    @FXML
    private void handleNewServiceRequest() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();

        // Close this dialog first
        stage.close();

        // Open the service request WIZARD, not the form
        ServiceRequestViewController.show(
                (Stage) stage.getOwner(),
                serviceRequestService,
                customerService,
                technicianService,
                null,  // null for new service request (not editing)
                null   // null for no pre-selected customer
        );
    }

    /**
     * Handle new quote button click
     */
    @FXML
    private void handleNewQuote() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();

        // Close this dialog first
        stage.close();

        // Show message for upcoming feature
        AlertUtils.showInformationAlert(
                "New Quote",
                "The New Quote feature will be implemented in a future version."
        );
    }

    /**
     * Handle new payment button click
     */
    @FXML
    private void handleNewPayment() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();

        // Close this dialog first
        stage.close();

        // Show message for upcoming feature
        AlertUtils.showInformationAlert(
                "New Payment",
                "The New Payment feature will be implemented in a future version."
        );
    }

    /**
     * Handle dialog close
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }
}