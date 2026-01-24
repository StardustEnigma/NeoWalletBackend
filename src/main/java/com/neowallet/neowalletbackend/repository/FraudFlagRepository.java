package com.neowallet.neowalletbackend.repository;

import com.neowallet.neowalletbackend.model.FraudFlag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudFlagRepository extends JpaRepository<FraudFlag,Long> {
}
