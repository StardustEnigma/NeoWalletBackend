package com.neowallet.neowalletbackend.repository;

import com.neowallet.neowalletbackend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
}
