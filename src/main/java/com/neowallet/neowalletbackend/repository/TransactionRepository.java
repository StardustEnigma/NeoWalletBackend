package com.neowallet.neowalletbackend.repository;

import com.neowallet.neowalletbackend.model.Transaction;
import com.neowallet.neowalletbackend.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    int countByWalletAndCreatedAtAfter(Wallet wallet, LocalDateTime time);

}
