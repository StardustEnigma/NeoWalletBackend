package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.model.*;
import com.neowallet.neowalletbackend.repository.FraudFlagRepository;
import com.neowallet.neowalletbackend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;

    public FraudDetectionServiceImpl(
            FraudFlagRepository fraudFlagRepository,
            TransactionRepository transactionRepository
    ) {
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void checkAndFlag(Transaction transaction) {

        List<FraudFlag> flags = new ArrayList<>();

        Wallet wallet = transaction.getWallet();
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        int recentTxnCount =
                transactionRepository.countByWalletAndCreatedAtAfter(
                        wallet, fiveMinutesAgo
                );

        // Rule 1: High amount
        if (transaction.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            flags.add(buildFlag(
                    transaction,
                    FraudRules.HIGH_AMOUNT,
                    Severity.HIGH
            ));
        }

        // Rule 2: Transaction velocity
        if (recentTxnCount >= 5) {
            flags.add(buildFlag(
                    transaction,
                    FraudRules.TXN_VELOCITY,
                    Severity.HIGH
            ));
        }

        if (!flags.isEmpty()) {
            fraudFlagRepository.saveAll(flags);
        }
    }

    private FraudFlag buildFlag(
            Transaction transaction,
            String rule,
            Severity severity
    ) {
        FraudFlag flag = new FraudFlag();
        flag.setTransaction(transaction);
        flag.setRuleTriggered(rule);
        flag.setSeverity(severity);
        flag.setStatus(FraudFlagStatus.OPEN);
        flag.setCreatedAt(LocalDateTime.now());
        return flag;
    }
}
