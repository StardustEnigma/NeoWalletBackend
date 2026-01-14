package com.neowallet.neowalletbackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FraudFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fraudId;

    @ManyToOne
    @JoinColumn(name = "transactionId",nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    private String ruleTriggered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudFlagStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
