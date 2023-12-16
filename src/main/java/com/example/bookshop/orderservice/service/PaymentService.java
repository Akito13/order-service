package com.example.bookshop.orderservice.service;

import com.example.bookshop.orderservice.exception.PaymentException;
import com.example.bookshop.orderservice.exception.RefundException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final String stripeSecreyKey;

    public PaymentService(@Value("${payment.stripe.secret-key}") String stripeSecreyKey) {
        this.stripeSecreyKey = stripeSecreyKey;
    }

    public PaymentIntent createPaymentIntent(Long amount, String paymentId, String receiptEmail) throws StripeException {
        Stripe.apiKey = stripeSecreyKey;
        PaymentIntentCreateParams intentCreateParams = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setConfirm(true)
                .setCurrency("VND")
                .setReceiptEmail(receiptEmail)
                .setPaymentMethod(paymentId)
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                        .build())
                .build();
        return PaymentIntent.create(intentCreateParams);
    }

    public void refundPaymentIntent(String paymentId) throws StripeException {
        Stripe.apiKey = stripeSecreyKey;
        RefundCreateParams refundCreateParams = RefundCreateParams.builder().setPaymentIntent(paymentId).setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER).build();
        Refund refund = Refund.create(refundCreateParams);
        if (refund.getStatus().equals("failed")){
            System.out.println(refund.getFailureReason());
//            throw new PaymentException("Gặp lỗi hoàn trả", refund.getId(), refund.getCharge(), 500);
            throw new RefundException("Hoàn trả thất bại");
        }
    }
}
