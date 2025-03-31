package com.management.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.management.model.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating PDF documents
 */
public class PDFGenerator {

    // Document constants
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font HEADING_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    /**
     * Generate a service request report PDF
     *
     * @param serviceRequest The service request
     * @param filePath       The output file path
     * @throws Exception if PDF generation fails
     */
    public static void generateServiceRequestReport(ServiceRequest serviceRequest, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Create page event handler for page numbers
        PageNumeration pageEvent = new PageNumeration();
        writer.setPageEvent(pageEvent);

        document.open();

        // Add header
        addHeader(document, "Service Request Report");

        // Add service request details
        addServiceRequestDetails(document, serviceRequest);

        // Add customer details
        if (serviceRequest.getCustomer() != null) {
            addCustomerDetails(document, serviceRequest.getCustomer());
        }

        // Add technician details
        if (serviceRequest.getTechnicians() != null && !serviceRequest.getTechnicians().isEmpty()) {
            addTechnicianDetails(document, serviceRequest.getTechnicians());
        }

        // Add footer
        addFooter(document);

        document.close();
    }

    /**
     * Generate a quote report PDF
     *
     * @param quote    The quote
     * @param filePath The output file path
     * @throws Exception if PDF generation fails
     */
    public static void generateQuoteReport(Quote quote, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Create page event handler for page numbers
        PageNumeration pageEvent = new PageNumeration();
        writer.setPageEvent(pageEvent);

        document.open();

        // Add header
        addHeader(document, "Quote Report");

        // Add quote details
        addQuoteDetails(document, quote);

        // Add service request details if available
        if (quote.getServiceRequest() != null) {
            addServiceRequestDetails(document, quote.getServiceRequest());

            // Add customer details if available
            if (quote.getServiceRequest().getCustomer() != null) {
                addCustomerDetails(document, quote.getServiceRequest().getCustomer());
            }
        }

        // Add footer
        addFooter(document);

        document.close();
    }

    /**
     * Generate a payment report PDF
     *
     * @param payment  The payment
     * @param filePath The output file path
     * @throws Exception if PDF generation fails
     */
    public static void generatePaymentReport(Payment payment, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Create page event handler for page numbers
        PageNumeration pageEvent = new PageNumeration();
        writer.setPageEvent(pageEvent);

        document.open();

        // Add header
        addHeader(document, "Payment Receipt");

        // Add payment details
        addPaymentDetails(document, payment);

        // Add service request details if available
        if (payment.getServiceRequest() != null) {
            addServiceRequestDetails(document, payment.getServiceRequest());

            // Add customer details if available
            if (payment.getServiceRequest().getCustomer() != null) {
                addCustomerDetails(document, payment.getServiceRequest().getCustomer());
            }
        }

        // Add footer
        addFooter(document);

        document.close();
    }

    /**
     * Generate a customer report PDF
     *
     * @param customer        The customer
     * @param serviceRequests The customer's service requests
     * @param filePath        The output file path
     * @throws Exception if PDF generation fails
     */
    public static void generateCustomerReport(Customer customer, List<ServiceRequest> serviceRequests, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Create page event handler for page numbers
        PageNumeration pageEvent = new PageNumeration();
        writer.setPageEvent(pageEvent);

        document.open();

        // Add header
        addHeader(document, "Customer Report");

        // Add customer details
        addCustomerDetails(document, customer);

        // Add service request summary table
        if (serviceRequests != null && !serviceRequests.isEmpty()) {
            addServiceRequestSummaryTable(document, serviceRequests);
        }

        // Add footer
        addFooter(document);

        document.close();
    }

    /**
     * Generate a technician report PDF
     *
     * @param technician      The technician
     * @param serviceRequests The technician's service requests
     * @param filePath        The output file path
     * @throws Exception if PDF generation fails
     */
    public static void generateTechnicianReport(Technician technician, List<ServiceRequest> serviceRequests, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Create page event handler for page numbers
        PageNumeration pageEvent = new PageNumeration();
        writer.setPageEvent(pageEvent);

        document.open();

        // Add header
        addHeader(document, "Technician Report");

        // Add technician details
        addSingleTechnicianDetails(document, technician);

        // Add service request summary table
        if (serviceRequests != null && !serviceRequests.isEmpty()) {
            addServiceRequestSummaryTable(document, serviceRequests);
        }

        // Add footer
        addFooter(document);

        document.close();
    }

    /**
     * Generate a byte array for a service request report PDF
     *
     * @param serviceRequest The service request
     * @return Byte array containing the PDF
     * @throws Exception if PDF generation fails
     */
    public static byte[] generateServiceRequestReportBytes(ServiceRequest serviceRequest) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // Create page event handler for page numbers
        PageNumeration pageEvent = new PageNumeration();
        writer.setPageEvent(pageEvent);

        document.open();

        // Add header
        addHeader(document, "Service Request Report");

        // Add service request details
        addServiceRequestDetails(document, serviceRequest);

        // Add customer details
        if (serviceRequest.getCustomer() != null) {
            addCustomerDetails(document, serviceRequest.getCustomer());
        }

        // Add technician details
        if (serviceRequest.getTechnicians() != null && !serviceRequest.getTechnicians().isEmpty()) {
            addTechnicianDetails(document, serviceRequest.getTechnicians());
        }

        // Add footer
        addFooter(document);

        document.close();

        return baos.toByteArray();
    }

    /**
     * Add a header to the document
     *
     * @param document The document
     * @param title    The header title
     * @throws DocumentException if adding the header fails
     */
    private static void addHeader(Document document, String title) throws DocumentException {
        Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(titleParagraph);

        Paragraph dateParagraph = new Paragraph(
                "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")),
                SMALL_FONT
        );
        dateParagraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(dateParagraph);

        // Add company info
        Paragraph companyParagraph = new Paragraph(
                "Client Management System\n" +
                        "123 Business Street\n" +
                        "Cityville, State 12345\n" +
                        "Phone: (555) 123-4567\n" +
                        "Email: support@clientmanagement.com",
                SMALL_FONT
        );
        companyParagraph.setAlignment(Element.ALIGN_LEFT);
        document.add(companyParagraph);

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add service request details to the document
     *
     * @param document       The document
     * @param serviceRequest The service request
     * @throws DocumentException if adding the details fails
     */
    private static void addServiceRequestDetails(Document document, ServiceRequest serviceRequest) throws DocumentException {
        Paragraph heading = new Paragraph("Service Request Details", SUBTITLE_FONT);
        heading.setSpacingBefore(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Set column widths
        try {
            table.setWidths(new float[]{1, 3});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // Add service request details
        addTableRow(table, "Job ID", String.valueOf(serviceRequest.getJobId()));
        addTableRow(table, "Description", serviceRequest.getDescription());
        addTableRow(table, "Service Date", formatDate(serviceRequest.getServiceDate()));

        if (serviceRequest.getStartTime() != null) {
            addTableRow(table, "Start Time", serviceRequest.getStartTime().toString());
        }

        if (serviceRequest.getEndTime() != null) {
            addTableRow(table, "End Time", serviceRequest.getEndTime().toString());
        }

        addTableRow(table, "Status", serviceRequest.getStatus());
        addTableRow(table, "Service Cost", formatCurrency(serviceRequest.getServiceCost()));
        addTableRow(table, "Added Cost", formatCurrency(serviceRequest.getAddedCost()));
        addTableRow(table, "Parking Fees", formatCurrency(serviceRequest.getParkingFees()));
        addTableRow(table, "Total Cost", formatCurrency(serviceRequest.getTotalCost()));

        if (serviceRequest.getServiceAddress() != null) {
            addTableRow(table, "Service Location", serviceRequest.getServiceLocation());
        }

        if (serviceRequest.getServiceNotes() != null && !serviceRequest.getServiceNotes().isEmpty()) {
            addTableRow(table, "Notes", serviceRequest.getServiceNotes());
        }

        document.add(table);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add customer details to the document
     *
     * @param document The document
     * @param customer The customer
     * @throws DocumentException if adding the details fails
     */
    private static void addCustomerDetails(Document document, Customer customer) throws DocumentException {
        Paragraph heading = new Paragraph("Customer Information", SUBTITLE_FONT);
        heading.setSpacingBefore(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Set column widths
        try {
            table.setWidths(new float[]{1, 3});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // Add customer details
        addTableRow(table, "Name", customer.getFirstName() + " " + customer.getLastName());
        addTableRow(table, "Customer #", customer.getCustomerNumber());
        addTableRow(table, "Email", customer.getEmail());

        if (customer.getPhoneNumber() != null) {
            addTableRow(table, "Phone", customer.getPhoneNumber());
        }

        if (customer.getMobileNumber() != null) {
            addTableRow(table, "Mobile", customer.getMobileNumber());
        }

        if (customer.getCompanyName() != null) {
            addTableRow(table, "Company", customer.getCompanyName());
        }

        if (customer.getPosition() != null) {
            addTableRow(table, "Position", customer.getPosition());
        }

        if (customer.getStreetAddress() != null) {
            String address = customer.getStreetAddress();
            if (customer.getState() != null) {
                address += ", " + customer.getState();
            }
            if (customer.getZipCode() != null) {
                address += " " + customer.getZipCode();
            }
            addTableRow(table, "Address", address);
        }

        document.add(table);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add technician details to the document
     *
     * @param document    The document
     * @param technicians The list of technicians
     * @throws DocumentException if adding the details fails
     */
    private static void addTechnicianDetails(Document document, List<Technician> technicians) throws DocumentException {
        Paragraph heading = new Paragraph("Assigned Technicians", SUBTITLE_FONT);
        heading.setSpacingBefore(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        // Set column widths
        try {
            table.setWidths(new float[]{3, 3, 2, 2});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // Add table header
        PdfPCell nameHeader = new PdfPCell(new Phrase("Name", HEADING_FONT));
        PdfPCell emailHeader = new PdfPCell(new Phrase("Email", HEADING_FONT));
        PdfPCell credentialsHeader = new PdfPCell(new Phrase("Credentials", HEADING_FONT));
        PdfPCell coverageHeader = new PdfPCell(new Phrase("Coverage Area", HEADING_FONT));

        nameHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        emailHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        credentialsHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        coverageHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);

        table.addCell(nameHeader);
        table.addCell(emailHeader);
        table.addCell(credentialsHeader);
        table.addCell(coverageHeader);

        // Add technician rows
        for (Technician technician : technicians) {
            table.addCell(new PdfPCell(new Phrase(technician.getFirstName() + " " + technician.getLastName(), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(technician.getEmail(), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(technician.getCredentials() != null ? technician.getCredentials() : "", NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(technician.getCoverageArea() != null ? technician.getCoverageArea() : "", NORMAL_FONT)));
        }

        document.add(table);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add single technician details to the document
     *
     * @param document   The document
     * @param technician The technician
     * @throws DocumentException if adding the details fails
     */
    private static void addSingleTechnicianDetails(Document document, Technician technician) throws DocumentException {
        Paragraph heading = new Paragraph("Technician Information", SUBTITLE_FONT);
        heading.setSpacingBefore(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Set column widths
        try {
            table.setWidths(new float[]{1, 3});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // Add technician details
        addTableRow(table, "Name", technician.getFirstName() + " " + technician.getLastName());
        addTableRow(table, "Email", technician.getEmail());

        if (technician.getCredentials() != null) {
            addTableRow(table, "Credentials", technician.getCredentials());
        }

        if (technician.getCredentialLevel() != null) {
            addTableRow(table, "Credential Level", technician.getCredentialLevel());
        }

        if (technician.getCoverageArea() != null) {
            addTableRow(table, "Coverage Area", technician.getCoverageArea());
        }

        if (technician.getPayType() != null) {
            addTableRow(table, "Pay Type", technician.getPayType());
        }

        document.add(table);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add quote details to the document
     *
     * @param document The document
     * @param quote    The quote
     * @throws DocumentException if adding the details fails
     */
    private static void addQuoteDetails(Document document, Quote quote) throws DocumentException {
        Paragraph heading = new Paragraph("Quote Details", SUBTITLE_FONT);
        heading.setSpacingBefore(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Set column widths
        try {
            table.setWidths(new float[]{1, 3});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // Add quote details
        addTableRow(table, "Quote ID", String.valueOf(quote.getQuoteId()));
        addTableRow(table, "Job ID", String.valueOf(quote.getJobId()));
        addTableRow(table, "Amount", formatCurrency(quote.getAmount()));
        addTableRow(table, "Status", quote.getStatus());

        if (quote.getStartDate() != null) {
            addTableRow(table, "Start Date", formatDate(quote.getStartDate()));
        }

        if (quote.getEndDate() != null) {
            addTableRow(table, "End Date", formatDate(quote.getEndDate()));
        }

        if (quote.getCreatedAt() != null) {
            addTableRow(table, "Created", formatDateTime(quote.getCreatedAt()));
        }

        document.add(table);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add payment details to the document
     *
     * @param document The document
     * @param payment  The payment
     * @throws DocumentException if adding the details fails
     */
    private static void addPaymentDetails(Document document, Payment payment) throws DocumentException {
        Paragraph heading = new Paragraph("Payment Details", SUBTITLE_FONT);
        heading.setSpacingBefore(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Set column widths
        try {
            table.setWidths(new float[]{1, 3});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // Add payment details
        addTableRow(table, "Payment ID", String.valueOf(payment.getPaymentId()));
        addTableRow(table, "Job ID", String.valueOf(payment.getJobId()));
        addTableRow(table, "Amount", formatCurrency(payment.getAmount()));
        addTableRow(table, "Status", payment.getStatus());

        if (payment.getPaymentDate() != null) {
            addTableRow(table, "Payment Date", formatDate(payment.getPaymentDate()));
        }

        if (payment.getPaymentMethod() != null) {
            addTableRow(table, "Payment Method", payment.getPaymentMethod());
        }

        if (payment.getNotes() != null && !payment.getNotes().isEmpty()) {
            addTableRow(table, "Notes", payment.getNotes());
        }

        document.add(table);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add a service request summary table to the document
     *
     * @param document        The document
     * @param serviceRequests The list of service requests
     * @throws DocumentException if adding the table fails
     */
    private static void addServiceRequestSummaryTable(Document document, List<ServiceRequest> serviceRequests) throws DocumentException {
        Paragraph heading = new Paragraph("Service Request History", SUBTITLE_FONT);
        heading.setSpacingBefore(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        // Set column widths
        try {
            table.setWidths(new float[]{1, 2, 3, 2, 1, 2});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // Add table header
        PdfPCell idHeader = new PdfPCell(new Phrase("ID", HEADING_FONT));
        PdfPCell dateHeader = new PdfPCell(new Phrase("Date", HEADING_FONT));
        PdfPCell descriptionHeader = new PdfPCell(new Phrase("Description", HEADING_FONT));
        PdfPCell locationHeader = new PdfPCell(new Phrase("Location", HEADING_FONT));
        PdfPCell statusHeader = new PdfPCell(new Phrase("Status", HEADING_FONT));
        PdfPCell costHeader = new PdfPCell(new Phrase("Cost", HEADING_FONT));

        idHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        dateHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        descriptionHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        locationHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        statusHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        costHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);

        table.addCell(idHeader);
        table.addCell(dateHeader);
        table.addCell(descriptionHeader);
        table.addCell(locationHeader);
        table.addCell(statusHeader);
        table.addCell(costHeader);

        // Add service request rows
        for (ServiceRequest sr : serviceRequests) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(sr.getJobId()), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatDate(sr.getServiceDate()), NORMAL_FONT)));

            String description = sr.getDescription();
            if (description.length() > 30) {
                description = description.substring(0, 27) + "...";
            }
            table.addCell(new PdfPCell(new Phrase(description, NORMAL_FONT)));

            String location = sr.getServiceCity() != null ? sr.getServiceCity() : "";
            if (sr.getServiceState() != null) {
                location += location.isEmpty() ? sr.getServiceState() : ", " + sr.getServiceState();
            }
            table.addCell(new PdfPCell(new Phrase(location, NORMAL_FONT)));

            table.addCell(new PdfPCell(new Phrase(sr.getStatus(), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatCurrency(sr.getTotalCost()), NORMAL_FONT)));
        }

        document.add(table);
        document.add(new Paragraph(" ")); // Add some space
    }

    /**
     * Add a footer to the document
     *
     * @param document The document
     * @throws DocumentException if adding the footer fails
     */
    private static void addFooter(Document document) throws DocumentException {
        // Add a horizontal line
        LineSeparator line = new LineSeparator();
        line.setOffset(5);
        document.add(line);

        // Add footer text
        Paragraph footer = new Paragraph(
                "This document is generated by the Client Management System. " +
                        "For any questions, please contact support@clientmanagement.com.",
                SMALL_FONT
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    /**
     * Add a row to a table
     *
     * @param table The table
     * @param label The row label
     * @param value The row value
     */
    private static void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADING_FONT));
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", NORMAL_FONT));

        labelCell.setBorderWidth(0.5f);
        valueCell.setBorderWidth(0.5f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Format a date
     *
     * @param date The date
     * @return The formatted date string
     */
    private static String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
    }

    /**
     * Format a datetime
     *
     * @param dateTime The datetime
     * @return The formatted datetime string
     */
    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")) : "";
    }

    /**
     * Format a currency value
     *
     * @param amount The amount
     * @return The formatted currency string
     */
    private static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }

    /**
     * Page event class for adding page numbers
     */
    static class PageNumeration extends PdfPageEventHelper {
        private PdfTemplate total;
        private BaseFont baseFont;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();

            String text = "Page " + writer.getPageNumber() + " of ";
            float textSize = baseFont.getWidthPoint(text, 8);
            float textBase = document.bottom() - 20;

            cb.beginText();
            cb.setFontAndSize(baseFont, 8);

            // Center the text
            float adjust = baseFont.getWidthPoint("0", 8);
            cb.setTextMatrix(document.right() - textSize - adjust, textBase);
            cb.showText(text);
            cb.endText();
            cb.addTemplate(total, document.right() - adjust, textBase);

            cb.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            total.beginText();
            total.setFontAndSize(baseFont, 8);
            total.setTextMatrix(0, 0);
            total.showText(String.valueOf(writer.getPageNumber() - 1));
            total.endText();
        }
    }
}