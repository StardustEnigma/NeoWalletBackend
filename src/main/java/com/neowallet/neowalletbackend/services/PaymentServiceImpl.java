package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.PaymentGatewayDto;
import com.neowallet.neowalletbackend.exception.PayementStripeRazorpayException;
import com.neowallet.neowalletbackend.model.PaymentAttempt;
import com.neowallet.neowalletbackend.model.PaymentGateway;
import com.neowallet.neowalletbackend.model.PaymentStatus;
import com.neowallet.neowalletbackend.model.Transaction;
import com.neowallet.neowalletbackend.repository.PaymentAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final PaymentAttemptRepository paymentAttemptRepository;

    public PaymentServiceImpl(PaymentAttemptRepository paymentAttemptRepository) {
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    @Override
    public PaymentGatewayDto paymentAttempt(Transaction transaction, PaymentGateway gateway) {
        PaymentAttempt attempt=new PaymentAttempt();
        attempt.setTransaction(transaction);
        attempt.setCreatedAt(LocalDateTime.now());
        if (gateway==PaymentGateway.MOCK_BANK){
            attempt.setGateway(PaymentGateway.MOCK_BANK);
            attempt.setStatus(PaymentStatus.CAPTURED);//directly success for mock
            attempt.setRawResponse("Transaction Success");
        }
        else{
            throw new PayementStripeRazorpayException("We are currently not compatible with Razorypay and Stripe");
        }
        attempt.setGatewayId("MOCK");
        paymentAttemptRepository.save(attempt);
        if (attempt.getStatus()==PaymentStatus.CAPTURED){
          return new PaymentGatewayDto(
                  attempt.getPaymentAttemptId(),
                  "success",
                  attempt.getGatewayId()
          );
        }
        else {
            return new PaymentGatewayDto(
                    attempt.getPaymentAttemptId(),
                    "failed",
                    attempt.getGatewayId()
            );
        }
    }
}
