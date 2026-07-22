package fr.esgi.fx.kanban.configuration;

import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DatabaseConfigurationTest {

    private DatabaseConfiguration databaseConfiguration;

    @BeforeEach
    void setUp() {
        databaseConfiguration = new DatabaseConfiguration();
    }

    @Test
    void testDatabaseConfigurationInstantiation_shouldSucceed() {
        // Act & Assert
        assertNotNull(databaseConfiguration);
        assertDoesNotThrow(DatabaseConfiguration::new);
    }

    @Test
    void testContextDestroyed_shouldNotThrowException() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);

        // Act & Assert - pas d'exception levée
        assertDoesNotThrow(() -> databaseConfiguration.contextDestroyed(mockEvent));
    }

    @Test
    void testContextInitialized_implementsServletContextListener() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);

        // Act & Assert
        assertDoesNotThrow(() -> databaseConfiguration.contextInitialized(mockEvent));
    }
}




