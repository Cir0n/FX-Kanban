package fr.esgi.fx.kanban.service.implementation;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import fr.esgi.fx.kanban.service.IStripeService;

public class StripeServiceImpl implements IStripeService {

    public StripeServiceImpl(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Override
    public Session createCheckoutSession(long amountInCents, String currency, String productName,
                                         String successUrl, String cancelUrl) throws StripeException {
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

        return Session.create(params);
    }

    @Override
    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }
}

