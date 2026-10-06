package com.releasesentinel.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    void convertsRenderConnectionStringIntoJdbcProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://release_sentinel:s3cret@dpg-abc123-a:5432/release_sentinel");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://dpg-abc123-a:5432/release_sentinel");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("release_sentinel");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("s3cret");
    }

    @Test
    void acceptsAConnectionStringWithoutAPort() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://user:pw@dpg-abc123-a/release_sentinel");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://dpg-abc123-a/release_sentinel");
    }

    @Test
    void keepsQueryParametersSuchAsSslMode() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://user:pw@db.example.com/sentinel?sslmode=require");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com/sentinel?sslmode=require");
    }

    @Test
    void splitsCredentialsOnTheLastSeparatorSoPasswordsMayContainThem() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://user:pa:ss@word@dpg-abc123-a/sentinel");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("user");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("pa:ss@word");
    }

    @Test
    void decodesPercentEncodedCredentials() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://user:p%40ssword@dpg-abc123-a/sentinel");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("p@ssword");
    }

    @Test
    void leavesExistingConfigurationAloneWhenDatabaseUrlIsAbsent() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/release_sentinel");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://localhost:5432/release_sentinel");
    }

    @Test
    void treatsABlankDatabaseUrlAsAbsent() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "   ")
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/release_sentinel");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://localhost:5432/release_sentinel");
    }

    @Test
    void overridesStaleDatasourceConfigurationWhenDatabaseUrlIsPresent() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://dpg-deleted-a/release_sentinel")
                .withProperty("DATABASE_URL", "postgresql://user:pw@dpg-current-a/release_sentinel");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://dpg-current-a/release_sentinel");
    }

    @Test
    void rejectsAConnectionStringWithNoDatabaseName() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://user:pw@dpg-abc123-a");

        assertThatThrownBy(() -> processor.postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not name a database");
    }

    @Test
    void rejectsAValueThatIsNotAConnectionString() {
        MockEnvironment environment = new MockEnvironment().withProperty("DATABASE_URL", "not-a-url");

        assertThatThrownBy(() -> processor.postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("postgresql://");
    }
}
