package fr.esgi.fx.kanban.service.implementation;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import fr.esgi.fx.kanban.service.IStripeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StripeServiceImpl implements IStripeService {

    private static final Logger LOGGER = LogManager.getLogger(StripeServiceImpl.class);

    public StripeServiceImpl(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Override
    public Session createCheckoutSession(long amountInCents, String currency, String productName,
                                         String successUrl, String cancelUrl) throws StripeException {
        LOGGER.info("Création d'une session de paiement Stripe pour '{}' ({} {})", productName, amountInCents, currency);
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(productName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            return Session.create(params);
        } catch (StripeException e) {
            LOGGER.error("Échec de la création de la session de paiement Stripe pour '{}'", productName, e);
            throw e;
        }
    }

    @Override
    public Session retrieveSession(String sessionId) throws StripeException {
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            LOGGER.error("Échec de la récupération de la session de paiement Stripe id={}", sessionId, e);
            throw e;
        }
    }
}

