package fr.esgi.fx.kanban.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface IStripeService {
    Session createCheckoutSession(long amountInCents, String currency, String productName,
                                  String successUrl, String cancelUrl) throws StripeException;

}

