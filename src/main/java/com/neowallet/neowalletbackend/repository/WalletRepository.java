package com.neowallet.neowalletbackend.repository;

import com.neowallet.neowalletbackend.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet,Long> {
}
