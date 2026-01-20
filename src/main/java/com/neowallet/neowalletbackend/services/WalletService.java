package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.PaymentGatewayDto;
import com.neowallet.neowalletbackend.model.PaymentGateway;
import com.neowallet.neowalletbackend.model.User;
import com.neowallet.neowalletbackend.model.Wallet;

import java.math.BigDecimal;

public interface WalletService {

    PaymentGatewayDto addMoneyWallet(Long userId, BigDecimal amount, String idempotencyKey, PaymentGateway gateway);
    void transfer(Long fromUserId,Long toUserId,BigDecimal amount,String idempotencyKey);
    public Wallet confirmTransaction(Long paymentAttemptId);
}
