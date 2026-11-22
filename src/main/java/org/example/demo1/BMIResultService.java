package org.example.demo1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BMIResultService {

    // Logger instead of System.out and System.err
    private static final Logger LOGGER =
            Logger.getLogger(BMIResultService.class.getName());

    // Prevent creating instances of this utility class
    private BMIResultService() {
        throw new IllegalStateException("Utility class");
    }

    // Read database settings from environment variables
    private static String getEnv(
            String name,
            String defaultValue) {

        String value = System.getenv(name);

        return value == null || value.isBlank()
                ? defaultValue
                : value;
    }

    // Build MariaDB database connection URL
    private static String getDatabaseUrl() {
            String host = getEnv("DB_HOST", "localhost");
            String port = getEnv("DB_PORT", "3306");
            String database = getEnv("DB_NAME", "bmi_localization");

            return "jdbc:mariadb://" + host + ":" + port + "/" + database;
    }
    // Save BMI calculation result to MariaDB
    public static void saveResult(
            double weight,
            double height,
            double bmi,
            String language) {

        // Read database credentials
        String dbUser = getEnv("DB_USER", "root");
        String dbPassword = System.getenv("DB_PASSWORD");

        // Validate database password configuration
        if (dbPassword == null || dbPassword.isBlank()) {

            LOGGER.warning(
                    "Database password is not configured."
            );

            return;
        }

        // SQL query using PreparedStatement
        String query =
                "INSERT INTO bmi_results "
                        + "(weight, height, bmi, language) "
                        + "VALUES (?, ?, ?, ?)";

        // Connect to MariaDB
        try (Connection conn =
                     DriverManager.getConnection(
                             getDatabaseUrl(),
                             dbUser,
                             dbPassword
                     );

             PreparedStatement stmt =
                     conn.prepareStatement(query)) {

            // Set SQL parameters
            stmt.setDouble(1, weight);
            stmt.setDouble(2, height);
            stmt.setDouble(3, bmi);
            stmt.setString(4, language);

            // Execute INSERT query
            int rowsInserted = stmt.executeUpdate();

            // Log successful database operation
            LOGGER.log(
                    Level.INFO,
                    "BMI result saved successfully. Rows inserted: {0}",
                    rowsInserted
            );

        } catch (SQLException e) {

            LOGGER.log(
                    Level.WARNING,
                    "Unable to save BMI result to database. SQLState: "
                            + e.getSQLState()
                            + ", Error code: "
                            + e.getErrorCode(),
                    e
            );
        }
    }
}
