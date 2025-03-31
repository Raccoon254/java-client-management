package com.management.component;

import com.management.model.Customer;
import com.management.model.Technician;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom cell factories for customers and technicians
 */
public class CustomCellFactories {

    /**
     * Create a cell factory for customer list items
     * @param searchText The text to highlight in the cell
     * @return A cell factory for customer list items
     */
    public static Callback<ListView<Customer>, ListCell<Customer>> createCustomerCellFactory(String searchText) {
        return listView -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);

                if (customer == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Create the container
                HBox container = new HBox(10);
                container.setAlignment(Pos.CENTER_LEFT);

                // Create avatar/initials circle
                Circle avatar = createAvatarCircle(customer.getFirstName(), customer.getLastName());

                // Create text container
                VBox textContainer = new VBox(2);
                textContainer.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(textContainer, Priority.ALWAYS);

                // Create name and details
                Label nameLabel = new Label(customer.getFirstName() + " " + customer.getLastName());
                nameLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

                // Highlight search text if present
                if (searchText != null && !searchText.isEmpty()) {
                    String name = customer.getFirstName() + " " + customer.getLastName();
                    if (name.toLowerCase().contains(searchText.toLowerCase())) {
                        nameLabel.setStyle("-fx-background-color: yellow;");
                    }
                }

                // Add details
                Label detailsLabel = new Label();
                if (customer.getCompanyName() != null && !customer.getCompanyName().isEmpty()) {
                    detailsLabel.setText(customer.getCompanyName() + " • " + customer.getCustomerNumber());
                } else {
                    detailsLabel.setText(customer.getCustomerNumber());
                }
                detailsLabel.setTextFill(Color.GRAY);

                textContainer.getChildren().addAll(nameLabel, detailsLabel);

                // Add elements to container
                container.getChildren().addAll(avatar, textContainer);

                setGraphic(container);
            }
        };
    }

    /**
     * Create a cell factory for technician list items
     * @param searchText The text to highlight in the cell
     * @param serviceDate The service date to check availability
     * @param assignedTechnicians List of already assigned technicians
     * @return A cell factory for technician list items
     */
    public static Callback<ListView<Technician>, ListCell<Technician>> createTechnicianCellFactory(
            String searchText, LocalDate serviceDate, List<Technician> assignedTechnicians) {

        return listView -> new ListCell<Technician>() {
            @Override
            protected void updateItem(Technician technician, boolean empty) {
                super.updateItem(technician, empty);

                if (technician == null || empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Create the container
                HBox container = new HBox(10);
                container.setAlignment(Pos.CENTER_LEFT);

                // Create avatar/initials circle
                Circle avatar = createAvatarCircle(technician.getFirstName(), technician.getLastName());

                // Create text container
                VBox textContainer = new VBox(2);
                textContainer.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(textContainer, Priority.ALWAYS);

                // Create name and details
                Label nameLabel = new Label(technician.getFirstName() + " " + technician.getLastName());
                nameLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

                // Highlight search text if present
                if (searchText != null && !searchText.isEmpty()) {
                    String name = technician.getFirstName() + " " + technician.getLastName();
                    if (name.toLowerCase().contains(searchText.toLowerCase())) {
                        nameLabel.setStyle("-fx-background-color: yellow;");
                    }
                }

                // Add details
                Label detailsLabel = new Label();
                if (technician.getCredentials() != null && !technician.getCredentials().isEmpty()) {
                    detailsLabel.setText(technician.getCredentials());
                } else if (technician.getCredentialLevel() != null && !technician.getCredentialLevel().isEmpty()) {
                    detailsLabel.setText("Level: " + technician.getCredentialLevel());
                } else {
                    detailsLabel.setText("Technician");
                }
                detailsLabel.setTextFill(Color.GRAY);

                textContainer.getChildren().addAll(nameLabel, detailsLabel);

                // Create availability indicator
                Label availabilityLabel = new Label();

                // Check if technician is already assigned
                boolean isAssigned = assignedTechnicians != null &&
                        assignedTechnicians.stream()
                                .anyMatch(t -> t.getTechnicianId() == technician.getTechnicianId());

                if (isAssigned) {
                    availabilityLabel.setText("Assigned");
                    availabilityLabel.setTextFill(Color.GREEN);
                } else {
                    // In a real app, you'd check the actual availability here
                    boolean isAvailable = true; // Placeholder - get real availability

                    if (isAvailable) {
                        availabilityLabel.setText("Available");
                        availabilityLabel.setTextFill(Color.GREEN);
                    } else {
                        availabilityLabel.setText("Unavailable");
                        availabilityLabel.setTextFill(Color.RED);
                    }
                }

                container.getChildren().addAll(avatar, textContainer, availabilityLabel);

                setGraphic(container);
            }
        };
    }

    /**
     * Create an avatar circle with initials
     * @param firstName The first name
     * @param lastName The last name
     * @return A circle with initials
     */
    private static Circle createAvatarCircle(String firstName, String lastName) {
        Circle circle = new Circle(20);

        // Get initials for avatar
        String initials = "";
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.charAt(0);
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.charAt(0);
        }

        // Set random background color based on name
        int hash = (firstName + lastName).hashCode();
        String[] colors = {
                "#1abc9c", "#2ecc71", "#3498db", "#9b59b6", "#34495e",
                "#16a085", "#27ae60", "#2980b9", "#8e44ad", "#2c3e50",
                "#f1c40f", "#e67e22", "#e74c3c", "#f39c12", "#d35400"
        };
        String color = colors[Math.abs(hash) % colors.length];

        circle.setFill(Color.web(color));

        // Create label for initials
        Label initialsLabel = new Label(initials);
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font(null, FontWeight.BOLD, 14));

        return circle;
    }
}