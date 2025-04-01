package com.management.controller.service;

import com.management.component.WizardFramework;
import com.management.component.ServiceRequestWizardSteps;
import com.management.model.Customer;
import com.management.model.ServiceRequest;
import com.management.service.CustomerService;
import com.management.service.ServiceRequestService;
import com.management.service.TechnicianService;
import com.management.util.AlertUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the service request view that can switch between wizard and form modes
 */
public class ServiceRequestViewController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private ToggleButton wizardToggle;

    @FXML
    private ToggleButton formToggle;

    @FXML
    private StackPane contentContainer;

    @FXML
    private HBox wizardNavigation;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button finishButton;

    private ServiceRequestService serviceRequestService;
    private CustomerService customerService;
    private TechnicianService technicianService;

    private WizardFramework wizard;
    private ServiceRequest serviceRequest;
    private boolean isEditMode;
    private Customer preselectedCustomer;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Create toggle group
        ToggleGroup viewToggle = new ToggleGroup();
        wizardToggle.setToggleGroup(viewToggle);
        formToggle.setToggleGroup(viewToggle);
        wizardToggle.setSelected(true);

        // Add listeners
        viewToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == wizardToggle) {
                showWizardView();
            } else if (newVal == formToggle) {
                showFormView();
            }
        });

        // Setup navigation buttons
        prevButton.setOnAction(e -> handlePrevious());
        nextButton.setOnAction(e -> handleNext());
        finishButton.setOnAction(e -> handleFinish());

        // Initialize with empty service request
        serviceRequest = new ServiceRequest();
        isEditMode = false;
    }

    /**
     * Set the required services
     * @param serviceRequestService The service request service
     * @param customerService The customer service
     * @param technicianService The technician service
     */
    public void setServices(ServiceRequestService serviceRequestService,
                            CustomerService customerService,
                            TechnicianService technicianService) {
        this.serviceRequestService = serviceRequestService;
        this.customerService = customerService;
        this.technicianService = technicianService;
    }

    /**
     * Set up edit mode with an existing service request
     * @param serviceRequest The service request to edit
     */
    public void editServiceRequest(ServiceRequest serviceRequest) {
        this.serviceRequest = serviceRequest;
        this.isEditMode = true;

        // If already initialized, refresh the views
        if (contentContainer.getChildren().size() > 0) {
            if (wizardToggle.isSelected()) {
                showWizardView();
            } else {
                showFormView();
            }
        }
    }

    /**
     * Pre-select a customer for a new service request
     * @param customer The customer to pre-select
     */
    public void setCustomer(Customer customer) {
        this.preselectedCustomer = customer;

        if (serviceRequest != null && customer != null) {
            serviceRequest.setCustomerId(customer.getCustomerId());
            serviceRequest.setCustomer(customer);
        }
    }

    /**
     * Show the wizard view
     */
    private void showWizardView() {
        contentContainer.getChildren().clear();
        wizardNavigation.setVisible(true);

        // Create wizard
        wizard = new WizardFramework();

        // Create steps
        ServiceRequestWizardSteps.CustomerStep customerStep =
                new ServiceRequestWizardSteps.CustomerStep(customerService, serviceRequest);

        ServiceRequestWizardSteps.ServiceDetailsStep detailsStep =
                new ServiceRequestWizardSteps.ServiceDetailsStep(serviceRequest);

        ServiceRequestWizardSteps.SchedulingStep schedulingStep =
                new ServiceRequestWizardSteps.SchedulingStep(serviceRequest);

        ServiceRequestWizardSteps.LocationStep locationStep =
                new ServiceRequestWizardSteps.LocationStep(serviceRequest);

        ServiceRequestWizardSteps.TechnicianStep technicianStep =
                new ServiceRequestWizardSteps.TechnicianStep(technicianService, serviceRequest);

        ServiceRequestWizardSteps.CostEstimationStep costStep =
                new ServiceRequestWizardSteps.CostEstimationStep(serviceRequest);

        ServiceRequestWizardSteps.ReviewStep reviewStep =
                new ServiceRequestWizardSteps.ReviewStep(serviceRequest, this::saveServiceRequest);

        // Add steps to wizard
        wizard.addStep(customerStep);
        wizard.addStep(detailsStep);
        wizard.addStep(schedulingStep);
        wizard.addStep(locationStep);
        wizard.addStep(technicianStep);
        wizard.addStep(costStep);
        wizard.addStep(reviewStep);

        // Pre-select customer if provided
        if (preselectedCustomer != null && !isEditMode) {
            serviceRequest.setCustomerId(preselectedCustomer.getCustomerId());
            serviceRequest.setCustomer(preselectedCustomer);
        }

        // Add wizard to container
        contentContainer.getChildren().add(wizard.getContent());

        // Update button states
        updateNavigationButtons();

        // Add listener for step changes
        wizard.currentStepIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateNavigationButtons();
        });
    }

    /**
     * Show the form view
     */
    private void showFormView() {
        contentContainer.getChildren().clear();
        wizardNavigation.setVisible(false);

        try {
            // Load the form FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/service/service_request_form.fxml"));
            Parent formView = loader.load();

            // Get controller
            ServiceRequestFormController controller = loader.getController();
            controller.setServiceRequestService(serviceRequestService);
            controller.setCustomerService(customerService);

            // Set mode
            if (isEditMode) {
                controller.setMode(ServiceRequestFormController.Mode.EDIT);
                controller.loadServiceRequest(serviceRequest);
            } else {
                controller.setMode(ServiceRequestFormController.Mode.ADD);

                // Set pre-selected customer if any
                if (preselectedCustomer != null) {
                    controller.setCustomer(preselectedCustomer);
                }
            }

            contentContainer.getChildren().add(formView);

        } catch (IOException e) {
            AlertUtils.showErrorAlert("Error", "Failed to load service request form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle previous button click
     */
    private void handlePrevious() {
        if (wizard != null) {
            wizard.previousStep();
            updateNavigationButtons();
        }
    }

    /**
     * Handle next button click
     */
    private void handleNext() {
        if (wizard != null) {
            wizard.nextStep();
            updateNavigationButtons();
        }
    }

    /**
     * Handle finish button click
     */
    private void handleFinish() {
        if (wizard != null && wizard.getCurrentStep().validate()) {
            // Close the window
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Update navigation buttons based on current step
     */
    private void updateNavigationButtons() {
        if (wizard != null) {
            prevButton.setDisable(wizard.isFirstStep());
            nextButton.setVisible(!wizard.isLastStep());
            finishButton.setVisible(wizard.isLastStep());
        }
    }

    /**
     * Save the service request
     * @param serviceRequest The service request to save
     */
    private void saveServiceRequest(ServiceRequest serviceRequest) {
        try {
            if (isEditMode) {
                boolean success = serviceRequestService.updateServiceRequest(serviceRequest);
                if (success) {
                    AlertUtils.showInformationAlert("Success", "Service request updated successfully.");
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to update service request.");
                }
            } else {
                int jobId = serviceRequestService.createServiceRequest(serviceRequest);
                if (jobId > 0) {
                    serviceRequest.setJobId(jobId);
                    AlertUtils.showInformationAlert("Success", "Service request created successfully.");
                } else {
                    AlertUtils.showErrorAlert("Error", "Failed to create service request.");
                }
            }
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create and show the service request view
     * @param parentStage The parent stage
     * @param serviceRequestService The service request service
     * @param customerService The customer service
     * @param technicianService The technician service
     * @param serviceRequest The service request to edit (or null for new)
     * @param customer The customer to pre-select (or null)
     */
    public static void show(Stage parentStage,
                            ServiceRequestService serviceRequestService,
                            CustomerService customerService,
                            TechnicianService technicianService,
                            ServiceRequest serviceRequest,
                            Customer customer) {
        try {
            // Load the view FXML
            FXMLLoader loader = new FXMLLoader(ServiceRequestViewController.class.getResource(
                    "/fxml/service/service_request_view.fxml"));
            Parent root = loader.load();

            // Get controller
            ServiceRequestViewController controller = loader.getController();
            controller.setServices(serviceRequestService, customerService, technicianService);

            // Set service request if editing
            if (serviceRequest != null) {
                controller.editServiceRequest(serviceRequest);
            }

            // Set customer if provided
            if (customer != null) {
                controller.setCustomer(customer);
            }

            // Create stage
            Stage stage = new Stage();
            stage.setTitle(serviceRequest != null ? "Edit Service Request" : "New Service Request");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parentStage);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            AlertUtils.showErrorAlert("Error", "Failed to load service request view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}