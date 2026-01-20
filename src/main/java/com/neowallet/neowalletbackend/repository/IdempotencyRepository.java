package com.neowallet.neowalletbackend.repository;

import com.neowallet.neowalletbackend.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey,Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
