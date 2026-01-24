package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.model.Transaction;

public interface FraudDetectionService {
    void checkAndFlag(Transaction transaction);
}
