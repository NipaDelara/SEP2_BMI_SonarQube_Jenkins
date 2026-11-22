
package org.example.demo1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class BMIController {

    // Constants to avoid duplicate string literals
    private static final String INVALID_KEY = "invalid";
    private static final String INVALID_MESSAGE = "Invalid input";

    // FXML components
    @FXML
    private Label welcomeText;

    @FXML
    private Label lblWeight;

    @FXML
    private Label lblHeight;

    @FXML
    private TextField tfWeight;

    @FXML
    private TextField tfHeight;

    @FXML
    private Button btnCalculate;

    @FXML
    private Label lblResult;

    @FXML
    private Label lblLocalTime;

    @FXML
    private VBox rootVBox;

    // Current selected language
    private Locale currentLocale = Locale.US;

    // Translations loaded from database
    private Map<String, String> localizedStrings = Map.of();

    // Clock with explicit system time zone
    private final Clock clock = Clock.systemDefaultZone();

    // Initialize the application
    @FXML
    public void initialize() {
        setLanguage(Locale.US);
    }

    // Change application language
    private void setLanguage(Locale locale) {

        currentLocale = locale;

        // Clear previous BMI result
        lblResult.setText("");

        // Load translations from MariaDB
        localizedStrings =
                LocalizationService.getLocalizedStrings(locale);

        // Update weight label
        lblWeight.setText(
                localizedStrings.getOrDefault(
                        "weight",
                        "Weight"
                )
        );

        // Update height label
        lblHeight.setText(
                localizedStrings.getOrDefault(
                        "height",
                        "Height"
                )
        );

        // Update calculate button
        btnCalculate.setText(
                localizedStrings.getOrDefault(
                        "calculate",
                        "Calculate"
                )
        );

        // Update time and layout direction
        displayLocalTime(locale);
        applyTextDirection(locale);
    }

    // Calculate BMI and save result
    @FXML
    public void onCalculateClick() {

        try {
            // Read weight in kilograms
            double weight =
                    Double.parseDouble(tfWeight.getText());

            // Convert height from centimeters to meters
            double height =
                    Double.parseDouble(tfHeight.getText()) / 100.0;

            // Validate weight and height
            if (!Double.isFinite(weight)
                    || !Double.isFinite(height)
                    || weight <= 0
                    || height <= 0) {

                lblResult.setText(
                        localizedStrings.getOrDefault(
                                INVALID_KEY,
                                INVALID_MESSAGE
                        )
                );
                return;
            }

            // Calculate BMI
            double bmi = weight / (height * height);

            // Validate calculated BMI
            if (!Double.isFinite(bmi)) {

                lblResult.setText(
                        localizedStrings.getOrDefault(
                                INVALID_KEY,
                                INVALID_MESSAGE
                        )
                );
                return;
            }

            // Format BMI to two decimal places
            DecimalFormat df =
                    new DecimalFormat("#0.00");

            String resultText =
                    localizedStrings.getOrDefault(
                            "result",
                            "Your BMI is"
                    );

            // Display BMI result
            lblResult.setText(
                    resultText + " " + df.format(bmi)
            );

            // Save BMI result to database
            String language =
                    currentLocale.getLanguage();

            BMIResultService.saveResult(
                    weight,
                    height,
                    bmi,
                    language
            );

        } catch (NumberFormatException e) {

            // Handle invalid numeric input
            lblResult.setText(
                    localizedStrings.getOrDefault(
                            INVALID_KEY,
                            INVALID_MESSAGE
                    )
            );
        }
    }

    // English language
    @FXML
    public void onENClick() {
        setLanguage(Locale.US);
    }

    // French language
    @FXML
    public void onFRClick() {
        setLanguage(Locale.FRANCE);
    }

    // Urdu language
    @FXML
    public void onURClick() {
        setLanguage(Locale.forLanguageTag("ur-PK"));
    }

    // Vietnamese language
    @FXML
    public void onVIClick() {
        setLanguage(Locale.forLanguageTag("vi-VN"));
    }

    // Persian language
    @FXML
    public void onFAClick() {
        setLanguage(Locale.forLanguageTag("fa-IR"));
    }

    // Display local time
    private void displayLocalTime(Locale locale) {

        // Get time using explicit clock
        LocalTime currentTime =
                LocalTime.now(clock);

        // Format time according to selected locale
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "HH:mm:ss",
                        locale
                );

        String formattedTime =
                currentTime.format(formatter);

        // Update time label
        lblLocalTime.setText(
                localizedStrings.getOrDefault(
                        "localTime",
                        "Local Time"
                ) + " " + formattedTime
        );
    }

    // Apply RTL or LTR layout
    private void applyTextDirection(Locale locale) {

        String language = locale.getLanguage();

        // Determine whether language uses RTL
        boolean isRTL =
                language.equals("fa")
                        || language.equals("ur")
                        || language.equals("ar")
                        || language.equals("he");

        Platform.runLater(() -> {

            // Change layout direction
            if (rootVBox != null) {

                rootVBox.setNodeOrientation(
                        isRTL
                                ? NodeOrientation.RIGHT_TO_LEFT
                                : NodeOrientation.LEFT_TO_RIGHT
                );
            }

            // Align weight input
            tfWeight.setStyle(
                    isRTL
                            ? "-fx-text-alignment: right;"
                            : "-fx-text-alignment: left;"
            );

            // Align height input
            tfHeight.setStyle(
                    isRTL
                            ? "-fx-text-alignment: right;"
                            : "-fx-text-alignment: left;"
            );
        });
    }
}
