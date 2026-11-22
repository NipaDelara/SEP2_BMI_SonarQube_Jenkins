package org.example.demo1;

import java.sql.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LocalizationService {

    // Logger instead of System.out and System.err
    private static final Logger LOGGER =
            Logger.getLogger(LocalizationService.class.getName());

    // Prevent creating objects of this utility class
    private LocalizationService() {
        throw new IllegalStateException("Utility class");
    }

    // Read database configuration from environment variables
    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);

        return value == null || value.isBlank()
                ? defaultValue
                : value;
    }

    // Build database connection URL
    private static String getDatabaseUrl() {
        String host = getEnv("DB_HOST", "localhost");
        String port = getEnv("DB_PORT", "3306");
        String database = getEnv("DB_NAME", "bmi_localization");

        return "jdbc:mysql://" + host + ":" + port + "/"
                + database
                + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    // Load translations from database
    public static Map<String, String> getLocalizedStrings(Locale locale) {

        Map<String, String> strings = new HashMap<>();

        String language = locale.getLanguage();
        String dbUrl = getDatabaseUrl();

        String dbUser = getEnv("DB_USER", "root");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbPassword == null || dbPassword.isBlank()) {
            LOGGER.warning("DB_PASSWORD is not configured.");
            return strings;
        }

        String query =
                "SELECT `key`, value FROM localization_strings "
                        + "WHERE language = ?";

        try (Connection conn =
                     DriverManager.getConnection(
                             dbUrl, dbUser, dbPassword);

             PreparedStatement stmt =
                     conn.prepareStatement(query)) {

            stmt.setString(1, language);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    strings.put(
                            rs.getString("key"),
                            rs.getString("value")
                    );
                }
            }

            LOGGER.log(
                    Level.INFO,
                    "Loaded {0} translations for language: {1}",
                    new Object[]{strings.size(), language}
            );

        } catch (SQLException e) {
            // Log a controlled message without dumping a stack trace
            LOGGER.warning("Unable to load localization strings from database.");
        }

        return strings;
    }
}
