package com.management.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

/**
 * A framework for creating multi-step wizards
 */
public class WizardFramework {
    private final StackPane mainContainer;
    private final List<WizardStep> steps;
    private final IntegerProperty currentStepIndex;

    /**
     * Create a new wizard framework
     */
    public WizardFramework() {
        this.mainContainer = new StackPane();
        this.steps = new ArrayList<>();
        this.currentStepIndex = new SimpleIntegerProperty(0);

        // Add styling to the container
        mainContainer.getStyleClass().add("wizard-container");
    }

    /**
     * Add a step to the wizard
     * @param step The step to add
     */
    public void addStep(WizardStep step) {
        steps.add(step);
        mainContainer.getChildren().add(step.getContent());

        // Initially hide all steps except the first one
        if (steps.size() > 1) {
            step.getContent().setVisible(false);
        }
    }

    /**
     * Get the main container node
     * @return The main container node
     */
    public Node getContent() {
        return mainContainer;
    }

    /**
     * Move to the next step
     * @return true if moved to the next step, false if already at the last step
     */
    public boolean nextStep() {
        int currentIndex = currentStepIndex.get();
        if (currentIndex < steps.size() - 1) {
            if (steps.get(currentIndex).validate()) {
                // Hide current step
                steps.get(currentIndex).getContent().setVisible(false);

                // Show next step
                currentIndex++;
                steps.get(currentIndex).getContent().setVisible(true);
                steps.get(currentIndex).onEnter();
                currentStepIndex.set(currentIndex);

                return true;
            }
        }
        return false;
    }

    /**
     * Move to the previous step
     * @return true if moved to the previous step, false if already at the first step
     */
    public boolean previousStep() {
        int currentIndex = currentStepIndex.get();
        if (currentIndex > 0) {
            // Hide current step
            steps.get(currentIndex).getContent().setVisible(false);

            // Show previous step
            currentIndex--;
            steps.get(currentIndex).getContent().setVisible(true);
            steps.get(currentIndex).onEnter();
            currentStepIndex.set(currentIndex);

            return true;
        }
        return false;
    }

    /**
     * Get the current step
     * @return The current step
     */
    public WizardStep getCurrentStep() {
        return steps.get(currentStepIndex.get());
    }

    /**
     * Get the current step index
     * @return The current step index
     */
    public int getCurrentStepIndex() {
        return currentStepIndex.get();
    }

    /**
     * Get the current step index property
     * @return The current step index property
     */
    public IntegerProperty currentStepIndexProperty() {
        return currentStepIndex;
    }

    /**
     * Get the total number of steps
     * @return The total number of steps
     */
    public int getTotalSteps() {
        return steps.size();
    }

    /**
     * Check if the wizard is at the first step
     * @return true if at the first step
     */
    public boolean isFirstStep() {
        return currentStepIndex.get() == 0;
    }

    /**
     * Check if the wizard is at the last step
     * @return true if at the last step
     */
    public boolean isLastStep() {
        return currentStepIndex.get() == steps.size() - 1;
    }

    /**
     * Reset the wizard to the first step
     */
    public void reset() {
        // Hide current step
        steps.get(currentStepIndex.get()).getContent().setVisible(false);

        // Reset all steps
        for (WizardStep step : steps) {
            step.reset();
        }

        // Show first step
        steps.get(0).getContent().setVisible(true);
        steps.get(0).onEnter();
        currentStepIndex.set(0);
    }

    /**
     * An interface for wizard steps
     */
    public interface WizardStep {
        /**
         * Get the content node for this step
         * @return The content node
         */
        Node getContent();

        /**
         * Validate this step
         * @return true if valid
         */
        boolean validate();

        /**
         * Called when entering this step
         */
        void onEnter();

        /**
         * Reset this step
         */
        void reset();

        /**
         * Get the step title
         * @return The step title
         */
        String getTitle();
    }
}