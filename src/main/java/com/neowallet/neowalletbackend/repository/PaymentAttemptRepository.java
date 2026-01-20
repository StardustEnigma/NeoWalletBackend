package com.neowallet.neowalletbackend.repository;

import com.neowallet.neowalletbackend.model.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt,Long> {
}
