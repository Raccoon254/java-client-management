package com.management.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A custom TextField with autocomplete capabilities.
 * @param <T> The type of items to suggest
 */
public class AutoCompleteTextField<T> extends TextField {
    private final ObservableList<T> originalItems;
    private final ObservableList<T> filteredItems;
    private final ListView<T> suggestionList;
    private final Popup popup;
    private final Function<T, String> displayTextExtractor;
    private final StringConverter<T> converter;
    private T selectedItem;
    private boolean ignoreTextChange = false;

    /**
     * Create a new autocomplete text field
     * @param items The initial list of items for suggestions
     * @param displayTextExtractor Function to extract display text from an item
     */
    public AutoCompleteTextField(List<T> items, Function<T, String> displayTextExtractor) {
        this.originalItems = FXCollections.observableArrayList(items);
        this.filteredItems = FXCollections.observableArrayList(items);
        this.suggestionList = new ListView<>(filteredItems);
        this.popup = new Popup();
        this.displayTextExtractor = displayTextExtractor;

        this.converter = new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object != null ? displayTextExtractor.apply(object) : "";
            }

            @Override
            public T fromString(String string) {
                // Find the first item that matches the string
                return originalItems.stream()
                        .filter(item -> displayTextExtractor.apply(item).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        };

        setupTextField();
        setupSuggestionList();
        setupPopup();
    }

    /**
     * Setup the text field behavior
     */
    private void setupTextField() {
        // Add listener for text changes
        textProperty().addListener((observable, oldValue, newValue) -> {
            if (ignoreTextChange) {
                ignoreTextChange = false;
                return;
            }

            if (newValue == null || newValue.isEmpty()) {
                hidePopup();
                selectedItem = null;
                filteredItems.setAll(originalItems);
            } else {
                filterItems(newValue);
                if (!filteredItems.isEmpty()) {
                    showPopup();
                } else {
                    hidePopup();
                }
            }
        });

        // Handle key events for navigation
        setOnKeyPressed(event -> {
            if (popup.isShowing()) {
                if (event.getCode() == KeyCode.DOWN) {
                    suggestionList.getSelectionModel().selectNext();
                    suggestionList.scrollTo(suggestionList.getSelectionModel().getSelectedIndex());
                    event.consume();
                } else if (event.getCode() == KeyCode.UP) {
                    suggestionList.getSelectionModel().selectPrevious();
                    suggestionList.scrollTo(suggestionList.getSelectionModel().getSelectedIndex());
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER) {
                    selectItem(suggestionList.getSelectionModel().getSelectedItem());
                    event.consume();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    hidePopup();
                    event.consume();
                }
            }
        });

        // Handle focus events
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Lost focus
                hidePopup();
            } else if (!getText().isEmpty()) { // Gained focus with text
                filterItems(getText());
                if (!filteredItems.isEmpty()) {
                    showPopup();
                }
            }
        });
    }

    /**
     * Setup the suggestion list
     */
    private void setupSuggestionList() {
        suggestionList.setPrefWidth(this.getPrefWidth());
        suggestionList.setPrefHeight(200);

        // Set cell factory to highlight matching text
        suggestionList.setCellFactory(listView -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String text = displayTextExtractor.apply(item);
                    setText(text);

                    // Optionally add CSS styling or other customizations
                    if (item.equals(selectedItem)) {
                        getStyleClass().add("selected-item");
                    } else {
                        getStyleClass().removeAll("selected-item");
                    }
                }
            }
        });

        // Handle selection
        suggestionList.setOnMouseClicked(event -> {
            T selectedItem = suggestionList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                selectItem(selectedItem);
            }
        });
    }

    /**
     * Setup the popup to show suggestions
     */
    private void setupPopup() {
        VBox container = new VBox(suggestionList);
        container.getStyleClass().add("autocomplete-popup");
        popup.getContent().add(container);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        // Add event filter to prevent hiding when clicking inside
        popup.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() == popup) {
                event.consume();
            }
        });
    }

    /**
     * Filter the items based on input text
     * @param text The text to filter by
     */
    private void filterItems(String text) {
        String lowerCaseText = text.toLowerCase();

        List<T> filtered = originalItems.stream()
                .filter(item -> {
                    String itemText = displayTextExtractor.apply(item).toLowerCase();
                    return itemText.contains(lowerCaseText);
                })
                .collect(Collectors.toList());

        filteredItems.setAll(filtered);

        if (!filteredItems.isEmpty()) {
            suggestionList.getSelectionModel().select(0);
        }
    }

    /**
     * Show the popup with suggestions
     */
    private void showPopup() {
        if (!popup.isShowing()) {
            Bounds bounds = localToScreen(getBoundsInLocal());
            popup.show(this, bounds.getMinX(), bounds.getMaxY());
        }
    }

    /**
     * Hide the popup
     */
    private void hidePopup() {
        popup.hide();
    }

    /**
     * Select an item from the suggestions
     * @param item The item to select
     */
    private void selectItem(T item) {
        if (item != null) {
            selectedItem = item;

            // Set text without triggering filter
            ignoreTextChange = true;
            setText(displayTextExtractor.apply(item));

            // Position caret at end
            positionCaret(getText().length());

            // Hide popup
            hidePopup();

            // Fire event for selection
            fireEvent(new CustomEvent(CustomEvent.ITEM_SELECTED, item));
        }
    }

    /**
     * Get the selected item
     * @return The selected item
     */
    public T getSelectedItem() {
        return selectedItem;
    }

    /**
     * Set the selected item
     * @param item The item to select
     */
    public void setSelectedItem(T item) {
        if (item != null) {
            selectItem(item);
        } else {
            selectedItem = null;
            setText("");
        }
    }

    /**
     * Update the list of items
     * @param items The new list of items
     */
    public void updateItems(List<T> items) {
        originalItems.setAll(items);

        if (getText().isEmpty()) {
            filteredItems.setAll(items);
        } else {
            filterItems(getText());
        }
    }

    /**
     * Reset the selection
     */
    public void reset() {
        selectedItem = null;
        setText("");
        filteredItems.setAll(originalItems);
    }

    /**
     * Custom event class for selection events
     */
    public static class CustomEvent<T> extends javafx.event.Event {
        public static final javafx.event.EventType<CustomEvent> ITEM_SELECTED =
                new javafx.event.EventType<>("ITEM_SELECTED");

        private final T item;

        public CustomEvent(javafx.event.EventType<CustomEvent> eventType, T item) {
            super(eventType);
            this.item = item;
        }

        public T getItem() {
            return item;
        }
    }

    /**
     * Add handler for item selection
     * @param handler The handler for selection events
     */
    public void setOnItemSelected(EventHandler<CustomEvent> handler) {
        addEventHandler(CustomEvent.ITEM_SELECTED, handler);
    }

    /**
     * Get the string converter
     * @return The string converter
     */
    public StringConverter<T> getConverter() {
        return converter;
    }
}