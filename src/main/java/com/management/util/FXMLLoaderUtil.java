package com.management.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for loading FXML files and managing scenes
 */
public class FXMLLoaderUtil {
    private static final Logger LOGGER = Logger.getLogger(FXMLLoaderUtil.class.getName());

    // Private constructor to prevent instantiation
    private FXMLLoaderUtil() { }

    /**
     * Load an FXML file and return the loader
     * @param fxmlPath The path to the FXML file
     * @return The FXMLLoader
     */
    public static FXMLLoader getLoader(String fxmlPath) {
        return new FXMLLoader(FXMLLoaderUtil.class.getResource(fxmlPath));
    }

    /**
     * Load an FXML file and return the root element
     * @param fxmlPath The path to the FXML file
     * @return The loaded Parent node
     * @throws IOException If the FXML file cannot be loaded
     */
    public static Parent loadFXML(String fxmlPath) throws IOException {
        LOGGER.info("Loading FXML: " + fxmlPath);
        return getLoader(fxmlPath).load();
    }

    /**
     * Load an FXML file and return the controller
     * @param fxmlPath The path to the FXML file
     * @param <T> The type of the controller
     * @return The controller
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> T loadController(String fxmlPath) throws IOException {
        FXMLLoader loader = getLoader(fxmlPath);
        loader.load();
        return loader.getController();
    }

    /**
     * Load an FXML file and apply a configuration to the controller
     * @param fxmlPath The path to the FXML file
     * @param controllerConfig The configuration function to apply to the controller
     * @param <T> The type of the controller
     * @return The loaded Parent node
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> Parent loadFXML(String fxmlPath, Consumer<T> controllerConfig) throws IOException {
        FXMLLoader loader = getLoader(fxmlPath);
        Parent root = loader.load();
        T controller = loader.getController();
        controllerConfig.accept(controller);
        return root;
    }

    /**
     * Load an FXML file into a container
     * @param container The container to load the FXML into
     * @param fxmlPath The path to the FXML file
     * @return true if loading was successful
     */
    public static boolean loadIntoContainer(Pane container, String fxmlPath) {
        return loadIntoContainer(container, fxmlPath, "Error");
    }

    /**
     * Load an FXML file into a container
     * @param container The container to load the FXML into
     * @param fxmlPath The path to the FXML file
     * @param errorTitle The title for error alerts
     * @return true if loading was successful
     */
    public static boolean loadIntoContainer(Pane container, String fxmlPath, String errorTitle) {
        try {
            container.getChildren().clear();
            container.getChildren().add(loadFXML(fxmlPath));
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + fxmlPath, e);
            AlertUtils.showErrorAlert(errorTitle, "Failed to load view: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load an FXML file into a container with controller configuration
     * @param container The container to load the FXML into
     * @param fxmlPath The path to the FXML file
     * @param controllerConfig The configuration function to apply to the controller
     * @param <T> The type of the controller
     * @return true if loading was successful
     */
    public static <T> boolean loadIntoContainer(Pane container, String fxmlPath, Consumer<T> controllerConfig) {
        return loadIntoContainer(container, fxmlPath, controllerConfig, "Error");
    }

    /**
     * Load an FXML file into a container with controller configuration
     * @param container The container to load the FXML into
     * @param fxmlPath The path to the FXML file
     * @param controllerConfig The configuration function to apply to the controller
     * @param errorTitle The title for error alerts
     * @param <T> The type of the controller
     * @return true if loading was successful
     */

    public static <T> boolean loadIntoContainer(Pane container, String fxmlPath, Consumer<T> controllerConfig, String errorTitle) {
        try {
            FXMLLoader loader = getLoader(fxmlPath);
            Parent root = loader.load();
            T controller = loader.getController();
            controllerConfig.accept(controller);

            container.getChildren().clear();

            // If the container is a StackPane and has a ScrollPane child, use that for proper scrolling
            if (container instanceof StackPane) {
                StackPane stackPane = (StackPane) container;
                ScrollPane scrollPane = null;

                // First check if there's already a ScrollPane child
                for (Node child : stackPane.getChildren()) {
                    if (child instanceof ScrollPane) {
                        scrollPane = (ScrollPane) child;
                        break;
                    }
                }

                // If no ScrollPane found, create one
                if (scrollPane == null) {
                    scrollPane = new ScrollPane();
                    scrollPane.setFitToWidth(true);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.getStyleClass().add("content-scrollpane");
                    stackPane.getChildren().add(scrollPane);
                }

                // Configure the content properly
                VBox contentWrapper = new VBox(root);
                contentWrapper.setFillWidth(true);

                // This ensures the content takes all available width but can grow vertically
                VBox.setVgrow(root, Priority.ALWAYS);

                scrollPane.setContent(contentWrapper);

                return true;
            }

            // Fallback if not a StackPane - wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(root);
            scrollPane.setFitToWidth(true);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            container.getChildren().add(scrollPane);

            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + fxmlPath, e);
            AlertUtils.showErrorAlert(errorTitle, "Failed to load view: " + e.getMessage());
            return false;
        }
    }

    /**
     * Open a new window from an FXML file
     * @param fxmlPath The path to the FXML file
     * @param title The window title
     * @param ownerWindow The owner window
     * @param modality The modality type
     * @return The created Stage
     * @throws IOException If the FXML file cannot be loaded
     */
    public static Stage openNewWindow(String fxmlPath, String title, Window ownerWindow, Modality modality) throws IOException {
        Parent root = loadFXML(fxmlPath);
        Scene scene = new Scene(root);
        return configureStage(scene, title, ownerWindow, modality);
    }

    /**
     * Open a new window from an FXML file with CSS
     * @param fxmlPath The path to the FXML file
     * @param title The window title
     * @param ownerWindow The owner window
     * @param modality The modality type
     * @param stylesheets The CSS stylesheets to apply
     * @return The created Stage
     * @throws IOException If the FXML file cannot be loaded
     */
    public static Stage openNewWindow(String fxmlPath, String title, Window ownerWindow,
                                      Modality modality, List<String> stylesheets) throws IOException {
        Parent root = loadFXML(fxmlPath);
        Scene scene = new Scene(root);
        stylesheets.forEach(css -> scene.getStylesheets().add(FXMLLoaderUtil.class.getResource(css).toExternalForm()));
        return configureStage(scene, title, ownerWindow, modality);
    }

    /**
     * Open a new window from an FXML file with controller configuration
     * @param fxmlPath The path to the FXML file
     * @param title The window title
     * @param ownerWindow The owner window
     * @param modality The modality type
     * @param controllerConfig The configuration function to apply to the controller
     * @param <T> The type of the controller
     * @return The created Stage
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> Stage openNewWindow(String fxmlPath, String title, Window ownerWindow,
                                          Modality modality, Consumer<T> controllerConfig) throws IOException {
        FXMLLoader loader = getLoader(fxmlPath);
        Parent root = loader.load();
        T controller = loader.getController();
        controllerConfig.accept(controller);

        Scene scene = new Scene(root);
        return configureStage(scene, title, ownerWindow, modality);
    }

    /**
     * Open a dialog from an FXML file
     * @param fxmlPath The path to the FXML file
     * @param title The dialog title
     * @param ownerWindow The owner window
     * @return The created Stage
     */
    public static Stage openDialog(String fxmlPath, String title, Window ownerWindow) {
        try {
            Stage stage = openNewWindow(fxmlPath, title, ownerWindow, Modality.APPLICATION_MODAL);
            stage.showAndWait();
            return stage;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open dialog: " + fxmlPath, e);
            AlertUtils.showErrorAlert("Error", "Failed to open dialog: " + e.getMessage());
            return null;
        }
    }

    /**
     * Open a dialog from an FXML file with controller configuration
     * @param fxmlPath The path to the FXML file
     * @param title The dialog title
     * @param ownerWindow The owner window
     * @param controllerConfig The configuration function to apply to the controller
     * @param <T> The type of the controller
     * @return The created Stage
     */
    public static <T> Stage openDialog(String fxmlPath, String title, Window ownerWindow, Consumer<T> controllerConfig) {
        try {
            Stage stage = openNewWindow(fxmlPath, title, ownerWindow, Modality.APPLICATION_MODAL, controllerConfig);
            stage.showAndWait();
            return stage;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open dialog: " + fxmlPath, e);
            AlertUtils.showErrorAlert("Error", "Failed to open dialog: " + e.getMessage());
            return null;
        }
    }

    /**
     * Change the scene of a stage to a new FXML file
     * @param stage The stage to change the scene of
     * @param fxmlPath The path to the FXML file
     * @param title The new window title
     * @return true if the scene change was successful
     */
    public static boolean changeScene(Stage stage, String fxmlPath, String title) {
        try {
            Parent root = loadFXML(fxmlPath);
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to change scene: " + fxmlPath, e);
            AlertUtils.showErrorAlert("Error", "Failed to change scene: " + e.getMessage());
            return false;
        }
    }

    /**
     * Change the scene of a stage to a new FXML file with CSS
     * @param stage The stage to change the scene of
     * @param fxmlPath The path to the FXML file
     * @param title The new window title
     * @param stylesheets The CSS stylesheets to apply
     * @return true if the scene change was successful
     */
    public static boolean changeScene(Stage stage, String fxmlPath, String title, List<String> stylesheets) {
        try {
            Parent root = loadFXML(fxmlPath);
            Scene scene = new Scene(root);
            stylesheets.forEach(css -> scene.getStylesheets().add(FXMLLoaderUtil.class.getResource(css).toExternalForm()));
            stage.setTitle(title);
            stage.setScene(scene);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to change scene: " + fxmlPath, e);
            AlertUtils.showErrorAlert("Error", "Failed to change scene: " + e.getMessage());
            return false;
        }
    }

    /**
     * Change the scene of a stage to a new FXML file with controller configuration
     * @param stage The stage to change the scene of
     * @param fxmlPath The path to the FXML file
     * @param title The new window title
     * @param controllerConfig The configuration function to apply to the controller
     * @param <T> The type of the controller
     * @return true if the scene change was successful
     */
    public static <T> boolean changeScene(Stage stage, String fxmlPath, String title, Consumer<T> controllerConfig) {
        try {
            FXMLLoader loader = getLoader(fxmlPath);
            Parent root = loader.load();
            T controller = loader.getController();
            controllerConfig.accept(controller);

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to change scene: " + fxmlPath, e);
            AlertUtils.showErrorAlert("Error", "Failed to change scene: " + e.getMessage());
            return false;
        }
    }

    /**
     * Configure a stage with common settings
     * @param scene The scene to set
     * @param title The window title
     * @param ownerWindow The owner window
     * @param modality The modality type
     * @return The configured Stage
     */
    private static Stage configureStage(Scene scene, String title, Window ownerWindow, Modality modality) {
        Stage stage = new Stage();
        if (ownerWindow != null) {
            stage.initOwner(ownerWindow);
        }
        if (modality != null) {
            stage.initModality(modality);
        }
        stage.setTitle(title);
        stage.setScene(scene);
        return stage;
    }
}