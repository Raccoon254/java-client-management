package com.management.util;

import com.management.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting data to CSV files
 */
public class CSVExporter {

    // CSV constants
    private static final String CSV_SEPARATOR = ",";
    private static final String NEW_LINE = "\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Export customers to a CSV file
     * @param customers The list of customers
     * @param filePath The output file path
     * @throws IOException if file writing fails
     */
    public static void exportCustomers(List<Customer> customers, String filePath) throws IOException {
        FileWriter writer = new FileWriter(new File(filePath));

        // Write header
        writer.append("Customer ID")
                .append(CSV_SEPARATOR).append("Customer Number")
                .append(CSV_SEPARATOR).append("First Name")
                .append(CSV_SEPARATOR).append("Last Name")
                .append(CSV_SEPARATOR).append("Email")
                .append(CSV_SEPARATOR).append("Phone Number")
                .append(CSV_SEPARATOR).append("Mobile Number")
                .append(CSV_SEPARATOR).append("Company Name")
                .append(CSV_SEPARATOR).append("Position")
                .append(CSV_SEPARATOR).append("Business Name")
                .append(CSV_SEPARATOR).append("Street Address")
                .append(CSV_SEPARATOR).append("State")
                .append(CSV_SEPARATOR).append("Zip Code")
                .append(CSV_SEPARATOR).append("Website")
                .append(NEW_LINE);

        // Write customer data
        for (Customer customer : customers) {
            writer.append(String.valueOf(customer.getCustomerId()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getCustomerNumber()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getFirstName()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getLastName()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getEmail()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getPhoneNumber()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getMobileNumber()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getCompanyName()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getPosition()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getBusinessName()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getStreetAddress()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getState()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getZipCode()))
                    .append(CSV_SEPARATOR).append(csvFormat(customer.getWebsite()))
                    .append(NEW_LINE);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Export technicians to a CSV file
     * @param technicians The list of technicians
     * @param filePath The output file path
     * @throws IOException if file writing fails
     */
    public static void exportTechnicians(List<Technician> technicians, String filePath) throws IOException {
        FileWriter writer = new FileWriter(new File(filePath));

        // Write header
        writer.append("Technician ID")
                .append(CSV_SEPARATOR).append("First Name")
                .append(CSV_SEPARATOR).append("Last Name")
                .append(CSV_SEPARATOR).append("Email")
                .append(CSV_SEPARATOR).append("Credentials")
                .append(CSV_SEPARATOR).append("Credential Level")
                .append(CSV_SEPARATOR).append("Zip Code")
                .append(CSV_SEPARATOR).append("Coverage Area")
                .append(CSV_SEPARATOR).append("Pay Type")
                .append(CSV_SEPARATOR).append("Address")
                .append(CSV_SEPARATOR).append("City")
                .append(CSV_SEPARATOR).append("State")
                .append(CSV_SEPARATOR).append("Zip")
                .append(CSV_SEPARATOR).append("Legal Name")
                .append(NEW_LINE);

        // Write technician data
        for (Technician technician : technicians) {
            writer.append(String.valueOf(technician.getTechnicianId()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getFirstName()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getLastName()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getEmail()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getCredentials()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getCredentialLevel()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getZipCode()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getCoverageArea()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getPayType()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getAddress()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getCity()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getState()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getZip()))
                    .append(CSV_SEPARATOR).append(csvFormat(technician.getLegalName()))
                    .append(NEW_LINE);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Export service requests to a CSV file
     * @param serviceRequests The list of service requests
     * @param includeCustomerInfo Whether to include customer information
     * @param filePath The output file path
     * @throws IOException if file writing fails
     */
    public static void exportServiceRequests(List<ServiceRequest> serviceRequests, boolean includeCustomerInfo, String filePath) throws IOException {
        FileWriter writer = new FileWriter(new File(filePath));

        // Write header
        writer.append("Job ID")
                .append(CSV_SEPARATOR).append("Customer ID");

        if (includeCustomerInfo) {
            writer.append(CSV_SEPARATOR).append("Customer Name")
                    .append(CSV_SEPARATOR).append("Customer Email")
                    .append(CSV_SEPARATOR).append("Customer Phone");
        }

        writer.append(CSV_SEPARATOR).append("Description")
                .append(CSV_SEPARATOR).append("Service Date")
                .append(CSV_SEPARATOR).append("Start Time")
                .append(CSV_SEPARATOR).append("End Time")
                .append(CSV_SEPARATOR).append("Service Address")
                .append(CSV_SEPARATOR).append("Service City")
                .append(CSV_SEPARATOR).append("Service State")
                .append(CSV_SEPARATOR).append("Service Zip")
                .append(CSV_SEPARATOR).append("Service Cost")
                .append(CSV_SEPARATOR).append("Added Cost")
                .append(CSV_SEPARATOR).append("Parking Fees")
                .append(CSV_SEPARATOR).append("Total Cost")
                .append(CSV_SEPARATOR).append("Status")
                .append(CSV_SEPARATOR).append("Assigned Technicians")
                .append(NEW_LINE);

        // Write service request data
        for (ServiceRequest serviceRequest : serviceRequests) {
            writer.append(String.valueOf(serviceRequest.getJobId()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getCustomerId()));

            if (includeCustomerInfo && serviceRequest.getCustomer() != null) {
                Customer customer = serviceRequest.getCustomer();
                writer.append(CSV_SEPARATOR).append(csvFormat(customer.getFirstName() + " " + customer.getLastName()))
                        .append(CSV_SEPARATOR).append(csvFormat(customer.getEmail()))
                        .append(CSV_SEPARATOR).append(csvFormat(customer.getPhoneNumber()));
            }

            writer.append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getDescription()))
                    .append(CSV_SEPARATOR).append(serviceRequest.getServiceDate() != null ? csvFormat(serviceRequest.getServiceDate().format(DATE_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(serviceRequest.getStartTime() != null ? csvFormat(serviceRequest.getStartTime().format(TIME_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(serviceRequest.getEndTime() != null ? csvFormat(serviceRequest.getEndTime().format(TIME_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getServiceAddress()))
                    .append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getServiceCity()))
                    .append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getServiceState()))
                    .append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getServiceZip()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getServiceCost()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getAddedCost()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getParkingFees()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getTotalCost()))
                    .append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getStatus()));

            // Add technician information
            StringBuilder technicianInfo = new StringBuilder();
            if (serviceRequest.getTechnicians() != null && !serviceRequest.getTechnicians().isEmpty()) {
                for (int i = 0; i < serviceRequest.getTechnicians().size(); i++) {
                    Technician technician = serviceRequest.getTechnicians().get(i);
                    technicianInfo.append(technician.getFirstName()).append(" ").append(technician.getLastName());

                    if (i < serviceRequest.getTechnicians().size() - 1) {
                        technicianInfo.append("; ");
                    }
                }
            }

            writer.append(CSV_SEPARATOR).append(csvFormat(technicianInfo.toString()))
                    .append(NEW_LINE);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Export quotes to a CSV file
     * @param quotes The list of quotes
     * @param filePath The output file path
     * @throws IOException if file writing fails
     */
    public static void exportQuotes(List<Quote> quotes, String filePath) throws IOException {
        FileWriter writer = new FileWriter(new File(filePath));

        // Write header
        writer.append("Quote ID")
                .append(CSV_SEPARATOR).append("Job ID")
                .append(CSV_SEPARATOR).append("Amount")
                .append(CSV_SEPARATOR).append("Start Date")
                .append(CSV_SEPARATOR).append("End Date")
                .append(CSV_SEPARATOR).append("Status")
                .append(CSV_SEPARATOR).append("Created At")
                .append(NEW_LINE);

        // Write quote data
        for (Quote quote : quotes) {
            writer.append(String.valueOf(quote.getQuoteId()))
                    .append(CSV_SEPARATOR).append(String.valueOf(quote.getJobId()))
                    .append(CSV_SEPARATOR).append(String.valueOf(quote.getAmount()))
                    .append(CSV_SEPARATOR).append(quote.getStartDate() != null ? csvFormat(quote.getStartDate().format(DATE_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(quote.getEndDate() != null ? csvFormat(quote.getEndDate().format(DATE_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(csvFormat(quote.getStatus()))
                    .append(CSV_SEPARATOR).append(quote.getCreatedAt() != null ? csvFormat(quote.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))) : "")
                    .append(NEW_LINE);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Export payments to a CSV file
     * @param payments The list of payments
     * @param filePath The output file path
     * @throws IOException if file writing fails
     */
    public static void exportPayments(List<Payment> payments, String filePath) throws IOException {
        FileWriter writer = new FileWriter(new File(filePath));

        // Write header
        writer.append("Payment ID")
                .append(CSV_SEPARATOR).append("Job ID")
                .append(CSV_SEPARATOR).append("Amount")
                .append(CSV_SEPARATOR).append("Status")
                .append(CSV_SEPARATOR).append("Payment Date")
                .append(CSV_SEPARATOR).append("Payment Method")
                .append(CSV_SEPARATOR).append("Notes")
                .append(NEW_LINE);

        // Write payment data
        for (Payment payment : payments) {
            writer.append(String.valueOf(payment.getPaymentId()))
                    .append(CSV_SEPARATOR).append(String.valueOf(payment.getJobId()))
                    .append(CSV_SEPARATOR).append(String.valueOf(payment.getAmount()))
                    .append(CSV_SEPARATOR).append(csvFormat(payment.getStatus()))
                    .append(CSV_SEPARATOR).append(payment.getPaymentDate() != null ? csvFormat(payment.getPaymentDate().format(DATE_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(csvFormat(payment.getPaymentMethod()))
                    .append(CSV_SEPARATOR).append(csvFormat(payment.getNotes()))
                    .append(NEW_LINE);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Export a service schedule for a technician
     * @param technician The technician
     * @param serviceRequests The list of service requests
     * @param filePath The output file path
     * @throws IOException if file writing fails
     */
    public static void exportTechnicianSchedule(Technician technician, List<ServiceRequest> serviceRequests, String filePath) throws IOException {
        FileWriter writer = new FileWriter(new File(filePath));

        // Write header
        writer.append("Technician: ").append(technician.getFirstName()).append(" ").append(technician.getLastName()).append(NEW_LINE);
        writer.append("Email: ").append(technician.getEmail()).append(NEW_LINE);
        writer.append(NEW_LINE);

        writer.append("Date")
                .append(CSV_SEPARATOR).append("Start Time")
                .append(CSV_SEPARATOR).append("End Time")
                .append(CSV_SEPARATOR).append("Job ID")
                .append(CSV_SEPARATOR).append("Customer")
                .append(CSV_SEPARATOR).append("Location")
                .append(CSV_SEPARATOR).append("Description")
                .append(NEW_LINE);

        // Write schedule data
        for (ServiceRequest serviceRequest : serviceRequests) {
            writer.append(serviceRequest.getServiceDate() != null ? csvFormat(serviceRequest.getServiceDate().format(DATE_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(serviceRequest.getStartTime() != null ? csvFormat(serviceRequest.getStartTime().format(TIME_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(serviceRequest.getEndTime() != null ? csvFormat(serviceRequest.getEndTime().format(TIME_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getJobId()));

            // Add customer information
            if (serviceRequest.getCustomer() != null) {
                writer.append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getCustomer().getFirstName() + " " + serviceRequest.getCustomer().getLastName()));
            } else {
                writer.append(CSV_SEPARATOR).append("");
            }

            // Add location and description
            writer.append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getServiceLocation()))
                    .append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getDescription()))
                    .append(NEW_LINE);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Export a monthly financial report
     * @param month The month (1-12)
     * @param year The year
     * @param serviceRequests The list of service requests
     * @param payments The list of payments
     * @param filePath The output file path
     * @throws IOException if file writing fails
     */
    public static void exportMonthlyFinancialReport(int month, int year, List<ServiceRequest> serviceRequests, List<Payment> payments, String filePath) throws IOException {
        FileWriter writer = new FileWriter(new File(filePath));

        // Write header
        writer.append("Monthly Financial Report").append(NEW_LINE);
        writer.append("Month: ").append(String.valueOf(month)).append(NEW_LINE);
        writer.append("Year: ").append(String.valueOf(year)).append(NEW_LINE);
        writer.append(NEW_LINE);

        // Calculate totals
        double totalServiceCost = 0;
        double totalAddedCost = 0;
        double totalParkingFees = 0;
        double totalCost = 0;
        double totalPayments = 0;
        double totalOutstanding = 0;

        for (ServiceRequest serviceRequest : serviceRequests) {
            totalServiceCost += serviceRequest.getServiceCost();
            totalAddedCost += serviceRequest.getAddedCost();
            totalParkingFees += serviceRequest.getParkingFees();
            totalCost += serviceRequest.getTotalCost();
        }

        for (Payment payment : payments) {
            totalPayments += payment.getAmount();
        }

        totalOutstanding = totalCost - totalPayments;

        // Write summary section
        writer.append("Summary").append(NEW_LINE);
        writer.append("Total Service Cost: $").append(String.format("%.2f", totalServiceCost)).append(NEW_LINE);
        writer.append("Total Added Cost: $").append(String.format("%.2f", totalAddedCost)).append(NEW_LINE);
        writer.append("Total Parking Fees: $").append(String.format("%.2f", totalParkingFees)).append(NEW_LINE);
        writer.append("Total Cost: $").append(String.format("%.2f", totalCost)).append(NEW_LINE);
        writer.append("Total Payments: $").append(String.format("%.2f", totalPayments)).append(NEW_LINE);
        writer.append("Total Outstanding: $").append(String.format("%.2f", totalOutstanding)).append(NEW_LINE);
        writer.append(NEW_LINE);

        // Write service requests section
        writer.append("Service Requests").append(NEW_LINE);
        writer.append("Job ID")
                .append(CSV_SEPARATOR).append("Customer")
                .append(CSV_SEPARATOR).append("Service Date")
                .append(CSV_SEPARATOR).append("Service Cost")
                .append(CSV_SEPARATOR).append("Added Cost")
                .append(CSV_SEPARATOR).append("Parking Fees")
                .append(CSV_SEPARATOR).append("Total Cost")
                .append(CSV_SEPARATOR).append("Status")
                .append(NEW_LINE);

        for (ServiceRequest serviceRequest : serviceRequests) {
            writer.append(String.valueOf(serviceRequest.getJobId()));

            // Add customer information
            if (serviceRequest.getCustomer() != null) {
                writer.append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getCustomer().getFirstName() + " " + serviceRequest.getCustomer().getLastName()));
            } else {
                writer.append(CSV_SEPARATOR).append("");
            }

            writer.append(CSV_SEPARATOR).append(serviceRequest.getServiceDate() != null ? csvFormat(serviceRequest.getServiceDate().format(DATE_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getServiceCost()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getAddedCost()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getParkingFees()))
                    .append(CSV_SEPARATOR).append(String.valueOf(serviceRequest.getTotalCost()))
                    .append(CSV_SEPARATOR).append(csvFormat(serviceRequest.getStatus()))
                    .append(NEW_LINE);
        }

        writer.append(NEW_LINE);

        // Write payments section
        writer.append("Payments").append(NEW_LINE);
        writer.append("Payment ID")
                .append(CSV_SEPARATOR).append("Job ID")
                .append(CSV_SEPARATOR).append("Payment Date")
                .append(CSV_SEPARATOR).append("Amount")
                .append(CSV_SEPARATOR).append("Method")
                .append(CSV_SEPARATOR).append("Status")
                .append(NEW_LINE);

        for (Payment payment : payments) {
            writer.append(String.valueOf(payment.getPaymentId()))
                    .append(CSV_SEPARATOR).append(String.valueOf(payment.getJobId()))
                    .append(CSV_SEPARATOR).append(payment.getPaymentDate() != null ? csvFormat(payment.getPaymentDate().format(DATE_FORMATTER)) : "")
                    .append(CSV_SEPARATOR).append(String.valueOf(payment.getAmount()))
                    .append(CSV_SEPARATOR).append(csvFormat(payment.getPaymentMethod()))
                    .append(CSV_SEPARATOR).append(csvFormat(payment.getStatus()))
                    .append(NEW_LINE);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Format a string for CSV output (add quotes if necessary)
     * @param str The string to format
     * @return The CSV-formatted string
     */
    private static String csvFormat(String str) {
        if (str == null) {
            return "";
        }

        // Escape quotes by doubling them and add quotes around the string if it contains commas, quotes, or newlines
        if (str.contains("\"") || str.contains(",") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }

        return str;
    }
}