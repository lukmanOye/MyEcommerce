package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripePaymentService {

    private final String stripeSecretKey;

    public StripePaymentService(@Value("${stripe.secret.key}") String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey;
        Stripe.apiKey = stripeSecretKey;
    }

    public boolean processPayment(Order order, String paymentMethodId) throws StripeException {
        long amountInCents = order.getTotalAmount()
                .multiply(new BigDecimal("100"))
                .longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return "succeeded".equals(paymentIntent.getStatus());
    }
}