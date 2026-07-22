package fr.esgi.fx.kanban.configuration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ThymeleafConfigurationTest {

    private ThymeleafConfiguration thymeleafConfiguration;

    @BeforeEach
    void setUp() {
        thymeleafConfiguration = new ThymeleafConfiguration();
    }

    @Test
    void testThymeleafConfigurationInstantiation_shouldSucceed() {
        assertNotNull(thymeleafConfiguration);
        assertDoesNotThrow(ThymeleafConfiguration::new);
    }

    /**
     * Vérifie que contextInitialized enregistre bien un TemplateEngine non nul
     * sous la clé "templateEngine" dans le ServletContext.
     * Ce test couvre les trois anciens tests redondants :
     *   - shouldCreateTemplateEngine (verify + any)
     *   - shouldStoreTemplateEngineInContext (captor clé + type)
     *   - shouldConfigureTemplateResolver (captor + assertNotNull)
     */
    @Test
    void testContextInitialized_shouldRegisterTemplateEngineInServletContext() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);
        ServletContext mockContext = mock(ServletContext.class);
        when(mockEvent.getServletContext()).thenReturn(mockContext);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        // Act
        thymeleafConfiguration.contextInitialized(mockEvent);

        // Assert
        verify(mockContext).setAttribute(keyCaptor.capture(), valueCaptor.capture());
        assertEquals("templateEngine", keyCaptor.getValue());
        assertNotNull(valueCaptor.getValue());
        assertInstanceOf(TemplateEngine.class, valueCaptor.getValue());
    }

    @Test
    void testContextInitialized_shouldNotThrowException() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);
        ServletContext mockContext = mock(ServletContext.class);
        when(mockEvent.getServletContext()).thenReturn(mockContext);

        // Act & Assert
        assertDoesNotThrow(() -> thymeleafConfiguration.contextInitialized(mockEvent));
    }

    @Test
    void testContextDestroyed_shouldNotThrowException() {
        // Arrange
        ServletContextEvent mockEvent = mock(ServletContextEvent.class);

        // Act & Assert
        assertDoesNotThrow(() -> thymeleafConfiguration.contextDestroyed(mockEvent));
    }
}
