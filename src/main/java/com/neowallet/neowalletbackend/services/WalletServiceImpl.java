package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.PaymentGatewayDto;
import com.neowallet.neowalletbackend.exception.PaymentFailedException;
import com.neowallet.neowalletbackend.exception.WalletBlockedException;
import com.neowallet.neowalletbackend.model.*;
import com.neowallet.neowalletbackend.repository.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PaymentService paymentService;
    private final PaymentAttemptRepository paymentAttemptRepository;

    public WalletServiceImpl(WalletRepository walletRepository, IdempotencyRepository idempotencyRepository, UserRepository userRepository, TransactionRepository transactionRepository, LedgerEntryRepository ledgerEntryRepository, PaymentService paymentService, PaymentAttemptRepository paymentAttemptRepository) {
        this.walletRepository = walletRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.paymentService = paymentService;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    @Override
    @Transactional
    public PaymentGatewayDto addMoneyWallet(Long userId, BigDecimal amount, String idempotencyKey, PaymentGateway gateway) {
        User user=userRepository.findById(userId).orElseThrow(()->
                new UsernameNotFoundException("userId does not exist"));
        Wallet wallet=walletRepository.findByUser_UserId(userId).
                orElseThrow(()->new RuntimeException("Something went wrong!!"));


        if(wallet.getStatus()== WalletStatus.BLOCKED){
            throw new WalletBlockedException("Account is under Review !! please wait for some time");
        }
        if(idempotencyRepository.existsByIdempotencyKey(idempotencyKey)){
            throw new RuntimeException("Duplicate request");
        }
        Transaction transaction=new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.ADD_MONEY);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        PaymentGatewayDto paymentGateway = paymentService.paymentAttempt(transaction,gateway);
        if (paymentGateway.getStatus().equals("failed")){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new PaymentFailedException("Payment failed plz try again in sometime");
        }

        IdempotencyKey key=new IdempotencyKey();
        key.setIdempotencyKey(idempotencyKey);
        key.setRequestHash("ADD_MONEY_"+userId+"_"+amount);
        key.setCreatedAt(LocalDateTime.now());
        key.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        idempotencyRepository.save(key);

        return paymentGateway;
    }

    @Override
    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount, String idempotencyKey) {

    }

    @Transactional
    @Override
    public Wallet confirmTransaction(Long paymentAttemptId) {
        PaymentAttempt attempt=paymentAttemptRepository.
                findById(paymentAttemptId).orElseThrow(
                        ()-> new RuntimeException("Payment attempt not found"));

        if (attempt.getStatus() != PaymentStatus.CAPTURED){
            throw new RuntimeException("Payment not successful,cannot confirm transaction");
        }
        Transaction transaction=attempt.getTransaction();

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING){
            throw new RuntimeException("Transaction already processed");
        }
        Wallet wallet=transaction.getWallet();
        BigDecimal newBalance=wallet.getBalance().add(transaction.getAmount());

        LedgerEntry ledgerEntry=new LedgerEntry();
        ledgerEntry.setWallet(wallet);
        ledgerEntry.setTransaction(transaction);
        ledgerEntry.setAmount(transaction.getAmount());
        ledgerEntry.setLedgerType(LedgerType.CREDIT);
        ledgerEntry.setBalanceAfter(newBalance);
        ledgerEntry.setCreatedAt(LocalDateTime.now());

        ledgerEntryRepository.save(ledgerEntry);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
        return wallet;
    }
}
