package com.neowallet.neowalletbackend.repository;

import com.neowallet.neowalletbackend.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry,Long> {
}
