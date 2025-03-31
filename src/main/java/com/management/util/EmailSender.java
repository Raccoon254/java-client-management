package com.management.util;

import com.management.model.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for sending email notifications
 */
public class EmailSender {
    private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());

    // Default email configuration
    private static String HOST = "smtp.gmail.com";
    private static int PORT = 587;
    private static String USERNAME = "notification.raccoon@gmail.com";
    private static String PASSWORD = "qkvvgrzdxxpsiqga";
    private static String FROM_EMAIL = "system@management.com";
    private static String FROM_NAME = "Client Management System";
    private static boolean USE_TLS = true;

    // Load email configuration from properties file
    static {
        try {
            Properties config = new Properties();
            File configFile = new File("config/email.properties");

            if (configFile.exists()) {
                FileInputStream fis = new FileInputStream(configFile);
                config.load(fis);
                fis.close();

                HOST = config.getProperty("mail.smtp.host", HOST);
                PORT = Integer.parseInt(config.getProperty("mail.smtp.port", String.valueOf(PORT)));
                USERNAME = config.getProperty("mail.username", USERNAME);
                PASSWORD = config.getProperty("mail.password", PASSWORD);
                FROM_EMAIL = config.getProperty("mail.from.email", FROM_EMAIL);
                FROM_NAME = config.getProperty("mail.from.name", FROM_NAME);
                USE_TLS = Boolean.parseBoolean(config.getProperty("mail.smtp.starttls.enable", String.valueOf(USE_TLS)));
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Failed to load email configuration", e);
        }
    }

    /**
     * Send a simple email
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @return true if email was sent successfully
     */
    public static boolean sendEmail(String to, String subject, String body) {
        try {
            Properties props = getEmailProperties();
            Session session = getSession(props);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            LOGGER.info("Email sent successfully to " + to);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send email", e);
            return false;
        }
    }

    /**
     * Send an HTML email
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body
     * @return true if email was sent successfully
     */
    public static boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            Properties props = getEmailProperties();
            Session session = getSession(props);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Set HTML content
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);
            LOGGER.info("HTML email sent successfully to " + to);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send HTML email", e);
            return false;
        }
    }

    /**
     * Send an email with an attachment
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @param attachmentPath Path to the attachment file
     * @param attachmentName Name of the attachment
     * @return true if email was sent successfully
     */
    public static boolean sendEmailWithAttachment(String to, String subject, String body,
                                                  String attachmentPath, String attachmentName) {
        try {
            Properties props = getEmailProperties();
            Session session = getSession(props);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Add attachment part
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentPath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachmentName);
            multipart.addBodyPart(messageBodyPart);

            // Set the complete message parts
            message.setContent(multipart);

            Transport.send(message);
            LOGGER.info("Email with attachment sent successfully to " + to);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send email with attachment", e);
            return false;
        }
    }

    /**
     * Send an email with a PDF attachment generated from a service request
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @param serviceRequest The service request
     * @return true if email was sent successfully
     */
    public static boolean sendServiceRequestEmail(String to, String subject, String body, ServiceRequest serviceRequest) {
        try {
            // Generate PDF
            byte[] pdfBytes = PDFGenerator.generateServiceRequestReportBytes(serviceRequest);

            Properties props = getEmailProperties();
            Session session = getSession(props);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Add attachment part
            messageBodyPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(pdfBytes, "application/pdf");
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName("ServiceRequest_" + serviceRequest.getJobId() + ".pdf");
            multipart.addBodyPart(messageBodyPart);

            // Set the complete message parts
            message.setContent(multipart);

            Transport.send(message);
            LOGGER.info("Service request email sent successfully to " + to);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send service request email", e);
            return false;
        }
    }

    /**
     * Send a welcome email to a new customer
     * @param customer The customer
     * @return true if email was sent successfully
     */
    public static boolean sendCustomerWelcomeEmail(Customer customer) {
        if (customer == null || customer.getEmail() == null) {
            return false;
        }

        String to = customer.getEmail();
        String subject = "Welcome to Client Management System";
        String body = "Dear " + customer.getFirstName() + " " + customer.getLastName() + ",\n\n" +
                "Welcome to the Client Management System! We are pleased to have you as a customer.\n\n" +
                "Your customer number is: " + customer.getCustomerNumber() + "\n\n" +
                "Please keep this information for your records. You can use this number " +
                "when contacting us about any service requests or inquiries.\n\n" +
                "If you have any questions, please feel free to contact us.\n\n" +
                "Best regards,\n" +
                "Client Management System Team";

        return sendEmail(to, subject, body);
    }

    /**
     * Send a service request confirmation email
     * @param serviceRequest The service request
     * @return true if email was sent successfully
     */
    public static boolean sendServiceRequestConfirmation(ServiceRequest serviceRequest) {
        if (serviceRequest == null || serviceRequest.getCustomer() == null ||
                serviceRequest.getCustomer().getEmail() == null) {
            return false;
        }

        Customer customer = serviceRequest.getCustomer();
        String to = customer.getEmail();
        String subject = "Service Request Confirmation - #" + serviceRequest.getJobId();
        String body = "Dear " + customer.getFirstName() + " " + customer.getLastName() + ",\n\n" +
                "Thank you for your service request. This email is to confirm that we have " +
                "received your request with the following details:\n\n" +
                "Service Request ID: " + serviceRequest.getJobId() + "\n" +
                "Description: " + serviceRequest.getDescription() + "\n" +
                "Service Date: " + DateTimeUtils.formatDate(serviceRequest.getServiceDate()) + "\n";

        if (serviceRequest.getStartTime() != null) {
            body += "Start Time: " + DateTimeUtils.formatTime(serviceRequest.getStartTime()) + "\n";
        }

        if (serviceRequest.getEndTime() != null) {
            body += "End Time: " + DateTimeUtils.formatTime(serviceRequest.getEndTime()) + "\n";
        }

        body += "Service Location: " + serviceRequest.getServiceLocation() + "\n\n" +
                "We will contact you soon to confirm the details and schedule the service.\n\n" +
                "If you have any questions, please feel free to contact us.\n\n" +
                "Best regards,\n" +
                "Client Management System Team";

        return sendEmail(to, subject, body);
    }

    /**
     * Send a quote notification email
     * @param quote The quote
     * @return true if email was sent successfully
     */
    public static boolean sendQuoteNotification(Quote quote) {
        if (quote == null || quote.getServiceRequest() == null ||
                quote.getServiceRequest().getCustomer() == null ||
                quote.getServiceRequest().getCustomer().getEmail() == null) {
            return false;
        }

        Customer customer = quote.getServiceRequest().getCustomer();
        String to = customer.getEmail();
        String subject = "Quote for Service Request #" + quote.getJobId();
        String htmlBody = "<html><body>" +
                "<h2>Service Quote</h2>" +
                "<p>Dear " + customer.getFirstName() + " " + customer.getLastName() + ",</p>" +
                "<p>We are pleased to provide you with a quote for your service request:</p>" +
                "<table border='1' cellpadding='5' cellspacing='0'>" +
                "<tr><td><strong>Quote ID:</strong></td><td>" + quote.getQuoteId() + "</td></tr>" +
                "<tr><td><strong>Service Request ID:</strong></td><td>" + quote.getJobId() + "</td></tr>" +
                "<tr><td><strong>Description:</strong></td><td>" + quote.getServiceRequest().getDescription() + "</td></tr>" +
                "<tr><td><strong>Amount:</strong></td><td>$" + String.format("%.2f", quote.getAmount()) + "</td></tr>";

        if (quote.getStartDate() != null) {
            htmlBody += "<tr><td><strong>Valid From:</strong></td><td>" + DateTimeUtils.formatDate(quote.getStartDate()) + "</td></tr>";
        }

        if (quote.getEndDate() != null) {
            htmlBody += "<tr><td><strong>Valid Until:</strong></td><td>" + DateTimeUtils.formatDate(quote.getEndDate()) + "</td></tr>";
        }

        htmlBody += "</table>" +
                "<p>Please review this quote and contact us if you have any questions or would like to proceed.</p>" +
                "<p>Best regards,<br>Client Management System Team</p>" +
                "</body></html>";

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send a payment receipt email
     * @param payment The payment
     * @return true if email was sent successfully
     */
    public static boolean sendPaymentReceipt(Payment payment) {
        if (payment == null || payment.getServiceRequest() == null ||
                payment.getServiceRequest().getCustomer() == null ||
                payment.getServiceRequest().getCustomer().getEmail() == null) {
            return false;
        }

        Customer customer = payment.getServiceRequest().getCustomer();
        String to = customer.getEmail();
        String subject = "Payment Receipt for Service #" + payment.getJobId();
        String htmlBody = "<html><body>" +
                "<h2>Payment Receipt</h2>" +
                "<p>Dear " + customer.getFirstName() + " " + customer.getLastName() + ",</p>" +
                "<p>Thank you for your payment. This email serves as your receipt:</p>" +
                "<table border='1' cellpadding='5' cellspacing='0'>" +
                "<tr><td><strong>Payment ID:</strong></td><td>" + payment.getPaymentId() + "</td></tr>" +
                "<tr><td><strong>Service Request ID:</strong></td><td>" + payment.getJobId() + "</td></tr>" +
                "<tr><td><strong>Description:</strong></td><td>" + payment.getServiceRequest().getDescription() + "</td></tr>" +
                "<tr><td><strong>Amount Paid:</strong></td><td>$" + String.format("%.2f", payment.getAmount()) + "</td></tr>" +
                "<tr><td><strong>Payment Date:</strong></td><td>" + DateTimeUtils.formatDate(payment.getPaymentDate()) + "</td></tr>";

        if (payment.getPaymentMethod() != null) {
            htmlBody += "<tr><td><strong>Payment Method:</strong></td><td>" + payment.getPaymentMethod() + "</td></tr>";
        }

        htmlBody += "</table>" +
                "<p>Thank you for your business.</p>" +
                "<p>Best regards,<br>Client Management System Team</p>" +
                "</body></html>";

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send a service reminder email to a customer
     * @param serviceRequest The service request
     * @param daysBefore Number of days before the service date
     * @return true if email was sent successfully
     */
    public static boolean sendServiceReminder(ServiceRequest serviceRequest, int daysBefore) {
        if (serviceRequest == null || serviceRequest.getCustomer() == null ||
                serviceRequest.getCustomer().getEmail() == null) {
            return false;
        }

        Customer customer = serviceRequest.getCustomer();
        String to = customer.getEmail();
        String subject = "Reminder: Upcoming Service - #" + serviceRequest.getJobId();
        String body = "Dear " + customer.getFirstName() + " " + customer.getLastName() + ",\n\n" +
                "This is a friendly reminder that you have a scheduled service in " + daysBefore +
                (daysBefore == 1 ? " day" : " days") + ":\n\n" +
                "Service Request ID: " + serviceRequest.getJobId() + "\n" +
                "Description: " + serviceRequest.getDescription() + "\n" +
                "Service Date: " + DateTimeUtils.formatDate(serviceRequest.getServiceDate()) + "\n";

        if (serviceRequest.getStartTime() != null) {
            body += "Start Time: " + DateTimeUtils.formatTime(serviceRequest.getStartTime()) + "\n";
        }

        if (serviceRequest.getEndTime() != null) {
            body += "End Time: " + DateTimeUtils.formatTime(serviceRequest.getEndTime()) + "\n";
        }

        body += "Service Location: " + serviceRequest.getServiceLocation() + "\n\n" +
                "Please ensure that the service location is accessible at the scheduled time.\n\n" +
                "If you need to reschedule or have any questions, please contact us as soon as possible.\n\n" +
                "Best regards,\n" +
                "Client Management System Team";

        return sendEmail(to, subject, body);
    }

    /**
     * Send a service assignment notification to a technician
     * @param serviceRequest The service request
     * @param technician The technician
     * @return true if email was sent successfully
     */
    public static boolean sendTechnicianAssignment(ServiceRequest serviceRequest, Technician technician) {
        if (serviceRequest == null || technician == null || technician.getEmail() == null) {
            return false;
        }

        String to = technician.getEmail();
        String subject = "New Service Assignment - #" + serviceRequest.getJobId();
        String body = "Dear " + technician.getFirstName() + " " + technician.getLastName() + ",\n\n" +
                "You have been assigned to the following service request:\n\n" +
                "Service Request ID: " + serviceRequest.getJobId() + "\n" +
                "Description: " + serviceRequest.getDescription() + "\n" +
                "Service Date: " + DateTimeUtils.formatDate(serviceRequest.getServiceDate()) + "\n";

        if (serviceRequest.getStartTime() != null) {
            body += "Start Time: " + DateTimeUtils.formatTime(serviceRequest.getStartTime()) + "\n";
        }

        if (serviceRequest.getEndTime() != null) {
            body += "End Time: " + DateTimeUtils.formatTime(serviceRequest.getEndTime()) + "\n";
        }

        body += "Service Location: " + serviceRequest.getServiceLocation() + "\n\n";

        if (serviceRequest.getCustomer() != null) {
            Customer customer = serviceRequest.getCustomer();
            body += "Customer Information:\n" +
                    "Name: " + customer.getFirstName() + " " + customer.getLastName() + "\n";

            if (customer.getPhoneNumber() != null) {
                body += "Phone: " + customer.getPhoneNumber() + "\n";
            }

            if (customer.getMobileNumber() != null) {
                body += "Mobile: " + customer.getMobileNumber() + "\n";
            }

            body += "\n";
        }

        body += "Please confirm that you are available for this assignment.\n\n" +
                "Best regards,\n" +
                "Client Management System Team";

        return sendEmail(to, subject, body);
    }

    /**
     * Configure and get email properties
     * @return Properties object with email configuration
     */
    private static Properties getEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", USE_TLS);
        return props;
    }

    /**
     * Get a mail session
     * @param props Properties object with email configuration
     * @return Mail session
     */
    private static Session getSession(Properties props) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }
}