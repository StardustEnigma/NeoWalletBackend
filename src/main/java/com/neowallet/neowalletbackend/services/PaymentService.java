package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.PaymentGatewayDto;
import com.neowallet.neowalletbackend.model.PaymentGateway;
import com.neowallet.neowalletbackend.model.Transaction;


public interface PaymentService {
    PaymentGatewayDto paymentAttempt(Transaction transaction, PaymentGateway gateway);


}
