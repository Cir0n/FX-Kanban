package fr.esgi.fx.kanban.service;

import com.stripe.Stripe;
import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import fr.esgi.fx.kanban.service.implementation.StripeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class StripeServiceTest {

    /**
     * Stripe.apiKey est un champ statique global de la bibliothèque Stripe.
     * Sa valeur est partagée entre tous les tests de la JVM.
     * On le remet à null après chaque test pour garantir l'isolation.
     */
    @AfterEach
    void resetStripeApiKey() {
        Stripe.apiKey = null;
    }

    @Test
    void testConstructor_shouldSetStripeApiKey() {
        new StripeServiceImpl("sk_test_abc");

        assertEquals("sk_test_abc", Stripe.apiKey);
    }

    @Test
    void testConstructor_whenApiKeyChanges_shouldOverwriteStripeApiKey() {
        new StripeServiceImpl("sk_test_old");
        new StripeServiceImpl("sk_test_new");

        assertEquals("sk_test_new", Stripe.apiKey);
    }

    @Test
    void testCreateCheckoutSession_shouldBuildParamsAndReturnSession() throws Exception {
        StripeServiceImpl stripeService = new StripeServiceImpl("sk_test_123");
        Session expected = mock(Session.class);
        AtomicReference<SessionCreateParams> captured = new AtomicReference<>();

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(org.mockito.ArgumentMatchers.any(SessionCreateParams.class)))
                    .thenAnswer(invocation -> {
                        captured.set(invocation.getArgument(0));
                        return expected;
                    });

            Session result = stripeService.createCheckoutSession(
                    500L,
                    "eur",
                    "Board Premium",
                    "https://app/success",
                    "https://app/cancel");

            assertSame(expected, result);
            SessionCreateParams params = captured.get();
            assertEquals(SessionCreateParams.Mode.PAYMENT, params.getMode());
            assertEquals("https://app/success", params.getSuccessUrl());
            assertEquals("https://app/cancel", params.getCancelUrl());
            assertEquals(1, params.getLineItems().size());
            assertEquals(500L, params.getLineItems().getFirst().getPriceData().getUnitAmount());
            assertEquals("eur", params.getLineItems().getFirst().getPriceData().getCurrency());
            assertEquals("Board Premium", params.getLineItems().getFirst().getPriceData().getProductData().getName());
        }
    }

    @Test
    void testCreateCheckoutSession_whenStripeThrows_shouldPropagate() {
        StripeServiceImpl stripeService = new StripeServiceImpl("sk_test_123");
        StripeException stripeException = new ApiException("error", "req_1", "code", 400, null);

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(org.mockito.ArgumentMatchers.any(SessionCreateParams.class)))
                    .thenThrow(stripeException);

            StripeException thrown = assertThrows(StripeException.class,
                    () -> stripeService.createCheckoutSession(
                            100L,
                            "eur",
                            "X",
                            "https://s",
                            "https://c"));
            assertSame(stripeException, thrown);
        }
    }

    @Test
    void testRetrieveSession_shouldReturnSession() throws Exception {
        StripeServiceImpl stripeService = new StripeServiceImpl("sk_test_123");
        Session expected = mock(Session.class);

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.retrieve("cs_123")).thenReturn(expected);

            Session result = stripeService.retrieveSession("cs_123");

            assertSame(expected, result);
        }
    }

    @Test
    void testRetrieveSession_whenStripeThrows_shouldPropagate() {
        StripeServiceImpl stripeService = new StripeServiceImpl("sk_test_123");
        StripeException stripeException = new ApiException("error", "req_1", "code", 400, null);

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.retrieve("cs_123")).thenThrow(stripeException);

            StripeException thrown = assertThrows(StripeException.class,
                    () -> stripeService.retrieveSession("cs_123"));
            assertSame(stripeException, thrown);
        }
    }
}

