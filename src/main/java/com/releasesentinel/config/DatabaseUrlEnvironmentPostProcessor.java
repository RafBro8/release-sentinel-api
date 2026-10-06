package com.releasesentinel.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Translates a platform supplied {@code DATABASE_URL} into the three properties Spring needs.
 *
 * <p>Render exposes a managed database to a service as a single connection string in the form
 * {@code postgresql://user:password@host:port/database}. The PostgreSQL JDBC driver cannot read
 * that form, and a Render Blueprint cannot build a {@code jdbc:} URL from its reference fields,
 * so the conversion has to happen here. Doing it this way means the Blueprint injects the
 * credentials itself and nobody copies a password by hand when the database is rebuilt.
 *
 * <p>When {@code DATABASE_URL} is absent or blank this does nothing, so local development and the
 * test suite keep using the ordinary {@code SPRING_DATASOURCE_*} configuration. When it is
 * present it takes precedence over those variables, which keeps a stale leftover value from
 * silently winning after a database is replaced.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "databaseUrl";
    private static final String DATABASE_URL = "DATABASE_URL";
    private static final String SCHEME_SEPARATOR = "://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty(DATABASE_URL);
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        Map<String, Object> properties = parse(databaseUrl.trim());
        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, properties));
    }

    private Map<String, Object> parse(String databaseUrl) {
        int schemeEnd = databaseUrl.indexOf(SCHEME_SEPARATOR);
        if (schemeEnd < 0) {
            throw new IllegalStateException(
                    "DATABASE_URL is not a valid connection string. Expected it to start with postgresql://");
        }

        String remainder = databaseUrl.substring(schemeEnd + SCHEME_SEPARATOR.length());

        // The password may legitimately contain a colon, so split the credentials on the last
        // separator rather than the first.
        int credentialsEnd = remainder.lastIndexOf('@');
        String credentials = credentialsEnd < 0 ? "" : remainder.substring(0, credentialsEnd);
        String hostAndDatabase = credentialsEnd < 0 ? remainder : remainder.substring(credentialsEnd + 1);

        if (!hostAndDatabase.contains("/")) {
            throw new IllegalStateException(
                    "DATABASE_URL does not name a database. Expected postgresql://user:password@host:port/database");
        }

        int passwordStart = credentials.indexOf(':');
        String username = passwordStart < 0 ? credentials : credentials.substring(0, passwordStart);
        String password = passwordStart < 0 ? "" : credentials.substring(passwordStart + 1);

        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", "jdbc:postgresql://" + hostAndDatabase);
        properties.put("spring.datasource.username", decode(username));
        properties.put("spring.datasource.password", decode(password));
        return properties;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
