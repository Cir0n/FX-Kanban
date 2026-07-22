package fr.esgi.fx.kanban.configuration;

import fr.esgi.fx.kanban.service.IStripeService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeConfigurationTest {

    private StripeConfiguration stripeConfiguration;

    @BeforeEach
    void setUp() {
        stripeConfiguration = new StripeConfiguration();
        // Garantit un état neutre : aucune propriété système résiduelle d'un test précédent
        System.clearProperty("stripe.api.key");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("stripe.api.key");
    }

    @Test
    void testStripeConfigurationInstantiation_shouldSucceed() {
        // Act & Assert
        assertNotNull(stripeConfiguration);
        assertDoesNotThrow(StripeConfiguration::new);
    }

    @Test
    void testContextInitialized_whenApiKeyIsNotConfigured_shouldNotSetAttribute() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);

        // Act
        stripeConfiguration.contextInitialized(mockEvent);

        // Assert
        // Sans clé API, l'initialisation s'arrête avant d'accéder au ServletContext.
        verify(mockEvent, never()).getServletContext();
    }

    @Test
    void testContextInitialized_whenApiKeyIsConfigured_shouldInitializeService() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);
        ServletContext mockContext = mock(ServletContext.class);
        when(mockEvent.getServletContext()).thenReturn(mockContext);
        System.setProperty("stripe.api.key", "sk_test_123456789");

        // Act
        stripeConfiguration.contextInitialized(mockEvent);

        // Assert
        verify(mockContext, times(1)).setAttribute(
                eq(StripeConfiguration.STRIPE_SERVICE_CONTEXT_KEY),
                any(IStripeService.class)
        );
    }

    @Test
    void testContextInitialized_withBlankApiKey_shouldNotSetAttribute() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);
        System.setProperty("stripe.api.key", "   ");

        // Act
        stripeConfiguration.contextInitialized(mockEvent);

        // Assert : la clé vide est rejetée avant d'accéder au ServletContext
        verify(mockEvent, never()).getServletContext();
    }

    @Test
    void testContextDestroyed_shouldNotThrowException() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);

        // Act & Assert
        assertDoesNotThrow(() -> stripeConfiguration.contextDestroyed(mockEvent));
    }

    @Test
    void testStripeServiceContextKeyConstant_shouldBeCorrect() {
        // Assert
        assertNotNull(StripeConfiguration.STRIPE_SERVICE_CONTEXT_KEY);
        assertEquals("stripeService", StripeConfiguration.STRIPE_SERVICE_CONTEXT_KEY);
    }
}





